package org.zkaleejoo.commands.subcommands.general;

import org.bukkit.command.CommandSender;
import org.zkaleejoo.OnlyClans;
import org.zkaleejoo.commands.SubCommand;
import org.zkaleejoo.utils.MessageUtils;

import java.util.List;

public class HelpSubCommand extends SubCommand {

    public HelpSubCommand(OnlyClans plugin) {
        super(plugin, "help");
    }

    @Override
    public String getPermission() {
        return "onlyclans.command.help";
    }

    @Override
    public boolean isPlayerOnly() {
        return false;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        String titleTemplate = plugin.getMainConfigManager().getHelpTitle();
        sender.sendMessage(MessageUtils.toComponent(plugin.getMainConfigManager().getPrefix()
                + titleTemplate.replace("{version}", plugin.getPluginMeta().getVersion())));

        List<String> helpLines = plugin.getMainConfigManager().getHelpLines();
        for (String line : helpLines) {
            sender.sendMessage(MessageUtils.toComponent(line));
        }
    }
}
