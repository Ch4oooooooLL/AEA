# Phase 1: Core Infrastructure - Research

**Researched:** 2026-03-16
**Domain:** Minecraft Forge 1.7.10 / GTNH AE2 Addon Development
**Confidence:** HIGH

## Summary

Phase 1 establishes the foundational infrastructure for the AE2 Channel Router mod. The project targets Minecraft 1.7.10 with Forge 10.13.4.1614, using the GTNH Convention Plugin for modern development workflow. The key integration point with AE2 is through `AENetworkProxy` and `AENetworkInvTile` - these classes provide the abstraction layer for connecting to AE2 grids.

The GTNH ecosystem provides significant advantages: Java 21 source compatibility (compiled to Java 8 bytecode via Jabel), automatic RetroFuturaGradle integration, and standardized build conventions. AE2 integration requires implementing `IGridProxyable` interface and handling `GridAccessException` when the grid is unavailable.

**Primary recommendation:** Use GTNH Convention Plugin with local AE2 source dependency for maximum compatibility and debugging capability.

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions
- Gradle 版本：8.x
- Java 版本：Java 21（GTNH 支持直接使用 Java 21 编译）
- AE2 依赖：本地源码依赖，指向 `D:/CODE/Sources/Applied-Energistics-2-Unofficial-rv3-beta-695-GTNH`
- 使用 GTNH Convention Plugin (`com.gtnewhorizons.gtnhconvention`)
- 主类结构：单类入口模式
- FML 初始化：全部在 preInit 中完成（遵循 GTNH 惯例）
- 包结构：按类型分组（block, tile, item, network, gui）
- AENetworkProxy 封装：单例模式，便于全局访问
- GridAccessException 处理：返回 null 或默认值，简化调用
- 基础 Tile Entity：继承 AENetworkInvTile，实现网格回调

### Claude's Discretion
- 具体 Forge 版本号选择
- 依赖配置细节（implementation vs compileOnly）
- 混淆配置

### Deferred Ideas (OUT OF SCOPE)
无 — 讨论保持在阶段范围内
</user_constraints>

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|-----------------|
| CORE-01 | Project setup with Gradle, Forge 10.13.4.1614, Jabel for Java 8 bytecode | GTNH Convention Plugin handles this automatically; gradle.properties sets `enableModernJavaSyntax = true` |
| CORE-02 | Main mod class with FML initialization and AE2 API integration | @Mod annotation with FMLPreInitializationEvent; AEApi.instance() for API access |
| CORE-03 | AENetworkProxy wrapper utility with GridAccessException handling | AENetworkProxy is AE2's standard wrapper; GridAccessException is checked exception requiring try-catch |
| CORE-04 | Base tile entity extending AENetworkInvTile for AE2 integration | AENetworkInvTile extends AEBaseInvTile and implements IGridProxyable, IActionHost |
</phase_requirements>

## Standard Stack

### Core
| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| Minecraft Forge | 10.13.4.1614 | Mod loader for 1.7.10 | GTNH standard, AE2 requires this version |
| GTNH Convention Plugin | latest (via Nexus) | Build configuration | Handles RFG, Jabel, deobfuscation automatically |
| AE2 Unofficial | rv3-beta-695-GTNH | AE2 API and integration | Local source dependency for debugging |
| Jabel | (via plugin) | Java 21 syntax → Java 8 bytecode | GTNH convention, `enableModernJavaSyntax = true` |

### Build Configuration
```properties
# gradle.properties
gtnh.settings.blowdryerTag = 0.2.0
modName = AE2 Channel Router
modId = ae2channelrouter
modGroup = com.ae2channelrouter

minecraftVersion = 1.7.10
forgeVersion = 10.13.4.1614
channel = stable
mappingsVersion = 12

enableModernJavaSyntax = true
enableGenericInjection = true
generateGradleTokenClass = com.ae2channelrouter.BuildTags
gradleTokenVersion = VERSION
apiPackage = api
usesMixins = false
forceEnableMixins = true
```

