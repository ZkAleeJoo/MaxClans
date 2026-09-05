package org.zkaleejoo.commands.subcommands.management;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.zkaleejoo.OnlyClans;
import org.zkaleejoo.commands.SubCommand;
import org.zkaleejoo.utils.MessageUtils;

public class CreateSubCommand extends SubCommand {

    public CreateSubCommand(OnlyClans plugin) {
        super(plugin, "create");
    }

    @Override
    public String getPermission() {
        return "onlyclans.command.create";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        if (args.length < 2) {
            plugin.getMenuBuilder().openMenu(player, "create", null);
            return;
        }

        String name = args[1];
        if (name.length() < 3 || name.length() > 16) {
            player.sendMessage(MessageUtils.toComponent(
                    plugin.getMainConfigManager().getPrefix()
                            + plugin.getMainConfigManager().getMessage("invalid-length",
                                    "&cClan name must be between 3 and 16 characters.")));
            return;
        }

        if (!name.matches("^[a-zA-Z0-9_]+$")) {
            player.sendMessage(MessageUtils.toComponent(
                    plugin.getMainConfigManager().getPrefix()
                            + plugin.getMainConfigManager().getMessage("invalid-characters",
                                    "&cClan name can only contain letters, numbers, and underscores.")));
            return;
        }

        String tag = name.substring(0, 3).toUpperCase();
        plugin.getClanManager().createClan(player, name, tag);
    }
}
