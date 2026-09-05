package org.zkaleejoo.commands.subcommands.general;

import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.zkaleejoo.OnlyClans;
import org.zkaleejoo.commands.SubCommand;
import org.zkaleejoo.models.Clan;
import org.zkaleejoo.models.ClanHome;
import org.zkaleejoo.models.ClanPlayer;
import org.zkaleejoo.models.ClanRole;
import org.zkaleejoo.utils.MessageUtils;
import org.zkaleejoo.utils.SoundUtils;

import java.util.Collections;
import java.util.List;

public class HomesSubCommand extends SubCommand {

    public HomesSubCommand(OnlyClans plugin) {
        super(plugin, "homes", "listhomes", "homelist");
    }

    @Override
    public String getPermission() {
        return "onlyclans.command.home";
    }

    @Override
    public List<String> getTabSuggestions(CommandSender sender) {
        if (!hasPermission(sender)) {
            return Collections.emptyList();
        }
        return List.of("homes");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        Clan clan = plugin.getClanManager().getClanByPlayer(player.getUniqueId());

        if (clan == null) {
            player.sendMessage(MessageUtils.toComponent(
                    plugin.getMainConfigManager().getPrefix() +
                            plugin.getMainConfigManager().getMessage("not-in-clan", "&cYou are not in a clan.")));
            SoundUtils.playSound(player, "ENTITY_VILLAGER_NO", 1.0f, 0.9f);
            return;
        }

        int maxHomes = plugin.getMainConfigManager().getMaxHomesForLevel(clan.getLevel());
        int currentHomes = clan.getHomeCount();

        String header = plugin.getMainConfigManager().getMessage("homes-list-header",
                "&8&m----------------&r &#00E5FF&lClan Homes &7({current}/{max}) &8&m----------------")
                .replace("{current}", String.valueOf(currentHomes))
                .replace("{max}", String.valueOf(maxHomes));

        player.sendMessage(MessageUtils.toComponent(header));

        if (currentHomes == 0) {
            String emptyMsg = plugin.getMainConfigManager().getMessage("homes-list-empty",
                    "&7No homes set. Use &e/clan sethome [name] &7to set your first base!");
            player.sendMessage(MessageUtils.toComponent(emptyMsg));
            return;
        }

        ClanPlayer cp = clan.getMember(player.getUniqueId());
        boolean isStaff = cp != null && cp.hasRoleAtLeast(ClanRole.MODERATOR);

        String hoverTeleport = plugin.getMainConfigManager().getMessage("homes-list-teleport-hover",
                "&aClick to teleport to &f{home}");
        String hoverDelete = plugin.getMainConfigManager().getMessage("homes-list-delete-hover",
                "&cClick to delete &f{home}");

        for (ClanHome home : clan.getHomes().values()) {
            StringBuilder line = new StringBuilder();
            line.append("&#718096▪ &#00FF88").append(home.getName())
                    .append(" &#718096(&f").append(home.getWorldName())
                    .append("&#718096: &f").append(home.getFormattedCoordinates())
                    .append("&#718096) ");

            line.append("<click:run_command:'/clan home ").append(home.getName())
                    .append("'><hover:show_text:'").append(hoverTeleport.replace("{home}", home.getName()))
                    .append("'>&#00E5FF[Teleport]</hover></click>");

            if (isStaff) {
                line.append(" <click:run_command:'/clan delhome ").append(home.getName())
                        .append("'><hover:show_text:'").append(hoverDelete.replace("{home}", home.getName()))
                        .append("'>&#FF3366[Delete]</hover></click>");
            }

            Component comp = MessageUtils.toComponent(line.toString());
            player.sendMessage(comp);
        }

        SoundUtils.playSound(player, "UI_BUTTON_CLICK", 0.8f, 1.2f);
    }
}
