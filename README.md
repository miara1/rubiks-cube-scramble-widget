# Rubik's Cube Scramble Widget for Windows

An **offline Windows desktop widget for 3×3 Rubik's Cube scrambles**, with global keyboard shortcuts and a compact, single-line white strip in the bottom-right corner. Random-state scrambles are generated locally by **TNoodle-lib**, the scrambling engine from the TNoodle project.

No browser, account, internet connection or separate Java installation is needed to use the packaged app.

![The widget: a white card with a 3x3 scramble and a refresh button](docs/screenshot.png)

[Polski / Polish instructions](README.pl.md)

## Download and run

1. Open this repository's **Releases** section and download `rubiks-cube-scramble-widget-VERSION-windows-x64.zip`.
2. Extract the entire ZIP to a folder you want to keep.
3. Run **`RubiksCubeScrambleWidget.exe`** inside that folder.

Requires **64-bit Windows 10 or Windows 11**. The release includes its own reduced Java runtime. Keep the `app` and `runtime` folders alongside the executable. You do not need administrator privileges.

If this repository has no release yet, build it using the instructions below. The app is currently unsigned; Windows may show an unknown-publisher warning. Only run releases from a source you trust. SHA-256 checksums accompany each packaged release.

## Controls

| Action | Control |
| --- | --- |
| Show or hide the panel | **Ctrl + Shift + Space** |
| Generate the next scramble | **Ctrl + Shift + F9** or the **↻** button |
| Open the tray menu | Right-click the cube icon in the notification area |
| Show or hide using the tray | Double-click the cube icon |
| Enable optional autostart | Tray menu → **Start with Windows** |
| Exit completely | Tray menu → **Quit** |

The panel appears above ordinary application windows on the monitor containing the mouse pointer, above the taskbar. Showing it does not take keyboard focus away from the current application. Hiding it preserves the scramble. Generating a scramble while hidden leaves the panel hidden. Busy requests are ignored; one next scramble is prepared in the background.

The strip is 440 × 28 logical pixels, with 12-point text and a small refresh icon. Longer scrambles use a slightly smaller font to fit on one line without ellipses. Windows display scaling applies normally.

Autostart is **off by default**. When enabled, the widget starts hidden at the next login. Disable it before moving or deleting the app's folder; enable it again after moving. Only one instance runs per Windows user.

## Change shortcuts

Use **Edit shortcuts (restart to apply)** in the tray menu. Settings are stored in:

```text
%APPDATA%\RubiksCubeScrambleWidget\settings.properties
```

```properties
toggle=Ctrl+Shift+Space
next=Ctrl+Shift+F9
```

Supported modifiers: `Ctrl`, `Shift`, `Alt`. Supported keys: `Space`, `A`–`Z`, `0`–`9`, `F1`–`F11`. Include Ctrl or Alt, and choose two distinct shortcuts. Avoid `Ctrl+Alt` with letters because it can interfere with AltGr and international keyboard layouts. Windows-key shortcuts and the reserved F12 key are not supported. If another app has claimed a shortcut, the widget reports it and the tray menu remains usable.

Quit and restart after editing. If settings are invalid, fix or delete the file to restore defaults. Diagnostics are written to `widget.log` in the same folder; logs are local and size-limited.

## Build from source

Install a **64-bit JDK 21**, for example [Eclipse Temurin](https://adoptium.net/), and put its `bin` folder on PATH or set `JAVA_HOME`. Maven is downloaded automatically by the included official Maven Wrapper. Building initially requires internet to obtain tools and dependencies; the built app runs offline.

From PowerShell in the project directory:

```powershell
.\build.cmd
```

Outputs:

```text
dist/RubiksCubeScrambleWidget/RubiksCubeScrambleWidget.exe
dist/rubiks-cube-scramble-widget-1.0.0-windows-x64.zip
dist/rubiks-cube-scramble-widget-1.0.0-source.zip
dist/SHA256SUMS.txt
```

Run `run.cmd` to launch the packaged app. To compile and test without packaging:

```powershell
.\mvnw.cmd -B verify
```

Run the optional desktop integration tests in an interactive Windows session (they briefly show the panel and press their own registered test shortcuts):

```powershell
.\mvnw.cmd -B "-DdesktopTests=true" test
```

After packaging, `powershell -File scripts/smoke-test-windows.ps1` checks the real `.exe` with isolated settings. It starts the app hidden, checks startup/logs, reports a local memory measurement and stops its own test process. Run it in an interactive Windows session with a system tray.

## Small architecture

- **Java 21 + Swing:** one small native desktop window and a system tray icon.
- **TNoodle-lib 0.20.0:** 3×3 random-state generation, including TNoodle's distance filter. No custom scrambling algorithm.
- **JNA:** Windows `RegisterHotKey` and the optional per-user autostart entry. No keyboard hook or polling loop.
- **Maven + jlink + jpackage:** pinned dependency versions, tests and a portable Windows build with a reduced runtime.

There is no web server, telemetry, auto-updater, database or online service. Dependency JARs remain unmodified. Generated files and bundled Java stay outside Git in ignored directories. The installed app is larger than its source because it includes Java; download size is separate from memory usage.

This is an independent **training utility**, not an official WCA competition application. Official competitions must use the current official WCA scramble program and procedures. See [TNoodle-lib](https://github.com/thewca/tnoodle-lib) and [WCA scrambles](https://www.worldcubeassociation.org/regulations/scrambles/).

## Contributing and publishing

Bug reports should include the Windows version, display scaling, widget version and relevant local log messages. Please keep changes focused on the small offline widget.

The Windows workflow tests and packages pushes and pull requests. Pushing a version tag such as `v1.0.0` creates a **draft** GitHub release with the binary ZIP, matching source ZIP and checksums. Review it before publishing. See [publishing instructions](docs/publishing.md) for repository setup, search topics and the release checklist.

## License

This project's code is licensed under **GPL-3.0-only**. Third-party components retain their own licenses. See [LICENSE](LICENSE), [third-party notices](THIRD-PARTY-NOTICES.md), and the runtime's `legal` directory. Distribute the matching source ZIP alongside the binary ZIP.
