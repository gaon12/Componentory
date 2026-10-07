"""Vendor a small, audited resource closure for current-OS recreations."""

import json
import re
import xml.etree.ElementTree as ET
from pathlib import Path

from .aosp import SourceCache, sha256
from .notices import collect_resource_notices, preserve_notices
from .resources import ResourceIndex

RESOURCE_REFERENCE = re.compile(
    r"@(?:\*?android:)?(drawable|color|dimen|integer|animator|anim|interpolator)/([\w.]+)"
)
THEME_REFERENCE = re.compile(r"\?(?:\*?android:)?attr/([\w]+)")
PUBLIC_ATTRIBUTES = {
    "colorAccent",
    "colorButtonNormal",
    "colorControlActivated",
    "colorControlHighlight",
    "colorControlNormal",
    "colorForeground",
    "disabledAlpha",
    "textColorPrimary",
    "textColorSecondary",
    "textColorHint",
}
PRIVATE_ATTRIBUTES = {"colorSwitchThumbNormal": "color_switch_thumb_normal"}


def adapted_xml(text: str, prefix: str) -> str:
    """Keep public widget layer IDs, but namespace private state IDs and references."""
    text = RESOURCE_REFERENCE.sub(lambda m: f"@{m[1]}/{prefix}{m[2]}", text)
    text = text.replace("@+android:id/", "@android:id/")
    text = re.sub(
        r"@\+?id/(background|progress|secondaryProgress)\b",
        lambda m: f"@android:id/{m[1]}",
        text,
    )
    declared_ids = set()

    def private_id(match):
        name = match[1]
        declaration = "+" if name not in declared_ids else ""
        declared_ids.add(name)
        return f"@{declaration}id/{prefix}{name}"

    text = re.sub(r"@\+?id/([\w]+)", private_id, text)
    if "<animated-selector" in text:
        item_ids = re.findall(r'<item[^>]*android:id="([^"]+)"', text)
        normalized_ids = [name.replace("@+id/", "@id/") for name in item_ids]
        if len(normalized_ids) != len(set(normalized_ids)):
            # AOSP reuses an animation state for disabled and unchecked items.
            text = text.replace(
                "<animated-selector",
                "<!-- Disabled and unchecked items intentionally share an AOSP animation state. -->\n"
                '<animated-selector xmlns:tools="http://schemas.android.com/tools" tools:ignore="DuplicateIds"',
                1,
            )

    def attribute(match):
        name = match[1]
        if name in PRIVATE_ATTRIBUTES:
            return f"?attr/{prefix}{PRIVATE_ATTRIBUTES[name]}"
        if name not in PUBLIC_ATTRIBUTES:
            raise ValueError(f"A private attribute needs an explicit adaptation: {name}")
        return f"?android:attr/{name}"

    return THEME_REFERENCE.sub(attribute, text)


def format_xml(text: str) -> str:
    # Keep upstream copyright comments while normalizing copied XML whitespace.
    return "\n".join(line.rstrip() for line in text.splitlines()).rstrip() + "\n"


