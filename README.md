# cloud-itonami-isco-9412

Open Occupation Blueprint for **ISCO-08 9412**: Kitchen Helpers.

This repository designs a forkable OSS business for an independent kitchen support worker: a kitchen-support robot performs dishwashing and cleaning tasks under a governor-gated actor, so the practice keeps its own food-safety and cleaning records instead of renting a closed kitchen-management SaaS.

**Maturity: `:implemented`.** `src/kitchen/` implements the
`KitchenSupportPracticeActor` as a `langgraph.graph/state-graph`
(`kitchen.actor`) wired to a `KitchenAdvisor` (`kitchen.advisor`) and an
independent `KitchenSupportGovernor` (`kitchen.governor`), following the
itonami actor pattern (ADR-2607011000): `:intake -> :advise -> :govern ->
:decide -+-> :commit (:ok?) +-> :request-approval (:escalate?, human-in-the-loop
interrupt) +-> :hold (:hard?)`. 14 tests / 30 assertions green
(`kbb -M:test`). HARD invariants (always hold, never overridable):
client provenance, no-actuation (`:effect` must be `:propose`), a registered
kitchen basis for any task, the proposed sanitize temperature falling inside
the kitchen's registered food-safety band (sanitizing water temperature is a
food-safety spec, not a feel test), and the proposed restock quantity not
exceeding the kitchen's registered ceiling (over-ordering beyond registered
capacity is a storage risk, not thrift). Always-escalate ops (human sign-off
regardless of confidence, mapping this repo's Trust Controls in
[`docs/business-model.md`](docs/business-model.md)): `:approve-hot-surface-proximity`
(no robot operation near hot surfaces/open flames without the governor gate)
and `:approve-sharp-tool-zone-entry` (sharp-tool zones require human sign-off).

## Robotics premise

All cloud-itonami verticals are designed on the premise that a **robot performs
the physical domain work**. Here a kitchen-support robot performs dishwashing, prep-surface cleaning and supply restocking under an actor that proposes
actions and an independent **Kitchen Support Governor** that gates them. The governor never
dispatches hardware itself; `:high`/`:safety-critical` actions (such as
operating near hot surfaces, sharp tools or open flames) require human sign-off.

A live sample of the operator console (robotics safety console, shared template) is rendered in [docs/samples/operator-console.html](docs/samples/operator-console.html) — pure-data HTML output of `kotoba.robotics.ui`.

## Core Contract

```text
kitchen schedule + food-safety checklist + supply order
        |
        v
Kitchen Advisor -> Kitchen Support Governor -> prep/clean, or human sign-off
        |
        v
robot actions (gated) + operating records + audit ledger
```

No automated advice can dispatch a robot action the governor refuses, suppress
an operating record, or disclose sensitive data without governor approval and
audit evidence.

## Capability layer

Resolves via [`kotoba-lang/occupation`](https://github.com/kotoba-lang/occupation)
(ISCO-08 `9412`). Required capabilities:

- :robotics
- :forms
- :audit-ledger

See [`docs/business-model.md`](docs/business-model.md) and
[`docs/operator-guide.md`](docs/operator-guide.md).

## License

AGPL-3.0-or-later.
