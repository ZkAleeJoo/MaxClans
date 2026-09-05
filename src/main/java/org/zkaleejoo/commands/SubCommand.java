package org.zkaleejoo.commands;

import org.bukkit.command.CommandSender;
import org.zkaleejoo.OnlyClans;
import org.zkaleejoo.utils.MessageUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class SubCommand {

    protected final OnlyClans plugin;
    private final String name;
    private final List<String> aliases;

    public SubCommand(OnlyClans plugin, String name, String... aliases) {
        this.plugin = plugin;
        this.name = name;
        this.aliases = aliases != null ? List.of(aliases) : Collections.emptyList();
    }

    public String getName() {
        return name;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public boolean matches(String sub) {
        if (name.equalsIgnoreCase(sub)) {
            return true;
        }
        for (String alias : aliases) {
            if (alias.equalsIgnoreCase(sub)) {
                return true;
            }
        }
        return false;
    }

    public abstract String getPermission();

    public boolean hasPermission(CommandSender sender) {
        String perm = getPermission();
        return perm == null || sender.hasPermission(perm);
    }

    public boolean isPlayerOnly() {
        return true;
    }

    public abstract void execute(CommandSender sender, String[] args);

    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }

    /**
     * Subcommands or aliases to suggest in the first argument completion (e.g.
     * /clan <subcommand>).
     */
    public List<String> getTabSuggestions(CommandSender sender) {
        if (!hasPermission(sender)) {
            return Collections.emptyList();
        }
        return List.of(name);
    }

    protected void sendNoPermission(CommandSender sender) {
        sender.sendMessage(MessageUtils.toComponent(
                plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager().getNoPermission()));
    }

    protected void sendConsoleNotAllowed(CommandSender sender) {
        sender.sendMessage(MessageUtils.toComponent(
                plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager().getMsgConsole()));
    }

    protected void sendPrefixMessage(CommandSender sender, String message) {
        sender.sendMessage(MessageUtils.toComponent(plugin.getMainConfigManager().getPrefix() + message));
    }

    protected List<String> filterCompletions(List<String> completions, String input) {
        List<String> filtered = new ArrayList<>();
        for (String completion : completions) {
            if (completion.toLowerCase().startsWith(input.toLowerCase())) {
                filtered.add(completion);
            }
        }
        return filtered;
    }
}
