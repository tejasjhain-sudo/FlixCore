package org.lime.swiftCore.api;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.inventory.ItemStack;
import org.lime.swiftCore.arena.ArenaType;
import org.lime.swiftCore.kit.KitRule;

public class CustomKitAPI {
   private final Map<UUID, CustomKitAPI.CustomKitData> pendingKits = new ConcurrentHashMap<>();

   public void registerKit(UUID var1, String var2, ItemStack[] var3, ItemStack[] var4, ArenaType var5) {
      this.registerKit(var1, var2, var3, var4, var5, this.defaultRules(), 1);
   }

   public void registerKit(UUID var1, String var2, ItemStack[] var3, ItemStack[] var4, ArenaType var5, Map<KitRule, Boolean> var6, int var7) {
      this.pendingKits
         .put(
            var1,
            new CustomKitAPI.CustomKitData(
               var2 != null ? var2 : "Custom Kit",
               var3 != null ? (ItemStack[])var3.clone() : new ItemStack[41],
               var4 != null ? (ItemStack[])var4.clone() : new ItemStack[4],
               var5,
               this.copyRules(var6),
               Math.max(1, var7)
            )
         );
   }

   public void registerKit(UUID var1, ItemStack[] var2, ItemStack[] var3, ArenaType var4) {
      this.registerKit(var1, "Custom Kit", var2, var3, var4);
   }

   public CustomKitAPI.CustomKitData getKit(UUID var1) {
      return this.pendingKits.get(var1);
   }

   public CustomKitAPI.CustomKitData removeKit(UUID var1) {
      return this.pendingKits.remove(var1);
   }

   public boolean hasKit(UUID var1) {
      return this.pendingKits.containsKey(var1);
   }

   private Map<KitRule, Boolean> defaultRules() {
      EnumMap var1 = new EnumMap<>(KitRule.class);

      for (KitRule var5 : KitRule.values()) {
         var1.put(var5, var5.getDefaultValue());
      }

      return var1;
   }

   private Map<KitRule, Boolean> copyRules(Map<KitRule, Boolean> var1) {
      Map var2 = this.defaultRules();
      if (var1 != null) {
         var2.putAll(var1);
      }

      return var2;
   }

   public static record CustomKitData(String displayName, ItemStack[] contents, ItemStack[] armor, ArenaType arenaType, Map<KitRule, Boolean> rules, int rounds) {
   }
}
