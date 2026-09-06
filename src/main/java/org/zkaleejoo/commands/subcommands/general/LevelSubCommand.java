package org.zkaleejoo.commands.subcommands.general;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.zkaleejoo.OnlyClans;
import org.zkaleejoo.commands.SubCommand;
import org.zkaleejoo.config.MainConfigManager;
import org.zkaleejoo.managers.ClanLevelManager;
import org.zkaleejoo.models.Clan;
import org.zkaleejoo.models.ClanLevelConfig;
import org.zkaleejoo.utils.MessageUtils;
import org.zkaleejoo.utils.SoundUtils;

import java.util.Locale;

public class LevelSubCommand extends SubCommand {

    public LevelSubCommand(OnlyClans plugin) {
        super(plugin, "level", "perks", "nivel", "niveles", "exp");
    }

    @Override
    public String getPermission() {
        return "onlyclans.command.level";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        Clan clan = plugin.getClanManager().getClanByPlayer(player.getUniqueId());
        if (clan == null) {
            player.sendMessage(MessageUtils.toComponent(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                            .getMessage("not-in-clan", "&cYou are not in a clan.")));
            return;
        }

        ClanLevelManager lm = plugin.getClanLevelManager();
        if (lm == null) return;

        int level = clan.getLevel();
        int maxLvl = lm.getMaxLevel();
        int currentExp = clan.getExp();
        int neededExp = lm.getExpForNextLevel(clan);
        double percent = lm.getExpPercentage(clan);
        String progressBar = lm.getExpProgressBar(clan, 20);

        ClanLevelConfig currentConfig = lm.getLevelConfig(level);
        ClanLevelConfig nextConfig = (level < maxLvl) ? lm.getLevelConfig(level + 1) : null;

        MainConfigManager cm = plugin.getMainConfigManager();

        String header = cm.getMessage("level-progression-header",
                "&8&m━━━━━━━━━━━━━&r &#2F6AFA&lCLAN PROGRESSION &8&m━━━━━━━━━━━━━");
        player.sendMessage(MessageUtils.toComponent(header));

        String clanLine = cm.getMessage("level-progression-clan",
                " &#00E5FF✦ &7Clan: &f{clan} &7[&e{tag}&7]")
                .replace("{clan}", clan.getName())
                .replace("{tag}", clan.getTag());
        player.sendMessage(MessageUtils.toComponent(clanLine));

        String maxTag = (level >= maxLvl) ? cm.getMessage("level-progression-max-tag", " &#00FF88(Max Level!)") : "";
        String currentLine = cm.getMessage("level-progression-current",
                " &#FFD700✦ &7Current Level: &#FFD700&lLevel {level}{max_tag}")
                .replace("{level}", String.valueOf(level))
                .replace("{max_tag}", maxTag);
        player.sendMessage(MessageUtils.toComponent(currentLine));

        if (level < maxLvl) {
            String expLine = cm.getMessage("level-progression-exp",
                    " &#00FF88✦ &7EXP: &#00FF88{current} &7/ &#00FF88{needed} &7({percent}%)")
                    .replace("{current}", String.valueOf(currentExp))
                    .replace("{needed}", String.valueOf(neededExp))
                    .replace("{percent}", String.format(Locale.US, "%.1f", percent));
            player.sendMessage(MessageUtils.toComponent(expLine));
            player.sendMessage(MessageUtils.toComponent("   &8[" + progressBar + "&8]"));
        } else {
            String maxReached = cm.getMessage("level-progression-max-reached",
                    " &#00FF88✦ &7Progress: &aYou have reached the peak of power!");
            player.sendMessage(MessageUtils.toComponent(maxReached));
        }

        player.sendMessage(MessageUtils.toComponent(""));

        String perksTitle = cm.getMessage("level-progression-active-perks",
                "&#FFAA00&lActive Perks for Level {level}:")
                .replace("{level}", String.valueOf(level));
        player.sendMessage(MessageUtils.toComponent(perksTitle));

        String membersLine = cm.getMessage("level-perk-members", " &7• Max members: &#00FF88{count}")
                .replace("{count}", String.valueOf(currentConfig.getMaxMembers()));
        player.sendMessage(MessageUtils.toComponent(membersLine));

        String alliesLine = cm.getMessage("level-perk-allies", " &7• Allowed allies: &#00FF88{count}")
                .replace("{count}", String.valueOf(currentConfig.getMaxAllies()));
        player.sendMessage(MessageUtils.toComponent(alliesLine));

        String homesLine = cm.getMessage("level-perk-homes", " &7• Clan homes: &#00FF88{count}")
                .replace("{count}", String.valueOf(currentConfig.getMaxHomes()));
        player.sendMessage(MessageUtils.toComponent(homesLine));

        String unlockedStatus = cm.getMessage("level-status-unlocked", "&#00FF88✔ Unlocked");
        String lockedStatus = cm.getMessage("level-status-locked", "&#FF3366✖ Locked");

        String bankStatus = currentConfig.hasBankAccess() ? unlockedStatus : lockedStatus;
        String bankLine = cm.getMessage("level-perk-bank", " &7• Clan Bank: {status}")
                .replace("{status}", bankStatus);
        player.sendMessage(MessageUtils.toComponent(bankLine));

        String chestStatus = currentConfig.hasChestAccess()
                ? cm.getMessage("level-status-unlocked-rows", "&#00FF88✔ Unlocked ({rows} rows)")
                        .replace("{rows}", String.valueOf(currentConfig.getChestRows()))
                : lockedStatus;
        String chestLine = cm.getMessage("level-perk-chest", " &7• Shared Chest (/clan chest): {status}")
                .replace("{status}", chestStatus);
        player.sendMessage(MessageUtils.toComponent(chestLine));

        String baseEffectStatus = currentConfig.hasBaseEffect()
                ? cm.getMessage("level-status-base-effect", "&#00FF88✔ {effect} (Radius {radius}m)")
                        .replace("{effect}", currentConfig.getBaseEffectType())
                        .replace("{radius}", String.valueOf(currentConfig.getBaseEffectRadius()))
                : lockedStatus;
        String baseEffectLine = cm.getMessage("level-perk-base-effect", " &7• Passive base effect: {status}")
                .replace("{status}", baseEffectStatus);
        player.sendMessage(MessageUtils.toComponent(baseEffectLine));

        if (nextConfig != null) {
            player.sendMessage(MessageUtils.toComponent(""));
            String nextTitle = cm.getMessage("level-progression-next-title",
                    "&#00E5FF&lNext Level {level} (Requires {needed} EXP):")
                    .replace("{level}", String.valueOf(level + 1))
                    .replace("{needed}", String.valueOf(neededExp));
            player.sendMessage(MessageUtils.toComponent(nextTitle));

            if (nextConfig.getMaxMembers() > currentConfig.getMaxMembers()) {
                String upg = cm.getMessage("level-upgrade-members", " &a+ Increase to {count} members")
                        .replace("{count}", String.valueOf(nextConfig.getMaxMembers()));
                player.sendMessage(MessageUtils.toComponent(upg));
            }
            if (nextConfig.getMaxAllies() > currentConfig.getMaxAllies()) {
                String upg = cm.getMessage("level-upgrade-allies", " &a+ Increase to {count} allies")
                        .replace("{count}", String.valueOf(nextConfig.getMaxAllies()));
                player.sendMessage(MessageUtils.toComponent(upg));
            }
            if (nextConfig.getMaxHomes() > currentConfig.getMaxHomes()) {
                String upg = cm.getMessage("level-upgrade-homes", " &a+ Increase to {count} homes")
                        .replace("{count}", String.valueOf(nextConfig.getMaxHomes()));
                player.sendMessage(MessageUtils.toComponent(upg));
            }
            if (!currentConfig.hasBankAccess() && nextConfig.hasBankAccess()) {
                player.sendMessage(MessageUtils.toComponent(
                        cm.getMessage("level-unlock-bank", " &a+ Unlock Clan Bank")));
            }
            if (!currentConfig.hasChestAccess() && nextConfig.hasChestAccess()) {
                player.sendMessage(MessageUtils.toComponent(
                        cm.getMessage("level-unlock-chest", " &a+ Unlock Shared Clan Chest")));
            }
            if (!currentConfig.hasBaseEffect() && nextConfig.hasBaseEffect()) {
                player.sendMessage(MessageUtils.toComponent(
                        cm.getMessage("level-unlock-base-effect", " &a+ Unlock Passive Base Effect ({effect})")
                                .replace("{effect}", nextConfig.getBaseEffectType())));
            }
        }

        String footer = cm.getMessage("level-progression-footer",
                "&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        player.sendMessage(MessageUtils.toComponent(footer));
        SoundUtils.playSound(player, "BLOCK_ENCHANTMENT_TABLE_USE", 0.9f, 1.2f);
    }
}
