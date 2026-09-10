package org.zkaleejoo.commands.subcommands.management;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.zkaleejoo.MaxClans;
import org.zkaleejoo.commands.SubCommand;
import org.zkaleejoo.models.Clan;
import org.zkaleejoo.models.ClanHome;
import org.zkaleejoo.models.ClanPlayer;
import org.zkaleejoo.models.ClanRole;
import org.zkaleejoo.utils.MessageUtils;
import org.zkaleejoo.utils.SoundUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

public class SetHomeSubCommand extends SubCommand {

    private static final Pattern HOME_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]{1,16}$");

    public SetHomeSubCommand(MaxClans plugin) {
        super(plugin, "sethome", "createsetpoint");
    }

    @Override
    public String getPermission() {
        return "maxclans.command.sethome";
    }

    @Override
    public List<String> getTabSuggestions(CommandSender sender) {
        if (!hasPermission(sender)) {
            return Collections.emptyList();
        }
        return List.of("sethome");
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

        ClanPlayer cp = clan.getMember(player.getUniqueId());
        if (cp == null || !cp.hasRoleAtLeast(ClanRole.MODERATOR)) {
            player.sendMessage(MessageUtils.toComponent(
                    plugin.getMainConfigManager().getPrefix() +
                            plugin.getMainConfigManager().getMessage("home-no-permission",
                                    "&cOnly clan leaders and moderators can set clan homes.")));
            SoundUtils.playSound(player, "ENTITY_VILLAGER_NO", 1.0f, 0.9f);
            return;
        }

        String homeName = (args.length >= 2) ? args[1].trim() : "default";

        if (!HOME_NAME_PATTERN.matcher(homeName).matches()) {
            player.sendMessage(MessageUtils.toComponent(
                    plugin.getMainConfigManager().getPrefix() +
                            plugin.getMainConfigManager().getMessage("home-invalid-name",
                                    "&cClan home name can only contain letters, numbers, hyphens and underscores (1-16 chars).")));
            SoundUtils.playSound(player, "ENTITY_VILLAGER_NO", 1.0f, 0.9f);
            return;
        }

        int maxHomes = plugin.getMainConfigManager().getMaxHomesForLevel(clan.getLevel());
        boolean alreadyExists = clan.hasHome(homeName);

        if (!alreadyExists && clan.getHomeCount() >= maxHomes) {
            player.sendMessage(MessageUtils.toComponent(
                    plugin.getMainConfigManager().getPrefix() +
                            plugin.getMainConfigManager().getMessage("home-limit-reached",
                                    "&cYour clan has reached the maximum limit of homes ({current}/{max}). Level up your clan to unlock more!")
                                    .replace("{current}", String.valueOf(clan.getHomeCount()))
                                    .replace("{max}", String.valueOf(maxHomes))));
            SoundUtils.playSound(player, "ENTITY_VILLAGER_NO", 1.0f, 0.9f);
            return;
        }

        ClanHome home = ClanHome.fromLocation(homeName, player.getLocation());
        clan.setHome(home);
        plugin.getClanStorage().saveHome(clan.getName(), home);

        player.sendMessage(MessageUtils.toComponent(
                plugin.getMainConfigManager().getPrefix() +
                        plugin.getMainConfigManager().getMessage("home-set-success",
                                "&aClan home '&f{home}&a' has been set to your current location.")
                                .replace("{home}", home.getName())));
        SoundUtils.playSound(player, "BLOCK_NOTE_BLOCK_PLING", 1.0f, 1.5f);

        String broadcastMsg = plugin.getMainConfigManager().getMessage("home-set-broadcast",
                "&eClan home '&f{home}&e' was set by &f{player}&e.")
                .replace("{home}", home.getName())
                .replace("{player}", player.getName());

        for (UUID memberUuid : clan.getMembers().keySet()) {
            if (memberUuid.equals(player.getUniqueId()))
                continue;
            Player member = Bukkit.getPlayer(memberUuid);
            if (member != null && member.isOnline()) {
                member.sendMessage(MessageUtils.toComponent(
                        plugin.getMainConfigManager().getPrefix() + broadcastMsg));
            }
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 2 && sender instanceof Player player) {
            Clan clan = plugin.getClanManager().getClanByPlayer(player.getUniqueId());
            if (clan != null) {
                List<String> names = new ArrayList<>(clan.getHomeNames());
                if (!names.contains("default")) {
                    names.add("default");
                }
                return filterCompletions(names, args[1]);
            }
        }
        return Collections.emptyList();
    }
}
