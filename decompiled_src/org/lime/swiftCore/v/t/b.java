package org.lime.swiftCore.v.t;

import java.util.Collections;
import java.util.List;
import org.bukkit.configuration.file.FileConfiguration;
import org.lime.swiftCore.SwiftCore;
import org.lime.swiftCore.v.p.e;

public final class b {
   private b() {
   }

   public static c o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
      SwiftCore var0
   ) {
      FileConfiguration var1 = var0.getConfig();
      boolean var2 = var1.getBoolean("cross_server.enabled", false);
      org.lime.swiftCore.v.b var3 = var2
         ? org.lime.swiftCore.v.b.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object
         : org.lime.swiftCore.v.b.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new;
      String var4 = var1.getString("cross_server.server-name", "lobby-1");

      e var5;
      try {
         var5 = e.valueOf(var1.getString("cross_server.server-type", "LOBBY").toUpperCase());
      } catch (IllegalArgumentException var36) {
         var5 = e.Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class;
      }

      String var6 = var1.getString("cross_server.transfer.host", "");
      int var7 = var1.getInt("cross_server.transfer.port", var0.getServer().getPort());
      String var8 = var1.getString("cross_server.redis.host", "localhost");
      int var9 = var1.getInt("cross_server.redis.port", 6379);
      String var10 = var1.getString("cross_server.redis.password", "");
      int var11 = var1.getInt("cross_server.redis.timeout", 3000);
      int var12 = var1.getInt("cross_server.redis.max-pool-size", 16);
      String var13 = var1.getString("cross_server.redis.channel-prefix", "swiftcore");
      String var14 = var1.getString("cross_server.mysql.host", "localhost");
      int var15 = var1.getInt("cross_server.mysql.port", 3306);
      String var16 = var1.getString("cross_server.mysql.database", "swiftcore");
      String var17 = var1.getString("cross_server.mysql.username", "root");
      String var18 = var1.getString("cross_server.mysql.password", "");
      int var19 = var1.getInt("cross_server.mysql.max-pool-size", 15);
      int var20 = var1.getInt("cross_server.mysql.min-idle", 3);
      int var21 = var1.getInt("cross_server.heartbeat.interval", 5);
      int var22 = var1.getInt("cross_server.heartbeat.timeout", 15);
      boolean var23 = var1.getBoolean("cross_server.transfer.retry-on-failure", true);
      int var24 = var1.getInt("cross_server.transfer.retry-delay", 2);
      int var25 = var1.getInt("cross_server.transfer.max-retries", 3);
      int var26 = var1.getInt("cross_server.transfer.timeout", 10);
      boolean var27 = var1.getBoolean("cross_server.fallback.enabled", true);
      List var28 = var1.getStringList("cross_server.fallback.lobby-priority");
      if (var28 == null) {
         var28 = Collections.emptyList();
      }

      boolean var29 = var1.getBoolean("cross_server.global-tab.enabled", true);
      int var30 = var1.getInt("cross_server.global-tab.update-interval", 3);
      boolean var31 = var1.getBoolean("cross_server.friends.cross-server-messaging", true);
      boolean var32 = var1.getBoolean("cross_server.friends.join-notifications", true);
      boolean var33 = var1.getBoolean("cross_server.friends.switch-notifications", true);
      boolean var34 = var1.getBoolean("cross_server.monitoring.enabled", true);
      String var35 = var1.getString("cross_server.monitoring.permission", "swiftcore.monitor");
      return new c(
         var3,
         var4,
         var5,
         var6,
         var7,
         var8,
         var9,
         var10,
         var11,
         var12,
         var13,
         var14,
         var15,
         var16,
         var17,
         var18,
         var19,
         var20,
         var21,
         var22,
         var23,
         var24,
         var25,
         var26,
         var27,
         var28,
         var29,
         var30,
         var31,
         var32,
         var33,
         var34,
         var35
      );
   }
}
