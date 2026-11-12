# AGENTS.md

Guidance for AI coding agents working in this repository. For human-oriented information see `readme.adoc` and the
user manual (`manual/`).

## Project overview

jQAssistant is a QA tool that scans software artifacts (Java bytecode, Maven POMs, XML, JSON, YAML, ...) into a Neo4j
graph and then executes rules (concepts, constraints, groups written in Cypher) against that graph.

This repository is a multi-module Maven build containing the core framework, the bundled plugins and the two
distributions (CLI and Maven plugin).

| Module                       | Purpose                                                                                     |
|------------------------------|---------------------------------------------------------------------------------------------|
| `bom`                        | Bill of materials for jQAssistant artifacts                                                 |
| `core/shared`                | Shared utilities, configuration infrastructure, plugin/artifact resolution (Aether)        |
| `core/store`, `core/neo4j`   | Graph store abstraction (XO) and embedded/remote Neo4j support                              |
| `core/scanner`               | Scanner API/SPI and implementation                                                          |
| `core/rule`                  | Rule model and readers (XML, YAML, AsciiDoc)                                                |
| `core/analysis`              | Rule execution (concepts, constraints, groups, baseline)                                   |
| `core/report`                | Report plugins (XML, HTML, ...)                                                             |
| `core/runtime`               | Plugin repository and runtime wiring                                                        |
| `core/schemata`              | XSD/JSON schemas (plugin descriptor, rules, reports)                                       |
| `core/artifact-resolver`     | Resolution of plugin artifacts                                                              |
| `core/test`                  | Test framework for plugins (`AbstractPluginIT`, matchers)                                   |
| `plugin/*`                   | Bundled plugins: `common`, `java`, `java-annotation`, `junit`, `json`, `maven`, `xml`, `yaml` |
| `distribution-specification` | Shared specification/tests for the distributions                                            |
| `cli`                        | Command line distribution (`application`, `neo4jv4`, `neo4jv5`, `assembly`, `test`)        |
| `maven`                      | Maven plugin distribution (Invoker ITs under `maven/src/it`)                                |
| `manual`, `arc42`            | AsciiDoc user manual and architecture documentation                                         |

Modules typically follow an `api` / `spi` / `impl` package split
(e.g. `com.buschmais.jqassistant.core.analysis.{api,spi,impl}`). Keep public contracts in `api`/`spi`; do not let
`api` depend on `impl`.

## Build and test

Prerequisites: JDK 11, 17 (recommended) or 21, Maven 3.8+. Snapshot dependencies are resolved from the Sonatype OSS
snapshot repository (see `readme.adoc`).

```shell
# Compile and run unit tests (*Test)
mvn verify

# Also run integration tests (*IT) and Maven Invoker tests (src/it) – slow, 30+ minutes for the full build
mvn verify -PIT

# Build/test a single module and the modules it depends on
mvn verify -pl plugin/java -am

# Run a single integration test in a module
mvn verify -PIT -pl plugin/java -Dit.test=LambdaMethodIT -Dtest=NoSuchTest -Dsurefire.failIfNoSpecifiedTests=false
```

- The Neo4j version is selected by JDK: JDK 11 activates profile `neo4jv4` (Neo4j 4.4), JDK 17+ activates `neo4jv5`
  (Neo4j 5.x). Code in `core` and `plugin` must work with both.
- The compiler target is Java 11 (`java.release`). Do not use language features or APIs newer than Java 11 outside of
  modules that are explicitly built for newer JDKs.
- Prefer running the narrowest build that covers a change (`-pl ... -am`); only run the full `-PIT` build when
  changes affect several modules or distributions.
- Checkstyle (`jqa-code-style.xml` from `jqassistant-build`) runs during the build and covers test sources too – a
  violation fails the build.
- The build analyzes itself with jQAssistant (`.jqassistant.yml`, rules in `jqassistant/`). A baseline
  (`jqassistant/jqassistant-baseline.xml`) is active; do not extend the baseline to silence new violations – fix
  them instead unless explicitly told otherwise.

## Code conventions

