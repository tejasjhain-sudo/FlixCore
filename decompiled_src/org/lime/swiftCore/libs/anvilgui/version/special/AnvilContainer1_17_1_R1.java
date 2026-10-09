package org.lime.swiftCore.libs.anvilgui.version.special;

import net.minecraft.core.BlockPosition;
import net.minecraft.network.chat.ChatComponentText;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.world.IInventory;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.inventory.ContainerAccess;
import net.minecraft.world.inventory.ContainerAnvil;
import net.minecraft.world.inventory.Slot;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.lime.swiftCore.libs.anvilgui.version.VersionWrapper;

public class AnvilContainer1_17_1_R1 extends ContainerAnvil implements VersionWrapper.AnvilContainerWrapper {
   public AnvilContainer1_17_1_R1(Player var1, int var2, IChatBaseComponent var3) {
      super(var2, ((CraftPlayer)var1).getHandle().getInventory(), ContainerAccess.at(((CraftWorld)var1.getWorld()).getHandle(), new BlockPosition(0, 0, 0)));
      this.checkReachable = false;
      this.setTitle(var3);
   }

   public void l() {
      Slot var1 = this.getSlot(2);
      if (!var1.hasItem()) {
         Slot var2 = this.getSlot(0);
         if (var2.hasItem()) {
            var1.set(var2.getItem().cloneItemStack());
         }
      }

      this.w.set(0);
      this.updateInventory();
      this.d();
   }

   public void b(EntityHuman var1) {
   }

   protected void a(EntityHuman var1, IInventory var2) {
   }

   public int getContainerId() {
      return this.j;
   }

   @Override
   public String getRenameText() {
      return this.v;
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
