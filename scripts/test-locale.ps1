function Read-DeviceTestAppLocale {
    param(
        [Parameter(Mandatory = $true)][string]$AdbPath,
        [Parameter(Mandatory = $true)][string]$Device,
        [string]$UserId
    )

    $arguments = @('cmd', 'locale', 'get-app-locales', 'xyz.gaon.componentory')
    if ($UserId) { $arguments += @('--user', $UserId) }
    $snapshot = Read-DeviceTestSnapshot -AdbPath $AdbPath -Device $Device -Arguments $arguments
    if (-not $snapshot.available) { throw "Could not capture the app locale: $($snapshot.error)" }
    $localeMatch = [regex]::Match($snapshot.value, '^Locales for xyz\.gaon\.componentory for user ([0-9]+) are \[([^\]]*)\]$')
    if (-not $localeMatch.Success) { throw 'The app locale response was not recognized.' }
    $languageTags = $localeMatch.Groups[2].Value
    if ($languageTags -and $languageTags -notmatch '^[A-Za-z0-9-]+(?:,[A-Za-z0-9-]+)*$') {
        throw 'The app locale response contains unexpected language tags.'
    }
    return [ordered]@{
        userId = $localeMatch.Groups[1].Value
        tags = $languageTags
        query = $snapshot
    }
}

function Restore-DeviceTestAppLocale {
    param(
        [Parameter(Mandatory = $true)][string]$AdbPath,
        [Parameter(Mandatory = $true)][string]$Device,
        [Parameter(Mandatory = $true)]$Snapshot
    )

    if ($Snapshot.tags -isnot [string] -or "$($Snapshot.userId)" -notmatch '^[0-9]+$' -or
        ($Snapshot.tags -and "$($Snapshot.tags)" -notmatch '^[A-Za-z0-9-]+(?:,[A-Za-z0-9-]+)*$')) {
        throw 'The saved app locale is not valid for restoration.'
    }
    $arguments = @('-s', $Device, 'shell', 'cmd', 'locale', 'set-app-locales', 'xyz.gaon.componentory', '--user', $Snapshot.userId)
    # Omitting --locales restores System without passing an empty shell argument.
    if ($Snapshot.tags) { $arguments += @('--locales', $Snapshot.tags) }
    $output = @(& $AdbPath @arguments 2>&1)
    if ($LASTEXITCODE -ne 0) { throw "Could not restore the app locale: $($output -join ' ')" }
    $restored = Read-DeviceTestAppLocale -AdbPath $AdbPath -Device $Device -UserId $Snapshot.userId
    if ($restored.userId -ne $Snapshot.userId -or $restored.tags -cne $Snapshot.tags) {
        throw 'The app locale did not match its original value after restoration.'
    }
}
