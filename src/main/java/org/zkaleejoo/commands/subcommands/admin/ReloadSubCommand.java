package org.zkaleejoo.commands.subcommands.admin;

import org.bukkit.command.CommandSender;
import org.zkaleejoo.MaxClans;
import org.zkaleejoo.commands.SubCommand;
import org.zkaleejoo.utils.MessageUtils;

public class ReloadSubCommand extends SubCommand {

    public ReloadSubCommand(MaxClans plugin) {
        super(plugin, "reload");
    }

    @Override
    public String getPermission() {
        return "maxclans.command.reload";
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return sender.hasPermission("maxclans.command.reload") || sender.hasPermission("maxclans.admin");
    }

    @Override
    public boolean isPlayerOnly() {
        return false;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        plugin.reloadPluginState();
        sender.sendMessage(MessageUtils.toComponent(
                plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager().getPluginReload()));
    }
}
