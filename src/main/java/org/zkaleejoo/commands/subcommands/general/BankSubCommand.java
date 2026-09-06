package org.zkaleejoo.commands.subcommands.general;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.zkaleejoo.OnlyClans;
import org.zkaleejoo.commands.SubCommand;
import org.zkaleejoo.models.Clan;
import org.zkaleejoo.models.ClanPlayer;
import org.zkaleejoo.models.ClanQuest;
import org.zkaleejoo.models.ClanRole;
import org.zkaleejoo.utils.MessageUtils;
import org.zkaleejoo.utils.SoundUtils;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;

public class BankSubCommand extends SubCommand {

    public BankSubCommand(OnlyClans plugin) {
        super(plugin, "bank", "banco");
    }

    @Override
    public String getPermission() {
        return "onlyclans.command.bank";
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

        if (plugin.getClanLevelManager() != null && !plugin.getClanLevelManager().hasBankAccess(clan.getLevel())) {
            player.sendMessage(MessageUtils.toComponent(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                            .getMessage("bank-locked",
                                    "&cAccess to the Clan Bank unlocks at Clan &eLevel 2&c. Current level: &e{level}")
                            .replace("{level}", String.valueOf(clan.getLevel()))));
            SoundUtils.playSound(player, "ENTITY_VILLAGER_NO", 1.0f, 0.9f);
            return;
        }

        if (args.length == 1 || args[1].equalsIgnoreCase("balance") || args[1].equalsIgnoreCase("saldo")) {
            showBalance(player, clan);
            return;
        }

        String sub = args[1].toLowerCase();
        if (sub.equals("deposit") || sub.equals("depositar")) {
            handleDeposit(player, clan, args);
        } else if (sub.equals("withdraw") || sub.equals("retirar")) {
            handleWithdraw(player, clan, args);
        } else {
            player.sendMessage(MessageUtils.toComponent(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                            .getMessage("usage-bank",
                                    "&cUsage: /clan bank [deposit <amount> | withdraw <amount> | balance]")));
        }
    }

    private void showBalance(Player player, Clan clan) {
        String formattedBal = String.format(Locale.US, "%,.2f", clan.getBankBalance());
        String prefix = plugin.getMainConfigManager().getPrefix();
        player.sendMessage(MessageUtils.toComponent(
                prefix + plugin.getMainConfigManager().getMessage("bank-balance",
                        "&#FDE047&lClan Bank: &#6EE7B7${balance}&e coins.")
                        .replace("{balance}", formattedBal)));
        SoundUtils.playSound(player, "BLOCK_NOTE_BLOCK_PLING", 0.8f, 1.4f);
    }

    private void handleDeposit(Player player, Clan clan, String[] args) {
        if (args.length < 3) {
            player.sendMessage(MessageUtils.toComponent(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                            .getMessage("usage-bank-deposit", "&cUsage: /clan bank deposit <amount>")));
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            player.sendMessage(MessageUtils.toComponent(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                            .getMessage("invalid-number", "&cPlease enter a valid numeric amount.")));
            return;
        }

        if (amount <= 0) {
            player.sendMessage(MessageUtils.toComponent(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                            .getMessage("invalid-amount", "&cAmount must be greater than 0.")));
            return;
        }

        if (!hasEconomy()) {
            clan.depositBank(amount);
            plugin.getClanStorage().updateClan(clan);
            notifyDeposit(player, clan, amount);
            return;
        }

        if (getPlayerBalance(player) < amount) {
            player.sendMessage(MessageUtils.toComponent(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                            .getMessage("bank-insufficient-player",
                                    "&cYou do not have enough money to deposit.")));
            return;
        }

        if (withdrawFromPlayer(player, amount)) {
            clan.depositBank(amount);
            plugin.getClanStorage().updateClan(clan);
            notifyDeposit(player, clan, amount);
        } else {
            player.sendMessage(MessageUtils.toComponent(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                            .getMessage("bank-error-transaction", "&cError processing economy transaction.")));
        }
    }

    private void notifyDeposit(Player player, Clan clan, double amount) {
        String formatted = String.format(Locale.US, "%,.2f", amount);
        String prefix = plugin.getMainConfigManager().getPrefix();
        String msg = prefix + plugin.getMainConfigManager().getMessage("bank-deposited",
                "&aYou deposited &#6EE7B7${amount}&a into the clan bank.")
                .replace("{amount}", formatted);
        player.sendMessage(MessageUtils.toComponent(msg));
        SoundUtils.playSound(player, "ENTITY_PLAYER_LEVELUP", 0.7f, 1.6f);

        if (plugin.getClanQuestManager() != null) {
            plugin.getClanQuestManager().incrementProgress(clan, ClanQuest.QuestType.DONATE_BANK, "COINS",
                    (int) amount);
        }

        String broadcastMsg = prefix + plugin.getMainConfigManager().getMessage("bank-deposited-broadcast",
                "&e{player} deposited &#6EE7B7${amount}&e into the clan bank.")
                .replace("{player}", player.getName())
                .replace("{amount}", formatted);

        for (var memberUuid : clan.getMembers().keySet()) {
            Player m = Bukkit.getPlayer(memberUuid);
            if (m != null && m.isOnline() && !m.equals(player)) {
                m.sendMessage(MessageUtils.toComponent(broadcastMsg));
            }
        }
    }

