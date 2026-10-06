"""Export version inventories and source resource graphs without an Android build."""

import argparse
import csv
import json
import re
import shutil
from pathlib import Path

from .aosp import SourceCache, resolve_commit, sha256
from .resources import ResourceIndex, analysis_grade, java_references
from .sdk import parse_signature, public_ui, source_path

SDK_REPOSITORY = "platform/prebuilts/sdk"
SDK_COMMIT = "3af7c93524be6f51e092b87b17f009f13ee98b43"
FRAMEWORK_REPOSITORY = "platform/frameworks/base"


def write_json(path: Path, value) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")


def inventory(args) -> None:
    cache = SourceCache(args.cache, SDK_REPOSITORY, args.sdk_commit)
    versions = []
    rows = []
    for api in range(1, args.max_api + 1):
        path = f"{api}/public/api/android.txt"
        # An existing research cache can seed the immutable source cache.
        seed = args.cache / "aosp-sdk" / args.sdk_commit / f"{api}.txt"
        destination = cache.root / path
        if seed.is_file() and not destination.is_file():
            destination.parent.mkdir(parents=True, exist_ok=True)
            shutil.copyfile(seed, destination)
        data = cache.file(path)
        if data is None:
            raise ValueError(
                f"Missing public SDK signature for API {api}; no version was inferred."
            )
        candidates = public_ui(parse_signature(data.decode("utf-8")))
        versions.append(
            {
                "api": api,
                "url": cache.url(path),
                "sha256": sha256(data),
                "publicUiClasses": len(candidates),
            }
        )
        rows.extend(
            (
                api,
                name,
                item.parent or "",
                category,
                str(item.abstract).lower(),
                str(item.deprecated).lower(),
            )
            for name, (item, category) in candidates.items()
        )
    args.output.mkdir(parents=True, exist_ok=True)
    with (args.output / "public-ui.csv").open("w", encoding="utf-8", newline="") as output:
        writer = csv.writer(output, lineterminator="\n")
        writer.writerow(("api", "class", "superClass", "category", "abstract", "deprecated"))
        writer.writerows(rows)
    write_json(
        args.output / "provenance.json",
        {
            "schemaVersion": 1,
            "repository": SDK_REPOSITORY,
            "commit": args.sdk_commit,
            "classification": "Public class ancestry: View, Dialog, PopupWindow, Preference; explicit visual controllers.",
            "versions": versions,
            "originalCaptures": "MISSING",
            "scope": "Android framework; no library APIs.",
            "tableSha256": sha256((args.output / "public-ui.csv").read_bytes()),
        },
    )
    print(f"Exported {len(rows)} public UI records across {len(versions)} API levels.")


def api_signature(framework: SourceCache, sdk: SourceCache, api: int) -> tuple[bytes, dict]:
    path = "api/current.txt"
    source = framework
    basis = "RELEASE_API_SIGNATURE"
    signature = source.file(path)
    if signature is None:
        source = sdk
        path = f"{api}/public/api/android.txt"
        basis = "PUBLIC_SDK_API_SIGNATURE"
        signature = source.file(path)
    if signature is None:
        raise ValueError("Neither the release nor the pinned public SDK contains this signature.")
    return signature, {
        "basis": basis,
        "repository": source.repository,
        "commit": source.commit,
        "url": source.url(path),
        "sha256": sha256(signature),
    }


