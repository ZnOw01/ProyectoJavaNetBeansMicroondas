# External parser dependencies

This project depends on the JFlex lexer generator and the Java CUP parser generator at build time.
To keep the repository lightweight and avoid committing large binary artifacts, the required jars are
not included directly. Download them into this folder before compiling the project (the suggested
versions are JFlex 1.6.1 and Java CUP 11b-20160615):

- `JFlex.jar`
- `java-cup.jar`
- `java-cup-runtime.jar`

The `scripts/fetch-libs.sh` helper can download the versions used by the project automatically.
After running it, the NetBeans project configuration will pick them up from this directory.
