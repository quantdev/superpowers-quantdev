---
name: designing-decomposition
description: Use after a spec is approved and before writing an implementation plan or any code — whenever about to decide module boundaries, interfaces, or how code will be organized. Produces a short decomposition design and blocks planning until it passes cold review and human approval.
---

# Designing Decomposition

A spec says WHAT to build. A plan says IN WHAT ORDER. Neither decides HOW the code
is structured — that decision gets made implicitly, file by file, unless something
forces it into the open. This skill forces it.

**Announce at start:** "I'm using the designing-decomposition skill before planning."

**Gate:** No task plan, no code, until the design passes cold review and your human
partner approves. Silence is not approval.

**Skip only if** the change adds no new production file, no new module, and no new
cross-module dependency (test files don't count). Announce the skip. Ambiguity means run.

## The design doc

Write `docs/superpowers/design/YYYY-MM-DD-<feature>-decomposition.md`. Short — a page
is usually right. It must answer, with specifics rather than assertions:

1. **What are the modules, and what does each own?** One responsibility and its
   state, per module.
2. **Why this decomposition and not the obvious one?** Interrogate the naive
   spec-nouns-become-modules split. "The obvious split survives scrutiny because X"
   is a valid answer; an unexamined noun-split is not.
3. **What crosses each boundary?** Inputs, outputs, errors, dependency direction.
   Contracts only — no internals.
4. **What changes does this survive?** Name one realistic future change and trace it.
5. **How does it fit what exists?** Name the modules/packages you searched for
   overlapping capability and what you found. The reviewer can't see the repo —
   your evidence here is all it gets.
6. **What's the simplest end-to-end slice, and what's deferred?**

## Cold review

Never review your own design in the context that produced it. Dispatch a subagent
with `design-reviewer-prompt.md` (this directory), giving it ONLY the design doc, the
spec, and `PRINCIPLES.md`. Fix CRITICALs and re-dispatch. If the same CRITICAL
survives two fix attempts or you believe the reviewer is wrong, stop and bring both
positions to your human partner.

## After approval

Invoke superpowers:writing-plans. Every task that creates production code names its
module from this design. If implementation later contradicts the design, stop —
amend the doc, get a quick delta review and re-approval, continue. An out-of-date
design doc is worse than none.
