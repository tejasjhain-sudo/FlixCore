package org.lime.swiftCore.libs.anvilgui.version;

import net.minecraft.core.BlockPosition;
import net.minecraft.network.chat.ChatComponentText;
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
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_17_R1.event.CraftEventFactory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.lime.swiftCore.libs.anvilgui.version.special.AnvilContainer1_17_1_R1;

public class Wrapper1_17_R1 implements VersionWrapper {
   private final boolean IS_ONE_SEVENTEEN_ONE = Bukkit.getBukkitVersion().contains("1.17.1");

   private int getRealNextContainerId(Player var1) {
      return this.toNMS(var1).nextContainerCounter();
   }

   @Override
   public int getNextContainerId(Player var1, VersionWrapper.AnvilContainerWrapper var2) {
      return this.IS_ONE_SEVENTEEN_ONE ? ((AnvilContainer1_17_1_R1)var2).getContainerId() : ((Wrapper1_17_R1.AnvilContainer)var2).getContainerId();
   }

   @Override
   public void handleInventoryCloseEvent(Player var1) {
      CraftEventFactory.handleInventoryCloseEvent(this.toNMS(var1));
      this.toNMS(var1).o();
   }

   @Override
   public void sendPacketOpenWindow(Player var1, int var2, Object var3) {
      this.toNMS(var1).b.sendPacket(new PacketPlayOutOpenWindow(var2, Containers.h, (IChatBaseComponent)var3));
   }

   @Override
   public void sendPacketCloseWindow(Player var1, int var2) {
      this.toNMS(var1).b.sendPacket(new PacketPlayOutCloseWindow(var2));
   }

   @Override
   public void sendPacketExperienceChange(Player var1, int var2) {
      this.toNMS(var1).b.sendPacket(new PacketPlayOutExperience(0.0F, 0, var2));
   }

   @Override
   public void setActiveContainerDefault(Player var1) {
      this.toNMS(var1).bV = this.toNMS(var1).bU;
   }

   @Override
   public void setActiveContainer(Player var1, VersionWrapper.AnvilContainerWrapper var2) {
      this.toNMS(var1).bV = (Container)var2;
   }

   @Override
   public void setActiveContainerId(VersionWrapper.AnvilContainerWrapper var1, int var2) {
   }

   @Override
   public void addActiveContainerSlotListener(VersionWrapper.AnvilContainerWrapper var1, Player var2) {
      this.toNMS(var2).initMenu((Container)var1);
   }

   @Override
   public VersionWrapper.AnvilContainerWrapper newContainerAnvil(Player var1, Object var2) {
      return (VersionWrapper.AnvilContainerWrapper)(this.IS_ONE_SEVENTEEN_ONE
         ? new AnvilContainer1_17_1_R1(var1, this.getRealNextContainerId(var1), (IChatBaseComponent)var2)
         : new Wrapper1_17_R1.AnvilContainer(var1, (IChatBaseComponent)var2));
   }

   @Override
   public Object literalChatComponent(String var1) {
      return new ChatComponentText(var1);
   }

   @Override
   public Object jsonChatComponent(String var1) {
      return ChatSerializer.a(var1);
   }

   private EntityPlayer toNMS(Player var1) {
      return ((CraftPlayer)var1).getHandle();
   }

   private class AnvilContainer extends ContainerAnvil implements VersionWrapper.AnvilContainerWrapper {
      public AnvilContainer(Player var2, IChatBaseComponent var3) {
         super(
            Wrapper1_17_R1.this.getRealNextContainerId(var2),
            ((CraftPlayer)var2).getHandle().getInventory(),
            ContainerAccess.at(((CraftWorld)var2.getWorld()).getHandle(), new BlockPosition(0, 0, 0))
         );
         this.checkReachable = false;
         this.setTitle(var3);
      }

      public void i() {
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
}
