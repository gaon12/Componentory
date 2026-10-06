"""Fetch immutable Gitiles sources and retain local caches with source hashes."""

import base64
import hashlib
import json
import re
import tarfile
import urllib.error
import urllib.request
from pathlib import Path

HOST = "https://android.googlesource.com/"


def sha256(data: bytes) -> str:
    return hashlib.sha256(data).hexdigest()


def read_url(url: str) -> bytes:
    with urllib.request.urlopen(url, timeout=90) as response:
        return response.read()


def resolve_commit(repository: str, ref: str) -> str:
    if not re.fullmatch(r"[\w./-]+", ref):
        raise ValueError("Invalid Git ref.")
    response = read_url(f"{HOST}{repository}/+/{ref}?format=JSON")
    commit = json.loads(response.split(b"\n", 1)[1])["commit"]
    if not re.fullmatch(r"[0-9a-f]{40}", commit):
        raise ValueError("Gitiles did not return a commit identity.")
    return commit


class SourceCache:
    def __init__(self, cache: Path, repository: str, commit: str):
        if not re.fullmatch(r"[0-9a-f]{40}", commit):
            raise ValueError("Sources must use an immutable Git commit.")
        self.root = cache / repository.replace("/", "-") / commit
        self.repository = repository
        self.commit = commit

    def url(self, path: str) -> str:
        return f"{HOST}{self.repository}/+/{self.commit}/{path}"

    def file(self, path: str) -> bytes | None:
        destination = self.root / path
        if destination.is_file():
            return destination.read_bytes()
        try:
            data = base64.b64decode(read_url(self.url(path) + "?format=TEXT"), validate=True)
        except urllib.error.HTTPError as error:
            if error.code == 404:
                return None
            raise
        destination.parent.mkdir(parents=True, exist_ok=True)
        destination.write_bytes(data)
        return data

    def resources(self) -> Path:
        archive = self.root / "resources.tar.gz"
        destination = self.root / "resources"
        marker = destination / ".complete"
        if (
            marker.is_file()
            and archive.is_file()
            and marker.read_text() == sha256(archive.read_bytes())
        ):
            return destination
        if not archive.is_file():
            archive.parent.mkdir(parents=True, exist_ok=True)
            data = read_url(f"{HOST}{self.repository}/+archive/{self.commit}/core/res/res.tar.gz")
            archive.write_bytes(data)
        extract_resources(archive, destination)
        marker.write_text(sha256(archive.read_bytes()))
        return destination


def extract_resources(archive: Path, destination: Path) -> None:
    destination.mkdir(parents=True, exist_ok=True)
    root = destination.resolve()
    with tarfile.open(archive, "r:gz") as bundle:
        members = bundle.getmembers()
        for member in members:
            target = (root / member.name).resolve()
            if not target.is_relative_to(root) or member.issym() or member.islnk():
                raise ValueError(f"Unsafe resource archive entry: {member.name}")
            if not member.isdir() and not member.isfile():
                raise ValueError(f"Unsupported archive entry: {member.name}")
        for member in members:
            target = root / member.name
            if member.isdir():
                target.mkdir(parents=True, exist_ok=True)
            else:
                target.parent.mkdir(parents=True, exist_ok=True)
                with bundle.extractfile(member) as source:
                    target.write_bytes(source.read())
