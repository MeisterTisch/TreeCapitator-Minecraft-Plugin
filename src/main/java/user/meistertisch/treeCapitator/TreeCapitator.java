package user.meistertisch.treeCapitator;

import org.bukkit.plugin.java.JavaPlugin;
import user.meistertisch.treeCapitator.api.CoreProtectHook;
import user.meistertisch.treeCapitator.api.LogChecker;
import user.meistertisch.treeCapitator.api.VanillaLogChecker;
import user.meistertisch.treeCapitator.event.EventBlockBreak;
import user.meistertisch.treeCapitator.manager.ConfigManager;
import user.meistertisch.treeCapitator.manager.LanguageManager;

public final class TreeCapitator extends JavaPlugin {
    static TreeCapitator plugin;
    private LogChecker logChecker;
    private LanguageManager lang;
    private ConfigManager configManager;

    @Override
    public void onEnable() {
        plugin = this;

        // for config.yml file
        this.saveDefaultConfig();
        this.configManager = new ConfigManager(this);

        String langCode = getConfig().getString("language", "en");
        this.lang = new LanguageManager(langCode);

        if (getServer().getPluginManager().getPlugin("CoreProtect") != null) {
            this.logChecker = new CoreProtectHook();
            getComponentLogger().info(lang.getMessage("coreprotect.hook_activated"));

            if(((CoreProtectHook) this.logChecker).isCoreProtectTreeGrowthEnabled()) {
                getComponentLogger().warn(lang.getMessage("coreprotect.tree_growth_logging_enabled"));
            }
        } else {
            this.logChecker = new VanillaLogChecker();
            getComponentLogger().warn(lang.getMessage("coreprotect.hook_not_found"));
        }

        getServer().getPluginManager().registerEvents(new EventBlockBreak(), this);

        getComponentLogger().info(lang.getMessage("plugin_enabled"));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static TreeCapitator getPlugin() {
        return plugin;
    }

    public LogChecker getLogChecker() {
        return logChecker;
    }

    public LanguageManager getLang() {
        if(lang == null) {
            this.lang = new LanguageManager("en");
        }

        return lang;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}
