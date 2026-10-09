package org.lime.swiftCore.libs.anvilgui.version.special;

import net.minecraft.core.BlockPosition;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.world.IInventory;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.inventory.ContainerAccess;
import net.minecraft.world.inventory.ContainerAnvil;
import net.minecraft.world.inventory.Slot;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.lime.swiftCore.libs.anvilgui.version.VersionWrapper;

public class AnvilContainer1_19_1_R1 extends ContainerAnvil implements VersionWrapper.AnvilContainerWrapper {
   public AnvilContainer1_19_1_R1(Player var1, int var2, IChatBaseComponent var3) {
      super(var2, ((CraftPlayer)var1).getHandle().fA(), ContainerAccess.a(((CraftWorld)var1.getWorld()).getHandle(), new BlockPosition(0, 0, 0)));
      this.checkReachable = false;
      this.setTitle(var3);
   }

   public void l() {
      Slot var1 = this.b(2);
      if (!var1.f()) {
         var1.e(this.b(0).e().o());
      }

      this.w.a(0);
      this.b();
      this.d();
   }

   public void b(EntityHuman var1) {
   }

   protected void a(EntityHuman var1, IInventory var2) {
   }

   @Override
   public String getRenameText() {
      return this.v;
   }

   @Override
   public void setRenameText(String var1) {
      Slot var2 = this.b(0);
      if (var2.f()) {
         var2.e().a(IChatBaseComponent.b(var1));
      }
   }

   public int getContainerId() {
      return this.j;
   }

   @Override
   public Inventory getBukkitInventory() {
      return this.getBukkitView().getTopInventory();
   }
}
