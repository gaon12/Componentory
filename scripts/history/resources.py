"""Follow framework styles, Java references, and every resource qualifier variant."""

import re
import xml.etree.ElementTree as ET
from collections import defaultdict
from dataclasses import dataclass
from pathlib import Path

from .aosp import sha256

RESOURCE_REF = re.compile(r"@\+?\*?(?:(android):)?([\w-]+)/([\w.]+)")
ATTRIBUTE_REF = re.compile(r"(?<!<)\?\*?(?:android:)?(?:attr/)?([\w.]+)")
JAVA_REF = re.compile(
    r"\b(?:(?:com\.android\.internal|android)\.)?R\.(layout|drawable|color|dimen|style|styleable|id|attr)\.(\w+)"
)
DEFAULT_STYLE = re.compile(
    r"\b(?:this|super)\s*\(\s*\w+\s*,\s*\w+\s*,\s*(?:[\w.]+\.)?R\.attr\.(\w+)"
)


def references(text: str) -> set[str]:
    refs = {f"{kind}/{name}" for _, kind, name in RESOURCE_REF.findall(text)}
    refs.update(f"attr/{name}" for name in ATTRIBUTE_REF.findall(text))
    return refs


def java_references(text: str) -> tuple[list[str], list[str]]:
    # Comments are evidence explanations, not executed resource references.
    code = re.sub(r"/\*.*?\*/|//[^\n]*", "", text, flags=re.DOTALL)
    return sorted(set(DEFAULT_STYLE.findall(code))), sorted(
        {f"{kind}/{name}" for kind, name in JAVA_REF.findall(code)}
    )


@dataclass(frozen=True)
class Definition:
    path: str
    element: ET.Element | None = None


def style_parent(element: ET.Element) -> str | None:
    parent = element.get("parent")
    if parent is not None:
        return parent.removeprefix("@android:style/").removeprefix("@style/") or None
    name = element.get("name", "")
    return name.rsplit(".", 1)[0] if "." in name else None


class ResourceIndex:
    def __init__(self, root: Path):
        self.root = root
        self.definitions: dict[str, list[Definition]] = defaultdict(list)
        self.hashes = {}
        for path in sorted(root.glob("*/*")):
            if not path.is_file():
                continue
            relative = path.relative_to(root).as_posix()
            kind = path.parent.name.split("-", 1)[0]
            if kind == "values" and path.suffix == ".xml":
                tree = ET.parse(path)
                for element in tree.getroot():
                    # Visibility and generated-ID declarations do not define styles or values.
                    if element.tag in {"public", "public-padding", "java-symbol"}:
                        continue
                    name = element.get("name")
                    if not name:
                        continue
                    resource_type = element.get("type", element.tag)
                    if resource_type == "declare-styleable":
                        resource_type = "styleable"
                        for attr in element.findall("attr"):
                            attr_name = attr.get("name", "").removeprefix("android:")
                            self.definitions[f"attr/{attr_name}"].append(Definition(relative, attr))
                            self.definitions[f"styleable/{name}_{attr_name}"].append(
                                Definition(relative, element)
                            )
                    self.definitions[f"{resource_type}/{name}"].append(
                        Definition(relative, element)
                    )
            elif kind != "values":
                name = (
                    path.name.removesuffix(".9.png") if path.name.endswith(".9.png") else path.stem
                )
                self.definitions[f"{kind}/{name}"].append(Definition(relative))
                if path.suffix == ".xml":
                    # IDs declared inside layouts have no separate file.
                    text = path.read_text(encoding="utf-8")
                    for name in re.findall(r"@\+id/([\w.]+)", text):
                        self.definitions[f"id/{name}"].append(Definition(relative))

    def attribute_bindings(self, theme: str, attribute: str, visited=None) -> list[dict]:
        visited = set() if visited is None else visited
        if theme in visited:
            return []
        visited = visited | {theme}
        result = []
        for definition in self.definitions.get(f"style/{theme}", []):
            element = definition.element
            if element is None:
                continue
            items = [
                item
                for item in element.findall("item")
                if item.get("name", "").removeprefix("android:") == attribute
            ]
            if items:
                result.extend(
                    {
                        "theme": theme,
                        "path": definition.path,
                        "value": "".join(item.itertext()).strip(),
                    }
                    for item in items
                )
            elif parent := style_parent(element):
                result.extend(self.attribute_bindings(parent, attribute, visited))
        return result

    def dependencies(self, definition: Definition) -> set[str]:
        path = self.root / definition.path
        if definition.element is not None:
            element = definition.element
            dependencies = references(ET.tostring(element, encoding="unicode"))
            if element.tag == "style" and (parent := style_parent(element)):
                dependencies.add(f"style/{parent}")
            if element.tag == "declare-styleable":
                dependencies.update(
                    f"attr/{item.get('name').removeprefix('android:')}"
                    for item in element.findall("attr")
                    if item.get("name")
                )
            return dependencies
        return references(path.read_text(encoding="utf-8")) if path.suffix == ".xml" else set()

    def describe(self, definition: Definition) -> dict:
        path = definition.path
        if path not in self.hashes:
            self.hashes[path] = sha256((self.root / path).read_bytes())
        result = {
            "path": path,
            "qualifiers": path.split("/", 1)[0].split("-")[1:],
            "sha256": self.hashes[path],
        }
        if definition.element is not None:
            result["declaration"] = ET.tostring(definition.element, encoding="unicode")
        return result

    def graph(self, roots: list[str], themes: list[str]) -> dict:
        pending = list(roots)
        nodes = {}
        missing = set()
        bindings = {}
        while pending:
            ref = pending.pop()
            if ref in nodes or ref in missing:
                continue
            definitions = self.definitions.get(ref, [])
            dependencies = set()
            if ref.startswith("attr/"):
                attribute = ref.split("/", 1)[1]
                bindings[attribute] = {}
                for theme in themes:
                    values = self.attribute_bindings(theme, attribute)
                    bindings[attribute][theme] = values
                    for value in values:
                        dependencies.update(references(value["value"]))
                # Attribute declarations can be public.xml items without defaults.
            if not definitions:
                missing.add(ref)
                # A binding may resolve an attribute whose declaration was not found.
                pending.extend(dependencies)
                continue
            for definition in definitions:
                dependencies.update(self.dependencies(definition))
            nodes[ref] = {
                "variants": [self.describe(item) for item in definitions],
                "dependencies": sorted(dependencies),
            }
            pending.extend(dependencies)
        return {
            "roots": sorted(set(roots)),
            "nodes": dict(sorted(nodes.items())),
            "themeBindings": bindings,
            "missingResources": sorted(missing),
            "variantPolicy": "All qualifiers retained; no device configuration is selected.",
        }


RESOURCE_ONLY = {
    "Button",
    "CheckBox",
    "RadioButton",
    "SeekBar",
    "RatingBar",
    "ImageView",
    "TextView",
}
RESOURCE_AND_LAYOUT = {"Spinner", "AlertDialog", "PopupWindow", "Toast", "PopupMenu"}


def analysis_grade(name: str) -> dict:
    simple = name.rsplit(".", 1)[-1]
    grade = (
        "RESOURCE_ONLY"
        if simple in RESOURCE_ONLY
        else (
            "RESOURCE_AND_LAYOUT" if simple in RESOURCE_AND_LAYOUT else "IMPLEMENTATION_DEPENDENT"
        )
    )
    return {
        "value": grade,
        "basis": "Conservative class policy; not a rendering or behavior test.",
        "originalCapture": "MISSING",
        "historicalBehavior": "UNVERIFIED",
    }
