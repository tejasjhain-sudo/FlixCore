package org.lime.swiftCore.libs.anvilgui.version;

import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.Blocks;
import net.minecraft.server.v1_12_R1.ChatMessage;
import net.minecraft.server.v1_12_R1.Container;
import net.minecraft.server.v1_12_R1.ContainerAnvil;
import net.minecraft.server.v1_12_R1.EntityHuman;
import net.minecraft.server.v1_12_R1.EntityPlayer;
import net.minecraft.server.v1_12_R1.IInventory;
import net.minecraft.server.v1_12_R1.PacketPlayOutCloseWindow;
import net.minecraft.server.v1_12_R1.PacketPlayOutExperience;
import net.minecraft.server.v1_12_R1.PacketPlayOutOpenWindow;
import net.minecraft.server.v1_12_R1.Slot;
import net.minecraft.server.v1_12_R1.World;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_12_R1.event.CraftEventFactory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class Wrapper1_12_R1 implements VersionWrapper {
   @Override
   public int getNextContainerId(Player var1, VersionWrapper.AnvilContainerWrapper var2) {
      return this.toNMS(var1).nextContainerCounter();
   }

   @Override
   public void handleInventoryCloseEvent(Player var1) {
      CraftEventFactory.handleInventoryCloseEvent(this.toNMS(var1));
      this.toNMS(var1).r();
   }

   @Override
   public void sendPacketOpenWindow(Player var1, int var2, Object var3) {
      this.toNMS(var1)
         .playerConnection
         .sendPacket(new PacketPlayOutOpenWindow(var2, "minecraft:anvil", new ChatMessage(Blocks.ANVIL.a() + ".name", new Object[0])));
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
      ((Container)var1).windowId = var2;
   }

   @Override
   public void addActiveContainerSlotListener(VersionWrapper.AnvilContainerWrapper var1, Player var2) {
      ((Container)var1).addSlotListener(this.toNMS(var2));
   }

   @Override
   public VersionWrapper.AnvilContainerWrapper newContainerAnvil(Player var1, Object var2) {
      return new Wrapper1_12_R1.AnvilContainer(this.toNMS(var1));
   }

   @Override
   public boolean isCustomTitleSupported() {
      return false;
   }

   @Override
   public Object literalChatComponent(String var1) {
      return null;
   }

   @Override
   public Object jsonChatComponent(String var1) {
      return null;
   }

   private EntityPlayer toNMS(Player var1) {
      return ((CraftPlayer)var1).getHandle();
   }

   private class AnvilContainer extends ContainerAnvil implements VersionWrapper.AnvilContainerWrapper {
      public AnvilContainer(EntityHuman var2) {
         super(var2.inventory, var2.world, new BlockPosition(0, 0, 0), var2);
         this.checkReachable = false;
      }

      public void e() {
         Slot var1 = this.getSlot(2);
         if (!var1.hasItem()) {
            Slot var2 = this.getSlot(0);
            if (var2.hasItem()) {
               var1.set(var2.getItem().cloneItemStack());
            }
         }

         this.levelCost = 0;
         this.b();
      }

      public void b(EntityHuman var1) {
      }

      protected void a(EntityHuman var1, World var2, IInventory var3) {
      }

      @Override
      public Inventory getBukkitInventory() {
         return this.getBukkitView().getTopInventory();
      }
   }
}
