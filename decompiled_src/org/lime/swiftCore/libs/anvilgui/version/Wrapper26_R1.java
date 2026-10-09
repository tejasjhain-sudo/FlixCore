package org.lime.swiftCore.libs.anvilgui.version;

import java.lang.reflect.Method;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundContainerClosePacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.network.protocol.game.ClientboundSetExperiencePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public final class Wrapper26_R1 implements VersionWrapper {
   private int getRealNextContainerId(Player var1) {
      return this.toNMS(var1).nextContainerCounter();
   }

   private ServerPlayer toNMS(Player var1) {
      return ((CraftPlayer)var1).getHandle();
   }

   @Override
   public int getNextContainerId(Player var1, VersionWrapper.AnvilContainerWrapper var2) {
      return ((Wrapper26_R1.AnvilContainer)var2).getContainerId();
   }

   @Override
   public void handleInventoryCloseEvent(Player var1) {
      try {
         CraftEventFactory.handleInventoryCloseEvent(this.toNMS(var1));
      } catch (NoSuchMethodError var6) {
         try {
            Class var3 = Class.forName("org.bukkit.event.inventory.InventoryCloseEvent$Reason");
            Method var4 = CraftEventFactory.class.getMethod("handleInventoryCloseEvent", net.minecraft.world.entity.player.Player.class, var3);
            var4.invoke(null, this.toNMS(var1), var3.getField("UNKNOWN").get(null));
         } catch (ReflectiveOperationException var5) {
            throw new RuntimeException(var5);
         }
      }

      this.toNMS(var1).doCloseContainer();
   }

   @Override
   public void sendPacketOpenWindow(Player var1, int var2, Object var3) {
      this.toNMS(var1).connection.send(new ClientboundOpenScreenPacket(var2, MenuType.ANVIL, (Component)var3));
   }

   @Override
   public void sendPacketCloseWindow(Player var1, int var2) {
      this.toNMS(var1).connection.send(new ClientboundContainerClosePacket(var2));
   }

   @Override
   public void sendPacketExperienceChange(Player var1, int var2) {
      this.toNMS(var1).connection.send(new ClientboundSetExperiencePacket(0.0F, 0, var2));
   }

   @Override
   public void setActiveContainerDefault(Player var1) {
      this.toNMS(var1).containerMenu = this.toNMS(var1).inventoryMenu;
   }

   @Override
   public void setActiveContainer(Player var1, VersionWrapper.AnvilContainerWrapper var2) {
      this.toNMS(var1).containerMenu = (AbstractContainerMenu)var2;
   }

   @Override
   public void setActiveContainerId(VersionWrapper.AnvilContainerWrapper var1, int var2) {
   }

   @Override
   public void addActiveContainerSlotListener(VersionWrapper.AnvilContainerWrapper var1, Player var2) {
      this.toNMS(var2).initMenu((AbstractContainerMenu)var1);
   }

   @Override
   public VersionWrapper.AnvilContainerWrapper newContainerAnvil(Player var1, Object var2) {
      return new Wrapper26_R1.AnvilContainer(var1, this.getRealNextContainerId(var1), (Component)var2);
   }

   @Override
   public Object literalChatComponent(String var1) {
      return Component.literal(var1);
   }

   @Override
   public Object jsonChatComponent(String var1) {
      return CraftChatMessage.fromJSON(var1);
   }

   private static class AnvilContainer extends AnvilMenu implements VersionWrapper.AnvilContainerWrapper {
      public AnvilContainer(Player var1, int var2, Component var3) {
         super(
            var2, ((CraftPlayer)var1).getHandle().getInventory(), ContainerLevelAccess.create(((CraftWorld)var1.getWorld()).getHandle(), new BlockPos(0, 0, 0))
         );
         this.checkReachable = false;
         this.setTitle(var3);
      }

      public void createResult() {
         Slot var1 = this.getSlot(2);
         if (!var1.hasItem()) {
            var1.set(this.getSlot(0).getItem().copy());
         }

         this.cost.set(0);
         this.sendAllDataToRemote();
         this.broadcastChanges();
      }

      public void removed(net.minecraft.world.entity.player.Player var1) {
      }

      protected void clearContainer(net.minecraft.world.entity.player.Player var1, Container var2) {
      }

      public int getContainerId() {
         return this.containerId;
      }

      @Override
      public String getRenameText() {
         return this.itemName;
      }

      @Override
      public void setRenameText(String var1) {
         Slot var2 = this.getSlot(0);
         if (var2.hasItem()) {
            var2.getItem().set(DataComponents.CUSTOM_NAME, Component.literal(var1));
         }
      }

      @Override
      public Inventory getBukkitInventory() {
         return this.getBukkitView().getTopInventory();
      }
   }
}
