# Design Principles — Hermetic Code

**This file is the single registry of architectural guidance.** Every stage
consumes it — the design reviewer reads it whole, plans copy the Generation-time
rules block into their Conventions section, the SDD controller pastes that block
into implementer and reviewer prompts. To add or tune a principle: edit this file
only — prose below for reviewers and humans, one line in the Generation-time
rules block if it's checkable while writing code, one row in the trust map.
Nothing else restates the rules.

One property underneath everything here: a hermetic unit can be understood,
changed, and tested without understanding, changing, or testing everything
around it.

You already know these principles. What this file adds is how to apply them
honestly: **evidence before verdict** — list the actual methods, state, imports,
or contracts first, then judge. Asserting compliance without evidence is how
reviews hallucinate. A test that fires is a flag to justify, not an automatic fail.

1. **High cohesion, low coupling.** One responsibility per unit, owning its own
   state; few boundary crossings, none reaching into another unit's internals.
   *Exemptions: stateless utilities; framework-inflated imports — judge, don't count.*
2. **Side-effect-free logic.** Don't mutate arguments or outside state; same
   inputs, same output; effects pushed to the edges. Local mutation that never
   escapes the function is not a side effect. The hidden Java offenders: static
   mutable fields, un-injected clocks, unseeded randomness.
3. **Bounded context.** A term means one thing inside its boundary; contexts
   translate at the border instead of sharing models. In this codebase, a Maven
   module is the default physical form of a context.
4. **Encapsulation.** Package-private by default — public only what a caller in
   another package needs. Never hand out live internal mutable references
   (List.copyOf, defensive copies). Tell, don't ask: decisions live with the
   data. *Records and DTOs legitimately skew public.*
5. **Composition over inheritance.** Inheritance couples a subclass to its
   parent's internals — change the base, break the children. Prefer composition
   (delegate to owned collaborators) and interfaces (sealed interfaces for closed
   sets of implementations); records + interfaces for data shapes. `extends` on a
   concrete class is a flag: justify it with a genuine, stable is-a relationship
   the design names. *Framework-mandated inheritance is a legitimate justification.*

## Generation-time rules

The locally-checkable subset, in paste-able form. Plans copy this block into
their Conventions section; the SDD controller pastes it into implementer and
quality-reviewer prompts; inline coding sessions read it via
superpowers:hermetic-implementation. Keep it short — every line costs context
in every prompt it travels in.

> 1. **Side-effect-free:** don't mutate arguments or outside state; same inputs →
>    same output; effects at the edges. Local mutation that never escapes is fine.
> 2. **Tell-Don't-Ask:** pulling data out of an object to decide on it means the
>    decision belongs in the object.
> 3. **Minimal surface:** package-private by default; nothing public without a
>    caller that needs it; never return live internal mutable references.
> 4. **Composition over inheritance:** `extends` on a concrete class is a flag —
>    prefer composition and (sealed) interfaces; justify any inheritance with a
>    genuine, stable is-a.
>
> A rule that fires is a flag to justify, not an automatic fail — an unjustified
> flag is a bug; a justified one is a documented decision.

## Who can verify what (the trust map)

| Property | Self-checkable while writing? | Verified by |
|---|---|---|
| Purity, Tell-Don't-Ask, minimal surface | Yes — visible in-context | generator + reviewers |
| Composition over inheritance | Yes — `extends` is visible in-context | generator + reviewers |
| Cohesion, coupling | Weakly — needs the whole unit and neighbors | cold reviewer + human |
| Bounded context | No — needs the system | reviewer's import heuristic + human gate |

The bottom rows are why the design gate and cold reviewer exist. Claiming them
from inside an implementation context is hallucinated compliance. (Deterministic
enforcement — ArchUnit — can harden the bottom rows later; it goes through the
java-development dependency gate like everything else.)
