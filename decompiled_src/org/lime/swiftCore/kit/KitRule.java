package org.lime.swiftCore.kit;

public enum KitRule {
   FALL_DAMAGE("falldamage", true, "Disable fall damage"),
   SATURATION_HEAL("saturation", true, "Disable automatic healing if saturation is full (UHC)"),
   DAMAGE("damage", true, "Player can hit but opponent will not get damaged"),
   DAMAGE_MULTIPLIER("damagemultiplier", false, "Double damage (2x) - configurable in config.yml"),
   BEDWARS("bedwars", false, "Game ends only when bed is destroyed"),
   STICK_SPAWN("stickspawn", false, "Player can't move during countdown and can't teleport"),
   HUNGER("hunger", true, "Disable automatic hunger loss"),
   SUMO("sumo", false, "Player eliminated if they touch water"),
   BOXING("boxing", false, "Player must hit opponent 100 times to win"),
   AUTO_TNT("autotnt", false, "TNT lights up automatically when placed"),
   SHOOT_FIREBALL("shootfireball", false, "Fireball explodes at hitting point with knockback"),
   HEARTS("hearts", false, "Use custom health for this kit"),
   BRIDGE("bridge", false, "Bridge mode: goals win, deaths respawn"),
   FIREBALL_JUMPS("fireballjumps", false, "Fireball gives knockback to player"),
   TNT_JUMPS("tntjumps", false, "TNT gives knockback to player"),
   BED_BOOM("bedboom", false, "Beds explode when right-clicked"),
   GOLDEN_HEAD("goldenhead", false, "Golden heads give regeneration and absorption"),
   CREEPER_SPAWN_EGG("creeperspawnegg", false, "Creeper spawn eggs spawn and immediately ignite creepers"),
   VISUAL_GAPPLE_COOLDOWN("visualgapplecooldown", false, "Show visual cooldown for golden apples"),
   ONLY_ARROW_DAMAGE("onlyarrowdamage", false, "Players only take damage from arrows"),
   VISUAL_GOD_APPLE_COOLDOWN("visualgodapplecooldown", false, "Show visual cooldown for enchanted golden apples"),
   ARROW_INSTANT_DESPAWN("arrowinstantdespawn", false, "Arrows despawn instantly when hitting ground"),
   VISUAL_ENDERPEARL_COOLDOWN("visualenderpearlcooldown", false, "Show visual cooldown for ender pearls"),
   CRYSTAL_NO_DAMAGE("crystalnodamage", false, "Players don't take damage from end crystals"),
   ONE_SHOT("oneshot", false, "Arrow kills player instantly, no other damage allowed"),
   SPLEEF("spleef", false, "Only break blocks below players"),
   NO_ITEM_DROP("noitemdrop", false, "Players cannot drop items"),
   TNT_TAG("tnttag", false, "TNT Tag elimination mode"),
   FLOWER_CROWN("flowercrown", false, "Flower Crown protection mode"),
   BLOCK_DECAY("blockdecay", false, "Blocks touched by players decay and disappear"),
   SKYWARS("skywars", false, "Fill arena chests with tiered random loot"),
   SKYWARS_AUTO_SMELT("skywarsautosmelt", false, "Ores drop smelted items"),
   SKYWARS_WOOD_TO_PLANKS("skywarswoodtoplanks", false, "Wood logs drop planks"),
   TNT_SUMO("tntsumo", false, "TNT Sumo mode with instant explosions and knockback"),
   CRAFTING("crafting", false, "Allow crafting during fight"),
   MAX_FIGHT_DURATION("maxfightduration", false, "Use custom max fight duration for this kit"),
   SNOWBALL_KNOCKBACK("snowballknockback", false, "Snowballs give knockback"),
   EGG_KNOCKBACK("eggknockback", false, "Eggs give knockback"),
   PARKOUR("parkour", false, "Parkour mode with checkpoint system"),
   INSTANT_RESPAWN("instantrespawn", false, "Skip respawn delay and respawn instantly"),
   BRIDGE_POINTS("bridgepoints", false, "Use custom points required to win Bridge"),
   GOLDEN_APPLE_FULL_GEN("goldengapfullgen", false, "Golden apple fully heals the player"),
   HP_INDICATOR("hpindicator", false, "Show health below player nametag"),
   PRE_POTIONS("prepotions", false, "Apply potion effects when the match starts"),
   CUSTOM_ITEMS("custom-items", false, "Allow custom items in the kit editor"),
   HUNTER_TROOPS("huntertroops", false, "Hunter and Troops prop hunt mode"),
   COLOR_PARTY("color-party", false, "Color Party Event"),
   CHICKEN_HUNT("chicken-hunt", false, "Timed chicken hunting score mode"),
   PILLARS_OF_FORTUNE("pillarsoffortune", false, "Pillars of Fortune sky pillar random item mode"),
   LAVA_RAISE("lavaraise", false, "Lava rises from the arena floor during each round"),
   LEGACY_COMBAT("legacycombat", false, "Use 1.8-style combat through the Legacy Combat addon");

   private final String name;
   private final boolean defaultValue;
   private final String description;

   private KitRule(String var3, boolean var4, String var5) {
      this.name = var3;
      this.defaultValue = var4;
      this.description = var5;
   }

   public String getName() {
      return this.name;
   }

   public boolean getDefaultValue() {
      return this.defaultValue;
   }

   public String getDescription() {
      return this.description;
   }

   public static KitRule fromName(String var0) {
      if ("creeperhead".equalsIgnoreCase(var0)) {
         return CREEPER_SPAWN_EGG;
      } else {
         for (KitRule var4 : values()) {
            if (var4.getName().equalsIgnoreCase(var0)) {
               return var4;
            }
         }

         return null;
      }
   }
}
