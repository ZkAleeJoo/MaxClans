package org.zkaleejoo.utils;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

@SuppressWarnings({ "removal" })
public class SoundUtils {

    public static void playSound(Player player, String soundName, float volume, float pitch) {
        if (player == null || soundName == null || soundName.isBlank() || soundName.equalsIgnoreCase("none")) {
            return;
        }
        try {
            String key = soundName.trim().toLowerCase().replace(" ", "_");
            if (key.contains(":")) {
                String[] split = key.split(":", 2);
                key = split[1];
            }
            Sound sound = Registry.SOUNDS.get(NamespacedKey.minecraft(key.replace("_", ".")));
            if (sound == null) {
                sound = Registry.SOUNDS.get(NamespacedKey.minecraft(key));
            }
            if (sound == null) {
                sound = Sound.valueOf(soundName.trim().toUpperCase().replace(".", "_").replace(" ", "_"));
            }
            player.playSound(player.getLocation(), sound, volume, pitch);
        } catch (Exception ignored) {
        }
    }

    public static void playSound(Player player, String soundName) {
        playSound(player, soundName, 1.0f, 1.0f);
    }
}
