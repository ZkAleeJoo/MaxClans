package org.zkaleejoo.listeners;

import com.google.common.cache.CacheBuilder;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.Boss;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.zkaleejoo.MaxClans;
import org.zkaleejoo.models.Clan;
import org.zkaleejoo.models.ClanQuest;

import java.time.Duration;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings("null")
public class ClanExpListener implements Listener {

    private final MaxClans plugin;
    private final Set<BlockPosition> placedBlocks = Collections.newSetFromMap(
            CacheBuilder.newBuilder()
                    .maximumSize(50_000)
                    .expireAfterWrite(Duration.ofDays(3))
                    .<BlockPosition, Boolean>build()
                    .asMap()
    );

    public ClanExpListener(MaxClans plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        Material type = block.getType();

        if (isOre(type) || isLog(type)) {
            placedBlocks.add(BlockPosition.of(block));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() != GameMode.SURVIVAL) {
            return;
        }

        Clan clan = plugin.getClanManager().getClanByPlayer(player.getUniqueId());
        if (clan == null) {
            return;
        }

        Block block = event.getBlock();
        if (placedBlocks.remove(BlockPosition.of(block))) {
            return;
        }

        Material type = block.getType();

        if (isLog(type)) {
            int exp = plugin.getClanLevelManager().getWoodCuttingExp(type.name());
            String sourceName = plugin.getMainConfigManager().getMessage("exp-source-woodcutting", "Woodcutting");
            plugin.getClanLevelManager().addExp(clan, exp, sourceName, player);
            if (plugin.getClanQuestManager() != null) {
                plugin.getClanQuestManager().incrementProgress(clan, ClanQuest.QuestType.CHOP_WOOD, type.name(), 1);
            }
        } else if (isOre(type)) {
            int exp = plugin.getClanLevelManager().getMiningExp(type.name());
            String sourceName = plugin.getMainConfigManager().getMessage("exp-source-mining", "Mining");
            plugin.getClanLevelManager().addExp(clan, exp, sourceName, player);
            if (plugin.getClanQuestManager() != null) {
                plugin.getClanQuestManager().incrementProgress(clan, ClanQuest.QuestType.MINE_BLOCK, type.name(), 1);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) {
            return;
        }

        Clan clan = plugin.getClanManager().getClanByPlayer(killer.getUniqueId());
        if (clan == null) {
            return;
        }

        var entity = event.getEntity();
        if (entity instanceof Player) {
            return; // Player PvP handled in ClanDamageListener
        }

        boolean isBoss = entity instanceof Boss;
        boolean isMonster = entity instanceof Monster || isBoss;

        if (isMonster) {
            String typeName = entity.getType().name();
            int exp = plugin.getClanLevelManager().getMonsterExp(typeName, isBoss);
            String sourceName = plugin.getMainConfigManager().getMessage("exp-source-monster", "Monster: {mob}")
                    .replace("{mob}", typeName);
            plugin.getClanLevelManager().addExp(clan, exp, sourceName, killer);

            if (plugin.getClanQuestManager() != null) {
                plugin.getClanQuestManager().incrementProgress(clan, ClanQuest.QuestType.KILL_MOB, typeName, 1);
            }
        }
    }

    private boolean isLog(Material material) {
        if (Tag.LOGS.isTagged(material)) return true;
        String name = material.name();
        return name.endsWith("_LOG") || name.endsWith("_WOOD") || name.endsWith("_STEM") || name.endsWith("_HYPHAE");
    }

    private boolean isOre(Material material) {
        if (material == Material.ANCIENT_DEBRIS) return true;
        String name = material.name();
        return name.endsWith("_ORE");
    }

    private record BlockPosition(UUID worldUid, int x, int y, int z) {
        public static BlockPosition of(Block block) {
            return new BlockPosition(block.getWorld().getUID(), block.getX(), block.getY(), block.getZ());
        }
    }
}
