package user.meistertisch.treeCapitator.event;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import user.meistertisch.treeCapitator.TreeCapitator;

import java.util.concurrent.atomic.AtomicInteger;


public class EventBlockBreak implements Listener {
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event){
        Material blockType = event.getBlock().getType();

        AtomicInteger counter = new AtomicInteger(32); // TODO: Change value to variable
        if(Tag.LOGS.isTagged(blockType)) {
            destroyBlock(event.getBlock(), event.getPlayer().getInventory().getItemInMainHand(), counter);
        }
    }

    private void destroyBlock(Block block, ItemStack tool, AtomicInteger counter){
        // Return if block is air
        if(counter.decrementAndGet() < 0 || block.getType().isAir()){
            return;
        }

        // Safe block type for later check
        Material blockType = block.getType();

        block.breakNaturally(tool, true, true); // TODO: Durability not reducing yet

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
                        Bukkit.getScheduler().scheduleSyncDelayedTask(TreeCapitator.getPlugin(), () -> destroyBlock(neighborBlock, tool, counter),
                                5+5*(Math.abs(x) +  Math.abs(y) + Math.abs(z))); // The outer the block, the later it breaks; TODO: Make Speed variable for user preferences
                    }
                }
            }
        }
    }
}