package org.lime.swiftCore.party;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.Title.Times;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.lime.swiftCore.SwiftCore;
import org.lime.swiftCore.api.CustomKitAPI;
import org.lime.swiftCore.arena.ArenaType;
import org.lime.swiftCore.b.k;
import org.lime.swiftCore.kit.ColorPartyKitGuard;
import org.lime.swiftCore.kit.KitRule;
import org.lime.swiftCore.kit.h;
import org.lime.swiftCore.kit.t;
import org.lime.swiftCore.scoreboard.ScoreboardState;
import org.lime.swiftCore.tablist.TablistContext;
import org.lime.swiftCore.v.f.g;

public class PartyGameManager {
   private final SwiftCore plugin;
   private final PartyManager partyManager;
   private final Map<UUID, UUID> pendingChallenges;
   private final Map<UUID, d> activeGames;
   private final Map<UUID, d> playerToGame;
   private final Map<d, UUID> gameToOwner;
   private final Map<d, String> crossServerRegistryIds;
   private final Set<String> pendingArenaStarts = ConcurrentHashMap.newKeySet();
   private k cachedCountdownSound;
   private k cachedStartSound;
   private List<Map<?, ?>> cachedTitles;
   private List<Map<?, ?>> cachedFinalTitle;
   private int cachedMaxFightDuration;
   private int cachedCountdownTime;
   private String cachedBlueTeamIcon;
   private String cachedBlueTeamColor;
   private String cachedRedTeamIcon;
   private String cachedRedTeamColor;
   private boolean cachedKitColoredArmor;
   private double cachedFfaSpawnRadius;
   private String cachedSpectatorGamemode;
   private k cachedEliminateSound;
   private k cachedWinSound;
   private Set<String> cachedPartyfFaDisabledKits;
   private Set<String> cachedPartySplitDisabledKits;
   private Set<String> cachedPartyVsDisabledKits;
   private boolean cachedAllowImbalance;
   private boolean cachedTeamGlowEnabled;
   private int cachedEndDelayTicks;
   private final Set<UUID> partyTransferring = ConcurrentHashMap.newKeySet();

   public PartyGameManager(SwiftCore var1, PartyManager var2) {
      this.plugin = var1;
      this.partyManager = var2;
      this.pendingChallenges = new ConcurrentHashMap<>();
      this.activeGames = new ConcurrentHashMap<>();
      this.playerToGame = new ConcurrentHashMap<>();
      this.gameToOwner = new ConcurrentHashMap<>();
      this.crossServerRegistryIds = new ConcurrentHashMap<>();
      this.cacheConfig();
   }

