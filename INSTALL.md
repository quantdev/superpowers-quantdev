# Installing superpowers-quantdev (this fork)

This is a personal fork of [obra/superpowers](https://github.com/obra/superpowers),
tailored for Java/Maven development with a hermetic design gate
(`designing-decomposition`), generation-time architecture rules, and an extensible
rules registry (`skills/designing-decomposition/PRINCIPLES.md`).

**Do not follow the upstream README's install instructions** — the official
marketplace installs upstream superpowers, not this fork. Both plugins are named
`superpowers`, so install exactly one of them.

## Prerequisites

- Claude Code
- This repo cloned locally (the marketplace points at the working tree, so keep
  the clone where it is — moving it breaks the install)

## Install

In any Claude Code session, run (adjust the path if your clone lives elsewhere):

```
/plugin marketplace add /Users/leemic/Devel/github/superpowers-quantdev
/plugin install superpowers@superpowers-dev
```

`superpowers-dev` is the marketplace name declared in
`.claude-plugin/marketplace.json`; its plugin source is `./`, i.e. this directory.

Then **start a fresh session**. The plugin's SessionStart hook loads the
`using-superpowers` bootstrap at startup — the session you installed from does
not have it.

## Verify

1. **Bootstrap check** — in a fresh session, send exactly:

   > Let's make a react todo list

   A working install auto-triggers the `brainstorming` skill before any code is
   written. If the agent starts scaffolding instead, the bootstrap didn't load.

2. **Design gate check** — run a small feature in a Java repo. After you approve
   the spec, the agent must announce `designing-decomposition` and refuse to plan
   or code until the decomposition design passes cold review and your explicit
   approval. A bugfix should instead announce the skip
   ("Decomposition gate skipped: ...") and go straight to a TDD fix.

3. `/plugin` (no arguments) opens the manager UI showing what's installed and
   enabled.

## Updating after editing skills

Installed plugins are cached copies — they do not live-track this directory.
After changing any skill (e.g. adding a rule to the PRINCIPLES.md registry):

```
git commit ...                                # keep the registry history clean
/plugin marketplace update superpowers-dev
/plugin update superpowers
```

then start a fresh session. Habit: **registry edit → commit → update → fresh
session.**

## Uninstall / switching to upstream

```
/plugin uninstall superpowers
/plugin marketplace remove superpowers-dev
```

Only then install upstream from the official marketplace, if desired. Never have
both installed at once — they share the plugin name and their skills collide.

## Troubleshooting

- **Skills never trigger** — the bootstrap isn't loading. Confirm the plugin is
  enabled in `/plugin`, and that the session is new (hooks fire on
  startup/clear/compact, not mid-session).
- **Old skill content after an edit** — you skipped the update step above; the
  cache is stale.
- **`designing-decomposition` doesn't fire after spec approval** — check that
  `skills/brainstorming/SKILL.md` still names it as the terminal state (a bad
  merge from upstream can revert the rewiring; see `git log skills/brainstorming/`).
