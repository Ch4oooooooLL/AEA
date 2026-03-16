# Domain Pitfalls: Minecraft 1.7.10 AE2 Optimization Mods

**Project:** AE2 Optimization Mod for GTNH 2.8.4
**Researched:** 2026-03-16
**Confidence:** MEDIUM (based on code analysis and domain expertise; web searches returned no results)

---

## Critical Pitfalls

### 1. Tick Budget Exhaustion

**What goes wrong:** Server tick time exceeds 50ms budget, causing noticeable server lag and TPS drops.

**Why it happens:** The TickHandler processes queues with a hard 50ms limit (TickHandler.java:236). Each Grid updates ALL IGridCache instances every tick (Grid.java:264-281). Large networks with many machines compound this.

**Consequences:**
- Server TPS drops from 20 to <5
- Players experience extreme input lag
- Crafting calculations get skipped (line 172-173 has per-job time budget)
- World may become unplayable

**Prevention:**
- Profile every optimization with the built-in Grid profiling (Grid.java:246-262)
- Batch operations across ticks instead of doing everything in one tick
- Use tick hooks sparingly - only for truly tick-dependent logic
- Implement work queues with per-tick budgets like AE2 does

**Warning signs:**
- Grid.update() taking >5ms per tick
- Crafting jobs not progressing
- Server console shows "Can't keep up" messages
- TPS < 18 consistently

**Phase:** Performance Optimization Phase - requires profiling infrastructure first

---

### 2. Incorrect GridCache Lifecycle Management

**What goes wrong:** IGridCache implementations leak memory, have stale data, or cause NPEs.

**Why it happens:** GridCache lifecycle is tied to Grid creation/destruction. The cache receives addNode/removeNode callbacks (Grid.java:100-103, 178-181) but modders sometimes skip proper cleanup or initialization.

**Consequences:**
- Memory leaks (cached data never released when nodes leave)
- NPE when accessing removed nodes
- Ghost items in network (items that exist in cache but not in world)
- World save corruption (stale references in GridStorage)

**Prevention:**
- ALWAYS call super in addNode/removeNode overrides
- Implement onJoin/onSplit handlers for storage-based state (Grid.java:145-170)
- Clear all collections in removeNode
- Validate node.getMachine() != null before use
- Test by adding and removing networks repeatedly

**Warning signs:**
- Memory usage grows over time
- getMachines() returns disconnected tiles
- World reload loses data that should persist

**Phase:** Grid Cache Implementation Phase

---

### 3. Event System Deadlocks

**What goes wrong:** Custom MENetworkEvents cause infinite loops or deadlock the server.

**Why it happens:** Events can trigger other events. The Grid.postEvent() pauses crafting rebuilds (Grid.java:195) but custom event handlers may not. Recursive event posting without guards causes stack overflow or infinite loops.

**Consequences:**
- Server freeze/crash
- StackOverflowError
- Crafting grid perpetually paused

**Prevention:**
- Use event flags or ThreadLocal to detect re-entrancy
- Always call super in event handler overrides
- Queue async operations instead of posting events from within events
- Use postEventTo for targeted events instead of broadcast when possible

**Warning signs:**
- Deep call stacks in debugging
- "Paused" crafting jobs never resume
- Single tick taking >1000ms

**Phase:** Event System Design Phase

---

### 4. Network Storage Split/Join Corruption

**What goes wrong:** GridStorage split operations lose data or create duplicate entries.

**Why it happens:** When a network splits (e.g., cable removed between two subnets), onSplit is called (Grid.java:158-159). If both sides reference the same storage object without proper division, you get duplicate items or lost items.

