<h1 align = "center">
    Skyblock Collection Tracker - Fabric
</h1>


<div align = "center">
  <img src="src/main/resources/assets/skyblockcollectiontracker/logo.png" alt="Collection Tracker GUI" width="96">
</div>

<p align = "center">
  <a href="https://modrinth.com/mod/sct">
    <img src="https://img.shields.io/modrinth/dt/sct?style=for-the-badge&logo=modrinth&label=Downloads" alt="Downloads" width="150">
  </a>
  <a href="https://modrinth.com/mod/sct">
    <img src="https://img.shields.io/modrinth/v/sct?style=for-the-badge&logo=modrinth&label=Version" alt="Version" width="150">
  </a>
  <a href="LICENSE">
    <img src="https://img.shields.io/github/license/ChindeaOne/SkyblockCollectionTracker-fabric?style=for-the-badge" alt="License" width="150">
  </a>
</p>

---

## Installation

1. Download the latest version from [here](https://github.com/ChindeaYTB/SkyblockCollectionTracker-fabric/releases).
2. Add the mod to your mods folder (`.minecraft/mods`).
3. Make sure you have the [Fabric API](https://modrinth.com/mod/fabric-api/versions) and [Fabric Language Kotlin](https://modrinth.com/mod/fabric-language-kotlin) in your Minecraft mods folder.
4. Run Minecraft with the [Fabric installer](https://fabricmc.net/use/installer/) selected.
5. Access the config GUI using `/sct`.
6. View all available commands with `/sct commands`.

---

## Features

<details>
<summary><strong>Collection & Profit Tracker</strong></summary>

- Track collection progress and profit using sack messages, via `/sct track <collection>`.
- Supports multi-tracking via `/sct track <collection1> <collection2> ...`.
- Option to track via Hypixel's API instead of sacks.
- NPC and Bazaar prices for profit calculations.
- Option to display enchanted/super enchanted/gemstone tier instead of base collection(s).
- Option to show a detailed session summary at the end.
- Provides leaderboard tracking for all collections, with data from Elite API.
- `/sct collections` for all available collections.

</details>

<details>
<summary><strong>Skill Tracker</strong></summary>

- Tracks all skills, displaying current XP, level and rates.
- Uses Hypixel’s API for non‑maxed skills and live in‑game XP messages for maxed skills.
- Additional tracking for Taming, only using Hypixel's API.

</details>

<details>
<summary><strong>Coleweight Port</strong></summary>

- Ported commands like `/sct cw find` or `/sct cw lb`, as well as `/sct cw detailed` for more info.
- Ported Coleweight chat ranks and the option to display them on Mining Islands only.
- Ported Dwarven Heatmap for Umber and Tungsten.
- Ported Precision Mining highlight.
- Ported `/sct timer <set|pause|resume|stop>` command.
- Ported `/sct stopwatch <start|pause|resume|stop>` command.
- Tracks Coleweight with `/sct cw track`.
- Detailed Coleweight profiler with `/sct cw profile`.

</details>

<details>
<summary><strong>Farming Weight</strong></summary>

- Provides commands like `/sct fw find` and `/sct fw lb` to access farming weight data from Elite API.
- Displays farming weight rank in chat for the top 1000 players, with customizable rank colors.
- Detailed farming weight profiler with `/sct fw profile`.

</details>

<details>
<summary><strong>Mining/Foraging Stats Overlay</strong></summary>

- Shows all mining/foraging‑related stats from the tab.
- Updates dynamically based on the targeted block.
- Compacts Fortune into a single context‑aware stat.

</details>

<details>
<summary><strong>Commissions Overlay and Claim Buttons</strong></summary>

- Displays active commissions and progress from tab data.
- Title notifications for completed and new commissions.
- Toggleable commission tracking.
- Keybinds for quick commission claiming.

</details>

<details>
<summary><strong>Pickaxe/Axe Ability Cooldown</strong></summary>

- Shows current pickaxe/axe ability, cooldown and duration while active.
  - by default, it shows only the cooldown
- Configurable title alert when the ability is ready or expired.
- Configurable indicators for ability duration and cooldown.
- Duration updates for MSB as per latest update.

</details>

<details>
<summary><strong>Sky Mall/Lottery/Beekeeper</strong></summary>

- Shows current Sky Mall/Lottery/Beekeeper buffs and their remaining time.
- Compacts chat messages.
- Option to show overlays only in Mining/Foraging Islands.

</details>

<details>
<summary><strong>Mining Deployable Tracker</strong></summary>

- Displays the lantern type and remaining duration while you're in range.
- Configurable title alert when the deployable has expired and out of range.

</details>

<details>
<summary><strong>Mining Routes</strong></summary>

- Displays ordered waypoints in mineshafts, only for jasper and crystal mineshafts.
- Displays ordered waypoints for mineshaft spawning routes.
- Displays ordered waypoints for dwarven metal and pure ore routes.
- All routes provided from the Mining Cult.

</details>

<details>
<summary><strong>Mining Consumable Tracker</strong></summary>

- Tracks consumables like fiesta flask or filet o' fortune.
- Configurable title alert when the consumable has expired.
- Time is updated when opening the Active Effects menu (/effects).

</details>

---

## Documentation

- [FAQ](FAQ.md)
- [Credits](CREDITS.md)

---
