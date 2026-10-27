# Locks - NeoForge 1.21.1 port

An **unofficial port** of [Locks](https://github.com/Melonslise/Locks) by **Melonslise**
from Forge 1.16.5 to **NeoForge 1.21.1**.

Locks adds flexible, universal locks that can be attached to any block (including blocks from
other mods), a lock-picking minigame, keys, key rings, lock picks, and related enchantments.

> **Status: work in progress / experimental.** The mod builds and installs, but not every feature
> has been tested in-game yet. Expect bugs and please report them.

## Requirements

- Minecraft 1.21.1
- NeoForge 21.1.x
- Java 21 (for building)

## Download

Built jars are attached to the [Releases](../../releases) page. Every push is also built by GitHub Actions
(see the *Actions* tab, artifact `locks-neoforge`).

## Building from source

```bash
./gradlew build          # Windows PowerShell: .\gradlew.bat build
```

The jar appears in `build/libs/`. To test in a dev environment: `./gradlew runClient`.

## Known issues

- Locked blocks do **not** ignore redstone yet (doors, trapdoors, fence gates). The old mixin target
  does not exist in 1.21.1 and was removed; a replacement is planned.
- Some mixin targets and the world-generation / loot injection have not been verified in-game.

## What changed compared with 1.16.5

Forge capabilities replaced by data attachments, items use data components, networking uses
payloads with stream codecs, enchantments / damage types / loot / worldgen are data-driven,
configs use `ModConfigSpec`, rendering and GUI code moved to the 1.21 APIs (Mojang mappings).

## Credits and license

- Original mod: **Melonslise** - https://github.com/Melonslise/Locks
- Textures: Hoonts, Artsy (ydgy); sounds: freesound.org
- Port to NeoForge 1.21.1: contributors of this repository

Licensed under **CC BY-NC 3.0** (same as the original), see [LICENSE](LICENSE).
Non-commercial use only; attribution to the original author is required.
