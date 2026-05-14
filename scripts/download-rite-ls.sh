#!/usr/bin/env bash
# Download rite-ls binaries into src/main/resources/bin/ from rite-ly/rite GitHub Releases.
#
# Usage:
#   ./scripts/download-rite-ls.sh VERSION
#
# Requires: gh CLI (https://cli.github.com/)

set -euo pipefail

REPO="rite-ly/rite"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_DIR="$(dirname "$SCRIPT_DIR")"
TMP_DIR="$(mktemp -d)"

cleanup() { rm -rf "$TMP_DIR"; }
trap cleanup EXIT

VERSION="${1:?Usage: $0 VERSION}"

echo "Downloading rite-ls v${VERSION} from GitHub Releases..."
gh release download "v${VERSION}" --repo "$REPO" --pattern 'rite-ls-*' --dir "$TMP_DIR"

place() {
    local rite_target="$1"
    local dest_rel="$2"
    local dest="$REPO_DIR/$dest_rel"
    local bin_name
    bin_name="$(basename "$dest_rel")"
    local release_file="rite-ls-${VERSION}-${rite_target}"
    [[ "$bin_name" == *.exe ]] && release_file="${release_file}.exe"
    local src="$TMP_DIR/$release_file"

    if [[ ! -f "$src" ]]; then
        echo "  Warning: rite-ls-${rite_target} not found — skipping" >&2
        return
    fi

    mkdir -p "$(dirname "$dest")"
    cp "$src" "$dest"
    chmod 755 "$dest"
    echo "  rite-ls-${rite_target} → $dest_rel"
}

place "darwin-arm64"     "src/main/resources/bin/darwin-arm64/rite-ls"
place "darwin-amd64"     "src/main/resources/bin/darwin-x86_64/rite-ls"
place "linux-arm64"      "src/main/resources/bin/linux-arm64/rite-ls"
place "linux-amd64"      "src/main/resources/bin/linux-x86_64/rite-ls"
place "windows-amd64"    "src/main/resources/bin/windows-x86_64/rite-ls.exe"

echo "Done."
