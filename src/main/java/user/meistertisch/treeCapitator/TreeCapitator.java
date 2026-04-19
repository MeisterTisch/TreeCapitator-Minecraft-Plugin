package user.meistertisch.treeCapitator;

import org.bukkit.plugin.java.JavaPlugin;
import user.meistertisch.treeCapitator.api.CoreProtectHook;
import user.meistertisch.treeCapitator.api.LogChecker;
import user.meistertisch.treeCapitator.api.VanillaLogChecker;
import user.meistertisch.treeCapitator.event.EventBlockBreak;

public final class TreeCapitator extends JavaPlugin {
    static TreeCapitator plugin;
    private LogChecker logChecker;
    private LanguageManager lang;

    @Override
    public void onEnable() {
        plugin = this;

        // for config.yml file
        this.saveDefaultConfig();


        if (getServer().getPluginManager().getPlugin("CoreProtect") != null) {
            this.logChecker = new CoreProtectHook();
            getLogger().info("CoreProtect Hook aktiviert!"); //TODO: Change to properties file
        } else {
            this.logChecker = new VanillaLogChecker();
            getLogger().info("CoreProtect nicht gefunden, nutze Blätter-Check."); //TODO: Change to properties file
        }

        String langCode = getConfig().getString("language", "en");
        this.lang = new LanguageManager(langCode);

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
        return lang;
    }
}