def export(args) -> None:
    commit = resolve_commit(FRAMEWORK_REPOSITORY, args.release)
    cache = SourceCache(args.cache, FRAMEWORK_REPOSITORY, commit)
    sdk = SourceCache(args.cache, SDK_REPOSITORY, args.sdk_commit)
    signature, signature_source = api_signature(cache, sdk, args.api)
    classes = parse_signature(signature.decode("utf-8"))
    candidates = public_ui(classes)
    requested = args.component or list(candidates)
    for name in requested:
        if name not in candidates:
            raise ValueError(f"{name} is not a classified public UI class in this release.")
    # Validate the declared API against the actual release metadata, not a filename.
    build_commit = resolve_commit("platform/build", args.release)
    build_cache = SourceCache(args.cache, "platform/build", build_commit)
    version_source = build_cache.file("core/version_defaults.mk")
    if version_source is None:
        raise ValueError("Missing release API metadata.")
    declared_api = re.search(rb"PLATFORM_SDK_VERSION\s*:?=\s*(\d+)", version_source)
    if declared_api is None or int(declared_api[1]) != args.api:
        raise ValueError("The requested API does not match this framework release.")
    index = ResourceIndex(cache.resources())
    destination = args.output / "versions" / args.release
    if destination.exists():
        raise ValueError("The export destination already exists. Choose a fresh output directory.")
    files = set()
    reports = []
    for name in requested:
        item, category = candidates[name]
        ancestor = name
        seen = set()
        roots = set()
        sources = []
        missing_sources = []
        attributes = {}
        while ancestor in classes and ancestor.startswith("android.") and ancestor not in seen:
            seen.add(ancestor)
            path = source_path(ancestor)
            data = cache.file(path)
            if data is None:
                missing_sources.append(path)
            else:
                styles, refs = java_references(data.decode("utf-8"))
                attributes[ancestor] = styles
                roots.update(refs)
                roots.update(f"attr/{style}" for style in styles)
                sources.append(
                    {
                        "class": ancestor,
                        "path": path,
                        "url": cache.url(path),
                        "sha256": sha256(data),
                    }
                )
            ancestor = classes[ancestor].parent
        graph = index.graph(sorted(roots), args.theme)
        files.update(
            variant["path"] for node in graph["nodes"].values() for variant in node["variants"]
        )
        # Preserve the original theme files which supplied each binding.
        files.update(
            binding["path"]
            for theme in graph["themeBindings"].values()
            for bindings in theme.values()
            for binding in bindings
        )
        report = {
            "class": name,
            "apiLevel": args.api,
            "release": args.release,
            "public": True,
            "superClass": item.parent,
            "category": category,
            "abstract": item.abstract,
            "deprecated": item.deprecated,
            "defaultStyleAttributesByClass": attributes,
            "sources": sources,
            "missingSources": sorted(set(missing_sources)),
            "analysisGrade": analysis_grade(name),
            "resourceGraph": graph,
        }
        relative = f"components/{name}.json"
        write_json(destination / relative, report)
        reports.append(
            {
                "class": name,
                "path": relative,
                "missingSources": len(missing_sources),
                "missingResources": len(graph["missingResources"]),
            }
        )
    for path in sorted(files):
        target = destination / "resources" / path
        target.parent.mkdir(parents=True, exist_ok=True)
        shutil.copyfile(index.root / path, target)
    write_json(
        destination / "manifest.json",
        {
            "schemaVersion": 1,
            "release": args.release,
            "apiLevel": args.api,
            "frameworkCommit": commit,
            "apiSignature": signature_source,
            "resourceAliases": json.loads(
                (index.root / ".aliases.json").read_text(encoding="utf-8")
            )
            if (index.root / ".aliases.json").is_file()
            else [],
            "releaseMetadata": {
                "url": build_cache.url("core/version_defaults.mk"),
                "commit": build_commit,
                "sha256": sha256(version_source),
            },
            "themes": args.theme,
            "components": reports,
            "resources": [
                {"path": path, "sha256": sha256((index.root / path).read_bytes())}
                for path in sorted(files)
            ],
            "evidence": {
                "kind": "SOURCE_ANALYSIS",
                "originalCapture": "MISSING",
                "behavior": "UNVERIFIED",
            },
            "limitations": [
                "All qualifiers are retained, not resolved for a device.",
                "Original values files may include unrelated declarations.",
                "Java delegates, dynamic resource names, and non-framework repositories need further analysis.",
                "This graph is not a runnable resource port or proof of original appearance.",
            ],
        },
    )
    print(
        f"Exported {len(reports)} components and {len(files)} original resource files to {destination}."
    )


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    subparsers = parser.add_subparsers(dest="command", required=True)
    catalog = subparsers.add_parser("inventory")
    catalog.add_argument("--cache", type=Path, default=Path(".local"))
    catalog.add_argument("--sdk-commit", default=SDK_COMMIT)
    catalog.add_argument("--max-api", type=int, default=36)
    catalog.add_argument("--output", type=Path, required=True)
    catalog.set_defaults(run=inventory)
    resources = subparsers.add_parser("export")
    resources.add_argument("--cache", type=Path, default=Path(".local"))
    resources.add_argument("--release", required=True)
    resources.add_argument("--api", type=int, required=True)
    resources.add_argument("--sdk-commit", default=SDK_COMMIT)
    resources.add_argument("--component", action="append")
    resources.add_argument("--theme", action="append", default=None)
    resources.add_argument("--output", type=Path, required=True)
    resources.set_defaults(run=export)
    args = parser.parse_args()
    if args.command == "export" and args.theme is None:
        args.theme = ["Theme.Holo", "Theme.Holo.Light"]
    args.run(args)


if __name__ == "__main__":
    main()
