package user.meistertisch.treeCapitator.event;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import user.meistertisch.treeCapitator.TreeCapitator;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;


public class EventBlockBreak implements Listener {
    private static final Set<String> PROCESSING_BLOCKS = new HashSet<>();

    //each category includes: oak, birch, spruce, jungle, dark oak, acacia, cherry, pale oak, mangrove, warped and crimson stem/hyphae.
    private static final Set<Material> LOGS = Set.of(
            Material.OAK_LOG, Material.BIRCH_LOG, Material.SPRUCE_LOG, Material.JUNGLE_LOG, Material.DARK_OAK_LOG, Material.ACACIA_LOG, Material.CHERRY_LOG, Material.PALE_OAK_LOG, Material.MANGROVE_LOG, Material.WARPED_STEM, Material.CRIMSON_STEM
    );
    private static final Set<Material> STRIPPED_LOGS = Set.of(
            Material.STRIPPED_OAK_LOG, Material.STRIPPED_BIRCH_LOG, Material.STRIPPED_SPRUCE_LOG, Material.STRIPPED_JUNGLE_LOG, Material.STRIPPED_DARK_OAK_LOG, Material.STRIPPED_ACACIA_LOG, Material.STRIPPED_CHERRY_LOG, Material.STRIPPED_PALE_OAK_LOG, Material.STRIPPED_MANGROVE_LOG, Material.STRIPPED_WARPED_STEM, Material.STRIPPED_CRIMSON_STEM
    );
    private static final Set<Material> WOOD = Set.of(
        Material.OAK_WOOD, Material.BIRCH_WOOD, Material.SPRUCE_WOOD, Material.JUNGLE_WOOD, Material.DARK_OAK_WOOD, Material.ACACIA_WOOD, Material.CHERRY_WOOD, Material.PALE_OAK_WOOD, Material.MANGROVE_WOOD, Material.WARPED_HYPHAE, Material.CRIMSON_HYPHAE
    );
    private static final Set<Material> STRIPPED_WOOD = Set.of(
        Material.STRIPPED_OAK_WOOD, Material.STRIPPED_BIRCH_WOOD, Material.STRIPPED_SPRUCE_WOOD, Material.STRIPPED_JUNGLE_WOOD, Material.STRIPPED_DARK_OAK_WOOD, Material.STRIPPED_ACACIA_WOOD, Material.STRIPPED_CHERRY_WOOD, Material.STRIPPED_PALE_OAK_WOOD, Material.STRIPPED_MANGROVE_WOOD, Material.STRIPPED_WARPED_HYPHAE, Material.STRIPPED_CRIMSON_HYPHAE
    );

    /**
     * Create a unique key for a block location
     */
    private String getBlockKey(Block block) {
        return block.getWorld().getName() + ":" + block.getX() + "," + block.getY() + "," + block.getZ();
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event){
        if(!TreeCapitator.getPlugin().getConfigManager().enabled) {
            return;
        }

        String blockKey = getBlockKey(event.getBlock());
        if (PROCESSING_BLOCKS.contains(blockKey)) {
            PROCESSING_BLOCKS.remove(blockKey);
            return;
        }

        Block block = event.getBlock();
        Material blockType = block.getType();

        // Is block a part of enabled log type?
        if(!isLogTypeAllowed(blockType)) return;

        Player player = event.getPlayer();
        NamespacedKey key = new NamespacedKey(TreeCapitator.getPlugin(), "treecapitator_enabled");
        boolean isEnabled = player.getPersistentDataContainer().getOrDefault(key, PersistentDataType.BOOLEAN, true);

        // Did player disable individual preference?
        if(!isEnabled){
            player.sendActionBar(TreeCapitator.getPlugin().getLang().getMessage("command.tc.toggle.disabled"));
            return;
        }

        // Is player in survival
        boolean onlySurvival = TreeCapitator.getPlugin().getConfigManager().onlySurvival;
        if (onlySurvival && player.getGameMode() != GameMode.SURVIVAL) {
            return;
        }

        ItemStack tool = player.getInventory().getItemInMainHand();

        // Has player an axe?
        boolean onlyAxe = TreeCapitator.getPlugin().getConfigManager().onlyAxe;
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
        boolean detectTrees = TreeCapitator.getPlugin().getConfigManager().treeDetectionEnabled;
        if (detectTrees && TreeCapitator.getPlugin().getLogChecker().isPlayerPlaced(block)) {
            return;
        }

        int limit = TreeCapitator.getPlugin().getConfigManager().limit;
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
        boolean detectTrees = TreeCapitator.getPlugin().getConfigManager().treeDetectionEnabled;
        boolean isDeep = TreeCapitator.getPlugin().getConfigManager().treeDetectionDeep;
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

        // Save block type for later check
        Material blockType = block.getType();

        // Mark block as being processed to prevent recursion
        String blockKey = getBlockKey(block);
        PROCESSING_BLOCKS.add(blockKey);
        EventBlockDropItem.addBlock(block);

        block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, block.getType());
        player.breakBlock(block);

        for (int y = -1; y <= 1; y++) {
            for (int z = -1; z <= 1; z++) {
                for (int x = -1; x <= 1; x++) {
                    //Skip the original block
                    if(x == 0 && y == 0 && z == 0) {
                        continue;
                    }

                    // Get speed from config and check in if it's in range; TODO: Fix corner speeds (Too fast with neighbor blocks somehow, will check later)
                    long speed = TreeCapitator.getPlugin().getConfigManager().speed;
                    if(speed == 0){
                        speed = getDynamicSpeed(tool);
                    }

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

    private long getDynamicSpeed(ItemStack tool) {
        if (tool == null || tool.getType().isAir()) return 20;

        long speed;

        switch (tool.getType()) {
            case NETHERITE_AXE -> speed = 2;
            case DIAMOND_AXE   -> speed = 4;
            case IRON_AXE      -> speed = 6;
            case GOLDEN_AXE    -> speed = 3;
            case COPPER_AXE    -> speed = 10;
            case STONE_AXE     -> speed = 12;
            case WOODEN_AXE    -> speed = 16;
            default            -> speed = 20;
        }

        int level = tool.getEnchantmentLevel(Enchantment.EFFICIENCY);
        double factor = 1.0 - (level * 0.15);

        long finalSpeed = (long) (speed * factor);

        return Math.max(1, finalSpeed);
    }

    private boolean isLogTypeAllowed(Material blockType) {
        if(TreeCapitator.getPlugin().getConfigManager().logTypeLog && LOGS.contains(blockType)){
            return true;
        }

        if(TreeCapitator.getPlugin().getConfigManager().logTypeStrippedLog && STRIPPED_LOGS.contains(blockType)){
            return true;
        }

        if(TreeCapitator.getPlugin().getConfigManager().logTypeWood && WOOD.contains(blockType)){
            return true;
        }

        if(TreeCapitator.getPlugin().getConfigManager().logTypeStrippedWood && STRIPPED_WOOD.contains(blockType)){
            return true;
        }

        return false;
    }
}