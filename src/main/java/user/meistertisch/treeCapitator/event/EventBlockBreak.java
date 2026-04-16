package user.meistertisch.treeCapitator.event;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import user.meistertisch.treeCapitator.TreeCapitator;


public class EventBlockBreak implements Listener {
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event){
        Material blockType = event.getBlock().getType();

        // TODO: Change Check to block tag check instead of material
        if(blockType == Material.OAK_LOG) {
            destroyBlock(event.getBlock(), event.getPlayer().getInventory().getItemInMainHand());
        }
    }

    int i = 0;
    private void destroyBlock(Block block, ItemStack tool){
        // Return if block is air
        if(block.getType().isAir()){
            return;
        }

        if(i>100){
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
                        Bukkit.getScheduler().scheduleSyncDelayedTask(TreeCapitator.getPlugin(), () -> destroyBlock(neighborBlock, tool),
                                5+5*(Math.abs(x) +  Math.abs(y) + Math.abs(z))); // The outer the block, the later it breaks; TODO: Make Speed variable for user preferences
                    }
                }
            }
        }
    }
}