def bundle_controls(plan: dict, cache_directory: Path, project: Path) -> dict:
    cache = SourceCache(cache_directory, "platform/frameworks/base", plan["commit"])
    resources = cache.resources()
    index = ResourceIndex(resources)
    prefix = f"aosp_{plan['family']}_"
    pending = [f"drawable/{name}" for name in plan["drawables"]]
    palette = {}
    for attribute in plan.get("palette", []):
        bindings = index.attribute_bindings(plan["theme"], attribute)
        if len(bindings) != 1:
            raise ValueError(f"Expected one {plan['theme']} binding for {attribute}: {bindings}")
        binding = bindings[0]
        palette[attribute] = binding
        pending.extend(f"{m[1]}/{m[2]}" for m in RESOURCE_REFERENCE.finditer(binding["value"]))

    seen = set()
    value_files = {}
    records = []
    while pending:
        reference = pending.pop()
        if reference in seen:
            continue
        seen.add(reference)
        definitions = index.definitions.get(reference)
        if not definitions:
            raise ValueError(f"Missing upstream resource: {reference}")
        for definition in definitions:
            source = resources / definition.path
            original = source.read_bytes()
            if definition.element is not None:
                element = ET.fromstring(ET.tostring(definition.element))
                text = ET.tostring(element, encoding="unicode")
                element.set("name", prefix + element.get("name"))
                adapted = adapted_xml(ET.tostring(element, encoding="unicode"), prefix)
                directory = definition.path.split("/")[0]
                destination = f"app/src/main/res/{directory}/{prefix}values.xml"
                value_files.setdefault(destination, {})[reference] = adapted
            else:
                filename = prefix + source.name
                destination = f"app/src/main/res/{source.parent.name}/{filename}"
                target = project / destination
                target.parent.mkdir(parents=True, exist_ok=True)
                text = original.decode("utf-8") if source.suffix == ".xml" else ""
                if text:
                    target.write_text(
                        format_xml(adapted_xml(text, prefix)), encoding="utf-8", newline="\n"
                    )
                else:
                    target.write_bytes(original)
            pending.extend(f"{m[1]}/{m[2]}" for m in RESOURCE_REFERENCE.finditer(text))
            record = {
                "resource": reference,
                "sourcePath": f"core/res/res/{definition.path}",
                "url": cache.url(f"core/res/res/{definition.path}"),
                "sourceSha256": sha256(original),
                "destination": destination,
                "adapted": source.suffix == ".xml",
            }
            if definition.element is not None:
                record["sourceDeclaration"] = text
            records.append(record)
    header = """<?xml version="1.0" encoding="utf-8"?>
<!--
Copyright The Android Open Source Project.
Licensed under the Apache License, Version 2.0. See licenses/Apache-2.0.txt.
Adapted for Componentory: resource names and references are prefixed.
Original declarations, source hashes, and release notices are retained.
-->
<resources xmlns:android="http://schemas.android.com/apk/res/android">
"""
    for destination, declarations in value_files.items():
        (project / destination).write_text(
            header + "\n".join(declarations.values()) + "\n</resources>\n",
            encoding="utf-8",
            newline="\n",
        )
    if palette:
        root = ET.Element("resources")
        style = ET.SubElement(root, "style", {"name": "AospMaterial1Colors", "parent": ""})
        for attribute, binding in palette.items():
            name = f"android:{attribute}"
            if attribute in PRIVATE_ATTRIBUTES:
                name = prefix + PRIVATE_ATTRIBUTES[attribute]
                ET.SubElement(root, "attr", {"name": name, "format": "color|reference"})
            ET.SubElement(style, "item", {"name": name}).text = adapted_xml(
                binding["value"], prefix
            )
        ET.indent(root, space="    ")
        destination = "app/src/main/res/values/aosp_material1_palette.xml"
        (project / destination).write_text(
            '<?xml version="1.0" encoding="utf-8"?>\n<!-- AOSP Theme.Material.Light color bindings; see the bundled provenance. -->\n'
            + ET.tostring(root, encoding="unicode")
            + "\n",
            encoding="utf-8",
            newline="\n",
        )
    for record in records:
        record["bundledSha256"] = sha256((project / record["destination"]).read_bytes())
    notices = preserve_notices(
        cache,
        project / "licenses/aosp-resources" / plan["release"],
        [record["sourcePath"] for record in records],
        resources,
    )
    return {
        **plan,
        "evidence": "RESOURCE_RECREATION",
        "originalCapture": "MISSING",
        "interactionEngine": "Installed Android OS; not historical OS behavior",
        "adaptations": [
            "Prefixed resource names and private references",
            "Theme colors bound to release values; private attributes use app-owned names",
            "Sample text has a 16sp readability floor",
        ],
        "paletteBindings": palette,
        "files": sorted(records, key=lambda r: (r["resource"], r["sourcePath"])),
        "notices": notices,
    }


def main():
    project = Path(__file__).resolve().parents[2]
    plans = json.loads((project / "data/aosp-resources/controls.json").read_text(encoding="utf-8"))
    result = [bundle_controls(plan, project / ".local", project) for plan in plans]
    destination = project / "app/src/main/assets/aosp-resources/controls.json"
    destination.parent.mkdir(parents=True, exist_ok=True)
    destination.write_text(json.dumps(result, indent=2) + "\n", encoding="utf-8", newline="\n")
    collect_resource_notices(project)
    print(
        f"Bundled {sum(len(r['files']) for r in result)} resource variants from {len(result)} releases."
    )


if __name__ == "__main__":
    main()
