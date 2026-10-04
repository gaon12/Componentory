param(
    [Parameter(Mandatory = $true)]
    [string]$Device,
    [string]$AdbPath,
    [switch]$SkipBuild
)

$ErrorActionPreference = 'Stop'
$projectDirectory = Split-Path -Parent $PSScriptRoot
Push-Location $projectDirectory

try {
    if (-not $AdbPath) {
        $sdkDirectory = $env:ANDROID_HOME
        if (-not $sdkDirectory) { $sdkDirectory = $env:ANDROID_SDK_ROOT }
        if (-not $sdkDirectory -and (Test-Path -LiteralPath 'local.properties')) {
            $sdkLine = Get-Content -LiteralPath 'local.properties' | Where-Object { $_ -match '^sdk.dir=' } | Select-Object -First 1
            if ($sdkLine) { $sdkDirectory = $sdkLine.Substring(8).Replace('\:', ':').Replace('\\', '\') }
        }
        if (-not $sdkDirectory) { throw 'Set ANDROID_HOME or pass -AdbPath.' }
        $AdbPath = Join-Path $sdkDirectory 'platform-tools/adb.exe'
    }
    if (-not (Test-Path -LiteralPath $AdbPath)) { throw "ADB was not found: $AdbPath" }

    $deviceState = & $AdbPath -s $Device get-state 2>&1
    if ($LASTEXITCODE -ne 0 -or ($deviceState -join '') -ne 'device') {
        throw 'The selected device is unavailable. Check adb devices -l.'
    }
    & $AdbPath -s $Device shell input keyevent KEYCODE_WAKEUP
    $windowPolicy = & $AdbPath -s $Device shell dumpsys window policy
    if ($windowPolicy -match 'mIsShowing=true') {
        throw 'Unlock the selected device before running touch tests.'
    }

    if (-not $SkipBuild) {
        # Keep verification phases ordered and avoid concurrent builds on a small machine.
        & .\gradlew.bat spotlessApply spotlessCheck :app:lintDebug --max-workers=1 '-Dorg.gradle.jvmargs=-Xmx1024m -Dfile.encoding=UTF-8' '-Pkotlin.compiler.execution.strategy=in-process' --console=plain
        if ($LASTEXITCODE -ne 0) { throw 'Formatting or lint failed.' }
        & .\gradlew.bat :app:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest --max-workers=1 '-Dorg.gradle.jvmargs=-Xmx1024m -Dfile.encoding=UTF-8' '-Pkotlin.compiler.execution.strategy=in-process' --console=plain
        if ($LASTEXITCODE -ne 0) { throw 'Unit tests or APK builds failed.' }
        & .\gradlew.bat --stop --console=plain
    }

    & $AdbPath -s $Device install -r app/build/outputs/apk/debug/app-debug.apk
    if ($LASTEXITCODE -ne 0) { throw 'App installation failed.' }
    & $AdbPath -s $Device install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
    if ($LASTEXITCODE -ne 0) { throw 'Test installation failed.' }

    New-Item -ItemType Directory -Path '.local' -Force | Out-Null
    $testOutput = & $AdbPath -s $Device shell am instrument -w -r xyz.gaon.componentory.test/androidx.test.runner.AndroidJUnitRunner 2>&1
    $testExitCode = $LASTEXITCODE
    $testOutput | Set-Content -LiteralPath '.local/device-tests.txt' -Encoding utf8
    $report = $testOutput -join "`n"
    $testOutput | Where-Object { $_ -match '^INSTRUMENTATION_STATUS: test=|^Time:|^OK \(|^Tests run:|^FAILURES!!!|^INSTRUMENTATION_FAILED|^INSTRUMENTATION_RESULT: shortMsg=' }
    # ADB can exit successfully even when the instrumentation reports failed tests.
    if ($testExitCode -ne 0 -or $report -notmatch 'OK \([1-9][0-9]* tests?\)' -or $report -match 'FAILURES!!!|INSTRUMENTATION_FAILED|INSTRUMENTATION_ABORTED|shortMsg=') {
        throw 'Device tests did not pass. See .local/device-tests.txt.'
    }
}
finally {
    Pop-Location
}
