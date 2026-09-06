package org.zkaleejoo.commands.subcommands.general;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.zkaleejoo.OnlyClans;
import org.zkaleejoo.commands.SubCommand;
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

        player.sendMessage(MessageUtils.toComponent("&8&m━━━━━━━━━━━━━&r &#2F6AFA&lPROGRESIÓN DE CLAN &8&m━━━━━━━━━━━━━"));
        player.sendMessage(MessageUtils.toComponent(" &#00E5FF✦ &7Clan: &f" + clan.getName() + " &7[&e" + clan.getTag() + "&7]"));
        player.sendMessage(MessageUtils.toComponent(" &#FFD700✦ &7Nivel actual: &#FFD700&lNivel " + level + (level >= maxLvl ? " &#00FF88(¡Nivel Máximo!)" : "")));
        if (level < maxLvl) {
            player.sendMessage(MessageUtils.toComponent(" &#00FF88✦ &7EXP: &#00FF88" + currentExp + " &7/ &#00FF88" + neededExp + " &7(" + String.format(Locale.US, "%.1f", percent) + "%)"));
            player.sendMessage(MessageUtils.toComponent("   &8[" + progressBar + "&8]"));
        } else {
            player.sendMessage(MessageUtils.toComponent(" &#00FF88✦ &7Progreso: &a¡Has alcanzado la cima del poder!"));
        }

        player.sendMessage(MessageUtils.toComponent(""));
        player.sendMessage(MessageUtils.toComponent("&#FFAA00&lVentajas activas de Nivel " + level + ":"));
        player.sendMessage(MessageUtils.toComponent(" &7• Miembros máximos: &#00FF88" + currentConfig.getMaxMembers()));
        player.sendMessage(MessageUtils.toComponent(" &7• Aliados permitidos: &#00FF88" + currentConfig.getMaxAllies()));
        player.sendMessage(MessageUtils.toComponent(" &7• Homes de Clan: &#00FF88" + currentConfig.getMaxHomes()));
        player.sendMessage(MessageUtils.toComponent(" &7• Banco del Clan: " + (currentConfig.hasBankAccess() ? "&#00FF88✔ Desbloqueado" : "&#FF3366✖ Bloqueado")));
        player.sendMessage(MessageUtils.toComponent(" &7• Baúl compartido (/clan chest): " + (currentConfig.hasChestAccess() ? "&#00FF88✔ Desbloqueado (" + currentConfig.getChestRows() + " filas)" : "&#FF3366✖ Bloqueado")));
        player.sendMessage(MessageUtils.toComponent(" &7• Habilidad pasiva en base: " + (currentConfig.hasBaseEffect() ? "&#00FF88✔ " + currentConfig.getBaseEffectType() + " (Radio " + currentConfig.getBaseEffectRadius() + "m)" : "&#FF3366✖ Bloqueado")));

        if (nextConfig != null) {
            player.sendMessage(MessageUtils.toComponent(""));
            player.sendMessage(MessageUtils.toComponent("&#00E5FF&lPróximo Nivel " + (level + 1) + " (Requiere " + neededExp + " EXP):"));
            if (nextConfig.getMaxMembers() > currentConfig.getMaxMembers()) {
                player.sendMessage(MessageUtils.toComponent(" &a+ Ampliación a " + nextConfig.getMaxMembers() + " miembros"));
            }
            if (nextConfig.getMaxAllies() > currentConfig.getMaxAllies()) {
                player.sendMessage(MessageUtils.toComponent(" &a+ Ampliación a " + nextConfig.getMaxAllies() + " aliados"));
            }
            if (nextConfig.getMaxHomes() > currentConfig.getMaxHomes()) {
                player.sendMessage(MessageUtils.toComponent(" &a+ Ampliación a " + nextConfig.getMaxHomes() + " homes"));
            }
            if (!currentConfig.hasBankAccess() && nextConfig.hasBankAccess()) {
                player.sendMessage(MessageUtils.toComponent(" &a+ Desbloqueo de Banco del Clan"));
            }
            if (!currentConfig.hasChestAccess() && nextConfig.hasChestAccess()) {
                player.sendMessage(MessageUtils.toComponent(" &a+ Desbloqueo de Baúl Compartido"));
            }
            if (!currentConfig.hasBaseEffect() && nextConfig.hasBaseEffect()) {
                player.sendMessage(MessageUtils.toComponent(" &a+ Desbloqueo de Habilidad Pasiva en Base (" + nextConfig.getBaseEffectType() + ")"));
            }
        }
        player.sendMessage(MessageUtils.toComponent("&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
        SoundUtils.playSound(player, "BLOCK_ENCHANTMENT_TABLE_USE", 0.9f, 1.2f);
    }
}
