$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'test-evidence.ps1')
. (Join-Path $PSScriptRoot 'test-locale.ps1')
$fixtureDirectory = Join-Path (Split-Path -Parent $PSScriptRoot) ('.local/locale-script-tests/' + [Guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $fixtureDirectory -Force | Out-Null
$statePath = Join-Path $fixtureDirectory 'state.json'
$fakeAdb = Join-Path $fixtureDirectory 'fake-adb.ps1'
# Fixture output is never a claim about a physical-device run.
@'
$statePath = Join-Path $PSScriptRoot 'state.json'
$state = Get-Content -LiteralPath $statePath -Raw | ConvertFrom-Json
if ($args -contains 'set-app-locales') {
    if ($state.mode -eq 'set-failed') { Write-Output 'Fixture set failure'; exit 1 }
    $tagsIndex = [Array]::IndexOf([string[]]$args, '--locales')
    $state.tags = if ($tagsIndex -ge 0) { $args[$tagsIndex + 1] } else { '' }
    if ($state.mode -eq 'mismatch') { $state.tags = 'ja' }
    $state.setUser = $args[[Array]::IndexOf([string[]]$args, '--user') + 1]
    $state | ConvertTo-Json | Set-Content -LiteralPath $statePath -Encoding utf8
    exit 0
}
if ($state.mode -eq 'get-failed') { Write-Output 'Fixture query failure'; exit 1 }
if ($state.mode -eq 'unknown') { Write-Output 'Unrecognized fixture output'; exit 0 }
Write-Output "Locales for xyz.gaon.componentory for user $($state.userId) are [$($state.tags)]"
exit 0
'@ | Set-Content -LiteralPath $fakeAdb -Encoding utf8
$checks = 0

function Set-LocaleFixture {
    param([string]$Tags, [string]$Mode = 'normal')
    [ordered]@{ userId = '10'; tags = $Tags; mode = $Mode; setUser = $null } |
        ConvertTo-Json | Set-Content -LiteralPath $statePath -Encoding utf8
}

function Assert-Locale {
    param([bool]$Condition, [string]$Message)
    if (-not $Condition) { throw $Message }
}

function Assert-LocaleFailure {
    param([scriptblock]$Action, [string]$Message)
    $failed = $false
    try { & $Action | Out-Null } catch { $failed = $true }
    Assert-Locale $failed $Message
}

foreach ($tags in @('', 'ko,en-US')) {
    Set-LocaleFixture -Tags $tags
    $original = Read-DeviceTestAppLocale -AdbPath $fakeAdb -Device 'fixture'
    Assert-Locale ($original.userId -eq '10' -and $original.tags -ceq $tags) 'The original user or ordered locale tags were lost.'
    $checks++
    Set-LocaleFixture -Tags 'en'
    Restore-DeviceTestAppLocale -AdbPath $fakeAdb -Device 'fixture' -Snapshot $original
    $saved = Get-Content -LiteralPath $statePath -Raw | ConvertFrom-Json
    Assert-Locale ($saved.tags -ceq $tags -and $saved.setUser -eq '10') 'Restoration did not recover the exact locale for the original user.'
    $checks++
}
foreach ($mode in @('get-failed', 'unknown')) {
    Set-LocaleFixture -Tags 'en' -Mode $mode
    Assert-LocaleFailure { Read-DeviceTestAppLocale -AdbPath $fakeAdb -Device 'fixture' } 'A failed or unrecognized query must stop capture.'
    $checks++
}
foreach ($mode in @('set-failed', 'mismatch')) {
    Set-LocaleFixture -Tags 'en' -Mode $mode
    Assert-LocaleFailure { Restore-DeviceTestAppLocale -AdbPath $fakeAdb -Device 'fixture' -Snapshot $original } 'A failed or incorrect restore must be reported.'
    $checks++
}
foreach ($invalid in @(@{ userId = 'other'; tags = 'en' }, @{ userId = '10'; tags = 'en;invalid' }, @{ userId = '10' })) {
    Assert-LocaleFailure { Restore-DeviceTestAppLocale -AdbPath $fakeAdb -Device 'fixture' -Snapshot $invalid } 'Malformed recovery data must not be used as command arguments.'
    $checks++
}
Write-Output "Locale script checks passed: $checks"