   public void cacheConfig() {
      this.cachedCountdownSound = k.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
         this.plugin.getConfig(), "party.sounds.countdown", "BLOCK_NOTE_BLOCK_PLING"
      );
      this.cachedStartSound = k.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
         this.plugin.getConfig(), "party.sounds.start", "BLOCK_NOTE_BLOCK_CHIME"
      );
      this.cachedTitles = this.plugin.getConfig().getMapList("party.titles");
      this.cachedFinalTitle = this.plugin.getConfig().getMapList("party.final-title");
      this.cachedMaxFightDuration = this.plugin.getConfig().getInt("match.max-fight-duration", 30);
      this.cachedCountdownTime = this.plugin.getConfig().getInt("party.countdown-time", 5);
      this.cachedBlueTeamIcon = this.plugin.getConfig().getString("team-settings.blue-team-icon", "&9[B] &9");
      this.cachedBlueTeamColor = this.plugin.getConfig().getString("team-settings.blue-team-color", "&9");
      this.cachedRedTeamIcon = this.plugin.getConfig().getString("team-settings.red-team-icon", "&c[R] &c");
      this.cachedRedTeamColor = this.plugin.getConfig().getString("team-settings.red-team-color", "&c");
      this.cachedKitColoredArmor = this.plugin.getConfig().getBoolean("match.kit-colored-armor", true);
      this.cachedFfaSpawnRadius = this.plugin.getConfig().getDouble("party.ffa-spawn-radius", 5.0);
      this.cachedSpectatorGamemode = this.plugin.getConfig().getString("spectator.spectator-gamemode", "SURVIVAL");
      this.cachedEliminateSound = k.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
         this.plugin.getConfig(), "party.sounds.eliminate", "ENTITY_VILLAGER_NO"
      );
      this.cachedWinSound = k.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
         this.plugin.getConfig(), "party.sounds.win", "ENTITY_PLAYER_LEVELUP"
      );
      this.cachedPartyfFaDisabledKits = h.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
         this.plugin.getConfig().getStringList("party.partyffa-disabled-kits")
      );
      this.cachedPartySplitDisabledKits = h.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
         this.plugin.getConfig().getStringList("party.partysplit-disabled-kits")
      );
      this.cachedPartyVsDisabledKits = h.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
         this.plugin.getConfig().getStringList("party.partyvs-disabled-kits")
      );
      this.cachedAllowImbalance = this.plugin.getConfig().getBoolean("party.allow-imbalance", false);
      this.cachedTeamGlowEnabled = this.plugin.getConfig().getBoolean("team-settings.team-glow-enabled", true);
      this.cachedEndDelayTicks = Math.max(0, this.plugin.getConfig().getInt("party.end-delay-seconds", 3)) * 20;
   }

   public boolean isTeamGlowEnabled() {
      return this.cachedTeamGlowEnabled;
   }

   public boolean isPartyVsKitDisabled(String var1) {
      return var1 != null && this.cachedPartyVsDisabledKits.contains(var1.toLowerCase(Locale.ROOT));
   }

   private boolean isHunterTroopsGame(d var1) {
      return var1 != null
         && this.plugin
            .getDataManager()
            .Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class(
               var1.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new()
            )
            .getOrDefault(KitRule.HUNTER_TROOPS, KitRule.HUNTER_TROOPS.getDefaultValue());
   }

   private UUID getGameOwner(d var1) {
      return this.gameToOwner.get(var1);
   }

   public void resolveChickenHuntIndividualRound(UUID var1, UUID var2) {
      d var3 = this.getPlayerActiveGame(var1);
      if (var3 != null
         && var3.Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return()
            == e.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new
         && var3.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object()
         && !var3.Óo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000intsuper()
         && !var3.oO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000do()
         && var3.öO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Stringsuper()
            .contains(var1)
         && var3.öO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Stringsuper()
            .contains(var2)
         && this.getKitRule(
            var3.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(),
            KitRule.CHICKEN_HUNT
         )) {
         UUID var4 = this.getGameOwner(var3);
         if (var4 != null) {
            this.cancelPartyTimeLimit(var3);

            for (UUID var6 : new ArrayList<>(this.getActiveFfaPlayers(var3))) {
               if (!var6.equals(var2)) {
                  var3.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int(
                     var6
                  );
               }
            }

            this.updateFfaAlivePlaceholders(var3, 1);
            this.scheduleFfaRoundResolution(var4, var3);
         }
      }
   }

   public void resolveChickenHuntTeamRound(UUID var1, String var2) {
      d var3 = this.getPlayerActiveGame(var1);
      if (var3 != null
         && var3.Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return()
            != e.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new
         && var3.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object()
         && !var3.Óo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000intsuper()
         && !var3.oO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000do()
         && var3.öO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Stringsuper()
            .contains(var1)
         && this.getKitRule(
            var3.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(),
            KitRule.CHICKEN_HUNT
         )) {
         String var4 = var2.trim().toLowerCase(Locale.ROOT);
         if (var4.equals("blue")) {
            var4 = "team1";
         }

         if (var4.equals("red")) {
            var4 = "team2";
         }

         if (var4.equals("team1") || var4.equals("team2")) {
            UUID var5 = this.getGameOwner(var3);
            if (var5 != null) {
               this.cancelPartyTimeLimit(var3);
               String var6 = var4.equals("team1") ? "Red Team" : "Blue Team";

               for (UUID var9 : var4.equals("team1")
                  ? var3.ÒÒ00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000newnew()
                  : var3.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int()) {
                  var3.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int(
                     var9
                  );
               }

               this.handleTeamEliminated(var5, var3, var4, var6);
            }
         }
      }
   }

   private void cancelPartyTimeLimit(d var1) {
      if (var1.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float()
         != -1) {
         Bukkit.getScheduler()
            .cancelTask(
               var1.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float()
            );
         var1.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
            -1
         );
      }
   }

   public int getTotalPlayersInGames() {
      return this.playerToGame.size();
   }

   public int getPlayersInGamesForKit(String var1) {
      if (var1 != null && !var1.isBlank()) {
         String var2 = var1.toLowerCase(Locale.ROOT);
         String var3 = var2.startsWith("tier") ? var2.substring(4) : var2;
         String var4 = "tier" + var3;
         Set var5 = Collections.newSetFromMap(new IdentityHashMap());
         int var6 = 0;

         for (d var8 : this.playerToGame.values()) {
            String var9 = var8 != null
                  && var8.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new()
                     != null
               ? var8.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new()
                  .toLowerCase(Locale.ROOT)
               : "";
            if (var8 != null
               && !var8.oO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000do()
               && (var9.equals(var3) || var9.equals(var4))
               && var5.add(var8)) {
               var6 += var8.öO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Stringsuper()
                  .size();
            }
         }

         return var6;
      } else {
         return 0;
      }
   }

   private boolean isCustomKit(String var1) {
      return var1 != null && var1.startsWith("customkit");
   }

   private UUID getCustomKitOwner(String var1) {
      if (!this.isCustomKit(var1)) {
         return null;
      } else {
         String[] var2 = var1.split(":");
         if (var2.length < 2) {
            return null;
         } else {
            try {
               return UUID.fromString(var2[1]);
            } catch (IllegalArgumentException var4) {
               return null;
            }
         }
      }
   }

   private CustomKitAPI.CustomKitData getCustomKitData(String var1) {
      if (this.plugin.getCustomKitAPI() == null) {
         return null;
      } else {
         UUID var2 = this.getCustomKitOwner(var1);
         return var2 != null ? this.plugin.getCustomKitAPI().getKit(var2) : null;
      }
   }

   private ArenaType getArenaTypeForKit(String var1) {
      if (this.isCustomKit(var1)) {
         CustomKitAPI.CustomKitData var3 = this.getCustomKitData(var1);
         return var3 != null ? var3.arenaType() : null;
      } else {
         t var2 = this.plugin
            .getKitManager()
            .oO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000do(
               var1
            );
         return var2 != null
            ? var2.Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return()
            : null;
      }
   }

   private String getPartyKitDisplayName(String var1) {
      if (this.isCustomKit(var1)) {
         CustomKitAPI.CustomKitData var2 = this.getCustomKitData(var1);
         return var2 != null && var2.displayName() != null && !var2.displayName().isBlank() ? var2.displayName() : "Custom Kit";
      } else {
         return this.plugin
            .getDataManager()
            .ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000null(
               var1
            );
      }
   }

   public void setPendingChallenge(UUID var1, UUID var2) {
      this.pendingChallenges.put(var1, var2);
   }

   public UUID getPendingChallenge(UUID var1) {
      return this.pendingChallenges.get(var1);
   }

   public void clearPendingChallenge(UUID var1) {
      this.pendingChallenges.remove(var1);
   }

   public void startPartyFFA(UUID var1, String var2, int var3) {
      this.startPartyFFAInternal(var1, var2, var3);
   }

   public void startPartyFFA(UUID var1, String var2) {
      this.startPartyFFAInternal(var1, var2, 1);
   }

   private void startPartyFFAInternal(UUID var1, String var2, int var3) {
      this.startPartyFFAInternal(var1, var2, var3, null);
   }

   private void startPartyFFAInternal(UUID var1, String var2, int var3, org.lime.swiftCore.arena.d var4) {
      this.startPartyFFAInternal(var1, var2, var3, var4, null);
   }

   private void startPartyFFAInternal(UUID var1, String var2, int var3, org.lime.swiftCore.arena.d var4, List<UUID> var5) {
      Party var6 = this.partyManager.getParty(var1);
      if (var6 != null) {
         boolean var7 = this.isCustomKit(var2);
         if (var7) {
            if (this.getCustomKitData(var2) == null) {
               Player var8 = Bukkit.getPlayer(var1);
               if (var8 != null) {
                  HashMap var9 = new HashMap();
                  var9.put("kit", this.getPartyKitDisplayName(var2));
                  this.plugin
                     .getMessagesManager()
                     .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                        var8, "kit-not-found", var9
                     );
               }

               return;
            }
         } else if (!this.plugin
            .getKitManager()
            .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
               var2
            )) {
            Player var21 = Bukkit.getPlayer(var1);
            if (var21 != null) {
               HashMap var23 = new HashMap();
               var23.put("kit", var2);
               this.plugin
                  .getMessagesManager()
                  .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                     var21, "kit-not-found", var23
                  );
            }

            return;
         }

         Player var20 = Bukkit.getPlayer(var1);
         if (var7
            || var20 == null
            || this.plugin
               .getKitManager()
               .Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String(
                  var20, var2
               )) {
            Set var22 = this.cachedPartyfFaDisabledKits;
            if (var22.contains(var2.toLowerCase())) {
               Player var26 = Bukkit.getPlayer(var1);
               if (var26 != null) {
                  this.plugin
                     .getMessagesManager()
                     .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                        var26, "party-kit-disabled"
                     );
               }
            } else {
               if (this.plugin
                  .getFlowerCrownManager()
                  .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                     var2
                  )) {
               }

               if (var6.getSize() < 2) {
                  Player var25 = Bukkit.getPlayer(var1);
                  if (var25 != null) {
                     HashMap var27 = new HashMap();
                     var27.put("min", "2");
                     this.plugin
                        .getMessagesManager()
                        .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                           var25, "party-need-members", var27
                        );
                  }
               } else if (this.shouldTransferCrossServer()) {
                  ArrayList var24 = new ArrayList<>(var6.getMembers());
                  var24.removeIf(var0 -> Bukkit.getPlayer(var0) == null || !Bukkit.getPlayer(var0).isOnline());
                  if (!var24.contains(var1)) {
                     var24.add(0, var1);
                  }

                  this.transferPartyMatch(
                     org.lime.swiftCore.v.f.c.Ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000void,
                     var2,
                     var24,
                     null,
                     null,
                     var3
                  );
               } else {
                  ArenaType var10 = this.getArenaTypeForKit(var2);
                  org.lime.swiftCore.arena.d var11 = var4 != null
                     ? var4
                     : this.plugin
                        .getArenaManager()
                        .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
                           var10, var2
                        );
                  if (var11 == null) {
                     this.acquirePartyArena("FFA:" + var1, var1, var10, var2, var4x -> this.startPartyFFAInternal(var1, var2, var3, var4x));
                  } else if (var11.Oo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000returnsuper()
                        != org.lime.swiftCore.arena.c.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super
                     && !this.plugin
                        .getArenaManager()
                        .ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float(
                           var11
                        )) {
                     this.releasePartyArena(var11);
                  } else {
                     Object var12;
                     if (var5 != null && !var5.isEmpty()) {
                        var12 = this.filterOnlinePlayers(var5);
                     } else {
                        var12 = new ArrayList<>(var6.getMembers());
                        var12.removeIf(var0 -> Bukkit.getPlayer(var0) == null || !Bukkit.getPlayer(var0).isOnline());
                     }

                     d var13 = new d(
                        e.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new,
                        (List<UUID>)var12,
                        var11,
                        var2,
                        var3
                     );
                     var13.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                        this.plugin.getConfig().getBoolean("match.first-to", false)
                     );
                     var13.Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class(
                        (List<UUID>)var12
                     );
                     this.activeGames.put(var1, var13);
                     this.gameToOwner.put(var13, var1);

                     for (UUID var15 : var12) {
                        this.playerToGame.put(var15, var13);
                        this.plugin.getKitRulesListener().addActivePlayer(var15, var2);
                     }

                     this.clearPartyTransferring((Collection<UUID>)var12);
                     this.registerCrossServerPartyMatch(var13);
                     var6.setMode(PartyMode.IN_GAME);

                     for (UUID var30 : var12) {
                        this.plugin
                           .getArenaManager()
                           .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                              var30,
                              var11.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int()
                           );
                     }

                     this.plugin
                        .getChunkyIntegration()
                        .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                           var11
                        );
                     CompletableFuture var29 = this.spawnPlayersInCircle((List<UUID>)var12, var11, var2);
                     var13.Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class(
                        true
                     );
                     if (this.plugin.getPillarsOfFortuneManager() != null
                        && this.plugin
                           .getPillarsOfFortuneManager()
                           .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                              var2
                           )) {
                        this.plugin
                           .getPillarsOfFortuneManager()
                           .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                              var13, (List<UUID>)var12
                           );
                     }

                     this.plugin
                        .getSkyWarsLootManager()
                        .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                           var11, var2
                        );
                     ScoreboardState var31 = this.plugin
                        .getKitStateResolver()
                        .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                           var2, ScoreboardState.PARTY_FFA
                        );
                     TablistContext var16 = this.plugin
                        .getKitStateResolver()
                        .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                           var2, TablistContext.PARTY_FFA
                        );

                     for (UUID var18 : var12) {
                        Player var19 = Bukkit.getPlayer(var18);
                        if (var19 != null) {
                           this.plugin.getScoreboardManager().setState(var19, var31);
                           this.plugin.getScoreboardManager().setPlaceholder(var19, "in_party", "true");
                           this.plugin.getScoreboardManager().setPlaceholder(var19, "in_fight", "true");
                           this.plugin.getScoreboardManager().setPlaceholder(var19, "kit", this.getPartyKitDisplayName(var2));
                           this.plugin.getScoreboardManager().setPlaceholder(var19, "fight_kitname", this.getPartyKitDisplayName(var2));
                           this.plugin.getScoreboardManager().setPlaceholder(var19, "party_ffa_alive", String.valueOf(var12.size()));
                           this.plugin.getScoreboardManager().setPlaceholder(var19, "party_size", String.valueOf(var12.size()));
                           this.plugin.getScoreboardManager().setPlaceholder(var19, "party_max_size", String.valueOf(var6.getMaxSize()));
                        }
                     }

                     if (this.plugin.getTablistManager() != null) {
                        this.plugin.getTablistManager().setupPartyFFAContext((List<UUID>)var12, var13, var16);
                     }

                     if (this.getKitRule(var2, KitRule.PARKOUR)) {
                        for (UUID var33 : var12) {
                           this.plugin
                              .getParkourManager()
                              .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                                 var33,
                                 var11.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int()
                              );
                        }
                     }

                     this.forceShowGameParticipants((List<UUID>)var12);
                     this.startHpIndicator(var13, (Collection<UUID>)var12);
                     var29.thenRun(
                        () -> Bukkit.getScheduler()
                              .runTask(
                                 this.plugin,
                                 () -> {
                                    if (!var13.oO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000do()
                                       )
                                     {
                                       this.startCountdown(var12, this.cachedCountdownTime);
                                    }
                                 }
                              )
                     );
                  }
               }
            }
         }
      }
   }

   public void startPartySplit(UUID var1, String var2, int var3) {
      this.startPartySplitInternal(var1, var2, var3, null, null);
   }

   public void startPartySplit(UUID var1, String var2, int var3, List<UUID> var4, List<UUID> var5) {
      this.startPartySplitInternal(var1, var2, var3, var4, var5);
   }

   private List<UUID> buildSelectedSplitPlayers(Party var1, List<UUID> var2, List<UUID> var3) {
      if (var2 != null && var3 != null) {
         Set var4 = var1.getMembers();
         ArrayList var5 = new ArrayList();

         for (UUID var7 : var2) {
            if (var4.contains(var7) && !var5.contains(var7)) {
               var5.add(var7);
            }
         }

         for (UUID var10 : var3) {
            if (var4.contains(var10) && !var5.contains(var10)) {
               var5.add(var10);
            }
         }

         for (UUID var11 : var4) {
            if (!var5.contains(var11)) {
               var5.add(var11);
            }
         }

         return var5;
      } else {
         return new ArrayList<>(var1.getMembers());
      }
   }

   private boolean validatePartySplitTeams(UUID var1, List<UUID> var2, List<UUID> var3) {
      if (var2 != null && !var2.isEmpty() && var3 != null && !var3.isEmpty()) {
         return true;
      } else {
         Player var4 = Bukkit.getPlayer(var1);
         if (var4 != null) {
            var4.sendMessage(Component.text("Each team needs at least one online player before starting.").color(NamedTextColor.RED));
         }

         return false;
      }
   }

   private void startPartySplitInternal(UUID var1, String var2, int var3, List<UUID> var4, List<UUID> var5) {
      this.startPartySplitInternal(var1, var2, var3, var4, var5, null);
   }

   private void startPartySplitInternal(UUID var1, String var2, int var3, List<UUID> var4, List<UUID> var5, org.lime.swiftCore.arena.d var6) {
      Party var7 = this.partyManager.getParty(var1);
      if (var7 != null) {
         boolean var8 = this.isCustomKit(var2);
         if (var8) {
            if (this.getCustomKitData(var2) == null) {
               Player var9 = Bukkit.getPlayer(var1);
               if (var9 != null) {
                  HashMap var10 = new HashMap();
                  var10.put("kit", this.getPartyKitDisplayName(var2));
                  this.plugin
                     .getMessagesManager()
                     .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                        var9, "kit-not-found", var10
                     );
               }

               return;
            }
         } else if (!this.plugin
            .getKitManager()
            .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
               var2
            )) {
            Player var26 = Bukkit.getPlayer(var1);
            if (var26 != null) {
               HashMap var28 = new HashMap();
               var28.put("kit", var2);
               this.plugin
                  .getMessagesManager()
                  .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                     var26, "kit-not-found", var28
                  );
            }

            return;
         }

         Player var25 = Bukkit.getPlayer(var1);
         if (var8 || !ColorPartyKitGuard.blockPartyUse(this.plugin, var25, var2)) {
            Player var27 = Bukkit.getPlayer(var1);
            if (var8
               || var27 == null
               || this.plugin
                  .getKitManager()
                  .Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String(
                     var27, var2
                  )) {
               Set var11 = this.cachedPartySplitDisabledKits;
               if (var11.contains(var2.toLowerCase())) {
                  Player var31 = Bukkit.getPlayer(var1);
                  if (var31 != null) {
                     this.plugin
                        .getMessagesManager()
                        .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                           var31, "party-kit-disabled"
                        );
                  }
               } else if (this.plugin
                  .getFlowerCrownManager()
                  .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                     var2
                  )) {
                  Player var30 = Bukkit.getPlayer(var1);
                  if (var30 != null) {
                     this.plugin
                        .getMessagesManager()
                        .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                           var30, "party-flower-crown-disabled"
                        );
                  }
               } else if (var7.getSize() < 2) {
                  Player var29 = Bukkit.getPlayer(var1);
                  if (var29 != null) {
                     HashMap var32 = new HashMap();
                     var32.put("min", "2");
                     this.plugin
                        .getMessagesManager()
                        .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                           var29, "party-need-members", var32
                        );
                  }
               } else {
                  List var12 = this.buildSelectedSplitPlayers(var7, var4, var5);
                  var12.removeIf(var0 -> Bukkit.getPlayer(var0) == null || !Bukkit.getPlayer(var0).isOnline());
                  boolean var13 = var4 != null && var5 != null;
                  if (!var13) {
                     Collections.shuffle(var12);
                  }

                  ArrayList var14;
                  ArrayList var15;
                  if (var13) {
                     var14 = new ArrayList(var4);
                     var15 = new ArrayList(var5);
                     var14.removeIf(var1x -> !var12.contains(var1x));
                     var15.removeIf(var1x -> !var12.contains(var1x));
                  } else {
                     int var16 = var12.size() / 2;
                     var14 = new ArrayList(var12.subList(0, var16));
                     var15 = new ArrayList(var12.subList(var16, var12.size()));
                  }

                  if (this.validatePartySplitTeams(var1, var14, var15)) {
                     if (this.shouldTransferCrossServer()) {
                        if (var12.remove(var1)) {
                           var12.add(0, var1);
                        }

                        this.transferPartyMatch(
                           org.lime.swiftCore.v.f.c.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int,
                           var2,
                           var12,
                           var14,
                           var15,
                           var3
                        );
                     } else {
                        ArenaType var33 = this.getArenaTypeForKit(var2);
                        org.lime.swiftCore.arena.d var17 = var6 != null
                           ? var6
                           : this.plugin
                              .getArenaManager()
                              .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
                                 var33, var2
                              );
                        if (var17 == null) {
                           Set var34 = var7.getMembers();
                           this.acquirePartyArena("SPLIT:" + var1, var1, var33, var2, var7x -> {
                              Party var8x = this.partyManager.getParty(var1);
                              if (var8x != null && var8x.getMembers().equals(var34)) {
                                 this.startPartySplitInternal(var1, var2, var3, var4, var5, var7x);
                              } else {
                                 this.releasePartyArena(var7x);
                              }
                           });
                        } else if (var17.Oo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000returnsuper()
                              != org.lime.swiftCore.arena.c.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super
                           && !this.plugin
                              .getArenaManager()
                              .ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float(
                                 var17
                              )) {
                           this.releasePartyArena(var17);
                        } else {
                           d var18 = new d(
                              e.Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String,
                              var12,
                              var17,
                              var2,
                              var3
                           );
                           var18.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                              this.plugin.getConfig().getBoolean("match.first-to", false)
                           );
                           var18.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
                              new ArrayList<>(var14)
                           );
                           var18.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                              new ArrayList<>(var15)
                           );
                           var18.Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String(
                              new ArrayList<>(var14)
                           );
                           var18.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
                              new ArrayList<>(var15)
                           );
                           this.activeGames.put(var1, var18);
                           this.gameToOwner.put(var18, var1);

                           for (UUID var20 : var12) {
                              this.playerToGame.put(var20, var18);
                              this.plugin.getKitRulesListener().addActivePlayer(var20, var2);
                           }

                           this.clearPartyTransferring(var12);
                           this.registerCrossServerPartyMatch(var18);
                           var7.setMode(PartyMode.IN_GAME);

                           for (UUID var37 : var12) {
                              this.plugin
                                 .getArenaManager()
                                 .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                                    var37,
                                    var17.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int()
                                 );
                           }

                           this.plugin
                              .getChunkyIntegration()
                              .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                                 var17
                              );
                           CompletableFuture var36 = CompletableFuture.allOf(
                              this.teleportTeamToPosition(
                                 var14,
                                 var17.öo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000privatesuper(),
                                 var2,
                                 true
                              ),
                              this.teleportTeamToPosition(
                                 var15,
                                 var17.ôO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000newsuper(),
                                 var2,
                                 false
                              )
                           );
                           var18.Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class(
                              true
                           );
                           if (this.plugin.getPillarsOfFortuneManager() != null
                              && this.plugin
                                 .getPillarsOfFortuneManager()
                                 .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                                    var2
                                 )) {
                              this.plugin
                                 .getPillarsOfFortuneManager()
                                 .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                                    var18, var12
                                 );
                           }

                           if (this.getKitRule(var2, KitRule.BEDWARS)) {
                              var17.õO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Objectsuper();
                           }

                           this.plugin
                              .getSkyWarsLootManager()
                              .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                                 var17, var2
                              );
                           ScoreboardState var38 = this.plugin
                              .getKitStateResolver()
                              .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                                 var2, ScoreboardState.PARTY_SPLIT
                              );
                           TablistContext var21 = this.plugin
                              .getKitStateResolver()
                              .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                                 var2, TablistContext.PARTY_SPLIT
                              );

                           for (int var22 = 0; var22 < var12.size(); var22++) {
                              UUID var23 = (UUID)var12.get(var22);
                              Player var24 = Bukkit.getPlayer(var23);
                              if (var24 != null) {
                                 this.plugin.getScoreboardManager().setState(var24, var38);
                                 this.plugin.getScoreboardManager().setPlaceholder(var24, "in_party", "true");
                                 this.plugin.getScoreboardManager().setPlaceholder(var24, "in_fight", "true");
                                 this.plugin.getScoreboardManager().setPlaceholder(var24, "kit", this.getPartyKitDisplayName(var2));
                                 this.plugin.getScoreboardManager().setPlaceholder(var24, "fight_kitname", this.getPartyKitDisplayName(var2));
                                 this.plugin.getScoreboardManager().setPlaceholder(var24, "team", var14.contains(var23) ? "Blue" : "Red");
                                 this.plugin.getScoreboardManager().setPlaceholder(var24, "blue_alive", String.valueOf(var14.size()));
                                 this.plugin.getScoreboardManager().setPlaceholder(var24, "red_alive", String.valueOf(var15.size()));
                                 this.plugin.getScoreboardManager().setPlaceholder(var24, "blue_total", String.valueOf(var14.size()));
                                 this.plugin.getScoreboardManager().setPlaceholder(var24, "red_total", String.valueOf(var15.size()));
                                 this.plugin.getScoreboardManager().setPlaceholder(var24, "party_size", String.valueOf(var12.size()));
                                 this.plugin.getScoreboardManager().setPlaceholder(var24, "party_max_size", String.valueOf(var7.getMaxSize()));
                                 if (var3 > 1) {
                                    this.plugin.getScoreboardManager().setPlaceholder(var24, "round", "1");
                                    this.plugin.getScoreboardManager().setPlaceholder(var24, "total_rounds", String.valueOf(var3));
                                    this.plugin.getScoreboardManager().setPlaceholder(var24, "blue_wins", "0");
                                    this.plugin.getScoreboardManager().setPlaceholder(var24, "red_wins", "0");
                                 }
                              }
                           }

                           if (this.plugin.getTablistManager() != null) {
                              this.plugin
                                 .getTablistManager()
                                 .setupPartySplitContext(
                                    var18.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int(),
                                    var18.ÒÒ00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000newnew(),
                                    var18,
                                    var21
                                 );
                           }

                           if (this.getKitRule(var2, KitRule.PARKOUR)) {
                              for (UUID var40 : var12) {
                                 this.plugin
                                    .getParkourManager()
                                    .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                                       var40,
                                       var17.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int()
                                    );
                              }
                           }

                           this.forceShowGameParticipants(var12);
                           this.startHpIndicator(var18, var12);
                           var36.thenRun(
                              () -> Bukkit.getScheduler()
                                    .runTask(
                                       this.plugin,
                                       () -> {
                                          if (!var18.oO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000do()
                                             )
                                           {
                                             this.startCountdown(var12, this.cachedCountdownTime, var18);
                                          }
                                       }
                                    )
                           );
                        }
                     }
                  }
               }
            }
         }
      }
   }

   public void startPartyVsParty(UUID var1, UUID var2, String var3) {
      this.startPartyVsPartyInternal(var1, var2, var3, 1);
   }

   public void startPartyVsParty(UUID var1, UUID var2, String var3, int var4) {
      this.startPartyVsPartyInternal(var1, var2, var3, var4);
   }

   public void startPartyVsPartyFromIntent(UUID var1, UUID var2, String var3, int var4, List<UUID> var5, List<UUID> var6, String var7) {
      org.lime.swiftCore.arena.d var8 = this.resolveStagingArena(var7);
      this.startPartyVsPartyInternal(var1, var2, var3, var4, var8, var5, var6);
      if (var8 != null && this.activeGames.get(var1) == null && this.activeGames.get(var2) == null) {
         this.releasePartyArena(var8);
      }
   }

   public void startPartySplitFromIntent(UUID var1, String var2, int var3, List<UUID> var4, List<UUID> var5, String var6) {
      org.lime.swiftCore.arena.d var7 = this.resolveStagingArena(var6);
      this.startPartySplitInternal(var1, var2, var3, var4, var5, var7);
      if (var7 != null && this.activeGames.get(var1) == null) {
         this.releasePartyArena(var7);
      }
   }

   public void startPartyFFAFromIntent(UUID var1, String var2, int var3, List<UUID> var4, String var5) {
      org.lime.swiftCore.arena.d var6 = this.resolveStagingArena(var5);
      this.startPartyFFAInternal(var1, var2, var3, var6, var4);
      if (var6 != null && this.activeGames.get(var1) == null) {
         this.releasePartyArena(var6);
      }
   }

   public boolean startTeamQueueMatch(org.lime.swiftCore.fb.c var1, List<UUID> var2, List<UUID> var3, String var4, String var5) {
      return this.startTeamQueueMatch(var1, var2, var3, var4, var5, null);
   }

   public boolean startTeamQueueMatch(org.lime.swiftCore.fb.c var1, List<UUID> var2, List<UUID> var3, String var4, String var5, Runnable var6) {
      if (!Bukkit.isPrimaryThread()) {
         Bukkit.getScheduler().runTask(this.plugin, () -> this.startTeamQueueMatch(var1, var2, var3, var4, var5, var6));
         return true;
      } else if (var1 != null
         && var2 != null
         && var3 != null
         && var2.size() >= 2
         && var2.size() <= 4
         && var2.size() == var3.size()
         && this.plugin
            .getKitManager()
            .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
               var4
            )
         && !this.isPartyVsKitDisabled(var4)) {
         ArrayList var7 = new ArrayList(var2);
         ArrayList var8 = new ArrayList(var3);
         if (var1
               == org.lime.swiftCore.fb.c.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super
            && !this.matchesCurrentPartyRosters(var7, var8)) {
            return false;
         } else {
            ArrayList var9 = new ArrayList(var7);
            var9.addAll(var8);
            if (var9.stream().distinct().count() == (long)var9.size() && !var9.stream().anyMatch(var1x -> {
               Player var2x = Bukkit.getPlayer(var1x);
               return var2x == null || !var2x.isOnline() || this.playerToGame.containsKey(var1x);
            })) {
               org.lime.swiftCore.arena.d var10 = this.resolveStagingArena(var5);
               if (var10 == null) {
                  ArenaType var11 = this.getArenaTypeForKit(var4);
                  var10 = this.plugin
                     .getArenaManager()
                     .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
                        var11, var4
                     );
                  if (var10 == null && (var5 == null || var5.isBlank()) && this.plugin.getDynamicArenaManager() != null) {
                     String var25 = "TEAM_QUEUE:" + var9.stream().map(var0 -> var0.toString()).sorted().collect(Collectors.joining(":"));
                     if (!this.pendingArenaStarts.add(var25)) {
                        return true;
                     }

                     this.plugin
                        .getDynamicArenaManager()
                        .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
                           var11, var4
                        )
                        .whenComplete(
                           (var8x, var9x) -> Bukkit.getScheduler()
                                 .runTask(
                                    this.plugin,
                                    () -> {
                                       this.pendingArenaStarts.remove(var25);
                                       boolean var10x = var9x == null
                                          && var8x != null
                                          && this.startTeamQueueMatch(
                                             var1,
                                             var2,
                                             var3,
                                             var4,
                                             var8x.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int(),
                                             null
                                          );
                                       if (!var10x) {
                                          this.releasePartyArena(var8x);

                                          for (UUID var12x : var9) {
                                             Player var13x = Bukkit.getPlayer(var12x);
                                             if (var13x != null && var13x.isOnline()) {
                                                this.plugin
                                                   .getMessagesManager()
                                                   .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                                                      var13x, "team-queue-no-arenas"
                                                   );
                                             }
                                          }

                                          if (var6 != null) {
                                             var6.run();
                                          }
                                       }
                                    }
                                 )
                        );
                     return true;
                  }
               }

               if (var10 != null
                  && (
                     var10.Oo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000returnsuper()
                           == org.lime.swiftCore.arena.c.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super
                        || this.plugin
                           .getArenaManager()
                           .ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float(
                              var10
                           )
                  )) {
                  e var24 = var1
                        == org.lime.swiftCore.fb.c.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super
                     ? e.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super
                     : e.Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String;
                  int var12 = this.plugin.getTeamQueueManager() != null
                     ? this.plugin
                        .getTeamQueueManager()
                        .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new()
                     : 1;
                  d var13 = new d(var24, var9, var10, var4, var12);
                  var13.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                     this.plugin.getTeamQueueManager() != null
                        && this.plugin
                           .getTeamQueueManager()
                           .Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class()
                  );
                  var13.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                     var1
                  );
                  var13.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
                     var7
                  );
                  var13.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                     var8
                  );
                  var13.Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String(
                     var7
                  );
                  var13.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
                     var8
                  );
                  UUID var14 = (UUID)var7.get(0);
                  this.activeGames.put(var14, var13);
                  if (var24
                     == e.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super
                     )
                   {
                     this.activeGames.put((UUID)var8.get(0), var13);
                  }

                  this.gameToOwner.put(var13, var14);

                  for (UUID var16 : var9) {
                     this.playerToGame.put(var16, var13);
                     this.plugin.getKitRulesListener().addActivePlayer(var16, var4);
                     this.plugin
                        .getArenaManager()
                        .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                           var16,
                           var10.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int()
                        );
                  }

                  this.clearPartyTransferring(var9);
                  this.registerCrossServerPartyMatch(var13);
                  this.plugin
                     .getChunkyIntegration()
                     .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                        var10
                     );
                  CompletableFuture var26 = CompletableFuture.allOf(
                     this.teleportTeamToPosition(
                        var7,
                        var10.öo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000privatesuper(),
                        var4,
                        true
                     ),
                     this.teleportTeamToPosition(
                        var8,
                        var10.ôO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000newsuper(),
                        var4,
                        false
                     )
                  );
                  var13.Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class(
                     true
                  );
                  if (this.plugin.getPillarsOfFortuneManager() != null
                     && this.plugin
                        .getPillarsOfFortuneManager()
                        .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                           var4
                        )) {
                     this.plugin
                        .getPillarsOfFortuneManager()
                        .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                           var13, var9
                        );
                  }

                  if (this.getKitRule(var4, KitRule.BEDWARS)) {
                     var10.õO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Objectsuper();
                  }

                  this.plugin
                     .getSkyWarsLootManager()
                     .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                        var10, var4
                     );
                  ScoreboardState var27 = var1
                        == org.lime.swiftCore.fb.c.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super
                     ? ScoreboardState.PARTY_VS
                     : ScoreboardState.PARTY_SPLIT;
                  TablistContext var17 = var1
                        == org.lime.swiftCore.fb.c.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super
                     ? TablistContext.PARTY_VS
                     : TablistContext.PARTY_SPLIT;
                  ScoreboardState var18 = this.plugin
                     .getKitStateResolver()
                     .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                        var4, var27
                     );
                  TablistContext var19 = this.plugin
                     .getKitStateResolver()
                     .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                        var4, var17
                     );

                  for (UUID var21 : var9) {
                     Player var22 = Bukkit.getPlayer(var21);
                     if (var22 != null) {
                        boolean var23 = var7.contains(var21);
                        this.plugin.getScoreboardManager().setState(var22, var18);
                        this.plugin
                           .getScoreboardManager()
                           .setPlaceholder(
                              var22,
                              "in_party",
                              String.valueOf(
                                 var1
                                    == org.lime.swiftCore.fb.c.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super
                              )
                           );
                        this.plugin.getScoreboardManager().setPlaceholder(var22, "in_fight", "true");
                        this.plugin.getScoreboardManager().setPlaceholder(var22, "kit", this.getPartyKitDisplayName(var4));
                        this.plugin.getScoreboardManager().setPlaceholder(var22, "fight_kitname", this.getPartyKitDisplayName(var4));
                        this.plugin.getScoreboardManager().setPlaceholder(var22, "team", var23 ? "Blue" : "Red");
                        this.plugin.getScoreboardManager().setPlaceholder(var22, "blue_alive", String.valueOf(var7.size()));
                        this.plugin.getScoreboardManager().setPlaceholder(var22, "red_alive", String.valueOf(var8.size()));
                        this.plugin.getScoreboardManager().setPlaceholder(var22, "blue_total", String.valueOf(var7.size()));
                        this.plugin.getScoreboardManager().setPlaceholder(var22, "red_total", String.valueOf(var8.size()));
                        this.plugin.getScoreboardManager().setPlaceholder(var22, "your_party_alive", String.valueOf(var23 ? var7.size() : var8.size()));
                        this.plugin.getScoreboardManager().setPlaceholder(var22, "your_party_total", String.valueOf(var23 ? var7.size() : var8.size()));
                        this.plugin.getScoreboardManager().setPlaceholder(var22, "enemy_party_alive", String.valueOf(var23 ? var8.size() : var7.size()));
                        this.plugin.getScoreboardManager().setPlaceholder(var22, "enemy_party_total", String.valueOf(var23 ? var8.size() : var7.size()));
                        this.plugin.getScoreboardManager().setPlaceholder(var22, "party_size", String.valueOf(var7.size()));
                        this.plugin.getScoreboardManager().setPlaceholder(var22, "party_max_size", String.valueOf(var7.size()));
                     }
                  }

                  if (this.plugin.getTablistManager() != null) {
                     if (var1
                        == org.lime.swiftCore.fb.c.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super
                        )
                      {
                        this.plugin.getTablistManager().setupPartyVsContext(var7, var8, var13, var19);
                     } else {
                        this.plugin.getTablistManager().setupPartySplitContext(var7, var8, var13, var19);
                     }

                     for (UUID var30 : var9) {
                        Player var32 = Bukkit.getPlayer(var30);
                        if (var32 != null && var32.isOnline()) {
                           this.plugin.getTablistManager().setPlaceholder(var32, "team_queue_size", String.valueOf(var7.size()));
                           this.plugin.getTablistManager().setPlaceholder(var32, "team_queue_mode", var1.name().toLowerCase(Locale.ROOT));
                           this.plugin.getTablistManager().setupTeamQueueContext(var32, var9);
                        }
                     }
                  }

                  if (this.getKitRule(var4, KitRule.PARKOUR)) {
                     for (UUID var31 : var9) {
                        this.plugin
                           .getParkourManager()
                           .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                              var31,
                              var10.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int()
                           );
                     }
                  }

                  this.forceShowGameParticipants(var9);
                  this.startHpIndicator(var13, var9);
                  var26.thenRun(
                     () -> Bukkit.getScheduler()
                           .runTask(
                              this.plugin,
                              () -> {
                                 if (!var13.oO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000do()
                                    )
                                  {
                                    this.startCountdown(var9, this.cachedCountdownTime, var13);
                                 }
                              }
                           )
                  );
                  return true;
               } else {
                  return false;
               }
            } else {
               return false;
            }
         }
      } else {
         return false;
      }
   }

   private boolean matchesCurrentPartyRosters(List<UUID> var1, List<UUID> var2) {
      Party var3 = this.partyManager.getPlayerParty((UUID)var1.get(0));
      Party var4 = this.partyManager.getPlayerParty((UUID)var2.get(0));
      return var3 == null && var4 == null
         ? true
         : var3 != null
            && var4 != null
            && var3.isOwner((UUID)var1.get(0))
            && var4.isOwner((UUID)var2.get(0))
            && new HashSet<>(var3.getMembers()).equals(new HashSet(var1))
            && new HashSet<>(var4.getMembers()).equals(new HashSet(var2));
   }

   private void startPartyVsPartyInternal(UUID var1, UUID var2, String var3, int var4) {
      this.startPartyVsPartyInternal(var1, var2, var3, var4, null, null, null);
   }

   private void startPartyVsPartyInternal(UUID var1, UUID var2, String var3, int var4, org.lime.swiftCore.arena.d var5) {
      this.startPartyVsPartyInternal(var1, var2, var3, var4, var5, null, null);
   }

   private void startPartyVsPartyInternal(UUID var1, UUID var2, String var3, int var4, org.lime.swiftCore.arena.d var5, List<UUID> var6, List<UUID> var7) {
      Party var8 = this.partyManager.getParty(var1);
      Party var9 = this.partyManager.getParty(var2);
      if (var8 != null && var9 != null) {
         boolean var10 = this.isCustomKit(var3);
         if (var10) {
            if (this.getCustomKitData(var3) == null) {
               Player var11 = Bukkit.getPlayer(var1);
               if (var11 != null) {
                  HashMap var12 = new HashMap();
                  var12.put("kit", this.getPartyKitDisplayName(var3));
                  this.plugin
                     .getMessagesManager()
                     .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                        var11, "kit-not-found", var12
                     );
               }

               return;
            }
         } else if (!this.plugin
            .getKitManager()
            .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
               var3
            )) {
            Player var34 = Bukkit.getPlayer(var1);
            if (var34 != null) {
               HashMap var36 = new HashMap();
               var36.put("kit", var3);
               this.plugin
                  .getMessagesManager()
                  .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                     var34, "kit-not-found", var36
                  );
            }

            return;
         }

         Player var33 = Bukkit.getPlayer(var1);
         if (var10 || !ColorPartyKitGuard.blockPartyUse(this.plugin, var33, var3)) {
            Player var35 = Bukkit.getPlayer(var1);
            Player var13 = Bukkit.getPlayer(var2);
            boolean var14 = var10
               || var35 == null
               || this.plugin
                  .getKitManager()
                  .Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String(
                     var35, var3
                  );
            boolean var15 = var10
               || var13 == null
               || this.plugin
                  .getKitManager()
                  .Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String(
                     var13, var3
                  );
            if (var14 && var15) {
               Set var16 = this.cachedPartyVsDisabledKits;
               if (var16.contains(var3.toLowerCase())) {
                  Player var38 = Bukkit.getPlayer(var1);
                  if (var38 != null) {
                     this.plugin
                        .getMessagesManager()
                        .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                           var38, "party-kit-disabled"
                        );
                  }
               } else if (this.plugin
                  .getFlowerCrownManager()
                  .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                     var3
                  )) {
                  Player var37 = Bukkit.getPlayer(var1);
                  if (var37 != null) {
                     this.plugin
                        .getMessagesManager()
                        .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                           var37, "party-flower-crown-disabled"
                        );
                  }
               } else {
                  boolean var17 = this.cachedAllowImbalance;
                  boolean var18 = var8.isAllowImbalance() || var9.isAllowImbalance();
                  if (!var17 && !var18 && var8.getSize() != var9.getSize()) {
                     this.plugin
                        .getMessagesManager()
                        .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                           Bukkit.getPlayer(var1), "party-imbalance-not-allowed"
                        );
                  } else if (this.shouldTransferCrossServer()) {
                     ArrayList var39 = new ArrayList<>(var8.getMembers());
                     var39.removeIf(var0 -> Bukkit.getPlayer(var0) == null || !Bukkit.getPlayer(var0).isOnline());
                     if (!var39.contains(var1)) {
                        var39.add(0, var1);
                     }

                     ArrayList var40 = new ArrayList<>(var9.getMembers());
                     var40.removeIf(var0 -> Bukkit.getPlayer(var0) == null || !Bukkit.getPlayer(var0).isOnline());
                     if (!var40.contains(var2)) {
                        var40.add(0, var2);
                     }

                     ArrayList var42 = new ArrayList(var39);
                     var42.addAll(var40);
                     this.transferPartyMatch(
                        org.lime.swiftCore.v.f.c.Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String,
                        var3,
                        var42,
                        var39,
                        var40,
                        var4
                     );
                  } else {
                     ArenaType var19 = this.getArenaTypeForKit(var3);
                     org.lime.swiftCore.arena.d var20 = var5 != null
                        ? var5
                        : this.plugin
                           .getArenaManager()
                           .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
                              var19, var3
                           );
                     if (var20 == null) {
                        Set var41 = var8.getMembers();
                        Set var43 = var9.getMembers();
                        this.acquirePartyArena(
                           "VS:" + var1 + ":" + var2,
                           var1,
                           var19,
                           var3,
                           var7x -> {
                              Party var8x = this.partyManager.getParty(var2);
                              if (var8x != null
                                 && var8x.getMode() == PartyMode.IDLE
                                 && var8x.getSize() >= 1
                                 && !this.activeGames.containsKey(var2)
                                 && this.partyManager.getParty(var1).getMembers().equals(var41)
                                 && var8x.getMembers().equals(var43)) {
                                 this.startPartyVsPartyInternal(var1, var2, var3, var4, var7x);
                              } else {
                                 this.releasePartyArena(var7x);
                              }
                           }
                        );
                     } else if (var20.Oo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000returnsuper()
                           != org.lime.swiftCore.arena.c.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super
                        && !this.plugin
                           .getArenaManager()
                           .ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float(
                              var20
                           )) {
                        this.releasePartyArena(var20);
                     } else {
                        Object var21;
                        Object var22;
                        if (var6 != null && var7 != null && !var6.isEmpty() && !var7.isEmpty()) {
                           var21 = this.filterOnlinePlayers(var6);
                           var22 = this.filterOnlinePlayers(var7);
                        } else {
                           var21 = new ArrayList<>(var8.getMembers());
                           var21.removeIf(var0 -> Bukkit.getPlayer(var0) == null || !Bukkit.getPlayer(var0).isOnline());
                           var22 = new ArrayList<>(var9.getMembers());
                           var22.removeIf(var0 -> Bukkit.getPlayer(var0) == null || !Bukkit.getPlayer(var0).isOnline());
                        }

                        ArrayList var23 = new ArrayList();
                        var23.addAll((Collection)var21);
                        var23.addAll((Collection)var22);
                        d var24 = new d(
                           e.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super,
                           var23,
                           var20,
                           var3,
                           var4
                        );
                        var24.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                           this.plugin.getConfig().getBoolean("match.first-to", false)
                        );
                        var24.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
                           new ArrayList<>((Collection<? extends UUID>)var21)
                        );
                        var24.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                           new ArrayList<>((Collection<? extends UUID>)var22)
                        );
                        var24.Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String(
                           new ArrayList<>((Collection<? extends UUID>)var21)
                        );
                        var24.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
                           new ArrayList<>((Collection<? extends UUID>)var22)
                        );
                        this.activeGames.put(var1, var24);
                        this.activeGames.put(var2, var24);
                        this.gameToOwner.put(var24, var1);

                        for (UUID var26 : var23) {
                           this.playerToGame.put(var26, var24);
                           this.plugin.getKitRulesListener().addActivePlayer(var26, var3);
                        }

                        this.clearPartyTransferring(var23);
                        this.registerCrossServerPartyMatch(var24);
                        var8.setMode(PartyMode.IN_GAME);
                        var9.setMode(PartyMode.IN_GAME);

                        for (UUID var46 : var23) {
                           this.plugin
                              .getArenaManager()
                              .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                                 var46,
                                 var20.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int()
                              );
                        }

                        this.plugin
                           .getChunkyIntegration()
                           .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                              var20
                           );
                        CompletableFuture var45 = CompletableFuture.allOf(
                           this.teleportTeamToPosition(
                              (Collection<UUID>)var21,
                              var20.öo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000privatesuper(),
                              var3,
                              true
                           ),
                           this.teleportTeamToPosition(
                              (Collection<UUID>)var22,
                              var20.ôO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000newsuper(),
                              var3,
                              false
                           )
                        );
                        var24.Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class(
                           true
                        );
                        if (this.plugin.getPillarsOfFortuneManager() != null
                           && this.plugin
                              .getPillarsOfFortuneManager()
                              .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                                 var3
                              )) {
                           this.plugin
                              .getPillarsOfFortuneManager()
                              .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                                 var24, var23
                              );
                        }

                        if (this.getKitRule(var3, KitRule.BEDWARS)) {
                           var20.õO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Objectsuper();
                        }

                        this.plugin
                           .getSkyWarsLootManager()
                           .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                              var20, var3
                           );
                        ScoreboardState var47 = this.plugin
                           .getKitStateResolver()
                           .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                              var3, ScoreboardState.PARTY_VS
                           );
                        TablistContext var27 = this.plugin
                           .getKitStateResolver()
                           .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                              var3, TablistContext.PARTY_VS
                           );

                        for (UUID var29 : var23) {
                           Player var30 = Bukkit.getPlayer(var29);
                           if (var30 != null) {
                              boolean var31 = var8.isMember(var29);
                              this.plugin.getScoreboardManager().setState(var30, var47);
                              this.plugin.getScoreboardManager().setPlaceholder(var30, "in_party", "true");
                              this.plugin.getScoreboardManager().setPlaceholder(var30, "in_fight", "true");
                              this.plugin.getScoreboardManager().setPlaceholder(var30, "kit", this.getPartyKitDisplayName(var3));
                              this.plugin.getScoreboardManager().setPlaceholder(var30, "fight_kitname", this.getPartyKitDisplayName(var3));
                              if (var31) {
                                 this.plugin.getScoreboardManager().setPlaceholder(var30, "your_party_alive", String.valueOf(var8.getSize()));
                                 this.plugin.getScoreboardManager().setPlaceholder(var30, "your_party_total", String.valueOf(var8.getSize()));
                                 this.plugin.getScoreboardManager().setPlaceholder(var30, "enemy_party_alive", String.valueOf(var9.getSize()));
                                 this.plugin.getScoreboardManager().setPlaceholder(var30, "enemy_party_total", String.valueOf(var9.getSize()));
                              } else {
                                 this.plugin.getScoreboardManager().setPlaceholder(var30, "your_party_alive", String.valueOf(var9.getSize()));
                                 this.plugin.getScoreboardManager().setPlaceholder(var30, "your_party_total", String.valueOf(var9.getSize()));
                                 this.plugin.getScoreboardManager().setPlaceholder(var30, "enemy_party_alive", String.valueOf(var8.getSize()));
                                 this.plugin.getScoreboardManager().setPlaceholder(var30, "enemy_party_total", String.valueOf(var8.getSize()));
                              }

                              Party var32 = var31 ? var8 : var9;
                              this.plugin.getScoreboardManager().setPlaceholder(var30, "party_size", String.valueOf(var32.getSize()));
                              this.plugin.getScoreboardManager().setPlaceholder(var30, "party_max_size", String.valueOf(var32.getMaxSize()));
                              if (var4 > 1) {
                                 this.plugin.getScoreboardManager().setPlaceholder(var30, "round", "1");
                                 this.plugin.getScoreboardManager().setPlaceholder(var30, "total_rounds", String.valueOf(var4));
                                 this.plugin.getScoreboardManager().setPlaceholder(var30, "blue_wins", "0");
                                 this.plugin.getScoreboardManager().setPlaceholder(var30, "red_wins", "0");
                              }
                           }
                        }

                        if (this.plugin.getTablistManager() != null) {
                           this.plugin
                              .getTablistManager()
                              .setupPartyVsContext(
                                 var24.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int(),
                                 var24.ÒÒ00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000newnew(),
                                 var24,
                                 var27
                              );
                        }

                        if (this.getKitRule(var3, KitRule.PARKOUR)) {
                           for (UUID var49 : var23) {
                              this.plugin
                                 .getParkourManager()
                                 .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                                    var49,
                                    var20.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int()
                                 );
                           }
                        }

                        this.forceShowGameParticipants(var23);
                        this.startHpIndicator(var24, var23);
                        var45.thenRun(
                           () -> Bukkit.getScheduler()
                                 .runTask(
                                    this.plugin,
                                    () -> {
                                       if (!var24.oO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000do()
                                          )
                                        {
                                          this.startCountdown(var23, this.cachedCountdownTime, var24);
                                       }
                                    }
                                 )
                        );
                     }
                  }
               }
            }
         }
      }
   }

   private CompletableFuture<Void> spawnPlayersInCircle(List<UUID> var1, org.lime.swiftCore.arena.d var2, String var3) {
      Location var4 = var2.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super();
      double var5 = this.cachedFfaSpawnRadius;
      double var7 = (Math.PI * 2) / (double)var1.size();
      boolean var9 = this.plugin.getPillarsOfFortuneManager() != null
         && this.plugin
            .getPillarsOfFortuneManager()
            .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
               var3
            );
      ArrayList var10 = new ArrayList();

      for (int var11 = 0; var11 < var1.size(); var11++) {
         UUID var12 = (UUID)var1.get(var11);
         Player var13 = Bukkit.getPlayer(var12);
         if (var13 != null) {
            double var14 = (double)var11 * var7;
            Location var16 = var9
               ? this.plugin
                  .getPillarsOfFortuneManager()
                  .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                     var2, var11
                  )
               : null;
            if (var16 == null) {
               double var17 = var4.getX() + var5 * Math.cos(var14);
               double var19 = var4.getZ() + var5 * Math.sin(var14);
               var16 = new Location(var4.getWorld(), var17, var4.getY(), var19);
               var16.setYaw((float)Math.toDegrees(var14 + Math.PI));
            }

            for (PotionEffect var18 : var13.getActivePotionEffects()) {
               var13.removePotionEffect(var18.getType());
            }

            var13.setFallDistance(0.0F);
            var13.setFireTicks(0);
            var13.getInventory().clear();
            var13.setInvulnerable(true);
            var13.setCollidable(true);
            this.applyKitHealth(var13, var3);
            this.plugin.getArenaListener().addTeleportGracePeriod(var12);
            CompletableFuture var22 = new CompletableFuture();
            var10.add(var22);
            var13.teleportAsync(var16).whenComplete((var4x, var5x) -> Bukkit.getScheduler().runTask(this.plugin, () -> {
                  Player var6 = Bukkit.getPlayer(var12);
                  if (var5x == null && Boolean.TRUE.equals(var4x) && var6 != null && var6.isOnline()) {
                     var6.setFallDistance(0.0F);
                     var6.setGameMode(GameMode.SURVIVAL);
                     var6.setAllowFlight(false);
                     var6.setFlying(false);
                     var6.setInvulnerable(false);
                     var6.setCollidable(true);
                     var6.getInventory().clear();
                     this.givePartyKit(var6, var3);
                  }

                  var22.complete(null);
               }));
         }
      }

      return CompletableFuture.allOf(var10.toArray(CompletableFuture[]::new));
   }

   private void acquirePartyArena(String var1, UUID var2, ArenaType var3, String var4, Consumer<org.lime.swiftCore.arena.d> var5) {
      if (this.pendingArenaStarts.add(var1)) {
         this.plugin
            .getDynamicArenaManager()
            .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
               var3, var4
            )
            .whenComplete(
               (var4x, var5x) -> Bukkit.getScheduler()
                     .runTask(
                        this.plugin,
                        () -> {
                           this.pendingArenaStarts.remove(var1);
                           Player var6 = Bukkit.getPlayer(var2);
                           if (var5x != null) {
                              if (var6 != null && var6.isOnline()) {
                                 this.plugin
                                    .getMessagesManager()
                                    .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                                       var6, "party-no-arenas"
                                    );
                              }
                           } else {
                              Party var7 = this.partyManager.getParty(var2);
                              if (var6 != null
                                 && var6.isOnline()
                                 && var7 != null
                                 && var7.getMode() == PartyMode.IDLE
                                 && var7.getSize() >= 2
                                 && !this.activeGames.containsKey(var2)) {
                                 var5.accept(var4x);
                              } else {
                                 this.releasePartyArena(var4x);
                              }
                           }
                        }
                     )
            );
      }
   }

   private void releasePartyArena(org.lime.swiftCore.arena.d var1) {
      if (var1 != null
         && var1.ØO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000supersuper()
         && this.plugin.getDynamicArenaManager() != null) {
         this.plugin
            .getDynamicArenaManager()
            .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
               var1
            );
      } else if (var1 != null) {
         this.plugin
            .getArenaManager()
            .Ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000void(
               var1
            );
      }
   }

   private void givePartyKit(Player var1, String var2) {
      if (var1 != null && var1.isOnline()) {
         if (!this.isCustomKit(var2)) {
            this.plugin
               .getKitManager()
               .Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return(
                  var1, var2
               );
         } else {
            CustomKitAPI.CustomKitData var3 = this.getCustomKitData(var2);
            if (var3 != null) {
               if (this.plugin.getSpawnItemsManager() != null) {
                  this.plugin.getSpawnItemsManager().clearTracking(var1);
               }

               var1.getInventory().setContents(var3.contents() != null ? (ItemStack[])var3.contents().clone() : new ItemStack[41]);
               var1.getInventory().setArmorContents(var3.armor() != null ? (ItemStack[])var3.armor().clone() : new ItemStack[4]);
               var1.updateInventory();
            }
         }
      }
   }

   private CompletableFuture<Void> teleportTeamToPosition(Collection<UUID> var1, Location var2, String var3, boolean var4) {
      boolean var5 = this.getKitRule(var3, KitRule.BRIDGE);
      boolean var6 = this.getKitRule(var3, KitRule.BEDWARS);
      boolean var7 = this.cachedKitColoredArmor || var5 || var6;
      String var8 = var4 ? "blue" : "red";
      ArrayList var9 = new ArrayList(var1);
      ArrayList var10 = new ArrayList();
      double var11 = Math.max(0.5, this.plugin.getConfig().getDouble("party.team-spawn-spacing", 1.5));
      double var13 = Math.toRadians((double)var2.getYaw());
      double var15 = Math.cos(var13);
      double var17 = Math.sin(var13);

      for (int var19 = 0; var19 < var9.size(); var19++) {
         UUID var20 = (UUID)var9.get(var19);
         Player var21 = Bukkit.getPlayer(var20);
         if (var21 != null) {
            double var22 = ((double)var19 - (double)(var9.size() - 1) / 2.0) * var11;
            Location var24 = var2.clone().add(var15 * var22, 0.0, var17 * var22);

            for (PotionEffect var26 : var21.getActivePotionEffects()) {
               var21.removePotionEffect(var26.getType());
            }

            var21.setFallDistance(0.0F);
            var21.setFireTicks(0);
            var21.getInventory().clear();
            var21.setInvulnerable(true);
            var21.setCollidable(true);
            this.applyKitHealth(var21, var3);
            if (var4) {
               this.plugin.getScoreboardManager().setPlaceholder(var21, "team_icon", this.cachedBlueTeamIcon);
               this.plugin.getScoreboardManager().setPlaceholder(var21, "team_color", this.cachedBlueTeamColor);
            } else {
               this.plugin.getScoreboardManager().setPlaceholder(var21, "team_icon", this.cachedRedTeamIcon);
               this.plugin.getScoreboardManager().setPlaceholder(var21, "team_color", this.cachedRedTeamColor);
            }

            this.plugin.getArenaListener().addTeleportGracePeriod(var20);
            CompletableFuture var27 = new CompletableFuture();
            var10.add(var27);
            var21.teleportAsync(var24)
               .whenComplete(
                  (var6x, var7x) -> Bukkit.getScheduler()
                        .runTask(
                           this.plugin,
                           () -> {
                              Player var8x = Bukkit.getPlayer(var20);
                              if (var7x == null && Boolean.TRUE.equals(var6x) && var8x != null && var8x.isOnline()) {
                                 var8x.setFallDistance(0.0F);
                                 var8x.setGameMode(GameMode.SURVIVAL);
                                 var8x.setAllowFlight(false);
                                 var8x.setFlying(false);
                                 var8x.setInvulnerable(false);
                                 var8x.setCollidable(true);
                                 var8x.getInventory().clear();
                                 if (this.isCustomKit(var3)) {
                                    this.givePartyKit(var8x, var3);
                                 } else if (var7) {
                                    this.plugin
                                       .getKitManager()
                                       .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                                          var8x, var3, var8
                                       );
                                 } else {
                                    this.plugin
                                       .getKitManager()
                                       .Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return(
                                          var8x, var3
                                       );
                                 }
                              } else if (var8x != null && var8x.isOnline()) {
                                 this.clearArrivalMask(var8x);
                              }

                              var27.complete(null);
                           }
                        )
               );
         }
      }

      return CompletableFuture.allOf(var10.toArray(CompletableFuture[]::new));
   }

   private void startCountdown(final List<UUID> var1, final int var2) {
      final d var3 = !var1.isEmpty() ? this.playerToGame.get(var1.get(0)) : null;
      if (var3 != null) {
         var3.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
            true
         );
      }

      BukkitTask var4 = (new BukkitRunnable() {
            int countdown = var2;

            public void run() {
               if (var3 != null
                  && var3.oO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000do()
                  )
                {
                  this.cancel();
               } else if (this.countdown <= 0) {
                  if (var3 != null) {
                     var3.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
                        false
                     );
                     var3.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                        -1
                     );
                  }

                  PartyGameManager.this.finalizePartyFightStart(var1, var3);
                  String var12 = "&a&lFIGHT!";
                  String var13 = "";
                  if (!PartyGameManager.this.cachedFinalTitle.isEmpty()) {
                     Map var14 = PartyGameManager.this.cachedFinalTitle.get(0);
                     var12 = var14.get("title") != null ? var14.get("title").toString() : "&a&lFIGHT!";
                     var13 = var14.get("subtitle") != null ? var14.get("subtitle").toString() : "";
                  }

                  TextComponent var15 = LegacyComponentSerializer.legacyAmpersand().deserialize(var12);
                  TextComponent var16 = LegacyComponentSerializer.legacyAmpersand().deserialize(var13);
                  Title var18 = Title.title(var15, var16, Times.times(Duration.ofMillis(0L), Duration.ofSeconds(1L), Duration.ofMillis(500L)));

                  for (UUID var21 : var1) {
                     if (var3 == null || PartyGameManager.this.isPlayerStillInGame(var3, var21)) {
                        Player var23 = Bukkit.getPlayer(var21);
                        if (var23 != null) {
                           var23.showTitle(var18);
                           if (PartyGameManager.this.cachedStartSound != null) {
                              PartyGameManager.this.cachedStartSound
                                 .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                                    var23
                                 );
                           }
                        }
                     }
                  }

                  int var20 = PartyGameManager.this.cachedMaxFightDuration;
                  d var22 = !var1.isEmpty() ? PartyGameManager.this.playerToGame.get(var1.get(0)) : null;
                  UUID var24 = var22 != null ? PartyGameManager.this.getGameOwner(var22) : null;
                  int var25 = Bukkit.getScheduler()
                     .runTaskLater(
                        PartyGameManager.this.plugin,
                        () -> {
                           if (var24 != null) {
                              d var2xxx = PartyGameManager.this.activeGames.get(var24);
                              if (var2xxx != null) {
                                 for (UUID var4x : var2xxx.öO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Stringsuper()) {
                                    Player var5x = Bukkit.getPlayer(var4x);
                                    if (var5x != null) {
                                       PartyGameManager.this.plugin
                                          .getMessagesManager()
                                          .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                                             var5x, "party-match-time-limit"
                                          );
                                    }
                                 }

                                 PartyGameManager.this.endGame(var24);
                              }
                           }
                        },
                        (long)(var20 * 60) * 20L
                     )
                     .getTaskId();
                  if (var22 != null) {
                     var22.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
                        var25
                     );
                     if (PartyGameManager.this.plugin
                        .getTntTagManager()
                        .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                           var22.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new()
                        )) {
                        PartyGameManager.this.plugin
                           .getTntTagManager()
                           .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
                              var1
                           );
                     }

                     if (PartyGameManager.this.plugin
                        .getFlowerCrownManager()
                        .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                           var22.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new()
                        )) {
                        PartyGameManager.this.plugin
                           .getFlowerCrownManager()
                           .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
                              var1
                           );
                     }

                     if (PartyGameManager.this.plugin.getPrePotionsManager() != null) {
                        Player[] var11 = var1.stream().<Player>map(Bukkit::getPlayer).filter(var0 -> var0 != null && var0.isOnline()).toArray(Player[]::new);
                        PartyGameManager.this.plugin
                           .getPrePotionsManager()
                           .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                              var22.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(),
                              var11
                           );
                     }

                     if (PartyGameManager.this.plugin.getLavaRaiseManager() != null) {
                        PartyGameManager.this.plugin.getLavaRaiseManager().startPartyRound(var22);
                     }
                  }

                  this.cancel();
               } else {
                  String var1x = "";
                  String var2x = "";
                  k var3x = PartyGameManager.this.cachedCountdownSound;
                  int var4 = var2 - this.countdown;
                  if (var4 >= 0 && var4 < PartyGameManager.this.cachedTitles.size()) {
                     Map var5 = PartyGameManager.this.cachedTitles.get(var4);
                     var1x = var5.get("title") != null ? var5.get("title").toString() : "";
                     var2x = var5.get("subtitle") != null ? var5.get("subtitle").toString() : "";
                     var3x = PartyGameManager.this.countdownSoundForTitle(var5);
                  }

                  TextComponent var17 = LegacyComponentSerializer.legacyAmpersand().deserialize(var1x);
                  TextComponent var6 = LegacyComponentSerializer.legacyAmpersand().deserialize(var2x);
                  Title var7 = Title.title(var17, var6, Times.times(Duration.ofMillis(0L), Duration.ofSeconds(1L), Duration.ofMillis(0L)));

                  for (UUID var9 : var1) {
                     if (var3 == null || PartyGameManager.this.isPlayerStillInGame(var3, var9)) {
                        Player var10 = Bukkit.getPlayer(var9);
                        if (var10 != null) {
                           var10.showTitle(var7);
                           if (var3x != null) {
                              var3x.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                                 var10
                              );
                           }
                        }
                     }
                  }

                  this.countdown--;
               }
            }
         })
         .runTaskTimer(this.plugin, 0L, 20L);
      if (var3 != null) {
         var3.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
            var4.getTaskId()
         );
      }
   }

   private void startCountdown(final List<UUID> var1, final int var2, final d var3) {
      if (var3 != null
         && var3.Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return()
            != e.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new
         )
       {
         ArrayList var4 = new ArrayList();
         ArrayList var5 = new ArrayList();

         for (UUID var7 : var3.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int()) {
            Player var8 = Bukkit.getPlayer(var7);
            if (var8 != null && var8.isOnline()) {
               var4.add(var8);
            }
         }

         for (UUID var11 : var3.ÒÒ00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000newnew()) {
            Player var12 = Bukkit.getPlayer(var11);
            if (var12 != null && var12.isOnline()) {
               var5.add(var12);
            }
         }

         this.plugin
            .getDuelManager()
            .getMatchFoundEffects()
            .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
               var4, var5, var3
            );
      }

      if (var3 != null) {
         var3.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
            true
         );
      }

      BukkitTask var9 = (new BukkitRunnable() {
            int countdown = var2;

            public void run() {
               if (var3 != null
                  && var3.oO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000do()
                  )
                {
                  this.cancel();
               } else if (this.countdown <= 0) {
                  if (var3 != null) {
                     var3.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
                        false
                     );
                     var3.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                        -1
                     );
                  }

                  PartyGameManager.this.plugin
                     .getDuelManager()
                     .getMatchFoundEffects()
                     .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
                        var3
                     );
                  PartyGameManager.this.finalizePartyFightStart(var1, var3);
                  String var11 = "&a&lFIGHT!";
                  String var12 = "";
                  if (!PartyGameManager.this.cachedFinalTitle.isEmpty()) {
                     Map var13 = PartyGameManager.this.cachedFinalTitle.get(0);
                     var11 = var13.get("title") != null ? var13.get("title").toString() : "&a&lFIGHT!";
                     var12 = var13.get("subtitle") != null ? var13.get("subtitle").toString() : "";
                  }

                  TextComponent var14 = LegacyComponentSerializer.legacyAmpersand().deserialize(var11);
                  TextComponent var15 = LegacyComponentSerializer.legacyAmpersand().deserialize(var12);
                  Title var17 = Title.title(var14, var15, Times.times(Duration.ofMillis(0L), Duration.ofSeconds(1L), Duration.ofMillis(500L)));

                  for (UUID var20 : var1) {
                     if (var3 == null || PartyGameManager.this.isPlayerStillInGame(var3, var20)) {
                        Player var22 = Bukkit.getPlayer(var20);
                        if (var22 != null) {
                           var22.showTitle(var17);
                           if (PartyGameManager.this.cachedStartSound != null) {
                              PartyGameManager.this.cachedStartSound
                                 .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                                    var22
                                 );
                           }
                        }
                     }
                  }

                  int var19 = PartyGameManager.this.cachedMaxFightDuration;
                  UUID var21 = PartyGameManager.this.getGameOwner(var3);
                  int var23 = Bukkit.getScheduler()
                     .runTaskLater(
                        PartyGameManager.this.plugin,
                        () -> {
                           if (var21 != null) {
                              d var2xxx = PartyGameManager.this.activeGames.get(var21);
                              if (var2xxx != null) {
                                 for (UUID var4x : var2xxx.öO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Stringsuper()) {
                                    Player var5x = Bukkit.getPlayer(var4x);
                                    if (var5x != null) {
                                       PartyGameManager.this.plugin
                                          .getMessagesManager()
                                          .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                                             var5x, "party-match-time-limit"
                                          );
                                    }
                                 }

                                 PartyGameManager.this.endGame(var21);
                              }
                           }
                        },
                        (long)(var19 * 60) * 20L
                     )
                     .getTaskId();
                  if (var3 != null) {
                     var3.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
                        var23
                     );
                     if (PartyGameManager.this.plugin
                        .getTntTagManager()
                        .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                           var3.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new()
                        )) {
                        PartyGameManager.this.plugin
                           .getTntTagManager()
                           .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
                              var1
                           );
                     }

                     if (PartyGameManager.this.plugin
                        .getFlowerCrownManager()
                        .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                           var3.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new()
                        )) {
                        PartyGameManager.this.plugin
                           .getFlowerCrownManager()
                           .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
                              var1
                           );
                     }

                     if (PartyGameManager.this.plugin.getPrePotionsManager() != null) {
                        Player[] var24 = var1.stream().<Player>map(Bukkit::getPlayer).filter(var0 -> var0 != null && var0.isOnline()).toArray(Player[]::new);
                        PartyGameManager.this.plugin
                           .getPrePotionsManager()
                           .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                              var3.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(),
                              var24
                           );
                     }

                     if (PartyGameManager.this.plugin.getLavaRaiseManager() != null) {
                        PartyGameManager.this.plugin.getLavaRaiseManager().startPartyRound(var3);
                     }

                     if (PartyGameManager.this.cachedTeamGlowEnabled
                        && (
                           var3.Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return()
                                 == e.Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String
                              || var3.Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return()
                                 == e.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super
                        )
                        && !PartyGameManager.this.isHunterTroopsGame(var3)
                        && PartyGameManager.this.plugin.getTablistManager() != null) {
                        PartyGameManager.this.plugin
                           .getTablistManager()
                           .applyTeamGlow(
                              var3.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int(),
                              var3.ÒÒ00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000newnew()
                           );
                     }
                  }

                  this.cancel();
               } else {
                  String var1x = "";
                  String var2x = "";
                  k var3x = PartyGameManager.this.cachedCountdownSound;
                  int var4 = var2 - this.countdown;
                  if (var4 >= 0 && var4 < PartyGameManager.this.cachedTitles.size()) {
                     Map var5 = PartyGameManager.this.cachedTitles.get(var4);
                     var1x = var5.get("title") != null ? var5.get("title").toString() : "";
                     var2x = var5.get("subtitle") != null ? var5.get("subtitle").toString() : "";
                     var3x = PartyGameManager.this.countdownSoundForTitle(var5);
                  }

                  TextComponent var16 = LegacyComponentSerializer.legacyAmpersand().deserialize(var1x);
                  TextComponent var6 = LegacyComponentSerializer.legacyAmpersand().deserialize(var2x);
                  Title var7 = Title.title(var16, var6, Times.times(Duration.ofMillis(0L), Duration.ofSeconds(1L), Duration.ofMillis(0L)));

                  for (UUID var9 : var1) {
                     if (var3 == null || PartyGameManager.this.isPlayerStillInGame(var3, var9)) {
                        Player var10 = Bukkit.getPlayer(var9);
                        if (var10 != null) {
                           var10.showTitle(var7);
                           if (var3x != null) {
                              var3x.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                                 var10
                              );
                           }
                        }
                     }
                  }

                  this.countdown--;
               }
            }
         })
         .runTaskTimer(this.plugin, 0L, 20L);
      if (var3 != null) {
         var3.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
            var9.getTaskId()
         );
      }
   }

   private k countdownSoundForTitle(Map<?, ?> var1) {
      if (this.cachedCountdownSound == null) {
         return null;
      } else {
         float var2 = this.cachedCountdownSound
            .Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object();
         if (var1 != null) {
            Object var3 = var1.get("pitch");
            if (var3 instanceof Number var4) {
               var2 = k.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                  var4.floatValue()
               );
            } else if (var3 != null) {
               try {
                  var2 = k.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                     Float.parseFloat(var3.toString())
                  );
               } catch (NumberFormatException var6) {
               }
            }
         }

         return new k(
            this.cachedCountdownSound
               .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(),
            this.cachedCountdownSound
               .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(),
            var2
         );
      }
   }

   private boolean isPlayerStillInGame(d var1, UUID var2) {
      return var1 != null
         && var2 != null
         && this.playerToGame.get(var2) == var1
         && var1.öO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Stringsuper()
            .contains(var2)
         && !var1.Oo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000returnsuper()
            .contains(var2);
   }

   private boolean isAttachedPartyGameParticipant(d var1, UUID var2) {
      return var1 != null
         && var2 != null
         && this.playerToGame.get(var2) == var1
         && var1.öO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Stringsuper()
            .contains(var2);
   }

   public d getActiveGame(UUID var1) {
      return this.activeGames.get(var1);
   }

   public List<d> getActiveGamesSnapshot() {
      Set var1 = Collections.newSetFromMap(new IdentityHashMap());
      var1.addAll(this.activeGames.values());
      return new ArrayList<>(var1);
   }

   public d getPlayerActiveGame(UUID var1) {
      return this.playerToGame.get(var1);
   }

   public boolean isPlayerInPartyGame(UUID var1) {
      return this.playerToGame.containsKey(var1);
   }

   public String getPlayerTeamName(UUID var1) {
      if (var1 == null) {
         return "none";
      } else {
         d var2 = this.getPlayerActiveGame(var1);
         if (var2 == null
            || var2.Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return()
               == e.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new
            )
          {
            return "none";
         } else if (var2.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int()
               != null
            && var2.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int()
               .contains(var1)) {
            return "team1";
         } else {
            return var2.ÒÒ00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000newnew()
                     != null
                  && var2.ÒÒ00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000newnew()
                     .contains(var1)
               ? "team2"
               : "none";
         }
      }
   }

   public boolean isPlayerInTeam(UUID var1, String var2) {
      String var3 = this.normalizeTeamName(var2);
      return var3 == null ? false : this.getPlayerTeamName(var1).equals(var3);
   }

   public boolean arePlayersInSameTeam(UUID var1, UUID var2) {
      if (var1 != null && var2 != null) {
         d var3 = this.getPlayerActiveGame(var1);
         if (var3 != null
            && var3 == this.getPlayerActiveGame(var2)
            && var3.Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return()
               != e.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new
            )
          {
            String var4 = this.getPlayerTeamName(var1);
            return !"none".equals(var4) && var4.equals(this.getPlayerTeamName(var2));
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private String normalizeTeamName(String var1) {
      if (var1 != null && !var1.isBlank()) {
         String var2 = var1.toLowerCase(Locale.ROOT).replace("-", "").replace("_", "");

         return switch (var2) {
            case "1", "team1", "blue", "blueteam" -> "team1";
            case "2", "team2", "red", "redteam" -> "team2";
            default -> null;
         };
      } else {
         return null;
      }
   }

   public boolean handleBridgeGoal(Player var1, Location var2) {
      if (var1 != null && var2 != null) {
         d var3 = this.getPlayerActiveGame(var1.getUniqueId());
         if (var3 != null
            && !var3.oO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000do()
            && !var3.Óo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000intsuper()
            && !var3.Oo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000returnsuper()
               .contains(var1.getUniqueId())) {
            String var4 = var3.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new();
            if (var4 != null && this.getKitRule(var4, KitRule.BRIDGE)) {
               org.lime.swiftCore.arena.d var5 = var3.øO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000classsuper();
               if (var5 != null
                  && var5.öo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000privatesuper()
                     != null
                  && var5.ôO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000newsuper()
                     != null) {
                  boolean var6 = var3.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int()
                        != null
                     && var3.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int()
                        .contains(var1.getUniqueId());
                  boolean var7 = var3.ÒÒ00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000newnew()
                        != null
                     && var3.ÒÒ00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000newnew()
                        .contains(var1.getUniqueId());
                  if (!var6 && !var7) {
                     return false;
                  } else {
                     double var8 = var2.distanceSquared(
                        var5.öo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000privatesuper()
                     );
                     double var10 = var2.distanceSquared(
                        var5.ôO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000newsuper()
                     );
                     boolean var12 = var8 <= var10;
                     boolean var13 = var6 && var12 || var7 && !var12;
                     if (var13) {
                        return false;
                     } else {
                        UUID var14 = this.getGameOwner(var3);
                        if (var14 == null) {
                           return false;
                        } else {
                           this.handleTeamEliminated(var14, var3, var6 ? "team1" : "team2", var6 ? "Red Team" : "Blue Team");
                           return true;
                        }
                     }
                  }
               } else {
                  return false;
               }
            } else {
               return false;
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private void startHpIndicator(d var1, Collection<UUID> var2) {
      if (var1 != null && var2 != null && !var2.isEmpty() && this.plugin.getHPIndicatorManager() != null) {
         boolean var3 = this.plugin
            .getDataManager()
            .Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class(
               var1.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new()
            )
            .getOrDefault(KitRule.HP_INDICATOR, KitRule.HP_INDICATOR.getDefaultValue());
         if (var3) {
            org.lime.swiftCore.arena.d var4 = var1.øO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000classsuper();
            this.plugin
               .getHPIndicatorManager()
               .startForGroup(
                  var2,
                  var4 != null
                     ? var4.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int()
                     : null
               );
         }
      }
   }

   public void forceRemovePlayer(UUID var1) {
      d var2 = this.playerToGame.remove(var1);
      if (var2 != null) {
         if ((
               var2.Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return()
                     == e.Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String
                  || var2.Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return()
                     == e.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super
            )
            && this.plugin.getTablistManager() != null) {
            this.plugin
               .getTablistManager()
               .removePlayerGlow(
                  var1,
                  var2.öO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Stringsuper()
               );
         }

         this.plugin.getKitRulesListener().removeActivePlayer(var1);
         this.plugin
            .getTntTagManager()
            .Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
               var1
            );
         this.plugin
            .getArenaManager()
            .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
               var1
            );
         if (this.plugin.getHPIndicatorManager() != null) {
            this.plugin.getHPIndicatorManager().removeFromGroup(var1);
         }

         if (this.plugin
            .getSpectatorManager()
            .Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String(
               var1
            )) {
            this.plugin
               .getSpectatorManager()
               .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                  var1
               );
         }
      }
   }

   public boolean notifyNewJoinerIfInMatch(UUID var1, UUID var2) {
      d var3 = this.activeGames.get(var2);
      if (var3 == null) {
         return false;
      } else {
         Player var4 = Bukkit.getPlayer(var1);
         if (var4 != null && var4.isOnline()) {
            this.plugin
               .getMessagesManager()
               .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                  var4, "party-join-in-match"
               );
            return false;
         } else {
            return false;
         }
      }
   }

   public void surrenderPartyGame(Player var1) {
      UUID var2 = var1.getUniqueId();
      d var3 = this.getPlayerActiveGame(var2);
      if (var3 != null) {
         this.plugin.getArenaListener().cancelThrownPearl(var2);
         this.plugin
            .getDuelManager()
            .getMatchFoundEffects()
            .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
               var1
            );
         if ((
               var3.Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return()
                     == e.Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String
                  || var3.Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return()
                     == e.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super
            )
            && this.plugin.getTablistManager() != null) {
            this.plugin.getTablistManager().removePlayerGlow(var2);
         }

         var3.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int(
            var2
         );
         var3.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int()
            .remove(var2);
         var3.ÒÒ00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000newnew()
            .remove(var2);
         this.removePlayerFromFuturePartyRounds(var3, var2);
         this.playerToGame.remove(var2);
         this.plugin.getKitRulesListener().removeActivePlayer(var2);
         this.plugin
            .getTntTagManager()
            .Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
               var2
            );
         this.plugin
            .getArenaManager()
            .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
               var2
            );
         if (this.plugin.getHPIndicatorManager() != null) {
            this.plugin.getHPIndicatorManager().removeFromGroup(var2);
         }

         if (this.plugin
            .getSpectatorManager()
            .Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String(
               var2
            )) {
            this.plugin
               .getSpectatorManager()
               .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                  var2
               );
         }

         this.plugin
            .getSpectatorManager()
            .oO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000do(
               var1
            );
         var1.getInventory().clear();
         this.plugin
            .getKitManager()
            .Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
               var1
            );
         var1.setHealth(var1.getAttribute(Attribute.MAX_HEALTH).getValue());
         var1.setFoodLevel(20);
         var1.setSaturation(5.0F);
         var1.setFireTicks(0);
         var1.setGameMode(GameMode.SURVIVAL);
         var1.setInvulnerable(false);
         boolean var4 = this.partyManager.isInParty(var2);
         boolean var5 = var4 && this.partyManager.isPartyOwner(var2);
         boolean var6 = var4 && !var5;
         if (var5) {
            UUID var7 = this.getGameOwner(var3);
            this.partyManager.leaveParty(var2);
            if (var7 != null && var7.equals(var2)) {
               UUID var8 = var3.öO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Stringsuper()
                  .stream()
                  .filter(
                     var1x -> !var3.Oo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000returnsuper()
                           .contains(var1x)
                  )
                  .findFirst()
                  .orElse(
                     var3.öO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Stringsuper()
                           .isEmpty()
                        ? null
                        : var3.öO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Stringsuper()
                           .get(0)
                  );
               if (var8 != null) {
                  this.partyManager.transferOwnershipToMember(var8);
                  this.activeGames.remove(var7);
                  this.activeGames.put(var8, var3);
                  this.gameToOwner.remove(var3);
                  this.gameToOwner.put(var3, var8);
               }
            }
         }

         if (var6) {
            this.plugin.getScoreboardManager().setState(var1, ScoreboardState.PARTY);
            this.plugin.getScoreboardManager().setPlaceholder(var1, "in_party", "true");
         } else {
            this.plugin.getScoreboardManager().setState(var1, ScoreboardState.DEFAULT);
            this.plugin.getScoreboardManager().setPlaceholder(var1, "in_party", "false");
         }

         this.plugin.getScoreboardManager().setPlaceholder(var1, "in_match", "false");
         this.plugin.getScoreboardManager().setPlaceholder(var1, "in_fight", "false");
         this.plugin.getScoreboardManager().setPlaceholder(var1, "kit", "");
         this.plugin.getScoreboardManager().setPlaceholder(var1, "fight_kitname", "");
         this.plugin.getScoreboardManager().setPlaceholder(var1, "team_icon", "");
         this.plugin.getScoreboardManager().setPlaceholder(var1, "team_color", "");
         this.plugin.getScoreboardManager().setPlaceholder(var1, "party_ffa_alive", "");
         this.plugin.getScoreboardManager().setPlaceholder(var1, "blue_alive", "");
         this.plugin.getScoreboardManager().setPlaceholder(var1, "red_alive", "");
         if (this.plugin.getTablistManager() != null) {
            this.plugin.getTablistManager().resetContext(var1);
         }

         if (this.isCrossServerArena()) {
            HashMap var13 = new HashMap();
            var13.put("party_state", "surrendered");
            var13.put("post_match", "true");
            var13.put("post_match_created_at", String.valueOf(System.currentTimeMillis()));
            this.plugin
               .getLimboManager()
               .getPlayerDataSync()
               .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                  var2, var13
               );
         }

         this.sendPlayerToLobby(var1);
         if (var6) {
            this.plugin.getSpawnItemsManager().giveSpawnItems(var1, "party", true, false);
         } else {
            this.plugin.getSpawnItemsManager().giveSpawnItems(var1, "default", false, false);
         }

         this.plugin
            .getMessagesManager()
            .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
               var1, "surrender-success"
            );
         HashMap var14 = new HashMap();
         var14.put("player", var1.getName());

         for (UUID var9 : var3.öO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Stringsuper()) {
            Player var10 = Bukkit.getPlayer(var9);
            if (var10 != null && var10.isOnline()) {
               this.plugin
                  .getMessagesManager()
                  .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                     var10, "party-player-surrendered", var14
                  );
            }
         }

         if (var3.Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return()
               != e.Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String
            && var3.Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return()
               != e.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super
            )
          {
            List var18 = this.getActiveFfaPlayers(var3);
            int var20 = var18.size();

            for (UUID var24 : var3.öO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Stringsuper()) {
               Player var12 = Bukkit.getPlayer(var24);
               if (var12 != null && var12.isOnline()) {
                  this.plugin.getScoreboardManager().setPlaceholder(var12, "party_ffa_alive", String.valueOf(var20));
               }
            }

            if (var20 <= 1) {
               UUID var23 = this.getGameOwner(var3);
               if (var23 != null) {
                  Bukkit.getScheduler().runTaskLater(this.plugin, () -> this.endGame(var23), 40L);
               }
            }
         } else {
            for (UUID var19 : var3.öO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Stringsuper()) {
               Player var21 = Bukkit.getPlayer(var19);
               if (var21 != null && var21.isOnline()) {
                  if (var3.Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return()
                     == e.Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String
                     )
                   {
                     this.plugin
                        .getScoreboardManager()
                        .setPlaceholder(
                           var21,
                           "blue_alive",
                           String.valueOf(
                              var3.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int()
                                 .size()
                           )
                        );
                     this.plugin
                        .getScoreboardManager()
                        .setPlaceholder(
                           var21,
                           "red_alive",
                           String.valueOf(
                              var3.ÒÒ00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000newnew()
                                 .size()
                           )
                        );
                  } else {
                     boolean var11 = var3.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int()
                        .contains(var19);
                     if (var11) {
                        this.plugin
                           .getScoreboardManager()
                           .setPlaceholder(
                              var21,
                              "your_party_alive",
                              String.valueOf(
                                 var3.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int()
                                    .size()
                              )
                           );
                        this.plugin
                           .getScoreboardManager()
                           .setPlaceholder(
                              var21,
                              "enemy_party_alive",
                              String.valueOf(
                                 var3.ÒÒ00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000newnew()
                                    .size()
                              )
                           );
                     } else {
                        this.plugin
                           .getScoreboardManager()
                           .setPlaceholder(
                              var21,
                              "your_party_alive",
                              String.valueOf(
                                 var3.ÒÒ00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000newnew()
                                    .size()
                              )
                           );
                        this.plugin
                           .getScoreboardManager()
                           .setPlaceholder(
                              var21,
                              "enemy_party_alive",
                              String.valueOf(
                                 var3.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int()
                                    .size()
                              )
                           );
                     }
                  }
               }
            }

            UUID var17 = this.getGameOwner(var3);
            if (var3.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int()
                  .isEmpty()
               && var17 != null) {
               this.handleTeamEliminated(var17, var3, "team2", "Blue Team");
            } else if (var3.ÒÒ00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000newnew()
                  .isEmpty()
               && var17 != null) {
               this.handleTeamEliminated(var17, var3, "team1", "Red Team");
            }
         }
      }
   }

   private void startPartyRoundSpectating(Player var1, Player var2, d var3) {
      if (var1 != null && var2 != null && var3 != null) {
         var3.Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class(
            var1.getUniqueId()
         );
         var1.getInventory().clear();
         String var4 = this.cachedSpectatorGamemode;
         GameMode var5 = GameMode.SURVIVAL;

         try {
            var5 = GameMode.valueOf(var4);
         } catch (IllegalArgumentException var12) {
         }

         var1.setGameMode(var5);
         var1.setAllowFlight(true);
         var1.setFlying(true);
         var1.setInvulnerable(true);
         this.plugin
            .getSpectatorManager()
            .Ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000void(
               var1
            );
         this.plugin.getSpawnItemsManager().giveSpawnItemsSync(var1, "spectating", false, false);
         var1.setFallDistance(0.0F);
         if (var1.getFireTicks() > 0) {
            var1.setFireTicks(0);
         }

         try {
            var1.setVelocity(new Vector(0, 0, 0));
         } catch (Throwable var11) {
         }

         Location var6 = var2.getLocation();
         org.lime.swiftCore.arena.d var7 = var3.øO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000classsuper();
         if (var7 != null) {
            Location var8 = var7.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super();
            double var9 = var7.ÓO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000public()
               ? (double)var7.Ôo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000ifsuper()
                  .intValue()
               : Double.NEGATIVE_INFINITY;
            if ((var6 == null || var6.getWorld() == null || var6.getY() - 2.0 <= var9) && var8 != null) {
               var6 = var8;
            }
         }

         if (var6 != null) {
            var1.teleportAsync(var6);
         }

         for (UUID var15 : var3.öO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Stringsuper()) {
            if (!var3.õo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000publicsuper()
               .contains(var15)) {
               Player var10 = Bukkit.getPlayer(var15);
               if (var10 != null && var10.isOnline()) {
                  var10.hidePlayer(this.plugin, var1);
               }
            }
         }

         for (Player var16 : Bukkit.getOnlinePlayers()) {
            if (var3.öO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Stringsuper()
               .contains(var16.getUniqueId())) {
               var1.showPlayer(this.plugin, var16);
            }
         }
      }
   }

   private void cleanupPartyRoundSpectators(d var1) {
      for (UUID var3 : var1.õo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000publicsuper()) {
         Player var4 = Bukkit.getPlayer(var3);
         if (var4 != null && var4.isOnline()) {
            this.plugin
               .getSpectatorManager()
               .oO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000do(
                  var4
               );
            this.plugin.getSpawnItemsManager().clearTracking(var4);
            var4.setInvulnerable(true);
            var4.setFlying(false);
            var4.setAllowFlight(false);
            var4.setFallDistance(0.0F);
            var4.setFireTicks(0);

            for (UUID var6 : var1.öO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Stringsuper()) {
               Player var7 = Bukkit.getPlayer(var6);
               if (var7 != null && var7.isOnline()) {
                  var7.showPlayer(this.plugin, var4);
                  var4.showPlayer(this.plugin, var7);
               }
            }
         } else {
            this.plugin
               .getSpectatorManager()
               .Ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000void(
                  var3
               );
         }
      }

      var1.Ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000void();
   }

   private void removePlayerFromFuturePartyRounds(d var1, UUID var2) {
      var1.öO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Stringsuper()
         .remove(var2);
      var1.ÕO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000interface()
         .remove(var2);
      var1.õo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000publicsuper()
         .remove(var2);
      if (var1.Òo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000nullsuper()
         != null) {
         var1.Òo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000nullsuper()
            .remove(var2);
      }

      if (var1.Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class()
         != null) {
         var1.Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class()
            .remove(var2);
      }

      if (var1.ØO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000supersuper()
         != null) {
         var1.ØO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000supersuper()
            .remove(var2);
      }

      if (this.plugin.getTablistManager() != null) {
         this.plugin
            .getTablistManager()
            .removePlayerGlow(
               var2,
               var1.öO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Stringsuper()
            );
      }
   }

   private void startNextRound(UUID var1, d var2, String var3) {
      if (this.plugin.getLavaRaiseManager() != null) {
         this.plugin
            .getLavaRaiseManager()
            .stopRound(
               var2.øO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000classsuper()
            );
      }

      if (var2.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float()
         != -1) {
         Bukkit.getScheduler()
            .cancelTask(
               var2.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float()
            );
         var2.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
            -1
         );
      }

      if (this.plugin.getTablistManager() != null) {
         this.plugin
            .getTablistManager()
            .removeAllTeamGlow(
               var2.öO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Stringsuper()
            );
      }

      this.cleanupPartyRoundSpectators(var2);
      var2.öo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000privatesuper();
      this.plugin.getKitRulesListener().clearDecayingBlocks();
      org.lime.swiftCore.arena.d var4 = var2.øO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000classsuper();
      String var5 = var2.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new();
      ArrayList var6 = new ArrayList<>(
         var2.Òo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000nullsuper()
      );
      ArrayList var7 = new ArrayList<>(
         var2.Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class()
      );
      var6.removeIf(
         var1x -> Bukkit.getPlayer(var1x) == null
               || !Bukkit.getPlayer(var1x).isOnline()
               || var2.Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String(
                  var1x
               )
      );
      var7.removeIf(
         var1x -> Bukkit.getPlayer(var1x) == null
               || !Bukkit.getPlayer(var1x).isOnline()
               || var2.Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String(
                  var1x
               )
      );
      if (!var6.isEmpty() && !var7.isEmpty()) {
         var2.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
            var6
         );
         var2.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
            var7
         );
         var2.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
            true
         );
         var2.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
            false
         );

         for (UUID var9 : var2.ÕO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000interface()) {
            var2.öO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Stringsuper()
               .remove(var9);
            if (this.plugin.getTablistManager() != null) {
               this.plugin
                  .getTablistManager()
                  .removePlayerGlow(
                     var9,
                     var2.öO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Stringsuper()
                  );
            }

            this.plugin
               .getArenaManager()
               .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
                  var9
               );
            Player var10 = Bukkit.getPlayer(var9);
            if (var10 != null && var10.isOnline()) {
               if (this.plugin
                  .getSpectatorManager()
                  .Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String(
                     var9
                  )) {
                  this.plugin
                     .getSpectatorManager()
                     .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                        var9
                     );
               }

               var10.getInventory().clear();
               this.plugin
                  .getKitManager()
                  .Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
                     var10
                  );
               this.resetPlayerHealth(var10);
               var10.setGameMode(GameMode.SURVIVAL);
               this.sendPlayerToLobby(var10);
               this.plugin.getSpawnItemsManager().giveSpawnItems(var10, "default", false, false);
               this.plugin.getScoreboardManager().setState(var10, ScoreboardState.DEFAULT);
               this.plugin.getScoreboardManager().setPlaceholder(var10, "in_party", "false");
               this.plugin.getScoreboardManager().setPlaceholder(var10, "in_fight", "false");
            }
         }

         var2.ÕO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000interface()
            .clear();

         for (UUID var14 : var2.öO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Stringsuper()) {
            Player var15 = Bukkit.getPlayer(var14);
            if (var15 != null && var15.isOnline()) {
               if (this.plugin
                  .getSpectatorManager()
                  .Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String(
                     var14
                  )) {
                  this.plugin
                     .getSpectatorManager()
                     .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                        var14
                     );
               }

               this.plugin.getSpawnItemsManager().clearTracking(var15);
               var15.getInventory().clear();
               this.plugin
                  .getKitManager()
                  .Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
                     var15
                  );
               this.plugin.getKitRulesListener().clearBoxingHits(var14);
               this.plugin.getKitRulesListener().clearCooldowns(var14);
               var15.setInvulnerable(true);
               var15.setFallDistance(0.0F);
               var15.setFireTicks(0);

               for (PotionEffect var12 : var15.getActivePotionEffects()) {
                  var15.removePotionEffect(var12.getType());
               }

               this.applyKitHealth(var15, var5);
            }
         }

         this.plugin
            .getArenaResetManager()
            .Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
               var4,
               () -> {
                  this.plugin
                     .getSkyWarsLootManager()
                     .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                        var4, var5
                     );
                  if (this.getKitRule(var5, KitRule.BEDWARS)) {
                     var4.õO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Objectsuper();
                  }

                  Bukkit.getScheduler()
                     .runTask(
                        this.plugin,
                        () -> {
                           CompletableFuture var6x = CompletableFuture.allOf(
                              this.teleportTeamToPosition(
                                 var6,
                                 var4.öo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000privatesuper(),
                                 var5,
                                 true
                              ),
                              this.teleportTeamToPosition(
                                 var7,
                                 var4.ôO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000newsuper(),
                                 var5,
                                 false
                              )
                           );

                           for (UUID var8 : var2.öO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Stringsuper()) {
                              Player var9x = Bukkit.getPlayer(var8);
                              if (var9x != null && var9x.isOnline()) {
                                 this.plugin
                                    .getScoreboardManager()
                                    .setPlaceholder(
                                       var9x,
                                       "round",
                                       String.valueOf(
                                          var2.ÔO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000private()
                                       )
                                    );
                                 this.plugin
                                    .getScoreboardManager()
                                    .setPlaceholder(
                                       var9x,
                                       "blue_wins",
                                       String.valueOf(
                                          var2.ÖO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000thissuper()
                                       )
                                    );
                                 this.plugin
                                    .getScoreboardManager()
                                    .setPlaceholder(
                                       var9x,
                                       "red_wins",
                                       String.valueOf(
                                          var2.õO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Objectsuper()
                                       )
                                    );
                                 if (var2.Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return()
                                    == e.Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String
                                    )
                                  {
                                    this.plugin.getScoreboardManager().setPlaceholder(var9x, "blue_alive", String.valueOf(var6.size()));
                                    this.plugin.getScoreboardManager().setPlaceholder(var9x, "red_alive", String.valueOf(var7.size()));
                                 } else if (var2.Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return()
                                    == e.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super
                                    )
                                  {
                                    boolean var10x = var6.contains(var8);
                                    if (var10x) {
                                       this.plugin.getScoreboardManager().setPlaceholder(var9x, "your_party_alive", String.valueOf(var6.size()));
                                       this.plugin.getScoreboardManager().setPlaceholder(var9x, "enemy_party_alive", String.valueOf(var7.size()));
                                    } else {
                                       this.plugin.getScoreboardManager().setPlaceholder(var9x, "your_party_alive", String.valueOf(var7.size()));
                                       this.plugin.getScoreboardManager().setPlaceholder(var9x, "enemy_party_alive", String.valueOf(var6.size()));
                                    }
                                 }
                              }
                           }

                           ArrayList var12x = new ArrayList();
                           var12x.addAll(var6);
                           var12x.addAll(var7);
                           ScoreboardState var13 = this.plugin
                              .getKitStateResolver()
                              .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                                 var5,
                                 var2.Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return()
                                       == e.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super
                                    ? ScoreboardState.PARTY_VS
                                    : ScoreboardState.PARTY_SPLIT
                              );

                           for (UUID var16 : var12x) {
                              Player var11 = Bukkit.getPlayer(var16);
                              if (var11 != null && var11.isOnline()) {
                                 this.plugin.getScoreboardManager().setState(var11, var13);
                                 this.plugin.getScoreboardManager().setPlaceholder(var11, "in_fight", "true");
                                 this.plugin.getScoreboardManager().setPlaceholder(var11, "in_party", "true");
                                 this.plugin.getScoreboardManager().setPlaceholder(var11, "kit", this.getPartyKitDisplayName(var5));
                              }
                           }

                           if (this.plugin.getTablistManager() != null) {
                              TablistContext var15x = this.plugin
                                 .getKitStateResolver()
                                 .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                                    var5,
                                    var2.Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return()
                                          == e.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super
                                       ? TablistContext.PARTY_VS
                                       : TablistContext.PARTY_SPLIT
                                 );
                              if (var2.Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return()
                                 == e.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super
                                 )
                               {
                                 this.plugin.getTablistManager().setupPartyVsContext(var6, var7, var2, var15x);
                              } else {
                                 this.plugin.getTablistManager().setupPartySplitContext(var6, var7, var2, var15x);
                              }
                           }

                           this.startHpIndicator(var2, var12x);
                           var6x.thenRun(
                              () -> Bukkit.getScheduler()
                                    .runTask(
                                       this.plugin,
                                       () -> {
                                          if (!var2.oO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000do()
                                             )
                                           {
                                             this.startCountdown(var12x, this.cachedCountdownTime, var2);
                                          }
                                       }
                                    )
                           );
                        }
                     );
               }
            );
      } else {
         this.endGame(var1);
      }
   }

   private void startNextRoundFFA(UUID var1, d var2) {
      if (this.plugin.getLavaRaiseManager() != null) {
         this.plugin
            .getLavaRaiseManager()
            .stopRound(
               var2.øO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000classsuper()
            );
      }

      if (var2.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float()
         != -1) {
         Bukkit.getScheduler()
            .cancelTask(
               var2.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float()
            );
         var2.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
            -1
         );
      }

      this.cleanupPartyRoundSpectators(var2);
      var2.öo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000privatesuper();
      this.plugin.getKitRulesListener().clearDecayingBlocks();
      org.lime.swiftCore.arena.d var3 = var2.øO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000classsuper();
      String var4 = var2.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new();
      List var5 = var2.ØO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000supersuper();
      if (var5 != null && !var5.isEmpty()) {
         ArrayList var6 = new ArrayList(var5);
         var6.removeIf(
            var1x -> Bukkit.getPlayer(var1x) == null
                  || !Bukkit.getPlayer(var1x).isOnline()
                  || var2.Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String(
                     var1x
                  )
         );
         if (var6.size() < 2) {
            this.endGame(var1);
         } else {
            var2.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
               true
            );
            var2.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
               false
            );

            for (UUID var8 : var2.ÕO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000interface()) {
               var2.öO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Stringsuper()
                  .remove(var8);
               if (this.plugin.getTablistManager() != null) {
                  this.plugin
                     .getTablistManager()
                     .removePlayerGlow(
                        var8,
                        var2.öO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Stringsuper()
                     );
               }

               this.plugin
                  .getArenaManager()
                  .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
                     var8
                  );
               Player var9 = Bukkit.getPlayer(var8);
               if (var9 != null && var9.isOnline()) {
                  if (this.plugin
                     .getSpectatorManager()
                     .Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String(
                        var8
                     )) {
                     this.plugin
                        .getSpectatorManager()
                        .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                           var8
                        );
                  }

                  var9.getInventory().clear();
                  this.plugin
                     .getKitManager()
                     .Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
                        var9
                     );
                  this.resetPlayerHealth(var9);
                  var9.setGameMode(GameMode.SURVIVAL);
                  this.sendPlayerToLobby(var9);
                  this.plugin.getSpawnItemsManager().giveSpawnItems(var9, "default", false, false);
                  this.plugin.getScoreboardManager().setState(var9, ScoreboardState.DEFAULT);
                  this.plugin.getScoreboardManager().setPlaceholder(var9, "in_party", "false");
                  this.plugin.getScoreboardManager().setPlaceholder(var9, "in_fight", "false");
               }
            }

            var2.ÕO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000interface()
               .clear();

            for (UUID var13 : var2.öO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Stringsuper()) {
               Player var14 = Bukkit.getPlayer(var13);
               if (var14 != null && var14.isOnline()) {
                  if (this.plugin
                     .getSpectatorManager()
                     .Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String(
                        var13
                     )) {
                     this.plugin
                        .getSpectatorManager()
                        .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                           var13
                        );
                  }

                  this.plugin.getSpawnItemsManager().clearTracking(var14);
                  var14.getInventory().clear();
                  this.plugin
                     .getKitManager()
                     .Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
                        var14
                     );
                  this.plugin.getKitRulesListener().clearBoxingHits(var13);
                  this.plugin.getKitRulesListener().clearCooldowns(var13);
                  var14.setInvulnerable(true);
                  var14.setFallDistance(0.0F);
                  var14.setFireTicks(0);

                  for (PotionEffect var11 : var14.getActivePotionEffects()) {
                     var14.removePotionEffect(var11.getType());
                  }

                  this.applyKitHealth(var14, var4);
               }
            }

            this.plugin
               .getArenaResetManager()
               .Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
                  var3,
                  () -> {
                     this.plugin
                        .getSkyWarsLootManager()
                        .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                           var3, var4
                        );
                     Bukkit.getScheduler()
                        .runTask(
                           this.plugin,
                           () -> {
                              CompletableFuture var5x = this.spawnPlayersInCircle(var6, var3, var4);

                              for (UUID var7 : var2.öO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Stringsuper()) {
                                 Player var8x = Bukkit.getPlayer(var7);
                                 if (var8x != null && var8x.isOnline()) {
                                    this.plugin
                                       .getScoreboardManager()
                                       .setPlaceholder(
                                          var8x,
                                          "round",
                                          String.valueOf(
                                             var2.ÔO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000private()
                                          )
                                       );
                                    this.plugin.getScoreboardManager().setPlaceholder(var8x, "party_ffa_alive", String.valueOf(var6.size()));

                                    for (UUID var10 : var2.öO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Stringsuper()) {
                                       Player var11x = Bukkit.getPlayer(var10);
                                       if (var11x != null) {
                                          this.plugin
                                             .getScoreboardManager()
                                             .setPlaceholder(
                                                var11x,
                                                "ffa_wins",
                                                String.valueOf(
                                                   var2.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
                                                      var10
                                                   )
                                                )
                                             );
                                       }
                                    }
                                 }
                              }

                              ScoreboardState var12 = this.plugin
                                 .getKitStateResolver()
                                 .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                                    var4, ScoreboardState.PARTY_FFA
                                 );

                              for (UUID var14x : var6) {
                                 Player var15 = Bukkit.getPlayer(var14x);
                                 if (var15 != null && var15.isOnline()) {
                                    this.plugin.getScoreboardManager().setState(var15, var12);
                                    this.plugin.getScoreboardManager().setPlaceholder(var15, "in_fight", "true");
                                    this.plugin.getScoreboardManager().setPlaceholder(var15, "in_party", "true");
                                    this.plugin.getScoreboardManager().setPlaceholder(var15, "kit", this.getPartyKitDisplayName(var4));
                                 }
                              }

                              this.startHpIndicator(var2, var6);
                              var5x.thenRun(
                                 () -> Bukkit.getScheduler()
                                       .runTask(
                                          this.plugin,
                                          () -> {
                                             if (!var2.oO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000do()
                                                )
                                              {
                                                this.startCountdown(var6, this.cachedCountdownTime);
                                             }
                                          }
                                       )
                              );
                           }
                        );
                  }
               );
         }
      } else {
         this.endGame(var1);
      }
   }

   public void endGame(UUID var1) {
      d var2 = this.activeGames.remove(var1);
      if (var2 != null) {
         this.recordKitMatchStat(var2);
         if (this.plugin.getPillarsOfFortuneManager() != null) {
            this.plugin
               .getPillarsOfFortuneManager()
               .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                  var2
               );
         }

         if (this.plugin.getLavaRaiseManager() != null) {
            this.plugin.getLavaRaiseManager().cleanupParty(var2);
         }

         this.unregisterCrossServerPartyMatch(var2);

         for (UUID var4 : var2.öO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Stringsuper()) {
            this.plugin.getArenaListener().blockEnderPearlTeleportAfterMatch(var4);
         }

         this.gameToOwner.remove(var2);
         if (this.plugin.getHPIndicatorManager() != null) {
            this.plugin
               .getHPIndicatorManager()
               .stopForGroup(
                  var2.öO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Stringsuper()
               );
         }

         for (UUID var17 : var2.öO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Stringsuper()) {
            this.playerToGame.remove(var17);
            this.plugin.getKitRulesListener().removeActivePlayer(var17);
         }

         if (this.isCustomKit(
               var2.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new()
            )
            && this.plugin.getCustomKitAPI() != null) {
            UUID var13 = this.getCustomKitOwner(
               var2.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new()
            );
            if (var13 != null) {
               this.plugin.getCustomKitAPI().removeKit(var13);
            }
         }

         if (var2.Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return()
            == e.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super
            )
          {
            this.activeGames.entrySet().removeIf(var1x -> var1x.getValue() == var2);
         }

         this.plugin
            .getDuelManager()
            .getMatchFoundEffects()
            .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
               var2
            );
         if (var2.ÓO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000public()
            != -1) {
            Bukkit.getScheduler()
               .cancelTask(
                  var2.ÓO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000public()
               );
            var2.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
               -1
            );
            var2.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
               false
            );
         }

         if (var2.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float()
            != -1) {
            Bukkit.getScheduler()
               .cancelTask(
                  var2.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float()
               );
         }

         this.cleanupPartyRoundSpectators(var2);

         for (UUID var18 : var2.öO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Stringsuper()) {
            if (this.plugin.getArenaListener().isTempRespawnSpectator(var18)) {
               this.plugin.getArenaListener().removeTempRespawnSpectator(var18);
               this.plugin
                  .getSpectatorManager()
                  .Ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000void(
                     var18
                  );
               Player var5 = Bukkit.getPlayer(var18);
               if (var5 != null && var5.isOnline()) {
                  this.plugin.getArenaListener().showTempSpectatorToAll(var5);
                  if (var5.getGameMode() == GameMode.ADVENTURE) {
                     var5.setGameMode(GameMode.SURVIVAL);
                     var5.setAllowFlight(false);
                     var5.setFlying(false);
                     var5.setInvulnerable(false);
                  }
               }
            } else if (this.plugin
               .getSpectatorManager()
               .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
                  var18
               )) {
               this.plugin
                  .getSpectatorManager()
                  .Ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000void(
                     var18
                  );
            }
         }

         if ((
               var2.Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return()
                     == e.Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String
                  || var2.Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return()
                     == e.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super
            )
            && this.plugin.getTablistManager() != null) {
            this.plugin
               .getTablistManager()
               .removeAllTeamGlow(
                  var2.öO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Stringsuper()
               );
         }

         this.plugin
            .getTntTagManager()
            .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
               var2.öO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Stringsuper()
            );
         this.plugin
            .getFlowerCrownManager()
            .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
               var2.öO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Stringsuper()
            );
         this.plugin.getKitRulesListener().clearDecayingBlocks();

         for (UUID var19 : var2.öO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Stringsuper()) {
            this.plugin
               .getParkourManager()
               .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                  var19
               );
         }

         org.lime.swiftCore.arena.d var16 = var2.øO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000classsuper();
         if (var16 != null) {
            for (Player var6 : this.plugin
               .getSpectatorManager()
               .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                  var16.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int()
               )) {
               if (!var2.öO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Stringsuper()
                  .contains(var6.getUniqueId())) {
                  this.plugin
                     .getSpectatorManager()
                     .Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
                        var6
                     );
               }
            }
         }

         for (UUID var24 : var2.öO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Stringsuper()) {
            Party var26 = this.partyManager.getPlayerParty(var24);
            if (var26 != null) {
               var26.setMode(PartyMode.IDLE);
            }
         }

         for (UUID var25 : var2.öO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Stringsuper()) {
            if (!var2.Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String(
               var25
            )) {
               Player var27 = Bukkit.getPlayer(var25);
               this.plugin
                  .getArenaManager()
                  .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
                     var25
                  );
               if (var27 != null) {
                  if (this.plugin
                     .getSpectatorManager()
                     .Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String(
                        var25
                     )) {
                     this.plugin
                        .getSpectatorManager()
                        .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                           var25
                        );
                  }

                  this.plugin.getArenaListener().cancelThrownPearl(var25);
                  var27.getInventory().clear();
                  this.plugin
                     .getKitManager()
                     .Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
                        var27
                     );
                  this.plugin.getKitRulesListener().clearBoxingHits(var25);
                  this.plugin.getKitRulesListener().clearCooldowns(var25);
                  this.resetPlayerHealth(var27);
                  var27.setGameMode(GameMode.SURVIVAL);
                  boolean var7 = this.plugin.getPartyManager().isInParty(var25);
                  boolean var8 = var7 && this.plugin.getPartyManager().isPartyOwner(var25);
                  if (this.isCrossServerArena()) {
                     this.returnPartyPlayerToLobbyAfterCrossServerGame(var27, var2, var25, var8);
                  } else {
                     this.sendPlayerToLobby(var27);
                     if (var7) {
                        this.plugin.getScoreboardManager().setState(var27, ScoreboardState.PARTY);
                        this.plugin.getScoreboardManager().setPlaceholder(var27, "in_party", "true");
                        this.plugin.getSpawnItemsManager().giveSpawnItems(var27, "party", true, var8);
                     } else {
                        this.plugin.getScoreboardManager().setState(var27, ScoreboardState.DEFAULT);
                        this.plugin.getScoreboardManager().setPlaceholder(var27, "in_party", "false");
                        this.plugin.getSpawnItemsManager().giveSpawnItems(var27, "default", false, false);
                     }

                     this.plugin.getScoreboardManager().setPlaceholder(var27, "in_fight", "false");
                     this.plugin.getScoreboardManager().setPlaceholder(var27, "kit", "");
                     this.plugin.getScoreboardManager().setPlaceholder(var27, "fight_kitname", "");
                     this.plugin.getScoreboardManager().setPlaceholder(var27, "team_icon", "");
                     this.plugin.getScoreboardManager().setPlaceholder(var27, "team_color", "");
                     this.plugin.getScoreboardManager().setPlaceholder(var27, "party_ffa_alive", "");
                     this.plugin.getScoreboardManager().setPlaceholder(var27, "blue_alive", "");
                     this.plugin.getScoreboardManager().setPlaceholder(var27, "red_alive", "");
                     this.plugin.getScoreboardManager().setPlaceholder(var27, "blue_total", "");
                     this.plugin.getScoreboardManager().setPlaceholder(var27, "red_total", "");
                     this.plugin.getScoreboardManager().setPlaceholder(var27, "your_party_alive", "");
                     this.plugin.getScoreboardManager().setPlaceholder(var27, "your_party_total", "");
                     this.plugin.getScoreboardManager().setPlaceholder(var27, "enemy_party_alive", "");
                     this.plugin.getScoreboardManager().setPlaceholder(var27, "enemy_party_total", "");
                     Party var9 = this.partyManager.getPlayerParty(var25);
                     if (var9 != null) {
                        Player var10 = Bukkit.getPlayer(var9.getOwner());
                        String var11 = var10 != null ? var10.getName() : "Unknown";
                        this.plugin.getScoreboardManager().setPlaceholder(var27, "party_owner", var11);
                        this.plugin.getScoreboardManager().setPlaceholder(var27, "party_size", String.valueOf(var9.getSize()));
                        this.plugin.getScoreboardManager().setPlaceholder(var27, "party_max_size", String.valueOf(var9.getMaxSize()));
                        this.plugin.getScoreboardManager().setPlaceholder(var27, "party_mode", var9.getMode().toString());
                     }

                     this.plugin.getScoreboardManager().updateScoreboard(var27);
                     if (this.plugin.getTablistManager() != null) {
                        this.plugin.getTablistManager().resetContext(var27);
                     }
                  }
               }
            }
         }

         if (var16 != null) {
            if (var16.oO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000do()
               == ArenaType.BUILD) {
               this.plugin
                  .getArenaResetManager()
                  .ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000if(
                     var16
                  );
            } else {
               this.plugin
                  .getArenaResetManager()
                  .Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return(
                     var16
                  );
               this.releasePartyArena(var16);
            }
         }
      }
   }

   private void returnPartyPlayerToLobbyAfterCrossServerGame(Player var1, d var2, UUID var3, boolean var4) {
      if (var1 != null && var1.isOnline()) {
         if (this.plugin.getLimboManager() != null
            && this.plugin.getLimboManager().getPlayerDataSync() != null
            && this.plugin.getLimboManager().getTransferManager() != null) {
            HashMap var5 = new HashMap<>(
               this.plugin
                  .getLimboManager()
                  .getPlayerDataSync()
                  .Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
                     var3
                  )
            );
            var5.put("party_state", "returning");
            var5.put("party_owner", String.valueOf(var4));
            var5.put("post_match", "true");
            var5.putIfAbsent("post_match_created_at", String.valueOf(System.currentTimeMillis()));
            if (var2 != null
               && var2.Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return()
                  != null) {
               var5.put(
                  "party_game_type",
                  var2.Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return()
                     .name()
               );
            }

            this.plugin
               .getLimboManager()
               .getPlayerDataSync()
               .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                  var3, var5
               );
            this.plugin
               .getLimboManager()
               .getPlayerDataSync()
               .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                  var1.getName(), var5
               );
            if (this.plugin.isDebug()) {
               this.plugin
                  .getLogger()
                  .info(
                     "[CrossServer Debug] Returning party game player "
                        + var1.getName()
                        + " to lobby after "
                        + (
                           var2 != null
                              ? var2.Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return()
                              : "party"
                        )
                        + " game end."
                  );
            }

            this.plugin
               .getLimboManager()
               .getTransferManager()
               .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                  var1
               );
         } else {
            this.plugin
               .getLobbyManager()
               .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
                  var1
               );
         }
      }
   }

   private void endGameWithDelay(UUID var1, d var2) {
      if (this.cachedEndDelayTicks <= 0) {
         this.endGame(var1);
      } else {
         var2.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
            true
         );

         for (UUID var4 : var2.öO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Stringsuper()) {
            this.plugin.getArenaListener().cancelOutboundCountdown(var4);
            Player var5 = Bukkit.getPlayer(var4);
            if (var5 != null && var5.isOnline()) {
               var5.setInvulnerable(true);
            }
         }

         Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
            UUID var3 = this.getGameOwner(var2);
            this.endGame(var3 != null ? var3 : var1);
         }, (long)this.cachedEndDelayTicks);
      }
   }

   private void endGameVsPartyWithDelay(UUID var1, d var2, List<UUID> var3, List<UUID> var4, String var5) {
      var2.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
         true
      );

      for (UUID var7 : var2.öO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Stringsuper()) {
         this.plugin.getArenaListener().cancelOutboundCountdown(var7);
         Player var8 = Bukkit.getPlayer(var7);
         if (var8 != null && var8.isOnline()) {
            var8.setInvulnerable(true);
         }
      }

      org.lime.swiftCore.arena.d var11 = var2.øO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000classsuper();
      Location var12 = var11 != null
         ? var11.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super()
         : null;

      for (UUID var9 : var4) {
         if (this.isAttachedPartyGameParticipant(var2, var9)) {
            Player var10 = Bukkit.getPlayer(var9);
            if (var10 != null && var10.isOnline() && var12 != null) {
               var10.teleportAsync(var12);
            }
         }
      }

      this.announceTeamVictory(var3, var5, var2, true);
      ArrayList var14 = new ArrayList<>(
         var2.öO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Stringsuper()
      );
      var14.addAll(var4);
      Bukkit.getScheduler()
         .runTaskLater(
            this.plugin,
            () -> {
               for (UUID var5x : var14) {
                  if (this.plugin.getArenaListener().isTempRespawnSpectator(var5x)) {
                     this.plugin.getArenaListener().removeTempRespawnSpectator(var5x);
                     this.plugin
                        .getSpectatorManager()
                        .Ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000void(
                           var5x
                        );
                     Player var6 = Bukkit.getPlayer(var5x);
                     if (var6 != null && var6.isOnline()) {
                        this.plugin.getArenaListener().showTempSpectatorToAll(var6);
                     }
                  } else if (this.plugin
                     .getSpectatorManager()
                     .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
                        var5x
                     )) {
                     this.plugin
                        .getSpectatorManager()
                        .Ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000void(
                           var5x
                        );
                  }

                  if (this.plugin
                     .getSpectatorManager()
                     .Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String(
                        var5x
                     )) {
                     this.plugin
                        .getSpectatorManager()
                        .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                           var5x
                        );
                  }
               }

               UUID var7x = this.getGameOwner(var2);
               this.endGame(var7x != null ? var7x : var1);
            },
            (long)this.cachedEndDelayTicks
         );
   }

   public void handlePlayerDeath(Player var1) {
      d var2 = this.playerToGame.get(var1.getUniqueId());
      if (var2 != null) {
         UUID var3 = this.getGameOwner(var2);
         if (var3 != null) {
            if (var2.Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return()
               == e.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new
               )
             {
               if (var2.Oo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000returnsuper()
                  .contains(var1.getUniqueId())) {
                  return;
               }

               var2.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int(
                  var1.getUniqueId()
               );
               if (this.plugin.getHPIndicatorManager() != null) {
                  this.plugin.getHPIndicatorManager().removeFromGroup(var1.getUniqueId());
               }

               Player var4 = var1.getKiller();
               if (var4 != null && var4.isOnline()) {
                  this.plugin
                     .getKillEffectManager()
                     .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                        var4, var1
                     );
               }

               HashMap var5 = new HashMap();
               var5.put("victim", var1.getName());
               if (var4 != null && var4.isOnline() && !var4.equals(var1)) {
                  var5.put("killer", var4.getName());

                  for (UUID var21 : var2.öO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Stringsuper()) {
                     Player var27 = Bukkit.getPlayer(var21);
                     if (var27 != null && var27.isOnline()) {
                        this.plugin
                           .getMessagesManager()
                           .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                              var27, "party-death-killed", var5
                           );
                     }
                  }
               } else {
                  for (UUID var7 : var2.öO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Stringsuper()) {
                     Player var8 = Bukkit.getPlayer(var7);
                     if (var8 != null && var8.isOnline()) {
                        this.plugin
                           .getMessagesManager()
                           .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                              var8, "party-death-eliminated", var5
                           );
                     }
                  }
               }

               Player var17 = var2.öO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Stringsuper()
                  .stream()
                  .filter(
                     var1x -> !var2.Oo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000returnsuper()
                           .contains(var1x)
                  )
                  .<Player>map(Bukkit::getPlayer)
                  .filter(var0 -> var0 != null && var0.isOnline())
                  .findFirst()
                  .orElse(null);
               if (var17 != null) {
                  if (var2.ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000null()
                     > 1) {
                     this.startPartyRoundSpectating(var1, var17, var2);
                  } else {
                     this.plugin
                        .getSpectatorManager()
                        .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                           var1, var17
                        );
                  }
               }

               try {
                  if (this.cachedEliminateSound != null) {
                     this.cachedEliminateSound
                        .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                           var1
                        );
                  }
               } catch (IllegalArgumentException var13) {
               }

               List var22 = this.getActiveFfaPlayers(var2);
               int var28 = var22.size();

               for (UUID var10 : var2.öO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Stringsuper()) {
                  Player var11 = Bukkit.getPlayer(var10);
                  if (var11 != null && var11.isOnline()) {
                     this.plugin.getScoreboardManager().setPlaceholder(var11, "party_ffa_alive", String.valueOf(var28));
                  }
               }

               if (var28 <= 1) {
                  this.scheduleFfaRoundResolution(var3, var2);
               }
            } else if (var2.Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return()
                  == e.Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String
               || var2.Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return()
                  == e.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super
               )
             {
               if (var2.Oo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000returnsuper()
                  .contains(var1.getUniqueId())) {
                  return;
               }

               if (this.plugin.getHPIndicatorManager() != null) {
                  this.plugin.getHPIndicatorManager().removeFromGroup(var1.getUniqueId());
               }

               Player var14 = var1.getKiller();
               if (var14 != null && var14.isOnline()) {
                  this.plugin
                     .getKillEffectManager()
                     .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                        var14, var1
                     );
               }

               HashMap var15 = new HashMap();
               var15.put("victim", var1.getName());
               if (var14 != null && var14.isOnline() && !var14.equals(var1)) {
                  var15.put("killer", var14.getName());

                  for (UUID var24 : var2.öO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Stringsuper()) {
                     Player var30 = Bukkit.getPlayer(var24);
                     if (var30 != null && var30.isOnline()) {
                        this.plugin
                           .getMessagesManager()
                           .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                              var30, "party-death-killed", var15
                           );
                     }
                  }
               } else {
                  for (UUID var23 : var2.öO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Stringsuper()) {
                     Player var29 = Bukkit.getPlayer(var23);
                     if (var29 != null && var29.isOnline()) {
                        this.plugin
                           .getMessagesManager()
                           .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                              var29, "party-death-eliminated", var15
                           );
                     }
                  }
               }

               if (this.plugin.getTablistManager() != null) {
                  this.plugin
                     .getTablistManager()
                     .removePlayerGlow(
                        var1.getUniqueId(),
                        var2.öO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Stringsuper()
                     );
               }

               boolean var20 = var2.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int()
                  .contains(var1.getUniqueId());
               Object var25 = null;
               if (var20) {
                  var25 = var2.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int()
                     .stream()
                     .filter(var1x -> !var1x.equals(var1.getUniqueId()))
                     .<Player>map(Bukkit::getPlayer)
                     .filter(var0 -> var0 != null && var0.isOnline())
                     .findFirst()
                     .orElse(null);
                  var2.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int()
                     .remove(var1.getUniqueId());
                  var2.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int(
                     var1.getUniqueId()
                  );
                  if (var2.Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return()
                     == e.Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String
                     )
                   {
                     for (UUID var38 : var2.öO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Stringsuper()) {
                        Player var42 = Bukkit.getPlayer(var38);
                        if (var42 != null && var42.isOnline()) {
                           this.plugin
                              .getScoreboardManager()
                              .setPlaceholder(
                                 var42,
                                 "blue_alive",
                                 String.valueOf(
                                    var2.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int()
                                       .size()
                                 )
                              );
                           this.plugin
                              .getScoreboardManager()
                              .setPlaceholder(
                                 var42,
                                 "red_alive",
                                 String.valueOf(
                                    var2.ÒÒ00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000newnew()
                                       .size()
                                 )
                              );
                        }
                     }
                  } else if (var2.Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return()
                     == e.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super
                     )
                   {
                     for (UUID var39 : var2.öO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Stringsuper()) {
                        Player var43 = Bukkit.getPlayer(var39);
                        if (var43 != null && var43.isOnline()) {
                           boolean var45 = var2.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int()
                              .contains(var39);
                           if (var45) {
                              this.plugin
                                 .getScoreboardManager()
                                 .setPlaceholder(
                                    var43,
                                    "your_party_alive",
                                    String.valueOf(
                                       var2.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int()
                                          .size()
                                    )
                                 );
                              this.plugin
                                 .getScoreboardManager()
                                 .setPlaceholder(
                                    var43,
                                    "enemy_party_alive",
                                    String.valueOf(
                                       var2.ÒÒ00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000newnew()
                                          .size()
                                    )
                                 );
                           } else {
                              this.plugin
                                 .getScoreboardManager()
                                 .setPlaceholder(
                                    var43,
                                    "your_party_alive",
                                    String.valueOf(
                                       var2.ÒÒ00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000newnew()
                                          .size()
                                    )
                                 );
                              this.plugin
                                 .getScoreboardManager()
                                 .setPlaceholder(
                                    var43,
                                    "enemy_party_alive",
                                    String.valueOf(
                                       var2.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int()
                                          .size()
                                    )
                                 );
                           }
                        }
                     }
                  }

                  if (var2.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int()
                     .isEmpty()) {
                     this.handleTeamEliminated(var3, var2, "team2", "Blue Team");
                     if (!this.activeGames.containsValue(var2)) {
                        return;
                     }
                  }
               } else {
                  var25 = var2.ÒÒ00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000newnew()
                     .stream()
                     .filter(var1x -> !var1x.equals(var1.getUniqueId()))
                     .<Player>map(Bukkit::getPlayer)
                     .filter(var0 -> var0 != null && var0.isOnline())
                     .findFirst()
                     .orElse(null);
                  var2.ÒÒ00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000newnew()
                     .remove(var1.getUniqueId());
                  var2.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int(
                     var1.getUniqueId()
                  );
                  if (var2.Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return()
                     == e.Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String
                     )
                   {
                     for (UUID var36 : var2.öO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Stringsuper()) {
                        Player var40 = Bukkit.getPlayer(var36);
                        if (var40 != null && var40.isOnline()) {
                           this.plugin
                              .getScoreboardManager()
                              .setPlaceholder(
                                 var40,
                                 "blue_alive",
                                 String.valueOf(
                                    var2.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int()
                                       .size()
                                 )
                              );
                           this.plugin
                              .getScoreboardManager()
                              .setPlaceholder(
                                 var40,
                                 "red_alive",
                                 String.valueOf(
                                    var2.ÒÒ00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000newnew()
                                       .size()
                                 )
                              );
                        }
                     }
                  } else if (var2.Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return()
                     == e.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super
                     )
                   {
                     for (UUID var37 : var2.öO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Stringsuper()) {
                        Player var41 = Bukkit.getPlayer(var37);
                        if (var41 != null && var41.isOnline()) {
                           boolean var44 = var2.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int()
                              .contains(var37);
                           if (var44) {
                              this.plugin
                                 .getScoreboardManager()
                                 .setPlaceholder(
                                    var41,
                                    "your_party_alive",
                                    String.valueOf(
                                       var2.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int()
                                          .size()
                                    )
                                 );
                              this.plugin
                                 .getScoreboardManager()
                                 .setPlaceholder(
                                    var41,
                                    "enemy_party_alive",
                                    String.valueOf(
                                       var2.ÒÒ00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000newnew()
                                          .size()
                                    )
                                 );
                           } else {
                              this.plugin
                                 .getScoreboardManager()
                                 .setPlaceholder(
                                    var41,
                                    "your_party_alive",
                                    String.valueOf(
                                       var2.ÒÒ00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000newnew()
                                          .size()
                                    )
                                 );
                              this.plugin
                                 .getScoreboardManager()
                                 .setPlaceholder(
                                    var41,
                                    "enemy_party_alive",
                                    String.valueOf(
                                       var2.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int()
                                          .size()
                                    )
                                 );
                           }
                        }
                     }
                  }

                  if (var2.ÒÒ00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000newnew()
                     .isEmpty()) {
                     this.handleTeamEliminated(var3, var2, "team1", "Red Team");
                     if (!this.activeGames.containsValue(var2)) {
                        return;
                     }
                  }
               }

               Player var35 = (Player)var25;
               if (var25 == null) {
                  var35 = var2.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int()
                     .stream()
                     .<Player>map(Bukkit::getPlayer)
                     .filter(var0 -> var0 != null && var0.isOnline())
                     .findFirst()
                     .orElse(
                        var2.ÒÒ00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000newnew()
                           .stream()
                           .<Player>map(Bukkit::getPlayer)
                           .filter(var0 -> var0 != null && var0.isOnline())
                           .findFirst()
                           .orElse(null)
                     );
               }

               if (var35 != null) {
                  if (var2.ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000null()
                     > 1) {
                     this.startPartyRoundSpectating(var1, var35, var2);
                  } else {
                     this.plugin
                        .getSpectatorManager()
                        .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                           var1, var35
                        );
                  }
               }

               try {
                  if (this.cachedEliminateSound != null) {
                     this.cachedEliminateSound
                        .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                           var1
                        );
                  }
               } catch (IllegalArgumentException var12) {
               }
            }
         }
      }
   }

   public void handlePlayerQuit(UUID var1) {
      this.pendingChallenges.remove(var1);
      this.plugin.getArenaListener().cancelThrownPearl(var1);
      this.plugin
         .getDuelManager()
         .getMatchFoundEffects()
         .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
            var1
         );
      d var2 = this.getPlayerActiveGame(var1);
      if (var2 != null) {
         UUID var3 = this.getGameOwner(var2);
         boolean var4 = var1.equals(var3);
         if ((
               var2.Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return()
                     == e.Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String
                  || var2.Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return()
                     == e.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super
            )
            && this.plugin.getTablistManager() != null) {
            this.plugin
               .getTablistManager()
               .removePlayerGlow(
                  var1,
                  var2.öO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Stringsuper()
               );
         }

         var2.öO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Stringsuper()
            .remove(var1);
         var2.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int(
            var1
         );
         var2.õo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000publicsuper()
            .remove(var1);
         this.playerToGame.remove(var1);
         this.plugin.getKitRulesListener().removeActivePlayer(var1);
         this.plugin
            .getTntTagManager()
            .Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
               var1
            );
         if (var4 && var3 != null) {
            UUID var5 = var2.öO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Stringsuper()
               .stream()
               .filter(
                  var1x -> !var2.Oo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000returnsuper()
                        .contains(var1x)
               )
               .findFirst()
               .orElse(
                  var2.öO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Stringsuper()
                        .isEmpty()
                     ? null
                     : var2.öO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Stringsuper()
                        .get(0)
               );
            if (var5 != null) {
               this.partyManager.transferOwnershipToMember(var5);
               this.activeGames.remove(var3);
               this.activeGames.put(var5, var2);
               this.gameToOwner.remove(var2);
               this.gameToOwner.put(var2, var5);
            }
         }

         if (var2.Òo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000nullsuper()
            != null) {
            var2.Òo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000nullsuper()
               .remove(var1);
         }

         if (var2.Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class()
            != null) {
            var2.Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class()
               .remove(var1);
         }

         if (var2.ØO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000supersuper()
            != null) {
            var2.ØO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000supersuper()
               .remove(var1);
         }

         if (var2.Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return()
            == e.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new
            )
          {
            int var16 = (int)var2.öO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Stringsuper()
               .stream()
               .filter(
                  var1x -> !var2.Oo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000returnsuper()
                        .contains(var1x)
               )
               .count();

            for (UUID var7 : var2.öO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Stringsuper()) {
               Player var8 = Bukkit.getPlayer(var7);
               if (var8 != null && var8.isOnline()) {
                  this.plugin.getScoreboardManager().setPlaceholder(var8, "party_ffa_alive", String.valueOf(var16));
               }
            }

            if (var16 <= 1) {
               UUID var19 = this.getGameOwner(var2);
               if (var19 != null) {
                  UUID var22 = var2.öO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Stringsuper()
                     .stream()
                     .filter(
                        var1x -> !var2.Oo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000returnsuper()
                              .contains(var1x)
                     )
                     .findFirst()
                     .orElse(null);
                  if (var22 != null) {
                     Player var24 = Bukkit.getPlayer(var22);
                     String var9 = var24 != null ? var24.getName() : "Unknown";
                     if (var2.ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000null()
                        > 1) {
                        var2.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
                           var22
                        );
                        UUID var10 = null;
                        int var11 = 0;

                        for (Entry var13 : var2.Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String()
                           .entrySet()) {
                           if ((Integer)var13.getValue() > var11) {
                              var11 = (Integer)var13.getValue();
                              var10 = (UUID)var13.getKey();
                           }
                        }

                        if (var10 != null) {
                           var2.Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return(
                              var10
                           );
                           Player var29 = Bukkit.getPlayer(var10);
                           var9 = var29 != null ? var29.getName() : "Unknown";
                        }
                     } else {
                        var2.Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return(
                           var22
                        );
                     }

                     HashMap var27 = new HashMap();
                     var27.put("partyffa_winner", var9);
                     var27.put("winner", var9);

                     for (UUID var30 : var2.öO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Stringsuper()) {
                        Player var31 = Bukkit.getPlayer(var30);
                        if (var31 != null && var31.isOnline()) {
                           this.plugin
                              .getMessagesManager()
                              .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
                                 var31, "party-ffa-win-title", var27
                              );
                           this.plugin
                              .getMessagesManager()
                              .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                                 var31, "party-ffa-win-message", var27
                              );

                           try {
                              if (this.cachedWinSound != null) {
                                 this.cachedWinSound
                                    .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                                       var31
                                    );
                              }
                           } catch (IllegalArgumentException var15) {
                           }
                        }
                     }
                  }

                  this.endGameWithDelay(var19, var2);
               }
            }
         } else if (var2.Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return()
               == e.Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String
            || var2.Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return()
               == e.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super
            )
          {
            boolean var17 = var2.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int()
               .contains(var1);
            if (var17) {
               var2.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int()
                  .remove(var1);
            } else {
               var2.ÒÒ00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000newnew()
                  .remove(var1);
            }

            for (UUID var23 : var2.öO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Stringsuper()) {
               Player var25 = Bukkit.getPlayer(var23);
               if (var25 != null && var25.isOnline()) {
                  if (var2.Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return()
                     == e.Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String
                     )
                   {
                     this.plugin
                        .getScoreboardManager()
                        .setPlaceholder(
                           var25,
                           "blue_alive",
                           String.valueOf(
                              var2.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int()
                                 .size()
                           )
                        );
                     this.plugin
                        .getScoreboardManager()
                        .setPlaceholder(
                           var25,
                           "red_alive",
                           String.valueOf(
                              var2.ÒÒ00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000newnew()
                                 .size()
                           )
                        );
                  } else {
                     boolean var26 = var2.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int()
                        .contains(var23);
                     if (var26) {
                        this.plugin
                           .getScoreboardManager()
                           .setPlaceholder(
                              var25,
                              "your_party_alive",
                              String.valueOf(
                                 var2.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int()
                                    .size()
                              )
                           );
                        this.plugin
                           .getScoreboardManager()
                           .setPlaceholder(
                              var25,
                              "enemy_party_alive",
                              String.valueOf(
                                 var2.ÒÒ00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000newnew()
                                    .size()
                              )
                           );
                     } else {
                        this.plugin
                           .getScoreboardManager()
                           .setPlaceholder(
                              var25,
                              "your_party_alive",
                              String.valueOf(
                                 var2.ÒÒ00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000newnew()
                                    .size()
                              )
                           );
                        this.plugin
                           .getScoreboardManager()
                           .setPlaceholder(
                              var25,
                              "enemy_party_alive",
                              String.valueOf(
                                 var2.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int()
                                    .size()
                              )
                           );
                     }
                  }
               }
            }

            UUID var21 = this.getGameOwner(var2);
            if (var2.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int()
                  .isEmpty()
               && var21 != null) {
               this.announceTeamVictory(
                  var2.ÒÒ00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000newnew(),
                  "Blue Team",
                  var2,
                  var2.Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return()
                     == e.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super
               );
               this.endGameWithDelay(var21, var2);
            } else if (var2.ÒÒ00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000newnew()
                  .isEmpty()
               && var21 != null) {
               this.announceTeamVictory(
                  var2.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int(),
                  "Red Team",
                  var2,
                  var2.Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return()
                     == e.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super
               );
               this.endGameWithDelay(var21, var2);
            }
         }

         this.plugin
            .getArenaManager()
            .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
               var1
            );
         this.plugin
            .getSpectatorManager()
            .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
               var1
            );
         this.plugin.getArenaListener().removeTempRespawnSpectator(var1);
         Player var18 = Bukkit.getPlayer(var1);
         if (var18 != null) {
            this.plugin
               .getSpectatorManager()
               .oO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000do(
                  var18
               );
            this.plugin
               .getKitManager()
               .Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
                  var18
               );
            var18.setInvulnerable(false);
            var18.setFlying(false);
            var18.setAllowFlight(false);
            var18.setFallDistance(0.0F);
         } else {
            this.plugin
               .getSpectatorManager()
               .Ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000void(
                  var1
               );
         }
      }
   }

   private List<UUID> getActiveFfaPlayers(d var1) {
      if (var1 == null) {
         return Collections.emptyList();
      } else {
         HashSet var2 = var1.ØO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000supersuper()
               == null
            ? null
            : new HashSet<>(
               var1.ØO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000supersuper()
            );
         ArrayList var3 = new ArrayList();

         for (UUID var5 : var1.öO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Stringsuper()) {
            if ((var2 == null || var2.contains(var5))
               && !var1.Oo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000returnsuper()
                  .contains(var5)
               && !var1.Ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000void(
                  var5
               )) {
               Player var6 = Bukkit.getPlayer(var5);
               if (var6 != null
                  && var6.isOnline()
                  && !this.plugin.getArenaListener().isTempRespawnSpectator(var5)
                  && !this.plugin
                     .getSpectatorManager()
                     .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
                        var5
                     )
                  && !this.plugin
                     .getSpectatorManager()
                     .Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String(
                        var5
                     )) {
                  var3.add(var5);
               }
            }
         }

         return var3;
      }
   }

   public void eliminateColorPartyFfaPlayer(UUID var1) {
      d var2 = this.playerToGame.get(var1);
      if (var2 != null
         && var2.Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return()
            == e.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new
         && ColorPartyKitGuard.isColorPartyKit(
            this.plugin,
            var2.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new()
         )
         && !var2.Oo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000returnsuper()
            .contains(var1)) {
         UUID var3 = this.getGameOwner(var2);
         if (var3 != null) {
            var2.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int(
               var1
            );
            if (this.plugin.getHPIndicatorManager() != null) {
               this.plugin.getHPIndicatorManager().removeFromGroup(var1);
            }

            Player var4 = Bukkit.getPlayer(var1);
            Player var5 = var2.öO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Stringsuper()
               .stream()
               .filter(var1x -> !var1x.equals(var1))
               .filter(
                  var1x -> !var2.Oo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000returnsuper()
                        .contains(var1x)
               )
               .filter(
                  var1x -> !var2.õo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000publicsuper()
                        .contains(var1x)
               )
               .<Player>map(Bukkit::getPlayer)
               .filter(var0 -> var0 != null && var0.isOnline())
               .findFirst()
               .orElse(null);
            if (var4 != null && var4.isOnline() && var5 != null) {
               if (var2.ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000null()
                  > 1) {
                  this.startPartyRoundSpectating(var4, var5, var2);
               } else {
                  this.plugin
                     .getSpectatorManager()
                     .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                        var4, var5
                     );
               }
            }

            List var6 = this.getActiveFfaPlayers(var2);
            this.updateFfaAlivePlaceholders(var2, var6.size());
            if (var6.size() <= 1) {
               this.scheduleFfaRoundResolution(var3, var2);
            }
         }
      }
   }

   public void resolveColorPartyFfaWinner(UUID var1) {
      if (var1 != null) {
         d var2 = this.playerToGame.get(var1);
         if (var2 != null
            && var2.Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return()
               == e.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new
            && ColorPartyKitGuard.isColorPartyKit(
               this.plugin,
               var2.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new()
            )) {
            UUID var3 = this.getGameOwner(var2);
            if (var3 != null) {
               for (UUID var5 : new ArrayList<>(this.getActiveFfaPlayers(var2))) {
                  if (!var5.equals(var1)) {
                     var2.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int(
                        var5
                     );
                  }
               }

               this.updateFfaAlivePlaceholders(var2, this.getActiveFfaPlayers(var2).size());
               this.scheduleFfaRoundResolution(var3, var2);
            }
         }
      }
   }

   private void scheduleFfaRoundResolution(UUID var1, d var2) {
      if (!var2.oO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000do()
         )
       {
         var2.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
            true
         );
         if (this.plugin.getLavaRaiseManager() != null) {
            this.plugin
               .getLavaRaiseManager()
               .stopRound(
                  var2.øO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000classsuper()
               );
         }

         Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
            d var3 = this.activeGames.get(var1);
            if (var3 != null && var3 == var2) {
               this.resolveFfaRound(var1, var2);
            }
         }, 1L);
      }
   }

   private void resolveFfaRound(UUID var1, d var2) {
      List var3 = this.getActiveFfaPlayers(var2);
      int var4 = var3.size();
      this.updateFfaAlivePlaceholders(var2, var4);
      if (var4 > 1) {
         var2.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
            false
         );
      } else {
         UUID var5 = var4 == 1 ? (UUID)var3.get(0) : null;
         if (var5 != null) {
            var2.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
               var5
            );
         }

         if (var2.ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000null()
               > 1
            && this.shouldContinueFfaAfterRound(var2, var5)) {
            this.announceFfaRoundResult(var2, var5);
            Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
               d var3x = this.activeGames.get(var1);
               if (var3x != null && var3x == var2) {
                  this.startNextRoundFFA(var1, var2);
               }
            }, 60L);
         } else {
            this.finishFfaGame(var1, var2, var5);
         }
      }
   }

   private boolean shouldContinueFfaAfterRound(d var1, UUID var2) {
      return var2 != null
            && var1.Öo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000forsuper()
         ? false
         : var1.Ôo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000ifsuper()
            || var1.ÔO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000private()
               < var1.ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000null();
   }

   private void updateFfaAlivePlaceholders(d var1, int var2) {
      for (UUID var4 : var1.öO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Stringsuper()) {
         Player var5 = Bukkit.getPlayer(var4);
         if (var5 != null && var5.isOnline()) {
            this.plugin.getScoreboardManager().setPlaceholder(var5, "party_ffa_alive", String.valueOf(var2));
         }
      }
   }

   private void announceFfaRoundResult(d var1, UUID var2) {
      String var3 = "No one";
      if (var2 != null) {
         Player var4 = Bukkit.getPlayer(var2);
         var3 = var4 != null ? var4.getName() : "Unknown";
      }

      HashMap var8 = new HashMap();
      var8.put("winner", var3);
      var8.put(
         "round",
         String.valueOf(
            var1.ÔO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000private()
         )
      );

      for (UUID var6 : var1.öO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Stringsuper()) {
         Player var7 = Bukkit.getPlayer(var6);
         if (var7 != null && var7.isOnline()) {
            this.plugin
               .getMessagesManager()
               .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
                  var7, "party-ffa-round-win-title", var8
               );
            this.plugin
               .getMessagesManager()
               .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                  var7, "party-ffa-round-win-message", var8
               );
         }
      }
   }

   private void finishFfaGame(UUID var1, d var2, UUID var3) {
      UUID var4 = var3;
      if (var2.ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000null()
         > 1) {
         var4 = this.getOverallFfaWinner(var2);
      }

      var2.Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return(
         var4
      );
      String var5 = "No one";
      if (var4 != null) {
         Player var6 = Bukkit.getPlayer(var4);
         var5 = var6 != null ? var6.getName() : "Unknown";
      }

      HashMap var12 = new HashMap();
      var12.put("partyffa_winner", var5);
      var12.put("winner", var5);

      for (UUID var8 : var2.öO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Stringsuper()) {
         Player var9 = Bukkit.getPlayer(var8);
         if (var9 != null && var9.isOnline()) {
            if (this.isCrossServerArena()) {
               this.saveCrossServerPostMatchMessage(var8, "title", "party-ffa-win-title", var12);
               this.saveCrossServerPostMatchMessage(var8, "message", "party-ffa-win-message", var12);
            } else {
               this.plugin
                  .getMessagesManager()
                  .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
                     var9, "party-ffa-win-title", var12
                  );
               this.plugin
                  .getMessagesManager()
                  .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                     var9, "party-ffa-win-message", var12
                  );
            }

            try {
               if (this.cachedWinSound != null) {
                  this.cachedWinSound
                     .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                        var9
                     );
               }
            } catch (IllegalArgumentException var11) {
            }
         }
      }

      if (this.plugin.getClanManager() != null && var4 != null) {
         this.plugin
            .getClanManager()
            .ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000null(
               var4
            );
      }

      this.endGameWithDelay(var1, var2);
   }

   private UUID getOverallFfaWinner(d var1) {
      UUID var2 = null;
      int var3 = 0;
      boolean var4 = false;

      for (Entry var6 : var1.Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String()
         .entrySet()) {
         int var7 = (Integer)var6.getValue();
         if (var7 > var3) {
            var3 = var7;
            var2 = (UUID)var6.getKey();
            var4 = false;
         } else if (var7 == var3 && var7 > 0) {
            var4 = true;
         }
      }

      return var4 ? null : var2;
   }

   private void handleTeamEliminated(UUID var1, d var2, String var3, String var4) {
      if (!var2.oO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000do()
         )
       {
         var2.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
            true
         );
         if (this.plugin.getLavaRaiseManager() != null) {
            this.plugin
               .getLavaRaiseManager()
               .stopRound(
                  var2.øO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000classsuper()
               );
         }

         boolean var5 = var3.equals("team1");
         if (var5) {
            var2.Øo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000dosuper();
         } else {
            var2.Õo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000floatsuper();
         }

         String var6 = var5 ? "Blue Team" : "Red Team";
         if (var2.ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000null()
               > 1
            && !var2.OÒ00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000thisnew()
            )
          {
            HashMap var11 = new HashMap();
            var11.put("winner_team", var6);
            var11.put(
               "round",
               String.valueOf(
                  var2.ÔO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000private()
               )
            );
            var11.put(
               "blue_wins",
               String.valueOf(
                  var2.ÖO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000thissuper()
               )
            );
            var11.put(
               "red_wins",
               String.valueOf(
                  var2.õO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Objectsuper()
               )
            );

            for (UUID var9 : var2.öO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Stringsuper()) {
               Player var10 = Bukkit.getPlayer(var9);
               if (var10 != null && var10.isOnline()) {
                  this.plugin
                     .getMessagesManager()
                     .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
                        var10, "party-round-win-title", var11
                     );
                  this.plugin
                     .getMessagesManager()
                     .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                        var10, "party-round-win-message", var11
                     );
               }
            }

            Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
               d var4x = this.activeGames.get(var1);
               if (var4x != null && var4x == var2) {
                  this.startNextRound(var1, var2, var6);
               }
            }, 60L);
         } else {
            ArrayList var7 = var5
               ? new ArrayList<>(
                  var2.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int()
               )
               : new ArrayList<>(
                  var2.ÒÒ00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000newnew()
               );
            if (var7.isEmpty()) {
               var7 = var5
                  ? new ArrayList<>(
                     var2.Òo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000nullsuper()
                  )
                  : new ArrayList<>(
                     var2.Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class()
                  );
            }

            if (var2.Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return()
               == e.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super
               )
             {
               List var8 = var2.Oo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000returnsuper()
                  .stream()
                  .filter(var2x -> this.isAttachedPartyGameParticipant(var2, var2x))
                  .collect(Collectors.toCollection(ArrayList::new));
               this.endGameVsPartyWithDelay(var1, var2, var7, var8, var4);
            } else {
               this.announceTeamVictory(var7, var4, var2);
               this.endGameWithDelay(var1, var2);
            }
         }
      }
   }

   private void announceTeamVictory(List<UUID> var1, String var2, d var3) {
      this.announceTeamVictory(var1, var2, var3, false);
   }

   private void announceTeamVictory(List<UUID> var1, String var2, d var3, boolean var4) {
      String var5 = var2.equals("Red Team") ? "Blue Team" : "Red Team";
      if (var3 != null) {
         var3.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
            var5
         );
      }

      HashMap var6 = new HashMap();
      var6.put("loser_team", var2);
      var6.put("winner_team", var5);
      var6.put("partysplit_winner_team", var5);
      var6.put("partyvs_winner_party", var5);

      for (UUID var9 : var3 != null
         ? var3.öO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Stringsuper()
         : var1) {
         Player var10 = Bukkit.getPlayer(var9);
         if (var10 != null && var10.isOnline()) {
            String var11 = var4 ? "party-vs-win-title" : "party-split-win-title";
            String var12 = var4 ? "party-vs-win-message" : "party-split-win-message";
            if (this.isCrossServerArena()) {
               this.saveCrossServerPostMatchMessage(var9, "title", var11, var6);
               this.saveCrossServerPostMatchMessage(var9, "message", var12, var6);
            } else {
               this.plugin
                  .getMessagesManager()
                  .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
                     var10, var11, var6
                  );
               this.plugin
                  .getMessagesManager()
                  .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                     var10, var12, var6
                  );
            }

            try {
               if (this.cachedWinSound != null) {
                  this.cachedWinSound
                     .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                        var10
                     );
               }
            } catch (IllegalArgumentException var14) {
            }
         }
      }

      if (this.plugin.getClanManager() != null) {
         for (UUID var16 : var1) {
            this.plugin
               .getClanManager()
               .ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000null(
                  var16
               );
         }
      }
   }

   private boolean getKitRule(String var1, KitRule var2) {
      if (this.isCustomKit(var1)) {
         CustomKitAPI.CustomKitData var3 = this.getCustomKitData(var1);
         return var3 != null && var3.rules() != null ? var3.rules().getOrDefault(var2, var2.getDefaultValue()) : var2.getDefaultValue();
      } else {
         return this.plugin
            .getDataManager()
            .Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class(
               var1
            )
            .getOrDefault(var2, var2.getDefaultValue());
      }
   }

   private void applyKitHealth(Player var1, String var2) {
      boolean var3 = this.getKitRule(var2, KitRule.HEARTS);
      AttributeInstance var4 = org.lime.swiftCore.b.b.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
         var1
      );
      if (var4 == null) {
         var1.setHealth(20.0);
         org.lime.swiftCore.b.h.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
            var1
         );
      } else {
         if (var3) {
            double var5 = this.plugin.getConfig().getDouble("kit-rules.hearts." + var2, 10.0);
            if (var5 < 1.0) {
               var5 = 10.0;
            }

            double var7 = var5 * 2.0;
            var4.setBaseValue(var7);
            var1.setHealth(Math.min(var7, var4.getValue()));
            var1.setHealthScaled(true);
            var1.setHealthScale(var7);
         } else {
            var4.setBaseValue(20.0);
            var1.setHealth(Math.min(20.0, var4.getValue()));
            var1.setHealthScaled(false);
         }

         org.lime.swiftCore.b.h.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
            var1
         );
      }
   }

   private void resetPlayerHealth(Player var1) {
      AttributeInstance var2 = org.lime.swiftCore.b.b.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
         var1
      );
      if (var2 != null) {
         var2.setBaseValue(20.0);
         var1.setHealth(Math.min(20.0, var2.getValue()));
      } else {
         var1.setHealth(20.0);
      }

      var1.setHealthScaled(false);
      var1.setFoodLevel(20);
      var1.setSaturation(5.0F);
   }

   private boolean shouldTransferCrossServer() {
      if (this.plugin.getLimboManager() != null && this.plugin.getLimboManager().isCrossServer()) {
         org.lime.swiftCore.v.t.c var1 = this.plugin
            .getLimboManager()
            .getConfigCache()
            .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new();
         return var1 != null
            && var1.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super()
               == org.lime.swiftCore.v.p.e.Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class;
      } else {
         return false;
      }
   }

   private org.lime.swiftCore.arena.d resolveStagingArena(String var1) {
      if (var1 != null && !var1.isBlank()) {
         org.lime.swiftCore.arena.d var2 = this.plugin
            .getArenaManager()
            .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
               var1
            );
         if (var2 == null) {
            return null;
         } else {
            return var2.Oo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000returnsuper()
                     != org.lime.swiftCore.arena.c.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super
                  && !this.plugin
                     .getArenaManager()
                     .ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float(
                        var2
                     )
               ? null
               : var2;
         }
      } else {
         return null;
      }
   }

   private List<UUID> filterOnlinePlayers(List<UUID> var1) {
      ArrayList var2 = new ArrayList();
      if (var1 == null) {
         return var2;
      } else {
         for (UUID var4 : var1) {
            Player var5 = Bukkit.getPlayer(var4);
            if (var5 != null && var5.isOnline()) {
               var2.add(var4);
            }
         }

         return var2;
      }
   }

   private void clearPartyTransferring(Collection<UUID> var1) {
      if (var1 != null) {
         for (UUID var3 : var1) {
            this.partyTransferring.remove(var3);
         }
      }
   }

   private void clearArrivalMask(Player var1) {
      if (var1 != null && var1.isOnline()) {
         var1.setInvulnerable(false);
         var1.setAllowFlight(false);
         var1.setFlying(false);
         var1.removePotionEffect(PotionEffectType.BLINDNESS);
      }
   }

   private void finalizePartyFightStart(List<UUID> var1, d var2) {
      if (var1 != null) {
         for (UUID var4 : var1) {
            if (var2 == null || this.isPlayerStillInGame(var2, var4)) {
               Player var5 = Bukkit.getPlayer(var4);
               this.clearArrivalMask(var5);
            }
         }
      }
   }

   private void recordKitMatchStat(d var1) {
      if (this.plugin.getKitStatsManager() != null
         && var1 != null
         && var1.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object()
         )
       {
         org.lime.swiftCore.ab.b._c var2 = switch (var1.Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return()) {
            case Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new -> org.lime.swiftCore.ab.b._c.Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String;
            case Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String -> org.lime.swiftCore.ab.b._c.Ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000void;
            case o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super -> org.lime.swiftCore.ab.b._c.Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return;
         };
         this.plugin
            .getKitStatsManager()
            .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
               var1.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(),
               this.getPartyKitDisplayName(
                  var1.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new()
               ),
               var2
            );
      }
   }

   private void registerCrossServerPartyMatch(d var1) {
      if (var1 != null
         && this.isCrossServerArena()
         && this.plugin.getLimboManager().getActiveMatchRegistry() != null
         && var1.øO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000classsuper()
            != null) {
         String var2 = "party-"
            + var1.Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return()
               .name()
               .toLowerCase(Locale.ROOT)
            + "-"
            + Long.toUnsignedString(System.nanoTime());
         this.crossServerRegistryIds.put(var1, var2);
         this.plugin
            .getLimboManager()
            .getActiveMatchRegistry()
            .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
               var2,
               new ArrayList<>(
                  var1.öO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Stringsuper()
               ),
               var1.øO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000classsuper()
                  .õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int(),
               var1.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(),
               var1.Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return()
                  .name()
            );
      }
   }

   private void unregisterCrossServerPartyMatch(d var1) {
      if (var1 != null && this.plugin.getLimboManager() != null && this.plugin.getLimboManager().getActiveMatchRegistry() != null) {
         String var2 = this.crossServerRegistryIds.remove(var1);
         if (var2 != null) {
            this.plugin
               .getLimboManager()
               .getActiveMatchRegistry()
               .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                  var2,
                  new ArrayList<>(
                     var1.öO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Stringsuper()
                  )
               );
         }
      }
   }

   private boolean isCrossServerArena() {
      if (this.plugin.getLimboManager() != null && this.plugin.getLimboManager().isCrossServer()) {
         org.lime.swiftCore.v.t.c var1 = this.plugin
            .getLimboManager()
            .getConfigCache()
            .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new();
         return var1 != null
            && var1.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super()
               == org.lime.swiftCore.v.p.e.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super;
      } else {
         return false;
      }
   }

   private void saveCrossServerPostMatchMessage(UUID var1, String var2, String var3, Map<String, String> var4) {
      if (this.isCrossServerArena() && this.plugin.getLimboManager().getPlayerDataSync() != null) {
         HashMap var5 = new HashMap<>(
            this.plugin
               .getLimboManager()
               .getPlayerDataSync()
               .Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
                  var1
               )
         );
         int var6 = Integer.parseInt(var5.getOrDefault("post_message_count", "0"));
         String var7 = "post_message_" + var6;
         var5.put(var7 + "_type", var2);
         var5.put(var7 + "_key", var3);
         if (var4 != null) {
            for (Entry var9 : var4.entrySet()) {
               var5.put(var7 + "_placeholder_" + (String)var9.getKey(), var9.getValue() != null ? (String)var9.getValue() : "");
            }
         }

         var5.put("post_message_count", String.valueOf(var6 + 1));
         var5.put("post_match", "true");
         var5.putIfAbsent("post_match_created_at", String.valueOf(System.currentTimeMillis()));
         this.plugin
            .getLimboManager()
            .getPlayerDataSync()
            .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
               var1, var5
            );
         Player var10 = Bukkit.getPlayer(var1);
         if (var10 != null) {
            this.plugin
               .getLimboManager()
               .getPlayerDataSync()
               .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                  var10.getName(), var5
               );
         }
      }
   }

   public boolean isPartyTransferring(UUID var1) {
      return this.partyTransferring.contains(var1);
   }

   public void removePartyTransferring(UUID var1) {
      this.partyTransferring.remove(var1);
   }

   public void markPartyTransferring(Collection<UUID> var1) {
      if (var1 != null) {
         this.partyTransferring.addAll(var1);
      }
   }

   private void transferPartyMatch(org.lime.swiftCore.v.f.c var1, String var2, List<UUID> var3, List<UUID> var4, List<UUID> var5, int var6) {
      org.lime.swiftCore.v.p.b var7 = this.plugin
         .getLimboManager()
         .getServerRegistry()
         .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new();
      if (var7 == null) {
         Player var12 = Bukkit.getPlayer((UUID)var3.get(0));
         if (var12 != null) {
            this.plugin
               .getMessagesManager()
               .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                  var12, "party-no-arenas"
               );
         }
      } else {
         ArrayList var8 = new ArrayList();

         for (UUID var10 : var3) {
            Player var11 = Bukkit.getPlayer(var10);
            if (var11 != null && var11.isOnline()) {
               var8.add(var11);
            }
         }

         this.plugin
            .getDuelManager()
            .getMatchFoundEffects()
            .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
               var8, var2
            );
         if (var1
            == org.lime.swiftCore.v.f.c.Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String
            )
          {
            if (var4 != null && !var4.isEmpty()) {
               this.partyManager.setPartyMode((UUID)var4.get(0), PartyMode.IN_GAME);
            }

            if (var5 != null && !var5.isEmpty()) {
               this.partyManager.setPartyMode((UUID)var5.get(0), PartyMode.IN_GAME);
            }
         } else if (!var3.isEmpty()) {
            this.partyManager.setPartyMode((UUID)var3.get(0), PartyMode.IN_GAME);
         }

         this.partyTransferring.addAll(var3);
         int var13 = this.plugin
            .getDuelManager()
            .getMatchFoundEffects()
            .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new();
         String var14 = var7.Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String();
         org.lime.swiftCore.v.f.d var15 = this.plugin.getLimboManager().getTransferManager();
         Bukkit.getScheduler()
            .runTaskLater(
               this.plugin,
               () -> {
                  org.lime.swiftCore.v.f.b var9 = new org.lime.swiftCore.v.f.b(
                     org.lime.swiftCore.v.f.b.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(),
                     var1,
                     var2,
                     var3,
                     var4,
                     var5,
                     var6,
                     false,
                     this.plugin
                        .getLimboManager()
                        .getConfigCache()
                        .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new()
                        .Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class(),
                     null,
                     null
                  );
                  this.plugin
                     .getLimboManager()
                     .getMatchIntentManager()
                     .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                        var9
                     );
                  String var10x = this.plugin
                     .getLimboManager()
                     .getConfigCache()
                     .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new()
                     .Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class();

                  for (UUID var12x : var3) {
                     Player var13x = Bukkit.getPlayer(var12x);
                     if (var13x != null) {
                        this.plugin
                           .getLimboManager()
                           .getMatchIntentManager()
                           .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                              var12x,
                              var13x.getName(),
                              var9.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object()
                           );
                     }

                     this.plugin
                        .getLimboManager()
                        .getMatchIntentManager()
                        .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                           var12x, var10x
                        );
                  }

                  if (this.plugin.isDebug()) {
                     this.plugin
                        .getLogger()
                        .info(
                           "[CrossServer Debug] Saved party MatchIntent "
                              + var9.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object()
                              + " type="
                              + var1
                              + " kit="
                              + var2
                              + " players="
                              + var3.size()
                              + " targetArenaServer="
                              + var14
                              + " returnLobby="
                              + var10x
                              + "."
                        );
                  }

                  for (UUID var15x : var3) {
                     Player var16 = Bukkit.getPlayer(var15x);
                     if (var16 != null && var16.isOnline()) {
                        var16.removePotionEffect(PotionEffectType.BLINDNESS);
                        var15.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                           var16, var14
                        );
                     }
                  }
               },
               (long)var13
            );
      }
   }

   private void sendPlayerToLobby(Player var1) {
      this.sendPlayerToLobby(var1, 0);
   }

   private void sendPlayerToLobby(Player var1, int var2) {
      if (var1 != null && var1.isOnline()) {
         if (this.plugin.getLimboManager() != null && this.plugin.getLimboManager().isCrossServer()) {
            org.lime.swiftCore.v.t.c var3 = this.plugin
               .getLimboManager()
               .getConfigCache()
               .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new();
            if (var3 != null
               && var3.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super()
                  == org.lime.swiftCore.v.p.e.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super
               )
             {
               g var4 = this.plugin
                  .getLimboManager()
                  .getTransferManager()
                  .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                     var1
                  );
               if (var4
                     != g.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object
                  && var2 < 2) {
                  Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
                     Player var3x = Bukkit.getPlayer(var1.getUniqueId());
                     if (var3x != null && var3x.isOnline()) {
                        this.sendPlayerToLobby(var3x, var2 + 1);
                     }
                  }, 20L);
               } else if (var4
                  != g.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object
                  )
                {
                  this.plugin.getLogger().warning("[Party] Failed to return " + var1.getName() + " to a network lobby after " + (var2 + 1) + " attempts.");
               }

               return;
            }
         }

         UUID var5 = var1.getUniqueId();
         this.plugin
            .getLobbyManager()
            .Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
               var1
            )
            .thenAccept(
               var2x -> {
                  if (!Boolean.TRUE.equals(var2x)) {
                     Bukkit.getScheduler()
                        .runTaskLater(
                           this.plugin,
                           () -> {
                              Player var2xx = Bukkit.getPlayer(var5);
                              if (var2xx != null && var2xx.isOnline()) {
                                 this.plugin
                                    .getLobbyManager()
                                    .Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
                                       var2xx
                                    )
                                    .thenAccept(var2xxx -> {
                                       if (!Boolean.TRUE.equals(var2xxx)) {
                                          this.plugin
                                             .getLogger()
                                             .warning("[Party] Failed to return " + var2xx.getName() + " to the local lobby after two attempts.");
                                       }
                                    });
                              }
                           },
                           20L
                        );
                  }
               }
            );
      }
   }

   private void forceShowGameParticipants(List<UUID> var1) {
      ArrayList var2 = new ArrayList();

      for (UUID var4 : var1) {
         Player var5 = Bukkit.getPlayer(var4);
         if (var5 != null && var5.isOnline()) {
            var2.add(var5);
         }
      }

      this.plugin
         .getPlayerSettingsManager()
         .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
            var2
         );
   }
}
