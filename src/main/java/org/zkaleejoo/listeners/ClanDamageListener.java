package org.zkaleejoo.listeners;

import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.zkaleejoo.OnlyClans;
import org.zkaleejoo.models.Clan;
import org.zkaleejoo.utils.MessageUtils;

public class ClanDamageListener implements Listener {

    private final OnlyClans plugin;

    public ClanDamageListener(OnlyClans plugin) {
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
                attacker.sendMessage(MessageUtils.getColoredMessage(
                        plugin.getMainConfigManager().getPrefix()
                                + plugin.getMainConfigManager().getMessage("friendly-fire-disabled",
                                        "&cFriendly fire is disabled in your clan.")));
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
}
