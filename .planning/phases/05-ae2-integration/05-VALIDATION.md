---
phase: 05
slug: ae2-integration
status: planned
nyquist_compliant: false
wave_0_complete: false
created: 2026-03-17
---

# Phase 05 — Validation Strategy

> Per-phase validation contract for AE2 Integration phase.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | Forge 1.7.10 built-in + Manual AE2 integration testing |
| **Test Environment** | Minecraft Client with AE2 rv3-beta-695-GTNH |
| **Quick run command** | `./gradlew build` |
| **Full suite command** | `./gradlew build` + manual in-game testing |
| **Estimated runtime** | ~60 seconds build + 30 minutes manual testing |

---

## Sampling Rate

- **After every task commit:** Run `./gradlew build`
- **After every plan wave:** Build + manual AE2 integration test
- **Before `/gsd:verify-work:** Full build + comprehensive AE2 testing
- **Max feedback latency:** 5 minutes (includes manual testing)

---

## Per-Plan Verification Map

### Plan 05-01: GridFlags Configuration

| Test Case | Requirement | Test Type | Automated | Status |
|-----------|-------------|-----------|-----------|--------|
| Controller channel usage | AEIN-04 | unit | ❌ Manual | ⬜ pending |
| Terminal channel neutrality | AEIN-04 | unit | ❌ Manual | ⬜ pending |
| Cable channel neutrality | AEIN-04 | unit | ❌ Manual | ⬜ pending |
| No connection errors | AEIN-04 | integration | ❌ Manual | ⬜ pending |

**Manual Test Steps:**
1. Place Routing Controller next to AE2 cable → Verify 1 channel consumed
2. Place Routing Terminal next to AE2 cable → Verify 0 channels consumed
3. Place Routing Cable next to AE2 cable → Verify 0 channels consumed
4. Check AE2 network tool for channel count accuracy

### Plan 05-02: Device Detection Improvement

| Test Case | Requirement | Test Type | Automated | Status |
|-----------|-------------|-----------|-----------|--------|
| ME Drive detection | AEIN-01 | unit | ❌ Manual | ⬜ pending |
| ME Interface detection | AEIN-01 | unit | ❌ Manual | ⬜ pending |
| Cable exclusion | AEIN-01 | unit | ❌ Manual | ⬜ pending |
| Non-AE block exclusion | AEIN-01 | unit | ❌ Manual | ⬜ pending |
| Device count accuracy | AEIN-01 | integration | ❌ Manual | ⬜ pending |

**Manual Test Steps:**
1. Place terminal next to ME Drive → Verify detected in logs/GUI
2. Place terminal next to ME Cable → Verify NOT counted
3. Place terminal next to vanilla chest → Verify NOT counted
4. Place terminal with 3 devices → Verify count = 3
5. Remove one device → Verify count updates

### Plan 05-03: Controller Event Handling

| Test Case | Requirement | Test Type | Automated | Status |
|-----------|-------------|-----------|-----------|--------|
| Controller addition | AEIN-03 | integration | ❌ Manual | ⬜ pending |
| Controller removal (under capacity) | AEIN-03 | integration | ❌ Manual | ⬜ pending |
| Controller removal (over capacity) | AEIN-03 | integration | ❌ Manual | ⬜ pending |
| Proportional reclamation | AEIN-03 | integration | ❌ Manual | ⬜ pending |
| Terminal notification | AEIN-03 | integration | ❌ Manual | ⬜ pending |
| Grid loss handling | AEIN-03 | integration | ❌ Manual | ⬜ pending |

**Manual Test Steps:**
1. Start with 1 controller (192 channels)
2. Add 2nd controller → Verify capacity becomes 384
3. Allocate 300 channels to terminals
4. Remove 1st controller → Verify reclamation to 192
5. Check terminal notifications
6. Disconnect controller from network → Verify all channels reclaimed

### Plan 05-04: Virtual Channel Injection

| Test Case | Requirement | Test Type | Automated | Status |
|-----------|-------------|-----------|-----------|--------|
| GridCache registration | AEIN-02 | unit | ❌ Build check | ⬜ pending |
| Device registration | AEIN-02 | integration | ❌ Manual | ⬜ pending |
| Channel query interception | AEIN-02 | integration | ❌ Manual | ⬜ pending |
| Allocation changes | AEIN-02 | integration | ❌ Manual | ⬜ pending |
| Fallback mode (if API unavailable) | AEIN-02 | integration | ❌ Manual | ⬜ pending |

**Manual Test Steps:**
1. Check logs for GridCache registration status
2. Connect device to terminal → Verify registration
3. Query device channel status → Verify available
4. Reduce allocation → Verify device sees change
5. If GridCache unavailable → Verify fallback mode works

---

## Integration Test Scenarios

### Scenario 1: Full Routing System
**Setup:**
- 2 AE2 controllers → 384 channels
- 1 Routing Controller
- 2 Routing Terminals
- 4 ME Drives (2 per terminal)

**Verification:**
- [ ] Controller consumes 1 channel
- [ ] Terminals consume 0 channels
- [ ] All 4 drives detected
- [ ] Channels allocated to terminals
- [ ] Drives show channel availability

### Scenario 2: Controller Failure Recovery
**Setup:**
- 2 controllers, 2 terminals, allocated channels
- Remove 1 controller

**Verification:**
- [ ] Capacity drops to 192
- [ ] Reclamation occurs if over capacity
- [ ] Terminals notified of changes
- [ ] System remains stable

### Scenario 3: Device Hot-Swapping
**Setup:**
- Terminal with connected devices
- Remove and add devices while running

**Verification:**
- [ ] Device count updates correctly
- [ ] New devices detected
- [ ] Removed devices cleaned up
- [ ] No memory leaks

---

## Success Criteria

### Phase 05 Success

All requirements must be verified:

| Requirement | Verification Method | Success Criteria |
|-------------|---------------------|------------------|
| AEIN-01 | Manual in-game test | Controllers detected, channels counted |
| AEIN-02 | Manual in-game test | Devices receive virtual channels (or fallback works) |
| AEIN-03 | Manual in-game test | Events handled, reclamation works |
| AEIN-04 | Manual in-game test | GridFlags correct per device type |

### Build Success

- [ ] `./gradlew build` completes without errors
- [ ] No compilation errors
- [ ] No runtime exceptions in logs
- [ ] Mod loads successfully in Forge

### Integration Success

- [ ] Routing system functions with AE2 network
- [ ] Channel accounting is accurate
- [ ] No crashes during normal operation
- [ ] Graceful handling of edge cases

---

## Wave 0 Requirements

### Prerequisites Checklist

- [ ] Phase 1, 2, 3, 4 complete and verified
- [ ] AE2 rv3-beta-695-GTNH available in dev environment
- [ ] Minecraft test environment ready
- [ ] All planning documents reviewed

### Plan Documents

- [ ] 05-CONTEXT.md - Context gathered
- [ ] 05-RESEARCH.md - Research complete
- [ ] 05-01-PLAN.md - GridFlags Configuration
- [ ] 05-02-PLAN.md - Device Detection
- [ ] 05-03-PLAN.md - Controller Event Handling
- [ ] 05-04-PLAN.md - Virtual Channel Injection
- [ ] PLANNING-SUMMARY.md - Overview complete

---

## Validation Commands

### Quick Validation
```bash
./gradlew build
```

### Full Validation
```bash
./gradlew clean build
# Then manual in-game testing
```

### Specific Plan Validation

**05-01:** Build + check channel counts in-game
**05-02:** Build + verify device detection logs
**05-03:** Build + test controller add/remove scenarios
**05-04:** Build + check GridCache registration logs + test device integration

---

## Known Limitations

1. **Manual Testing Required:** AE2 integration requires manual in-game verification
2. **AE2 Version Dependency:** 05-04 may not work with all AE2 versions
3. **Device Coverage:** Only IGridHost devices supported in v1
4. **Performance:** Large device counts (>100) need testing

---

## Rollback Criteria

Stop implementation if:
- Build fails and cannot be resolved within 30 minutes
- AE2 API causes crashes that cannot be caught
- Channel accounting errors found in testing
- Performance is unacceptable (>100ms per operation)

---

## Post-Validation

After all plans complete:

1. Update REQUIREMENTS.md with completion status
2. Update ROADMAP.md Phase 5 status to Complete
3. Create VERIFICATION.md with test results
4. Tag release as v1.0

---

*Validation strategy created: 2026-03-17*  
*Phase: 05-ae2-integration*
