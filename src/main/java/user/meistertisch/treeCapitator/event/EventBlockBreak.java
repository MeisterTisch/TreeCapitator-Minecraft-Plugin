package user.meistertisch.treeCapitator.event;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import user.meistertisch.treeCapitator.TreeCapitator;

import java.util.concurrent.atomic.AtomicInteger;


public class EventBlockBreak implements Listener {
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event){
        Block block = event.getBlock();
        Material blockType = block.getType();

        // Is block a log?
        if (!Tag.LOGS.isTagged(blockType)) {
            return;
        }

        Player player = event.getPlayer();

        // Is player in survival
        boolean onlySurvival = TreeCapitator.getPlugin().getConfig().getBoolean("onlySurvival", true);
        if (onlySurvival && player.getGameMode() != GameMode.SURVIVAL) {
            return;
        }

        ItemStack tool = player.getInventory().getItemInMainHand();

        // Has player an axe?
        boolean onlyAxe = TreeCapitator.getPlugin().getConfig().getBoolean("onlyAxe", true);
        Material toolType = tool.getType();

        if (onlyAxe) {
            // Only axes are allowed
            if (!Tag.ITEMS_AXES.isTagged(toolType)) return;
        } else {
            // Only axe or hand
            boolean isAxe = Tag.ITEMS_AXES.isTagged(toolType);
            boolean isHand = toolType.isAir();

            if (!isAxe && !isHand) {
                return;
            }
        }

        // Is log player placed?
        boolean detectTrees = TreeCapitator.getPlugin().getConfig().getInt("treeDetection.mode", 0) != 0;
        if (detectTrees && TreeCapitator.getPlugin().getLogChecker().isPlayerPlaced(block)) {
            return;
        }

        int limit = TreeCapitator.getPlugin().getConfig().getInt("limit", 64);
        AtomicInteger counter = new AtomicInteger(limit);
        destroyBlock(player, block, tool, counter, true);
    }

    private void destroyBlock(Player player, Block block, ItemStack tool, AtomicInteger counter, boolean isFirstBlock){
        // Return if player switched tool during the process
        if (!player.getInventory().getItemInMainHand().equals(tool)) {
            return;
        }

        // Return if counter is above limit or block is air
        if(block.getType().isAir()) {
            return;
        }

        // Is log player placed?
        boolean detectTrees = TreeCapitator.getPlugin().getConfig().getInt("treeDetection.mode", 0) != 0;
        boolean isDeep = TreeCapitator.getPlugin().getConfig().getBoolean("treeDetection.deep", true);
        if (detectTrees) {
            if(isFirstBlock || isDeep) {
                if(TreeCapitator.getPlugin().getLogChecker().isPlayerPlaced(block)){
                    return;
                }
            }
        }

        // Return if limit is surpassed
        if (counter.get() <= 0) return;

        // Return if limit will be surpassed
        if (counter.decrementAndGet() < 0) return;

        // Safe block type for later check
        Material blockType = block.getType();

        block.breakNaturally(tool, true, true);
        damageItem(player, tool);

        for (int y = -1; y <= 1; y++) {
            for (int z = -1; z <= 1; z++) {
                for (int x = -1; x <= 1; x++) {
                    //Skip the original block
                    if(x == 0 && y == 0 && z == 0) {
                        continue;
                    }

                    // Get speed from config and check in if it's in range; TODO: Fix corner speeds (Too fast with neighbor blocks somehow, will check later)
                    long speed = TreeCapitator.getPlugin().getConfig().getInt("speed", 3);
                    speed = Math.clamp(speed, 1, 5);

                    // Calculate distance
                    double distance = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));

                    // Calculate delay according to speed value from config
                    long delay = (speed * 2) + (long) (distance * 2.5);

                    // Get relatives, check for same type as original block. If same, recursive call of methode with delay for smooth gameplay
                    Block neighborBlock = block.getRelative(x, y, z);
                    if (neighborBlock.getType() == blockType) {
                        Bukkit.getScheduler().scheduleSyncDelayedTask(TreeCapitator.getPlugin(),
                                () -> destroyBlock(player, neighborBlock, tool, counter, false), delay); // The outer the block, the later it breaks;
                    }
                }
            }
        }
    }

    private void damageItem(Player player, ItemStack tool) {
        if (tool == null || tool.getType().isAir()) {
            return;
        }

        ItemMeta meta = tool.getItemMeta();
        if (meta instanceof Damageable damageable) {
            // Add damage
            damageable.setDamage(damageable.getDamage() + 1);
            tool.setItemMeta(damageable);

            // Item breaks
            if (damageable.getDamage() >= tool.getType().getMaxDurability()) {
                player.getInventory().setItemInMainHand(null);
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
            }
        }
    }
}