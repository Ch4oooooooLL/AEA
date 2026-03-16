# Technology Stack: Minecraft 1.7.10 Mod Development

**Project Context:** Greenfield 1.7.10 mod development
**Research Date:** 2026-03-16
**Confidence:** MEDIUM (based on real project example; limited web verification due to age of version)

## Recommended Stack

### Core Framework

| Technology | Version | Purpose | Why |
|------------|---------|---------|-----|
| Minecraft | 1.7.10 | Game version | Target version for mod |
| Minecraft Forge | 10.13.4.1614 | Modding API | Latest stable Forge for 1.7.10; provides mod loading, event system, and API |
| FML (Forge Mod Loader) | Built into Forge | Core loading | Required for mod initialization |

### Build Tools

| Tool | Version | Purpose | Why |
|------|---------|---------|-----|
| Gradle | 8.13 (GTNH) or 4.9-7.x (vanilla) | Build automation | Manages dependencies, deobfuscation, and JAR building |
| GTNH Convention Plugin | 1.0.42+ | Build conventions | Opinionated plugin with code style, publishing, auto-update |
| Jabel | Latest | Java syntax transformer | Allows modern Java (up to 17) while targeting JVM 8 |
| MCP (Mod Coder Pack) | Version 12 | Code deobfuscation | Required to deobfuscate Minecraft for modding |

### Java Configuration

| Setting | Value | Why |
|---------|-------|-----|
| Target JVM | 8 | 1.7.10 client/server run on Java 8 |
| Source Compatibility | 8 | Ensures bytecode runs on Java 8 |
| Modern Syntax | Enabled via Jabel | Better developer experience without runtime cost |

### IDE Support

| IDE | Recommended | Notes |
|-----|-------------|-------|
| IntelliJ IDEA | Yes | Best Gradle integration; GTNH plugin optimized for IDEA |
| Eclipse | Possible | Requires Gradle IDE plugin; more manual setup |
| VS Code | Possible | Requires Java extension pack; less common in 1.7.10 space |

## Project Structure

```
my-mod/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/example/mod/
│       │       └── ExampleMod.java
│       └── resources/
│           ├── mcmod.info
│           └── META-INF/
│               └── neenergistics_at.cfg (if using Access Transformers)
├── build.gradle
├── dependencies.gradle
├── settings.gradle
├── gradle.properties
├── gradlew
└── gradlew.bat
```

## Standard Build Configuration

### gradle.properties (Core Settings)

```properties
modName = MyMod
modId = mymodid
modGroup = com.example.mymodid
minecraftVersion = 1.7.10
forgeVersion = 10.13.4.1614
channel = stable
mappingsVersion = 12
enableModernJavaSyntax = true
enableGenericInjection = true
```

### build.gradle (Minimal)

```groovy
plugins {
    id 'com.gtnewhorizons.gtnhconvention'
}
```

### dependencies.gradle

```groovy
dependencies {
    // Compile-time dependencies (included in dev workspace)
    api('com.github.GTNewHorizons:Applied-Energistics-2-Unofficial:rv3-beta-XXX:dev')
    api('com.github.GTNewHorizons:NotEnoughItems:2.7.XXX-GTNH:dev')

    // Runtime-only dependencies (not in dev workspace, but in final JAR)
    runtimeOnly('curse.maven:cofh-core-69162:2388751')
}
```

## Key Dependencies for 1.7.10

### Common Modding Libraries

| Library | Purpose | When to Use |
|---------|---------|-------------|
| CodeChickenCore | NEI/ChickenChunks core | If mod interacts with NEI or world chunks |
| Applied Energistics 2 | Storage system API | If mod deals with AE2 crafting/ores |
| GT5 (GregTech 5) | Industrial mod API | If creating GT-compatible machines |
| Not Enough Items | Recipe viewing | If displaying custom recipes in NEI |
| Thaumcraft | Magic mod API | If adding magic-related content |

### Repository Sources

| Source | URL | Use Case |
|--------|-----|----------|
| GTNH Maven | https://nexus.gtnewhorizons.com/repository/public/ | GTNH-modified mods |
| CurseMaven | curse.maven | Third-party mods (Thermal Expansion, etc.) |
| Maven Central | mavenCentral() | Standard Java libraries |
| Maven Local | mavenLocal() | Local development builds |

## Alternatives Considered

| Category | Recommended | Alternative | Why Not |
|----------|-------------|-------------|---------|
| Build Plugin | GTNH Convention | Vanilla ForgeGradle | GTNH provides code style, publishing, auto-update out of the box |
| Gradle Version | 8.13 | 4.9-7.x | GTNH plugin requires 8.x; vanilla ForgeGradle 1.2 was last for 1.7.10 |
| Java Version | 8 (with Jabel) | 11+ direct | 1.7.10 runtime requires Java 8 bytecode |

## Anti-Patterns to Avoid

1. **Do NOT use Java 11+ direct compilation** - 1.7.10 runtime does not support Java 9+ bytecode; use Jabel to transform modern syntax to Java 8 bytecode

2. **Do NOT use the latest Forge version blindly** - 10.13.4.1614 is stable; newer versions may break compatibility with existing mods

3. **Do NOT skip MCP mappings** - Required for deobfuscation; version 12 is the standard for 1.7.10

4. **Do NOT use modern Gradle plugins without compatibility check** - Many plugins assume Minecraft 1.13+ or Java 11+

## Installation Quick Start

```bash
# Clone the template or create project structure
# Then run:

# Setup workspace (downloads Minecraft, deobfuscates)
./gradlew setupDecompWorkspace

# Generate IDE project
./gradlew idea  # For IntelliJ
# OR
./gradlew eclipse  # For Eclipse

# Run development client
./gradlew runClient

# Build JAR
./gradlew build
```

## Sources

- **Primary Reference:** NotEnoughEnergistics-1.7.14 project (GTNH mod)
  - `gradle.properties` - Core version configuration
  - `gradle/wrapper/gradle-wrapper.properties` - Gradle 8.13
  - `settings.gradle` - GTNH plugin v1.0.42

**Confidence Notes:**
- The GTNH convention plugin represents the modern 1.7.10 modding ecosystem
- Vanilla ForgeGradle (forgegradle 1.2) is older but still works
- Web search could not verify versions (1.7.10 is from 2014, limited current documentation)
- Recommendations based on active 1.7.10 project (NotEnoughEnergistics)