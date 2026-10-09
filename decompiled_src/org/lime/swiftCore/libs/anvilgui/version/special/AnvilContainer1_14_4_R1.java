package org.lime.swiftCore.libs.anvilgui.version.special;

import net.minecraft.server.v1_14_R1.BlockPosition;
import net.minecraft.server.v1_14_R1.ChatComponentText;
import net.minecraft.server.v1_14_R1.ContainerAccess;
import net.minecraft.server.v1_14_R1.ContainerAnvil;
import net.minecraft.server.v1_14_R1.EntityHuman;
import net.minecraft.server.v1_14_R1.IChatBaseComponent;
import net.minecraft.server.v1_14_R1.IInventory;
import net.minecraft.server.v1_14_R1.Slot;
import net.minecraft.server.v1_14_R1.World;
import org.bukkit.craftbukkit.v1_14_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.lime.swiftCore.libs.anvilgui.version.VersionWrapper;

public class AnvilContainer1_14_4_R1 extends ContainerAnvil implements VersionWrapper.AnvilContainerWrapper {
   public AnvilContainer1_14_4_R1(Player var1, int var2, IChatBaseComponent var3) {
      super(var2, ((CraftPlayer)var1).getHandle().inventory, ContainerAccess.at(((CraftWorld)var1.getWorld()).getHandle(), new BlockPosition(0, 0, 0)));
      this.checkReachable = false;
      this.setTitle(var3);
   }

   public void e() {
      Slot var1 = this.getSlot(2);
      if (!var1.hasItem()) {
         Slot var2 = this.getSlot(0);
         if (var2.hasItem()) {
            var1.set(var2.getItem().cloneItemStack());
         }
      }

      this.levelCost.set(0);
      this.c();
   }

   public void b(EntityHuman var1) {
   }

   protected void a(EntityHuman var1, World var2, IInventory var3) {
   }

   public int getContainerId() {
      return this.windowId;
   }

   @Override
   public String getRenameText() {
      return this.renameText;
   }

   @Override
   public void setRenameText(String var1) {
      Slot var2 = this.getSlot(0);
      if (var2.hasItem()) {
         var2.getItem().a(new ChatComponentText(var1));
      }
   }

   @Override
   public Inventory getBukkitInventory() {
      return this.getBukkitView().getTopInventory();
   }
}
