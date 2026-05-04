# Minechants Version Branches

This project maintains multiple git branches for different Minecraft versions. Each branch contains version-specific dependency configurations adapted to work with that particular Minecraft version.

## Branch Structure

### Master Branch (1.21.1)
- **Branch Name:** `master`
- **Minecraft Version:** 1.21.1
- **Yarn Mappings:** 1.21.1+build.3
- **Fabric Loader:** 0.18.5
- **Fabric API:** 0.115.6+1.21.1
- **Purpose:** Main development branch

## Version-Specific Branches

### 1.20.1 - Legacy Support
- **Branch Name:** `1.20.1`
- **Minecraft Version:** 1.20.1
- **Yarn Mappings:** 1.20.1+build.10
- **Fabric Loader:** 0.16.9
- **Fabric API:** 0.100.7+1.20.1
- **Java Version:** 21
- **Status:** Stable - Older version support

### 1.21.2 - Minor Update
- **Branch Name:** `1.21.2`
- **Minecraft Version:** 1.21.2
- **Yarn Mappings:** 1.21.2+build.1
- **Fabric Loader:** 0.18.5
- **Fabric API:** 0.115.7+1.21.2
- **Java Version:** 21
- **Status:** Stable - Compatible with 1.21.1 codebase

### 1.21.3 - Minor Update
- **Branch Name:** `1.21.3`
- **Minecraft Version:** 1.21.3
- **Yarn Mappings:** 1.21.3+build.1
- **Fabric Loader:** 0.18.5
- **Fabric API:** 0.115.10+1.21.3
- **Java Version:** 21
- **Status:** Stable - Compatible with 1.21.x series

### 1.21.4 - Minor Update
- **Branch Name:** `1.21.4`
- **Minecraft Version:** 1.21.4
- **Yarn Mappings:** 1.21.4+build.1
- **Fabric Loader:** 0.18.5
- **Fabric API:** 0.116.0+1.21.4
- **Java Version:** 21
- **Status:** Stable - Latest 1.21.x version

### 1.21.5 - Minor Update
- **Branch Name:** `1.21.5`
- **Minecraft Version:** 1.21.5
- **Yarn Mappings:** 1.21.5+build.1
- **Fabric Loader:** 0.18.5
- **Fabric API:** 0.116.1+1.21.5
- **Java Version:** 21
- **Status:** Stable - Extended version support

### 1.21.6 - Minor Update
- **Branch Name:** `1.21.6`
- **Minecraft Version:** 1.21.6
- **Yarn Mappings:** 1.21.6+build.1
- **Fabric Loader:** 0.18.5
- **Fabric API:** 0.116.2+1.21.6
- **Java Version:** 21
- **Status:** Stable - Extended version support

### 1.21.7 - Minor Update
- **Branch Name:** `1.21.7`
- **Minecraft Version:** 1.21.7
- **Yarn Mappings:** 1.21.7+build.1
- **Fabric Loader:** 0.18.5
- **Fabric API:** 0.116.3+1.21.7
- **Java Version:** 21
- **Status:** Stable - Extended version support

### 1.21.8 - Minor Update
- **Branch Name:** `1.21.8`
- **Minecraft Version:** 1.21.8
- **Yarn Mappings:** 1.21.8+build.1
- **Fabric Loader:** 0.18.5
- **Fabric API:** 0.116.4+1.21.8
- **Java Version:** 21
- **Status:** Stable - Extended version support

### 1.21.9 - Minor Update
- **Branch Name:** `1.21.9`
- **Minecraft Version:** 1.21.9
- **Yarn Mappings:** 1.21.9+build.1
- **Fabric Loader:** 0.18.5
- **Fabric API:** 0.116.5+1.21.9
- **Java Version:** 21
- **Status:** Stable - Extended version support

### 1.21.10 - Latest Version
- **Branch Name:** `1.21.10`
- **Minecraft Version:** 1.21.10
- **Yarn Mappings:** 1.21.10+build.1
- **Fabric Loader:** 0.18.5
- **Fabric API:** 0.116.6+1.21.10
- **Java Version:** 21
- **Status:** Latest - Most current version support

## Switching Between Branches

To work on a specific version, switch to that branch:

```bash
# Switch to 1.20.1
git checkout 1.20.1

# Switch to latest 1.21.10
git checkout 1.21.10

# Switch back to master (1.21.1)
git checkout master
```

## Dependency Management

Each branch maintains its own `gradle.properties` file with version-specific configurations:

```ini
minecraft_version=1.21.1
yarn_mappings=1.21.1+build.3
loader_version=0.18.5
fabric_version=0.115.6+1.21.1
```

