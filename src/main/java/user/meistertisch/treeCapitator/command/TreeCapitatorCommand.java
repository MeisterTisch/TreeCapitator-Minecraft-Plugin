package user.meistertisch.treeCapitator.command;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jspecify.annotations.NonNull;
import user.meistertisch.treeCapitator.TreeCapitator;
import user.meistertisch.treeCapitator.manager.ConfigManager;

import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class TreeCapitatorCommand implements TabExecutor {
    private final TreeCapitator plugin;

    public TreeCapitatorCommand(TreeCapitator plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, @NonNull Command command, @NonNull String label, String @NonNull [] args) {
        // TODO: Set permission check here

        if (args.length == 0) {
            // TODO: open GUI here
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> {
                plugin.getConfigManager().reload();
                sender.sendMessage(plugin.getLang().getMessage("command.tc.reloaded"));
                return true;
            }
            case "settings" -> {
                Component message = plugin.getLang().getMessage("command.tc.settings.message");
                message = message.appendNewline().append(plugin.getConfigManager().getAllSettings());
                sender.sendMessage(message);
                return true;
            }
            case "toggle" -> {
                // TODO: Here toggle
            }
            case "set" -> {

            }
        }

        return true;
    }

    private void handleSet(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("§cBenutzung: /tc set <einstellung> <wert>");
            return;
        }

        ConfigManager cm = plugin.getConfigManager();
        String setting = args[1].toLowerCase();
        String value = args[2];

        try {
            switch (setting) {
                case "speed":
                    cm.setSpeed(Integer.parseInt(value));
                    break;
                case "limit":
                    cm.setLimit(Integer.parseInt(value));
                    break;
                case "onlyaxe":
                    cm.setOnlyAxe(Boolean.parseBoolean(value));
                    break;
                // ... weitere Fälle ...
            }
            sender.sendMessage("§aEinstellung §e" + setting + " §awurde auf §e" + value + " §ageändert.");
        } catch (NumberFormatException e) {
            sender.sendMessage("§cBitte gib eine gültige Zahl ein!");
        }
    }

    @Override
    public List<String> onTabComplete(@NonNull CommandSender sender, @NonNull Command command, @NonNull String alias, String[] args) {
        if (args.length == 1) {
            return Stream.of("reload", "set", "settings", "toggle")
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .toList();
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("set")) {
            return Stream.of("language", "onlyAxe", "onlySurvival", "speed", "limit", "treeDetection", "status")
                    .filter(s -> s.startsWith(args[1]))
                    .toList();
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("set")) {
            String setting = args[1].toLowerCase();

            switch (setting) {
                case "onlysurvival", "onlyaxe", "status":
                    return List.of("enable", "disable");
                case "speed":
                    return List.of("1", "2", "3", "4", "5");
                case "limit":
                    return List.of("32", "64", "128", "256");
                case "treedetection":
                    return Stream.of("mode", "deep", "status")
                            .filter(s -> s.startsWith(args[2].toLowerCase()))
                            .toList();
                case "language":
                    return List.of("en", "de");
            }
        }

        if (args.length == 4 && args[1].equalsIgnoreCase("treeDetection")) {
            String subSetting = args[2].toLowerCase();

            if (subSetting.equals("mode")) {
                return Stream.of("leaf", "coreprotect")
                        .filter(s -> s.startsWith(args[3].toLowerCase()))
                        .toList();
            }
            if (subSetting.equals("deep") || subSetting.equals("status")) {
                return List.of("enable", "disable");
            }
        }

        return Collections.emptyList();
    }
}
