"""Read SDK signatures rather than treating implementation files as public APIs."""

import re
from dataclasses import dataclass


@dataclass(frozen=True)
class ApiClass:
    name: str
    parent: str | None
    deprecated: bool
    abstract: bool


def without_generics(text: str) -> str:
    depth = 0
    result = []
    for char in text:
        if char == "<":
            depth += 1
        elif char == ">":
            depth = max(0, depth - 1)
        elif depth == 0:
            result.append(char)
    return "".join(result)


def parse_signature(text: str) -> dict[str, ApiClass]:
    classes = {}
    package = None
    for line in text.splitlines():
        package_match = re.match(r"package ([\w.]+)\s*\{", line.strip())
        if package_match:
            package = package_match[1]
            continue
        # A signature declares nested classes by their qualified local name.
        declaration = without_generics(line)
        match = re.search(r"\bpublic\b.*?\bclass ([\w.]+)(?: extends ([\w.]+))?", declaration)
        if package and match:
            name = f"{package}.{match[1]}"
            classes[name] = ApiClass(
                name,
                match[2],
                bool(re.search(r"@(?:java.lang.)?Deprecated\b|\bdeprecated\b", declaration)),
                bool(re.search(r"\babstract\b", declaration)),
            )
    if not classes:
        raise ValueError("The SDK signature contains no public classes.")
    return classes


UI_PACKAGES = (
    "android.widget.",
    "android.view.",
    "android.app.",
    "android.preference.",
    "android.gesture.",
    "android.webkit.",
)
UI_ROOTS = {
    "android.view.View": "VIEW",
    "android.app.Dialog": "DIALOG",
    "android.widget.PopupWindow": "POPUP",
    "android.preference.Preference": "PREFERENCE",
}
UI_OBJECTS = {
    "android.widget.Toast": "POPUP",
    "android.widget.PopupMenu": "POPUP",
    "android.app.ActionBar": "CONTROLLER",
    "android.view.ActionMode": "CONTROLLER",
    "android.widget.EdgeEffect": "CONTROLLER",
    "android.widget.ZoomButtonsController": "CONTROLLER",
}


def ui_category(name: str, classes: dict[str, ApiClass]) -> str | None:
    if not name.startswith(UI_PACKAGES):
        return None
    if name in UI_OBJECTS:
        return UI_OBJECTS[name]
    visited = set()
    ancestor = name
    while ancestor and ancestor not in visited:
        if ancestor in UI_ROOTS:
            return UI_ROOTS[ancestor]
        visited.add(ancestor)
        ancestor = classes.get(ancestor).parent if ancestor in classes else None
    return None


def public_ui(classes: dict[str, ApiClass]) -> dict[str, tuple[ApiClass, str]]:
    return {
        name: (item, category)
        for name, item in sorted(classes.items())
        if (category := ui_category(name, classes)) is not None
    }


def source_path(name: str) -> str:
    parts = name.split(".")
    outer = next(index for index, part in enumerate(parts) if part[0].isupper())
    return "core/java/" + "/".join(parts[: outer + 1]) + ".java"
