package user.meistertisch.treeCapitator.manager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import user.meistertisch.treeCapitator.TreeCapitator;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Scanner;

public class UpdateChecker implements Listener {
    private final TreeCapitator plugin;
    private String latestVersion;
    private String downloadUrl;
    private boolean checkedOnce;

    public UpdateChecker(TreeCapitator plugin) {
        this.plugin = plugin;
        this.checkedOnce = false;
        this.latestVersion = null;
        this.downloadUrl = null;
    }

    /**
     * Check for updates asynchronously on startup
     */
    public void checkForUpdates() {
        if (checkedOnce) {
            return;
        }

        checkedOnce = true;

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                plugin.getComponentLogger().info(plugin.getLang().getMessage("update.check_started"));

                fetchLatestRelease();

                if (latestVersion != null && isNewVersionAvailable()) {
                    plugin.getComponentLogger().info(plugin.getLang().getMessage("update.new_version_available",
                            Placeholder.unparsed("version", latestVersion),
                            Placeholder.unparsed("url", downloadUrl != null ? downloadUrl : "")
                    ));
                } else if (latestVersion != null) {
                    plugin.getComponentLogger().info(plugin.getLang().getMessage("update.already_latest"));
                }
            } catch (IOException e) {
                String errorKey = getLocalizedErrorKey(e.getMessage());
                String errorMsg;

                if (errorKey.equals("update.error.api_error")) {
                    String httpCode = extractHttpCode(e.getMessage());
                    errorMsg = plugin.getLang().getMessage(errorKey,
                            Placeholder.unparsed("code", httpCode)
                    ).toString();
                } else {
                    errorMsg = plugin.getLang().getMessage(errorKey).toString();
                }

                plugin.getComponentLogger().warn(plugin.getLang().getMessage("update.check_failed",
                        Placeholder.unparsed("error", errorMsg)
                ));
            } catch (Exception e) {
                plugin.getComponentLogger().warn(plugin.getLang().getMessage("update.check_failed",
                        Placeholder.unparsed("error", e.getMessage())
                ));
            } finally {
                // Log plugin enabled message after update check completes
                plugin.getComponentLogger().info(plugin.getLang().getMessage("plugin_enabled"));
            }
        });
    }

    /**
     * Fetch the latest stable (non-prerelease) release information from GitHub API
     */
    private void fetchLatestRelease() throws IOException {
        URI uri = URI.create("https://api.github.com/repos/MeisterTisch/TreeCapitator-Minecraft-Plugin/releases");
        HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);

        int responseCode = connection.getResponseCode();

        if (responseCode == 404) {
            throw new IOException("No releases found in repository");
        }

        if (responseCode != 200) {
            throw new IOException("GitHub API error (HTTP " + responseCode + ")");
        }

        String jsonResponse = new Scanner(connection.getInputStream()).useDelimiter("\\A").next();
        connection.disconnect();

        // Find the first stable (non-prerelease) release
        findStableRelease(jsonResponse);

        // Clean up version (remove 'v' prefix if present)
        if (latestVersion != null && latestVersion.startsWith("v")) {
            latestVersion = latestVersion.substring(1);
        }
    }

    /**
     * Find the first stable (non-prerelease) release from the releases array
     */
    private void findStableRelease(String jsonResponse) {
        // The response is a JSON array [{ ... }, { ... }, ...]
        int index = 0;
        boolean hasAnyReleases = false;
        String latestPrereleaseVersion = null;

        while (index < jsonResponse.length()) {
            int releaseStart = jsonResponse.indexOf("{", index);
            if (releaseStart == -1) break;

            // Count braces to find the complete JSON object (handles nested objects)
            int braceCount = 0;
            int releaseEnd = releaseStart;

            for (int i = releaseStart; i < jsonResponse.length(); i++) {
                char c = jsonResponse.charAt(i);
                if (c == '{') braceCount++;
                else if (c == '}') braceCount--;

                if (braceCount == 0) {
                    releaseEnd = i;
                    break;
                }
            }

            String releaseObj = jsonResponse.substring(releaseStart, releaseEnd + 1);
            hasAnyReleases = true;

            // Check if this release is not a prerelease
            if (releaseObj.contains("\"prerelease\":false")) {
                latestVersion = extractJsonValue(releaseObj, "tag_name");
                downloadUrl = extractJsonValue(releaseObj, "html_url");
                return;
            } else if (latestPrereleaseVersion == null) {
                // Save the first prerelease we find
                latestPrereleaseVersion = extractJsonValue(releaseObj, "tag_name");
            }

            index = releaseEnd + 1;
        }

        // Inform user if only prerelease versions are available
        if (hasAnyReleases && latestPrereleaseVersion != null) {
            plugin.getComponentLogger().warn(plugin.getLang().getMessage("update.only_prerelease_available",
                    Placeholder.unparsed("version", latestPrereleaseVersion)
            ));
        }
    }

    /**
     * Simple JSON value extractor without external dependencies
     */
    private String extractJsonValue(String json, String key) {
        String searchKey = "\"" + key + "\":\"";
        int startIndex = json.indexOf(searchKey);

        if (startIndex == -1) {
            return null;
        }

        startIndex += searchKey.length();
        int endIndex = json.indexOf("\"", startIndex);

        return json.substring(startIndex, endIndex);
    }

    /**
     * Compare versions: returns true if latestVersion > currentVersion
     */
    private boolean isNewVersionAvailable() {
        String currentVersion = plugin.getPluginMeta().getVersion();
        return compareVersions(latestVersion, currentVersion) > 0;
    }

    /**
     * Compare two semantic versions
     * Returns: 1 if v1 > v2, -1 if v1 < v2, 0 if equal
     */
    private int compareVersions(String v1, String v2) {
        String[] parts1 = v1.split("\\.");
        String[] parts2 = v2.split("\\.");

        int maxLength = Math.max(parts1.length, parts2.length);

        for (int i = 0; i < maxLength; i++) {
            int num1 = i < parts1.length ? Integer.parseInt(parts1[i]) : 0;
            int num2 = i < parts2.length ? Integer.parseInt(parts2[i]) : 0;

            if (num1 > num2) return 1;
            if (num1 < num2) return -1;
        }

        return 0;
    }

    /**
     * Register event listener to notify admins when they join
     */
    public void registerListener() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Notify admins on join event
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (latestVersion != null && isNewVersionAvailable() && player.isOp()) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                Component message = plugin.getLang().getMessage("update_available",
                        Placeholder.unparsed("version", latestVersion),
                        Placeholder.unparsed("url", downloadUrl != null ? downloadUrl : "")
                );
                player.sendMessage(message);
            }, 20L); // 1 second delay to ensure player is fully loaded
        }
    }

    /**
     * Map error messages to localized message keys
     */
    private String getLocalizedErrorKey(String errorMessage) {
        if (errorMessage.contains("No releases found")) {
            return "update.error.no_releases";
        } else if (errorMessage.contains("GitHub API error")) {
            return "update.error.api_error";
        }
        return "update.check_failed";
    }

    /**
     * Extract HTTP code from error message
     */
    private String extractHttpCode(String errorMessage) {
        // Message format: "GitHub API error (HTTP <code>)"
        int startIndex = errorMessage.indexOf("HTTP ") + 5;
        int endIndex = errorMessage.indexOf(")", startIndex);
        if (startIndex > 4 && endIndex > startIndex) {
            return errorMessage.substring(startIndex, endIndex);
        }
        return "UNKNOWN";
    }
}








