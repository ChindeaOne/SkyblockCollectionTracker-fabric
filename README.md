<h1 align = "center">Skyblock Collection Tracker - Fabric</h1>


<div align="center">
  <img src="src/main/resources/assets/skyblockcollectiontracker/logo.png" alt="Collection Tracker GUI" width="80">
</div>

---

## Installation

1. Download the latest version from [here](https://github.com/ChindeaYTB/SkyblockCollectionTracker-fabric/releases).
2. Add the mod to your mods folder (`.minecraft/mods`).
3. Make sure you have the [Fabric API](https://modrinth.com/mod/fabric-api/versions) and [Fabric Language Kotlin](https://modrinth.com/mod/fabric-language-kotlin) in your Minecraft mods folder.
4. Run Minecraft with the [Fabric installer](https://fabricmc.net/use/installer/) selected.
5. Check all available commands via `/sct commands`.

---

## Features

<details>
<summary><strong>Collection/Profit Tracker</strong></summary>

- Tracks collections and profit over time using Hypixel’s API.
- Uses Bazaar Instant Sell/Buy prices.
- Configurable overlay lines showing collection progress and profit rates.
- Optional Sack Tracking mode for faster live updates.
- Detailed session summary (best/worst rates, elapsed time, profit, etc.).
- AFK detection stops tracking when idle.
- Use `/sct collection` to list available collections.

</details>

<details>
<summary><strong>Skill Tracker</strong></summary>

- Tracks all skills, displaying current XP, level, and rates.
- Uses Hypixel’s API for non‑maxed skills; live in‑game XP messages for maxed skills.
- Additional tracking for Taming.

</details>

<details>
<summary><strong>Coleweight Modern</strong></summary>

- Ported `/cw find`, `/cw lb` commands from original Coleweight mod as `/sct cw find` and `/sct cw lb`.
- New `/sct cw find detailed` command for more comprehensive player stats.
- Ported chat ranks with new custom rank system and colors for top 1000 players in coleweight.
- Option to display cw ranks in chat on Mining Islands only.

</details>

<details>
<summary><strong>Mining Stats Overlay</strong></summary>

- Shows all mining‑related stats from tab.
- Updates dynamically based on the targeted block.
- Compacts Mining Fortune into a single context‑aware stat.

</details>

<details>
<summary><strong>Commissions Overlay and Claim Buttons</strong></summary>

- Displays active commissions and progress from tab data.
- Keybinds for quick commission claiming.

</details>

---

## Credits

All credits can be found [here](CREDITS.md).

---