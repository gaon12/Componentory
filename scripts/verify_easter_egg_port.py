"""Verify imported game bytes, source attribution, and Android component boundaries."""

import argparse
import hashlib
import json
import subprocess
import xml.etree.ElementTree as ET
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
PORT = ROOT / "eastereggs"
ANDROID = "{http://schemas.android.com/apk/res/android}"


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def verify(upstream=None):
    metadata = json.loads((PORT / "provenance.json").read_text(encoding="utf-8"))
    paths = set()
    resources = 0
    if upstream:
        revision = subprocess.run(
            ["git", "-C", str(upstream), "rev-parse", "HEAD"],
            check=True,
            capture_output=True,
            text=True,
        ).stdout.strip()
        assert revision == metadata["revision"], "Upstream revision does not match the pin."
    for record in metadata["files"]:
        relative = record["path"]
        path = (PORT / relative).resolve()
        assert path.is_relative_to(PORT.resolve()), "Imported path leaves the module."
        assert relative not in paths, f"Duplicate import: {relative}"
        paths.add(relative)
        assert digest(path) == record["importedSha256"], f"Changed import: {relative}"
        if record["sourceSha256"] != record["importedSha256"]:
            assert record["adaptations"], f"Unrecorded adaptation: {relative}"
        if relative.startswith("src/main/res/"):
            resources += 1
            assert record["sourceSha256"] == record["importedSha256"], relative
        if upstream:
            source = (upstream / record["sourcePath"]).resolve()
            assert source.is_relative_to(upstream.resolve()), "Source path leaves the checkout."
            assert digest(source) == record["sourceSha256"], f"Changed source: {source}"
    assert "LICENSE" in paths
    for path in (PORT / "src/main").rglob("*"):
        if path.is_file() and path.name != "AndroidManifest.xml":
            assert path.relative_to(PORT).as_posix() in paths, f"Unrecorded source: {path}"
    manifest_path = PORT / "src/main/AndroidManifest.xml"
    assert digest(manifest_path) == metadata["generatedManifestSha256"]
    manifest = ET.parse(manifest_path).getroot()
    assert not any(
        p.get(ANDROID + "name") == "android.permission.INTERNET"
        for p in manifest.findall("uses-permission")
    )
    declared = {}
    for element in manifest.find("application"):
        if element.tag not in {"activity", "service", "receiver"}:
            continue
        name = element.get(ANDROID + "name")
        assert name not in declared, f"Duplicate component: {name}"
        declared[name] = element.tag
        if element.tag == "activity":
            assert element.get(ANDROID + "exported") == "false", name
        if element.tag == "service":
            assert element.get(ANDROID + "permission", "").startswith("android.permission.BIND_"), (
                name
            )
    expected = {row["className"]: row["type"] for row in metadata["components"]}
    assert declared == expected, "Imported component declarations differ from the inventory."
    return {
        "retainedFiles": len(paths),
        "unchangedResources": resources,
        "components": len(declared),
        "upstreamRevision": metadata["revision"],
        "upstreamBytesChecked": upstream is not None,
    }


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--upstream", type=Path, help="Optional pinned upstream checkout")
    arguments = parser.parse_args()
    print(json.dumps(verify(arguments.upstream), indent=2))
