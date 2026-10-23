# Third-party components

Original upstream copyright and license notices remain in the unmodified JARs and matching source JARs. No affiliation with or endorsement by the World Cube Association is implied.

| Component | Version | License | Upstream |
| --- | --- | --- | --- |
| TNoodle lib-scrambles, lib-svglite and scrambler modules | 0.20.0 | GPL v3 | https://github.com/thewca/tnoodle-lib |
| Java Native Access (jna and jna-platform) | 5.19.1 | Apache License 2.0 option of the upstream dual license | https://github.com/java-native-access/jna |
| GWT Exporter | 2.5.1 | Apache License 2.0 | https://github.com/manolo/gwt-exporter |
| SLF4J API | 2.0.20 | MIT | https://www.slf4j.org/ |
| Apache Maven Wrapper (build scripts) | 3.3.4 | Apache License 2.0 | https://maven.apache.org/wrapper/ |

TNoodle incorporates work by Jeremy Fleischman, Ryan Zheng, Clément Gallet, Shuang Chen, Bruce Norskog, Lucas Garron and other contributors credited upstream. JNA is copyright its authors, including Timothy Wall and contributors. The Maven Wrapper scripts retain their Apache Software Foundation license headers.

License texts: [GPL v3](LICENSE), [Apache License 2.0](licenses/Apache-2.0.txt), [SLF4J MIT](licenses/SLF4J-MIT.txt). The test-only JUnit dependency is EPL-2.0 and is not included in the app runtime.

The portable Windows release also bundles a reduced **OpenJDK runtime** from the build JDK, licensed under GPL v2 with the Classpath Exception and additional component-specific licenses. Its exact version is in `runtime/release`; all runtime legal notices are retained under `runtime/legal`, including upstream source information where supplied by the JDK. OpenJDK source: https://github.com/openjdk/jdk21u ; Eclipse Temurin builds and corresponding source releases: https://github.com/adoptium/temurin21-binaries/releases .

The matching source archive is distributed alongside each binary archive and includes dependency source JARs. Build-only tools and test dependencies are resolved by Maven and are not packaged into the app.
