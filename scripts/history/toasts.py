"""Bundle audited AOSP toast artwork for an explicit in-app recreation."""

import json
import re
from pathlib import Path

from .aosp import SourceCache, sha256
from .bundle import format_xml
from .notices import collect_resource_notices, preserve_notices

PROJECT = Path(__file__).resolve().parents[2]
OUTPUT = PROJECT
PLANS = [
    ("classic", "android-2.3.7_r1", "3f2821425f1ab6eddb76a8725e3f2c3edb5d8b07"),
    ("holo", "android-4.4.4_r2", "63ade05d76785975fc3292ca030abbaa1dda8891"),
    ("material1", "android-5.0.2_r1", "0ac9664a9ee2e6d1d3b68fe00c264ad94fe98966"),
    ("material2", "android-9.0.0_r1", "6549309f6c473b792ec62d1aebadec62bcf07827"),
    ("material3", "android-12.0.0_r1", "cebf5c06997b64f4e47a1611edb5f97044509d76"),
    ("expressive", "android-16.0.0_r1", "99b01a65cc4c104933788b3143285ab6bae65827"),
]


def main():
    releases = []
    for index, (family, release, commit) in enumerate(PLANS):
        cache = SourceCache(PROJECT / ".local", "platform/frameworks/base", commit)
        old = index < 3
        resource_root = cache.resources() if old else None
        prefix = f"aosp_toast_{family}_"
        records = []
        source_paths = []

        def write(source_path, destination, adapted=None):
            original = (
                (resource_root / source_path.removeprefix("core/res/res/")).read_bytes()
                if old
                else cache.file(source_path)
            )
            if original is None:
                raise ValueError(f"Missing original resource {source_path}")
            data = adapted(original.decode("utf-8")).encode("utf-8") if adapted else original
            target = OUTPUT / destination
            target.parent.mkdir(parents=True, exist_ok=True)
            target.write_bytes(data)
            source_paths.append(source_path)
            record = {
                "sourcePath": source_path,
                "url": cache.url(source_path),
                "sourceSha256": sha256(original),
                "destination": destination,
                "bundledSha256": sha256(data),
                "adapted": adapted is not None,
            }
            if source_path.endswith(".xml"):
                record["originalXml"] = original.decode("utf-8")
            records.append(record)

        if old:
            for path in sorted(resource_root.glob("drawable*/toast_frame.9.png")):
                write(
                    "core/res/res/" + path.relative_to(resource_root).as_posix(),
                    "app/src/main/res/" + path.parent.name + "/" + prefix + "frame.9.png",
                )
        else:

            def frame(text):
                # Keep original corner geometry; dynamic library palettes are bound explicitly at runtime.
                text = re.sub(r"\?android:attr/(colorBackground|colorSurface)", "#FFFAFAFA", text)
                return format_xml(text)

            write(
                "core/res/res/drawable/toast_frame.xml",
                "app/src/main/res/drawable/" + prefix + "frame.xml",
                frame,
            )

        def layout(text):
            text = re.sub(
                r'android:layout_(width|height)="match_parent"',
                r'android:layout_\1="wrap_content"',
                text,
            )
            text = re.sub(
                r'android:background="[^"]+"', f'android:background="@drawable/{prefix}frame"', text
            )
            text = re.sub(
                r'android:textAppearance="[^"]+"',
                f'android:textAppearance="@style/AospToast{family.title()}Text"',
                text,
            )
            text = re.sub(
                r'android:textColor="[^"]+"',
                'android:textColor="' + ("#FFFFFFFF" if old else "#DE000000") + '"',
                text,
            )
            text = text.replace("@+id/icon", "@+id/aosp_toast_icon").replace(
                "@+id/text", "@+id/aosp_toast_text"
            )
            text = re.sub(r'android:maxWidth="[^"]+"', "", text)
            text = re.sub(r'android:layout_weight="[^"]+"', "", text)
            text = text.replace(
                "<ImageView",
                '<ImageView android:importantForAccessibility="no" android:contentDescription="@null"',
            )
            text = re.sub(r'android:elevation="[^"]+"', 'android:elevation="4dp"', text)
            # The host applies the source line height on old APIs without using private framework IDs.
            text = re.sub(r'android:lineHeight="[^"]+"', "", text)
            text = text.replace(
                'android:layout_marginHorizontal="24dp"',
                'android:layout_marginStart="24dp" android:layout_marginEnd="24dp"',
            )
            text = text.replace(
                'android:layout_marginVertical="15dp"',
                'android:layout_marginTop="15dp" android:layout_marginBottom="15dp"',
            )
            return format_xml(text)

        source_layout = (
            "packages/SystemUI/res/layout/text_toast.xml"
            if index >= 4
            else "core/res/res/layout/transient_notification.xml"
        )
        write(source_layout, "app/src/main/res/layout/" + prefix + "layout.xml", layout)
        notices = preserve_notices(
            cache, OUTPUT / "licenses/aosp-resources" / release, source_paths, resource_root
        )
        releases.append(
            {
                "family": family,
                "release": release,
                "commit": commit,
                "evidence": "RESOURCE_RECREATION",
                "originalCapture": "MISSING",
                "interactionEngine": "Componentory in-app popup; no system Toast",
                "hashScope": "Source and bundled resource bytes before Android resource compilation",
                "adaptations": [
                    "App-owned resource IDs, dimensions, and text appearance",
                    "App-owned 16sp text size and explicit font bindings",
                    "Wrap-content panel preview and in-app popup instead of a system window",
                    "Modern surface and text colors follow the selected library palette",
                    "Source line height is applied by the host on Android 12 artwork",
                    "Private elevation and width become app-owned 4dp and 320dp constraints",
                    "Remove unused layout weights and mark the app icon as decorative",
                ],
                "files": records,
                "notices": notices,
            }
        )
    values = [
        '<?xml version="1.0" encoding="utf-8"?>',
        "<!-- Readable AOSP toast text bindings. Original layouts and source hashes are bundled. -->",
        "<resources>",
    ]
    for family, _, _ in PLANS:
        font = "sans-serif-condensed" if family in ("holo", "material1") else "sans-serif"
        values += [
            f'    <style name="AospToast{family.title()}Text" parent="@android:style/TextAppearance">',
            '        <item name="android:textSize">16sp</item>',
            f'        <item name="android:fontFamily">{font}</item>',
            "    </style>",
        ]
    values += ["</resources>"]
    path = OUTPUT / "app/src/main/res/values/aosp_toast_text.xml"
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text("\n".join(values) + "\n", encoding="utf-8", newline="\n")
    path = OUTPUT / "app/src/main/assets/aosp-resources/toasts.json"
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(releases, indent=2) + "\n", encoding="utf-8", newline="\n")
    collect_resource_notices(PROJECT)
    print(
        f"Bundled {len(releases)} toast releases and {sum(len(item['files']) for item in releases)} original variants."
    )


if __name__ == "__main__":
    main()
