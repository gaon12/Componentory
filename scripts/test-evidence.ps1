function Read-DeviceTestSnapshot {
    param(
        [Parameter(Mandatory = $true)][string]$AdbPath,
        [Parameter(Mandatory = $true)][string]$Device,
        [Parameter(Mandatory = $true)][string[]]$Arguments,
        [string]$LinePattern
    )

    $output = @(& $AdbPath -s $Device shell @Arguments 2>&1)
    $exitCode = $LASTEXITCODE
    $message = ($output -join "`n").Trim()
    if ($exitCode -ne 0) {
        return [ordered]@{ available = $false; value = $null; nativeExitCode = $exitCode; error = $message }
    }
    if ($LinePattern) {
        $message = (@($output | Where-Object { "$_" -match $LinePattern }) -join "`n").Trim()
    }
    return [ordered]@{
        available = ($message.Length -gt 0)
        value = if ($message.Length -gt 0) { $message } else { $null }
        nativeExitCode = $exitCode
        error = if ($message.Length -gt 0) { $null } else { 'The query returned no matching data.' }
    }
}

function Test-DeviceKeyguardShowing {
    param([Parameter(Mandatory = $true)][string]$Policy)

    # Recent Samsung builds report the delegate's showing field instead of mIsShowing.
    $states = [regex]::Matches($Policy, '(?m)^\s*(?:mIsShowing|showing)\s*=\s*(true|false)\b')
    if ($states.Count -eq 0) { throw 'The device did not report a recognized keyguard state.' }
    return @($states | Where-Object { $_.Groups[1].Value -eq 'true' }).Count -gt 0
}

function Assert-DeviceTestScope {
    param([bool]$NoUi, [string]$TestClass)

    # Only reviewed tests without Activities or input may skip screen preparation.
    if ($NoUi -and $TestClass -cnotmatch '^xyz\.gaon\.componentory\.(?:icons\.IconCatalogResourceTest|catalog\.ComponentInventoryResourceTest|eastereggs\.EasterEggResourceTest|survivor\.GameArtworkTest)(?:#[A-Za-z_][A-Za-z0-9_]*)?$') {
        throw 'The -NoUi mode requires IconCatalogResourceTest, ComponentInventoryResourceTest, EasterEggResourceTest, or GameArtworkTest, optionally followed by #method. UI tests still require an unlocked screen.'
    }
}

function Save-DeviceTestEvidence {
    param([Parameter(Mandatory = $true)]$Evidence)

    $Evidence.Manifest | ConvertTo-Json -Depth 10 |
        Set-Content -LiteralPath (Join-Path $Evidence.Directory 'manifest.json') -Encoding utf8
}

function New-DeviceTestEvidence {
    param(
        [Parameter(Mandatory = $true)][string]$Directory,
        [Parameter(Mandatory = $true)][System.Collections.IDictionary]$Metadata
    )

    $started = [DateTime]::UtcNow
    $runId = $started.ToString('yyyyMMddTHHmmssfffZ') + '-' + [Guid]::NewGuid().ToString('N').Substring(0, 8)
    $runDirectory = Join-Path $Directory $runId
    New-Item -ItemType Directory -Path $runDirectory -Force | Out-Null
    $evidence = [pscustomobject]@{
        Directory = $runDirectory
        Manifest = [ordered]@{
            schemaVersion = 1
            runId = $runId
            startedAtUtc = $started.ToString('o')
            finishedAtUtc = $null
            status = 'running'
            metadata = $Metadata
            result = $null
            restorationErrors = @()
        }
    }
    Save-DeviceTestEvidence -Evidence $evidence
    return $evidence
}

function Complete-DeviceTestEvidence {
    param(
        [Parameter(Mandatory = $true)]$Evidence,
        [AllowEmptyCollection()][string[]]$Output,
        [Parameter(Mandatory = $true)][int]$ExitCode
    )

    $report = $Output -join "`n"
    Set-Content -LiteralPath (Join-Path $Evidence.Directory 'instrumentation.txt') -Value $report -Encoding utf8
    # A successful transport or a stale OK line cannot override a reported failure.
    $passed = $ExitCode -eq 0 -and $report -match 'OK \([1-9][0-9]* tests?\)' -and
        $report -notmatch 'FAILURES!!!|INSTRUMENTATION_FAILED|INSTRUMENTATION_ABORTED|shortMsg='
    $summary = [regex]::Match($report, '(?m)^OK \([1-9][0-9]* tests?\)|^Tests run:.*$').Value.Trim()
    $Evidence.Manifest['finishedAtUtc'] = [DateTime]::UtcNow.ToString('o')
    $Evidence.Manifest['status'] = if ($passed) { 'passed' } else { 'failed' }
    $Evidence.Manifest['result'] = [ordered]@{
        nativeExitCode = $ExitCode
        summary = $summary
        report = 'instrumentation.txt'
    }
    Save-DeviceTestEvidence -Evidence $Evidence
    return $passed
}
