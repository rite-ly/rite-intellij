# Rite IntelliJ Plugin

IntelliJ plugin adding LSP-based language support for `.rite.yaml` ceremony files.

## Platform Support

The plugin bundles the `rite-ls` binary for all supported platforms:

- `darwin-arm64` (macOS Apple Silicon, including Rosetta 2)
- `darwin-x86_64` (macOS Intel)
- `linux-arm64`
- `linux-x86_64`

## Development

Run a sandboxed IDE with the plugin loaded:
```bash
./gradlew runIde
```

Build the distributable ZIP:
```bash
./gradlew buildPlugin
```
The output is in `build/distributions/`.

## Testing

Open a file ending in `.rite.yaml` in the sandbox IDE. The LSP server should start automatically — check the **LSP** tool window for status and logs.
