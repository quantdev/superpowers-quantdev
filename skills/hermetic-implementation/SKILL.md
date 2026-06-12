---
name: hermetic-implementation
description: Use while writing or modifying any code — implementing tasks, fixing bugs, refactoring. Three local design rules applied at generation time, and a hard boundary on what you may not self-certify.
---

# Hermetic Implementation

Three rules for every unit you write — the only design properties you can actually
verify from inside an implementation context:

1. **Side-effect-free:** don't mutate arguments or outside state; same inputs →
   same output; effects at the edges. Local mutation that never escapes is fine.
2. **Tell-Don't-Ask:** if you're pulling data out of an object to decide on it,
   the decision belongs in the object.
3. **Minimal surface:** package-private by default; nothing public without a
   caller that needs it; never return live internal mutable references.

In TDD: shape the API hermetically at RED (the test demands the interface), keep
GREEN minimal, enforce these rules at REFACTOR. Justify any violation out loud —
an unjustified one is a bug; a justified one (hot-path mutation, contained and
named) is a decision.

**Hard boundary:** cohesion, coupling, and context integrity need context you do
not have here. They belong to the design gate's cold reviewer and your human
partner. Do not claim them — that is hallucinated compliance, not verification.
