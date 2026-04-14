package user.meistertisch.treeCapitator.event;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import user.meistertisch.treeCapitator.TreeCapitator;


public class EventBlockBreak implements Listener {
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event){
        Material blockType = event.getBlock().getType();

        // TODO: Change Check to block tag check instead of materialbtop
        if(blockType == Material.OAK_LOG) {
            destroyBlock(event.getBlock(), event.getPlayer().getInventory().getItemInMainHand());
        }
    }

    private void destroyBlock(Block block, ItemStack tool){
        Material blockType = block.getType();
        block.breakNaturally(tool, true, true); // TODO: Durability not reducing yet

        Block[] neighbors = getBlockRelatives(block);

        for(Block neighborBlock : neighbors){
            if(neighborBlock.getType() == blockType){
                Bukkit.getScheduler().runTaskLater(TreeCapitator.getPlugin(), () -> {
                    //TODO: Important: Need a cancel if no neighboring block is the same type.
//                    destroyBlock(neighborBlock, tool);
                }, 10L);
            }
        }
    }

    private Block[] getBlockRelatives(Block block){
        Block[] neighbors = new Block[6];

        neighbors[0] = block.getRelative(BlockFace.DOWN);
        neighbors[1] = block.getRelative(BlockFace.UP);
        neighbors[2] = block.getRelative(BlockFace.NORTH);
        neighbors[3] = block.getRelative(BlockFace.EAST);
        neighbors[4] = block.getRelative(BlockFace.SOUTH);
        neighbors[5] = block.getRelative(BlockFace.WEST);

        return neighbors;
    }
}
