package org.zkaleejoo.commands.subcommands.general;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
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
                "&#94A3B8&m----------------&r &#7DD3FC&lClan Homes &7({current}/{max}) &#94A3B8&m----------------")
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
        String btnTeleport = plugin.getMainConfigManager().getMessage("homes-list-teleport-button",
                "&#7DD3FC[Teleport]");
        String btnDelete = plugin.getMainConfigManager().getMessage("homes-list-delete-button",
                "&#FDA4AF[Delete]");

        for (ClanHome home : clan.getHomes().values()) {
            Component prefix = MessageUtils.toComponent("&#94A3B8▪ &#7DD3FC" + home.getName()
                    + " &#94A3B8(&f" + home.getWorldName()
                    + "&#94A3B8: &f" + home.getFormattedCoordinates()
                    + "&#94A3B8) ");

            Component tpBtn = MessageUtils.toComponent(btnTeleport)
                    .clickEvent(ClickEvent.runCommand("/clan home " + home.getName()))
                    .hoverEvent(HoverEvent.showText(MessageUtils.toComponent(hoverTeleport.replace("{home}", home.getName()))));

            Component line = prefix.append(tpBtn);

            if (isStaff) {
                Component delBtn = MessageUtils.toComponent(btnDelete)
                        .clickEvent(ClickEvent.runCommand("/clan delhome " + home.getName()))
                        .hoverEvent(HoverEvent.showText(MessageUtils.toComponent(hoverDelete.replace("{home}", home.getName()))));
                line = line.append(Component.space()).append(delBtn);
            }

            player.sendMessage(line);
        }

        SoundUtils.playSound(player, "UI_BUTTON_CLICK", 0.8f, 1.2f);
    }
}
