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
5. [Database Configuration](#-database-configuration)
6. [Future Updates](#-future-updates)

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

### Player Commands
Basic commands accessible to all players by default (`onlyclans.use`).

| Command | Description |
|---------|-------------|
| `/clan` | Opens the main GUI menu. |
| `/clan create <name>` | Creates a new clan (can also be done via GUI). |
| `/clan info` | Displays your clan's info in a menu. |
| `/clan invite <player>` | Invites a player to your clan. |
| `/clan accept` | Accepts a pending invitation. |
| `/clan deny` | Denies a pending invitation. |
| `/clan leave` | Leaves your current clan. |
| `/clan chat <msg>` or `/c <msg>` | Sends a message to the private clan chat. |

### Clan Management Commands
Commands available for Clan Leaders and Moderators.

| Command | Description | Role Required |
|---------|-------------|---------------|
| `/clan kick <player>` | Kicks a player from the clan. | Leader, Moderator |
| `/clan promote <player>` | Promotes a Member to Moderator. | Leader |
| `/clan demote <player>` | Demotes a Moderator to Member. | Leader |
| `/clan disband` | Permanently deletes the clan. | Leader |

### Admin Commands
Commands reserved for server administrators (`onlyclans.admin`).

| Command | Description | Permission |
|---------|-------------|------------|
| `/clan reload` | Reloads all configuration files and menus. | `onlyclans.admin` |

---

## Placeholders

OnlyClans integrates with **PlaceholderAPI** and provides internal placeholders for menus and chat.

### PlaceholderAPI Expansion (`%onlyclans_<placeholder>%`)
You can use these placeholders in plugins like TAB, EssentialsX Chat, DecentHolograms, or scoreboard plugins:

| Placeholder | Output |
|-------------|--------|
| `%onlyclans_name%` | The name of the player's clan. |
| `%onlyclans_tag%` | The 3-letter tag of the player's clan. |
| `%onlyclans_tag_formatted%` | Formatted colored tag (e.g., `&#8727F5[TAG]`). |
| `%onlyclans_role%` | Raw role name (`LEADER`, `MODERATOR`, `MEMBER`). |
| `%onlyclans_role_formatted%` | Role badge with icon (`★ Líder`, `◆ Moderador`, `● Miembro`). |
| `%onlyclans_members_count%` | Total member count in the player's clan. |
| `%onlyclans_members_online%` | Number of currently online clan members. |
| `%onlyclans_leader%` | Username of the clan leader. |
| `%onlyclans_ff%` | Friendly Fire status (`ON` / `OFF`). |
| `%onlyclans_ff_badge%` | Friendly Fire badge (`HABILITADO` / `DESHABILITADO`). |
| `%onlyclans_created%` | Clan founding date (`dd/MM/yyyy`). |

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