**Consequences:**
- Items duplicated across networks
- Items lost permanently
- Crafting jobs lose ingredients
- Impossible to craft certain items (ingredients exist but can't be found)

**Prevention:**
- Test network topology changes extensively
- Ensure onSplit creates a new GridStorage and properly divides item/energy
- Verify onJoin merges correctly (Grid.java:162-164)
- Never modify myStorage without going through the split/join flow

**Warning signs:**
- Duplicate items when cables are removed/replaced
- Crafting says "missing ingredients" that clearly exist
- Item counts don't add up across networks

**Phase:** Network Topology Phase (post-GridCache)

---

### 5. Java Bytecode Incompatibility

**What goes wrong:** Mod crashes immediately on load with VerifyError or UnsupportedClassVersionError.

**Why it happens:** 1.7.10 runs on Java 8 bytecode. Using Java 11+ syntax (switch expressions, records, var) produces incompatible bytecode unless Jabel is configured.

**Consequences:**
- "NoClassDefFoundError" at startup
- "UnsupportedClassVersionError"
- Mod fails to load, crashes game

**Prevention:**
- Use Jabel plugin to transpile modern Java to Java 8 bytecode
- Set sourceCompatibility and targetCompatibility to Java 8
- Test with actual Java 8 JRE (not just JDK)
- Check compiled class files with javap -version

**Warning signs:**
- Build succeeds but game crashes
- Error mentions "major version 61" (Java 17) on Java 8 runtime

**Phase:** Build Configuration Phase (addressed in STACK.md)

---

## Moderate Pitfalls

### 6. Thread Safety Violations

**What goes wrong:** ConcurrentModificationException or stale data when accessed from multiple threads.

**Why it happens:** Minecraft's tick runs on server thread. However, AE2 has async queue processing (TickHandler.java:232-242). Collections like HashMap in Grid.java (line 45-46) are not thread-safe.

**Consequences:**
- Random crashes during chunk loading/unloading
- ConcurrentModificationException
- Race conditions causing item loss

**Prevention:**
- Use ConcurrentHashMap for shared state
- Synchronize access to non-thread-safe collections
- Only access world/grid from main thread (use addCallable for async)
- Check Platform.isServer() before world access

**Warning signs:**
- Crashes during world save/load
- Errors in tick handler context
- Intermittent item duplication or loss

**Phase:** Async Operations Phase

---

### 7. World/Chunk Loading Order Bugs

**What goes wrong:** Tile entities accessed before chunk is fully loaded, causing NPE or ghost tiles.

**Why it happens:** TickHandler.onChunkLoad (lines 145-151) iterates tiles, but Grid.add() may be called before onReady(). The pivot node gets grid reference set (Grid.java:73) but machine may not be fully initialized.

**Consequences:**
- NPE when accessing tile methods
- Machines not appearing in network
- Desync between world state and grid state

**Prevention:**
- Don't assume tile is fully loaded in addNode
- Defer complex initialization to onReady() (TickHandler.java:186)
- Check !tile.isInvalid() before operations
- Use onChunkLoad only for basic state restoration

**Warning signs:**
- NPE in first tick after chunk load
- Machines not responding until chunk reload
- Null machine in getMachines()

**Phase:** Tile Integration Phase

---

### 8. Recursive Grid Traversal Stack Overflow

**What goes wrong:** Infinite recursion when traversing grid connections, causing StackOverflowError.

**Why it happens:** Grid.getAllRecursiveGridConnections (Grid.java:321-361) uses recursion with depth limiting. If depth limit is too high or visited set fails, infinite loop occurs.

**Consequences:**
- Server crash with StackOverflowError
- Infinite loop consuming all available memory

**Prevention:**
- Always track visited nodes (line 338-352 uses visited Set)
- Check depth against AEConfig.maxRecursiveDepth (line 341)
- Add null checks for grid connections (line 348)
- Prefer iterative over recursive where possible

**Warning signs:**
- Deep call stacks in profiler
- "maxRecursiveDepth exceeded" in logs

**Phase:** Grid Topology Phase

---

### 9. Missing API Version Compatibility

**What goes wrong:** Using AE2 API methods that don't exist in the target version, causing NoSuchMethodError.

**Why it happens:** GTNH uses a forked/rv3-beta version of AE2. Public API methods may differ from standard AE2. GTNH 2.8.4 has specific API version requirements.

**Consequences:**
- NoSuchMethodError at runtime
- Mod loads but crashes when specific features used

**Prevention:**
- Check AEApi.instance() methods against source
- Verify IGridCache interface matches exactly
- Test against actual GTNH 2.8.4 environment
- Use reflection as last resort for version-specific code

**Warning signs:**
- Works in dev but crashes in packaged mod
- Different AE2 versions have different APIs

**Phase:** API Integration Phase

---

### 10. Crafting Rebuild Starvation

**What goes wrong:** Crafting grid never rebuilds, causing stale craftable items list.

**Why it happens:** CraftingGridCache.pauseRebuilds() is called during event posting (Grid.java:195). If unpause is not called (line 197), or rebuild is never triggered, craftable items become stale.

**Consequences:**
- Crafting card shows items as craftable that aren't
- Autocrafting fails unexpectedly
- Pattern logic uses wrong data

**Prevention:**
- Always balance pause/unpause pairs
- Trigger rebuild after network changes
- Use pauseRebuilds only for atomic operations
- Test by making items and verifying craftable state

**Warning signs:**
- Crafting pattern says "can craft" but fails
- Items disappear from craftable but still in storage

**Phase:** Crafting System Phase

---

## Minor Pitfalls

### 11. WeakHashMap Memory Retention

**What goes wrong:** Objects retained longer than expected because WeakHashMap doesn't behave like regular Map.

**Why it happens:** TickHandler uses WeakHashMap for callQueue (TickHandler.java:58). Keys are weakly referenced - when no strong references exist, entries are removed. This is often misunderstood.

**Prevention:**
- Don't rely on WeakHashMap for explicit cleanup
- Keep strong references where you need guaranteed cleanup
- Understand GC timing is non-deterministic

---

### 12. Display List Memory Leaks

**What goes wrong:** OpenGL display lists not freed, causing memory growth.

**Why it happens:** TickHandler schedules GLAllocation.deleteDisplayLists (line 210) but only runs on client. Server-side mods don't need this, but render-focused mods do.

**Prevention:**
- Use GLAllocation properly
- Don't create display lists in common code paths
- Delete in onUnload or similar

---

### 13. Config Synchronization Issues

**What goes wrong:** Server and client config drift causes desync or crashes.

**Why it happens:** AEConfig settings affect network behavior. If server has different config than client, they may disagree on tick times, limits, or storage formats.

**Prevention:**
- Use Forge's config synchronization for critical values
- Log config differences at startup
- Validate config in init, not just constructor

---

## Phase-Specific Warnings

| Phase Topic | Likely Pitfall | Mitigation |
|-------------|----------------|------------|
| Build Setup | Bytecode incompatibility | Use Jabel, test with Java 8 |
| GridCore | Tick budget issues | Profile early, set budgets |
| GridCache | Lifecycle bugs | Test add/remove cycles |
| Events | Deadlocks | Use re-entrancy guards |
| Network Storage | Split/join corruption | Test topology changes |
| Crafting | Rebuild starvation | Verify craftable state |
| Async | Thread safety | Use ConcurrentHashMap |

---

## Sources

- **Grid.java** - Core ME network management, tick updates, storage handling
- **TickHandler.java** - Tick scheduling, queue processing, event hooks
- **GTNH Community Knowledge** - Known 1.7.10 modding patterns (web search limited by version age)
- **Minecraft Forge Documentation** - Event handling, tick system
- **Code Analysis** - Internal patterns from AE2 source

---

## Confidence Assessment

| Area | Confidence | Reason |
|------|------------|--------|
| Tick Budget Issues | HIGH | Directly observable in code (50ms limit) |
| GridCache Lifecycle | HIGH | Clear from source analysis |
| Event Deadlocks | HIGH | Code pattern clearly shows risk |
| Storage Split/Join | MEDIUM | Logic present but complex |
| Bytecode Compatibility | HIGH | Well-known 1.7.10 issue |
| Thread Safety | MEDIUM | Collections identified, risk clear |
| Version Compatibility | MEDIUM | GTNH-specific, requires testing |

---

## Recommendations

1. **Start with profiling infrastructure** - Grid.java already has profiling; enable it early
2. **Test topology changes thoroughly** - Network split/join is most dangerous for item loss
3. **Follow AE2's budget patterns** - 50ms queue, per-tick crafting limits should be your model
4. **Never skip lifecycle methods** - addNode/removeNode/onJoin/onSplit must all be handled
5. **Validate with actual GTNH** - Dev environment may differ from packaged game