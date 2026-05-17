<img src="./src/main/resources/assets/blockify/icon.png" width="150" align="left"/>

## Blockify Mod
[![Crowdin](https://is.gd/TfsUVl)](https://www.crowdin.com/project/blockify)
[![CurseForge](https://is.gd/BeBNjV)](https://www.curseforge.com/minecraft/mc-mods/blockify)
[![Modrinth](https://is.gd/h1Lgw5)](https://modrinth.com/mod/blockify)
[![License](https://img.shields.io/github/license/clownless/blockify?style=flat-square)](https://github.com/clownless/Blockify/blob/main/LICENSE)
[![ModLoader](https://img.shields.io/badge/modloader-Fabric%2C%20Quilt-1976d2?style=flat-square)](https://fabricmc.net/)
[![Minecraft](https://img.shields.io/badge/minecraft-26.1.2-1976d2?style=flat-square)](https://www.minecraft.net/)
[![Java Version](https://img.shields.io/badge/java-25%20(or%20above)-1976d2?style=flat-square)](https://adoptium.net/releases.html)
![Environment](https://img.shields.io/badge/environment-client-1976d2?style=flat-square)
[![Discord](https://img.shields.io/discord/837540892411691008?label=discord&style=flat-square)](https://discord.gg/bSgZxY3rQm)


Fork of the original [Blockify](https://github.com/BuffMage/Blockify) — overlays your Spotify playback on the Minecraft HUD: track title, artist, album art, progress bar, and volume.

### Requirements

- Minecraft **26.1.2** with **Fabric Loader** 0.19.2+ and **Fabric API**
- Java **25** or newer
- [MidnightLib](https://modrinth.com/mod/midnightlib) 1.9.3+ (for the in-game config screen)
- A **Spotify Premium** account (the Web API endpoints used for playback control are Premium-only)

## How to use

Press `Play/Pause` once to open the Spotify authorization page in your browser. Blockify runs a temporary local server on port `8001` to receive the OAuth callback; tokens are stored in `config/blockifyTokens.json`.

### Default key bindings

| Action | Key |
| --- | --- |
| Play/Pause (or Authorize on first run) | `Num 5` |
| Previous Song | `Num 4` |
| Next Song | `Num 6` |
| Save current track to your library | `L` |
| Force Update | `Num 8` |
| Hide Blockify HUD | `Num 9` |
| Increase Volume | `Num +` |
| Decrease Volume | `Num -` |
| Toggle In-Game Music | `Num 1` |

All bindings can be remapped from the standard Minecraft Controls screen under the **Blockify** category.

### Commands

- `/sharetrack` — sends a link to the currently playing track into chat (visible only to you).

### Configuration

Open the in-game config screen via Mod Menu → Blockify. Available options include HUD scale, position, anchor corner, album-art toggle, custom colors for title/artist/time/progress bar/background, background transparency, in-game music volume, and volume step.

## Screenshots

![Screenshot 1](https://i.imgur.com/5gebkFC.jpeg)
![Screenshot 2](https://i.imgur.com/J74wZr8.jpeg)
![Screenshot 3](https://i.imgur.com/c9Lajim.png)

## Building from source

```bash
./gradlew build          # build the mod JAR (output in build/libs/)
./gradlew runClient      # launch a dev Minecraft client with the mod
```

## Contributing

Contributions are **greatly appreciated**.

1. Fork the project
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## Localisation

Translations are managed through Crowdin — please don't open PRs that hardcode translated strings.

[![Crowdin](https://is.gd/TfsUVl)](https://www.crowdin.com/project/blockify)
