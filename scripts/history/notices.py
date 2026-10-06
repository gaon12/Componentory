"""Preserve upstream terms without assigning one license to every AOSP file."""

from pathlib import Path, PurePosixPath

from .aosp import SourceCache, sha256

APACHE_LICENSE = Path(__file__).resolve().parents[2] / "licenses" / "Apache-2.0.txt"
RESOURCE_PREFIX = "core/res/res"


def is_notice(name: str) -> bool:
    upper = name.upper()
    return upper.startswith("MODULE_LICENSE_") or upper.split(".", 1)[0] in {
        "LICENSE",
        "LICENCE",
        "NOTICE",
        "COPYING",
        "COPYRIGHT",
    }


def preserve_notices(
    cache: SourceCache,
    destination: Path,
    source_paths: list[str],
    resource_root: Path | None = None,
) -> dict:
    directories = {""}
    for path in source_paths:
        directories.update(
            str(parent) if str(parent) != "." else "" for parent in PurePosixPath(path).parents
        )
    files = []
    for directory in sorted(directories):
        local = None
        if resource_root is not None and (
            directory == RESOURCE_PREFIX or directory.startswith(RESOURCE_PREFIX + "/")
        ):
            local = resource_root / directory.removeprefix(RESOURCE_PREFIX).lstrip("/")
            names = sorted(
                path.name for path in local.iterdir() if path.is_file() and is_notice(path.name)
            )
        else:
            names = sorted(
                entry["name"]
                for entry in cache.directory(directory)
                if entry["type"] == "blob" and is_notice(entry["name"])
            )
        for name in names:
            source = f"{directory}/{name}" if directory else name
            data = (local / name).read_bytes() if local else cache.file(source)
            if data is None:
                raise ValueError(f"A listed upstream notice is missing: {source}")
            relative = f"third-party/{cache.repository.replace('/', '-')}/{source}"
            target = destination / relative
            target.parent.mkdir(parents=True, exist_ok=True)
            target.write_bytes(data)
            files.append(
                {
                    "sourcePath": source,
                    "path": relative,
                    "url": cache.url(source),
                    "sha256": sha256(data),
                }
            )
    apache_marker = any(item["sourcePath"] == "MODULE_LICENSE_APACHE2" for item in files)
    if apache_marker:
        relative = "third-party/Apache-2.0.txt"
        target = destination / relative
        target.parent.mkdir(parents=True, exist_ok=True)
        data = APACHE_LICENSE.read_bytes()
        target.write_bytes(data)
        license_copy = {
            "path": relative,
            "url": "https://www.apache.org/licenses/LICENSE-2.0.txt",
            "sha256": sha256(data),
        }
    else:
        license_copy = None
    return {
        "repository": cache.repository,
        "commit": cache.commit,
        "checkedDirectories": sorted(directories),
        "rootNotice": "PRESENT"
        if any(item["sourcePath"] == "NOTICE" for item in files)
        else "ABSENT",
        "declaredModuleLicense": "Apache-2.0" if apache_marker else "NOT_INFERRED",
        "files": files,
        "apacheLicenseCopy": license_copy,
        "scope": "Original ancestor notices are retained whole. File headers and third-party terms still apply; this is not a blanket license assignment.",
    }
