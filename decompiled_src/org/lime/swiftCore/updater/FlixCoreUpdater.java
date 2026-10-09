package org.lime.swiftCore.updater;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FlixCoreUpdater implements Listener {

    private static String currentCommit = "unknown";
    private static String currentBranch = "main";
    private static String currentVersion = "4.7.0";
    private static String latestRemoteCommit = null;
    private static String latestRemoteMessage = null;
    private static boolean updateAvailable = false;
    private static JavaPlugin plugin;

    public static void init(JavaPlugin pl) {
        plugin = pl;
        loadLocalVersion();
        printStartupBanner();
        Bukkit.getPluginManager().registerEvents(new FlixCoreUpdater(), pl);
        checkRemoteAsync();
    }

    private static void loadLocalVersion() {
        try (InputStream is = FlixCoreUpdater.class.getResourceAsStream("/git-version.properties")) {
            if (is != null) {
                Properties p = new Properties();
                p.load(is);
                currentCommit = p.getProperty("git.commit", "unknown");
                currentBranch = p.getProperty("git.branch", "main");
                currentVersion = p.getProperty("git.version", "4.7.0");
            }
        } catch (Exception ignored) {}
    }

    public static void printStartupBanner() {
        String c1 = "§b"; // Aqua #00E5FF
        String c2 = "§3"; // Dark Cyan #0091EA
        String cW = "§f";
        String cG = "§7";
        String cS = "§a";

        Bukkit.getConsoleSender().sendMessage("");
        Bukkit.getConsoleSender().sendMessage(c1 + "  ______ _ _       _____                 ");
        Bukkit.getConsoleSender().sendMessage(c1 + " |  ____| (_)     / ____|                ");
        Bukkit.getConsoleSender().sendMessage(c1 + " | |__  | |___  _| |     ___  _ __ ___   ");
        Bukkit.getConsoleSender().sendMessage(c1 + " |  __| | | \\ \\/ / |    / _ \\| '__/ _ \\  ");
        Bukkit.getConsoleSender().sendMessage(c1 + " | |    | | |>  <| |___| (_) | | |  __/  ");
        Bukkit.getConsoleSender().sendMessage(c1 + " |_|    |_|_/_/\\_\\\\_____\\___/|_|  \\___|  ");
        Bukkit.getConsoleSender().sendMessage("");
        Bukkit.getConsoleSender().sendMessage(c2 + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        Bukkit.getConsoleSender().sendMessage(c1 + " • Version:      " + cW + "v" + currentVersion + " " + cG + "(Commit: " + c1 + currentCommit + cG + ")");
        Bukkit.getConsoleSender().sendMessage(c1 + " • Branch:       " + cW + currentBranch + " " + cG + "(github.com/tejasjhain-sudo/FlixCore)");
        Bukkit.getConsoleSender().sendMessage(c1 + " • Authors:      " + cW + "Lime & FlixCore Team");
        Bukkit.getConsoleSender().sendMessage(c1 + " • Auto-Updater: " + cS + "ENABLED " + cG + "(Fetches new version on server restart)");
        Bukkit.getConsoleSender().sendMessage(c2 + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        Bukkit.getConsoleSender().sendMessage("");
    }

    public static void checkRemoteAsync() {
        if (plugin == null) return;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                URL url = URI.create("https://api.github.com/repos/tejasjhain-sudo/FlixCore/commits/main").toURL();
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", "FlixCore-Updater");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                if (conn.getResponseCode() == 200) {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            sb.append(line);
                        }
                        String json = sb.toString();

                        // Parse commit sha
                        Pattern shaPattern = Pattern.compile("\"sha\":\\s*\"([a-f0-9]{7,40})\"");
                        Matcher m = shaPattern.matcher(json);
                        if (m.find()) {
                            String fullSha = m.group(1);
                            String shortSha = fullSha.substring(0, Math.min(7, fullSha.length()));
                            latestRemoteCommit = shortSha;

                            // Parse commit message
                            Pattern msgPattern = Pattern.compile("\"message\":\\s*\"([^\"]+)\"");
                            Matcher mm = msgPattern.matcher(json);
                            if (mm.find()) {
                                latestRemoteMessage = mm.group(1).replace("\\n", " ");
                            }

                            if (!"unknown".equalsIgnoreCase(currentCommit) && !shortSha.equalsIgnoreCase(currentCommit)) {
                                updateAvailable = true;
                                Bukkit.getConsoleSender().sendMessage("§3[§bFlixCore Updater§3] §e═════════════════════════════════════════════════");
                                Bukkit.getConsoleSender().sendMessage("§3[§bFlixCore Updater§3] §b>>> NEW UPDATE DETECTED ON GITHUB! <<<");
                                Bukkit.getConsoleSender().sendMessage("§3[§bFlixCore Updater§3] §eLatest Commit: §f" + shortSha + " §7(Current: " + currentCommit + ")");
                                if (latestRemoteMessage != null) {
                                    Bukkit.getConsoleSender().sendMessage("§3[§bFlixCore Updater§3] §eMessage: §f" + latestRemoteMessage);
                                }
                                Bukkit.getConsoleSender().sendMessage("§3[§bFlixCore Updater§3] §aThis update will be automatically applied on next server restart!");
                                Bukkit.getConsoleSender().sendMessage("§3[§bFlixCore Updater§3] §e═════════════════════════════════════════════════");
                            } else {
                                Bukkit.getConsoleSender().sendMessage("§3[§bFlixCore Updater§3] §aFlixCore is up-to-date with GitHub (commit: " + shortSha + ").");
                            }
                        }
                    }
                }
            } catch (Exception e) {
                // Offline or rate limit - silent
            }
        });
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("flixcore.admin") || player.isOp()) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (updateAvailable) {
                    player.sendMessage("§3[§bFlixCore§3] §eA new update (§b" + latestRemoteCommit + "§e) was found on GitHub!");
                    player.sendMessage("§3[§bFlixCore§3] §aIt will automatically install on next server restart.");
                }
            }, 40L);
        }
    }

    public static String getCurrentCommit() { return currentCommit; }
    public static String getLatestRemoteCommit() { return latestRemoteCommit; }
    public static boolean isUpdateAvailable() { return updateAvailable; }
}
