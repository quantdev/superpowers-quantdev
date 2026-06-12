# Design Principles — Hermetic Code

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

## Who can verify what (the trust map)

| Property | Self-checkable while writing? | Verified by |
|---|---|---|
| Purity, Tell-Don't-Ask, minimal surface | Yes — visible in-context | generator + reviewers |
| Cohesion, coupling | Weakly — needs the whole unit and neighbors | cold reviewer + human |
| Bounded context | No — needs the system | reviewer's import heuristic + human gate |

The bottom rows are why the design gate and cold reviewer exist. Claiming them
from inside an implementation context is hallucinated compliance. (Deterministic
enforcement — ArchUnit — can harden the bottom rows later; it goes through the
java-development dependency gate like everything else.)
