package user.meistertisch.treeCapitator.api;

import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import user.meistertisch.treeCapitator.TreeCapitator;

import java.io.File;
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

    public boolean isCoreProtectTreeGrowthEnabled() {
        // Get config file from CoreProtect: /plugins/CoreProtect/config.yml
        File cpConfigFile = new File(TreeCapitator.getPlugin().getDataFolder().getParentFile(), "CoreProtect/config.yml");

        if (!cpConfigFile.exists()) return false;

        FileConfiguration cpConfig = YamlConfiguration.loadConfiguration(cpConfigFile);

        // Check for boolean. Default is true
        return cpConfig.getBoolean("tree-growth", true);
    }
}
