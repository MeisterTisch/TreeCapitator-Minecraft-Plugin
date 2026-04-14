package user.meistertisch.treeCapitator;

import com.sun.source.tree.Tree;
import org.bukkit.plugin.java.JavaPlugin;
import user.meistertisch.treeCapitator.event.EventBlockBreak;

public final class TreeCapitator extends JavaPlugin {
    static TreeCapitator plugin;

    @Override
    public void onEnable() {
        plugin = this;

        getServer().getPluginManager().registerEvents(new EventBlockBreak(), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static TreeCapitator getPlugin() {
        return plugin;
    }
}
