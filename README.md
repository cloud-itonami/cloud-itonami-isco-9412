# cloud-itonami-isco-9412

Open Occupation Blueprint for **ISCO-08 9412**: Kitchen Helpers.

This repository designs a forkable OSS business for an independent kitchen support worker: a kitchen-support robot performs dishwashing and cleaning tasks under a governor-gated actor, so the practice keeps its own food-safety and cleaning records instead of renting a closed kitchen-management SaaS.

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
