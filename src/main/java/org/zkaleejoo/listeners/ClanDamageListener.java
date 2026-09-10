package org.zkaleejoo.listeners;

import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.zkaleejoo.MaxClans;
import org.zkaleejoo.models.Clan;
import org.zkaleejoo.utils.MessageUtils;

public class ClanDamageListener implements Listener {

    private final MaxClans plugin;

    public ClanDamageListener(MaxClans plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim))
            return;

        Player attacker = getAttacker(event);
        if (attacker == null || attacker.equals(victim))
            return;

        Clan attackerClan = plugin.getClanManager().getClanByPlayer(attacker.getUniqueId());
        Clan victimClan = plugin.getClanManager().getClanByPlayer(victim.getUniqueId());

        if (attackerClan == null || victimClan == null)
            return;

        if (attackerClan.getName().equalsIgnoreCase(victimClan.getName())) {
            if (!attackerClan.isFriendlyFire()) {
                event.setCancelled(true);
                attacker.sendMessage(MessageUtils.toComponent(
                        plugin.getMainConfigManager().getPrefix()
                                + plugin.getMainConfigManager().getMessage("friendly-fire-disabled",
                                        "&cFriendly fire is disabled in your clan.")));
            }
        } else if (attackerClan.isAlly(victimClan.getName()) || victimClan.isAlly(attackerClan.getName())) {
            if (!attackerClan.isAllyDamage() || !victimClan.isAllyDamage()) {
                event.setCancelled(true);
                attacker.sendMessage(MessageUtils.toComponent(
                        plugin.getMainConfigManager().getPrefix()
                                + plugin.getMainConfigManager().getMessage("ally-damage-disabled",
                                        "&cDamage towards allied clans is disabled.")));
            }
        }
    }

    private Player getAttacker(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            return player;
        }
        if (event.getDamager() instanceof Projectile projectile) {
            if (projectile.getShooter() instanceof Player shooter) {
                return shooter;
            }
        }
        return null;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getPlayer();
        Player attacker = victim.getKiller();
        if (attacker != null && !attacker.equals(victim)) {
            plugin.getClanManager().registerPvPStats(attacker, victim);

            Clan attackerClan = plugin.getClanManager().getClanByPlayer(attacker.getUniqueId());
            if (attackerClan != null) {
                Clan victimClan = plugin.getClanManager().getClanByPlayer(victim.getUniqueId());
                boolean isSameClan = (victimClan != null
                        && attackerClan.getName().equalsIgnoreCase(victimClan.getName()));
                boolean isAlly = (victimClan != null
                        && (attackerClan.isAlly(victimClan.getName()) || victimClan.isAlly(attackerClan.getName())));

                if (isSameClan || isAlly) {
                    return;
                }

                boolean isRival = (victimClan != null);
                if (plugin.getClanLevelManager() != null) {
                    int exp = plugin.getClanLevelManager().getPvPExp(isRival);
                    plugin.getClanLevelManager().addExp(attackerClan, exp, isRival ? "PvP Rival" : "PvP", attacker);
                }

                if (plugin.getClanQuestManager() != null) {
                    plugin.getClanQuestManager().incrementProgress(attackerClan,
                            org.zkaleejoo.models.ClanQuest.QuestType.PVP_KILLS, "PLAYER", 1);
                }
            }
        }
    }
}
