package org.lime.swiftCore.libs.anvilgui.version;

import net.minecraft.core.BlockPosition;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.chat.IChatBaseComponent.ChatSerializer;
import net.minecraft.network.protocol.game.PacketPlayOutCloseWindow;
import net.minecraft.network.protocol.game.PacketPlayOutExperience;
import net.minecraft.network.protocol.game.PacketPlayOutOpenWindow;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.IInventory;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.inventory.Container;
import net.minecraft.world.inventory.ContainerAccess;
import net.minecraft.world.inventory.ContainerAnvil;
import net.minecraft.world.inventory.Containers;
import net.minecraft.world.inventory.Slot;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R1.event.CraftEventFactory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public final class Wrapper1_20_R1 implements VersionWrapper {
   private int getRealNextContainerId(Player var1) {
      return this.toNMS(var1).nextContainerCounter();
   }

   private EntityPlayer toNMS(Player var1) {
      return ((CraftPlayer)var1).getHandle();
   }

   @Override
   public int getNextContainerId(Player var1, VersionWrapper.AnvilContainerWrapper var2) {
      return ((Wrapper1_20_R1.AnvilContainer)var2).getContainerId();
   }

   @Override
   public void handleInventoryCloseEvent(Player var1) {
      CraftEventFactory.handleInventoryCloseEvent(this.toNMS(var1));
      this.toNMS(var1).r();
   }

   @Override
   public void sendPacketOpenWindow(Player var1, int var2, Object var3) {
      this.toNMS(var1).c.a(new PacketPlayOutOpenWindow(var2, Containers.h, (IChatBaseComponent)var3));
   }

   @Override
   public void sendPacketCloseWindow(Player var1, int var2) {
      this.toNMS(var1).c.a(new PacketPlayOutCloseWindow(var2));
   }

   @Override
   public void sendPacketExperienceChange(Player var1, int var2) {
      this.toNMS(var1).c.a(new PacketPlayOutExperience(0.0F, 0, var2));
   }

   @Override
   public void setActiveContainerDefault(Player var1) {
      this.toNMS(var1).bR = this.toNMS(var1).bQ;
   }

   @Override
   public void setActiveContainer(Player var1, VersionWrapper.AnvilContainerWrapper var2) {
      this.toNMS(var1).bR = (Container)var2;
   }

   @Override
   public void setActiveContainerId(VersionWrapper.AnvilContainerWrapper var1, int var2) {
   }

   @Override
   public void addActiveContainerSlotListener(VersionWrapper.AnvilContainerWrapper var1, Player var2) {
      this.toNMS(var2).a((Container)var1);
   }

   @Override
   public VersionWrapper.AnvilContainerWrapper newContainerAnvil(Player var1, Object var2) {
      return new Wrapper1_20_R1.AnvilContainer(var1, this.getRealNextContainerId(var1), (IChatBaseComponent)var2);
   }

   @Override
   public Object literalChatComponent(String var1) {
      return IChatBaseComponent.b(var1);
   }

   @Override
   public Object jsonChatComponent(String var1) {
      return ChatSerializer.a(var1);
   }

   private static class AnvilContainer extends ContainerAnvil implements VersionWrapper.AnvilContainerWrapper {
      public AnvilContainer(Player var1, int var2, IChatBaseComponent var3) {
         super(var2, ((CraftPlayer)var1).getHandle().fN(), ContainerAccess.a(((CraftWorld)var1.getWorld()).getHandle(), new BlockPosition(0, 0, 0)));
         this.checkReachable = false;
         this.setTitle(var3);
      }

      public void m() {
         Slot var1 = this.b(2);
         if (!var1.f()) {
            var1.e(this.b(0).e().p());
         }

         this.w.a(0);
         this.b();
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
         Slot var2 = this.b(0);
         if (var2.f()) {
            var2.e().a(IChatBaseComponent.b(var1));
         }
      }

      @Override
      public Inventory getBukkitInventory() {
         return this.getBukkitView().getTopInventory();
      }
   }
}
