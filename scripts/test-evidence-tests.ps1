$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'test-evidence.ps1')
$testDirectory = Join-Path (Split-Path -Parent $PSScriptRoot) '.local/evidence-script-tests'
$metadata = [ordered]@{
    source = [ordered]@{ revision = 'fixture'; dirty = $true; changes = @(' M app/Fixture.kt') }
    testScope = 'example.Test#focusedMethod'
    apkIdentities = @([ordered]@{ path = 'fixture.apk'; sha256 = 'fixture-hash' })
    deviceProperties = [ordered]@{ 'ro.product.model' = 'Fixture device' }
}
$checks = 0

function Assert-Evidence {
    param([bool]$Condition, [string]$Message)
    if (-not $Condition) { throw $Message }
}

$first = New-DeviceTestEvidence -Directory $testDirectory -Metadata $metadata
$initial = Get-Content -LiteralPath (Join-Path $first.Directory 'manifest.json') -Raw | ConvertFrom-Json
Assert-Evidence ($initial.status -eq 'running' -and $null -eq $initial.finishedAtUtc) 'A new run must be recorded before it finishes.'
Assert-Evidence ($initial.metadata.source.dirty -and $initial.metadata.testScope -eq 'example.Test#focusedMethod') 'Source and focused scope were lost.'
$checks++

$passed = Complete-DeviceTestEvidence -Evidence $first -Output @('Time: 2.0', 'OK (2 tests)') -ExitCode 0
$saved = Get-Content -LiteralPath (Join-Path $first.Directory 'manifest.json') -Raw | ConvertFrom-Json
Assert-Evidence ($passed -and $saved.status -eq 'passed' -and $saved.finishedAtUtc) 'A successful result was not saved.'
Assert-Evidence ($saved.metadata.apkIdentities[0].sha256 -eq 'fixture-hash') 'Artifact identity was lost.'
$firstReport = Get-Content -LiteralPath (Join-Path $first.Directory 'instrumentation.txt') -Raw
$checks++

$cases = @(
    @{ Output = @('FAILURES!!!', 'Tests run: 2, Failures: 1'); ExitCode = 0 },
    @{ Output = @('OK (2 tests)'); ExitCode = 1 },
    @{ Output = @('INSTRUMENTATION_RESULT: shortMsg=Process crashed'); ExitCode = 0 },
    @{ Output = @('OK (2 tests)', 'INSTRUMENTATION_ABORTED'); ExitCode = 0 },
    @{ Output = @('INSTRUMENTATION_STATUS: numtests=2'); ExitCode = 0 }
)
foreach ($case in $cases) {
    $run = New-DeviceTestEvidence -Directory $testDirectory -Metadata $metadata
    $result = Complete-DeviceTestEvidence -Evidence $run -Output $case.Output -ExitCode $case.ExitCode
    $saved = Get-Content -LiteralPath (Join-Path $run.Directory 'manifest.json') -Raw | ConvertFrom-Json
    Assert-Evidence (-not $result -and $saved.status -eq 'failed') 'A failed or incomplete run was marked as passing.'
    Assert-Evidence ($run.Directory -ne $first.Directory) 'Two runs reused the same directory.'
    $checks++
}

$lastRun = New-DeviceTestEvidence -Directory $testDirectory -Metadata $metadata
$single = Complete-DeviceTestEvidence -Evidence $lastRun -Output @('OK (1 test)') -ExitCode 0
Assert-Evidence $single 'A focused single-test summary must be accepted.'
$lastRun.Manifest['restorationErrors'] += 'animator_duration_scale'
Save-DeviceTestEvidence -Evidence $lastRun
$saved = Get-Content -LiteralPath (Join-Path $lastRun.Directory 'manifest.json') -Raw | ConvertFrom-Json
Assert-Evidence ($saved.restorationErrors[0] -eq 'animator_duration_scale') 'Setting restoration failure was lost.'
Assert-Evidence ((Get-Content -LiteralPath (Join-Path $first.Directory 'instrumentation.txt') -Raw) -eq $firstReport) 'A later run overwrote the first report.'
$checks++

$emptyRun = New-DeviceTestEvidence -Directory $testDirectory -Metadata $metadata
$empty = Complete-DeviceTestEvidence -Evidence $emptyRun -Output @() -ExitCode 0
Assert-Evidence (-not $empty -and (Test-Path -LiteralPath (Join-Path $emptyRun.Directory 'instrumentation.txt'))) 'An empty result must leave a failed run and its report file.'
$checks++

