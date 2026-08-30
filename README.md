<div align="center">
  <h1>OnlyClans - Official Wiki</h1>
  <p>
    <img src="https://img.shields.io/badge/version-1.0.0-blue" alt="Version">
    <img src="https://img.shields.io/badge/Java-21+-red" alt="Java">
    <img src="https://img.shields.io/badge/Paper--Folia-1.21--26.1.2+-green" alt="Paper-Folia">
    <img src="https://img.shields.io/badge/Languages-EN_|_ES-blue" alt="Languages">
  </p>
</div>

Welcome to the **OnlyClans** Official Wiki! Here you will find all the information you need to configure and use the plugin on your Minecraft server.

## Table of Contents
1. [Features](#-features)
2. [Commands & Permissions](#-commands--permissions)
3. [Placeholders](#-placeholders)
4. [Menus Guide](#-menus-guide)
5. [Performance & Rate Limits Guide](#-performance--rate-limits-guide)
6. [Database Configuration](#-database-configuration)
7. [Future Updates](#-future-updates)

---

## Features
OnlyClans is designed to be a lightweight, highly customizable, and easy-to-use clan system for modern Minecraft servers.
- **Full GUI Support**: Create, manage, and view your clan through interactive inventory menus.
- **Clan Roles**: Leader, Moderator, and Member roles with different permissions (invite, kick, promote, demote).
- **Friendly Fire Toggle**: Clan leaders can toggle PvP between clan members.
- **Clan Chat**: Private communication channel for clan members (`/c <message>`).
- **PlaceholderAPI Integration**: Display clan stats and info anywhere (chat, scoreboard, tablist).
- **Multiple Databases**: Supports both SQLite (local) and MySQL (remote) storage.
- **Multilingual**: Built-in support for English and Spanish, fully customizable in `messages_en.yml` and `messages_es.yml`.
- **Folia Support**: Fully compatible with Folia and Paper servers.

---

## Commands & Permissions

> [!NOTE]
> All `/clan` commands can also be executed using the aliases `/olc` or `/onlyclans`.

### Player Commands
Basic commands accessible to all players by default (`onlyclans.use`).

| Command | Aliases | Description |
|---------|---------|-------------|
| `/clan` | `/olc`, `/onlyclans` | Opens the main GUI menu. |
| `/clan create <name>` | `/olc create <name>` | Creates a new clan (can also be done via GUI). |
| `/clan info` | `/olc info` | Displays your clan's info in a menu. |
| `/clan invite <player>` | `/olc invite <player>` | Invites a player to your clan. |
| `/clan accept` | `/olc accept` | Accepts a pending invitation. |
| `/clan deny` | `/olc deny` | Denies a pending invitation. |
| `/clan leave` | `/olc leave` | Leaves your current clan. |
| `/clan chat <msg>` | `/c <msg>`, `/cc <msg>` | Sends a message to the private clan chat. |

### Clan Management Commands
Commands available for Clan Leaders and Moderators.

| Command | Aliases | Description | Role Required |
|---------|---------|-------------|---------------|
| `/clan kick <player>` | `/olc kick <player>` | Kicks a player from the clan. | Leader, Moderator |
| `/clan promote <player>` | `/olc promote <player>` | Promotes a Member to Moderator. | Leader |
| `/clan demote <player>` | `/olc demote <player>` | Demotes a Moderator to Member. | Leader |
| `/clan disband` | `/olc disband` | Permanently deletes the clan. | Leader |

### Admin Commands
Commands reserved for server administrators (`onlyclans.admin`).

| Command | Aliases | Description | Permission |
|---------|---------|-------------|------------|
| `/clan reload` | `/olc reload`, `/onlyclans reload` | Reloads all configuration files and menus without restarting. | `onlyclans.admin` |

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
| `%onlyclans_created%` | Clan founding date | `29/08/2026` | `29/08/2026` |

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

---

## Menus Guide

OnlyClans features a fully customizable, animated GUI system in `menus.yml` with support for HEX gradients, custom sound effects, multi-slot decorative frames, custom Base64 heads, and dynamic member rendering.

### Modifying `menus.yml`
Each menu supports configurable sizes, opening sounds, background fillers, and item definitions.

#### Item Configuration Example:
```yaml
      'btn_clan_info':
        slot: 22                             # Single slot (0 to size-1)
        material: PLAYER_HEAD                # Item Material
        owner: "%player%"                    # Dynamic player skull (see Player Heads guide below)
        name: "&#00E5FF&l🛡 Tu Clan: &#FFFFFF%clan_name%" # Item display name (Supports HEX)
        lore:
          - "&#718096━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
          - "&#718096▪ &#A0AEC0Líder: &#FFD700%clan_leader%"
          - "&#718096▪ &#A0AEC0Tu Rango: %clan_role%"
          - "&#718096▪ &#A0AEC0Miembros: &#00FF88%clan_members_online%&#718096/&#FFFFFF%clan_members%"
          - "&#718096▪ &#A0AEC0PvP Aliado: %clan_ff%"
          - "&#718096━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
          - "&#00E5FF▶ Clic para abrir el panel"
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
| `open:<menu_id>` | Opens another menu (e.g., `open:create`, `open:info`, `open:members`, `open:settings`). |
| `action:create_clan` | Prompts the player to type their clan name in chat to create it. |
| `action:toggle_ff` | Toggles the clan's Friendly Fire (Leader only). |
| `action:disband` | Opens the disband confirmation menu (Leader only). |
| `action:confirm_disband` | Permanently deletes the clan (Leader only). |
| `action:leave` | Leaves the player's current clan. |
| `command:<command>` | Executes a command as the player (e.g., `command:clan help`). |
| `console_command:<cmd>` | Executes a command as the console. |
| `close` | Closes the open inventory. |

---

### Menu General Settings
Each menu in `menus.yml` can be configured with:
* `title`: Display title of the GUI (supports HEX colors and placeholders).
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
