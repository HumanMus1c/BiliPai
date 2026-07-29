[CmdletBinding()]
param(
    [string]$RepoRoot
)

$ErrorActionPreference = "Stop"
if ([string]::IsNullOrWhiteSpace($RepoRoot)) {
    $RepoRoot = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
}
$audit = Join-Path $RepoRoot "scripts/ui_architecture_audit.ps1"
$probeRoot = Join-Path ([IO.Path]::GetTempPath()) ("bilipai-ui-audit-" + [guid]::NewGuid().ToString("N"))

function Assert-AuditRejectsProbe {
    param(
        [string]$Name,
        [string]$Source,
        [string]$ExpectedPattern,
        [switch]$DesignSystemBuild
    )

    $extension = if ($DesignSystemBuild) { ".gradle.kts" } else { ".kt" }
    $probe = Join-Path $probeRoot ("$Name$extension")
    $Source | Set-Content -LiteralPath $probe -Encoding utf8

    $auditArguments = @("-NoProfile", "-File", $audit, "-RepoRoot", $RepoRoot)
    if ($DesignSystemBuild) {
        $auditArguments += @("-SyntheticDesignSystemBuildFile", $probe)
    } else {
        $auditArguments += @("-SyntheticFeatureFile", $probe)
    }

    $ErrorActionPreference = "Continue"
    $output = & powershell @auditArguments 2>&1
    $exitCode = $LASTEXITCODE
    $ErrorActionPreference = "Stop"
    $text = $output -join [Environment]::NewLine
    if ($exitCode -eq 0) {
        throw "$Name probe unexpectedly passed.`n$text"
    }
    if ($text -notmatch $ExpectedPattern) {
        throw "$Name probe failed without the expected gate error '$ExpectedPattern'.`n$text"
    }
    Write-Output ("SELF_TEST_{0}_OK" -f $Name.ToUpperInvariant())
}

try {
    New-Item -ItemType Directory -Path $probeRoot | Out-Null
    Assert-AuditRejectsProbe -Name "style" -ExpectedPattern 'style_feature=1 exceeds baseline maximum 0' -Source @"
package com.android.purebilibili.feature.auditprobe

val illegalStyle = UiStyle.IOS
"@

    Assert-AuditRejectsProbe -Name "local" -ExpectedPattern 'local_feature=1 exceeds baseline maximum 0' -Source @"
package com.android.purebilibili.feature.auditprobe

val illegalLocal = LocalUiPreset.current
"@

    Assert-AuditRejectsProbe -Name "renderer" -ExpectedPattern 'renderer_feature=1 exceeds baseline maximum 0' -Source @"
package com.android.purebilibili.feature.auditprobe

import com.android.purebilibili.core.ui.renderer.material3.Material3ButtonRenderer
"@

    Assert-AuditRejectsProbe -Name "ios" -ExpectedPattern 'ios_feature_callers=1 exceeds baseline maximum 0' -Source @"
package com.android.purebilibili.feature.auditprobe

fun illegalIos() {
    IOSGroup()
}
"@

    $baseline = Get-Content -Raw -LiteralPath (Join-Path $RepoRoot "docs/UI_ARCHITECTURE_BASELINE.json") | ConvertFrom-Json
    $vendorExpected = [int]$baseline.gates.vendor_component_feature_max + 1
    Assert-AuditRejectsProbe -Name "vendor" -ExpectedPattern "vendor_component_feature=$vendorExpected exceeds baseline maximum $($baseline.gates.vendor_component_feature_max)" -Source @"
package com.android.purebilibili.feature.auditprobe

import androidx.compose.material3.Button

fun illegalVendor() {
    Button(onClick = {}) {}
}
"@

    Assert-AuditRejectsProbe -Name "design_system_gradle" -DesignSystemBuild -ExpectedPattern 'design_system_boundary_violations=1 exceeds baseline maximum 0' -Source @"
dependencies {
    implementation(project(":app"))
}
"@

    $cleanOutput = & powershell -NoProfile -File $audit -RepoRoot $RepoRoot 2>&1
    $cleanOutput | Write-Output
    if ($LASTEXITCODE -ne 0 -or $cleanOutput -notcontains "AUDIT_OK") {
        throw "Clean audit did not recover to AUDIT_OK."
    }
    Write-Output "SELF_TEST_CLEANUP_OK"
}
finally {
    if (Test-Path -LiteralPath $probeRoot) {
        Remove-Item -LiteralPath $probeRoot -Recurse -Force
    }
}
