package org.lime.swiftCore.updater;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FlixCoreUpdater implements Listener {

    private static String currentCommit = "unknown";
    private static String currentBranch = "main";
    private static String currentVersion = "4.7.0";
    private static String latestRemoteCommit = null;
    private static String latestRemoteMessage = null;
    private static boolean updateAvailable = false;
    private static boolean updateDownloaded = false;
    private static boolean downloadInProgress = false;
    private static JavaPlugin plugin;

    public static void init(JavaPlugin pl) {
        plugin = pl;
        loadLocalVersion();
        printStartupBanner();
        Bukkit.getPluginManager().registerEvents(new FlixCoreUpdater(), pl);

        // Check for updates asynchronously on startup
        checkRemoteAsync(null);

        // Schedule periodic update check every 30 minutes (36000 ticks)
        Bukkit.getScheduler().runTaskTimerAsynchronously(pl, () -> {
            if (!updateDownloaded && !downloadInProgress) {
                checkRemoteAsync(null);
            }
        }, 36000L, 36000L);
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
        Bukkit.getConsoleSender().sendMessage(c1 + " • Auto-Updater: " + cS + "ENABLED " + cG + "(Auto-downloads updates into plugins/update/)");
        Bukkit.getConsoleSender().sendMessage(c2 + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        Bukkit.getConsoleSender().sendMessage("");
    }

    public static void checkRemoteAsync(CommandSender notifyTarget) {
        if (plugin == null) return;
        if (downloadInProgress) {
            if (notifyTarget != null) {
                notifyTarget.sendMessage("§3[§bFlixCore Updater§3] §eA download is already in progress. Please wait...");
            }
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                if (notifyTarget != null) {
                    notifyTarget.sendMessage("§3[§bFlixCore Updater§3] §7Checking GitHub for newer versions...");
                }

                URL url = URI.create("https://api.github.com/repos/tejasjhain-sudo/FlixCore/commits/main").toURL();
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", "FlixCore-Updater");
                conn.setConnectTimeout(8000);
                conn.setReadTimeout(8000);

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
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
                                Bukkit.getConsoleSender().sendMessage("§3[§bFlixCore Updater§3] §aDownloading update package from GitHub Releases...");
                                Bukkit.getConsoleSender().sendMessage("§3[§bFlixCore Updater§3] §e═════════════════════════════════════════════════");

                                if (notifyTarget != null && !(notifyTarget instanceof org.bukkit.command.ConsoleCommandSender)) {
                                    notifyTarget.sendMessage("§3[§bFlixCore Updater§3] §aNew update found: §b" + shortSha + " §7(Current: §c" + currentCommit + "§7)");
                                    notifyTarget.sendMessage("§3[§bFlixCore Updater§3] §eDownloading update in background...");
                                }

                                // Trigger actual download
                                downloadUpdateAsync(shortSha, notifyTarget);
                            } else {
                                Bukkit.getConsoleSender().sendMessage("§3[§bFlixCore Updater§3] §aFlixCore is up-to-date with GitHub (commit: " + shortSha + ").");
                                if (notifyTarget != null) {
                                    notifyTarget.sendMessage("§3[§bFlixCore Updater§3] §aFlixCore is up-to-date! Current commit: §b" + currentCommit);
                                }
                            }
                        }
                    }
                } else {
                    if (notifyTarget != null) {
                        notifyTarget.sendMessage("§3[§bFlixCore Updater§3] §cGitHub API returned HTTP " + responseCode + " (rate-limited or offline).");
                    }
                }
            } catch (Exception e) {
                if (notifyTarget != null) {
                    notifyTarget.sendMessage("§3[§bFlixCore Updater§3] §cCould not check for updates: " + e.getMessage());
                }
            }
        });
    }

    private static void downloadUpdateAsync(String targetCommit, CommandSender notifyTarget) {
        if (downloadInProgress) return;
        downloadInProgress = true;

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            File tempTarget = null;
            try {
                // Determine current plugin file name
                File currentJarFile = null;
                try {
                    currentJarFile = new File(plugin.getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
                } catch (Exception ignored) {}

                String jarName = "FlixCore.jar";
                if (currentJarFile != null && currentJarFile.getName().endsWith(".jar")) {
                    jarName = currentJarFile.getName();
                }

                // Resolve server plugins/update directory
                File pluginsDir = plugin.getDataFolder().getParentFile();
                String updateFolderName = Bukkit.getUpdateFolder();
                if (updateFolderName == null || updateFolderName.isEmpty()) {
                    updateFolderName = "update";
                }
                File updateFolder = new File(pluginsDir, updateFolderName);
                if (!updateFolder.exists()) {
                    updateFolder.mkdirs();
                }

                File finalTarget = new File(updateFolder, jarName);
                tempTarget = new File(updateFolder, jarName + ".part");

                String downloadUrl = "https://github.com/tejasjhain-sudo/FlixCore/releases/latest/download/FlixCore.jar";
                Bukkit.getConsoleSender().sendMessage("§3[§bFlixCore Updater§3] §7Connecting to release asset: " + downloadUrl);

                try (InputStream in = openStreamWithRedirects(downloadUrl);
                     FileOutputStream out = new FileOutputStream(tempTarget)) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    long totalDownloaded = 0;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                        totalDownloaded += bytesRead;
                    }
                    out.flush();
                }

                // Verify file is a valid jar
                if (tempTarget.length() < 100_000) {
                    throw new IOException("Downloaded file too small (" + tempTarget.length() + " bytes), possibly an error page.");
                }

                try (JarFile testJar = new JarFile(tempTarget)) {
                    if (testJar.getJarEntry("plugin.yml") == null) {
                        throw new IOException("Downloaded file is not a valid plugin jar (missing plugin.yml).");
                    }
                }

                // Move tempTarget to finalTarget (Paper will automatically swap this on startup!)
                try {
                    Files.move(tempTarget.toPath(), finalTarget.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                } catch (Exception e) {
                    Files.move(tempTarget.toPath(), finalTarget.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }

                updateDownloaded = true;
                downloadInProgress = false;

                String successMsg1 = "§3[§bFlixCore Updater§3] §a=================================================";
                String successMsg2 = "§3[§bFlixCore Updater§3] §aSuccessfully downloaded update (commit: " + targetCommit + ")!";
                String successMsg3 = "§3[§bFlixCore Updater§3] §eSaved to: §f" + finalTarget.getPath();
                String successMsg4 = "§3[§bFlixCore Updater§3] §b>>> This update will automatically load on next restart! <<<";
                String successMsg5 = "§3[§bFlixCore Updater§3] §a=================================================";

                Bukkit.getConsoleSender().sendMessage(successMsg1);
                Bukkit.getConsoleSender().sendMessage(successMsg2);
                Bukkit.getConsoleSender().sendMessage(successMsg3);
                Bukkit.getConsoleSender().sendMessage(successMsg4);
                Bukkit.getConsoleSender().sendMessage(successMsg5);

                if (notifyTarget != null && !(notifyTarget instanceof org.bukkit.command.ConsoleCommandSender)) {
                    notifyTarget.sendMessage(successMsg2);
                    notifyTarget.sendMessage(successMsg4);
                }

                // Alert online operators
                for (Player op : Bukkit.getOnlinePlayers()) {
                    if (op.hasPermission("flixcore.admin") || op.isOp()) {
                        op.sendMessage("§3[§bFlixCore§3] §aUpdate §b" + targetCommit + " §adownloaded! Restart server to apply.");
                    }
                }

            } catch (Exception e) {
                downloadInProgress = false;
                if (tempTarget != null && tempTarget.exists()) {
                    tempTarget.delete();
                }
                Bukkit.getConsoleSender().sendMessage("§3[§bFlixCore Updater§3] §cFailed to download update: " + e.getMessage());
                if (notifyTarget != null) {
                    notifyTarget.sendMessage("§3[§bFlixCore Updater§3] §cUpdate download failed: " + e.getMessage());
                }
            }
        });
    }

    private static InputStream openStreamWithRedirects(String initialUrl) throws IOException {
        String currentUrl = initialUrl;
        for (int redirectCount = 0; redirectCount < 8; redirectCount++) {
            URL url = URI.create(currentUrl).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "FlixCore-Updater");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(30000);
            conn.setInstanceFollowRedirects(false);

            int status = conn.getResponseCode();
            if (status == HttpURLConnection.HTTP_MOVED_TEMP
                    || status == HttpURLConnection.HTTP_MOVED_PERM
                    || status == HttpURLConnection.HTTP_SEE_OTHER
                    || status == 307
                    || status == 308) {
                String redirectTarget = conn.getHeaderField("Location");
                if (redirectTarget != null) {
                    currentUrl = redirectTarget;
                    conn.disconnect();
                    continue;
                }
            }

            if (status == HttpURLConnection.HTTP_OK) {
                return conn.getInputStream();
            }

            throw new IOException("HTTP " + status + " while requesting " + currentUrl);
        }
        throw new IOException("Too many HTTP redirects when fetching " + initialUrl);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        String msg = event.getMessage().trim().toLowerCase();
        if (msg.equals("/flixcore update") || msg.equals("/flixcore checkupdate")) {
            Player p = event.getPlayer();
            if (p.hasPermission("flixcore.admin") || p.isOp()) {
                event.setCancelled(true);
                p.sendMessage("§3[§bFlixCore§3] §7Checking for updates...");
                checkRemoteAsync(p);
            }
        } else if (msg.equals("/flixcore version")) {
            event.setCancelled(true);
            Player p = event.getPlayer();
            p.sendMessage("§3━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            p.sendMessage("§b FlixCore §fv" + currentVersion + " §7(Commit: §b" + currentCommit + "§7)");
            p.sendMessage("§7 Branch: §f" + currentBranch);
            p.sendMessage("§7 Status: " + (updateDownloaded ? "§aUpdate downloaded (Restart pending)" : (updateAvailable ? "§eUpdate available" : "§aUp-to-date")));
            p.sendMessage("§3━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onServerCommand(ServerCommandEvent event) {
        String cmd = event.getCommand().trim().toLowerCase();
        if (cmd.equals("flixcore update") || cmd.equals("flixcore checkupdate")) {
            event.setCancelled(true);
            checkRemoteAsync(event.getSender());
        } else if (cmd.equals("flixcore version")) {
            event.setCancelled(true);
            printStartupBanner();
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // 1. If lobby is not configured yet on this server, automatically initialize it to the default world spawn!
        try {
            if (plugin instanceof org.lime.swiftCore.SwiftCore sc) {
                org.lime.swiftCore.spawn.b lobbyMgr = sc.getLobbyManager();
                if (lobbyMgr != null && !lobbyMgr.Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String()) {
                    org.bukkit.World defaultWorld = Bukkit.getWorlds().isEmpty() ? null : Bukkit.getWorlds().get(0);
                    if (defaultWorld != null) {
                        org.bukkit.Location defSpawn = defaultWorld.getSpawnLocation().clone().add(0.5, 0.0, 0.5);
                        lobbyMgr.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(defSpawn);
                        sc.getLogger().info("Automatically initialized lobby location to world '" + defaultWorld.getName() + "' spawn. Run /setlobby anytime to update it.");
                    }
                }
            }
        } catch (Throwable ignored) {}

        // 2. Ensure player gets spawn items if they don't have them (fail-safe for fresh servers)
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;
            try {
                if (plugin instanceof org.lime.swiftCore.SwiftCore sc) {
                    org.lime.swiftCore.spawn.SpawnItemsManager sim = sc.getSpawnItemsManager();
                    if (sim != null && !sim.hasSpawnItems(player)) {
                        // Check if in duel/match or ffa
                        if ((sc.getDuelManager() == null || !sc.getDuelManager().isInMatch(player.getUniqueId()))
                                && (sc.getFFAManager() == null || !sc.getFFAManager().isInFFA(player.getUniqueId()))) {
                            sim.giveSpawnItems(player, "default", false, false);
                        }
                    }
                }
            } catch (Throwable ignored) {}
        }, 15L);

        // 3. Update alert for server admins
        if (player.hasPermission("flixcore.admin") || player.isOp()) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (updateDownloaded) {
                    player.sendMessage("§3[§bFlixCore§3] §aAn update (§b" + latestRemoteCommit + "§a) has been downloaded!");
                    player.sendMessage("§3[§bFlixCore§3] §eRestart the server to apply it.");
                } else if (updateAvailable) {
                    player.sendMessage("§3[§bFlixCore§3] §eA new update (§b" + latestRemoteCommit + "§e) was found on GitHub!");
                    player.sendMessage("§3[§bFlixCore§3] §7Type §b/flixcore update §7to download it now.");
                }
            }, 40L);
        }
    }

    public static String getCurrentCommit() { return currentCommit; }
    public static String getLatestRemoteCommit() { return latestRemoteCommit; }
    public static boolean isUpdateAvailable() { return updateAvailable; }
    public static boolean isUpdateDownloaded() { return updateDownloaded; }
}
