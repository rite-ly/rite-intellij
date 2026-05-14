# Rite IntelliJ Plugin

IntelliJ plugin adding LSP-based language support for `.rite.yaml` ceremony files from the [Rite](https://github.com/rite-ly/rite) project.

## Features

- Diagnostics (errors and warnings)
- Hover information
- Completions

The language server attaches only to `*.rite.yaml` (or `*.rite.yml`) files, so unrelated YAML documents are untouched.

## Platform Support

The plugin bundles the `rite-ls` binary for:

- `darwin-arm64` (macOS Apple Silicon, including Rosetta 2)
- `darwin-x86_64` (macOS Intel)
- `linux-arm64`
- `linux-x86_64`
- `windows-x86_64`

## Settings

Configure a custom `rite-ls` binary under **Settings | Languages & Frameworks | Rite**. Leave the field empty to use the bundled binary.

## License

The plugin code is licensed under [MIT](LICENSE).
The bundled `rite-ls` binary is part of the [Rite](https://ritely.io) project and is distributed under its own license.
