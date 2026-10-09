package com.lime.practicebot.api;

import org.bukkit.inventory.ItemStack;

public class BotEquipmentSpec {
    private final ItemStack[] armor;
    private final ItemStack[] inventory;
    private final ItemStack offHand;

    public BotEquipmentSpec(ItemStack[] armor, ItemStack[] inventory, ItemStack offHand) {
        this.armor = armor;
        this.inventory = inventory;
        this.offHand = offHand;
    }

    public ItemStack[] getArmor() {
        return armor;
    }

    public ItemStack[] getInventory() {
        return inventory;
    }

    public ItemStack getOffHand() {
        return offHand;
    }
}
