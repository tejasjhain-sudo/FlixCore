package org.lime.swiftCore.libs.anvilgui;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.logging.Level;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.geysermc.geyser.api.GeyserApi;
import org.lime.swiftCore.libs.anvilgui.version.VersionMatcher;
import org.lime.swiftCore.libs.anvilgui.version.VersionWrapper;

public class AnvilGUI {
   private static final VersionWrapper WRAPPER = new VersionMatcher().match();
   private static final ItemStack AIR = new ItemStack(Material.AIR);
   private final Plugin plugin;
   private final Player player;
   private final Executor mainThreadExecutor;
   private final Object titleComponent;
   private final ItemStack[] initialContents;
   private final boolean preventClose;
   private final boolean geyserCompatibility;
   private final Set<Integer> interactableSlots;
   private final Consumer<AnvilGUI.StateSnapshot> closeListener;
   private final boolean concurrentClickHandlerExecution;
   private final AnvilGUI.ClickHandler clickHandler;
   private int containerId;
   private Inventory inventory;
   private final AnvilGUI.ListenUp listener = new AnvilGUI.ListenUp();
   private boolean open;
   private VersionWrapper.AnvilContainerWrapper container;

   private static ItemStack itemNotNull(ItemStack var0) {
      return var0 == null ? AIR : var0;
   }

   private AnvilGUI(
      Plugin var1,
      Player var2,
      Executor var3,
      Object var4,
      ItemStack[] var5,
      boolean var6,
      boolean var7,
      Set<Integer> var8,
      Consumer<AnvilGUI.StateSnapshot> var9,
      boolean var10,
      AnvilGUI.ClickHandler var11
   ) {
      this.plugin = var1;
      this.player = var2;
      this.mainThreadExecutor = var3;
      this.titleComponent = var4;
      this.initialContents = var5;
      this.preventClose = var6;
      this.geyserCompatibility = var7;
      this.interactableSlots = Collections.unmodifiableSet(var8);
      this.closeListener = var9;
      this.concurrentClickHandlerExecution = var10;
      this.clickHandler = var11;
   }

   private void openInventory() {
      Bukkit.getPluginManager().registerEvents(this.listener, this.plugin);
      this.container = WRAPPER.newContainerAnvil(this.player, this.titleComponent);
      this.inventory = this.container.getBukkitInventory();

      for (int var1 = 0; var1 < this.initialContents.length; var1++) {
         this.inventory.setItem(var1, this.initialContents[var1]);
      }

      this.containerId = WRAPPER.getNextContainerId(this.player, this.container);
      WRAPPER.handleInventoryCloseEvent(this.player);
      WRAPPER.sendPacketOpenWindow(this.player, this.containerId, this.titleComponent);
      WRAPPER.setActiveContainer(this.player, this.container);
      WRAPPER.setActiveContainerId(this.container, this.containerId);
      WRAPPER.addActiveContainerSlotListener(this.container, this.player);
      if (this.geyserCompatibility
         && this.plugin.getServer().getPluginManager().getPlugin("Geyser-Spigot") != null
         && this.plugin.getServer().getPluginManager().getPlugin("Geyser-Spigot").isEnabled()
         && GeyserApi.api().isBedrockPlayer(this.player.getUniqueId())) {
         WRAPPER.sendPacketExperienceChange(this.player, 20);
      }

      this.open = true;
   }

   public void closeInventory() {
      this.closeInventory(true);
   }

   private void closeInventory(boolean var1) {
      if (this.open) {
         this.open = false;
         HandlerList.unregisterAll(this.listener);
         if (var1) {
            WRAPPER.handleInventoryCloseEvent(this.player);
            WRAPPER.setActiveContainerDefault(this.player);
            WRAPPER.sendPacketCloseWindow(this.player, this.containerId);
         }

         if (this.geyserCompatibility
            && this.plugin.getServer().getPluginManager().getPlugin("Geyser-Spigot") != null
            && this.plugin.getServer().getPluginManager().getPlugin("Geyser-Spigot").isEnabled()
            && GeyserApi.api().isBedrockPlayer(this.player.getUniqueId())) {
            WRAPPER.sendPacketExperienceChange(this.player, this.player.getLevel());
         }

         if (this.closeListener != null) {
            this.closeListener.accept(AnvilGUI.StateSnapshot.fromAnvilGUI(this));
         }
      }
   }

