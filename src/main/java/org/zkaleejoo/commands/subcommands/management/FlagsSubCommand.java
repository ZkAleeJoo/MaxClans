package org.zkaleejoo.commands.subcommands.management;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.zkaleejoo.MaxClans;
import org.zkaleejoo.commands.SubCommand;
import org.zkaleejoo.models.Clan;
import org.zkaleejoo.models.ClanPlayer;
import org.zkaleejoo.models.ClanRole;
import org.zkaleejoo.utils.MessageUtils;

import java.util.ArrayList;
import java.util.List;

public class FlagsSubCommand extends SubCommand {

    public FlagsSubCommand(MaxClans plugin) {
        super(plugin, "flags", "settings");
    }

    @Override
    public String getPermission() {
        return "maxclans.command.flags";
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return sender.hasPermission("maxclans.command.flags") || sender.hasPermission("maxclans.command.settings");
    }

    @Override
    public List<String> getTabSuggestions(CommandSender sender) {
        List<String> list = new ArrayList<>();
        if (sender.hasPermission("maxclans.command.flags")) {
            list.add("flags");
        }
        if (sender.hasPermission("maxclans.command.settings")) {
            list.add("settings");
        }
        return list;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        Clan clan = plugin.getClanManager().getClanByPlayer(player.getUniqueId());
        if (clan != null) {
            ClanPlayer cp = clan.getMember(player.getUniqueId());
            boolean canAccess = player.hasPermission("maxclans.admin")
                    || (cp != null && cp.hasRoleAtLeast(ClanRole.MODERATOR));
            if (canAccess) {
                String sub = args.length > 0 ? args[0].toLowerCase() : "flags";
                plugin.getMenuBuilder().openMenu(player, sub.equals("flags") ? "flags" : "settings", clan);
            } else {
                player.sendMessage(MessageUtils.toComponent(
                        plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                                .getMessage("no-flag-permission",
                                        "&cOnly clan leaders and moderators can access clan settings.")));
            }
        } else {
            player.sendMessage(MessageUtils.toComponent(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                            .getMessage("not-in-clan", "&cYou are not in a clan.")));
        }
    }
}
