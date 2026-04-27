package user.meistertisch.treeCapitator.permission;

import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import user.meistertisch.treeCapitator.TreeCapitator;

public class PermissionRegistry {
    private final TreeCapitator plugin;

    public PermissionRegistry(TreeCapitator plugin) {
        this.plugin = plugin;
    }

    public void registerAll() {
        register(Permissions.ADMIN, "Allows access to all admin commands and features.", PermissionDefault.OP);
        register(Permissions.USE, "Allows players to use the main features of the plugin.", PermissionDefault.TRUE);
        register(Permissions.RELOAD, "Allows reloading the plugin's configuration without restarting the server.", PermissionDefault.OP);
        register(Permissions.TOGGLE, "Allows players to toggle the plugin's features on or off for themselves.",  PermissionDefault.TRUE);
        register(Permissions.SETTINGS, "Allows access to see the current settings of the plugin.", PermissionDefault.OP);
        register(Permissions.SET, "Allows changing the plugin's settings via commands or GUI.", PermissionDefault.OP);
    }

    private void register(String name, String description, PermissionDefault defaultValue) {
        Permission perm = new Permission(name, description, defaultValue);
        plugin.getServer().getPluginManager().addPermission(perm);
    }
}