   public void setTitle(String var1, boolean var2) {
      Validate.notNull(var1, "literalTitle cannot be null");
      this.setTitle(WRAPPER.literalChatComponent(var1), var2);
   }

   public void setJsonTitle(String var1, boolean var2) {
      Validate.notNull(var1, "json cannot be null");
      this.setTitle(WRAPPER.jsonChatComponent(var1), var2);
   }

   private void setTitle(Object var1, boolean var2) {
      if (WRAPPER.isCustomTitleSupported()) {
         String var3 = this.container.getRenameText();
         WRAPPER.sendPacketOpenWindow(this.player, this.containerId, var1);
         if (var2) {
            this.container.setRenameText(var3 == null ? "" : var3);
         }
      }
   }

   public Inventory getInventory() {
      return this.inventory;
   }

   public static class Builder {
      private Executor mainThreadExecutor;
      private Consumer<AnvilGUI.StateSnapshot> closeListener;
      private boolean concurrentClickHandlerExecution = false;
      private AnvilGUI.ClickHandler clickHandler;
      private boolean preventClose = false;
      private boolean geyserCompatibility = true;
      private Set<Integer> interactableSlots = Collections.emptySet();
      private Plugin plugin;
      private Object titleComponent = AnvilGUI.WRAPPER.literalChatComponent("Repair & Name");
      private String itemText;
      private ItemStack itemLeft;
      private ItemStack itemRight;
      private ItemStack itemOutput;

      public AnvilGUI.Builder mainThreadExecutor(Executor var1) {
         Validate.notNull(var1, "Executor cannot be null");
         this.mainThreadExecutor = var1;
         return this;
      }

      public AnvilGUI.Builder preventClose() {
         this.preventClose = true;
         return this;
      }

      public AnvilGUI.Builder disableGeyserCompat() {
         this.geyserCompatibility = false;
         return this;
      }

      public AnvilGUI.Builder interactableSlots(int... var1) {
         HashSet var2 = new HashSet();

         for (int var6 : var1) {
            var2.add(var6);
         }

         this.interactableSlots = var2;
         return this;
      }

      public AnvilGUI.Builder onClose(Consumer<AnvilGUI.StateSnapshot> var1) {
         Validate.notNull(var1, "closeListener cannot be null");
         this.closeListener = var1;
         return this;
      }

      public AnvilGUI.Builder onClickAsync(AnvilGUI.ClickHandler var1) {
         Validate.notNull(var1, "click function cannot be null");
         this.clickHandler = var1;
         return this;
      }

      public AnvilGUI.Builder allowConcurrentClickHandlerExecution() {
         this.concurrentClickHandlerExecution = true;
         return this;
      }

      public AnvilGUI.Builder onClick(BiFunction<Integer, AnvilGUI.StateSnapshot, List<AnvilGUI.ResponseAction>> var1) {
         Validate.notNull(var1, "click function cannot be null");
         this.clickHandler = (var1x, var2) -> CompletableFuture.completedFuture((List)var1.apply(var1x, var2));
         return this;
      }

      public AnvilGUI.Builder plugin(Plugin var1) {
         Validate.notNull(var1, "Plugin cannot be null");
         this.plugin = var1;
         return this;
      }

      public AnvilGUI.Builder text(String var1) {
         Validate.notNull(var1, "Text cannot be null");
         this.itemText = var1;
         return this;
      }

      public AnvilGUI.Builder title(String var1) {
         Validate.notNull(var1, "title cannot be null");
         this.titleComponent = AnvilGUI.WRAPPER.literalChatComponent(var1);
         return this;
      }

      public AnvilGUI.Builder jsonTitle(String var1) {
         Validate.notNull(var1, "json cannot be null");
         this.titleComponent = AnvilGUI.WRAPPER.jsonChatComponent(var1);
         return this;
      }

      public AnvilGUI.Builder itemLeft(ItemStack var1) {
         Validate.notNull(var1, "item cannot be null");
         this.itemLeft = var1;
         return this;
      }

      public AnvilGUI.Builder itemRight(ItemStack var1) {
         this.itemRight = var1;
         return this;
      }

      public AnvilGUI.Builder itemOutput(ItemStack var1) {
         this.itemOutput = var1;
         return this;
      }

