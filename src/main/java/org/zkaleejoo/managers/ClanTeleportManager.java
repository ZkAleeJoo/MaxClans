package org.zkaleejoo.managers;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.zkaleejoo.MaxClans;
import org.zkaleejoo.models.Clan;
import org.zkaleejoo.models.ClanHome;
import org.zkaleejoo.utils.FoliaCompat;
import org.zkaleejoo.utils.FoliaCompat.WrappedTask;
import org.zkaleejoo.utils.MessageUtils;
import org.zkaleejoo.utils.SoundUtils;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ClanTeleportManager {

    private final MaxClans plugin;
    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();
    private final Map<UUID, PendingTeleport> pendingTeleports = new ConcurrentHashMap<>();

    public static class PendingTeleport {
        private final UUID playerUuid;
        private final Location initialLocation;
        private final Location targetLocation;
        private final String homeName;
        private final String clanName;
        private int secondsRemaining;
        private WrappedTask task;

        public PendingTeleport(UUID playerUuid, Location initialLocation, Location targetLocation,
                               String homeName, String clanName, int secondsRemaining) {
            this.playerUuid = playerUuid;
            this.initialLocation = initialLocation.clone();
            this.targetLocation = targetLocation.clone();
            this.homeName = homeName;
            this.clanName = clanName;
            this.secondsRemaining = secondsRemaining;
        }

        public UUID getPlayerUuid() {
            return playerUuid;
        }

        public Location getInitialLocation() {
            return initialLocation;
        }

        public Location getTargetLocation() {
            return targetLocation;
        }

        public String getHomeName() {
            return homeName;
        }

        public String getClanName() {
            return clanName;
        }

        public int getSecondsRemaining() {
            return secondsRemaining;
        }

        public void decrementSeconds() {
            this.secondsRemaining--;
        }

        public void setTask(WrappedTask task) {
            this.task = task;
        }

        public void cancelTask() {
            if (task != null) {
                task.cancel();
                task = null;
            }
        }
    }

    public ClanTeleportManager(MaxClans plugin) {
        this.plugin = plugin;
    }

    public boolean hasPendingTeleport(UUID uuid) {
        return pendingTeleports.containsKey(uuid);
    }

    public PendingTeleport getPendingTeleport(UUID uuid) {
        return pendingTeleports.get(uuid);
    }

    public boolean isOnCooldown(Player player) {
        if (player.hasPermission("maxclans.bypass.cooldown")) {
            return false;
        }
        Long expiry = cooldowns.get(player.getUniqueId());
        if (expiry == null) {
            return false;
        }
        if (System.currentTimeMillis() >= expiry) {
            cooldowns.remove(player.getUniqueId());
            return false;
        }
        return true;
    }

    public long getRemainingCooldownSeconds(UUID uuid) {
        Long expiry = cooldowns.get(uuid);
        if (expiry == null) {
            return 0;
        }
        long remainingMs = expiry - System.currentTimeMillis();
        return remainingMs > 0 ? (remainingMs + 999) / 1000 : 0;
    }

    public void setCooldown(UUID uuid) {
        int seconds = plugin.getMainConfigManager().getHomeCooldownSeconds();
        if (seconds > 0) {
            cooldowns.put(uuid, System.currentTimeMillis() + (seconds * 1000L));
        }
    }

    public void resetCooldown(UUID uuid) {
        cooldowns.remove(uuid);
    }

    public void startTeleport(Player player, Clan clan, ClanHome home) {
        UUID uuid = player.getUniqueId();

        if (isOnCooldown(player)) {
            long remaining = getRemainingCooldownSeconds(uuid);
            String timeStr = formatRemainingTime(remaining);
            player.sendMessage(MessageUtils.toComponent(
                    plugin.getMainConfigManager().getPrefix() +
                            plugin.getMainConfigManager().getMessage("home-cooldown",
                                    "&cYou must wait {time} before teleporting again.")
                                    .replace("{time}", timeStr)
                                    .replace("{seconds}", String.valueOf(remaining))));
            SoundUtils.playSound(player, "ENTITY_VILLAGER_NO", 1.0f, 0.9f);
            return;
        }

        Location targetLocation = home.toLocation();
        if (targetLocation == null || targetLocation.getWorld() == null) {
            player.sendMessage(MessageUtils.toComponent(
                    plugin.getMainConfigManager().getPrefix() +
                            plugin.getMainConfigManager().getMessage("home-world-not-found",
                                    "&cThe world for this clan home is currently not loaded.")));
            SoundUtils.playSound(player, "ENTITY_VILLAGER_NO", 1.0f, 0.9f);
            return;
        }

        int warmupSeconds = plugin.getMainConfigManager().getHomeWarmupSeconds();
        boolean bypassWarmup = player.hasPermission("maxclans.bypass.warmup");

        if (warmupSeconds <= 0 || bypassWarmup) {
            executeTeleport(player, targetLocation, home.getName());
            return;
        }

        cancelSilently(uuid);

        PendingTeleport pending = new PendingTeleport(
                uuid,
                player.getLocation(),
                targetLocation,
                home.getName(),
                clan.getName(),
                warmupSeconds
        );

        pendingTeleports.put(uuid, pending);

        player.sendMessage(MessageUtils.toComponent(
                plugin.getMainConfigManager().getPrefix() +
                        plugin.getMainConfigManager().getMessage("home-warmup-start",
                                "&aTeleporting to '&f{home}&a' in &e{seconds}s&a... Do not move or take damage!")
                                .replace("{home}", home.getName())
                                .replace("{seconds}", String.valueOf(warmupSeconds))));
        SoundUtils.playSound(player, "BLOCK_NOTE_BLOCK_PLING", 0.7f, 1.2f);

        WrappedTask task = FoliaCompat.runForEntityTimer(plugin, player, () -> {
            PendingTeleport current = pendingTeleports.get(uuid);
            if (current == null) {
                return;
            }

            if (!player.isOnline() || player.isDead()) {
                cancelSilently(uuid);
                return;
            }

            current.decrementSeconds();

            if (current.getSecondsRemaining() > 0) {
                SoundUtils.playSound(player, "BLOCK_NOTE_BLOCK_HAT", 0.5f, 1.5f);
            } else {
                cancelSilently(uuid);
                executeTeleport(player, current.getTargetLocation(), current.getHomeName());
            }
        }, 20L, 20L);

        pending.setTask(task);
    }

    private void executeTeleport(Player player, Location targetLocation, String homeName) {
        FoliaCompat.teleport(player, targetLocation);
        SoundUtils.playSound(player, "ENTITY_ENDERMAN_TELEPORT", 1.0f, 1.0f);
        player.sendMessage(MessageUtils.toComponent(
                plugin.getMainConfigManager().getPrefix() +
                        plugin.getMainConfigManager().getMessage("home-teleported",
                                "&aTeleported to clan home '&f{home}&a'!")
                                .replace("{home}", homeName)));
        setCooldown(player.getUniqueId());
    }

    public void cancelOnMove(Player player) {
        if (!plugin.getMainConfigManager().isHomeCancelOnMove()) {
            return;
        }
        PendingTeleport pending = pendingTeleports.remove(player.getUniqueId());
        if (pending != null) {
            pending.cancelTask();
            SoundUtils.playSound(player, "ENTITY_VILLAGER_NO", 1.0f, 0.8f);
            player.sendMessage(MessageUtils.toComponent(
                    plugin.getMainConfigManager().getPrefix() +
                            plugin.getMainConfigManager().getMessage("home-teleport-cancelled-move",
                                    "&cTeleportation cancelled because you moved!")));
        }
    }

    public void cancelOnDamage(Player player) {
        if (!plugin.getMainConfigManager().isHomeCancelOnDamage()) {
            return;
        }
        PendingTeleport pending = pendingTeleports.remove(player.getUniqueId());
        if (pending != null) {
            pending.cancelTask();
            SoundUtils.playSound(player, "ENTITY_VILLAGER_NO", 1.0f, 0.8f);
            player.sendMessage(MessageUtils.toComponent(
                    plugin.getMainConfigManager().getPrefix() +
                            plugin.getMainConfigManager().getMessage("home-teleport-cancelled-damage",
                                    "&cTeleportation cancelled because you took damage!")));
        }
    }

    public void cancelSilently(UUID uuid) {
        PendingTeleport pending = pendingTeleports.remove(uuid);
        if (pending != null) {
            pending.cancelTask();
        }
    }

    public void cancelAll() {
        for (PendingTeleport pending : pendingTeleports.values()) {
            pending.cancelTask();
        }
        pendingTeleports.clear();
        cooldowns.clear();
    }

    private String formatRemainingTime(long seconds) {
        if (seconds < 60) {
            return seconds + "s";
        }
        long minutes = seconds / 60;
        long remainingSec = seconds % 60;
        if (remainingSec == 0) {
            return minutes + "m";
        }
        return minutes + "m " + remainingSec + "s";
    }
}
