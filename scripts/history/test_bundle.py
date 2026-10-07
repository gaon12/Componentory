"""Check the shipped graphics, provenance, and namespace adaptations."""

import hashlib
import json
import unittest
import xml.etree.ElementTree as ET
from pathlib import Path

from .bundle import adapted_xml

PROJECT = Path(__file__).resolve().parents[2]


class BundleTests(unittest.TestCase):
    def test_native_progress_layer_ids_survive_namespacing(self):
        source = '<item id="@id/progress" src="@android:drawable/frame" next="@id/checked" />'
        result = adapted_xml(source, "aosp_test_")
        self.assertIn("@android:id/progress", result)
        self.assertIn("@drawable/aosp_test_frame", result)
        self.assertIn("@+id/aosp_test_checked", result)
        self.assertNotIn("@+android:id/", adapted_xml("@+android:id/background", "aosp_test_"))
        self.assertEqual(
            "@interpolator/aosp_test_ease",
            adapted_xml("@android:interpolator/ease", "aosp_test_"),
        )
        self.assertEqual(
            "@+id/aosp_test_off @id/aosp_test_off",
            adapted_xml("@id/off @id/off", "aosp_test_"),
        )

    def test_private_theme_attributes_cannot_silently_use_current_os_values(self):
        with self.assertRaisesRegex(ValueError, "explicit adaptation"):
            adapted_xml("?attr/privateFrameworkColor", "aosp_test_")
        self.assertEqual(
            "?android:attr/colorControlNormal",
            adapted_xml("?attr/colorControlNormal", "aosp_test_"),
        )

    def test_all_bundled_sources_match_the_recorded_hashes_and_terms(self):
        releases = json.loads(
            (PROJECT / "app/src/main/assets/aosp-resources/controls.json").read_text(
                encoding="utf-8"
            )
        )
        self.assertEqual(["classic", "holo", "material1"], [r["family"] for r in releases])
        for release in releases:
            self.assertEqual("RESOURCE_RECREATION", release["evidence"])
            self.assertEqual("MISSING", release["originalCapture"])
            self.assertIn("Installed Android OS", release["interactionEngine"])
            self.assertEqual(40, len(release["commit"]))
            for record in release["files"]:
                target = PROJECT / record["destination"]
                self.assertTrue(target.resolve().is_relative_to(PROJECT.resolve()))
                digest = hashlib.sha256(target.read_bytes()).hexdigest()
                self.assertEqual(record["bundledSha256"], digest, str(target))
                if not record["adapted"]:
                    self.assertEqual(record["sourceSha256"], digest, str(target))
                else:
                    ET.parse(target)
                self.assertIn(release["commit"], record["url"])
            terms = release["notices"]
            self.assertEqual("Apache-2.0", terms["declaredModuleLicense"])
            for record in terms["files"]:
                target = PROJECT / "licenses/aosp-resources" / release["release"] / record["path"]
                self.assertEqual(record["sha256"], hashlib.sha256(target.read_bytes()).hexdigest())


if __name__ == "__main__":
    unittest.main()
