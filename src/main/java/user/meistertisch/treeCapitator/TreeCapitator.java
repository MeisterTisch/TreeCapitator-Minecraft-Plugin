package user.meistertisch.treeCapitator;

import org.bukkit.plugin.java.JavaPlugin;
import user.meistertisch.treeCapitator.api.CoreProtectHook;
import user.meistertisch.treeCapitator.api.LogChecker;
import user.meistertisch.treeCapitator.api.VanillaLogChecker;
import user.meistertisch.treeCapitator.command.TreeCapitatorCommand;
import user.meistertisch.treeCapitator.event.EventBlockBreak;
import user.meistertisch.treeCapitator.manager.ConfigManager;
import user.meistertisch.treeCapitator.manager.LanguageManager;
import user.meistertisch.treeCapitator.permission.PermissionRegistry;

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

        setupLogChecker();

        // Permissions
        new PermissionRegistry(this).registerAll();

        // Events
        getServer().getPluginManager().registerEvents(new EventBlockBreak(), this);

        // Commands
        getCommand("treecapitator").setExecutor(new TreeCapitatorCommand(this));

        getComponentLogger().info(lang.getMessage("plugin_enabled"));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public void reloadLogChecker(){ setupLogChecker(); }

    private void setupLogChecker() {
        int mode = configManager.treeDetectionMode;

        if(!configManager.treeDetectionEnabled){
            return;
        }

        // Mode 2 = CoreProtect
        if (mode == 2) {
            if (getServer().getPluginManager().getPlugin("CoreProtect") != null) {
                this.logChecker = new CoreProtectHook();
                getComponentLogger().info(lang.getMessage("coreprotect.hook_activated"));

                if (((CoreProtectHook) this.logChecker).isCoreProtectTreeGrowthEnabled()) {
                    getComponentLogger().warn(lang.getMessage("coreprotect.tree_growth_logging_enabled"));
                }
            } else {
                getComponentLogger().error(lang.getMessage("coreprotect.hook_not_found"));
                this.logChecker = new VanillaLogChecker();
                configManager.setTreeDetectionMode(1);
                configManager.setTreeDetectionEnabled(false);
            }
        } else if(mode == 1) {
            this.logChecker = new VanillaLogChecker();
            getComponentLogger().info(lang.getMessage("tree_detection.leaf_mode"));
        }
    }

    // Getters
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
