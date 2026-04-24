package user.meistertisch.treeCapitator.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jspecify.annotations.NonNull;
import user.meistertisch.treeCapitator.TreeCapitator;
import user.meistertisch.treeCapitator.manager.ConfigManager;

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
                handleSet(sender, args);
                return true;
            }
        }

        return true;
    }

    private void handleSet(CommandSender sender, String[] args) {
        if (args.length < 3 || args.length > 4) {
            sender.sendMessage(plugin.getLang().getMessage("command.invalid_use"));
            return;
        }

        ConfigManager cm = plugin.getConfigManager();
        String setting = args[1].toLowerCase();
        String value = args[2];

        switch (setting) {
            case "status", "onlyaxe", "onlysurvival" -> {
                if (args.length != 3) {
                    sender.sendMessage(plugin.getLang().getMessage("command.invalid_use"));
                    return;
                }

                boolean current;

                switch (setting) {
                    case "status" -> current = cm.enabled;
                    case "onlyaxe" -> current = cm.onlyAxe;
                    case "onlysurvival" -> current = cm.onlySurvival;
                    default -> {
                        return;
                    }
                }

                // Not a boolean
                if (!value.equalsIgnoreCase("enable") && !value.equalsIgnoreCase("disable")) {
                    sender.sendMessage(plugin.getLang().getMessage(
                            "command.invalid_input",
                            Placeholder.unparsed("input", value)
                    ));
                    return;
                }

                boolean isEnabling = value.equalsIgnoreCase("enable");

                // Same status
                if (isEnabling == current) {
                    sender.sendMessage(plugin.getLang().getMessage(
                            "command.tc.set.already_set",
                            Placeholder.unparsed("value", value),
                            Placeholder.unparsed("setting", setting)
                    ));
                    return;
                }

                switch (setting) {
                    case "status" -> cm.setEnabled(isEnabling);
                    case "onlyaxe" -> cm.setOnlyAxe(isEnabling);
                    case "onlysurvival" -> cm.setOnlySurvival(isEnabling);
                    default -> {
                        return;
                    }
                }
                sender.sendMessage(plugin.getLang().getMessage(
                        "command.tc.set.success", //TODO: Maybe make the text specific to each setting? "status has been set to enabled!" sounds weird
                        Placeholder.unparsed("value", value),
                        Placeholder.unparsed("setting", setting)
                ));
            }
            case "language" -> {
                if (args.length != 3) {
                    sender.sendMessage(plugin.getLang().getMessage("command.invalid_use"));
                    return;
                }

                String current = cm.language;

                // Invalid language
                if (!plugin.getLang().getLanguages().contains(value)) {
                    sender.sendMessage(plugin.getLang().getMessage(
                            "command.invalid_input",
                            Placeholder.unparsed("input", value)
                    ));
                    return;
                }

                // Same language
                if (value.equalsIgnoreCase(current)) {
                    sender.sendMessage(plugin.getLang().getMessage(
                            "command.tc.set.already_set",
                            Placeholder.unparsed("value", value),
                            Placeholder.unparsed("setting", setting)
                    ));
                    return;
                }

                cm.setLanguage(value);
                sender.sendMessage(plugin.getLang().getMessage(
                        "command.tc.set.success",
                        Placeholder.unparsed("value", value),
                        Placeholder.unparsed("setting", setting)
                ));
            }
            case "speed", "limit" -> {
                if (args.length != 3) {
                    sender.sendMessage(plugin.getLang().getMessage("command.invalid_use"));
                    return;
                }

                int current;

                switch (setting) {
                    case "speed" -> current = cm.speed;
                    case "limit" -> current = cm.limit;
                    default -> {
                        return;
                    }
                }

                int intValue;
                // Not an integer
                try {
                    intValue = Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    sender.sendMessage(plugin.getLang().getMessage(
                            "command.invalid_input",
                            Placeholder.unparsed("input", value)
                    ));
                    return;
                }

                // negative value
                if (intValue < 0) {
                    sender.sendMessage(plugin.getLang().getMessage(
                            "command.invalid_input",
                            Placeholder.unparsed("input", value)
                    ));
                    return;
                }

                // If setting to set is speed and value is below 1 or above 5
                if (setting.equalsIgnoreCase("speed")) {
                    if (intValue < 1 || intValue > 5) {
                        sender.sendMessage(plugin.getLang().getMessage(
                                "command.invalid_input",
                                Placeholder.unparsed("input", value)
                        ));
                        return;
                    }
                }

                // Same value
                if (intValue == current) {
                    sender.sendMessage(plugin.getLang().getMessage(
                            "command.tc.set.already_set",
                            Placeholder.unparsed("value", value),
                            Placeholder.unparsed("setting", setting)
                    ));
                    return;
                }

                switch (setting) {
                    case "speed" -> cm.setSpeed(intValue);
                    case "limit" -> cm.setLimit(intValue);
                    default -> {
                        return;
                    }
                }
                sender.sendMessage(plugin.getLang().getMessage(
                        "command.tc.set.success",
                        Placeholder.unparsed("value", value),
                        Placeholder.unparsed("setting", setting)
                ));
            }
            case "treedetection" -> {
                if (args.length != 4) {
                    sender.sendMessage(plugin.getLang().getMessage("command.invalid_use"));
                    return;
                }
                setting = args[2].toLowerCase();
                value = args[3];

                // Mode section
                if (setting.equalsIgnoreCase("mode")) {
                    int current = cm.treeDetectionMode;

                    // Not a String
                    if (!value.equalsIgnoreCase("leaf") && !value.equalsIgnoreCase("coreprotect")) { //TODO: Maybe change this if check to a more generic one if more modes will be added in the future
                        sender.sendMessage(plugin.getLang().getMessage(
                                "command.invalid_input",
                                Placeholder.unparsed("input", value)
                        ));
                        return;
                    }

                    int intToSet;
                    switch (value) {
                        case "leaf" -> intToSet = 1;
                        case "coreprotect" -> intToSet = 2;
                        default -> {
                            return;
                        }
                    }

                    // Same mode
                    if (intToSet == current) {
                        sender.sendMessage(plugin.getLang().getMessage(
                                "command.tc.set.already_set",
                                Placeholder.unparsed("value", value),
                                Placeholder.unparsed("setting", "treeDetection." + setting)
                        ));
                        return;
                    }

                    if (plugin.getServer().getPluginManager().getPlugin("CoreProtect") == null) {
                        sender.sendMessage(plugin.getLang().getMessage("coreprotect.hook_not_found"));
                        cm.setTreeDetectionMode(intToSet);
                        return;
                    }

                    cm.setTreeDetectionMode(intToSet);
                    sender.sendMessage(plugin.getLang().getMessage(
                            "command.tc.set.success",
                            Placeholder.unparsed("value", value),
                            Placeholder.unparsed("setting", "treeDetection." + setting)
                    ));
                } else {
                    boolean current;

                    switch (setting) {
                        case "deep" -> current = cm.treeDetectionDeep;
                        case "status" -> current = cm.treeDetectionEnabled;
                        default -> {
                            sender.sendMessage(plugin.getLang().getMessage("command.invalid_use"));
                            return;
                        }
                    }

                    // Not a boolean
                    if (!value.equalsIgnoreCase("enable") && !value.equalsIgnoreCase("disable")) {
                        sender.sendMessage(plugin.getLang().getMessage(
                                "command.invalid_input",
                                Placeholder.unparsed("input", value)
                        ));
                        return;
                    }

                    boolean isEnabling = value.equalsIgnoreCase("enable");

                    // Same status
                    if (isEnabling == current) {
                        sender.sendMessage(plugin.getLang().getMessage(
                                "command.tc.set.already_set",
                                Placeholder.unparsed("value", value),
                                Placeholder.unparsed("setting", "treeDetection." + setting)
                        ));
                        return;
                    }

                    switch (setting) {
                        case "deep" -> cm.setTreeDetectionDeep(isEnabling);
                        case "status" -> cm.setTreeDetectionEnabled(isEnabling);
                        default -> {
                            sender.sendMessage(plugin.getLang().getMessage("command.invalid_use"));
                            return;
                        }
                    }
                    sender.sendMessage(plugin.getLang().getMessage(
                            "command.tc.set.success",
                            Placeholder.unparsed("value", value),
                            Placeholder.unparsed("setting", "treeDetection." + setting)
                    ));
                }
            }
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
                    return Stream.of("enable", "disable")
                            .filter(s -> s.startsWith(args[2].toLowerCase()))
                            .toList();
                case "speed":
                    return Stream.of("1", "2", "3", "4", "5")
                            .filter(s -> s.startsWith(args[2].toLowerCase()))
                            .toList();
                case "limit":
                    return Stream.of("32", "64", "128", "256")
                            .filter(s -> s.startsWith(args[2].toLowerCase()))
                            .toList();
                case "treedetection":
                    return Stream.of("mode", "deep", "status")
                            .filter(s -> s.startsWith(args[2].toLowerCase()))
                            .toList();
                case "language":
                    return plugin.getLang().getLanguages().stream()
                            .filter(s -> s.startsWith(args[2].toLowerCase()))
                            .toList();
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
                return Stream.of("enable", "disable")
                        .filter(s -> s.startsWith(args[3].toLowerCase()))
                        .toList();
            }
        }

        return Collections.emptyList();
    }
}
