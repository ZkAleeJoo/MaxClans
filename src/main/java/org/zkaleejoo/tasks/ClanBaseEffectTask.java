package org.zkaleejoo.tasks;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.zkaleejoo.OnlyClans;
import org.zkaleejoo.models.Clan;
import org.zkaleejoo.models.ClanHome;
import org.zkaleejoo.models.ClanLevelConfig;
import org.zkaleejoo.utils.FoliaCompat;

public class ClanBaseEffectTask implements Runnable {

    private final OnlyClans plugin;

    public ClanBaseEffectTask(OnlyClans plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Clan clan = plugin.getClanManager().getClanByPlayer(player.getUniqueId());
            if (clan == null) continue;

            if (plugin.getClanLevelManager() == null || !plugin.getClanLevelManager().hasBaseEffect(clan.getLevel())) {
                continue;
            }

            ClanLevelConfig config = plugin.getClanLevelManager().getLevelConfig(clan.getLevel());
            if (!config.hasBaseEffect()) continue;

            double radius = config.getBaseEffectRadius();
            double radiusSquared = radius * radius;

            boolean inBase = false;
            for (ClanHome home : clan.getHomes().values()) {
                if (home.getWorldName() != null && home.getWorldName().equalsIgnoreCase(player.getWorld().getName())) {
                    double dx = player.getLocation().getX() - home.getX();
                    double dy = player.getLocation().getY() - home.getY();
                    double dz = player.getLocation().getZ() - home.getZ();
                    double distSq = dx * dx + dy * dy + dz * dz;

                    if (distSq <= radiusSquared) {
                        inBase = true;
                        break;
                    }
                }
            }

            if (inBase) {
                PotionEffectType type = resolveEffectType(config.getBaseEffectType());
                if (type != null) {
                    FoliaCompat.runForEntity(plugin, player, () -> {
                        if (player.isOnline()) {
                            player.addPotionEffect(new PotionEffect(type, 160, config.getBaseEffectAmplifier(), true, false, true));
                        }
                    });
                }
            }
        }
    }

    private PotionEffectType resolveEffectType(String name) {
        if (name == null) return PotionEffectType.HASTE;
        String clean = name.toUpperCase().trim();
        return switch (clean) {
            case "REGEN", "REGENERATION" -> PotionEffectType.REGENERATION;
            case "SPEED" -> PotionEffectType.SPEED;
            case "RESISTANCE" -> PotionEffectType.RESISTANCE;
            case "STRENGTH" -> PotionEffectType.STRENGTH;
            case "JUMP", "JUMP_BOOST" -> PotionEffectType.JUMP_BOOST;
            default -> PotionEffectType.HASTE;
        };
    }
}