      public AnvilGUI open(Player var1) {
         Validate.notNull(this.plugin, "Plugin cannot be null");
         Validate.notNull(this.clickHandler, "click handler cannot be null");
         Validate.notNull(var1, "Player cannot be null");
         if (this.itemText != null) {
            if (this.itemLeft == null) {
               this.itemLeft = new ItemStack(Material.PAPER);
            }

            ItemMeta var2 = this.itemLeft.getItemMeta();
            var2.setDisplayName(this.itemText);
            this.itemLeft.setItemMeta(var2);
         }

         if (this.mainThreadExecutor == null) {
            this.mainThreadExecutor = var1x -> Bukkit.getScheduler().runTask(this.plugin, var1x);
         }

         AnvilGUI var3 = new AnvilGUI(
            this.plugin,
            var1,
            this.mainThreadExecutor,
            this.titleComponent,
            new ItemStack[]{this.itemLeft, this.itemRight, this.itemOutput},
            this.preventClose,
            this.geyserCompatibility,
            this.interactableSlots,
            this.closeListener,
            this.concurrentClickHandlerExecution,
            this.clickHandler
         );
         var3.openInventory();
         return var3;
      }
   }

   @FunctionalInterface
   public interface ClickHandler extends BiFunction<Integer, AnvilGUI.StateSnapshot, CompletableFuture<List<AnvilGUI.ResponseAction>>> {
   }

   private class ListenUp implements Listener {
      private boolean clickHandlerRunning = false;

      private ListenUp() {
      }

      @EventHandler
      public void onInventoryClick(InventoryClickEvent var1) {
         if (var1.getInventory().equals(AnvilGUI.this.inventory)) {
            int var2 = var1.getRawSlot();
            if (var2 != -999) {
               Player var3 = (Player)var1.getWhoClicked();
               Inventory var4 = var1.getClickedInventory();
               if (var4 != null) {
                  if (var4.equals(var3.getInventory())) {
                     if (var1.getClick().equals(ClickType.DOUBLE_CLICK)) {
                        var1.setCancelled(true);
                        return;
                     }

                     if (var1.isShiftClick()) {
                        var1.setCancelled(true);
                        return;
                     }
                  }

                  if (var1.getCursor() != null
                     && var1.getCursor().getType() != Material.AIR
                     && !AnvilGUI.this.interactableSlots.contains(var2)
                     && var1.getClickedInventory().equals(AnvilGUI.this.inventory)) {
                     var1.setCancelled(true);
                     return;
                  }
               }

               if (var2 < 3 && var2 >= 0 || var1.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY)) {
                  var1.setCancelled(!AnvilGUI.this.interactableSlots.contains(var2));
                  if (this.clickHandlerRunning && !AnvilGUI.this.concurrentClickHandlerExecution) {
                     return;
                  }

                  CompletableFuture var5 = AnvilGUI.this.clickHandler.apply(Integer.valueOf(var2), AnvilGUI.StateSnapshot.fromAnvilGUI(AnvilGUI.this));
                  Consumer var6 = var2x -> {
                     for (AnvilGUI.ResponseAction var4x : var2x) {
                        var4x.accept(AnvilGUI.this, var3);
                     }
                  };
                  if (var5.isDone()) {
                     var5.thenAccept(var6).join();
                  } else {
                     this.clickHandlerRunning = true;
                     var5.thenAcceptAsync(var6, AnvilGUI.this.mainThreadExecutor).handle((var1x, var2x) -> {
                        if (var2x != null) {
                           AnvilGUI.this.plugin.getLogger().log(Level.SEVERE, "An exception occurred in the AnvilGUI clickHandler", var2x);
                        }

                        this.clickHandlerRunning = false;
                        return null;
                     });
                  }
               }
            }
         }
      }

      @EventHandler
      public void onInventoryDrag(InventoryDragEvent var1) {
         if (var1.getInventory().equals(AnvilGUI.this.inventory)) {
            for (int var5 : AnvilGUI.Slot.values()) {
               if (var1.getRawSlots().contains(var5)) {
                  var1.setCancelled(!AnvilGUI.this.interactableSlots.contains(var5));
                  break;
               }
            }
         }
      }

