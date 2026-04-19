package user.meistertisch.treeCapitator.api;

import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class CoreProtectHook implements LogChecker {
    private CoreProtectAPI getAPI() {
        Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("CoreProtect");
        if (plugin instanceof CoreProtect cp) {
            return cp.getAPI();
        }
        return null;
    }

    @Override
    public boolean isPlayerPlaced(Block block) {
        CoreProtectAPI api = getAPI();
        if (api == null) return false;

        List<String[]> lookup = api.blockLookup(block, 31536000);
        if (lookup != null) {
            for (String[] result : lookup) {
                if (api.parseResult(result).getActionId() == 1) {
                    return true;
                }
            }
        }
        return false;
    }
}
