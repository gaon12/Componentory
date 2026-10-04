param(
    [Parameter(Mandatory = $true)]
    [string]$Device,
    [string]$AdbPath,
    [string]$TestClass,
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

    # Prepare the screen after installation so it cannot time out during APK transfer.
    $windowPolicy = (& $AdbPath -s $Device shell dumpsys window policy) -join "`n"
    if ($windowPolicy -match 'screenState=SCREEN_STATE_OFF') {
        & $AdbPath -s $Device shell input keyevent KEYCODE_POWER
    }
    else {
        & $AdbPath -s $Device shell input keyevent KEYCODE_WAKEUP
    }
    # Wait for a ready screen, then allow the dismissal request to finish.
    for ($attempt = 0; $attempt -lt 10; $attempt++) {
        $windowPolicy = (& $AdbPath -s $Device shell dumpsys window policy) -join "`n"
        if ($windowPolicy -match 'screenState=SCREEN_STATE_ON' -and $windowPolicy -match 'interactiveState=INTERACTIVE_STATE_AWAKE') { break }
        Start-Sleep -Milliseconds 500
    }
    if ($windowPolicy -notmatch 'screenState=SCREEN_STATE_ON' -or $windowPolicy -notmatch 'interactiveState=INTERACTIVE_STATE_AWAKE') {
        throw 'The selected screen did not wake. Turn it on before running touch tests.'
    }
    & $AdbPath -s $Device shell wm dismiss-keyguard
    for ($attempt = 0; $attempt -lt 10; $attempt++) {
        $windowPolicy = (& $AdbPath -s $Device shell dumpsys window policy) -join "`n"
        if ($windowPolicy -notmatch 'mIsShowing=true') { break }
        Start-Sleep -Milliseconds 500
    }
    if ($windowPolicy -match 'mIsShowing=true') {
        throw 'Unlock the selected device before running touch tests.'
    }

    New-Item -ItemType Directory -Path '.local' -Force | Out-Null
    $instrumentationArguments = @('-s', $Device, 'shell', 'am', 'instrument', '-w', '-r')
    if ($TestClass) { $instrumentationArguments += @('-e', 'class', $TestClass) }
    $instrumentationArguments += 'xyz.gaon.componentory.test/xyz.gaon.componentory.testing.ComponentoryTestRunner'
    $testOutput = & $AdbPath @instrumentationArguments 2>&1
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