- Formatting: follow `.editorconfig` (UTF-8, LF, 4 spaces for Java/XML, final newline) and the IDE settings from
  [jqassistant-build/ide](https://github.com/jQAssistant/jqassistant-build/tree/main/ide). Import order: `java.*`,
  `javax.*`, then `com.buschmais.*`, then other third-party packages, static imports last – match surrounding files.
- Lombok is used (`@Getter`, `@RequiredArgsConstructor`, `@Slf4j`, `@Builder`, ...). `lombok.config` marks generated
  code with `@Generated`.
- Configuration is defined as SmallRye `@ConfigMapping` interfaces (prefix `jqassistant.*`). Every new property needs
  a `@Description`, a sensible `@WithDefault` or `Optional<>`, and documentation in the manual.
- Graph model: descriptors are XO interfaces (`*Descriptor`, annotated with `@Label`, `@Relation`, `@Property`). New
  descriptor types of a plugin must be registered in the `<model>` section of its
  `src/main/resources/META-INF/jqassistant-plugin.xml`.
- Always specify charsets explicitly when reading/writing files or streams (no platform default).
- Keep backward compatibility of public APIs, SPIs, rule IDs, graph labels/relations and configuration properties.
  Changing any of them is a breaking change for users and third-party plugins – deprecate first and call it out.
- Use SLF4J for logging; do not write to `System.out`/`System.err` outside the CLI.

## Plugins and rules

- A plugin is described by `META-INF/jqassistant-plugin.xml` (scanners, model classes, rules, report plugins).
- Rules live in `src/main/resources/META-INF/jqassistant-rules/` as XML (or YAML/AsciiDoc). Rule IDs are namespaced by
  plugin (e.g. `java:TestMethod`, `junit5:TestClass`).
- Cypher must be valid for both Neo4j 4.4 and 5.x. Expensive concepts that write large numbers of relations use
  `CALL { ... } IN TRANSACTIONS` (see `java-classpath.xml`).
- Every new or changed concept/constraint needs an integration test and documentation (rule descriptions are rendered
  into the manual).

## Testing

- Unit tests: `*Test`, JUnit 5 and Mockito.
- Use AssertJ (`org.assertj.core.api.Assertions.assertThat`) for all assertions. Do not write new assertions with
  Hamcrest or JUnit `Assertions`.
- Existing tests using Hamcrest are to be migrated: when touching such a test class, rewrite all of its assertions to
  AssertJ and remove the Hamcrest imports. Replace Hamcrest matchers (e.g. `MethodDescriptorMatcher`) with AssertJ
  equivalents (e.g. extracting properties or custom `Condition`s).
- Integration tests: `*IT`, extending `AbstractPluginIT` (or a plugin-specific subclass such as
  `AbstractJavaPluginIT`). Typical pattern: `scanClasses(...)` / scan test resources, `applyConcept(...)` or
  `validateConstraint(...)`, then query the store inside `store.beginTransaction()` / `store.commitTransaction()`.
- Test data classes for the Java plugin live under `.../test/set/...`.
- Maven plugin behaviour is tested with Maven Invoker projects under `maven/src/it/`; CLI behaviour in `cli/test`.
- Add a regression test for every bug fix.

## Documentation

- User documentation is AsciiDoc: `manual/src/main/asciidoc`, plus per-plugin docs in `plugin/*/src/main/asciidoc`.
- Document new configuration properties, rules, scanner features and labels/relations in the corresponding AsciiDoc.
- Release notes are generated by release-drafter from PR titles/labels; the `release-notes.adoc` files are historical.

## Git and pull requests

- Branch from `master` (bug fixes for older lines go to `bugfix/*`). Branch names usually start with the issue
  number, e.g. `1392-custom-assert-method`.
- Commit messages and PR titles reference the GitHub issue: `#<issue> <imperative summary>`.
- For non-trivial changes an issue should exist before a PR is opened.
- Do not bump dependency versions as part of unrelated changes (Dependabot handles version bumps); never change the
  parent `jqassistant-build` version or project version unless asked.
- Do not commit build output (`target/`) or IDE files.
    
## What not to do

- Do not run `mvn deploy`, `mvn release:*` or anything publishing artifacts.
- Do not reformat whole files or reorder imports in code you are not otherwise changing.
- Do not add new third-party dependencies without a clear need; manage versions via properties in the root `pom.xml`.
