---
name: java-development
description: Use when writing, testing, building, planning, or debugging any Java code — establishes the Maven, JUnit 5, and dependency conventions every task must follow. Consult before running builds, choosing test tooling, structuring modules, or adding ANY dependency, even a test-scoped one.
---

# Java Development Conventions

## Overview

Other skills define *how* to work (TDD, debugging, planning). This skill defines *what toolchain and idioms to use* when the project is Java. Every subagent working on Java code needs these conventions — include them (or a pointer to this skill) in task prompts.

## Toolchain Defaults

| Concern | Default |
|---|---|
| Build | Maven, multi-module |
| Java version | 25 — use modern language features (records, sealed types, pattern matching) where the codebase already does |
| Unit tests | JUnit 5 (Jupiter) |
| Mocking | Mockito — sparingly; real collaborators first (see superpowers:test-driven-development anti-patterns) |
| Integration tests | Testcontainers, or docker compose if the project already uses it |
| Formatting | Spotless if configured; otherwise match the existing code exactly |

In an existing project, what's already there wins. These defaults apply when starting fresh or when the project has no established choice.

## Build & Test Commands

Run Maven from the repository root. In multi-module projects, scope to the module you're working on — full-reactor builds waste minutes per cycle:

```bash
mvn -pl <module> -am test                       # module + its upstream deps
mvn -pl <module> test -Dtest=ClassTest          # one test class
mvn -pl <module> test -Dtest=ClassTest#method   # one test method
mvn verify                                       # full build before declaring done
```

`mvn verify` (not just `test`) is the completion bar — it runs integration tests and enforcement plugins that `test` skips. A task isn't done until `mvn verify` exits 0 from the root.

Watch the build output for skipped tests. `Tests run: 34, Failures: 0, Errors: 0, Skipped: 3` is not green — find out why 3 are skipped.

## Dependency Policy — Hard Gate

Be parsimonious about external dependencies. The JDK is large and Java 25 is capable; prefer the standard library.

**Never add a dependency — including test-scoped, including a new Maven plugin — without explicitly asking your human partner first.** Name the artifact, what it's for, and what the standard-library alternative would cost. This applies to transitive-version bumps you'd make in `dependencyManagement` too.

Why this is a hard gate: each dependency is a supply-chain exposure, an upgrade burden, and a constraint on every future module. Your human partner owns that tradeoff, not you.

| Tempting reach | Reach for first |
|---|---|
| Apache Commons / Guava utilities | `java.util`, `java.util.stream`, records |
| HTTP client libraries | `java.net.http.HttpClient` |
| JSON "just for this one thing" | Ask — JSON libs are a real decision |
| Lombok | Records, compact constructors — ask before introducing |

## Multi-Module Conventions

- New code goes in the module that owns the domain concept, not wherever is convenient. If no module fits, ask your human partner before creating one.
- Inter-module dependencies flow one direction. If you need a cycle, the design is wrong — stop and rethink.
- Declare versions in the parent POM (`dependencyManagement` / `pluginManagement`); child POMs reference without versions.

## Formatting

- Project has Spotless: run `mvn spotless:apply` before committing. Never hand-fight the formatter.
- Project doesn't have Spotless: match the existing code's style exactly — indentation, import order, brace placement. Don't introduce Spotless or reformat existing files without asking your human partner.

## Integration Tests

- Default to Testcontainers for anything needing real infrastructure (databases, brokers). If the project already orchestrates with docker compose, follow that instead.
- Keep integration tests in the failsafe phase (`*IT.java`, run by `mvn verify`) so unit cycles stay fast.
- Never substitute mocks for infrastructure in an integration test — that's a unit test wearing a costume.

## Red Flags

- Adding a dependency "temporarily" or "just for tests" without asking
- `@Disabled` on a failing test to get to green
- Running only `mvn test` and declaring the task complete
- Reformatting files you didn't otherwise change
- Build passes only with `-DskipTests` or `-Dspotless.check.skip`

All of these mean: stop, fix properly, or ask your human partner.
