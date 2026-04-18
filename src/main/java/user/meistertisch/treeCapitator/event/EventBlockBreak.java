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

        // Is player in survival; TODO: Replace with variable setting
        if (player.getGameMode() != GameMode.SURVIVAL) {
            return;
        }

        ItemStack tool = player.getInventory().getItemInMainHand();

        // Has player an axe?; TODO: Replace with variable setting (allowing other tools/hand)
        if (!Tag.ITEMS_AXES.isTagged(tool.getType())) {
            return;
        }

        int limit = TreeCapitator.getPlugin().getConfig().getInt("limit", 64);
        AtomicInteger counter = new AtomicInteger(limit);
        destroyBlock(player, block, tool, counter);
    }

    private void destroyBlock(Player player, Block block, ItemStack tool, AtomicInteger counter){
        System.out.println(counter.get());

        // Return if player switched tool during the process
        if (!player.getInventory().getItemInMainHand().equals(tool)) {
            return;
        }

        // Return if counter is above limit or block is air
        if(counter.decrementAndGet() < 0 || block.getType().isAir()){
            return;
        }

        // Safe block type for later check
        Material blockType = block.getType();

        block.breakNaturally(tool, true, true);
        damageItem(player, tool);

        for (int y = -1; y <= 1; y++) {
            for (int z = -1; z <= 1; z++) {
                for (int x = -1; x <= 1; x++) {
                    //Skip the original block
                    if(x == 0 && y == 0 && z == 0){
                        continue;
                    }

                    // Get relatives, check for same type as original block. If same, recursive call of methode with delay for smooth gameplay
                    Block neighborBlock = block.getRelative(x, y, z);
                    if (neighborBlock.getType() == blockType) {
                        Bukkit.getScheduler().scheduleSyncDelayedTask(TreeCapitator.getPlugin(), () -> destroyBlock(player, neighborBlock, tool, counter),
                                5 + 5 * (Math.abs(x) +  Math.abs(y) + Math.abs(z))); // The outer the block, the later it breaks; TODO: Make Speed variable for user preferences
                    } //TODO: Solve Problem: When multiple logs nearby, count goes too fast and skips
                }
            }
        }
    }

    private void damageItem(Player player, ItemStack tool) {
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