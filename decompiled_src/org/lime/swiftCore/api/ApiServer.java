package org.lime.swiftCore.api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.lime.swiftCore.SwiftCore;

public class ApiServer {
   private final SwiftCore plugin;
   private final ApiConfig config;
   private final ApiHandler handler;
   private final Gson gson;
   private HttpServer server;
   private ScheduledExecutorService executor;
   private final Map<String, ApiServer.RateLimitEntry> rateLimitMap = new ConcurrentHashMap<>();

   public ApiServer(SwiftCore var1) {
      this.plugin = var1;
      this.config = new ApiConfig(var1.getConfig());
      this.handler = new ApiHandler(var1);
      this.gson = new Gson();
   }

   public boolean start() {
      if (!this.config.isEnabled()) {
         this.plugin.getLogger().info("[API] REST API is disabled in config.");
         return false;
      } else if (!this.config.getApiKey().isEmpty() && !this.config.getApiKey().equals("change-me-to-a-secure-key")) {
         try {
            this.server = HttpServer.create(new InetSocketAddress(this.config.getPort()), 0);
            this.executor = Executors.newScheduledThreadPool(2, var0 -> {
               Thread var1 = new Thread(var0, "SwiftCore-API");
               var1.setDaemon(true);
               return var1;
            });
            this.server.setExecutor(this.executor);
            this.server.createContext("/api/", this::handleRequest);
            this.executor.scheduleAtFixedRate(this::cleanupRateLimits, 60L, 60L, TimeUnit.SECONDS);
            this.server.start();
            this.plugin.getLogger().info("[API] REST API started on port " + this.config.getPort());
            return true;
         } catch (IOException var2) {
            this.plugin.getLogger().severe("[API] Failed to start REST API: " + var2.getMessage());
            return false;
         }
      } else {
         this.plugin.getLogger().warning("[API] API key is not set! Please configure api.api-key in config.yml");
         return false;
      }
   }

   public void stop() {
      if (this.server != null) {
         this.server.stop(1);
         this.server = null;
         this.plugin.getLogger().info("[API] REST API stopped.");
      }

      if (this.executor != null) {
         this.executor.shutdownNow();
         this.executor = null;
      }

      this.rateLimitMap.clear();
   }

   private void handleRequest(HttpExchange var1) throws IOException {
      try {
         if ("OPTIONS".equalsIgnoreCase(var1.getRequestMethod())) {
            this.addCorsHeaders(var1);
            var1.sendResponseHeaders(204, -1L);
            return;
         }

         if (!"GET".equalsIgnoreCase(var1.getRequestMethod())) {
            this.sendJson(var1, 405, ApiHandler.errorJson(405, "Method not allowed"));
            return;
         }

         this.addCorsHeaders(var1);
         String var2 = var1.getRequestHeaders().getFirst("X-API-Key");
         if (var2 == null || !var2.equals(this.config.getApiKey())) {
            this.sendJson(var1, 401, ApiHandler.errorJson(401, "Unauthorized"));
            return;
         }

         String var3 = var1.getRemoteAddress().getAddress().getHostAddress();
         if (this.isRateLimited(var3)) {
            this.sendJson(var1, 429, ApiHandler.errorJson(429, "Rate limit exceeded"));
            return;
         }

         URI var4 = var1.getRequestURI();
         String var5 = var4.getPath();
         Map var6 = this.parseQuery(var4.getRawQuery());
         JsonObject var7 = this.routeRequest(var5, var6);
         int var8 = 200;
         if (var7.has("error") && var7.get("error").getAsBoolean()) {
            var8 = var7.get("code").getAsInt();
         }

         this.sendJson(var1, var8, var7);
      } catch (Exception var10) {
         this.plugin.getLogger().warning("[API] Error handling request: " + var10.getMessage());

         try {
            this.sendJson(var1, 500, ApiHandler.errorJson(500, "Internal server error"));
         } catch (Exception var9) {
         }
      }
   }

