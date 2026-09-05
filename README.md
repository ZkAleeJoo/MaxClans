<div align="center">
  <h1>OnlyClans - Official Wiki</h1>
  <p>
    <img src="https://img.shields.io/badge/version-1.0.0-blue" alt="Version">
    <img src="https://img.shields.io/badge/Java-21+-red" alt="Java">
    <img src="https://img.shields.io/badge/Paper--Folia-1.21--26.1.2+-green" alt="Paper-Folia">
    <img src="https://img.shields.io/badge/Adventure-MiniMessage-brightgreen" alt="MiniMessage">
    <img src="https://img.shields.io/badge/Languages-EN_|_ES-blue" alt="Languages">
  </p>
</div>

Welcome to the **OnlyClans** Official Wiki! Here you will find all the information you need to configure and use the plugin on your Minecraft server.

## Table of Contents
1. [Features](#-features)
2. [Commands & Permissions](#-commands--permissions)
3. [Clan Flags & Privileges](#-clan-flags--privileges)
4. [Clan KDR & Global Statistics](#-clan-kdr--global-statistics)
5. [Leaderboard & Top Clans (/clan top)](#-leaderboard--top-clans-clan-top)
6. [Placeholders](#-placeholders)
7. [Color & Text Formatting Guide (MiniMessage & Legacy)](#-color--text-formatting-guide-minimessage--legacy)
8. [Menus Guide](#-menus-guide)
9. [Performance & Rate Limits Guide](#-performance--rate-limits-guide)
10. [Database Configuration](#-database-configuration)
11. [Future Updates](#-future-updates)

---

## Features
OnlyClans is designed to be a lightweight, highly customizable, and easy-to-use clan system for modern Minecraft servers.
- **Full GUI Support**: Create, manage, and view your clan through interactive inventory menus.
- **Full MiniMessage & Rich Text Support**: Native support for Adventure MiniMessage (`<gradient>`, `<rainbow>`, `<#hex>`, `<b>`, `<i>`, `<click>`, `<hover>`), Spigot hex (`&#RRGGBB`), and legacy color codes (`&`, `§`) across all menus, config files, messages, and clan chat.
- **Clan Roles**: Leader, Moderator, and Member roles with different permissions (invite, kick, promote, demote).
- **Clan KDR & Global Statistics**: Real-time database tracking for clan kills, deaths, rival war kills, and ratio ($KDR = \frac{Kills}{Deaths}$).
- **Leaderboard & Top Clans (`/clan top`)**: Interactive paginated GUI and commands to rank top clans by KDR, Kills, or Member roster size.
- **Advanced Clan Flags & Privileges**: Interactive GUI menu for leaders and moderators to toggle 7 advanced clan flags: Friendly Fire, Open Join, Ally Damage, Member Invites, Visibility in List, Public Home, and Administrative Spy Chat.
- **Clan Chat & Spy**: Private communication channel for clan members (`/c <message>`) and administrative monitoring mode for staff (`/clan spy`).
- **PlaceholderAPI Integration**: Display clan stats, global top leaderboards, roles, and flag statuses anywhere (chat, scoreboard, tablist, holograms).
- **Multiple Databases**: Supports both SQLite (local) and MySQL (remote) storage with automated migrations.
- **Multilingual**: Built-in support for English and Spanish, fully customizable in `messages_en.yml` and `messages_es.yml`.
- **Folia Support**: Fully compatible with Folia and Paper servers.

---

## Commands & Permissions

> [!NOTE]
> All `/clan` commands can also be executed using the aliases `/olc` or `/onlyclans`.

### Player Commands
Basic commands accessible to all players by default (`onlyclans.use` or individual `onlyclans.command.*` permissions).

| Command | Aliases | Description | Permission | Default |
|---------|---------|-------------|------------|---------|
| `/clan` | `/olc`, `/onlyclans` | Opens the main GUI menu. | `onlyclans.command.main` | `true` |
| `/clan help` | `/olc help` | Displays the help message and wiki link. | `onlyclans.command.help` | `true` |
| `/clan list` | `/olc browse`, `/clan browse` | Opens the clan browser GUI to view all clans. | `onlyclans.command.list` | `true` |
| `/clan info` | `/olc info` | Displays your clan's info and roster in a menu. | `onlyclans.command.info` | `true` |
| `/clan top [kdr\|kills\|members]` | `/olc top`, `/clan leaderboard` | Opens the clan leaderboard GUI (or filters by category). | `onlyclans.command.top` | `true` |
| `/clan create <name>` | `/olc create <name>` | Creates a new clan (can also be done via GUI). | `onlyclans.command.create` | `true` |
| `/clan request <clan>` | `/olc join <clan>`, `/clan join` | Requests to join an existing clan. | `onlyclans.command.request` | `true` |
| `/clan accept` | `/olc accept` | Accepts a pending clan invitation. | `onlyclans.command.accept` | `true` |
| `/clan deny` | `/olc deny` | Denies a pending clan invitation. | `onlyclans.command.deny` | `true` |
| `/clan leave` | `/olc leave` | Leaves your current clan. | `onlyclans.command.leave` | `true` |
| `/clan chat <msg>` | `/c <msg>`, `/cc <msg>`, `/clanchat` | Sends a message to the private clan chat. | `onlyclans.command.chat` | `true` |

### Clan Management Commands
Commands available for Clan Leaders and Moderators.

| Command | Aliases | Description | Role Required | Permission | Default |
|---------|---------|-------------|---------------|------------|---------|
| `/clan flags` | `/clan settings`, `/olc flags` | Opens the Clan Flags & Privileges GUI. | Leader, Moderator | `onlyclans.command.flags` | `true` |
| `/clan flag <flag> [on\|off\|toggle]` | `/olc flag ...` | Toggles or sets a clan flag via command. | Leader, Moderator | `onlyclans.command.flags` | `true` |
| `/clan invite <player>` | `/olc invite <player>` | Invites a player to your clan (Members can invite if `member_invites` is enabled). | Leader, Moderator, Member* | `onlyclans.command.invite` | `true` |
| `/clan acceptrequest <player>` | `/olc acceptjoin` | Accepts a player's request to join the clan. | Leader, Moderator | `onlyclans.command.acceptrequest` | `true` |
| `/clan denyrequest <player>` | `/olc denyjoin` | Denies a player's request to join the clan. | Leader, Moderator | `onlyclans.command.denyrequest` | `true` |
| `/clan kick <player>` | `/olc kick <player>` | Kicks a player from the clan. | Leader, Moderator | `onlyclans.command.kick` | `true` |
| `/clan promote <player>` | `/olc promote <player>` | Promotes a Member to Moderator. | Leader | `onlyclans.command.promote` | `true` |
| `/clan demote <player>` | `/olc demote <player>` | Demotes a Moderator to Member. | Leader | `onlyclans.command.demote` | `true` |
| `/clan disband` | `/olc disband` | Permanently deletes the clan. | Leader | `onlyclans.command.disband` | `true` |

### Admin Commands
Commands and privileges reserved for server administrators.

| Command | Aliases | Description | Permission | Default |
|---------|---------|-------------|------------|---------|
| `/clan spy [on\|off]` | `/olc spy` | Toggles global clan chat spy monitoring for staff. | `onlyclans.spy` / `onlyclans.admin` | `op` |
| `/clan reload` | `/olc reload`, `/onlyclans reload` | Reloads all configuration files, messages, and menus. | `onlyclans.command.reload` / `onlyclans.admin` | `op` |
| *Update Notifications* | N/A | Receives in-game notifications when a new update is available. | `onlyclans.admin` | `op` |

---

### Permissions Reference & Hierarchy

OnlyClans uses a hierarchical permission structure compatible with all standard permission managers (**LuckPerms**, UltraPermissions, PermissionsEx, etc.).

```text
onlyclans.admin (default: op)
├── onlyclans.command.reload
├── onlyclans.spy
└── onlyclans.use (default: true)
    ├── onlyclans.command.main
    ├── onlyclans.command.help
    ├── onlyclans.command.list
    ├── onlyclans.command.top
    ├── onlyclans.command.info
    ├── onlyclans.command.flags
    ├── onlyclans.command.settings
    ├── onlyclans.command.create
    ├── onlyclans.command.request
    ├── onlyclans.command.accept
    ├── onlyclans.command.deny
    ├── onlyclans.command.leave
    ├── onlyclans.command.chat
    ├── onlyclans.command.invite
    ├── onlyclans.command.acceptrequest
    ├── onlyclans.command.denyrequest
    ├── onlyclans.command.kick
    ├── onlyclans.command.promote
    ├── onlyclans.command.demote
    └── onlyclans.command.disband
```

#### Example LuckPerms Configurations:
* **Revoke clan creation for default players (e.g. VIP-only feature):**
  ```bash
  /lp group default permission set onlyclans.command.create false
  /lp group vip permission set onlyclans.command.create true
  ```
* **Disable clan chat on a specific world/server:**
  ```bash
  /lp group default permission set onlyclans.command.chat false
  ```
* **Grant clan chat spy permission to moderators:**
  ```bash
  /lp group mod permission set onlyclans.spy true
  ```
* **Grant full admin access:**
  ```bash
  /lp user <admin> permission set onlyclans.admin true
  ```

---

## Clan Flags & Privileges

OnlyClans features an advanced flag system allowing leaders and moderators to fine-tune combat, invitations, visibility, and privacy. Flags can be toggled through the interactive GUI (`/clan flags` or `/clan settings`) or via command (`/clan flag <flag> [on|off|toggle]`).

| Flag Key | Icon | Default | Who Can Toggle | Description |
|---|:---:|:---:|---|---|
| `friendly_fire` | ⚔️ | `false` | Leader, Moderator | Enables or disables PvP combat damage between members of the same clan. |
| `open_join` | 🔓 | `false` | Leader, Moderator | When enabled, players can join instantly without needing an invite or waiting for request approval (works via `/clan join <clan>` or clicking in `/clan list`). |
| `ally_damage` | 🛡️ | `false` | Leader, Moderator | Prevents or allows PvP combat damage against members of allied clans. If either clan has it disabled, damage is cancelled. |
| `member_invites`| 📢 | `false` | Leader, Moderator | Allows regular Members to invite new players (`/clan invite <player>`). If disabled, only Leaders and Moderators can invite. |
| `visible_in_list`| 👁️ | `true` | Leader, Moderator | Controls whether the clan appears in the public `/clan list` browser. Server admins and members of the clan can always see it. |
| `public_home` | 🏠 | `false` | Leader, Moderator | Allows or restricts allied clans from teleporting to your clan base. |
| `spy_chat` | 💬 | `false` | **Admin Only** | Administrative monitoring flag that mirrors private clan chat messages to staff members with `onlyclans.spy`. |

---

## Clan KDR & Global Statistics

OnlyClans features its own dedicated, server-wide clan PvP combat and kill/death ratio tracking system stored directly in the database. Unlike vanilla Minecraft player statistics, this system records organized clan warfare and collective performance.

### Recorded Database Statistics
* **Clan Kills (`clan_kills`)**: Total kills made by clan members against clanless players or members of rival clans.
* **Clan Deaths (`clan_deaths`)**: Total deaths suffered by clan members at the hands of enemy players.
* **Rival Clan Kills (`rival_kills`)**: Special war counter tracking kills specifically made against members of other clans.
* **Clan KDR (`KDR`)**: Real-time ratio calculated using:
  $$\text{KDR} = \frac{\text{Clan Kills}}{\text{Clan Deaths}}$$
  *(If deaths equal 0, the KDR equals the total kills. Formatted to two decimal places, e.g. `4.50`)*.

### PvP Combat & Anti-Exploit Rules
| Killer | Victim | Stat Effects |
|---|---|---|
| Member of Clan A | Clanless Player | Clan A `clan_kills + 1` |
| Member of Clan A | Member of Clan B | Clan A `clan_kills + 1`<br>Clan A `rival_kills + 1`<br>Clan B `clan_deaths + 1` |
| Clanless Player | Member of Clan A | Clan A `clan_deaths + 1` |
| Member of Clan A | Member of Clan A (Friendly Fire) | **No stats awarded** (Prevents stat boosting / kill farming) |

---

## Leaderboard & Top Clans (`/clan top`)

Players and administrators can view real-time rankings of the best clans on the server using `/clan top` or by clicking the Top Clans icon in the main menu (`/clan`).

### Subcommands & Filters
* `/clan top`: Opens the interactive Top Clans GUI with the default filter (KDR).
* `/clan top kdr`: Opens the leaderboard sorted by highest KDR ratio.
* `/clan top kills`: Opens the leaderboard sorted by most bloodthirsty clans (total clan kills).
* `/clan top members`: Opens the leaderboard sorted by largest and most active rosters.

### Interactive Leaderboard GUI Features
* **Live Sorting Buttons**: Toggle directly between Top KDR, Top Kills, and Top Members from within the menu with one click.
* **Dynamic Clan Heads**: Renders real-time player heads of top clan leaders.
* **Rank Badges**: Distinct visual badges for top clans (`#1 ✦` Gold, `#2 ✦` Silver, `#3 ✦` Bronze, `#4+` Gray).
* **Multi-Page Pagination**: Seamlessly browse through all server clans with Next and Previous buttons.

---

## Placeholders

OnlyClans integrates with **PlaceholderAPI** and provides internal placeholders for menus and chat. Placeholders dynamically adapt according to `general.language` in `config.yml`, and can also be queried with an explicit language suffix (`_en`, `_es`) or prefix (`en_`, `es_`).

### PlaceholderAPI Expansion (`%onlyclans_<placeholder>%`)
You can use these placeholders in plugins like TAB, EssentialsX Chat, DecentHolograms, or scoreboard plugins:

| Placeholder | Description | Example (EN) | Example (ES) |
|-------------|-------------|--------------|--------------|
| `%onlyclans_name%` | Clan name | `Vikings` | `Vikings` |
| `%onlyclans_tag%` | 3-letter clan tag | `VIK` | `VIK` |
| `%onlyclans_tag_formatted%` | Formatted colored tag | `&#8727F5[VIK]` | `&#8727F5[VIK]` |
| `%onlyclans_role%` | Localized role name | `Leader` / `Moderator` / `Member` | `Líder` / `Moderador` / `Miembro` |
| `%onlyclans_role_raw%` | Raw role enum name | `LEADER` / `MODERATOR` / `MEMBER` | `LEADER` / `MODERATOR` / `MEMBER` |
| `%onlyclans_role_formatted%` | Role badge with icon | `&#FFD700★ Leader` | `&#FFD700★ Líder` |
| `%onlyclans_members_count%` | Total clan member count | `8` | `8` |
| `%onlyclans_members_online%` | Number of currently online members | `3` | `3` |
| `%onlyclans_leader%` | Username of the clan leader | `ZkAleeJoo` | `ZkAleeJoo` |
| `%onlyclans_ff%` | Friendly Fire status | `ON` / `OFF` | `ON` / `OFF` |
| `%onlyclans_ff_status%` | Friendly Fire formatted status | `&#00FF88✔ Enabled` | `&#00FF88✔ Activado` |
| `%onlyclans_ff_badge%` | Friendly Fire badge | `&#00FF88&lENABLED` | `&#00FF88&lHABILITADO` |
| `%onlyclans_flag_<flag>%` | Formatted status of a clan flag | `&#00FF88✔ Enabled` | `&#00FF88✔ Activado` |
| `%onlyclans_flag_<flag>_badge%` | Formatted badge of a clan flag | `&#00FF88&lENABLED` | `&#00FF88&lHABILITADO` |
| `%onlyclans_flag_<flag>_status%` | Formatted status of a clan flag | `&#00FF88✔ Enabled` | `&#00FF88✔ Activado` |
| `%onlyclans_clan_kills%` | Total clan kills in PvP wars | `142` | `142` |
| `%onlyclans_clan_deaths%` | Total clan deaths in PvP wars | `38` | `38` |
| `%onlyclans_clan_kdr%` | Real-time Clan KDR ratio | `3.74` | `3.74` |
| `%onlyclans_clan_rival_kills%` | Bajas contra miembros de clanes rivales | `95` | `95` |
| `%onlyclans_created%` | Clan founding date | `29/08/2026` | `29/08/2026` |

### Global Top & Leaderboard Placeholders
Easily create Holograms, Scoreboards, and Tablists displaying the server's top-ranking clans!
Format: `%onlyclans_top_<type>_<rank>_<property>%`

* **Available Types (`<type>`)**:
  * `kdr` — Ranked by highest Kill/Death ratio
  * `kills` — Ranked by total clan kills
  * `members` — Ranked by member roster size
* **Available Ranks (`<rank>`)**:
  * `1`, `2`, `3`, `4`, `5` ... any integer rank.
* **Available Properties (`<property>`)**:
  * `name` — Clan name (e.g. `Vikings`)
  * `tag` — 3-letter tag (e.g. `VIK`)
  * `tag_formatted` — Formatted tag with brackets
  * `leader` — Username of the clan leader
  * `val` / `value` — Value according to the sort type (`KDR` formatted for kdr, count for kills/members)
  * `kdr` — Explicit KDR ratio (`3.50`)
  * `kills` — Total clan kills
  * `deaths` — Total clan deaths
  * `rival_kills` — Kills against rival clans
  * `members` — Total clan members

#### Top Placeholders Examples:
| Placeholder | Description | Example Output |
|---|---|---|
| `%onlyclans_top_kdr_1_name%` | Clan name of #1 KDR clan | `Vikings` |
| `%onlyclans_top_kdr_1_val%` | KDR value of #1 KDR clan | `5.20` |
| `%onlyclans_top_kdr_1_leader%` | Leader of #1 KDR clan | `ZkAleeJoo` |
| `%onlyclans_top_kills_1_name%` | Clan name of #1 Most Kills | `Spartans` |
| `%onlyclans_top_kills_1_val%` | Total kills of #1 clan | `1250` |
| `%onlyclans_top_members_1_name%` | Name of largest clan | `Empire` |
| `%onlyclans_top_members_1_val%` | Member count of largest clan | `32` |

### Multi-Language Explicit Placeholders
If your network supports multiple languages at once, you can explicitly request the output language directly:
* `%onlyclans_role_formatted_en%` ➔ `★ Leader`
* `%onlyclans_role_formatted_es%` ➔ `★ Líder`
* `%onlyclans_role_en%` ➔ `Leader`
* `%onlyclans_role_es%` ➔ `Líder`
* `%onlyclans_ff_badge_en%` ➔ `ENABLED`
* `%onlyclans_ff_badge_es%` ➔ `HABILITADO`
* `%onlyclans_ff_status_en%` ➔ `✔ Enabled`
* `%onlyclans_ff_status_es%` ➔ `✔ Activado`
* `%onlyclans_flag_open_join_en%` ➔ `✔ Enabled`
* `%onlyclans_flag_open_join_es%` ➔ `✔ Activado`

---

## Color & Text Formatting Guide (MiniMessage & Legacy)

OnlyClans natively implements the official **[PaperMC Adventure MiniMessage](https://docs.papermc.io/adventure/minimessage/)** format engine alongside an intelligent legacy preprocessor. This gives you ultimate styling freedom: you can write modern MiniMessage tags, smooth multi-color gradients, rainbow patterns, standard Spigot hex (`&#RRGGBB`), or classic legacy Minecraft codes (`&` / `§`) across all plugin configurations (`config.yml`, `menus.yml`, `messages_en.yml`, `messages_es.yml`), clan chat, and clan tags. You can even combine them in the very same line!

---

### Gradients & Rainbows
Smooth transitions between two or more hexadecimal colors or standard Minecraft color names:

| Format | Syntax Example | Description |
|---|---|---|
| **Two-Color Gradient** | `<gradient:#2F6AFA:#00E5FF>OnlyClans</gradient>` | Smooth linear fade between two hex colors. |
| **Multi-Stop Gradient** | `<gradient:#FF007A:#7928CA:#00DFD8>Top Clan</gradient>` | Linear interpolation across three or more colors. |
| **Phased Gradient** | `<gradient:red:blue:0.5>Dynamic Title</gradient>` | Moves the color gradient phase forward (between -1.0 and 1.0). |
| **Standard Rainbow** | `<rainbow>OnlyClans</rainbow>` | Full spectrum rainbow cycle. |
| **Inverted / Phased Rainbow** | `<rainbow:!>Reversed</rainbow>` or `<rainbow:5>Offset</rainbow>` | Inverts color direction or shifts initial phase. |

---

### Hex & Named Colors
You can use any modern Adventure color tag, standard Spigot hex, or legacy color codes:

| Style | Syntax | Example |
|---|---|---|
| **MiniMessage Hex** | `<#RRGGBB>Text</#RRGGBB>` or `<#RRGGBB>Text` | `<#00E5FF>Cyan Clan Name` |
| **MiniMessage Tagged** | `<color:#RRGGBB>Text</color>` | `<color:#FFAA00>Gold Clan Tag</color>` |
| **MiniMessage Named** | `<color_name>Text</color_name>` | `<gold>Gold</gold>`, `<yellow>Yellow</yellow>`, `<green>Green</green>` |
| **Spigot Hex** | `&#RRGGBBText` | `&#00E5FFCyan Clan Name` |
| **Legacy Hex (Bungee)**| `&x&r&r&g&g&b&bText` | `&x&0&0&E&5&F&FCyan Clan Name` |
| **Classic Codes** | `&0`–`&9`, `&a`–`&f` | `&aGreen &6Gold &cRed` |

> [!TIP]
> Standard supported Adventure color names include: `black`, `dark_blue`, `dark_green`, `dark_aqua`, `dark_red`, `dark_purple`, `gold`, `gray`, `dark_gray`, `blue`, `green`, `aqua`, `red`, `light_purple`, `yellow`, and `white`.

---

### Text Styles & Decorations
Decorations can be applied using MiniMessage tags or standard legacy style codes:

| Decoration | MiniMessage Tags | Legacy Code |
|---|---|---|
| **Bold** | `<b>Text</b>` or `<bold>Text</bold>` | `&l` |
| **Italic** | `<i>Text</i>` or `<italic>Text</italic>` | `&o` |
| **Underlined** | `<u>Text</u>` or `<underlined>Text</underlined>` | `&n` |
| **Strikethrough**| `<s>Text</s>` or `<strikethrough>Text</strikethrough>` | `&m` |
| **Obfuscated** | `<obf>Text</obf>` or `<obfuscated>Text</obfuscated>` | `&k` |
| **Reset** | `<reset>` | `&r` |

> [!NOTE]
> **No-Italic GUI Protection**: In vanilla Minecraft, custom item display names and lore in inventory menus are forced into *italics* by default. OnlyClans automatically disables italics (`italic: false`) on all GUI items so your menu text remains crisp, straight, and clean. If you intentionally want italics on an item or a specific line of lore, simply add `<i>...</i>` or `&o`.

---

### Interactive Click & Hover Events (Chat)
You can embed clickable actions and hover tooltips directly inside chat messages and clan broadcasts:

* **Execute Command on Click**:
  ```yaml
  "<click:run_command:/clan accept><green>[ACCEPT]</green></click>"
  ```
* **Suggest Command into Player's Chat Bar**:
  ```yaml
  "<click:suggest_command:/clan invite ><yellow>[INVITE]</yellow></click>"
  ```
* **Open External URL**:
  ```yaml
  "<click:open_url:https://discord.gg/yourserver><aqua>[DISCORD]</aqua></click>"
  ```
* **Hover Tooltip**:
  ```yaml
  "<hover:show_text:'<gray>Click to view stats'><gold>%clan_name%</gold></hover>"
  ```
* **Combined Click + Hover Button**:
  ```yaml
  "<click:run_command:/clan accept><hover:show_text:'<green>Click to accept the invitation!'>&a&l[ACCEPT]</hover></click>"
  ```

---

### Pixel-Perfect Chat Centering (`<center>`)
OnlyClans features a built-in text centering system that works seamlessly with MiniMessage!

When you prefix a line with `<center>` (or wrap it in `<center>...</center>`), the plugin calculates the pixel width of every character according to Minecraft's vanilla font glyph metrics:
- All MiniMessage tags (`<gradient:...>`, `</gradient>`, `<hover:...>`, etc.), Hex codes, and legacy color codes are cleanly ignored during measurement.
- Bold formatting (`<b>` or `&l`) is automatically taken into account (each character is 1 pixel wider).
- The text is padded with spaces to center it in the default Minecraft chat window (320 pixels wide).

**Example in `messages_en.yml`:**
```yaml
clan_invite_received:
  - "&8━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
  - "<center><gradient:#2F6AFA:#00E5FF><b>CLAN INVITATION</b></gradient></center>"
  - "<center>&7You have been invited to join <gold>%clan%</gold> by <yellow>%inviter%</yellow></center>"
  - ""
  - "<center><click:run_command:/clan accept><hover:show_text:'&aClick to join %clan%'>&a&l[ACCEPT]</hover></click>     <click:run_command:/clan deny><hover:show_text:'&cClick to reject'>&c&l[DENY]</hover></click></center>"
  - "&8━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
```

---

### Dual-Engine & Hybrid Syntax
You do not need to rewrite your existing configuration files. OnlyClans transparently converts legacy `&` codes into the Adventure component tree while leaving native MiniMessage tags intact:

```yaml
# Mixing legacy codes, MiniMessage gradients, and placeholders in menus.yml:
title: "&8» <gradient:#2F6AFA:#00E5FF>Clan Members</gradient> &7(Page {PAGE})"

# Using gradients in item names:
name: "<gradient:#FFD700:#FFA500><b>★ Clan Leader:</b></gradient> &f%clan_leader%"
```

---

## Menus Guide

OnlyClans features a fully customizable, animated GUI system in `menus.yml` with support for MiniMessage, HEX gradients, custom sound effects, multi-slot decorative frames, custom Base64 heads, and dynamic member rendering.

### Modifying `menus.yml`
Each menu supports configurable sizes, opening sounds, background fillers, and item definitions.

#### Item Configuration Example:
```yaml
      'btn_clan_info':
        slot: 22                             # Single slot (0 to size-1)
        material: PLAYER_HEAD                # Item Material
        owner: "%player%"                    # Dynamic player skull (see Player Heads guide below)
        name: "<gradient:#00E5FF:#0070F3><b>🛡 Tu Clan:</b></gradient> <white>%clan_name%</white>" # Supports MiniMessage, HEX & Legacy
        lore:
          - "&#718096━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
          - "&#718096▪ &#A0AEC0Líder: <gold>%clan_leader%</gold>"
          - "&#718096▪ &#A0AEC0Tu Rango: %clan_role%"
          - "&#718096▪ &#A0AEC0Miembros: <green>%clan_members_online%</green>&#718096/<white>%clan_members%</white>"
          - "&#718096▪ &#A0AEC0PvP Aliado: %clan_ff%"
          - "&#718096━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
          - "<gradient:#00E5FF:#2F6AFA>▶ Clic para abrir el panel</gradient>"
        glow: true                           # Glowing enchantment effect
        sound: "UI_BUTTON_CLICK"             # Sound played when clicked
        sound_volume: 1.0                    # Sound volume (default 1.0)
        sound_pitch: 1.2                     # Sound pitch (default 1.0)
        action: "open:info"                  # Click action
```

### Configuring Player Heads (`PLAYER_HEAD`)
You have three flexible ways to display player heads:

1. **Viewing Player's Head (Automatic):**
   Simply set `material: PLAYER_HEAD` and omit the `base64` line. The plugin will automatically render the skin of the player viewing the menu:
   ```yaml
   material: PLAYER_HEAD
   ```
2. **Explicit Owner with Placeholders:**
   Use the `owner:` key with placeholders or usernames:
   * `owner: "%player%"` — The viewing player's skin.
   * `owner: "%clan_leader%"` — The clan leader's skin.
   * `owner: "Notch"` — Specific player name.
   ```yaml
   material: PLAYER_HEAD
   owner: "%player%"
   ```
3. **Custom Base64 Skull Texture:**
   Provide a custom Base64 skin value:
   ```yaml
   material: PLAYER_HEAD
   base64: "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2VkMWFiYTczZjYzOWY0YmM0MmJkNDgxOTZjNzE1MTk3YmUyNzEyYzNiOTYyYzk3ZWJmOWU5ZWQ4ZWZhMDI1In19fQ=="
   ```

---

### Sound Effects Configuration
You can assign sounds to both menu openings and individual item clicks:

* **Menu Opening Sound:**
  ```yaml
  main:
    title: "&#8727F5&lOnlyClans"
    size: 45
    open_sound: "BLOCK_CHEST_OPEN"
    open_sound_volume: 0.8
    open_sound_pitch: 1.1
  ```
* **Item Click Sound:**
  ```yaml
  sound: "UI_BUTTON_CLICK"
  sound_volume: 1.0
  sound_pitch: 1.5
  ```

---

### Multi-Slot Borders & Frames
You can define multiple slots for decorative glass panes or borders using a list or range string:
```yaml
      'border_purple':
        slots: [0, 1, 7, 8, 9, 17, 27, 35, 36, 37, 43, 44]
        material: PURPLE_STAINED_GLASS_PANE
        name: " "
```

---

### Dynamic Clan Members Roster
In the `members` menu, you can enable `dynamic_members: true`. The plugin will automatically populate the specified slots with real player heads of all clan members, sorted by role (Leader ➔ Mod ➔ Member) with online status badges:
```yaml
  members:
    title: "&#8727F5&l%clan_name% &#718096» &#FFD700&lMiembros"
    size: 45
    dynamic_members: true
    member_slots: [10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34]
```

---

### Available Menu Actions
When a player clicks an item in the GUI, you can assign it an action:

| Action | Description |
|--------|-------------|
| `open:<menu_id>` | Opens another menu (e.g., `open:create`, `open:info`, `open:members`, `open:top`, `open:settings`, `open:flags`). |
| `action:top_sort:<type>` | Changes top leaderboard sorting category (`kdr`, `kills`, `members`). |
| `action:cycle_top_sort` | Cycles to the next top leaderboard sorting category. |
| `action:top_page_prev` | Navigates to the previous page in the Top Clans GUI. |
| `action:top_page_next` | Navigates to the next page in the Top Clans GUI. |
| `action:create_clan` | Prompts the player to type their clan name in chat to create it. |
| `action:toggle_ff` | Toggles the clan's Friendly Fire (Leader & Moderator). |
| `action:toggle_flag:<flag>` | Toggles a specific clan flag (Leader & Moderator, or Admin for `spy_chat`). |
| `action:disband` | Opens the disband confirmation menu (Leader only). |
| `action:confirm_disband` | Permanently deletes the clan (Leader only). |
| `action:leave` | Leaves the player's current clan. |
| `command:<command>` | Executes a command as the player (e.g., `command:clan help`). |
| `console_command:<cmd>` | Executes a command as the console. |
| `close` | Closes the open inventory. |

---

### Menu General Settings
Each menu in `menus.yml` can be configured with:
* `title`: Display title of the GUI (supports MiniMessage tags, HEX gradients, legacy colors, and placeholders).
* `size`: Inventory size (9, 18, 27, 36, 45, 54).
* `open_sound`: Sound played when the menu opens (e.g. `BLOCK_CHEST_OPEN` or `none`).
* `filler`: Background glass pane material (e.g. `BLACK_STAINED_GLASS_PANE` or `none`).
* `update_interval`: Frequency in server ticks to auto-refresh the menu while open (e.g., `60` for every 3 seconds). Set to `0` to disable auto-refresh.

---

### Menu Placeholders
Inside `menus.yml`, you can use these dynamic placeholders in item names, lore, and titles:

| Placeholder | Description | Example Output |
|-------------|-------------|----------------|
| `%player%` | The viewing player's name | `ZkAleeJoo` |
| `%clan_name%` | The clan's name | `Vikings` |
| `%clan_tag%` | The 3-letter clan tag | `VIK` |
| `%clan_members%` | Total clan member count | `8` |
| `%clan_members_online%` | Online member count | `3` |
| `%clan_leader%` | Clan leader's username | `ZkAleeJoo` |
| `%clan_role%` | Formatted role badge with icon | `&#FFD700★ Líder` |
| `%clan_ff%` | Friendly fire status | `&#00FF88✔ Activado` / `&#FF3366✖ Desactivado` |
| `%clan_ff_badge%` | Friendly fire badge | `HABILITADO` / `DESHABILITADO` |
| `%clan_flag_<flag>%` | Formatted clan flag status | `&#00FF88✔ Activado` / `&#FF3366✖ Desactivado` |
| `%clan_flag_<flag>_badge%` | Formatted clan flag badge | `HABILITADO` / `DESHABILITADO` |
| `%clan_kills%` | Total clan kills in PvP | `142` |
| `%clan_deaths%` | Total clan deaths in PvP | `38` |
| `%clan_kdr%` | Clan kill/death ratio | `3.74` |
| `%clan_rival_kills%` | Kills against rival clan members | `95` |
| `%top_sort_mode%` | Current leaderboard sort mode | `KDR (Ratio)` |
| `%clan_created%` | Clan creation date | `29/08/2026` |

*(Note: Also supports any installed PlaceholderAPI placeholders automatically!)*

---

## Performance & Rate Limits Guide

When building rich GUI menus with custom player heads, server performance and external API rate limits are important to understand.

### Mojang Session Rate Limits (`HTTP 429`)
Paper and Spigot servers query Mojang's session servers (`sessionserver.mojang.com`) in the background to fetch player profile textures and skins for `PLAYER_HEAD` items. Mojang enforces a rate limit per IP address. If this limit is exceeded, Paper logs an asynchronous warning:
```text
[WARN]: Couldn't look up profile properties for <uuid>
com.mojang.authlib.exceptions.MinecraftClientHttpException: Status: 429
```
> [!NOTE]
> This is a non-fatal warning handled asynchronously by Paper. It will **not** freeze or crash your server; it simply means the skin texture couldn't be loaded from Mojang at that exact moment.

### How OnlyClans Optimizes Head Loading
1. **In-Memory Profile Caching**:
   OnlyClans automatically reuses the in-memory `PlayerProfile` for online players (`Player.getPlayerProfile()`). This completely eliminates redundant HTTP network calls to Mojang when viewing menus, online clan leaders, or online clan members.
2. **Optimized Menu Refresh Rate (`update_interval`)**:
   Menus with dynamic content (such as `main`, `info`, and `settings`) use `update_interval: 60` (3 seconds) by default instead of aggressive 1-second refreshes. You can adjust or set it to `0` to disable automatic ticking.
3. **Use Base64 Textures for Decorative Icons**:
   For generic or decorative heads (buttons, navigation, crowns, coins), always use `base64:` in `menus.yml`. Base64 heads load instantly from texture hashes without querying Mojang.
4. **Offline-Mode Servers (No-Premium)**:
   In `online-mode=false` servers, player UUIDs are MD5 hashes (v3) that do not exist in Mojang's official registry. Using a skin management plugin like **SkinsRestorer** ensures offline player skins are cached and resolved locally without causing Mojang API lookup errors.

---

## Database Configuration

OnlyClans supports both **SQLite** (local storage) and **MySQL** (remote database for networks).
By default, the plugin uses SQLite and stores data in `plugins/OnlyClans/clans.db`.

To switch to MySQL, edit your `config.yml`:
```yaml
database:
  type: "mysql" # Change from 'sqlite' to 'mysql'
  host: "localhost"
  port: 3306
  database: "minecraft"
  username: "root"
  password: "password"
```
After making changes, restart your server or run `/clan reload`.

---

## Future Updates
We are constantly working to improve OnlyClans! Here are some features planned for future updates:
- **Clan Vaults**: Shared inventory storage for clan members.
- **Clan Levels and EXP**: Gain experience as a clan to level up and unlock perks.
- **Economy Integration (Vault)**: Charge money for creating clans or leveling up.
- **Clan Alliances and Rivalries**: Form pacts or declare war against other clans.
- **Clan Wars**: Arena integrations for competitive clan vs clan battles.
- **Discord Webhooks**: Integrations to sync clan chat or log events directly to your Discord server.
