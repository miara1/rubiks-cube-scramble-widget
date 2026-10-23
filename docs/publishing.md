# Publish the project on GitHub

Suggested repository name: **rubiks-cube-scramble-widget**.

Suggested description:

> Offline Windows desktop widget for 3x3 Rubik's Cube random-state scrambles. Global hotkeys, TNoodle, portable app and optional autostart.

Suggested GitHub topics:

```text
rubiks-cube rubik speedcubing scramble scrambler tnoodle windows desktop-widget offline java swing global-hotkeys
```

## First publication

Create an empty public GitHub repository without adding a generated README or license. Then, from the project folder:

```powershell
git init -b main
git add .
git diff --cached --stat
git commit -m "Add offline Rubik's Cube scramble widget for Windows"
git remote add origin https://github.com/YOUR-USERNAME/rubiks-cube-scramble-widget.git
git push -u origin main
```

Replace `YOUR-USERNAME` with the actual owner. `target`, `dist`, `.build`, local caches and Java runtimes are ignored. Review staged files before committing. No remote repository URL is claimed until one actually exists.

Set the About description and topics on GitHub. The English README explains the purpose, Windows requirements, exact controls, offline behavior, TNoodle usage and installation. It includes a screenshot and build instructions. These help search engines and AI tools understand and link the project; recommendation or indexing is not guaranteed. Keep descriptions factual and avoid keyword stuffing.

## Release

1. Review the changes, license notices and README. Set the version in `pom.xml`.
2. Build and test on Windows with `build.cmd`. Try the ZIP on a Windows account without a separate Java installation, including display scaling, global shortcuts, tray exit and optional autostart.
3. Commit the sources and create a matching tag:

   ```powershell
   git tag v1.0.0
   git push origin v1.0.0
   ```

4. GitHub Actions creates a **draft** release. Check that it includes the binary ZIP, matching source ZIP and `SHA256SUMS.txt`. Publish the draft when ready.

The source ZIP contains project sources/build scripts and matching dependency source JARs. Distribute it alongside the binary ZIP. Preserve third-party notices and the Java runtime's legal files. The Java runtime is generated from the JDK used for the build; use a maintained redistributable OpenJDK distribution and preserve its source information.

The app is unsigned. Signing can be added when there is a suitable certificate; it is not required to build or use a personal portable copy. Do not advertise the widget as WCA-approved competition software.
