package user.meistertisch.treeCapitator.event;

import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;
import user.meistertisch.treeCapitator.TreeCapitator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class EventBlockDropItem implements Listener {
    private static final Set<Block> BROKEN_BLOCKS = new HashSet<>();

    @EventHandler
    public void onBlockDrop(BlockDropItemEvent event) {
        if(!TreeCapitator.getPlugin().getConfigManager().enabled) {
            return;
        }

        if(!TreeCapitator.getPlugin().getConfigManager().dropToInv) {
            return;
        }

        Block block = event.getBlock();
        Player player = event.getPlayer();

        if(!BROKEN_BLOCKS.contains(block)) {
            return;
        }

        BROKEN_BLOCKS.remove(block);
        event.setCancelled(true);

        HashMap<Integer, ItemStack> overflow = new HashMap<>();
        for(Item item : event.getItems()) {
            ItemStack itemStack = item.getItemStack();
            overflow.putAll(
                    player.getInventory().addItem(itemStack)
            );
        }

        for(ItemStack item : overflow.values()) {
            block.getWorld().dropItem(block.getLocation().add(0.5,0.5,0.5), item);
        }
    }

    public static void addBlock(Block block){
        BROKEN_BLOCKS.add(block);
    }
}