      @EventHandler
      public void onInventoryClose(InventoryCloseEvent var1) {
         if (AnvilGUI.this.open && var1.getInventory().equals(AnvilGUI.this.inventory)) {
            AnvilGUI.this.closeInventory(false);
            if (AnvilGUI.this.preventClose) {
               Executor var10000 = AnvilGUI.this.mainThreadExecutor;
               AnvilGUI var2 = AnvilGUI.this;
               var10000.execute(() -> var2.openInventory());
            }
         }
      }
   }

   @Deprecated
   public static class Response {
      public static List<AnvilGUI.ResponseAction> close() {
         return Arrays.asList(AnvilGUI.ResponseAction.close());
      }

      public static List<AnvilGUI.ResponseAction> text(String var0) {
         return Arrays.asList(AnvilGUI.ResponseAction.replaceInputText(var0));
      }

      public static List<AnvilGUI.ResponseAction> openInventory(Inventory var0) {
         return Arrays.asList(AnvilGUI.ResponseAction.openInventory(var0));
      }
   }

   @FunctionalInterface
   public interface ResponseAction extends BiConsumer<AnvilGUI, Player> {
      static AnvilGUI.ResponseAction replaceInputText(String var0) {
         Validate.notNull(var0, "text cannot be null");
         return (var1, var2) -> {
            ItemStack var3 = var1.getInventory().getItem(2);
            if (var3 == null) {
               var3 = var1.getInventory().getItem(0);
            }

            if (var3 == null) {
               throw new IllegalStateException("replaceInputText can only be used if slots OUTPUT or INPUT_LEFT are not empty");
            } else {
               ItemStack var4 = var3.clone();
               ItemMeta var5 = var4.getItemMeta();
               var5.setDisplayName(var0);
               var4.setItemMeta(var5);
               var1.getInventory().setItem(0, var4);
            }
         };
      }

      static AnvilGUI.ResponseAction updateTitle(String var0, boolean var1) {
         Validate.notNull(var0, "literalTitle cannot be null");
         return (var2, var3) -> var2.setTitle(var0, var1);
      }

      static AnvilGUI.ResponseAction updateJsonTitle(String var0, boolean var1) {
         Validate.notNull(var0, "json cannot be null");
         return (var2, var3) -> var2.setJsonTitle(var0, var1);
      }

      static AnvilGUI.ResponseAction openInventory(Inventory var0) {
         Validate.notNull(var0, "otherInventory cannot be null");
         return (var1, var2) -> var2.openInventory(var0);
      }

      static AnvilGUI.ResponseAction close() {
         return (var0, var1) -> var0.closeInventory();
      }

      static AnvilGUI.ResponseAction run(Runnable var0) {
         Validate.notNull(var0, "runnable cannot be null");
         return (var1, var2) -> var0.run();
      }
   }

   public static class Slot {
      private static final int[] values = new int[]{0, 1, 2};
      public static final int INPUT_LEFT = 0;
      public static final int INPUT_RIGHT = 1;
      public static final int OUTPUT = 2;

      public static int[] values() {
         return values;
      }
   }

   public static final class StateSnapshot {
      private final ItemStack leftItem;
      private final ItemStack rightItem;
      private final ItemStack outputItem;
      private final Player player;

      private static AnvilGUI.StateSnapshot fromAnvilGUI(AnvilGUI var0) {
         Inventory var1 = var0.getInventory();
         return new AnvilGUI.StateSnapshot(
            AnvilGUI.itemNotNull(var1.getItem(0)).clone(),
            AnvilGUI.itemNotNull(var1.getItem(1)).clone(),
            AnvilGUI.itemNotNull(var1.getItem(2)).clone(),
            var0.player
         );
      }

      public StateSnapshot(ItemStack var1, ItemStack var2, ItemStack var3, Player var4) {
         this.leftItem = var1;
         this.rightItem = var2;
         this.outputItem = var3;
         this.player = var4;
      }

      public ItemStack getLeftItem() {
         return this.leftItem;
      }

      public ItemStack getRightItem() {
         return this.rightItem;
      }

      public ItemStack getOutputItem() {
         return this.outputItem;
      }

      public Player getPlayer() {
         return this.player;
      }

      public String getText() {
         return this.outputItem.hasItemMeta() ? this.outputItem.getItemMeta().getDisplayName() : "";
      }
   }
}