```gradle
// settings.gradle
plugins {
    id 'com.gtnewhorizons.gtnhsettingsconvention' version '1.0.44'
}
rootProject.name = "AE2-Channel-Router"
```

```gradle
// build.gradle
plugins {
    id 'com.gtnewhorizons.gtnhconvention'
}
```

```gradle
// dependencies.gradle
dependencies {
    // AE2 local source dependency
    implementation project(':Applied-Energistics-2-Unofficial')

    // Required GTNH deps
    implementation('com.github.GTNewHorizons:NotEnoughItems:2.7.91-GTNH:dev')
    implementation("com.github.GTNewHorizons:GTNHLib:0.6.40:dev")
}
```

## Architecture Patterns

### Recommended Project Structure
```
src/main/java/com/ae2channelrouter/
├── AE2ChannelRouter.java      # Main mod class (@Mod)
├── BuildTags.java             # Generated version class
├── block/                     # Block definitions
│   └── ModBlocks.java         # Block registry
├── tile/                      # Tile entities
│   └── AEBaseRouterTile.java  # Extends AENetworkInvTile
├── item/                      # Items
├── network/                   # Network helpers
│   └── NetworkProxy.java      # Wrapper around AENetworkProxy
├── client/                    # Client-only code
└── util/                      # Utilities

src/main/resources/
├── mcmod.info                 # Mod metadata
├── assets/ae2channelrouter/   # Textures, models
└── META-INF/                  # Access transformers
```

### Pattern 1: Main Mod Class
**What:** Single entry point with @Mod annotation, following GTNH conventions
**When to use:** All GTNH 1.7.10 mods
**Example:**
```java
// Source: AE2 AppEng.java (lines 54-61)
@Mod(
    modid = AE2ChannelRouter.MOD_ID,
    acceptedMinecraftVersions = "[1.7.10]",
    name = AE2ChannelRouter.MOD_NAME,
    version = BuildTags.VERSION,
    dependencies = "required-after:Forge@[10.13.4.1614,);required-after:appliedenergistics2;required-after:gtnhlib@[0.6.11,)"
)
public class AE2ChannelRouter {
    public static final String MOD_ID = "ae2channelrouter";
    public static final String MOD_NAME = "AE2 Channel Router";

    @Mod.Instance(MOD_ID)
    public static AE2ChannelRouter INSTANCE;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        // GTNH convention: do ALL initialization in preInit
        // Blocks, items, tile entities registered here
    }
}
```

### Pattern 2: Base Tile Entity with AE2 Integration
**What:** Extend AENetworkInvTile for automatic grid connectivity
**When to use:** Any block that needs AE2 network access
**Example:**
```java
// Source: AE2 AENetworkInvTile.java (lines 24-79)
public abstract class AENetworkInvTile extends AEBaseInvTile
        implements IActionHost, IGridProxyable {

    private final AENetworkProxy gridProxy = new AENetworkProxy(
        this, "proxy", this.getItemFromTile(this), true);

    @Override
    public AENetworkProxy getProxy() {
        return this.gridProxy;
    }

    @Override
    public void gridChanged() {
        // Called when grid state changes
        // Override in subclass to react to network changes
    }

    @Override
    public void onReady() {
        super.onReady();
        this.getProxy().onReady();  // Initializes grid node
    }

    @Override
    public void invalidate() {
        super.invalidate();
        this.getProxy().invalidate();  // Cleans up grid node
    }
}
```

### Pattern 3: AENetworkProxy Wrapper
**What:** Wrapper utility for safe grid access with exception handling
**When to use:** Simplifying GridAccessException handling throughout mod
**Example:**
```java
// Based on AE2 AENetworkProxy.java patterns (lines 145-172)
public class NetworkProxy {
    private final AENetworkProxy proxy;

    public NetworkProxy(AENetworkProxy proxy) {
        this.proxy = proxy;
    }

    public boolean isConnected() {
        return proxy.isActive() && proxy.isReady();
    }

    public IGrid getGridSafe() {
        try {
            return proxy.getGrid();
        } catch (GridAccessException e) {
            return null;  // As per user decision: return null/defaults
        }
    }

    public IPathingGrid getPathSafe() {
        try {
            return proxy.getPath();
        } catch (GridAccessException e) {
            return null;
        }
    }

    public IEnergyGrid getEnergySafe() {
        try {
            return proxy.getEnergy();
        } catch (GridAccessException e) {
            return null;
        }
    }

    public boolean isPowered() {
        return proxy.isPowered();  // Already handles exception internally
    }
}
```

