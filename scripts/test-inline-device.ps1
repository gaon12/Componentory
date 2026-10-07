param(
    [Parameter(Mandatory = $true)][string]$Device,
    [Parameter(Mandatory = $true)][string]$AdbPath,
    [switch]$SkipBuild
)

$ErrorActionPreference = 'Stop'
$projectDirectory = Split-Path -Parent $PSScriptRoot
$originalSettings = [ordered]@{}
$recovery = $null
Push-Location $projectDirectory

function Invoke-InlineAdb {
    param([string[]]$Arguments)
    if ($Arguments[0] -eq 'shell') {
        # ADB joins remote arguments into shell code. Preserve semicolon-separated IME subtypes.
        $quoted = foreach ($argument in $Arguments[1..($Arguments.Count - 1)]) { "'" + $argument.Replace("'", "'\''") + "'" }
        $output = @(& $AdbPath -s $Device shell ($quoted -join ' ') 2>&1)
    }
    else { $output = @(& $AdbPath -s $Device @Arguments 2>&1) }
    if ($LASTEXITCODE -ne 0) { throw "ADB failed: $($Arguments -join ' ')`n$($output -join "`n")" }
    return ($output -join "`n").Trim()
}

try {
    if (-not $SkipBuild) {
        & .\gradlew.bat spotlessApply spotlessCheck :app:lintDebug --max-workers=1 '-Dorg.gradle.jvmargs=-Xmx1024m -Dfile.encoding=UTF-8' '-Pkotlin.compiler.execution.strategy=in-process' --console=plain
        if ($LASTEXITCODE -ne 0) { throw 'Formatting or lint failed.' }
        & .\gradlew.bat :app:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest --max-workers=1 '-Dorg.gradle.jvmargs=-Xmx1024m -Dfile.encoding=UTF-8' '-Pkotlin.compiler.execution.strategy=in-process' --console=plain
        if ($LASTEXITCODE -ne 0) { throw 'Unit tests or APK builds failed.' }
    }
    if ([int](Invoke-InlineAdb @('shell', 'getprop', 'ro.build.version.sdk')) -lt 30) { throw 'The actual InlineContentView requires Android API 30 or later.' }
    # Install before enabling roles so Android can resolve both demo services.
    Invoke-InlineAdb @('install', '--no-streaming', '-r', 'app/build/outputs/apk/debug/app-debug.apk') | Out-Null
    foreach ($setting in @('enabled_input_methods', 'default_input_method', 'selected_input_method_subtype', 'input_methods_subtype_history', 'autofill_service')) {
        $originalSettings[$setting] = Invoke-InlineAdb @('shell', 'settings', 'get', 'secure', $setting)
    }
    New-Item -ItemType Directory -Path '.local' -Force | Out-Null
    $recoveryPath = Join-Path '.local' ('inline-settings-' + [DateTime]::UtcNow.ToString('yyyyMMddTHHmmssfffZ') + '.json')
    $recovery = [ordered]@{ device = $Device; original = $originalSettings; restored = [ordered]@{}; restorationErrors = @() }
    # Keep this file even when the test or host is interrupted.
    $recovery | ConvertTo-Json -Depth 5 | Set-Content -LiteralPath $recoveryPath -Encoding utf8
    $keyboard = 'xyz.gaon.componentory/.lab.inline.InlineDemoInputMethodService'
    Invoke-InlineAdb @('shell', 'ime', 'enable', $keyboard) | Out-Null
    Invoke-InlineAdb @('shell', 'ime', 'set', $keyboard) | Out-Null
    Invoke-InlineAdb @('shell', 'settings', 'put', 'secure', 'autofill_service', 'xyz.gaon.componentory/.lab.inline.InlineDemoAutofillService') | Out-Null
    & (Join-Path $PSScriptRoot 'test-device.ps1') -Device $Device -AdbPath $AdbPath -SkipBuild -TestClass 'xyz.gaon.componentory.lab.InlineContentSampleTest'
}
finally {
    # Restore every setting independently so one failure cannot prevent the others.
    foreach ($setting in @('default_input_method', 'enabled_input_methods', 'selected_input_method_subtype', 'input_methods_subtype_history', 'autofill_service')) {
        if (-not $originalSettings.Contains($setting)) { continue }
        try {
            $value = $originalSettings[$setting]
            if ($setting -eq 'default_input_method' -and $value -ne 'null') {
                Invoke-InlineAdb @('shell', 'ime', 'set', $value) | Out-Null
            }
            if ($value -eq 'null') { Invoke-InlineAdb @('shell', 'settings', 'delete', 'secure', $setting) | Out-Null }
            else { Invoke-InlineAdb @('shell', 'settings', 'put', 'secure', $setting, $value) | Out-Null }
            $restored = Invoke-InlineAdb @('shell', 'settings', 'get', 'secure', $setting)
            if ($restored -ne $value) { throw "Restored $setting does not match the original value." }
            if ($recovery) { $recovery.restored[$setting] = $restored }
        }
        catch {
            if ($recovery) { $recovery.restorationErrors += $setting }
            Write-Warning "Could not restore $setting. Recover the original value from $recoveryPath. $($_.Exception.Message)"
        }
    }
    if ($recovery) {
        $recovery | ConvertTo-Json -Depth 5 | Set-Content -LiteralPath $recoveryPath -Encoding utf8
        Write-Output "Saved inline role recovery: $recoveryPath"
    }
    Pop-Location
    if ($recovery -and $recovery.restorationErrors.Count -gt 0) { throw 'Inline demo role restoration failed. See the recovery record.' }
}
