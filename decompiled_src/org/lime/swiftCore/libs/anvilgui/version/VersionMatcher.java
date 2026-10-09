package org.lime.swiftCore.libs.anvilgui.version;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;

public class VersionMatcher {
   private static final Map<String, String> VERSION_TO_REVISION = new HashMap<String, String>() {
      {
         this.put("1.20", "1_20_R1");
         this.put("1.20.1", "1_20_R1");
         this.put("1.20.2", "1_20_R2");
         this.put("1.20.3", "1_20_R3");
         this.put("1.20.4", "1_20_R3");
         this.put("1.20.5", "1_20_R4");
         this.put("1.20.6", "1_20_R4");
         this.put("1.21", "1_21_R1");
         this.put("1.21.1", "1_21_R1");
         this.put("1.21.2", "1_21_R2");
         this.put("1.21.3", "1_21_R2");
         this.put("1.21.4", "1_21_R3");
         this.put("1.21.5", "1_21_R4");
         this.put("1.21.6", "1_21_R5");
         this.put("1.21.7", "1_21_R5");
         this.put("1.21.8", "1_21_R5");
         this.put("1.21.9", "1_21_R6");
         this.put("1.21.10", "1_21_R6");
         this.put("1.21.11", "1_21_R7");
         this.put("26.1", "26_R1");
         this.put("26.1.1", "26_R1");
      }
   };
   private static final String FALLBACK_REVISION = "26_R1";

   public VersionWrapper match() {
      String var1 = Bukkit.getServer().getClass().getPackage().getName();
      String var2;
      if (!var1.contains(".v")) {
         String var3 = Bukkit.getBukkitVersion().split("-")[0];
         var2 = VERSION_TO_REVISION.getOrDefault(var3, "26_R1");
      } else {
         var2 = var1.split("\\.")[3].substring(1);
      }

      try {
         return (VersionWrapper)Class.forName(this.getClass().getPackage().getName() + ".Wrapper" + var2).getDeclaredConstructor().newInstance();
      } catch (ClassNotFoundException var4) {
         throw new IllegalStateException("AnvilGUI does not support server version \"" + var2 + "\"", var4);
      } catch (ReflectiveOperationException var5) {
         throw new IllegalStateException("Failed to instantiate version wrapper for version " + var2, var5);
      }
   }
}
