package org.lime.swiftCore.libs.anvilgui.version;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public interface VersionWrapper {
   int getNextContainerId(Player var1, VersionWrapper.AnvilContainerWrapper var2);

   void handleInventoryCloseEvent(Player var1);

   void sendPacketOpenWindow(Player var1, int var2, Object var3);

   void sendPacketCloseWindow(Player var1, int var2);

   void sendPacketExperienceChange(Player var1, int var2);

   void setActiveContainerDefault(Player var1);

   void setActiveContainer(Player var1, VersionWrapper.AnvilContainerWrapper var2);

   void setActiveContainerId(VersionWrapper.AnvilContainerWrapper var1, int var2);

   void addActiveContainerSlotListener(VersionWrapper.AnvilContainerWrapper var1, Player var2);

   VersionWrapper.AnvilContainerWrapper newContainerAnvil(Player var1, Object var2);

   default boolean isCustomTitleSupported() {
      return true;
   }

   Object literalChatComponent(String var1);

   Object jsonChatComponent(String var1);

   public interface AnvilContainerWrapper {
      default String getRenameText() {
         return null;
      }

      default void setRenameText(String var1) {
      }

      Inventory getBukkitInventory();
   }
}
