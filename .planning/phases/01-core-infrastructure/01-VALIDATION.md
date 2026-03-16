---
phase: 01
slug: core-infrastructure
status: draft
nyquist_compliant: false
wave_0_complete: false
created: 2026-03-16
---

# Phase 01 — Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | Forge 1.7.10 built-in + JUnit 4 (standard GTNH) |
| **Config file** | None — Wave 0 creates |
| **Quick run command** | `./gradlew build` |
| **Full suite command** | `./gradlew build runClient` |
| **Estimated runtime** | ~120 seconds |

---

## Sampling Rate

- **After every task commit:** Run `./gradlew build`
- **After every plan wave:** Run `./gradlew build` + manual client test
- **Before `/gsd:verify-work:** Full suite must be green
- **Max feedback latency:** 120 seconds

---

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|-----------|-------------------|-------------|--------|
| 01-01-01 | 01 | 1 | CORE-01 | build | `./gradlew build` | ❌ W0 | ⬜ pending |
| 01-01-02 | 01 | 1 | CORE-02 | integration | `./gradlew runClient` (manual) | ❌ W0 | ⬜ pending |
| 01-01-03 | 01 | 1 | CORE-03 | unit | N/A - compile check | ❌ W0 | ⬜ pending |
| 01-01-04 | 01 | 1 | CORE-04 | compile | `./gradlew compileJava` | ❌ W0 | ⬜ pending |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

---

## Wave 0 Requirements

- [ ] `build.gradle` — GTNH convention setup
- [ ] `gradle.properties` — mod metadata, versions
- [ ] `settings.gradle` — plugin version, composite build for AE2
- [ ] `dependencies.gradle` — AE2 local dependency
- [ ] `src/main/java/com/ae2channelrouter/AE2ChannelRouter.java` — main mod class
- [ ] `src/main/java/com/ae2channelrouter/tile/AEBaseRouterTile.java` — base tile
- [ ] `src/main/java/com/ae2channelrouter/network/NetworkProxy.java` — wrapper utility
- [ ] `src/main/resources/mcmod.info` — mod metadata
- [ ] `.gitignore` — standard Forge mod ignores

*If none: "Existing infrastructure covers all phase requirements."*

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| Mod loads in Minecraft without crash | CORE-02 | Requires running Minecraft client | Launch `./gradlew runClient` and verify no crash logs |
| AENetworkProxy connects to AE2 grid | CORE-03 | Requires AE2 present in world | Place AE2 cable and check FML logs for "AENetwork initialized" |

*If none: "All phase behaviors have automated verification."*

---

## Validation Sign-Off

- [ ] All tasks have `<automated>` verify or Wave 0 dependencies
- [ ] Sampling continuity: no 3 consecutive tasks without automated verify
- [ ] Wave 0 covers all MISSING references
- [ ] No watch-mode flags
- [ ] Feedback latency < 120s
- [ ] `nyquist_compliant: true` set in frontmatter

**Approval:** pending