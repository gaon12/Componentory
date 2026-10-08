"""Check the real source artwork linked by the survival game's manifest."""

import hashlib
import json
from pathlib import Path


def verify(root: Path) -> int:
    manifest = json.loads(
        (root / "app/src/main/assets/survivor/resources.json").read_text(encoding="utf-8")
    )
    checked = 0
    for key, asset in manifest["assets"].items():
        assert asset["files"], f"{key}: no source files"
        for source in asset["files"]:
            path = root / source["path"]
            digest = hashlib.sha256(path.read_bytes()).hexdigest()
            assert digest == source["bundledSha256"], f"{key}: artwork changed: {path}"
            assert len(source["revision"]) == 40, f"{key}: unpinned source"
            assert len(source["sourceSha256"]) == 64, f"{key}: missing original hash"
            checked += 1
    return checked


if __name__ == "__main__":
    print(f"Verified {verify(Path(__file__).resolve().parents[1])} source files.")
