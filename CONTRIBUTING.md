# Contributing

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

To run against a locally built `rite-ls`, open the sandbox IDE's **Settings | Languages & Frameworks | Rite** and set the path to your binary. The same setting is used by end users.

## Testing

Open a file ending in `.rite.yaml` in the sandbox IDE. The LSP server should start automatically; check the **LSP** tool window for status and logs.

## Updating the bundled rite-ls

The bundled `rite-ls` version is pinned in `gradle.properties` under `riteLsVersion`. Renovate auto-opens PRs when a new release lands on [`rite-ly/rite`](https://github.com/rite-ly/rite/releases), running `scripts/download-rite-ls.sh` as a post-upgrade task to refresh the binaries under `src/main/resources/bin/`.

To update manually, pass the target version:

```bash
./scripts/download-rite-ls.sh 0.1.0-rc.8
```
