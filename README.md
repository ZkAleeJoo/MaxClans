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
OnlyClans integrates with **PlaceholderAPI**. You can use these placeholders in plugins like TAB, EssentialsX Chat, or any scoreboard plugin.

| Placeholder | Output |
|-------------|--------|
| `%onlyclans_name%` | The name of the player's clan. |
| `%onlyclans_tag%` | The 3-letter tag of the player's clan. |
| `%onlyclans_tag_formatted%` | The formatted tag with colors (e.g., `[TAG]`). |
| `%onlyclans_role%` | The player's role (`LEADER`, `MODERATOR`, `MEMBER`). |
| `%onlyclans_members_count%` | The number of members in the player's clan. |

---

## Menus Guide

OnlyClans features a fully customizable GUI system located in `menus.yml`. You can modify titles, sizes, items, and actions!

### How to use the GUI
Players can simply type `/clan` to open the Main Menu. From there, they can click on items to:
- **Create a clan**: Initiates a prompt where the user types their desired clan name in chat.
- **View Clan**: Check stats, members, and tags for your current clan.
- **Manage Settings**: Clan leaders can access settings to toggle Friendly Fire or disband the clan.
- **Manage Members**: View all members and run administrative commands.

### Modifying `menus.yml`
Every menu is constructed using individual items. Here is how an item is structured:

```yaml
      '1': # Item identifier
        slot: 11 # The inventory slot (0-26 for a size 27 menu)
        material: PLAYER_HEAD # Bukkit Material
        base64: "eyJ0..." # Optional custom skull texture
        name: "&#55FF55&lCreate Clan" # Item display name (Supports Hex Colors)
        lore:
          - "&7Start your own legacy"
        glow: true # Optional enchant glow effect
        action: "open:create" # The action to perform when clicked
```

### Available Menu Actions
When a player clicks an item, you can assign it an action:
- `open:<menu_id>` - Opens another menu (e.g., `open:info`).
- `action:create_clan` - Initiates the chat-based clan creation process.
- `action:toggle_ff` - Toggles the clan's Friendly Fire.
- `action:disband` - Opens the disband confirmation menu.
- `action:confirm_disband` - Permanently disbands the clan.
- `action:leave` - Leaves the current clan.
- `close` - Closes the inventory.

### Menu Placeholders
Inside `menus.yml`, you can use these special placeholders for item names and lore to make them dynamic:
- `{player}` - The viewing player's name.
- `{clan_name}` - The clan's name.
- `{clan_tag}` - The clan's tag.
- `{clan_members}` - Total member count of the clan.
- `{clan_ff}` - Friendly Fire status (ON/OFF).

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