   private JsonObject routeRequest(String var1, Map<String, String> var2) {
      if (var1.startsWith("/api/player/")) {
         String var12 = var1.substring("/api/player/".length()).trim();
         String[] var16 = var12.split("/");
         String var17 = var16.length > 0 ? var16[0].trim() : "";
         if (var17.isEmpty()) {
            return ApiHandler.errorJson(400, "Invalid player identifier");
         } else if (var16.length == 3 && var16[1].equalsIgnoreCase("elo") && var16[2].equalsIgnoreCase("history")) {
            int var19 = this.parseIntOrDefault((String)var2.get("days"), 30);
            int var20 = this.parseIntOrDefault((String)var2.get("limit"), 100);
            int var8 = this.parseIntOrDefault((String)var2.get("offset"), 0);
            return this.handler.handlePlayerEloHistory(var17, var19, var20, var8, (String)var2.get("kit"), (String)var2.get("scope"));
         } else if (var16.length == 3 && var16[1].equalsIgnoreCase("elo") && var16[2].equalsIgnoreCase("summary")) {
            int var18 = this.parseIntOrDefault((String)var2.get("days"), 30);
            return this.handler.handlePlayerEloSummary(var17, var18);
         } else if (var16.length == 2 && var16[1].equalsIgnoreCase("matches")) {
            int var6 = this.parseIntOrDefault((String)var2.get("limit"), 30);
            int var7 = this.parseIntOrDefault((String)var2.get("offset"), 0);
            return this.handler.handlePlayerMatchHistory(var17, var6, var7, (String)var2.get("kit"), (String)var2.get("ranked"));
         } else {
            return var16.length > 1 ? ApiHandler.errorJson(404, "Endpoint not found") : this.handler.handlePlayerLookup(var17);
         }
      } else if (var1.equals("/api/leaderboard/global")) {
         int var11 = this.parseIntOrDefault((String)var2.get("limit"), 50);
         int var15 = this.parseIntOrDefault((String)var2.get("offset"), 0);
         return this.handler.handleGlobalLeaderboard(var11, var15);
      } else if (var1.startsWith("/api/leaderboard/kit/")) {
         String var10 = var1.substring("/api/leaderboard/kit/".length()).trim();
         if (!var10.isEmpty() && !var10.contains("/")) {
            int var14 = this.parseIntOrDefault((String)var2.get("limit"), 50);
            int var5 = this.parseIntOrDefault((String)var2.get("offset"), 0);
            return this.handler.handleKitLeaderboard(var10, var14, var5);
         } else {
            return ApiHandler.errorJson(400, "Invalid kit name");
         }
      } else if (var1.equals("/api/leaderboard/kills")) {
         int var9 = this.parseIntOrDefault((String)var2.get("limit"), 50);
         int var13 = this.parseIntOrDefault((String)var2.get("offset"), 0);
         return this.handler.handleKillsLeaderboard(var9, var13);
      } else if (var1.equals("/api/leaderboard/wins")) {
         int var3 = this.parseIntOrDefault((String)var2.get("limit"), 50);
         int var4 = this.parseIntOrDefault((String)var2.get("offset"), 0);
         return this.handler.handleWinsLeaderboard(var3, var4);
      } else {
         return var1.equals("/api/server/info") ? this.handler.handleServerInfo() : ApiHandler.errorJson(404, "Endpoint not found");
      }
   }

   private void sendJson(HttpExchange var1, int var2, JsonObject var3) throws IOException {
      byte[] var4 = this.gson.toJson(var3).getBytes(StandardCharsets.UTF_8);
      var1.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
      var1.sendResponseHeaders(var2, (long)var4.length);

      try (OutputStream var5 = var1.getResponseBody()) {
         var5.write(var4);
      }
   }

   private void addCorsHeaders(HttpExchange var1) {
      var1.getResponseHeaders().set("Access-Control-Allow-Origin", this.config.getCorsOrigin());
      var1.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, OPTIONS");
      var1.getResponseHeaders().set("Access-Control-Allow-Headers", "X-API-Key, Content-Type");
      var1.getResponseHeaders().set("Access-Control-Max-Age", "3600");
   }

   private boolean isRateLimited(String var1) {
      long var2 = System.currentTimeMillis();
      ApiServer.RateLimitEntry var4 = this.rateLimitMap.get(var1);
      if (var4 == null) {
         this.rateLimitMap.put(var1, new ApiServer.RateLimitEntry(var2));
         return false;
      } else if (var2 - var4.windowStart > 60000L) {
         var4.count = 1;
         var4.windowStart = var2;
         return false;
      } else {
         var4.count++;
         return var4.count > this.config.getRateLimit();
      }
   }

   private void cleanupRateLimits() {
      long var1 = System.currentTimeMillis();
      this.rateLimitMap.entrySet().removeIf(var2 -> var1 - var2.getValue().windowStart > 120000L);
   }

   private Map<String, String> parseQuery(String var1) {
      HashMap var2 = new HashMap();
      if (var1 != null && !var1.isEmpty()) {
         for (String var6 : var1.split("&")) {
            int var7 = var6.indexOf(61);
            if (var7 > 0) {
               var2.put(var6.substring(0, var7), var6.substring(var7 + 1));
            }
         }

         return var2;
      } else {
         return var2;
      }
   }

   private int parseIntOrDefault(String var1, int var2) {
      if (var1 == null) {
         return var2;
      } else {
         try {
            return Integer.parseInt(var1);
         } catch (NumberFormatException var4) {
            return var2;
         }
      }
   }

   public ApiConfig getConfig() {
      return this.config;
   }

   private static class RateLimitEntry {
      int count = 1;
      long windowStart;

      RateLimitEntry(long var1) {
         this.windowStart = var1;
      }
   }
}
