package org.zkaleejoo.commands.subcommands.general;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.zkaleejoo.OnlyClans;
import org.zkaleejoo.commands.SubCommand;
import org.zkaleejoo.models.Clan;
import org.zkaleejoo.models.ClanHome;
import org.zkaleejoo.utils.MessageUtils;
import org.zkaleejoo.utils.SoundUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HomeSubCommand extends SubCommand {

    public HomeSubCommand(OnlyClans plugin) {
        super(plugin, "home", "base");
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
        return List.of("home");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        Clan playerClan = plugin.getClanManager().getClanByPlayer(player.getUniqueId());

        if (playerClan == null) {
            player.sendMessage(MessageUtils.toComponent(
                    plugin.getMainConfigManager().getPrefix() +
                            plugin.getMainConfigManager().getMessage("not-in-clan", "&cYou are not in a clan.")));
            SoundUtils.playSound(player, "ENTITY_VILLAGER_NO", 1.0f, 0.9f);
            return;
        }

        Clan targetClan = playerClan;
        ClanHome targetHome = null;

        if (args.length < 2) {
            if (playerClan.getHomeCount() == 0) {
                player.sendMessage(MessageUtils.toComponent(
                        plugin.getMainConfigManager().getPrefix() +
                                plugin.getMainConfigManager().getMessage("home-none-set",
                                        "&cYour clan does not have any homes set. Use &e/clan sethome [name] &cto set one.")));
                SoundUtils.playSound(player, "ENTITY_VILLAGER_NO", 1.0f, 0.9f);
                return;
            }

            if (playerClan.getHomeCount() == 1) {
                targetHome = playerClan.getHomes().values().iterator().next();
            } else if (playerClan.hasHome("default")) {
                targetHome = playerClan.getHome("default");
            } else {
                String homesList = String.join(", ", playerClan.getHomeNames());
                player.sendMessage(MessageUtils.toComponent(
                        plugin.getMainConfigManager().getPrefix() +
                                plugin.getMainConfigManager().getMessage("home-specify-name",
                                        "&cYour clan has multiple homes: {homes}. Specify one with &e/clan home <name>&c.")
                                        .replace("{homes}", homesList)));
                SoundUtils.playSound(player, "ENTITY_VILLAGER_NO", 1.0f, 0.9f);
                return;
            }
        } else {
            String nameArg = args[1].trim();

            if (playerClan.hasHome(nameArg)) {
                targetHome = playerClan.getHome(nameArg);
            } else {
                Clan otherClan = plugin.getClanManager().getClanByName(nameArg);
                if (otherClan != null) {
                    boolean isAlly = playerClan.isAlly(otherClan.getName());
                    boolean isPublic = otherClan.isPublicHome();
                    boolean isAdmin = player.hasPermission("onlyclans.admin");

                    if ((isAlly && isPublic && plugin.getMainConfigManager().isHomeAllowAlliesIfPublic()) || isAdmin) {
                        targetClan = otherClan;
                        if (args.length >= 3) {
                            String otherHomeName = args[2].trim();
                            targetHome = otherClan.getHome(otherHomeName);
                            if (targetHome == null) {
                                player.sendMessage(MessageUtils.toComponent(
                                        plugin.getMainConfigManager().getPrefix() +
                                                plugin.getMainConfigManager().getMessage("home-not-found",
                                                        "&cClan home '&f{home}&c' does not exist.")
                                                        .replace("{home}", otherHomeName)));
                                SoundUtils.playSound(player, "ENTITY_VILLAGER_NO", 1.0f, 0.9f);
                                return;
                            }
                        } else {
                            if (otherClan.getHomeCount() == 0) {
                                player.sendMessage(MessageUtils.toComponent(
                                        plugin.getMainConfigManager().getPrefix() +
                                                plugin.getMainConfigManager().getMessage("home-none-set",
                                                        "&cThat clan does not have any homes set.")));
                                SoundUtils.playSound(player, "ENTITY_VILLAGER_NO", 1.0f, 0.9f);
                                return;
                            }
                            if (otherClan.getHomeCount() == 1) {
                                targetHome = otherClan.getHomes().values().iterator().next();
                            } else if (otherClan.hasHome("default")) {
                                targetHome = otherClan.getHome("default");
                            } else {
                                String homesList = String.join(", ", otherClan.getHomeNames());
                                player.sendMessage(MessageUtils.toComponent(
                                        plugin.getMainConfigManager().getPrefix() +
                                                plugin.getMainConfigManager().getMessage("home-specify-name",
                                                        "&cClan '{clan}' has multiple homes: {homes}. Specify one with &e/clan home {clan} <name>&c.")
                                                        .replace("{clan}", otherClan.getName())
                                                        .replace("{homes}", homesList)));
                                SoundUtils.playSound(player, "ENTITY_VILLAGER_NO", 1.0f, 0.9f);
                                return;
                            }
                        }
                    } else if (!isAlly) {
                        player.sendMessage(MessageUtils.toComponent(
                                plugin.getMainConfigManager().getPrefix() +
                                        plugin.getMainConfigManager().getMessage("home-ally-not-allowed",
                                                "&cYou can only teleport to bases of allied clans with public homes.")));
                        SoundUtils.playSound(player, "ENTITY_VILLAGER_NO", 1.0f, 0.9f);
                        return;
                    } else {
                        player.sendMessage(MessageUtils.toComponent(
                                plugin.getMainConfigManager().getPrefix() +
                                        plugin.getMainConfigManager().getMessage("home-not-public",
                                                "&cThat allied clan has disabled public home access.")));
                        SoundUtils.playSound(player, "ENTITY_VILLAGER_NO", 1.0f, 0.9f);
                        return;
                    }
                } else {
                    player.sendMessage(MessageUtils.toComponent(
                            plugin.getMainConfigManager().getPrefix() +
                                    plugin.getMainConfigManager().getMessage("home-not-found",
                                            "&cClan home '&f{home}&c' does not exist.")
                                            .replace("{home}", nameArg)));
                    SoundUtils.playSound(player, "ENTITY_VILLAGER_NO", 1.0f, 0.9f);
                    return;
                }
            }
        }

        if (targetHome != null) {
            plugin.getClanTeleportManager().startTeleport(player, targetClan, targetHome);
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            return Collections.emptyList();
        }

        Clan clan = plugin.getClanManager().getClanByPlayer(player.getUniqueId());
        if (clan == null) {
            return Collections.emptyList();
        }

        if (args.length == 2) {
            List<String> suggestions = new ArrayList<>(clan.getHomeNames());

            if (plugin.getMainConfigManager().isHomeAllowAlliesIfPublic()) {
                for (String allyName : clan.getAllies()) {
                    Clan allyClan = plugin.getClanManager().getClanByName(allyName);
                    if (allyClan != null && allyClan.isPublicHome() && allyClan.getHomeCount() > 0) {
                        suggestions.add(allyClan.getName());
                    }
                }
            }

            return filterCompletions(suggestions, args[1]);
        }

        if (args.length == 3) {
            Clan otherClan = plugin.getClanManager().getClanByName(args[1]);
            if (otherClan != null && (clan.isAlly(otherClan.getName()) || player.hasPermission("onlyclans.admin"))) {
                return filterCompletions(new ArrayList<>(otherClan.getHomeNames()), args[2]);
            }
        }

        return Collections.emptyList();
    }
}