    private void handleWithdraw(Player player, Clan clan, String[] args) {
        ClanPlayer cp = clan.getMember(player.getUniqueId());
        if (cp == null || !cp.hasRoleAtLeast(ClanRole.MODERATOR)) {
            player.sendMessage(MessageUtils.toComponent(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                            .getMessage("bank-no-permission",
                                    "&cOnly clan leaders and moderators can withdraw funds from the bank.")));
            return;
        }

        if (args.length < 3) {
            player.sendMessage(MessageUtils.toComponent(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                            .getMessage("usage-bank-withdraw", "&cUsage: /clan bank withdraw <amount>")));
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            player.sendMessage(MessageUtils.toComponent(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                            .getMessage("invalid-number", "&cPlease enter a valid numeric amount.")));
            return;
        }

        if (amount <= 0) {
            player.sendMessage(MessageUtils.toComponent(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                            .getMessage("invalid-amount", "&cAmount must be greater than 0.")));
            return;
        }

        if (clan.getBankBalance() < amount) {
            player.sendMessage(MessageUtils.toComponent(
                    plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                            .getMessage("bank-insufficient-clan",
                                    "&cThe clan bank does not have sufficient funds.")));
            return;
        }

        if (hasEconomy()) {
            if (depositToPlayer(player, amount)) {
                clan.withdrawBank(amount);
                plugin.getClanStorage().updateClan(clan);
                notifyWithdraw(player, clan, amount);
            } else {
                player.sendMessage(MessageUtils.toComponent(
                        plugin.getMainConfigManager().getPrefix() + plugin.getMainConfigManager()
                                .getMessage("bank-error-deposit", "&cError depositing funds into your account.")));
            }
        } else {
            clan.withdrawBank(amount);
            plugin.getClanStorage().updateClan(clan);
            notifyWithdraw(player, clan, amount);
        }
    }

    private void notifyWithdraw(Player player, Clan clan, double amount) {
        String formatted = String.format(Locale.US, "%,.2f", amount);
        String prefix = plugin.getMainConfigManager().getPrefix();
        String msg = prefix + plugin.getMainConfigManager().getMessage("bank-withdrawn",
                "&eYou withdrew &#6EE7B7${amount}&e from the clan bank.")
                .replace("{amount}", formatted);
        player.sendMessage(MessageUtils.toComponent(msg));
        SoundUtils.playSound(player, "BLOCK_NOTE_BLOCK_PLING", 0.9f, 1.2f);

        String broadcastMsg = prefix + plugin.getMainConfigManager().getMessage("bank-withdrawn-broadcast",
                "&#FDA4AF{player} withdrew &#FDE047${amount}&#FDA4AF from the clan bank.")
                .replace("{player}", player.getName())
                .replace("{amount}", formatted);

        for (var memberUuid : clan.getMembers().keySet()) {
            Player m = Bukkit.getPlayer(memberUuid);
            if (m != null && m.isOnline() && !m.equals(player)) {
                m.sendMessage(MessageUtils.toComponent(broadcastMsg));
            }
        }
    }

    private boolean hasEconomy() {
        return Bukkit.getPluginManager().isPluginEnabled("Vault");
    }

    private double getPlayerBalance(Player player) {
        try {
            Class<?> econClass = Class.forName("net.milkbowl.vault.economy.Economy");
            RegisteredServiceProvider<?> rsp = Bukkit.getServicesManager().getRegistration(econClass);
            if (rsp != null) {
                Object econ = rsp.getProvider();
                Method getBal = econClass.getMethod("getBalance", org.bukkit.OfflinePlayer.class);
                return (double) getBal.invoke(econ, player);
            }
        } catch (Exception ignored) {
        }
        return Double.MAX_VALUE;
    }

    private boolean withdrawFromPlayer(Player player, double amount) {
        try {
            Class<?> econClass = Class.forName("net.milkbowl.vault.economy.Economy");
            RegisteredServiceProvider<?> rsp = Bukkit.getServicesManager().getRegistration(econClass);
            if (rsp != null) {
                Object econ = rsp.getProvider();
                Method withdraw = econClass.getMethod("withdrawPlayer", org.bukkit.OfflinePlayer.class, double.class);
                Object response = withdraw.invoke(econ, player, amount);
                Method transSuccess = response.getClass().getMethod("transactionSuccess");
                return (boolean) transSuccess.invoke(response);
            }
        } catch (Exception ignored) {
        }
        return true;
    }

    private boolean depositToPlayer(Player player, double amount) {
        try {
            Class<?> econClass = Class.forName("net.milkbowl.vault.economy.Economy");
            RegisteredServiceProvider<?> rsp = Bukkit.getServicesManager().getRegistration(econClass);
            if (rsp != null) {
                Object econ = rsp.getProvider();
                Method deposit = econClass.getMethod("depositPlayer", org.bukkit.OfflinePlayer.class, double.class);
                Object response = deposit.invoke(econ, player, amount);
                Method transSuccess = response.getClass().getMethod("transactionSuccess");
                return (boolean) transSuccess.invoke(response);
            }
        } catch (Exception ignored) {
        }
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 2) {
            return filterCompletions(List.of("deposit", "withdraw", "balance"), args[1]);
        }
        return super.tabComplete(sender, args);
    }
}
