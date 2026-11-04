# 🍬 FantaClient

**A free, open-source Minecraft Fabric fun and cheat client for Minecraft 1.21.11.**

FantaClient combines visual modules, movement utilities, cosmetics and fun features in one client-side mod with a custom ClickGUI.

> FantaClient is an independent community project and is not affiliated with or endorsed by Mojang or Microsoft. Some features may be restricted by multiplayer server rules. Check a server’s rules before using them.

## ✨ Features

### Movement
- **Freecam** — detach the camera and move it independently.
- **AutoSprint** — automatically sprint while moving forward.

### Visuals
- **FullBright** — increase world brightness.
- **Block ESP** and **Storage ESP** — highlight selected blocks and storage containers.
- **Player ESP** — player boxes and optional tracers.
- **Name Tags**, **Zoom**, **HUD**, **NoWeather** and **Custom Crosshair**.

### Utilities
- **Anti-Vanish**, **Chunk Analyzer** and **Schematic Builder**.

### Fun and cosmetics
- **Twerk**, **Derp**, **Spin**, **Rainbow**, **Flame Halo**, **Heart Aura**, **Sparkle Trail**, **Note Trail** and **Wings**.
- **Panic** — quickly disable all modules.

Features are subject to change; some may be experimental or have compatibility limitations.

## 📥 Requirements

- Minecraft **1.21.11**
- Java **21 or newer**
- Fabric Loader **0.20.1 or newer**
- Fabric API version compatible with Minecraft 1.21.11

## 📦 Installation

1. Install Fabric for Minecraft 1.21.11.
2. Install a compatible Fabric API release.
3. Download the FantaClient JAR from the repository’s **Releases** page (when a release is published).
4. Place the JAR in your Minecraft `mods` folder.
5. Launch the game using your Fabric profile.

To open or close the ClickGUI, press **Right Shift**. Press **Esc** to close it.

## 🔧 Build from source

You need Git and Java 21 or newer.

```bash
git clone <YOUR_REPOSITORY_URL>
cd fantaclient
```

On Windows:

```bat
gradlew.bat build
```

On Linux or macOS:

```bash
chmod +x ./gradlew
./gradlew build
```

The compiled mod JAR will be in `build/libs/`.

## ⚙️ Configuration

FantaClient stores its configuration in the Minecraft instance's `config/fantaclient/` directory. Settings and profiles can be managed from the in-game GUI.

### Schematic Builder

The Schematic Builder reads text-based schematic files from `config/fantaclient/schematics/`. The expected format is one block per line:

```text
x,y,z,minecraft:block_id
```

For example:

```text
0,0,0,minecraft:stone
1,0,0,minecraft:stone
```

You must have the required blocks available. Automated block placement may be prohibited on some servers; only use it where permitted.

## 🐛 Issues and contributions

Bug reports, suggestions and pull requests are welcome. When reporting a crash, include:

- Minecraft version
- Fabric Loader and Fabric API versions
- Other installed mods
- The relevant `latest.log` or crash report
- Steps to reproduce the issue

Please do not report passwords, access tokens or other private information.

## 📜 License

This project currently includes a **CC0 1.0 Universal** license. See [LICENSE](LICENSE) for the full text.

---

**Made for fun. Built to be shared. 🍬**