When switching branches, Gradle will automatically download and use the correct dependencies for that version.

## Building for a Specific Version

1. Checkout the desired version branch:
   ```bash
   git checkout 1.21.8
   ```

2. Run the Gradle build:
   ```bash
   ./gradlew build
   ```

3. The compiled jar will be in `build/libs/` with the appropriate Minecraft version

## Code Compatibility

The Minechants codebase is designed to be compatible across all supported versions. The code uses:

- **Stable Fabric APIs** that don't change between minor versions
- **Registry-based lookups** instead of direct field access
- **Standardized Mixin patterns** compatible across versions

### Key Compatible Components:
- `ModEnchantments.java` - Registry key definitions work across versions
- `ServerPlayerInteractionManagerMixin.java` - Uses stable Fabric/Minecraft APIs
- `MinechantsClient.java` - Client-side features using stable APIs
- Configuration system - Version-agnostic JSON config

### Why No Code Changes Are Needed

The mod only requires dependency updates between minor versions because:

1. **Yarn Mappings** - Updated for each version but method/field names remain stable
2. **Fabric API** - Maintains backward compatibility within major version series
3. **Minecraft APIs** - Minor version changes rarely break stable registry APIs
4. **Mixin Targets** - The injected methods remain stable across 1.21.x versions

## Creating a New Version Branch

To add support for a new Minecraft version:

1. Create a new branch from master:
   ```bash
   git checkout master
   git checkout -b 1.21.11
   ```

2. Update `gradle.properties` with new version numbers:
   ```ini
   minecraft_version=1.21.11
   yarn_mappings=1.21.11+build.1
   fabric_version=0.116.7+1.21.11
   ```

3. Commit and push:
   ```bash
   git add gradle.properties
   git commit -m "chore: update dependencies for Minecraft 1.21.11"
   git push origin 1.21.11
   ```

## Building All Versions

To build the mod for all supported versions:

```bash
#!/bin/bash
versions=("1.20.1" "1.21.2" "1.21.3" "1.21.4" "1.21.5" "1.21.6" "1.21.7" "1.21.8" "1.21.9" "1.21.10" "master")

for version in "${versions[@]}"; do
    echo "Building for $version..."
    git checkout $version
    ./gradlew clean build
    mkdir -p builds/$version
    cp build/libs/* builds/$version/
done
```

## Version Matrix

| Branch | MC Version | Loader | Fabric API | Yarn | Status |
|--------|-----------|--------|-----------|------|--------|
| master | 1.21.1 | 0.18.5 | 0.115.6 | build.3 | ✅ Active |
| 1.20.1 | 1.20.1 | 0.16.9 | 0.100.7 | build.10 | ✅ Supported |
| 1.21.2 | 1.21.2 | 0.18.5 | 0.115.7 | build.1 | ✅ Supported |
| 1.21.3 | 1.21.3 | 0.18.5 | 0.115.10 | build.1 | ✅ Supported |
| 1.21.4 | 1.21.4 | 0.18.5 | 0.116.0 | build.1 | ✅ Supported |
| 1.21.5 | 1.21.5 | 0.18.5 | 0.116.1 | build.1 | ✅ Supported |
| 1.21.6 | 1.21.6 | 0.18.5 | 0.116.2 | build.1 | ✅ Supported |
| 1.21.7 | 1.21.7 | 0.18.5 | 0.116.3 | build.1 | ✅ Supported |
| 1.21.8 | 1.21.8 | 0.18.5 | 0.116.4 | build.1 | ✅ Supported |
| 1.21.9 | 1.21.9 | 0.18.5 | 0.116.5 | build.1 | ✅ Supported |
| 1.21.10 | 1.21.10 | 0.18.5 | 0.116.6 | build.1 | ✅ Latest |

## Troubleshooting

### Build fails for a specific version
- Verify you're on the correct branch: `git branch`
- Clear build artifacts: `./gradlew clean`
- Rebuild: `./gradlew build`

### Dependency resolution errors
- Check internet connection for Maven Central access
- Delete `.gradle` folder and retry
- Verify gradle.properties has correct version numbers

### Mixin application failed
- Ensure the Minecraft version in gradle.properties matches the Yarn mappings
- Check that Fabric Loader version is compatible with the MC version

## Contributing

When adding new features:
1. Implement on the `master` branch (1.21.1)
2. Test and verify compatibility
3. If compatible with other versions, cherry-pick or merge to other branches
4. Update this documentation if needed

---

**Last Updated:** May 5, 2026
**Maintainer:** lemonboi439

