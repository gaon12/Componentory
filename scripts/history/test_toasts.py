"""Validate original toast artwork, adaptations, and complete retained terms."""

import hashlib
import json
import unittest
import xml.etree.ElementTree as ET
from pathlib import Path

PROJECT = Path(__file__).resolve().parents[2]
ANDROID = "{http://schemas.android.com/apk/res/android}"


class ToastResourcesTests(unittest.TestCase):
    def releases(self):
        return json.loads(
            (PROJECT / "app/src/main/assets/aosp-resources/toasts.json").read_text(encoding="utf-8")
        )

    def test_all_six_releases_preserve_original_and_bundled_hashes(self):
        releases = self.releases()
        self.assertEqual(
            ["classic", "holo", "material1", "material2", "material3", "expressive"],
            [r["family"] for r in releases],
        )
        self.assertEqual(22, sum(len(r["files"]) for r in releases))
        for release in releases:
            self.assertEqual("MISSING", release["originalCapture"])
            self.assertEqual("RESOURCE_RECREATION", release["evidence"])
            self.assertIn("no system Toast", release["interactionEngine"])
            for record in release["files"]:
                target = (PROJECT / record["destination"]).resolve()
                self.assertTrue(target.is_relative_to(PROJECT.resolve()))
                digest = hashlib.sha256(target.read_bytes()).hexdigest()
                self.assertEqual(record["bundledSha256"], digest)
                self.assertIn(release["commit"], record["url"])
                if record["adapted"]:
                    ET.parse(target)
                    self.assertEqual(
                        record["sourceSha256"],
                        hashlib.sha256(record["originalXml"].encode()).hexdigest(),
                    )
                    self.assertIn(
                        "Licensed under the Apache License", target.read_text(encoding="utf-8")
                    )
                else:
                    self.assertEqual(record["sourceSha256"], digest)

    def test_geometry_and_identical_upstream_artwork_are_not_invented(self):
        releases = {r["family"]: r for r in self.releases()}

        def mdpi(family):
            return next(
                r["sourceSha256"]
                for r in releases[family]["files"]
                if "/drawable-mdpi/" in r["sourcePath"]
            )

        self.assertEqual(mdpi("holo"), mdpi("material1"))
        self.assertNotEqual(mdpi("classic"), mdpi("holo"))
        for family, corner in [
            ("material2", "22dp"),
            ("material3", "28dp"),
            ("expressive", "28dp"),
        ]:
            root = ET.parse(
                PROJECT / f"app/src/main/res/drawable/aosp_toast_{family}_frame.xml"
            ).getroot()
            self.assertEqual(corner, root.find("corners").get(ANDROID + "radius"))
        for family in ("material3", "expressive"):
            root = ET.parse(
                PROJECT / f"app/src/main/res/layout/aosp_toast_{family}_layout.xml"
            ).getroot()
            self.assertEqual("24dp", root.find("ImageView").get(ANDROID + "layout_width"))
            self.assertEqual("2", root.find("TextView").get(ANDROID + "maxLines"))

    def test_every_original_ancestor_notice_is_readable_offline(self):
        combined = (PROJECT / "licenses/aosp-resources-NOTICE.txt").read_text(encoding="utf-8")
        for release in self.releases():
            self.assertEqual("Apache-2.0", release["notices"]["declaredModuleLicense"])
            for record in release["notices"]["files"]:
                data = (
                    PROJECT / "licenses/aosp-resources" / release["release"] / record["path"]
                ).read_bytes()
                self.assertEqual(record["sha256"], hashlib.sha256(data).hexdigest())
                self.assertIn(data.decode("utf-8").rstrip(), combined)
                self.assertIn(f"{release['release']} / {record['path']}", combined)


if __name__ == "__main__":
    unittest.main()
