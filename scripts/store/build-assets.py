"""Render and validate Google Play listing artwork without changing app captures."""

import argparse
import hashlib
import io
import json
import xml.etree.ElementTree as ET
from pathlib import Path

import resvg_py
from PIL import Image

ROOT = Path(__file__).resolve().parents[2]
STORE = ROOT / "distribution/google-play"


def render() -> None:
    output = STORE / "assets"
    output.mkdir(parents=True, exist_ok=True)
    icon = (ROOT / "design/app-icon.svg").read_text(encoding="utf-8")
    # Play supplies its own outer mask. Preserve the owner's artwork inside it.
    icon = icon.replace(
        'x="64" y="64" width="896" height="896" rx="224"', 'width="1024" height="1024"'
    )
    data = resvg_py.svg_to_bytes(svg_string=icon, width=512, height=512)
    with Image.open(io.BytesIO(data)) as image:
        image.convert("RGBA").save(output / "icon.png", optimize=True)
    alt_text = {
        "icon.png": {
            "en-US": "Flask with cyan liquid and a purple cube",
            "ko-KR": "청록색 액체가 담긴 플라스크와 보라색 정육면체",
            "ja-JP": "シアン色の液体が入ったフラスコと紫の立方体",
            "zh-CN": "装有青色液体的烧瓶和紫色立方体",
            "zh-TW": "裝有青色液體的燒瓶與紫色立方體",
        }
    }
    for source in sorted((STORE / "graphics").glob("feature-*.svg")):
        data = resvg_py.svg_to_bytes(svg_path=str(source))
        with Image.open(io.BytesIO(data)) as image:
            image.convert("RGB").save(output / f"{source.stem}.png", optimize=True)
        description = ET.parse(source).find("{http://www.w3.org/2000/svg}desc")
        if description is None or not description.text:
            raise ValueError(f"Missing feature graphic description: {source.name}")
        alt_text[f"{source.stem}.png"] = {source.stem.removeprefix("feature-"): description.text}
    (output / "alt-text.json").write_text(
        json.dumps(alt_text, ensure_ascii=False, indent=2) + "\n", encoding="utf-8"
    )


def validate() -> None:
    errors = []
    locales = {"ko-KR", "en-US", "ja-JP", "zh-CN", "zh-TW"}
    actual_locales = {path.name for path in (STORE / "listings").iterdir() if path.is_dir()}
    if actual_locales != locales:
        errors.append("Provide all five supported listing languages.")
    limits = {
        "title.txt": 30,
        "short-description.txt": 80,
        "full-description.txt": 4000,
        "release-notes.txt": 500,
    }
    for folder in sorted((STORE / "listings").iterdir()):
        for name, limit in limits.items():
            text = (folder / name).read_text(encoding="utf-8").strip()
            if not text or len(text) > limit:
                errors.append(f"{folder.name}/{name}: {len(text)} characters, limit {limit}")
    assets = {path.name for path in (STORE / "assets").glob("*.png")}
    expected_assets = {"icon.png"} | {f"feature-{locale}.png" for locale in locales}
    if assets != expected_assets:
        errors.append("Provide one icon and all five feature graphics.")
    alt_text = json.loads((STORE / "assets/alt-text.json").read_text(encoding="utf-8"))
    if set(alt_text) != expected_assets:
        errors.append("Provide alt text for every store artwork file.")
    if any(not text or len(text) > 140 for values in alt_text.values() for text in values.values()):
        errors.append("Artwork alt text must contain 1 to 140 characters.")
    for path in sorted((STORE / "assets").glob("*.png")):
        with Image.open(path) as image:
            expected = ((512, 512), "RGBA") if path.name == "icon.png" else ((1024, 500), "RGB")
            if (image.size, image.mode) != expected:
                errors.append(
                    f"{path.name}: expected {expected}, received {(image.size, image.mode)}"
                )
            if path.name == "icon.png" and path.stat().st_size > 1024 * 1024:
                errors.append("The store icon exceeds 1024 KB.")
    screenshot_folders = [path for path in (STORE / "screenshots").glob("*") if path.is_dir()]
    if not {"ko-KR", "en-US"}.issubset({path.name for path in screenshot_folders}):
        errors.append("Provide genuine Korean and English phone screenshots.")
    paths = set()
    for folder in sorted(screenshot_folders):
        screenshots = sorted(folder.glob("*.png"))
        if not 4 <= len(screenshots) <= 8:
            errors.append(f"{folder.name}: provide four to eight phone screenshots")
        for path in screenshots:
            paths.add(path.relative_to(STORE / "screenshots").as_posix())
            with Image.open(path) as image:
                width, height = image.size
                if image.mode != "RGB" or (width, height) != (1080, 1920):
                    errors.append(f"{path.name}: expected 1080 x 1920 RGB")
    manifest_path = STORE / "screenshots/manifest.json"
    if not manifest_path.is_file():
        errors.append("Provide the real screenshot capture manifest.")
    else:
        manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
        if {entry["path"] for entry in manifest["captures"]} != paths:
            errors.append("Screenshot paths do not match the capture manifest.")
        for entry in manifest["captures"]:
            path = STORE / "screenshots" / entry["path"]
            if (
                not path.is_file()
                or hashlib.sha256(path.read_bytes()).hexdigest() != entry["sha256"]
            ):
                errors.append(f"Capture hash mismatch: {entry['path']}")
            if not entry["altText"] or len(entry["altText"]) > 140:
                errors.append(f"Invalid screenshot alt text: {entry['path']}")
    if errors:
        raise SystemExit("\n".join(errors))
    print("Listing text lengths, artwork dimensions, image modes, and screenshot counts passed.")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--check-only", action="store_true")
    args = parser.parse_args()
    if not args.check_only:
        render()
    validate()
