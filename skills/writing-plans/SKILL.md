---
name: writing-plans
description: Use when you have a spec or requirements for a multi-step task, before touching code
---

# Writing Plans

## Overview

Write comprehensive implementation plans assuming the engineer has zero context for our codebase and questionable taste. Document everything they need to know: which files to touch for each task, code, testing, docs they might need to check, how to test it. Give them the whole plan as bite-sized tasks. DRY. YAGNI. TDD. Frequent commits.

Assume they are a skilled developer, but know almost nothing about our toolset or problem domain. Assume they don't know good test design very well.

**Announce at start:** "I'm using the writing-plans skill to create the implementation plan."

**Context:** If working in an isolated worktree, it should have been created via the `superpowers:using-git-worktrees` skill at execution time.

**Gate — already decomposed?** Planning requires an approved decomposition design in `docs/superpowers/design/`. If none exists, run the scope test in superpowers:designing-decomposition: if it says run, invoke that skill first; if it says skip, announce the skip and proceed. This guard exists because work can arrive here directly (ready-made spec) without passing through brainstorming.

**Save plans to:** `docs/superpowers/plans/YYYY-MM-DD-<feature-name>.md`
- (User preferences for plan location override this default)

## Scope Check

If the spec covers multiple independent subsystems, it should have been broken into sub-project specs during brainstorming. If it wasn't, suggest breaking this into separate plans — one per subsystem. Each plan should produce working, testable software on its own.

## File Structure

Derive the file structure from the approved decomposition design — the plan maps the design's modules onto concrete files; it does not invent structure. Module boundaries, interfaces, and dependency directions were decided and reviewed at the design gate.

Within that frame, file-level judgment is still yours:

- Each file should have one clear responsibility. You reason best about code you can hold in context at once — prefer smaller, focused files over large ones that do too much.
- Files that change together should live together.
- In existing codebases, follow established patterns. If a file you're modifying has grown unwieldy, including a split in the plan is reasonable — within its module's boundary.

Each task should produce self-contained changes that make sense independently. If mapping tasks to files reveals the decomposition itself is wrong, stop — that goes back through designing-decomposition's amendment path, not around it.

## Bite-Sized Task Granularity

**Each step is one action (2-5 minutes):**
- "Write the failing test" - step
- "Run it to make sure it fails" - step
- "Implement the minimal code to make the test pass" - step
- "Run the tests and make sure they pass" - step
- "Commit" - step

## Plan Document Header

**Every plan MUST start with this header:**

```markdown
# [Feature Name] Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** [One sentence describing what this builds]

**Architecture:** [2-3 sentences about approach]

**Tech Stack:** [Key technologies/libraries]

**Design:** [Path to the approved decomposition design, e.g. `docs/superpowers/design/YYYY-MM-DD-<feature>-decomposition.md` — or "decomposition gate skipped: <reason>"]

**Conventions:** [For Java projects: "Follow superpowers:java-development — Maven multi-module, JUnit 5, no new dependencies without explicit approval, Spotless/match-existing formatting." Then paste the current "Generation-time rules" block from `skills/designing-decomposition/PRINCIPLES.md` verbatim. Subagents executing tasks have no session context — the plan must carry the conventions and the rules as they stood when it was written.]

---
```

## Task Structure

````markdown
### Task N: [Component Name]

**Module:** `exact-module-name` (from the decomposition design; for multi-module Maven projects)

**Files:**
- Create: `module/src/main/java/com/example/feature/Thing.java`
- Modify: `module/src/main/java/com/example/feature/Existing.java:123-145`
- Test: `module/src/test/java/com/example/feature/ThingTest.java`

- [ ] **Step 1: Write the failing test**

```java
@Test
void specificBehavior() {
    var result = Thing.function(input);
    assertEquals(expected, result);
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn -pl module test -Dtest=ThingTest#specificBehavior`
Expected: FAIL with assertion error (if the class/method doesn't exist yet, create the minimal skeleton throwing `UnsupportedOperationException` so it compiles and fails the assertion)

- [ ] **Step 3: Write minimal implementation**

```java
static Result function(Input input) {
    return expected;
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn -pl module test -Dtest=ThingTest#specificBehavior`
Expected: BUILD SUCCESS

- [ ] **Step 5: Commit**

```bash
git add module/src/test/java/com/example/feature/ThingTest.java \
        module/src/main/java/com/example/feature/Thing.java
git commit -m "feat: add specific feature"
```
````

## No Placeholders

Every step must contain the actual content an engineer needs. These are **plan failures** — never write them:
- "TBD", "TODO", "implement later", "fill in details"
- "Add appropriate error handling" / "add validation" / "handle edge cases"
- "Write tests for the above" (without actual test code)
- "Similar to Task N" (repeat the code — the engineer may be reading tasks out of order)
- Steps that describe what to do without showing how (code blocks required for code steps)
- References to types, functions, or methods not defined in any task

## Remember
- Exact file paths always
- Complete code in every step — if a step changes code, show the code
- Exact commands with expected output
- DRY, YAGNI, TDD, frequent commits

## Self-Review

After writing the complete plan, look at the spec with fresh eyes and check the plan against it. This is a checklist you run yourself — not a subagent dispatch.

**1. Spec coverage:** Skim each section/requirement in the spec. Can you point to a task that implements it? List any gaps.

**2. Placeholder scan:** Search your plan for red flags — any of the patterns from the "No Placeholders" section above. Fix them.

**3. Type consistency:** Do the types, method signatures, and property names you used in later tasks match what you defined in earlier tasks? A function called `clearLayers()` in Task 3 but `clearFullLayers()` in Task 7 is a bug.

**4. Architecture-rules pass:** Plan code is code — the code you wrote into the plan is the code that ships, and this is the cheapest point in the pipeline to fix it. Check every code block against the Generation-time rules in `skills/designing-decomposition/PRINCIPLES.md`: argument mutation, pull-data-and-decide callers, over-exposed members, concrete inheritance. The implementer is instructed to flag plan code that violates the rules — don't make them.

If you find issues, fix them inline. No need to re-review — just fix and move on. If you find a spec requirement with no task, add the task.

## Execution Handoff

After saving the plan, offer execution choice:

**"Plan complete and saved to `docs/superpowers/plans/<filename>.md`. Two execution options:**

**1. Subagent-Driven (recommended)** - I dispatch a fresh subagent per task, review between tasks, fast iteration

**2. Inline Execution** - Execute tasks in this session using executing-plans, batch execution with checkpoints

**Which approach?"**

**If Subagent-Driven chosen:**
- **REQUIRED SUB-SKILL:** Use superpowers:subagent-driven-development
- Fresh subagent per task + two-stage review

**If Inline Execution chosen:**
- **REQUIRED SUB-SKILL:** Use superpowers:executing-plans
- Batch execution with checkpoints for review
