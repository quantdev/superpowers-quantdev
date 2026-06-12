# Defense-in-Depth Validation

## Overview

When you fix a bug caused by invalid data, adding validation at one place feels sufficient. But that single check can be bypassed by different code paths, refactoring, or mocks.

**Core principle:** Validate at EVERY layer data passes through. Make the bug structurally impossible.

## Why Multiple Layers

Single validation: "We fixed the bug"
Multiple layers: "We made the bug impossible"

Different layers catch different cases:
- Entry validation catches most bugs
- Business logic catches edge cases
- Environment guards prevent context-specific dangers
- Debug logging helps when other layers fail

## The Four Layers

### Layer 1: Entry Point Validation
**Purpose:** Reject obviously invalid input at API boundary

```java
Project createProject(String name, Path workingDirectory) {
    if (workingDirectory == null || workingDirectory.toString().isBlank()) {
        throw new IllegalArgumentException("workingDirectory cannot be empty");
    }
    if (!Files.exists(workingDirectory)) {
        throw new IllegalArgumentException("workingDirectory does not exist: " + workingDirectory);
    }
    if (!Files.isDirectory(workingDirectory)) {
        throw new IllegalArgumentException("workingDirectory is not a directory: " + workingDirectory);
    }
    // ... proceed
}
```

`Objects.requireNonNull` with a message is the right tool for plain null checks; explicit `IllegalArgumentException` for everything richer.

### Layer 2: Business Logic Validation
**Purpose:** Ensure data makes sense for this operation

```java
void initializeWorkspace(Path projectDir, String sessionId) {
    if (projectDir == null) {
        throw new IllegalStateException("projectDir required for workspace initialization");
    }
    // ... proceed
}
```

### Layer 3: Environment Guards
**Purpose:** Prevent dangerous operations in specific contexts

```java
void gitInit(Path directory) throws IOException, InterruptedException {
    // In tests, refuse git init outside the temp directory
    if (Boolean.getBoolean("test.mode")) { // set by surefire: <test.mode>true</test.mode>
        Path normalized = directory.toAbsolutePath().normalize();
        Path tmpDir = Path.of(System.getProperty("java.io.tmpdir")).toAbsolutePath().normalize();

        if (!normalized.startsWith(tmpDir)) {
            throw new IllegalStateException(
                "Refusing git init outside temp dir during tests: " + directory);
        }
    }
    // ... proceed
}
```

### Layer 4: Debug Instrumentation
**Purpose:** Capture context for forensics

```java
void gitInit(Path directory) throws IOException, InterruptedException {
    if (LOG.isLoggable(Level.FINE)) {
        LOG.log(Level.FINE,
            "About to git init: dir=" + directory + ", cwd=" + Path.of("").toAbsolutePath(),
            new Throwable("call site")); // logs the full stack trace
    }
    // ... proceed
}
```

## Applying the Pattern

When you find a bug:

1. **Trace the data flow** - Where does bad value originate? Where used?
2. **Map all checkpoints** - List every point data passes through
3. **Add validation at each layer** - Entry, business, environment, debug
4. **Test each layer** - Try to bypass layer 1, verify layer 2 catches it

## Example from Session

Bug: Empty `projectDir` caused `git init` in the source tree

**Data flow:**
1. Test setup → empty path
2. `Project.create(name, Path.of(""))`
3. `WorkspaceManager.createWorkspace(emptyPath)`
4. `git init` runs in the process working directory — the source checkout!

(`Path.of("")` resolves to the current working directory — the Java twin of the empty-string `cwd` bug.)

**Four layers added:**
- Layer 1: `Project.create()` validates not empty/exists/writable
- Layer 2: `WorkspaceManager` validates projectDir not empty
- Layer 3: `WorktreeManager` refuses git init outside `java.io.tmpdir` when `test.mode` is set
- Layer 4: Stack trace logging before git init

**Result:** All 1847 tests passed, bug impossible to reproduce

## Key Insight

All four layers were necessary. During testing, each layer caught bugs the others missed:
- Different code paths bypassed entry validation
- Mocks bypassed business logic checks
- Edge cases on different platforms needed environment guards
- Debug logging identified structural misuse

**Don't stop at one validation point.** Add checks at every layer.