### Pattern 4: AE2 API Access
**What:** Entry point to AE2 functionality
**When to use:** Creating grid nodes, accessing registries
**Example:**
```java
// Source: AE2 AEApi.java (lines 25-64)
// API Entry Point
IAppEngApi api = AEApi.instance();

// Creating grid nodes (for IGridBlock implementations)
IGridNode node = api.createGridNode(myGridBlock);

// Accessing registries
IRegistryContainer registries = api.registries();
IDefinitions definitions = api.definitions();
```

### Anti-Patterns to Avoid
- **Initializing in init/postInit:** GTNH convention puts ALL initialization in preInit
- **Catching GridAccessException and logging:** Per user decision, return null/defaults silently
- **Creating AENetworkProxy manually in tiles:** Use AENetworkInvTile which handles lifecycle
- **Manual grid node management:** Let AENetworkProxy handle node creation/destruction

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Grid node lifecycle | Manual node creation/destruction | AENetworkProxy/AENetworkInvTile | Handles NBT, chunk unload, validation automatically |
| Grid exception handling | Try-catch at every call site | NetworkProxy wrapper utility | User decision: return null/defaults |
| Forge mod setup | Manual build.gradle configuration | GTNH Convention Plugin | Handles RFG, Jabel, deobf, dependencies |
| Tile entity network callbacks | Manual FML event handling | AENetworkInvTile methods | onReady(), invalidate(), onChunkUnload() |
| Grid access | Direct IGridNode manipulation | AENetworkProxy.getGrid() | Validates ready state, handles exceptions |

**Key insight:** AENetworkProxy is the battle-tested abstraction from AE2 itself. It handles all edge cases including world loading, chunk unloading, network rebuilds, and NBT serialization. Building a custom grid connection mechanism would require reimplementing all this logic.

## Common Pitfalls

### Pitfall 1: Grid Access Before Ready
**What goes wrong:** Calling getGrid() before proxy is ready throws GridAccessException
**Why it happens:** Grid node not yet created or tile not validated
**How to avoid:** Always check `proxy.isReady()` before accessing grid, or use wrapper that returns null
**Warning signs:** NullPointerException or GridAccessException in logs during world load

### Pitfall 2: Missing Lifecycle Methods
**What goes wrong:** Grid nodes leak or don't reconnect after chunk unload
**Why it happens:** Forgot to call proxy.onReady(), proxy.invalidate(), proxy.onChunkUnload()
**How to avoid:** Extend AENetworkInvTile which handles this automatically, or manually call in tile methods
**Warning signs:** Devices stop working after leaving area, duplicate nodes in network

### Pitfall 3: GTNH Plugin Version Mismatch
**What goes wrong:** Build failures, missing RFG tasks
**Why it happens:** Using outdated plugin version or incompatible settings
**How to avoid:** Use exact version from AE2 source: `com.gtnewhorizons.gtnhsettingsconvention:1.0.44`
**Warning signs:** "Plugin not found" or "Task not found" errors

### Pitfall 4: AE2 Dependency Scope
**What goes wrong:** ClassNotFoundException at runtime
**Why it happens:** AE2 marked as compileOnly instead of implementation
**How to avoid:** Use `implementation` for local AE2 source dependency
**Warning signs:** Mod loads but crashes when accessing AE2 classes

### Pitfall 5: Modern Java Syntax Without Jabel
**What goes wrong:** Build succeeds but JVM 8 crashes
**Why it happens:** Using Java 9+ features without Jabel bytecode translation
**How to avoid:** Set `enableModernJavaSyntax = true` in gradle.properties
**Warning signs:** "Unsupported class file version" or "invokedynamic" errors

