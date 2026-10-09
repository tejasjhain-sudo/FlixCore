package org.lime.swiftCore.libs.lightcore;

import org.bukkit.plugin.java.JavaPlugin;
import org.lime.swiftCore.libs.lightcore.api.StartupMessage;

public final class LightCore extends JavaPlugin {
   public void onEnable() {
      StartupMessage.printWithAscii("LightCore", "&#FB7208");
   }

   public void onDisable() {
   }
}
