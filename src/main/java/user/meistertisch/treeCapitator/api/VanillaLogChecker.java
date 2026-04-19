package user.meistertisch.treeCapitator.api;

import org.bukkit.Tag;
import org.bukkit.block.Block;

public class VanillaLogChecker implements LogChecker {
    @Override
    public boolean isPlayerPlaced(Block block) {
        // Leaf-Checker
        for (int i = 1; i <= 25; i++) {
            Block above = block.getRelative(0, i, 0);
            if (Tag.LEAVES.isTagged(above.getType())) return false; // Ist ein Baum
            if (!Tag.LOGS.isTagged(above.getType()) && !above.getType().isAir()) break;
        }
        return true; // No leafs found, probably player-placed
    }
}
