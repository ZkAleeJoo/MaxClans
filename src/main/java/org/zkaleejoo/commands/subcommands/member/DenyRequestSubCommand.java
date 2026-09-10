package org.zkaleejoo.commands.subcommands.member;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.zkaleejoo.MaxClans;
import org.zkaleejoo.commands.SubCommand;
import org.zkaleejoo.models.Clan;
import org.zkaleejoo.utils.MessageUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DenyRequestSubCommand extends SubCommand {

    public DenyRequestSubCommand(MaxClans plugin) {
        super(plugin, "denyrequest", "denyjoin");
    }

    @Override
    public String getPermission() {
        return "maxclans.command.denyrequest";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        if (args.length < 2) {
            player.sendMessage(MessageUtils.toComponent(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                            .getMessage("usage-denyrequest", "&cUsage: /clan denyrequest <player>")));
            return;
        }

        plugin.getClanManager().denyJoinRequest(player, args[1]);
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 2) {
            List<String> completions = new ArrayList<>();
            if (sender instanceof Player p) {
                Clan clan = plugin.getClanManager().getClanByPlayer(p.getUniqueId());
                if (clan != null) {
                    for (UUID reqUuid : clan.getJoinRequests()) {
                        Player targetP = Bukkit.getPlayer(reqUuid);
                        if (targetP != null) {
                            completions.add(targetP.getName());
                        } else {
                            OfflinePlayer op = Bukkit.getOfflinePlayer(reqUuid);
                            if (op.getName() != null) {
                                completions.add(op.getName());
                            }
                        }
                    }
                }
            }
            return filterCompletions(completions, args[1]);
        }
        return super.tabComplete(sender, args);
    }
}
