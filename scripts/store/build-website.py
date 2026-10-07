"""Publish the same privacy text that is packaged in the Android app."""

import argparse
import html
import re
import shutil
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
STYLE = """
:root { color-scheme: light dark; font-family: system-ui, sans-serif; }
body { margin: 0; background: #f4f7fb; color: #172c49; line-height: 1.75; }
main { max-width: 800px; margin: 40px auto; padding: 36px; background: white;
       border-radius: 24px; }
h1 { line-height: 1.2; font-size: clamp(2rem, 5vw, 3rem); }
h2 { margin-top: 2rem; line-height: 1.4; font-size: 1.25rem; }
a { color: #2367bd; overflow-wrap: anywhere; }
nav { display: flex; gap: 24px; flex-wrap: wrap; }
img { width: 96px; height: 96px; }
@media (max-width: 600px) { main { margin: 16px; padding: 24px; } }
@media (prefers-color-scheme: dark) {
  body { background: #101824; color: #e2eaf5; }
  main { background: #1b283a; } a { color: #94c5ff; }
}
"""


def page(title: str, content: str) -> str:
    return f"""<!doctype html>
<html lang="en">
<head><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>{html.escape(title)}</title><style>{STYLE}</style></head>
<body><main>{content}</main></body></html>
"""


def linked_text(text: str) -> str:
    escaped = html.escape(text)
    return re.sub(r"https://[^\s<>]+", r'<a href="\g<0>">\g<0></a>', escaped)


def build(output: Path) -> None:
    output.mkdir(parents=True, exist_ok=True)
    policy = (ROOT / "docs/privacy-policy.txt").read_text(encoding="utf-8")
    sections = policy.strip().split("\n\n")
    introduction = sections[0].splitlines()
    content = f"<h1>{html.escape(introduction[0])}</h1><p>{html.escape(introduction[1])}</p>"
    headings = {
        "Local processing",
        "Optional keyboard and autofill demonstration",
        "Exports and external links",
        "Storage, retention, and deletion",
        "Contact and changes",
    }
    for section in sections[1:]:
        lines = section.splitlines()
        if lines[0] in headings:
            content += f"<h2>{html.escape(lines[0])}</h2><p>{linked_text(' '.join(lines[1:]))}</p>"
        else:
            content += f"<p>{linked_text(' '.join(lines))}</p>"
    content += '<nav><a href="./">Componentory</a><a href="https://github.com/gaon12/Componentory">Source code</a></nav>'
    (output / "privacy.html").write_text(
        page("Componentory Privacy Policy", content), encoding="utf-8"
    )
    index = """<img src="app-icon.svg" alt="Componentory flask and cube icon">
<h1>Componentory</h1><p>A hands-on Android UI lab for exploring components,
comparing design families, and reading version history.</p>
<p>Framework samples run on the installed operating system. Historical resources
and fixed Compose library versions have explicit source labels.</p>
<nav><a href="privacy.html">Privacy policy</a>
<a href="https://github.com/gaon12/Componentory">Source code</a>
<a href="https://github.com/gaon12/Componentory/issues/new">Report an issue</a></nav>
<p>Android is a trademark of Google LLC. Componentory is an independent project.</p>"""
    (output / "index.html").write_text(page("Componentory", index), encoding="utf-8")
    shutil.copyfile(ROOT / "design/app-icon.svg", output / "app-icon.svg")
    (output / ".nojekyll").touch()
    print(f"Website built at {output.resolve()}")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--output", type=Path, default=ROOT / ".local/site")
    build(parser.parse_args().output)
