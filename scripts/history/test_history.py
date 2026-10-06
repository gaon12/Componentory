"""Synthetic fixtures test analysis only; they are never original UI evidence."""

import io
import tarfile
import tempfile
import unittest
import urllib.error
from pathlib import Path
from unittest.mock import patch

from .aosp import extract_resources, read_url, sha256
from .resources import ResourceIndex, analysis_grade, java_references
from .sdk import parse_signature, public_ui, source_path


class SignatureTests(unittest.TestCase):
    def test_ancestry_excludes_adapters_and_non_public_implementation_classes(self):
        classes = parse_signature("""
package android.view {
 public class View {
 public abstract class ViewGroup extends android.view.View {
}
package android.widget {
 public class Adapter {
 public abstract class AdapterView<T extends android.widget.Adapter> extends android.view.ViewGroup {
 public class ListView extends android.widget.AdapterView<java.lang.Object> {
 protected class InternalView extends android.view.View {
 public class ArrayAdapter<T> extends java.lang.Object {
 @Deprecated public class OldView extends android.view.View {
}
package android.preference {
 public class Preference {
 public class ListPreference extends android.preference.Preference {
}
""")
        ui = public_ui(classes)
        self.assertIn("android.widget.ListView", ui)
        self.assertNotIn("android.widget.ArrayAdapter", ui)
        self.assertNotIn("android.widget.InternalView", ui)
        self.assertEqual("android.view.ViewGroup", classes["android.widget.AdapterView"].parent)
        self.assertTrue(classes["android.widget.AdapterView"].abstract)
        self.assertTrue(classes["android.widget.OldView"].deprecated)
        self.assertEqual("PREFERENCE", ui["android.preference.ListPreference"][1])

    def test_nested_class_source_and_cycles(self):
        self.assertEqual(
            "core/java/android/app/ActionBar.java",
            source_path("android.app.ActionBar.Tab"),
        )
        classes = parse_signature("""package android.widget {
 public class A extends android.widget.B {
 public class B extends android.widget.A {
}""")
        self.assertEqual({}, public_ui(classes))

    def test_java_references_include_constructor_styles_and_ignore_comments(self):
        attrs, refs = java_references("""
this(context, attrs, com.android.internal.R.attr.buttonStyle);
int layout = R.layout.widget_body;
int colors = android.R.color.white;
int fields = R.styleable.Widget_textColor;
// R.drawable.fake
/* R.dimen.fake */
""")
        self.assertEqual(["buttonStyle"], attrs)
        self.assertEqual(
            [
                "attr/buttonStyle",
                "color/white",
                "layout/widget_body",
                "styleable/Widget_textColor",
            ],
            refs,
        )


