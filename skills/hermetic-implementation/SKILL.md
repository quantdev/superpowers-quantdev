---
name: hermetic-implementation
description: Use while writing or modifying any code — implementing tasks, fixing bugs, refactoring. Three local design rules applied at generation time, and a hard boundary on what you may not self-certify.
---

# Hermetic Implementation

The architecture rules you apply while writing code live in ONE place: the
**Generation-time rules** block in `../designing-decomposition/PRINCIPLES.md`.
Read that block before your first unit of code in a session and apply it to
every unit you write. It is a registry your human partner extends over time —
never rely on a remembered copy.

In TDD: shape the API against the rules at RED (the test demands the interface),
keep GREEN minimal, enforce the rules at REFACTOR. Justify any violation out
loud — an unjustified one is a bug; a justified one (hot-path mutation,
contained and named) is a decision.

**Hard boundary:** cohesion, coupling, and context integrity need context you do
not have here. They belong to the design gate's cold reviewer and your human
partner. Do not claim them — that is hallucinated compliance, not verification.
