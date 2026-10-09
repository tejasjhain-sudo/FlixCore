package org.lime.swiftCore.libs.anvilgui.version;

import net.minecraft.server.v1_15_R1.BlockPosition;
import net.minecraft.server.v1_15_R1.ChatComponentText;
import net.minecraft.server.v1_15_R1.Container;
import net.minecraft.server.v1_15_R1.ContainerAccess;
import net.minecraft.server.v1_15_R1.ContainerAnvil;
import net.minecraft.server.v1_15_R1.Containers;
import net.minecraft.server.v1_15_R1.EntityHuman;
import net.minecraft.server.v1_15_R1.EntityPlayer;
import net.minecraft.server.v1_15_R1.IChatBaseComponent;
import net.minecraft.server.v1_15_R1.IInventory;
import net.minecraft.server.v1_15_R1.PacketPlayOutCloseWindow;
import net.minecraft.server.v1_15_R1.PacketPlayOutExperience;
import net.minecraft.server.v1_15_R1.PacketPlayOutOpenWindow;
import net.minecraft.server.v1_15_R1.Slot;
import net.minecraft.server.v1_15_R1.World;
import net.minecraft.server.v1_15_R1.IChatBaseComponent.ChatSerializer;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_15_R1.event.CraftEventFactory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class Wrapper1_15_R1 implements VersionWrapper {
   private int getRealNextContainerId(Player var1) {
      return this.toNMS(var1).nextContainerCounter();
   }

   @Override
   public int getNextContainerId(Player var1, VersionWrapper.AnvilContainerWrapper var2) {
      return ((Wrapper1_15_R1.AnvilContainer)var2).getContainerId();
   }

   @Override
   public void handleInventoryCloseEvent(Player var1) {
      CraftEventFactory.handleInventoryCloseEvent(this.toNMS(var1));
      this.toNMS(var1).m();
   }

   @Override
   public void sendPacketOpenWindow(Player var1, int var2, Object var3) {
      this.toNMS(var1).playerConnection.sendPacket(new PacketPlayOutOpenWindow(var2, Containers.ANVIL, (IChatBaseComponent)var3));
   }

   @Override
   public void sendPacketCloseWindow(Player var1, int var2) {
      this.toNMS(var1).playerConnection.sendPacket(new PacketPlayOutCloseWindow(var2));
   }

   @Override
   public void sendPacketExperienceChange(Player var1, int var2) {
      this.toNMS(var1).playerConnection.sendPacket(new PacketPlayOutExperience(0.0F, 0, var2));
   }

   @Override
   public void setActiveContainerDefault(Player var1) {
      this.toNMS(var1).activeContainer = this.toNMS(var1).defaultContainer;
   }

   @Override
   public void setActiveContainer(Player var1, VersionWrapper.AnvilContainerWrapper var2) {
      this.toNMS(var1).activeContainer = (Container)var2;
   }

   @Override
   public void setActiveContainerId(VersionWrapper.AnvilContainerWrapper var1, int var2) {
   }

   @Override
   public void addActiveContainerSlotListener(VersionWrapper.AnvilContainerWrapper var1, Player var2) {
      ((Container)var1).addSlotListener(this.toNMS(var2));
   }

   @Override
   public VersionWrapper.AnvilContainerWrapper newContainerAnvil(Player var1, Object var2) {
      return new Wrapper1_15_R1.AnvilContainer(var1, (IChatBaseComponent)var2);
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
            Wrapper1_15_R1.this.getRealNextContainerId(var2),
            ((CraftPlayer)var2).getHandle().inventory,
            ContainerAccess.at(((CraftWorld)var2.getWorld()).getHandle(), new BlockPosition(0, 0, 0))
         );
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
}