class ResourceTests(unittest.TestCase):
    def setUp(self):
        self.temporary = tempfile.TemporaryDirectory()
        self.root = Path(self.temporary.name)
        self.add(
            "values/theme.xml",
            """<resources>
<attr name="buttonStyle" format="reference"/><attr name="tone" format="color"/>
<style name="Theme.Base"><item name="buttonStyle">@style/Widget.Button</item></style>
<style name="Theme.Light" parent="Theme.Base"><item name="tone">@color/white</item></style>
<public type="style" name="Theme.Light"/>
<style name="Widget" parent=""><item name="textColor">?attr/tone</item></style>
<style name="Widget.Button"><item name="background">@drawable/button</item></style>
<color name="white">#ffffff</color>
</resources>""",
        )
        self.add(
            "drawable/button.xml",
            '<?xml version="1.0"?><selector><item drawable="@drawable/button_normal"/></selector>',
        )
        self.add("drawable-hdpi/button_normal.9.png", b"original-nine-patch")
        self.add("drawable-xhdpi/button_normal.9.png", b"other-density")
        self.add(
            "values-land/size.xml",
            '<resources><dimen name="spacing">12dp</dimen></resources>',
        )
        self.add(
            "values/size.xml",
            '<resources><dimen name="spacing">8dp</dimen></resources>',
        )

    def tearDown(self):
        self.temporary.cleanup()

    def add(self, relative, data):
        path = self.root / relative
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_bytes(data.encode("utf-8") if isinstance(data, str) else data)

    def test_theme_style_parents_recursive_drawables_and_qualifiers(self):
        graph = ResourceIndex(self.root).graph(
            ["attr/buttonStyle", "dimen/spacing"], ["Theme.Light"]
        )
        self.assertEqual([], graph["missingResources"])
        self.assertIn("style/Widget", graph["nodes"])
        self.assertIn("color/white", graph["nodes"])
        variants = graph["nodes"]["drawable/button_normal"]["variants"]
        self.assertEqual(
            ["drawable-hdpi/button_normal.9.png", "drawable-xhdpi/button_normal.9.png"],
            [item["path"] for item in variants],
        )
        self.assertEqual(sha256(b"original-nine-patch"), variants[0]["sha256"])
        self.assertEqual(["hdpi"], variants[0]["qualifiers"])
        self.assertEqual(2, len(graph["nodes"]["dimen/spacing"]["variants"]))
        self.assertEqual(
            "Theme.Base",
            graph["themeBindings"]["buttonStyle"]["Theme.Light"][0]["theme"],
        )

    def test_missing_dependencies_styleables_ids_and_cycles_are_explicit(self):
        self.add(
            "values/extra.xml",
            """<resources>
<declare-styleable name="Widget"><attr name="missing"/></declare-styleable>
<style name="Cycle.One" parent="Cycle.Two"/>
<style name="Cycle.Two" parent="Cycle.One"/>
</resources>""",
        )
        self.add(
            "layout/body.xml",
            '<View xmlns:android="http://schemas.android.com/apk/res/android" android:id="@+id/content" android:background="@drawable/not_found"/>',
        )
        graph = ResourceIndex(self.root).graph(
            ["style/Cycle.One", "styleable/Widget_missing", "layout/body"],
            ["Cycle.One"],
        )
        self.assertIn("drawable/not_found", graph["missingResources"])
        self.assertIn("id/content", graph["nodes"])
        self.assertIn("attr/missing", graph["nodes"])
        self.assertEqual([], graph["themeBindings"]["missing"]["Cycle.One"])

    def test_grades_never_claim_original_captures(self):
        self.assertEqual("RESOURCE_ONLY", analysis_grade("android.widget.Button")["value"])
        self.assertEqual(
            "IMPLEMENTATION_DEPENDENT",
            analysis_grade("android.widget.DatePicker")["value"],
        )
        self.assertEqual("MISSING", analysis_grade("android.widget.Button")["originalCapture"])


class ArchiveTests(unittest.TestCase):
    def test_temporary_rate_limit_retries_without_changing_source(self):
        error = urllib.error.HTTPError(
            "https://example.invalid/pinned", 429, "rate limited", {"Retry-After": "1"}, None
        )
        with (
            patch(
                "urllib.request.urlopen", side_effect=[error, io.BytesIO(b"original")]
            ) as request,
            patch("time.sleep") as sleep,
        ):
            self.assertEqual(b"original", read_url("https://example.invalid/pinned"))
            self.assertEqual(2, request.call_count)
            sleep.assert_called_once_with(1)

    def test_missing_sources_are_not_retried_and_rate_limit_retries_are_bounded(self):
        for status, attempts in ((404, 1), (429, 5)):
            error = urllib.error.HTTPError(
                "https://example.invalid/pinned", status, "unavailable", {}, None
            )
            with (
                self.subTest(status=status),
                patch("urllib.request.urlopen", side_effect=error) as request,
                patch("time.sleep"),
            ):
                with self.assertRaises(urllib.error.HTTPError):
                    read_url("https://example.invalid/pinned")
                self.assertEqual(attempts, request.call_count)

    def test_tar_rejects_traversal_and_links_before_writing_any_member(self):
        for name, link in (("../outside", False), ("link", True)):
            with self.subTest(name=name), tempfile.TemporaryDirectory() as temporary:
                root = Path(temporary)
                archive = root / "resources.tar.gz"
                with tarfile.open(archive, "w:gz") as bundle:
                    info = tarfile.TarInfo(name)
                    if link:
                        info.type = tarfile.SYMTYPE
                        info.linkname = "../outside"
                        bundle.addfile(info)
                    else:
                        info.size = 3
                        bundle.addfile(info, io.BytesIO(b"bad"))
                with self.assertRaises(ValueError):
                    extract_resources(archive, root / "export")
                self.assertFalse((root / "outside").exists())

    def test_tar_preserves_binary_and_qualified_paths(self):
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary)
            archive = root / "resources.tar.gz"
            with tarfile.open(archive, "w:gz") as bundle:
                info = tarfile.TarInfo("drawable-hdpi/button.9.png")
                info.size = 3
                bundle.addfile(info, io.BytesIO(b"png"))
            extract_resources(archive, root / "export")
            self.assertEqual(b"png", (root / "export/drawable-hdpi/button.9.png").read_bytes())


if __name__ == "__main__":
    unittest.main()
