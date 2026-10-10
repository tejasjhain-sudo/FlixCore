package club.aspvp.license;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.NetworkInterface;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Enumeration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class LicenseClient {

    // Direct Supabase Edge Function Endpoint (100% Serverless)
    private static final String SUPABASE_VERIFY_URL = "https://oeqrvkyrqtwcdvfrxcaz.supabase.co/functions/v1/license-verify";

    // Supabase Public Anonymous API Key
    private static final String SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im9lcXJ2a3lycXR3Y2R2ZnJ4Y2F6Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3OTE0NjQyOTEsImV4cCI6MjEwNzA0MDI5MX0.1lVvwGS2kD8xPh5etSogzyPz4hiQf0dphAqPG_aEaXs";

    private final Plugin plugin;
    private final String productId;
    private final String licenseKey;
    private final long gracePeriodMillis = 72L * 3600 * 1000; // 72 hours offline grace period
    private final File cacheFile;
    private final File identityFile;
    private String serverId;
    private boolean verified = false;

    public LicenseClient(Plugin plugin, String productId, String licenseKey) {
        this.plugin = plugin;
        this.productId = productId != null ? productId : "2";
        this.licenseKey = licenseKey != null ? licenseKey.trim().toUpperCase() : "";

        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        this.cacheFile = new File(dataFolder, ".license_cache.dat");
        this.identityFile = new File(dataFolder, ".server_identity.dat");

        initServerId();
    }

    private void initServerId() {
        if (identityFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(identityFile))) {
                String line = reader.readLine();
                if (line != null && !line.trim().isEmpty()) {
                    this.serverId = line.trim();
                    return;
                }
            } catch (IOException ignored) {}
        }

        try {
            StringBuilder sb = new StringBuilder();
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            if (interfaces != null) {
                while (interfaces.hasMoreElements()) {
                    NetworkInterface ni = interfaces.nextElement();
                    byte[] mac = ni.getHardwareAddress();
                    if (mac != null) {
                        for (byte b : mac) sb.append(String.format("%02X", b));
                        break;
                    }
                }
            }
            sb.append(System.getProperty("user.name", ""));
            sb.append(System.getProperty("os.name", ""));
            sb.append(UUID.randomUUID().toString());

            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(sb.toString().getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) hex.append(String.format("%02x", b));

            this.serverId = "srv-" + hex.substring(0, 24);

            try (FileWriter writer = new FileWriter(identityFile)) {
                writer.write(this.serverId);
            }
        } catch (Exception ex) {
            this.serverId = "srv-" + UUID.randomUUID().toString().replace("-", "").substring(0, 24);
        }
    }

    public CompletableFuture<Boolean> verifyOnStartup() {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        if (licenseKey.isEmpty() || licenseKey.contains("XXXX") || "YOUR_LICENSE_KEY_HERE".equalsIgnoreCase(licenseKey)) {
            plugin.getLogger().severe("=================================================");
            plugin.getLogger().severe("[FlixStudios] NO VALID LICENSE KEY FOUND!");
            plugin.getLogger().severe("[FlixStudios] Please enter your key in plugins/" + plugin.getName() + "/config.yml");
            plugin.getLogger().severe("=================================================");
            Bukkit.getScheduler().runTask(plugin, () -> Bukkit.getPluginManager().disablePlugin(plugin));
            future.complete(false);
            return future;
        }

        plugin.getLogger().info("[FlixStudios] Authenticating product license with Supabase...");

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            VerificationResult result = performVerification();

            if (result.isValid()) {
                this.verified = true;
                saveSuccessCache();
                plugin.getLogger().info("[FlixStudios] License verified successfully. Status: ACTIVE.");
                future.complete(true);
            } else if (result.isNetworkError() && isWithinGracePeriod()) {
                this.verified = true;
                long remainingHours = getRemainingGracePeriodHours();
                plugin.getLogger().warning("[FlixStudios] Licensing server temporarily unreachable. Operating under grace period ("
                        + remainingHours + "h remaining).");
                future.complete(true);
            } else {
                this.verified = false;
                plugin.getLogger().severe("=================================================");
                plugin.getLogger().severe("[FlixStudios] LICENSE REJECTED! Disabling plugin.");
                plugin.getLogger().severe("[FlixStudios] Reason: " + result.getReason());
                plugin.getLogger().severe("=================================================");

                Bukkit.getScheduler().runTask(plugin, () -> Bukkit.getPluginManager().disablePlugin(plugin));
                future.complete(false);
            }
        });

        return future;
    }

    public void startHeartbeat(long intervalTicks) {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            VerificationResult result = performVerification();
            if (result.isValid()) {
                saveSuccessCache();
            } else if (!result.isNetworkError() || !isWithinGracePeriod()) {
                plugin.getLogger().severe("[FlixStudios] License revoked or expired. Shutting down.");
                Bukkit.getScheduler().runTask(plugin, () -> Bukkit.getPluginManager().disablePlugin(plugin));
            }
        }, intervalTicks, intervalTicks);
    }

    public VerificationResult performVerification() {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(SUPABASE_VERIFY_URL);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + SUPABASE_ANON_KEY);
            conn.setRequestProperty("apikey", SUPABASE_ANON_KEY);
            conn.setRequestProperty("User-Agent", "FlixPluginRuntime/" + productId);
            conn.setConnectTimeout(6000);
            conn.setReadTimeout(6000);
            conn.setDoOutput(true);

            JsonObject payload = new JsonObject();
            payload.addProperty("license_key", licenseKey);
            payload.addProperty("product_id", productId);
            payload.addProperty("server_id", serverId);

            byte[] input = payload.toString().getBytes(StandardCharsets.UTF_8);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(input, 0, input.length);
            }

            int statusCode = conn.getResponseCode();
            InputStream is = (statusCode >= 200 && statusCode < 300) ? conn.getInputStream() : conn.getErrorStream();
            if (is == null) return new VerificationResult(false, "EMPTY_RESPONSE", false);

            BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            StringBuilder responseBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) responseBuilder.append(line);

            JsonObject responseJson = JsonParser.parseString(responseBuilder.toString()).getAsJsonObject();
            boolean valid = responseJson.has("valid") && responseJson.get("valid").getAsBoolean();
            String reason = responseJson.has("reason") ? responseJson.get("reason").getAsString() : (valid ? "ACTIVE" : "UNKNOWN_ERROR");

            return new VerificationResult(valid, reason, false);

        } catch (IOException e) {
            return new VerificationResult(false, "NETWORK_UNAVAILABLE", true);
        } catch (Exception e) {
            return new VerificationResult(false, "INTERNAL_CLIENT_ERROR", true);
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    private void saveSuccessCache() {
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(cacheFile))) {
            dos.writeLong(System.currentTimeMillis());
            dos.writeUTF(licenseKey);
            dos.writeUTF(productId);
            dos.writeUTF(serverId);
        } catch (IOException ignored) {}
    }

    private boolean isWithinGracePeriod() {
        if (!cacheFile.exists()) return false;
        try (DataInputStream dis = new DataInputStream(new FileInputStream(cacheFile))) {
            long lastVerified = dis.readLong();
            String cachedKey = dis.readUTF();
            String cachedProduct = dis.readUTF();
            if (!licenseKey.equalsIgnoreCase(cachedKey) || !productId.equalsIgnoreCase(cachedProduct)) return false;
            long elapsed = System.currentTimeMillis() - lastVerified;
            return elapsed >= 0 && elapsed <= gracePeriodMillis;
        } catch (IOException e) {
            return false;
        }
    }

    private long getRemainingGracePeriodHours() {
        if (!cacheFile.exists()) return 0;
        try (DataInputStream dis = new DataInputStream(new FileInputStream(cacheFile))) {
            long lastVerified = dis.readLong();
            long elapsed = System.currentTimeMillis() - lastVerified;
            return Math.max(0, (gracePeriodMillis - elapsed) / (3600 * 1000));
        } catch (IOException e) {
            return 0;
        }
    }

    public boolean isVerified() {
        return verified;
    }

    public String getServerId() {
        return serverId;
    }

    public String getLicenseKey() {
        return licenseKey;
    }

    public String getProductId() {
        return productId;
    }

    /**
     * Bridge method for SwiftCore AuthGuard hook during onEnable().
     * Enforces license verification on plugin startup before registering any features.
     */
    public static org.lime.swiftCore.libs.authguard.sdk.VerificationResult verifyForAuthGuard(Plugin plugin, String licenseKey) {
        LicenseClient client = new LicenseClient(plugin, "2", licenseKey);

        if (client.licenseKey.isEmpty() || client.licenseKey.contains("XXXX") || "YOUR_LICENSE_KEY_HERE".equalsIgnoreCase(client.licenseKey)) {
            plugin.getLogger().severe("=================================================");
            plugin.getLogger().severe("[FlixStudios] NO VALID LICENSE KEY FOUND!");
            plugin.getLogger().severe("[FlixStudios] Please enter your key in plugins/" + plugin.getName() + "/config.yml");
            plugin.getLogger().severe("=================================================");
            Bukkit.getScheduler().runTask(plugin, () -> Bukkit.getPluginManager().disablePlugin(plugin));
            return new org.lime.swiftCore.libs.authguard.sdk.VerificationResult(false, "NO_VALID_LICENSE_KEY");
        }

        plugin.getLogger().info("[FlixStudios] Authenticating product license with Supabase...");
        VerificationResult result = client.performVerification();

        if (result.isValid()) {
            client.verified = true;
            client.saveSuccessCache();
            plugin.getLogger().info("[FlixStudios] License verified successfully. Status: ACTIVE.");
            client.startHeartbeat(432000L); // 6 hours
            return new org.lime.swiftCore.libs.authguard.sdk.VerificationResult(true, "ACTIVE", "2", "never", "unlimited", "unlimited", "FlixStudios", true);
        } else if (result.isNetworkError() && client.isWithinGracePeriod()) {
            client.verified = true;
            long remainingHours = client.getRemainingGracePeriodHours();
            plugin.getLogger().warning("[FlixStudios] Licensing server temporarily unreachable. Operating under grace period ("
                    + remainingHours + "h remaining).");
            client.startHeartbeat(432000L); // 6 hours
            return new org.lime.swiftCore.libs.authguard.sdk.VerificationResult(true, "GRACE_PERIOD", "2", "never", "unlimited", "unlimited", "FlixStudios", true);
        } else {
            client.verified = false;
            plugin.getLogger().severe("=================================================");
            plugin.getLogger().severe("[FlixStudios] LICENSE REJECTED! Disabling plugin.");
            plugin.getLogger().severe("[FlixStudios] Reason: " + result.getReason());
            plugin.getLogger().severe("=================================================");

            Bukkit.getScheduler().runTask(plugin, () -> Bukkit.getPluginManager().disablePlugin(plugin));
            return new org.lime.swiftCore.libs.authguard.sdk.VerificationResult(false, result.getReason());
        }
    }

    public static class VerificationResult {
        private final boolean valid;
        private final String reason;
        private final boolean networkError;

        public VerificationResult(boolean valid, String reason, boolean networkError) {
            this.valid = valid;
            this.reason = reason;
            this.networkError = networkError;
        }

        public boolean isValid() { return valid; }
        public String getReason() { return reason; }
        public boolean isNetworkError() { return networkError; }
    }
}