## Code Examples

### Complete Minimal Mod Setup
```java
// AE2ChannelRouter.java
package com.ae2channelrouter;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

@Mod(
    modid = AE2ChannelRouter.MOD_ID,
    name = AE2ChannelRouter.MOD_NAME,
    version = BuildTags.VERSION,
    acceptedMinecraftVersions = "[1.7.10]",
    dependencies = "required-after:Forge@[10.13.4.1614,);" +
                   "required-after:appliedenergistics2;" +
                   "required-after:gtnhlib@[0.6.11,)"
)
public class AE2ChannelRouter {
    public static final String MOD_ID = "ae2channelrouter";
    public static final String MOD_NAME = "AE2 Channel Router";

    @Mod.Instance(MOD_ID)
    public static AE2ChannelRouter INSTANCE;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        // All initialization here per GTNH convention
        // Register blocks, items, tile entities
    }
}
```

### Base Router Tile
```java
// tile/AEBaseRouterTile.java
package com.ae2channelrouter.tile;

import appeng.tile.grid.AENetworkInvTile;
import appeng.api.networking.GridFlags;

public abstract class AEBaseRouterTile extends AENetworkInvTile {

    public AEBaseRouterTile() {
        // Configure proxy flags
        this.getProxy().setFlags(GridFlags.REQUIRE_CHANNEL);
        this.getProxy().setIdlePowerUsage(1.0);  // AE/t
    }

    @Override
    public void gridChanged() {
        super.gridChanged();
        // React to network changes (controller added/removed)
        if (this.getProxy().isActive()) {
            // Network is valid
        }
    }

    protected boolean hasGridAccess() {
        return this.getProxy().isReady() && this.getProxy().isActive();
    }
}
```

### mcmod.info
```json
[
    {
        "modid": "ae2channelrouter",
        "name": "AE2 Channel Router",
        "description": "Channel routing system for Applied Energistics 2",
        "version": "${version}",
        "mcversion": "1.7.10",
        "url": "",
        "updateUrl": "",
        "authorList": ["Author"],
        "credits": "GTNH Team, AE2 Team",
        "logoFile": "",
        "screenshots": [],
        "dependencies": ["appliedenergistics2", "gtnhlib"]
    }
]
```

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| Manual Gradle config | GTNH Convention Plugin | 2023+ | Automatic RFG, Jabel, deobfuscation |
| Java 8 only | Java 21 + Jabel | GTNH adoption | Modern syntax, still targets JVM 8 |
| External AE2 dep | Local source dep | User decision | Debug AE2, direct API access |
| init/postInit init | All in preInit | GTNH convention | Simpler lifecycle, earlier setup |

**Deprecated/outdated:**
- `gradle startscript` approach: Use GTNH Convention Plugin
- Multiple initialization events: GTNH uses preInit only
- Manual MCP mapping setup: RFG handles automatically

## Open Questions

1. **AE2 Source Integration Path**
   - What we know: AE2 source at `D:/CODE/Sources/Applied-Energistics-2-Unofficial-rv3-beta-695-GTNH`
   - What's unclear: Exact relative path for Gradle composite build
   - Recommendation: Use `includeBuild` in settings.gradle or local dependency

2. **Package Naming Convention**
   - What we know: User specified "按类型分组（block, tile, item, network, gui）"
   - What's unclear: Root package name
   - Recommendation: Use `com.ae2channelrouter` based on modId

## Validation Architecture

### Test Framework
| Property | Value |
|----------|-------|
| Framework | Forge 1.7.10 built-in + JUnit 4 (standard GTNH) |
| Config file | None - see Wave 0 |
| Quick run command | `./gradlew build` |
| Full suite command | `./gradlew build runClient` |

