package org.zkaleejoo.commands.subcommands.management;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.zkaleejoo.OnlyClans;
import org.zkaleejoo.commands.SubCommand;
import org.zkaleejoo.models.Clan;
import org.zkaleejoo.models.ClanFlag;
import org.zkaleejoo.utils.MessageUtils;

import java.util.ArrayList;
import java.util.List;

public class FlagSubCommand extends SubCommand {

    public FlagSubCommand(OnlyClans plugin) {
        super(plugin, "flag");
    }

    @Override
    public String getPermission() {
        return "onlyclans.command.flags";
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

        if (args.length < 2) {
            player.sendMessage(MessageUtils.toComponent(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                            .getMessage("usage-flag", "&cUsage: /clan flag <flag> [on|off|toggle]")));
            return;
        }

        ClanFlag flag = ClanFlag.fromKey(args[1]);
        if (flag == null) {
            player.sendMessage(MessageUtils.toComponent(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                            .getMessage("invalid-flag",
                                    "&cInvalid flag name. Available: friendly_fire, open_join, ally_damage, member_invites, visible_in_list, public_home, spy_chat")));
            return;
        }

        if (args.length >= 3) {
            String state = args[2].toLowerCase();
            boolean newVal;
            if (state.equals("on") || state.equals("true") || state.equals("enable") || state.equals("1")) {
                newVal = true;
            } else if (state.equals("off") || state.equals("false") || state.equals("disable")
                    || state.equals("0")) {
                newVal = false;
            } else {
                newVal = !clan.getFlag(flag);
            }
            plugin.getClanManager().setFlag(clan, flag, newVal, player);
        } else {
            plugin.getClanManager().toggleFlag(clan, flag, player);
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 2) {
            List<String> completions = new ArrayList<>();
            for (ClanFlag f : ClanFlag.values()) {
                completions.add(f.getKey());
            }
            return filterCompletions(completions, args[1]);
        }
        if (args.length == 3) {
            List<String> completions = new ArrayList<>();
            completions.add("on");
            completions.add("off");
            completions.add("toggle");
            return filterCompletions(completions, args[2]);
        }
        return super.tabComplete(sender, args);
    }
}
