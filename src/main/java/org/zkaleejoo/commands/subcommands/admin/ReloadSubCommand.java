package org.zkaleejoo.commands.subcommands.admin;

import org.bukkit.command.CommandSender;
import org.zkaleejoo.OnlyClans;
import org.zkaleejoo.commands.SubCommand;
import org.zkaleejoo.utils.MessageUtils;

public class ReloadSubCommand extends SubCommand {

    public ReloadSubCommand(OnlyClans plugin) {
        super(plugin, "reload");
    }

    @Override
    public String getPermission() {
        return "onlyclans.command.reload";
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return sender.hasPermission("onlyclans.command.reload") || sender.hasPermission("onlyclans.admin");
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