### Phase Requirements → Test Map
| Req ID | Behavior | Test Type | Automated Command | File Exists? |
|--------|----------|-----------|-------------------|-------------|
| CORE-01 | Gradle build completes | build | `./gradlew build` | ❌ Wave 0 |
| CORE-02 | Mod loads without crash | integration | `./gradlew runClient` (manual) | ❌ Wave 0 |
| CORE-03 | AENetworkProxy wrapper exists | unit | N/A - compile check | ❌ Wave 0 |
| CORE-04 | Base tile extends AENetworkInvTile | compile | `./gradlew compileJava` | ❌ Wave 0 |

### Sampling Rate
- **Per task commit:** `./gradlew build`
- **Per wave merge:** `./gradlew build` + manual client test
- **Phase gate:** Build produces valid JAR, mod loads in MC

### Wave 0 Gaps
- [ ] `build.gradle` - GTNH convention setup
- [ ] `gradle.properties` - mod metadata, versions
- [ ] `settings.gradle` - plugin version, composite build for AE2
- [ ] `dependencies.gradle` - AE2 local dependency
- [ ] `src/main/java/com/ae2channelrouter/AE2ChannelRouter.java` - main mod class
- [ ] `src/main/java/com/ae2channelrouter/tile/AEBaseRouterTile.java` - base tile
- [ ] `src/main/java/com/ae2channelrouter/network/NetworkProxy.java` - wrapper utility
- [ ] `src/main/resources/mcmod.info` - mod metadata
- [ ] `.gitignore` - standard Forge mod ignores

*(No existing test infrastructure - all must be created)*

## Sources

### Primary (HIGH confidence)
- AE2 Source: `D:/CODE/Sources/Applied-Energistics-2-Unofficial-rv3-beta-695-GTNH/src/main/java/appeng/me/helpers/AENetworkProxy.java` - Core grid proxy implementation
- AE2 Source: `D:/CODE/Sources/Applied-Energistics-2-Unofficial-rv3-beta-695-GTNH/src/main/java/appeng/tile/grid/AENetworkInvTile.java` - Base tile for AE2 integration
- AE2 Source: `D:/CODE/Sources/Applied-Energistics-2-Unofficial-rv3-beta-695-GTNH/src/main/java/appeng/me/GridAccessException.java` - Checked exception definition
- AE2 Source: `D:/CODE/Sources/Applied-Energistics-2-Unofficial-rv3-beta-695-GTNH/gradle.properties` - GTNH build configuration
- AE2 Source: `D:/CODE/Sources/Applied-Energistics-2-Unofficial-rv3-beta-695-GTNH/settings.gradle` - GTNH settings plugin version
- AE2 Source: `D:/CODE/Sources/Applied-Energistics-2-Unofficial-rv3-beta-695-GTNH/build.gradle` - Minimal convention plugin usage

### Secondary (MEDIUM confidence)
- AE2 Source: `D:/CODE/Sources/Applied-Energistics-2-Unofficial-rv3-beta-695-GTNH/src/main/java/appeng/api/networking/IGridBlock.java` - Interface for grid nodes
- AE2 Source: `D:/CODE/Sources/Applied-Energistics-2-Unofficial-rv3-beta-695-GTNH/src/main/java/appeng/api/networking/IGridHost.java` - Grid host interface
- AE2 Source: `D:/CODE/Sources/Applied-Energistics-2-Unofficial-rv3-beta-695-GTNH/src/main/java/appeng/api/AEApi.java` - API entry point
- AE2 Source: `D:/CODE/Sources/Applied-Energistics-2-Unofficial-rv3-beta-695-GTNH/src/main/java/appeng/tile/networking/TileController.java` - Example tile implementation

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH - Directly from AE2 source, GTNH conventions well-established
- Architecture: HIGH - Based on AE2 source patterns
- Pitfalls: MEDIUM - Inferred from code analysis, limited runtime validation
- Validation: MEDIUM - Standard GTNH patterns, no project-specific tests yet

**Research date:** 2026-03-16
**Valid until:** 30 days (GTNH conventions stable, AE2 rv3 mature)

---

*Research complete for Phase 1: Core Infrastructure*
