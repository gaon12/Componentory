"""Synthetic fixtures test analysis only; they are never original UI evidence."""

import io
import tarfile
import tempfile
import unittest
import urllib.error
from pathlib import Path
from unittest.mock import patch

from .__main__ import api_signature
from .aosp import SourceCache, extract_resources, read_url, sha256
from .notices import APACHE_LICENSE, preserve_notices
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

    def test_java_style_symbols_android_parents_and_platform_id_declarations(self):
        self.add(
            "values/platform.xml",
            '<resources><style name="Widget.Child" parent="android:Widget"/></resources>',
        )
        self.add(
            "layout/platform.xml",
            '<View xmlns:android="http://schemas.android.com/apk/res/android" android:id="@+android:id/content"/>',
        )
        graph = ResourceIndex(self.root).graph(
            ["style/Widget_Child", "layout/platform"], ["Theme.Light"]
        )
        self.assertEqual([], graph["missingResources"])
        self.assertIn("style/Widget.Child", graph["nodes"])
        self.assertIn("style/Widget", graph["nodes"])
        self.assertIn("id/content", graph["nodes"])


class NoticeTests(unittest.TestCase):
    def test_original_root_and_resource_notices_keep_bytes_hashes_and_sources(self):
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary)
            cache = SourceCache(root / "cache", "platform/frameworks/base", "a" * 40)
            resources = root / "resources"
            (resources / "drawable-hdpi").mkdir(parents=True)
            original = b"Unchanged notice\r\nCopyright upstream\r\n"
            (resources / "drawable-hdpi/NOTICE.txt").write_bytes(original)

            def listing(directory):
                return (
                    [
                        {"name": "NOTICE", "type": "blob"},
                        {"name": "MODULE_LICENSE_APACHE2", "type": "blob"},
                    ]
                    if not directory
                    else []
                )

            with (
                patch.object(cache, "directory", side_effect=listing),
                patch.object(
                    cache,
                    "file",
                    side_effect=lambda path: b"" if path == "MODULE_LICENSE_APACHE2" else original,
                ),
            ):
                result = preserve_notices(
                    cache, root / "export", ["core/res/res/drawable-hdpi/button.9.png"], resources
                )
            self.assertEqual("Apache-2.0", result["declaredModuleLicense"])
            self.assertEqual("PRESENT", result["rootNotice"])
            self.assertEqual(3, len(result["files"]))
            for item in result["files"]:
                data = (root / "export" / item["path"]).read_bytes()
                self.assertEqual(sha256(data), item["sha256"])
                self.assertIn("/" + "a" * 40 + "/", item["url"])
                if "NOTICE" in item["sourcePath"]:
                    self.assertEqual(original, data)
            self.assertEqual(
                APACHE_LICENSE.read_bytes(),
                (root / "export/third-party/Apache-2.0.txt").read_bytes(),
            )

    def test_missing_notices_are_explicit_and_sdk_terms_are_not_relabelled_apache(self):
        with tempfile.TemporaryDirectory() as temporary:
            cache = SourceCache(Path(temporary), "platform/prebuilts/sdk", "b" * 40)
            for has_notice in (False, True):
                with (
                    self.subTest(has_notice=has_notice),
                    patch.object(
                        cache,
                        "directory",
                        side_effect=lambda directory: (
                            [{"name": "NOTICE", "type": "blob"}]
                            if not directory and has_notice
                            else []
                        ),
                    ),
                    patch.object(cache, "file", return_value=b"SDK terms"),
                ):
                    result = preserve_notices(
                        cache, Path(temporary) / "export", ["19/public/api/android.txt"]
                    )
                self.assertEqual("PRESENT" if has_notice else "ABSENT", result["rootNotice"])
                self.assertEqual("NOT_INFERRED", result["declaredModuleLicense"])
                self.assertIsNone(result["apacheLicenseCopy"])

    def test_listed_notice_that_cannot_be_read_fails_the_export(self):
        with tempfile.TemporaryDirectory() as temporary:
            cache = SourceCache(Path(temporary), "platform/frameworks/base", "a" * 40)
            with (
                patch.object(cache, "directory", return_value=[{"name": "NOTICE", "type": "blob"}]),
                patch.object(cache, "file", return_value=None),
            ):
                with self.assertRaisesRegex(ValueError, "listed upstream notice"):
                    preserve_notices(cache, Path(temporary) / "export", [])


class ArchiveTests(unittest.TestCase):
    def test_internal_qualifier_aliases_keep_files_and_record_the_original_target(self):
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary)
            archive = root / "resources.tar.gz"
            with tarfile.open(archive, "w:gz") as bundle:
                info = tarfile.TarInfo("values-mcc310-mnc150/colors.xml")
                info.size = 3
                bundle.addfile(info, io.BytesIO(b"xml"))
                alias = tarfile.TarInfo("values-mcc310-mnc170")
                alias.type = tarfile.SYMTYPE
                alias.linkname = "./values-mcc310-mnc150"
                bundle.addfile(alias)
            aliases = extract_resources(archive, root / "export")
            self.assertEqual(b"xml", (root / "export/values-mcc310-mnc170/colors.xml").read_bytes())
            self.assertFalse((root / "export/values-mcc310-mnc170").is_symlink())
            self.assertEqual(
                [{"path": "values-mcc310-mnc170", "target": "values-mcc310-mnc150"}], aliases
            )

    def test_cycles_in_internal_aliases_are_rejected(self):
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary)
            archive = root / "resources.tar.gz"
            with tarfile.open(archive, "w:gz") as bundle:
                for name, target in (("one", "two"), ("two", "one")):
                    info = tarfile.TarInfo(name)
                    info.type = tarfile.SYMTYPE
                    info.linkname = target
                    bundle.addfile(info)
            with self.assertRaises(ValueError):
                extract_resources(archive, root / "export")

    def test_old_release_signature_falls_back_to_a_pinned_sdk_with_provenance(self):
        with tempfile.TemporaryDirectory() as temporary:
            from .aosp import SourceCache

            framework = SourceCache(Path(temporary), "platform/frameworks/base", "a" * 40)
            sdk = SourceCache(Path(temporary), "platform/prebuilts/sdk", "b" * 40)
            with (
                patch.object(framework, "file", return_value=None),
                patch.object(sdk, "file", return_value=b"signature"),
            ):
                data, provenance = api_signature(framework, sdk, 10)
            self.assertEqual(b"signature", data)
            self.assertEqual("PUBLIC_SDK_API_SIGNATURE", provenance["basis"])
            self.assertEqual("b" * 40, provenance["commit"])
            self.assertTrue(provenance["url"].endswith("/10/public/api/android.txt"))

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
