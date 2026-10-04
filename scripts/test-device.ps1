param(
    [Parameter(Mandatory = $true)]
    [string]$Device,
    [string]$AdbPath,
    [string]$TestClass,
    [switch]$SkipBuild
)

$ErrorActionPreference = 'Stop'
$projectDirectory = Split-Path -Parent $PSScriptRoot
$originalAnimationSettings = [ordered]@{}
$originalAppLocale = $null
$testEvidence = $null
. (Join-Path $PSScriptRoot 'test-evidence.ps1')
. (Join-Path $PSScriptRoot 'test-locale.ps1')
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

    & $AdbPath -s $Device install --no-streaming -r app/build/outputs/apk/debug/app-debug.apk
    if ($LASTEXITCODE -ne 0) { throw 'App installation failed.' }
    & $AdbPath -s $Device install --no-streaming -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
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
    # Espresso needs stationary window coordinates when touching native popups.
    foreach ($setting in @('window_animation_scale', 'transition_animation_scale', 'animator_duration_scale')) {
        $value = & $AdbPath -s $Device shell settings get global $setting
        if ($LASTEXITCODE -ne 0) { throw "Could not read $setting." }
        $originalAnimationSettings[$setting] = ($value -join '').Trim()
    }
    # Keep a recovery record if the host process is forcibly terminated.
    $originalAnimationSettings | ConvertTo-Json | Set-Content -LiteralPath '.local/device-animation-settings.json' -Encoding utf8

    # Keep the source, installed APK identities, and environment with each result.
    $sourceRevision = ((& git rev-parse HEAD) -join '').Trim()
    if ($LASTEXITCODE -ne 0) { throw 'Could not read the source revision.' }
    $sourceChanges = @(& git status --porcelain)
    if ($LASTEXITCODE -ne 0) { throw 'Could not read the working tree state.' }
    $apkIdentities = foreach ($apk in @('app/build/outputs/apk/debug/app-debug.apk', 'app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk')) {
        [ordered]@{ path = $apk; sha256 = (Get-FileHash -LiteralPath $apk -Algorithm SHA256).Hash.ToLowerInvariant() }
    }
    $deviceProperties = [ordered]@{}
    foreach ($property in @('ro.product.manufacturer', 'ro.product.model', 'ro.product.device', 'ro.build.version.release', 'ro.build.version.sdk', 'ro.build.id', 'ro.build.fingerprint', 'persist.sys.locale')) {
        $value = & $AdbPath -s $Device shell getprop $property
        if ($LASTEXITCODE -ne 0) { throw "Could not read device property $property." }
        $deviceProperties[$property] = ($value -join '').Trim()
    }
    if ([int]$deviceProperties['ro.build.version.sdk'] -ge 33) {
        $originalAppLocale = Read-DeviceTestAppLocale -AdbPath $AdbPath -Device $Device
        $originalAppLocale | ConvertTo-Json -Depth 5 | Set-Content -LiteralPath '.local/device-app-locale.json' -Encoding utf8
    }
    $providerVersions = [ordered]@{}
    $versionCatalog = Get-Content -LiteralPath 'gradle/libs.versions.toml' -Raw
    foreach ($dependency in @('composeMaterial2', 'composeMaterial3', 'composeMaterialIcons')) {
        $version = [regex]::Match($versionCatalog, '(?m)^' + $dependency + '\s*=\s*"([^"]+)"\s*$')
        $providerVersions[$dependency] = if ($version.Success) { $version.Groups[1].Value } else { $null }
    }
    $testEvidence = New-DeviceTestEvidence -Directory '.local/device-runs' -Metadata ([ordered]@{
        source = [ordered]@{ revision = $sourceRevision; dirty = ($sourceChanges.Count -gt 0); changes = $sourceChanges }
        apkIdentities = @($apkIdentities)
        device = $Device
        deviceProperties = $deviceProperties
        displaySize = Read-DeviceTestSnapshot -AdbPath $AdbPath -Device $Device -Arguments @('wm', 'size')
        displayDensity = Read-DeviceTestSnapshot -AdbPath $AdbPath -Device $Device -Arguments @('wm', 'density')
        fontScale = Read-DeviceTestSnapshot -AdbPath $AdbPath -Device $Device -Arguments @('settings', 'get', 'system', 'font_scale')
        logicalDisplayBeforeTests = Read-DeviceTestSnapshot -AdbPath $AdbPath -Device $Device -Arguments @('dumpsys', 'display') -LinePattern '^\s*m(?:Base|Override)DisplayInfo=DisplayInfo.*displayId 0,'
        windowStateBeforeTests = Read-DeviceTestSnapshot -AdbPath $AdbPath -Device $Device -Arguments @('dumpsys', 'window', 'displays') -LinePattern 'cur=|app=|mRotation=|mCurrentFocus|mFocusedApp'
        installedAppIdentity = Read-DeviceTestSnapshot -AdbPath $AdbPath -Device $Device -Arguments @('dumpsys', 'package', 'xyz.gaon.componentory') -LinePattern 'versionCode=|versionName='
        appLocaleBeforeTests = if ($originalAppLocale) {
            $originalAppLocale.query
        } else { [ordered]@{ available = $false; value = $null; nativeExitCode = $null; error = 'LocaleManager unavailable below API 33.' } }
        appLocaleRecovery = if ($originalAppLocale) { $originalAppLocale } else { 'Host locale recovery is unavailable below API 33; the runner restores it on normal finish.' }
        pinnedSourceProviderVersions = $providerVersions
        sampleThemeConfiguration = [ordered]@{
            CLASSIC = 'android:Theme.Light'
            HOLO = 'android:Theme.Holo.Light'
            MATERIAL = 'android:Theme.Material.Light'
            MATERIAL2 = 'lightColors'
            MATERIAL3 = 'lightColorScheme'
        }
        testScope = if ($TestClass) { $TestClass } else { 'All instrumentation tests' }
        skippedBuild = [bool]$SkipBuild
        originalAnimations = $originalAnimationSettings
        requestedTestAnimations = 0
        perTestOverride = 'NativeProgressIndicatorsTest uses animator scale 1.0 when included.'
        captures = @()
    })
    foreach ($setting in $originalAnimationSettings.Keys) {
        & $AdbPath -s $Device shell settings put global $setting 0
        if ($LASTEXITCODE -ne 0) { throw "Could not disable $setting for testing." }
    }
    $instrumentationArguments = @('-s', $Device, 'shell', 'am', 'instrument', '-w', '-r')
    if ($originalAppLocale) { $instrumentationArguments += @('--user', $originalAppLocale.userId) }
    if ($TestClass) { $instrumentationArguments += @('-e', 'class', $TestClass) }
    $instrumentationArguments += 'xyz.gaon.componentory.test/xyz.gaon.componentory.testing.ComponentoryTestRunner'
    $testOutput = & $AdbPath @instrumentationArguments 2>&1
    $testExitCode = $LASTEXITCODE
    $testOutput | Set-Content -LiteralPath '.local/device-tests.txt' -Encoding utf8
    $passed = Complete-DeviceTestEvidence -Evidence $testEvidence -Output $testOutput -ExitCode $testExitCode
    $testOutput | Where-Object { $_ -match '^INSTRUMENTATION_STATUS: test=|^Time:|^OK \(|^Tests run:|^FAILURES!!!|^INSTRUMENTATION_FAILED|^INSTRUMENTATION_RESULT: shortMsg=' }
    # ADB can exit successfully even when the instrumentation reports failed tests.
    Write-Output "Saved test evidence: $($testEvidence.Directory)"
    if (-not $passed) {
        throw "Device tests did not pass. See $($testEvidence.Directory)/instrumentation.txt."
    }
}
catch {
    if ($testEvidence -and $testEvidence.Manifest['status'] -eq 'running') {
        $testEvidence.Manifest['status'] = 'failed'
        $testEvidence.Manifest['finishedAtUtc'] = [DateTime]::UtcNow.ToString('o')
        $testEvidence.Manifest['result'] = [ordered]@{ error = $_.Exception.Message }
    }
    throw
}
finally {
    if ($originalAppLocale) {
        try {
            Restore-DeviceTestAppLocale -AdbPath $AdbPath -Device $Device -Snapshot $originalAppLocale
        }
        catch {
            if ($testEvidence) { $testEvidence.Manifest['restorationErrors'] += 'app_locale' }
            Write-Warning "App locale restoration failed: $($_.Exception.Message) Original values are in .local/device-app-locale.json."
        }
    }
    foreach ($setting in $originalAnimationSettings.Keys) {
        $value = $originalAnimationSettings[$setting]
        if ($value -eq 'null') {
            & $AdbPath -s $Device shell settings delete global $setting | Out-Null
        }
        else {
            & $AdbPath -s $Device shell settings put global $setting $value
        }
        if ($LASTEXITCODE -ne 0) {
            if ($testEvidence) { $testEvidence.Manifest['restorationErrors'] += $setting }
            Write-Warning "Could not restore $setting. Original values are in .local/device-animation-settings.json."
        }
    }
    if ($testEvidence) { Save-DeviceTestEvidence -Evidence $testEvidence }
    Pop-Location
}