# This fake transport checks metadata handling without claiming device evidence.
$fakeAdb = Join-Path $testDirectory 'fake-adb.ps1'
@'
if ($args[-1] -eq 'fail') {
    Write-Output 'Fixture transport failure'
    exit 1
}
Write-Output 'Fixture model'
Write-Output 'versionName=fixture'
exit 0
'@ | Set-Content -LiteralPath $fakeAdb -Encoding utf8
$snapshot = Read-DeviceTestSnapshot -AdbPath $fakeAdb -Device 'fixture' -Arguments @('success') -LinePattern 'versionName='
Assert-Evidence ($snapshot.available -and $snapshot.value -eq 'versionName=fixture' -and $snapshot.nativeExitCode -eq 0) 'A successful filtered snapshot was lost.'
$checks++
$snapshot = Read-DeviceTestSnapshot -AdbPath $fakeAdb -Device 'fixture' -Arguments @('fail')
Assert-Evidence (-not $snapshot.available -and $null -eq $snapshot.value -and $snapshot.nativeExitCode -eq 1 -and $snapshot.error -eq 'Fixture transport failure') 'A failed query must not become valid metadata.'
$checks++
$snapshot = Read-DeviceTestSnapshot -AdbPath $fakeAdb -Device 'fixture' -Arguments @('success') -LinePattern '^missing='
Assert-Evidence (-not $snapshot.available -and $null -eq $snapshot.value -and $snapshot.error) 'Missing metadata must be explicit.'
$checks++

$resourceTest = 'xyz.gaon.componentory.icons.IconCatalogResourceTest'
Assert-DeviceTestScope -NoUi $true -TestClass $resourceTest
Assert-DeviceTestScope -NoUi $true -TestClass ($resourceTest + '#selectedLookupsMatchCatalogEntriesAndKeepFallbacksWithinTheirSource')
Assert-DeviceTestScope -NoUi $false -TestClass 'xyz.gaon.componentory.icons.IconBrowserTest'
$checks += 3
$inventoryTest = 'xyz.gaon.componentory.catalog.ComponentInventoryResourceTest'
Assert-DeviceTestScope -NoUi $true -TestClass $inventoryTest
Assert-DeviceTestScope -NoUi $true -TestClass ($inventoryTest + '#packagedInventoryRetainsAuditedSourcesAndStatuses')
$checks += 2
$eggTest = 'xyz.gaon.componentory.eastereggs.EasterEggResourceTest'
Assert-DeviceTestScope -NoUi $true -TestClass $eggTest
Assert-DeviceTestScope -NoUi $true -TestClass ($eggTest + '#realNonogramRasterCluesAndPlayerMarksSurviveAnAndroidParcelRoundTrip')
$checks += 2
foreach ($invalidScope in @('', 'xyz.gaon.componentory.icons.IconBrowserTest', 'xyz.gaon.componentory.eastereggs.EasterEggCatalogUiTest', ($eggTest + 'Extra'), ($resourceTest + ',other.Test'), $resourceTest.ToLowerInvariant(), ($resourceTest + '#bad method'), ($resourceTest + ';invalid'), ($resourceTest + ',' + $inventoryTest), $inventoryTest.ToLowerInvariant(), ($inventoryTest + 'Extra'))) {
    $rejected = $false
    try { Assert-DeviceTestScope -NoUi $true -TestClass $invalidScope } catch { $rejected = $true }
    Assert-Evidence $rejected 'Resource-only mode must reject missing, interactive, or malformed scopes.'
    $checks++
}

$keyguardCases = @(
    @{ Policy = '  mIsShowing=true'; Showing = $true },
    @{ Policy = "  KeyguardServiceDelegate`n    showing=true`n    deviceHasKeyguard=true"; Showing = $true },
    @{ Policy = '  mIsShowing=false'; Showing = $false },
    @{ Policy = "  KeyguardServiceDelegate`n    showing=false`n    deviceHasKeyguard=true"; Showing = $false },
    @{ Policy = "  mIsShowing=false`n    showing=true"; Showing = $true }
)
foreach ($case in $keyguardCases) {
    Assert-Evidence ((Test-DeviceKeyguardShowing -Policy $case.Policy) -eq $case.Showing) 'The reported keyguard state was not recognized.'
    $checks++
}
foreach ($unknownPolicy in @('deviceHasKeyguard=true', 'anotherShowing=true', 'showing=trueish')) {
    $rejected = $false
    try { Test-DeviceKeyguardShowing -Policy $unknownPolicy | Out-Null } catch { $rejected = $true }
    Assert-Evidence $rejected 'Missing or malformed keyguard state must not be treated as unlocked.'
    $checks++
}

Write-Output "Evidence script checks passed: $checks"
