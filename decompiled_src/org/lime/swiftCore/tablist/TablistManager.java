package org.lime.swiftCore.tablist;

import com.destroystokyo.paper.profile.ProfileProperty;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerCommon;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.protocol.player.TextureProperty;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoRemove;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoUpdate;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerListHeaderAndFooter;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoUpdate.Action;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoUpdate.PlayerInfo;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams.CollisionRule;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams.NameTagVisibility;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams.OptionData;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams.ScoreBoardTeamInfo;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams.TeamMode;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentHashMap.KeySetView;
import java.util.concurrent.atomic.LongAdder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.ShadowColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.lime.swiftCore.SwiftCore;
import org.lime.swiftCore.b.e;
import org.lime.swiftCore.b.f;
import org.lime.swiftCore.duel.g;
import org.lime.swiftCore.h.c;
import org.lime.swiftCore.libs.caffeine.cache.Cache;
import org.lime.swiftCore.libs.caffeine.cache.Caffeine;
import org.lime.swiftCore.party.Party;
import org.lime.swiftCore.party.d;

public class TablistManager {
   private final SwiftCore plugin;
   private final Map<UUID, TablistPlayerData> playerData;
   private final Map<UUID, Set<UUID>> hiddenPlayers;
   private final Map<UUID, Set<UUID>> visiblePlayersMap;
   private final Map<UUID, TablistManager.PlayerRankCache> rankCache;
   private final Map<UUID, TablistManager.PlayerTeamData> teamNameCache;
   private final Map<UUID, TablistManager.CachedPlayerInfo> playerInfoCache;
   private final Map<UUID, Set<String>> viewerTeams;
   private final Map<UUID, Map<String, String>> viewerTeamSignatures;
   private final Map<UUID, Set<TablistManager.TeamSignatureRef>> playerTeamSignatureRefs;
   private final Map<UUID, NamedTextColor> glowColorOverrides;
   private final Map<UUID, Integer> sortOrderCache;
   private volatile long sortOrderCacheExpiresAt;
   private final Map<UUID, List<UUID>> socialFakeRows;
   private final Map<UUID, List<String>> socialSortTeams;
   private final Map<UUID, String> socialLayoutCache;
   private final Map<UUID, Map<Integer, UUID>> socialFakeIdCache;
   private final Map<UUID, Map<Integer, UUID>> duelFakeIdCache;
   private final Map<TablistManager.PlayerInfoSendKey, Boolean> playerInfoSendsThisTick;
   private int playerInfoSignatureTick = Integer.MIN_VALUE;
   private final TablistRefreshCoordinator refreshCoordinator;
   private final Cache<UUID, UserProfile> userProfileCache = Caffeine.newBuilder().maximumSize(1000L).expireAfterWrite(Duration.ofSeconds(30L)).build();
   private final Cache<UUID, TablistManager.SocialLayout> socialLayoutDataCache = Caffeine.newBuilder()
      .maximumSize(300L)
      .expireAfterWrite(Duration.ofSeconds(2L))
      .build();
   private TablistProvider provider;
   private FileConfiguration tabConfig;
   private TablistUpdater tablistUpdater;
   private PacketListenerCommon playerInfoFilter;
   private boolean enabled;
   private int updateInterval;
   private List<String> groupSorting;
   private boolean sortingEnabled;
   private TabSortOrder.Mode sortingMode;
   private String sortingPlaceholder;
   private TabSortOrder.ValueType sortingPlaceholderType;
   private TabSortOrder.Direction sortingPlaceholderDirection;
   private String prefixFormat;
   private String nameFormat;
   private String suffixFormat;
   private String tagPrefixFormat;
   private String tagSuffixFormat;
   private String tagNameColorFormat;
   private String tagBelowNameFormat;
   private TablistBelowNameManager belowNameManager;
   private boolean shadowEnabled;
   private ShadowColor shadowColor;
   private boolean socialLayoutEnabled;
   private int socialColumnRows;
   private int socialMaxFriends;
   private int socialMaxParty;
   private int socialHeaderGap;
   private int socialPartyStartRow;
   private int socialFakePing;
   private String socialFriendsHeaderFormat;
   private String socialPartyHeaderFormat;
   private String socialEmptyFriendsFormat;
   private String socialEmptyPartyFormat;
   private String socialBlankFormat;
   private boolean duelSocialLayoutEnabled;
   private int duelSocialBlankRows;
   private int duelSocialFakePing;
   private String duelSocialBlankFormat;
   private List<String> duelSocialRows;
   private String duelBluePlayerColor;
   private String duelRedPlayerColor;
   private String partyBluePlayerColor;
   private String partyRedPlayerColor;
   private Map<TablistContext, TablistManager.ContextSocialLayout> contextSocialLayouts;
   private int maxContextSocialRows;
   private PlaceholderCache placeholderCache;
   private LuckPermsListener luckPermsListener;
   private final LongAdder headerFooterUpdateAttempts = new LongAdder();
   private final LongAdder headerFooterPacketsSent = new LongAdder();
   private final Cache<String, Component> displayNameCache = Caffeine.newBuilder().maximumSize(300L).expireAfterAccess(Duration.ofMinutes(2L)).build();
   private static final MiniMessage MINI_MESSAGE = e.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super();
   private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.builder()
      .hexColors()
      .useUnusualXRepeatedCharacterHexFormat()
      .build();
   private static final Pattern HEX_SECTION_PATTERN = Pattern.compile("§x(§[0-9a-fA-F]){6}");
   private static final Pattern HEX_AMPERSAND_PATTERN = Pattern.compile("&#([0-9a-fA-F]{6})");
   private boolean globalEnabled;
   private Set<String> globalEnabledWorlds;
   private long lastTabPapiCleanup = 0L;
   private static final long TAB_PAPI_CLEANUP_INTERVAL_MS = 5000L;

   public TablistManager(SwiftCore var1) {
      this.plugin = var1;
      this.playerData = new ConcurrentHashMap<>();
      this.hiddenPlayers = new ConcurrentHashMap<>();
      this.visiblePlayersMap = new ConcurrentHashMap<>();
      this.rankCache = new ConcurrentHashMap<>();
      this.teamNameCache = new ConcurrentHashMap<>();
      this.playerInfoCache = new ConcurrentHashMap<>();
      this.viewerTeams = new ConcurrentHashMap<>();
      this.viewerTeamSignatures = new ConcurrentHashMap<>();
      this.playerTeamSignatureRefs = new ConcurrentHashMap<>();
      this.glowColorOverrides = new ConcurrentHashMap<>();
      this.sortOrderCache = new ConcurrentHashMap<>();
      this.sortOrderCacheExpiresAt = 0L;
      this.socialFakeRows = new ConcurrentHashMap<>();
      this.socialSortTeams = new ConcurrentHashMap<>();
      this.socialLayoutCache = new ConcurrentHashMap<>();
      this.socialFakeIdCache = new ConcurrentHashMap<>();
      this.duelFakeIdCache = new ConcurrentHashMap<>();
      this.playerInfoSendsThisTick = new HashMap<>();
      this.refreshCoordinator = new TablistRefreshCoordinator();
      this.tabConfig = this.loadTabConfig();
      this.enabled = this.tabConfig.getBoolean("tablist.enabled", true);
      this.updateInterval = this.tabConfig.getInt("tablist.update-interval", 20);
      this.loadSortingConfig();
      this.prefixFormat = this.tabConfig.getString("tablist.player-format.prefix", "");
      this.nameFormat = this.tabConfig.getString("tablist.player-format.name", "%player_name%");
      this.suffixFormat = this.tabConfig.getString("tablist.player-format.suffix", "");
      this.tagPrefixFormat = this.tabConfig.getString("tablist.tag-format.prefix", this.prefixFormat);
      this.tagSuffixFormat = this.tabConfig.getString("tablist.tag-format.suffix", this.suffixFormat);
      this.tagNameColorFormat = this.tabConfig.getString("tablist.tag-format.name-color", "");
      this.tagBelowNameFormat = this.tabConfig.getString("tablist.tag-format.below-name", "");
      this.shadowEnabled = this.tabConfig.getBoolean("tablist.shadow", false);
      this.shadowColor = this.parseShadowColor(this.tabConfig.getString("tablist.shadow-color", "#000000ff"));
      this.loadSocialLayoutConfig();
      this.provider = new TablistProvider(var1, this.tabConfig);
      this.placeholderCache = new PlaceholderCache(this);
      this.luckPermsListener = new LuckPermsListener(var1, this);
      this.belowNameManager = new TablistBelowNameManager(var1, this);
      this.luckPermsListener.setup();
      this.loadGlobalConfig();
      this.registerPlayerInfoFilter();
   }

   private void registerPlayerInfoFilter() {
      if (this.playerInfoFilter == null) {
         try {
            this.playerInfoFilter = PacketEvents.getAPI().getEventManager().registerListener(new TablistPlayerInfoFilter(this));
         } catch (Throwable var2) {
            this.plugin.getLogger().warning("[Tablist] Packet visibility filter unavailable: " + var2.getMessage());
         }
      }
   }

   private void loadSortingConfig() {
      String var1 = "tablist.group-sorting.";
      this.sortingEnabled = this.tabConfig.getBoolean(var1 + "enabled", false);
      this.groupSorting = this.tabConfig.getStringList(var1 + "groups").stream().map(var0 -> var0.toLowerCase(Locale.ROOT)).toList();
      this.sortingMode = TabSortOrder.Mode.parse(this.tabConfig.getString(var1 + "mode", "luckperms-weight"));
      this.sortingPlaceholder = this.tabConfig.getString(var1 + "placeholder.value", "%player_name%");
      this.sortingPlaceholderType = TabSortOrder.ValueType.parse(this.tabConfig.getString(var1 + "placeholder.type", "numeric"));
      this.sortingPlaceholderDirection = TabSortOrder.Direction.parse(this.tabConfig.getString(var1 + "placeholder.order", "descending"));
      this.invalidateSortOrder();
   }

   private void loadGlobalConfig() {
      this.globalEnabled = this.tabConfig.getBoolean("global.enable", false);
      this.globalEnabledWorlds = new HashSet<>();

      for (String var2 : this.tabConfig.getStringList("global.enable-worlds")) {
         this.globalEnabledWorlds.add(var2.toLowerCase());
      }
   }

   private void loadSocialLayoutConfig() {
      String var1 = "tablist.lobby_friends.social-layout.";
      this.socialLayoutEnabled = this.tabConfig.getBoolean(var1 + "enabled", true);
      this.socialColumnRows = Math.max(4, Math.min(20, this.tabConfig.getInt(var1 + "column-rows", 20)));
      this.socialMaxFriends = Math.max(0, Math.min(this.socialColumnRows - 2, this.tabConfig.getInt(var1 + "max-friends", this.socialColumnRows - 2)));
      this.socialMaxParty = Math.max(0, Math.min(this.socialColumnRows - 2, this.tabConfig.getInt(var1 + "max-party", this.socialColumnRows - 2)));
      this.socialHeaderGap = Math.max(0, Math.min(5, this.tabConfig.getInt(var1 + "header-gap", 1)));
      this.socialPartyStartRow = Math.max(0, Math.min(this.socialColumnRows, this.tabConfig.getInt(var1 + "party-start-row", this.socialColumnRows)));
      this.socialFakePing = Math.max(0, this.tabConfig.getInt(var1 + "ping", 1));
      this.socialFriendsHeaderFormat = this.tabConfig.getString(var1 + "friends-header", "&#00FDFF&lFriends &#7F7F7F(%online%/%total%)");
      this.socialPartyHeaderFormat = this.tabConfig.getString(var1 + "party-header", "&#D855FF&lParty &#7F7F7F(%count%)");
      this.socialEmptyFriendsFormat = this.tabConfig.getString(var1 + "empty-friends", " ");
      this.socialEmptyPartyFormat = this.tabConfig.getString(var1 + "empty-party", "&#555555No party");
      this.socialBlankFormat = this.tabConfig.getString(var1 + "blank", " ");
      String var2 = "tablist.duel.social-layout.";
      this.duelSocialLayoutEnabled = this.tabConfig.getBoolean(var2 + "enabled", false);
      this.duelSocialBlankRows = Math.max(0, Math.min(20, this.tabConfig.getInt(var2 + "blank-rows", 1)));
      this.duelSocialFakePing = Math.max(0, this.tabConfig.getInt(var2 + "ping", this.socialFakePing));
      this.duelSocialBlankFormat = this.tabConfig.getString(var2 + "blank", this.socialBlankFormat);
      this.duelBluePlayerColor = this.tabConfig.getString(var2 + "blue-player-color", "&#4DA3FF");
      this.duelRedPlayerColor = this.tabConfig.getString(var2 + "red-player-color", "&#FF5252");
      List var3 = this.tabConfig.getStringList(var2 + "lines");
      ArrayList var4 = new ArrayList();
      if (!var3.isEmpty()) {
         var4.addAll(var3);
      } else {
         for (int var5 = 0; var5 < this.duelSocialBlankRows; var5++) {
            var4.add(this.duelSocialBlankFormat);
         }
      }

      this.duelSocialRows = Collections.unmodifiableList(var4);
      this.partyBluePlayerColor = this.tabConfig.getString("tablist.party_split.social-layout.blue-player-color", "&#4DA3FF");
      this.partyRedPlayerColor = this.tabConfig.getString("tablist.party_split.social-layout.red-player-color", "&#FF5252");
      this.contextSocialLayouts = new EnumMap<>(TablistContext.class);
      this.maxContextSocialRows = 0;
      this.loadContextSocialLayout(TablistContext.PARTY_FFA);
      this.loadContextSocialLayout(TablistContext.PARTY_SPLIT);
      this.loadContextSocialLayout(TablistContext.PARTY_VS);
      this.loadContextSocialLayout(TablistContext.PARTY_BLOCK_DECAY);
      this.loadContextSocialLayout(TablistContext.PARTY_FLOWER_CROWN);
      this.loadContextSocialLayout(TablistContext.PARTY_TNT_TAG);
      this.loadContextSocialLayout(TablistContext.PARTY_BEDWARS);
      this.loadContextSocialLayout(TablistContext.EVENT);
      this.loadContextSocialLayout(TablistContext.TOURNAMENT);
      this.loadContextSocialLayout(TablistContext.TEAM_QUEUE);
   }

   private void loadContextSocialLayout(TablistContext var1) {
      String var2 = "tablist." + var1.name().toLowerCase(Locale.ROOT) + ".social-layout.";
      String var3 = "tablist." + var1.name().toLowerCase(Locale.ROOT) + ".";
      boolean var4 = this.tabConfig.getBoolean(var2 + "enabled", false);
      int var5 = Math.max(0, Math.min(20, this.tabConfig.getInt(var2 + "blank-rows", 1)));
      int var6 = Math.max(0, this.tabConfig.getInt(var2 + "ping", this.socialFakePing));
      String var7 = this.tabConfig.getString(var2 + "blank", this.socialBlankFormat);
      String var8 = this.tabConfig
         .getString(
            var3 + "blue-player-color",
            this.tabConfig.getString(var3 + "real-player-colors.blue", this.tabConfig.getString(var2 + "blue-player-color", this.partyBluePlayerColor))
         );
      String var9 = this.tabConfig
         .getString(
            var3 + "red-player-color",
            this.tabConfig.getString(var3 + "real-player-colors.red", this.tabConfig.getString(var2 + "red-player-color", this.partyRedPlayerColor))
         );
      List var10 = this.tabConfig.getStringList(var2 + "lines");
      ArrayList var11 = new ArrayList();
      if (!var10.isEmpty()) {
         var11.addAll(var10);
      } else {
         for (int var12 = 0; var12 < var5; var12++) {
            var11.add(var7);
         }
      }

      this.maxContextSocialRows = Math.max(this.maxContextSocialRows, Math.max(var5, var11.size()));
      this.contextSocialLayouts.put(var1, new TablistManager.ContextSocialLayout(var4, var6, Collections.unmodifiableList(var11), var8, var9));
   }

   public boolean isGlobalEnabled() {
      return this.globalEnabled;
   }

   public boolean isWorldEnabledForGlobal(String var1) {
      return this.globalEnabled && this.globalEnabledWorlds.contains(var1.toLowerCase());
   }

   private FileConfiguration loadTabConfig() {
      File var1 = new File(this.plugin.getDataFolder(), "tab.yml");
      if (!var1.exists()) {
         try {
            Files.createDirectories(var1.getParentFile().toPath());

            try (InputStream var2 = this.plugin.getResource("tab.yml")) {
               if (var2 != null) {
                  Files.copy(var2, var1.toPath());
               }
            }
         } catch (IOException var7) {
            this.plugin.getLogger().severe("Failed to save tab.yml: " + var7.getMessage());
         }
      }

      this.plugin.getLogger().info("[Tablist] Loading config from: " + var1.getAbsolutePath());
      YamlConfiguration var8 = YamlConfiguration.loadConfiguration(var1);
      this.plugin.getLogger().info("[Tablist] Config loaded, enabled: " + var8.getBoolean("tablist.enabled", true));
      return var8;
   }

   public void startUpdater() {
      if (this.enabled) {
         this.registerPlayerInfoFilter();
         if (this.tablistUpdater != null) {
            this.tablistUpdater.cancel();
         }

         this.tablistUpdater = new TablistUpdater(this.plugin, this, this.updateInterval);
         this.tablistUpdater.start(this.updateInterval);
         this.plugin.getLogger().info("[Tablist] Updater started with interval: " + this.updateInterval * 50 + "ms");
      }
   }

   public void stopUpdater() {
      if (this.tablistUpdater != null) {
         this.tablistUpdater.cancel();
         this.tablistUpdater = null;
      }

      if (this.playerInfoFilter != null) {
         try {
            PacketEvents.getAPI().getEventManager().unregisterListener(this.playerInfoFilter);
         } catch (Throwable var2) {
         }

         this.playerInfoFilter = null;
      }
   }

   public void reload() {
      this.stopUpdater();
      this.tabConfig = this.loadTabConfig();
      this.enabled = this.tabConfig.getBoolean("tablist.enabled", true);
      this.updateInterval = this.tabConfig.getInt("tablist.update-interval", 20);
      this.loadSortingConfig();
      this.prefixFormat = this.tabConfig.getString("tablist.player-format.prefix", "");
      this.nameFormat = this.tabConfig.getString("tablist.player-format.name", "%player_name%");
      this.suffixFormat = this.tabConfig.getString("tablist.player-format.suffix", "");
      this.tagPrefixFormat = this.tabConfig.getString("tablist.tag-format.prefix", this.prefixFormat);
      this.tagSuffixFormat = this.tabConfig.getString("tablist.tag-format.suffix", this.suffixFormat);
      this.tagNameColorFormat = this.tabConfig.getString("tablist.tag-format.name-color", "");
      this.tagBelowNameFormat = this.tabConfig.getString("tablist.tag-format.below-name", "");
      this.shadowEnabled = this.tabConfig.getBoolean("tablist.shadow", false);
      this.shadowColor = this.parseShadowColor(this.tabConfig.getString("tablist.shadow-color", "#000000ff"));
      this.loadSocialLayoutConfig();
      this.provider = new TablistProvider(this.plugin, this.tabConfig);
      this.removeAllViewerTeams();
      this.teamNameCache.clear();
      this.playerInfoCache.clear();
      this.invalidatePlayerInfoSendSignatures();
      this.viewerTeamSignatures.clear();
      this.playerTeamSignatureRefs.clear();
      this.displayNameCache.invalidateAll();

      for (TablistPlayerData var2 : this.playerData.values()) {
         var2.invalidateHeaderFooterSignature();
         var2.clearRenderedComponentCache();
      }

      this.clearAllSocialRows();
      if (this.belowNameManager != null) {
         this.belowNameManager.reload();
      }

      if (this.enabled) {
         this.registerPlayerInfoFilter();
         this.startUpdater();

         for (Player var4 : Bukkit.getOnlinePlayers()) {
            this.createTablist(var4);
            this.updatePlayerTabName(var4);
            this.applyTeamSorting(var4);
         }
      }

      this.plugin.getLogger().info("[Tablist] Reloaded configuration");
   }

   private void removeAllViewerTeams() {
      for (Entry var2 : this.viewerTeams.entrySet()) {
         Player var3 = Bukkit.getPlayer((UUID)var2.getKey());
         if (var3 != null && var3.isOnline()) {
            for (String var5 : new HashSet((Collection)var2.getValue())) {
               this.sendTeamRemovePacket(var3, var5);
            }
         }
      }

      this.viewerTeams.clear();
   }

   public void createTablist(Player var1) {
      if (this.enabled) {
         this.runOnMainThread(() -> {
            if (var1.isOnline()) {
               this.playerData.computeIfAbsent(var1.getUniqueId(), TablistPlayerData::new);
               this.hiddenPlayers.computeIfAbsent(var1.getUniqueId(), var0 -> ConcurrentHashMap.newKeySet());
               this.updateTablist(var1, true);
            }
         });
      }
   }

   public void createTablistAndApply(Player var1, String var2) {
      if (this.enabled) {
         this.runOnMainThread(() -> {
            if (var1.isOnline()) {
               TablistPlayerData var3 = this.playerData.computeIfAbsent(var1.getUniqueId(), TablistPlayerData::new);
               this.hiddenPlayers.computeIfAbsent(var1.getUniqueId(), var0 -> ConcurrentHashMap.newKeySet());
               if (var2 != null) {
                  var3.setCachedWorldName(var2);
                  if (this.isWorldEnabledForGlobal(var2) && this.canTransitionContext(var3.getContext(), TablistContext.GLOBAL)) {
                     this.applyContext(var3, TablistContext.GLOBAL);
                  }
               }

               this.updateTablist(var1, true);
               if (this.luckPermsListener != null && this.luckPermsListener.isEnabled()) {
                  this.luckPermsListener.cachePlayerRank(var1.getUniqueId());
               }

               this.placeholderCache.invalidateEventPlaceholders(var1.getUniqueId());
               this.updatePlayerTabName(var1);
               this.applyTeamSorting(var1);

               for (Player var5 : Bukkit.getOnlinePlayers()) {
                  if (!var5.equals(var1)) {
                     this.updatePlayerTabName(var5);
                     this.applyTeamSorting(var5);
                  }
               }
            }
         });
      }
   }

   public void ensureTablistData(Player var1, String var2) {
      if (this.enabled && var1 != null && var1.isOnline()) {
         this.runOnMainThread(() -> {
            if (var1.isOnline()) {
               UUID var3 = var1.getUniqueId();
               TablistPlayerData var4 = this.playerData.computeIfAbsent(var3, TablistPlayerData::new);
               this.hiddenPlayers.computeIfAbsent(var3, var0 -> ConcurrentHashMap.newKeySet());
               if (var2 != null) {
                  var4.setCachedWorldName(var2);
                  if (this.isWorldEnabledForGlobal(var2) && this.canTransitionContext(var4.getContext(), TablistContext.GLOBAL)) {
                     this.applyContext(var4, TablistContext.GLOBAL);
                  }
               }

               this.updateTablist(var1, true);
               if (this.luckPermsListener != null && this.luckPermsListener.isEnabled()) {
                  this.luckPermsListener.cachePlayerRank(var3);
               }

               this.placeholderCache.invalidateEventPlaceholders(var3);
               this.updatePlayerTabName(var1, true);
               this.applyTeamSorting(var1);
            }
         });
      }
   }

   public void updateTablist(Player var1, boolean var2) {
      if (this.enabled) {
         TablistPlayerData var3 = this.playerData.get(var1.getUniqueId());
         if (var3 != null) {
            long var4 = System.currentTimeMillis();
            if (var4 - this.lastTabPapiCleanup >= 5000L) {
               this.lastTabPapiCleanup = var4;
               var3.clearExpiredPapi();
            }

            String var6 = this.provider.getHeaderString(var1, var3);
            String var7 = this.provider.getFooterString(var1, var3);
            int var8 = Bukkit.getCurrentTick();
            int var9 = Objects.hash(var3.getContext(), var6, var7);
            this.headerFooterUpdateAttempts.increment();
            if (var3.wasHeaderFooterProcessed(var8, var9)) {
               var3.setDirty(false);
            } else {
               String var10 = var3.getPreviousHeader();
               String var11 = var3.getPreviousFooter();
               boolean var12 = !var6.equals(var10);
               boolean var13 = !var7.equals(var11);
               Component var14;
               if (!var12 && var3.getPreviousHeaderComponent() != null) {
                  var14 = var3.getPreviousHeaderComponent();
               } else {
                  var14 = this.resolveRenderedComponent(var3, var6);
               }

               Component var15;
               if (!var13 && var3.getPreviousFooterComponent() != null) {
                  var15 = var3.getPreviousFooterComponent();
               } else {
                  var15 = this.resolveRenderedComponent(var3, var7);
               }

               if (!TablistUpdatePolicy.renderedOutputChanged(var3.getPreviousHeaderComponent(), var3.getPreviousFooterComponent(), var14, var15)) {
                  var3.setPreviousHeader(var6);
                  var3.setPreviousFooter(var7);
                  var3.setPreviousHeaderComponent(var14);
                  var3.setPreviousFooterComponent(var15);
                  var3.setDirty(false);
                  var3.recordHeaderFooterProcessed(var8, var9);
                  this.scheduleNextHeaderFooterUpdate(var3, var4);
               } else if (var1.isOnline()) {
                  PacketEvents.getAPI().getPlayerManager().sendPacket(var1, new WrapperPlayServerPlayerListHeaderAndFooter(var14, var15));
                  this.headerFooterPacketsSent.increment();
                  var3.setPreviousHeader(var6);
                  var3.setPreviousFooter(var7);
                  var3.setPreviousHeaderComponent(var14);
                  var3.setPreviousFooterComponent(var15);
                  var3.setLastUpdate(var4);
                  var3.setDirty(false);
                  var3.recordHeaderFooterProcessed(var8, var9);
                  this.scheduleNextHeaderFooterUpdate(var3, var4);
               }
            }
         }
      }
   }

   private Component resolveRenderedComponent(TablistPlayerData var1, String var2) {
      Component var3 = var1.getCachedRenderedComponent(var2);
      if (var3 != null) {
         return var3;
      } else {
         Component var4 = this.provider.stringToComponent(var2);
         var1.cacheRenderedComponent(var2, var4);
         return var4;
      }
   }

   private void scheduleNextHeaderFooterUpdate(TablistPlayerData var1, long var2) {
      var1.setNextUpdateAt(TablistUpdatePolicy.nextUpdateAt(var2, var1.getAnimationInterval(), this.updateInterval));
   }

   public void removeTablist(Player var1) {
      UUID var2 = var1.getUniqueId();
      this.refreshCoordinator.remove(var2);
      this.playerData.remove(var2);
      this.rankCache.remove(var2);
      this.teamNameCache.remove(var2);
      this.glowColorOverrides.remove(var2);
      this.playerInfoCache.remove(var2);
      this.userProfileCache.invalidate(var2);
      this.socialFakeIdCache.remove(var2);
      this.duelFakeIdCache.remove(var2);
      this.invalidatePlayerInfoSendSignatures(var2);
      this.viewerTeamSignatures.remove(var2);
      Set var3 = this.playerTeamSignatureRefs.remove(var2);
      if (var3 != null) {
         for (TablistManager.TeamSignatureRef var5 : var3) {
            Map var6 = this.viewerTeamSignatures.get(var5.viewerId());
            if (var6 != null) {
               var6.remove(var5.signatureKey());
            }
         }
      }

      if (this.placeholderCache != null) {
         this.placeholderCache.cleanup(var2);
      }

      if (this.luckPermsListener != null) {
         this.luckPermsListener.invalidate(var2);
      }

      Set var7 = this.hiddenPlayers.remove(var2);
      if (var7 != null) {
         Bukkit.getScheduler().runTask(this.plugin, () -> {
            for (UUID var4 : var7) {
               Player var5x = Bukkit.getPlayer(var4);
               if (var5x != null && var5x.isOnline()) {
                  var1.showPlayer(this.plugin, var5x);
               }
            }
         });
      }

      Bukkit.getScheduler().runTask(this.plugin, () -> {
         if (var1.isOnline()) {
            var1.sendPlayerListHeaderAndFooter(Component.empty(), Component.empty());
         }
      });
   }

   public void showOnly(Player var1, Set<UUID> var2) {
      if (this.enabled && var1 != null) {
         HashSet var3 = new HashSet(var2);
         var3.add(var1.getUniqueId());
         Bukkit.getScheduler().runTask(this.plugin, () -> this.hideOtherPlayers(var1, var3));
      }
   }

   public void setupTeamQueueContext(Player var1, Collection<UUID> var2) {
      if (this.enabled && var1 != null && var2 != null) {
         HashSet var3 = new HashSet(var2);
         var3.add(var1.getUniqueId());
         this.runOnMainThread(() -> {
            TablistPlayerData var3x = this.playerData.get(var1.getUniqueId());
            if (var3x != null && var1.isOnline()) {
               this.applyContext(var3x, TablistContext.TEAM_QUEUE);
               this.removeSocialRows(var1);
               this.hideOtherPlayers(var1, var3);
               this.refreshVisiblePlayerEntries(var1, var3);
               this.updateTablist(var1, true);
            }
         });
      }
   }

   public void showLobbyFriendsView(Player var1) {
      if (this.enabled && var1 != null) {
         if (!this.socialLayoutEnabled) {
            Bukkit.getScheduler().runTask(this.plugin, () -> this.showFriendsOnlyNow(var1));
         } else {
            Bukkit.getScheduler().runTask(this.plugin, () -> this.refreshLobbySocialViewNow(var1, false));
         }
      }
   }

   public void refreshLobbySocialView(Player var1) {
      if (this.enabled && this.socialLayoutEnabled && var1 != null) {
         this.runOnMainThread(() -> this.refreshLobbySocialViewNow(var1, false));
      }
   }

   public void invalidateLobbySocialView(UUID var1) {
      if (var1 != null) {
         this.socialLayoutDataCache.invalidate(var1);
         this.socialLayoutCache.remove(var1);
         this.refreshCoordinator.requestSocial(var1);
      }
   }

   public boolean isLobbySocialLayoutEnabled() {
      return this.socialLayoutEnabled;
   }

   private void cleanupLobbySocialViewForContextSwitch(Player var1, TablistContext var2) {
      if (this.socialLayoutEnabled && var1 != null && var2 != TablistContext.LOBBY_FRIENDS) {
         UUID var3 = var1.getUniqueId();
         if (this.socialFakeRows.containsKey(var3) || this.socialLayoutCache.containsKey(var3)) {
            Bukkit.getScheduler().runTask(this.plugin, () -> {
               this.removeSocialRows(var1);
               this.showAllPlayers(var1);
            });
         }
      }
   }

   public void showAll(Player var1) {
      if (this.enabled && var1 != null) {
         Bukkit.getScheduler().runTask(this.plugin, () -> {
            this.removeSocialRows(var1);
            this.showAllPlayers(var1);
         });
      }
   }

   public TablistContext getContext(Player var1) {
      if (var1 == null) {
         return null;
      } else {
         TablistPlayerData var2 = this.playerData.get(var1.getUniqueId());
         return var2 != null ? var2.getContext() : null;
      }
   }

   public boolean hasContextSection(TablistContext var1) {
      String var2 = "tablist." + var1.name().toLowerCase();
      return this.tabConfig.isConfigurationSection(var2) || this.tabConfig.isList(var2) && !this.tabConfig.getStringList(var2).isEmpty();
   }

   public void setContext(Player var1, TablistContext var2) {
      if (this.enabled && var1 != null && var2 != null) {
         TablistPlayerData var3 = this.playerData.get(var1.getUniqueId());
         if (var3 != null) {
            if (var3.getContext() != var2 && this.canTransitionContext(var3.getContext(), var2)) {
               this.runOnMainThread(() -> {
                  TablistPlayerData var3x = this.playerData.get(var1.getUniqueId());
                  if (var3x != null && this.canTransitionContext(var3x.getContext(), var2)) {
                     this.cleanupLobbySocialViewForContextSwitch(var1, var2);
                     this.applyContext(var3x, var2);
                     this.updateTablist(var1, true);
                  }
               });
            }
         }
      }
   }

   public void setLobbySocialView(Player var1, boolean var2) {
      if (this.enabled && var1 != null && var1.isOnline()) {
         this.runOnMainThread(() -> {
            TablistPlayerData var3 = this.playerData.get(var1.getUniqueId());
            if (var3 != null) {
               TablistContext var4 = var3.getContext();
               if (var4 == TablistContext.DEFAULT || var4 == TablistContext.GLOBAL || var4 == TablistContext.LOBBY_FRIENDS) {
                  if (var2) {
                     this.applyContext(var3, TablistContext.LOBBY_FRIENDS);
                     this.refreshLobbySocialViewNow(var1, true);
                  } else {
                     this.removeSocialRows(var1);
                     TablistContext var5 = this.isWorldEnabledForGlobal(var1.getWorld().getName()) ? TablistContext.GLOBAL : TablistContext.DEFAULT;
                     this.applyContext(var3, var5);
                     this.showAll(var1);
                     this.updateTablist(var1, true);
                  }
               }
            }
         });
      }
   }

   private void applyContext(TablistPlayerData var1, TablistContext var2) {
      var1.setContext(var2);
      var1.clearPapiCache();
      var1.setAnimationInterval(this.provider.getAnimationInterval(var2));
      this.invalidatePlayerInfoSendSignatures();
   }

   private boolean canTransitionContext(TablistContext var1, TablistContext var2) {
      return TablistContextPolicy.canTransition(var1, var2);
   }

   private void runOnMainThread(Runnable var1) {
      if (Bukkit.isPrimaryThread()) {
         var1.run();
      } else {
         Bukkit.getScheduler().runTask(this.plugin, var1);
      }
   }

   public void setPlaceholder(Player var1, String var2, String var3) {
      TablistPlayerData var4 = this.playerData.get(var1.getUniqueId());
      if (var4 != null) {
         var4.setPlaceholder(var2, var3);
         var4.clearPapiCache();
         if (this.isCountdownPlaceholder(var2)) {
            var4.setNextUpdateAt(System.currentTimeMillis() + 1000L);
         }
      }
   }

   private boolean isCountdownPlaceholder(String var1) {
      return var1.contains("countdown")
         || var1.contains("timer")
         || var1.contains("time")
         || var1.contains("duration")
         || var1.contains("seconds")
         || var1.contains("wait");
   }

   public void setupDuelContext(Player var1, Player var2, g var3) {
      this.setupDuelContext(var1, var2, var3, TablistContext.DUEL);
   }

   public void refreshForFocusMode(Player var1) {
      if (this.enabled && var1 != null && var1.isOnline()) {
         UUID var2 = var1.getUniqueId();
         this.invalidatePlayerInfoSendSignatures(var2);
         g var3 = this.plugin.getDuelManager() != null ? this.plugin.getDuelManager().getMatch(var1.getUniqueId()) : null;
         if (var3 == null) {
            this.playerInfoCache.clear();
            this.updatePlayerTabName(var1, true);
            this.applyTeamSorting(var1);

            for (Player var5 : Bukkit.getOnlinePlayers()) {
               if (!var5.equals(var1)) {
                  this.sendPlayerInfoPacket(var5, var1, true);
               }
            }
         } else {
            Player var4 = Bukkit.getPlayer(
               var3.Ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000void(
                  var1.getUniqueId()
               )
            );
            if (var4 != null && var4.isOnline()) {
               this.sendPlayerInfoPacket(var1, var4, true);
               this.sendFocusedOpponentTeam(var1, var4);
            }
         }
      }
   }

   public void refreshMatchFocus(Player var1, Player var2) {
      this.refreshForFocusMode(var1);
      this.refreshForFocusMode(var2);
   }

   public void restoreFocusPair(Player var1, Player var2) {
      if (this.enabled) {
         if (var1 != null) {
            this.playerInfoCache.remove(var1.getUniqueId());
         }

         if (var2 != null) {
            this.playerInfoCache.remove(var2.getUniqueId());
         }

         this.restoreNormalView(var1, var2);
         this.restoreNormalView(var2, var1);
      }
   }

   private void restoreNormalView(Player var1, Player var2) {
      if (var1 != null && var2 != null && var1.isOnline() && var2.isOnline()) {
         this.sendPlayerInfoPacket(var1, var2, true);
         if (this.teamNameCache.get(var2.getUniqueId()) == null) {
            this.applyTeamSorting(var2);
         }

         this.sendFocusedOpponentTeam(var1, var2);
      }
   }

   public void setupDuelContext(Player var1, Player var2, g var3, TablistContext var4) {
      if (this.enabled) {
         Set var5 = Set.of(var1.getUniqueId(), var2.getUniqueId());
         this.runOnMainThread(
            () -> {
               TablistPlayerData var6 = this.playerData.get(var1.getUniqueId());
               TablistPlayerData var7 = this.playerData.get(var2.getUniqueId());
               if (var6 != null) {
                  this.applyContext(var6, var4);
                  var6.setPlaceholder(
                     "opponent",
                     this.plugin
                        .getPlayerSettingsManager()
                        .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                           var1, var2
                        )
                  );
                  var6.setPlaceholder(
                     "current_round",
                     String.valueOf(
                        var3.ÔO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000private()
                     )
                  );
                  var6.setPlaceholder(
                     "total_rounds",
                     String.valueOf(
                        var3.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int()
                     )
                  );
                  var6.setPlaceholder(
                     "your_wins",
                     String.valueOf(
                        var3.ÕO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000interface()
                     )
                  );
                  var6.setPlaceholder(
                     "opponent_wins",
                     String.valueOf(
                        var3.ØO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000supersuper()
                     )
                  );
               }

               if (var7 != null) {
                  this.applyContext(var7, var4);
                  var7.setPlaceholder(
                     "opponent",
                     this.plugin
                        .getPlayerSettingsManager()
                        .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                           var2, var1
                        )
                  );
                  var7.setPlaceholder(
                     "current_round",
                     String.valueOf(
                        var3.ÔO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000private()
                     )
                  );
                  var7.setPlaceholder(
                     "total_rounds",
                     String.valueOf(
                        var3.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int()
                     )
                  );
                  var7.setPlaceholder(
                     "your_wins",
                     String.valueOf(
                        var3.ØO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000supersuper()
                     )
                  );
                  var7.setPlaceholder(
                     "opponent_wins",
                     String.valueOf(
                        var3.ÕO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000interface()
                     )
                  );
               }

               if (!var1.canSee(var2)) {
                  var1.showPlayer(this.plugin, var2);
               }

               if (!var2.canSee(var1)) {
                  var2.showPlayer(this.plugin, var1);
               }

               this.removeSocialRows(var1);
               this.removeSocialRows(var2);
               this.hideOtherPlayers(var1, var5);
               this.hideOtherPlayers(var2, var5);
               this.refreshVisiblePlayerEntries(var1, var5);
               this.refreshVisiblePlayerEntries(var2, var5);
               this.applyDuelSocialLayout(var1, var1, var2);
               this.applyDuelSocialLayout(var2, var1, var2);
               this.refreshMatchFocus(var1, var2);
               this.updateTablist(var1, true);
               this.updateTablist(var2, true);
               this.refreshContextVisibleEntries(var5, var5);
            }
         );
      }
   }

   public void setupBotDuelContext(Player var1, UUID var2, Player var3, TablistContext var4) {
      if (this.enabled && var1 != null && var2 != null && var4 != null) {
         Set var5 = Set.of(var1.getUniqueId(), var2);
         this.runOnMainThread(() -> {
            if (var1.isOnline()) {
               this.removeSocialRows(var1);
               this.hideOtherPlayers(var1, var5);
               TablistPlayerData var5x = this.playerData.get(var1.getUniqueId());
               if (var5x != null) {
                  this.applyContext(var5x, var4);
               }

               if (var3 != null && var3.isValid()) {
                  this.showBotDuelOpponent(var1, var3);
               }

               this.updateTablist(var1, true);
            }
         });
      }
   }

   public void refreshBotDuelOpponent(Player var1, Player var2) {
      if (this.enabled && var1 != null && var2 != null && var1.isOnline() && var2.isValid()) {
         this.runOnMainThread(() -> {
            Set var3 = this.visiblePlayersMap.get(var1.getUniqueId());
            if (var3 != null && var3.contains(var2.getUniqueId())) {
               this.showBotDuelOpponent(var1, var2);
            }
         });
      }
   }

   private void showBotDuelOpponent(Player var1, Player var2) {
      if (!var1.canSee(var2)) {
         var1.showPlayer(this.plugin, var2);
      }

      this.sendPlayerInfoPacket(var1, var2, true);
   }

   public void setupPartyFFAContext(List<UUID> var1, d var2) {
      this.setupPartyFFAContext(var1, var2, TablistContext.PARTY_FFA);
   }

   public void setupPartyFFAContext(List<UUID> var1, d var2, TablistContext var3) {
      if (this.enabled) {
         HashSet var4 = new HashSet(var1);
         this.runOnMainThread(
            () -> {
               for (UUID var6 : var1) {
                  Player var7 = Bukkit.getPlayer(var6);
                  if (var7 != null) {
                     TablistPlayerData var8 = this.playerData.get(var6);
                     if (var8 != null) {
                        this.applyContext(var8, var3);
                        var8.setPlaceholder(
                           "fight_kitname",
                           this.plugin
                              .getDataManager()
                              .ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000null(
                                 var2.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new()
                              )
                        );
                        var8.setPlaceholder(
                           "party_ffa_alive",
                           String.valueOf(
                              var2.oÒ00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000supernew()
                           )
                        );
                        var8.setPlaceholder(
                           "party_size",
                           String.valueOf(
                              var2.öO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Stringsuper()
                                 .size()
                           )
                        );
                        Party var9 = this.plugin.getPartyManager().getPlayerParty(var6);
                        if (var9 != null) {
                           var8.setPlaceholder("party_max_size", String.valueOf(var9.getMaxSize()));
                        }
                     }

                     this.removeSocialRows(var7);
                     this.hideOtherPlayers(var7, var4);
                     this.refreshVisiblePlayerEntries(var7, var4);
                     this.applyContextSocialLayout(var7, var3, this.partyFfaLayoutPlaceholders(var7, var2));
                     this.updateTablist(var7, true);
                  }
               }

               this.refreshContextVisibleEntries(var1, var4);
            }
         );
      }
   }

   public void setupPartySplitContext(List<UUID> var1, List<UUID> var2, d var3) {
      this.setupPartySplitContext(var1, var2, var3, TablistContext.PARTY_SPLIT);
   }

   public void setupPartySplitContext(List<UUID> var1, List<UUID> var2, d var3, TablistContext var4) {
      if (this.enabled) {
         HashSet var5 = new HashSet();
         var5.addAll(var1);
         var5.addAll(var2);
         this.runOnMainThread(
            () -> {
               for (UUID var7 : var5) {
                  Player var8 = Bukkit.getPlayer(var7);
                  if (var8 != null) {
                     TablistPlayerData var9 = this.playerData.get(var7);
                     if (var9 != null) {
                        this.applyContext(var9, var4);
                        var9.setPlaceholder(
                           "fight_kitname",
                           this.plugin
                              .getDataManager()
                              .ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000null(
                                 var3.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new()
                              )
                        );
                        var9.setPlaceholder("blue_alive", String.valueOf(this.countAlive(var1, var3)));
                        var9.setPlaceholder("blue_total", String.valueOf(var1.size()));
                        var9.setPlaceholder("red_alive", String.valueOf(this.countAlive(var2, var3)));
                        var9.setPlaceholder("red_total", String.valueOf(var2.size()));
                     }

                     this.removeSocialRows(var8);
                     this.hideOtherPlayers(var8, var5);
                     this.refreshVisiblePlayerEntries(var8, var5);
                     this.applyContextSocialLayout(var8, var4, this.teamLayoutPlaceholders(var8, var1, var2, var3, var4));
                     this.updateTablist(var8, true);
                  }
               }

               this.refreshContextVisibleEntries(var5, var5);
            }
         );
      }
   }

   public void setupPartyVsContext(List<UUID> var1, List<UUID> var2, d var3) {
      this.setupPartyVsContext(var1, var2, var3, TablistContext.PARTY_VS);
   }

   public void setupPartyVsContext(List<UUID> var1, List<UUID> var2, d var3, TablistContext var4) {
      if (this.enabled) {
         HashSet var5 = new HashSet();
         var5.addAll(var1);
         var5.addAll(var2);
         this.runOnMainThread(
            () -> {
               for (UUID var7 : var1) {
                  Player var8 = Bukkit.getPlayer(var7);
                  if (var8 != null) {
                     TablistPlayerData var9 = this.playerData.get(var7);
                     if (var9 != null) {
                        this.applyContext(var9, var4);
                        var9.setPlaceholder(
                           "fight_kitname",
                           this.plugin
                              .getDataManager()
                              .ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000null(
                                 var3.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new()
                              )
                        );
                        var9.setPlaceholder("your_party_alive", String.valueOf(this.countAlive(var1, var3)));
                        var9.setPlaceholder("your_party_total", String.valueOf(var1.size()));
                        var9.setPlaceholder("enemy_party_alive", String.valueOf(this.countAlive(var2, var3)));
                        var9.setPlaceholder("enemy_party_total", String.valueOf(var2.size()));
                     }

                     this.removeSocialRows(var8);
                     this.hideOtherPlayers(var8, var5);
                     this.refreshVisiblePlayerEntries(var8, var5);
                     this.applyContextSocialLayout(var8, var4, this.teamLayoutPlaceholders(var8, var1, var2, var3, var4));
                     this.updateTablist(var8, true);
                  }
               }

               for (UUID var11 : var2) {
                  Player var12 = Bukkit.getPlayer(var11);
                  if (var12 != null) {
                     TablistPlayerData var13 = this.playerData.get(var11);
                     if (var13 != null) {
                        this.applyContext(var13, var4);
                        var13.setPlaceholder(
                           "fight_kitname",
                           this.plugin
                              .getDataManager()
                              .ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000null(
                                 var3.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new()
                              )
                        );
                        var13.setPlaceholder("your_party_alive", String.valueOf(this.countAlive(var2, var3)));
                        var13.setPlaceholder("your_party_total", String.valueOf(var2.size()));
                        var13.setPlaceholder("enemy_party_alive", String.valueOf(this.countAlive(var1, var3)));
                        var13.setPlaceholder("enemy_party_total", String.valueOf(var1.size()));
                     }

                     this.removeSocialRows(var12);
                     this.hideOtherPlayers(var12, var5);
                     this.refreshVisiblePlayerEntries(var12, var5);
                     this.applyContextSocialLayout(var12, var4, this.teamLayoutPlaceholders(var12, var2, var1, var3, var4));
                     this.updateTablist(var12, true);
                  }
               }

               this.refreshContextVisibleEntries(var5, var5);
            }
         );
      }
   }

   public void setupFFAContext(Player var1, String var2) {
      if (this.enabled) {
         Set var3 = this.plugin.getFFAManager().getArenaPlayers(var2);

         for (Player var5 : this.plugin
            .getSpectatorManager()
            .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
               var2
            )) {
            var3.add(var5.getUniqueId());
         }

         this.runOnMainThread(() -> {
            TablistPlayerData var4 = this.playerData.get(var1.getUniqueId());
            if (var4 != null) {
               this.applyContext(var4, TablistContext.FFA);
               var4.setPlaceholder("ffa_arena", var2);
            }

            this.removeSocialRows(var1);
            this.hideOtherPlayers(var1, var3);

            for (UUID var6 : this.plugin.getFFAManager().getArenaPlayers(var2)) {
               if (!var6.equals(var1.getUniqueId())) {
                  Player var7 = Bukkit.getPlayer(var6);
                  if (var7 != null && var7.isOnline()) {
                     Set var8 = this.visiblePlayersMap.get(var6);
                     if (var8 != null && !var8.contains(var1.getUniqueId())) {
                        var8.add(var1.getUniqueId());
                        this.sendPlayerInfoPacket(var7, var1, true);
                     }
                  }
               }
            }

            this.updateTablist(var1, true);
         });
      }
   }

   public void refreshFFAContext(String var1, UUID var2) {
      if (this.enabled && var1 != null && !var1.isBlank()) {
         Set var3 = this.plugin.getFFAManager().getArenaPlayers(var1);

         for (Player var5 : this.plugin
            .getSpectatorManager()
            .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
               var1
            )) {
            var3.add(var5.getUniqueId());
         }

         this.runOnMainThread(() -> {
            Player var4 = var2 != null ? Bukkit.getPlayer(var2) : null;

            for (UUID var6 : var3) {
               Player var7 = Bukkit.getPlayer(var6);
               if (var7 != null && var7.isOnline()) {
                  TablistPlayerData var8 = this.playerData.get(var6);
                  if (var8 != null) {
                     this.applyContext(var8, TablistContext.FFA);
                     var8.setPlaceholder("ffa_arena", var1);
                  }

                  this.removeSocialRows(var7);
                  this.hideOtherPlayers(var7, var3);
                  this.refreshVisiblePlayerEntries(var7, var3);
                  if (var4 != null && var4.isOnline()) {
                     this.removePlayerFromVisible(var7, var4);
                  }

                  this.updateTablist(var7, true);
               }
            }
         });
      }
   }

   public void setupSpectatorContext(Player var1, Player var2, String var3) {
      if (this.enabled) {
         g var4 = this.plugin.getDuelManager().getMatch(var2.getUniqueId());
         HashSet var5 = new HashSet();
         var5.add(var1.getUniqueId());
         if (var4 != null) {
            var5.add(
               var4.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float()
            );
            var5.add(
               var4.Øo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000dosuper()
            );
         } else if (this.plugin.getFFAManager().isInFFA(var2.getUniqueId())) {
            Set var6 = this.plugin.getFFAManager().getArenaPlayers(var3);
            var5.addAll(var6);
         } else {
            d var9 = this.plugin.getPartyGameManager().getPlayerActiveGame(var2.getUniqueId());
            if (var9 != null) {
               var5.addAll(
                  var9.öO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Stringsuper()
               );
            }
         }

         for (Player var7 : this.plugin
            .getSpectatorManager()
            .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
               var3
            )) {
            var5.add(var7.getUniqueId());
         }

         this.runOnMainThread(() -> {
            TablistPlayerData var5x = this.playerData.get(var1.getUniqueId());
            if (var5x != null) {
               this.applyContext(var5x, TablistContext.SPECTATING);
               var5x.setPlaceholder("spectating_player", var2.getName());
               var5x.setPlaceholder("spectating_arena", var3);
            }

            this.removeSocialRows(var1);
            this.hideOtherPlayers(var1, var5);
            this.updateTablist(var1, true);
         });

         for (Player var12 : this.plugin
            .getSpectatorManager()
            .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
               var3
            )) {
            if (!var12.getUniqueId().equals(var1.getUniqueId())) {
               Set var8 = this.visiblePlayersMap.get(var12.getUniqueId());
               if (var8 != null && !var8.contains(var1.getUniqueId())) {
                  var8.add(var1.getUniqueId());
                  Bukkit.getScheduler().runTask(this.plugin, () -> this.sendPlayerInfoPacket(var12, var1, true));
               }
            }
         }
      }
   }

   public void setupEventContext(Set<UUID> var1, UUID var2, UUID var3) {
      if (this.enabled) {
         HashSet var4 = new HashSet();
         if (var2 != null) {
            var4.add(var2);
         }

         if (var3 != null) {
            var4.add(var3);
         }

         this.runOnMainThread(() -> {
            for (UUID var6 : var1) {
               Player var7 = Bukkit.getPlayer(var6);
               if (var7 != null && var7.isOnline()) {
                  TablistPlayerData var8 = this.playerData.get(var6);
                  if (var8 != null) {
                     this.applyContext(var8, TablistContext.EVENT);
                  }

                  HashSet var9 = new HashSet(var4);
                  var9.add(var6);
                  this.removeSocialRows(var7);
                  this.hideOtherPlayers(var7, var9);
                  this.applyContextSocialLayout(var7, TablistContext.EVENT, this.eventLayoutPlaceholders(var7, var1, var2, var3, TablistContext.EVENT));
                  this.updateTablist(var7, true);
               }
            }
         });
      }
   }

   public void setupTournamentWaitingContext(Set<UUID> var1) {
      if (this.enabled) {
         HashSet var2 = new HashSet(var1);
         this.runOnMainThread(() -> {
            for (UUID var4 : var1) {
               Player var5 = Bukkit.getPlayer(var4);
               if (var5 != null && var5.isOnline()) {
                  TablistPlayerData var6 = this.playerData.get(var4);
                  if (var6 != null) {
                     this.applyContext(var6, TablistContext.TOURNAMENT);
                  }

                  this.removeSocialRows(var5);
                  this.hideOtherPlayers(var5, var2);
                  this.applyContextSocialLayout(var5, TablistContext.TOURNAMENT, this.tournamentLayoutPlaceholders(var5, var1));
                  this.updateTablist(var5, true);
               }
            }

            this.refreshContextVisibleEntries(var1, var1);
         });
         Bukkit.getScheduler().runTaskLater(this.plugin, () -> this.refreshContextVisibleEntries(var1, var1), 1L);
      }
   }

   public void resetEventContext(Set<UUID> var1) {
      if (this.enabled) {
         for (UUID var3 : var1) {
            Player var4 = Bukkit.getPlayer(var3);
            if (var4 != null && var4.isOnline()) {
               this.resetContext(var4);
            }
         }
      }
   }

   public void resetContext(Player var1) {
      UUID var2 = var1.getUniqueId();
      NamedTextColor var3 = this.glowColorOverrides.remove(var2);
      if (var3 != null && var1.isOnline()) {
         var1.setGlowing(false);
         this.teamNameCache.remove(var2);
         if (this.sortingEnabled) {
            this.applyTeamSorting(var1);
         } else {
            for (Player var5 : Bukkit.getOnlinePlayers()) {
               this.rebuildGlowTeamsForViewer(var5);
            }
         }
      }

      if (this.enabled) {
         Object var6 = this.visiblePlayersMap.get(var2) != null ? new HashSet<>(this.visiblePlayersMap.get(var2)) : Collections.emptySet();
         this.runOnMainThread(() -> {
            TablistPlayerData var3x = this.playerData.get(var1.getUniqueId());
            if (var3x != null) {
               this.applyContext(var3x, TablistContext.DEFAULT);
               var3x.clearPlaceholders();
            }

            this.removeSocialRows(var1);
            this.restoreVisiblePlayersAfterReset(var1, var6);
            this.refreshPlayerEntryForViewers(var1);
            this.updateTablist(var1, true);
         });
      }
   }

   public void addPlayerToVisible(Player var1, Player var2) {
      if (this.enabled && var1 != null && var2 != null) {
         UUID var3 = var1.getUniqueId();
         Set var4 = this.visiblePlayersMap.get(var3);
         if (var4 != null && !var4.contains(var2.getUniqueId())) {
            var4.add(var2.getUniqueId());
            Bukkit.getScheduler().runTask(this.plugin, () -> this.sendPlayerInfoPacket(var1, var2, true));
         }
      }
   }

   public void removePlayerFromVisible(Player var1, Player var2) {
      if (this.enabled && var1 != null && var2 != null) {
         UUID var3 = var1.getUniqueId();
         Set var4 = this.visiblePlayersMap.get(var3);
         if (var4 != null && var4.contains(var2.getUniqueId())) {
            var4.remove(var2.getUniqueId());
            Bukkit.getScheduler().runTask(this.plugin, () -> this.sendPlayerInfoPacket(var1, var2, false));
         }
      }
   }

   private void hideOtherPlayers(Player var1, Set<UUID> var2) {
      UUID var3 = var1.getUniqueId();
      Set var4 = this.visiblePlayersMap.get(var3);
      boolean var5 = var4 == null;
      HashSet var6 = new HashSet();
      HashSet var7 = new HashSet();
      if (var5) {
         for (Player var9 : Bukkit.getOnlinePlayers()) {
            UUID var10 = var9.getUniqueId();
            if (!var10.equals(var3)) {
               if (var2.contains(var10)) {
                  var7.add(var10);
               } else {
                  var6.add(var10);
               }
            }
         }
      } else {
         var6 = new HashSet(var4);
         var6.removeAll(var2);
         var6.remove(var3);
         var7 = new HashSet(var2);
         var7.removeAll(var4);
         var7.remove(var3);
      }

      this.visiblePlayersMap.put(var3, this.concurrentUuidSet(var2));
      Set var12 = this.hiddenPlayers.computeIfAbsent(var3, var0 -> ConcurrentHashMap.newKeySet());

      for (UUID var15 : var7) {
         var12.remove(var15);
         Player var11 = Bukkit.getPlayer(var15);
         if (var11 != null && var11.isOnline()) {
            this.sendPlayerInfoPacket(var1, var11, true);
            if (this.shouldHideFocusedOpponent(var1, var11)) {
               this.sendFocusedOpponentTeam(var1, var11);
            }
         }
      }

      for (UUID var16 : var6) {
         var12.add(var16);
         Player var17 = Bukkit.getPlayer(var16);
         if (var17 != null && var17.isOnline()) {
            this.sendPlayerInfoPacket(var1, var17, false);
         }
      }
   }

   private void refreshVisiblePlayerEntries(Player var1, Collection<UUID> var2) {
      if (var1 != null && var2 != null) {
         ArrayList var3 = new ArrayList(var2.size() + 1);
         var3.add(var1);

         for (UUID var5 : var2) {
            Player var6 = Bukkit.getPlayer(var5);
            if (var6 != null && var6.isOnline()) {
               var3.add(var6);
            }
         }

         this.sendPlayerInfoPacketBatch(var1, var3);
      }
   }

   private void refreshContextVisibleEntries(Collection<UUID> var1, Collection<UUID> var2) {
      if (var1 != null && var2 != null) {
         for (UUID var4 : var2) {
            this.playerInfoCache.remove(var4);
         }

         Bukkit.getScheduler().runTask(this.plugin, () -> {
            for (UUID var4x : var1) {
               Player var5 = Bukkit.getPlayer(var4x);
               if (var5 != null && var5.isOnline()) {
                  this.refreshVisiblePlayerEntries(var5, var2);
               }
            }
         });
      }
   }

   private void refreshPlayerEntryForViewers(Player var1) {
      if (var1 != null && var1.isOnline()) {
         for (Player var3 : Bukkit.getOnlinePlayers()) {
            if (!this.isHiddenFromViewer(var3, var1.getUniqueId())) {
               this.sendPlayerInfoPacket(var3, var1, true);
            }
         }
      }
   }

   private boolean isHiddenFromViewer(Player var1, UUID var2) {
      if (var1 != null && var2 != null) {
         Set var3 = this.hiddenPlayers.get(var1.getUniqueId());
         return var3 != null && var3.contains(var2);
      } else {
         return true;
      }
   }

   private void sendPlayerInfoPacket(Player var1, Player var2, boolean var3) {
      try {
         UUID var4 = var2.getUniqueId();
         boolean var5 = var3 && this.isPlayerEntryAllowed(var1, var4);
         if (!this.shouldSendPlayerInfo(var1.getUniqueId(), var4, var5)) {
            return;
         }

         if (var3) {
            if (!var5) {
               WrapperPlayServerPlayerInfoRemove var9 = new WrapperPlayServerPlayerInfoRemove(new UUID[]{var4});
               PacketEvents.getAPI().getPlayerManager().sendPacket(var1, var9);
               return;
            }

            PlayerInfo var6 = this.buildPlayerInfo(var1, var2, var4);
            WrapperPlayServerPlayerInfoUpdate var7 = new WrapperPlayServerPlayerInfoUpdate(
               EnumSet.of(Action.ADD_PLAYER, Action.UPDATE_DISPLAY_NAME, Action.UPDATE_LISTED), new PlayerInfo[]{var6}
            );
            PacketEvents.getAPI().getPlayerManager().sendPacket(var1, var7);
         } else {
            WrapperPlayServerPlayerInfoRemove var10 = new WrapperPlayServerPlayerInfoRemove(new UUID[]{var2.getUniqueId()});
            PacketEvents.getAPI().getPlayerManager().sendPacket(var1, var10);
         }
      } catch (Exception var8) {
         this.plugin.getLogger().warning("[Tablist] Failed to send packet: " + var8.getMessage());
      }
   }

   private synchronized boolean shouldSendPlayerInfo(UUID var1, UUID var2, boolean var3) {
      int var4 = Bukkit.getCurrentTick();
      if (var4 != this.playerInfoSignatureTick) {
         this.playerInfoSendsThisTick.clear();
         this.playerInfoSignatureTick = var4;
      }

      Boolean var5 = this.playerInfoSendsThisTick.put(new TablistManager.PlayerInfoSendKey(var1, var2), var3);
      return var5 == null || var5 != var3;
   }

   private synchronized void invalidatePlayerInfoSendSignatures() {
      this.playerInfoSendsThisTick.clear();
      this.playerInfoSignatureTick = Integer.MIN_VALUE;
   }

   private synchronized void invalidatePlayerInfoSendSignatures(UUID var1) {
      this.playerInfoSendsThisTick.keySet().removeIf(var1x -> var1x.viewerId().equals(var1) || var1x.targetId().equals(var1));
   }

   private void sendPlayerInfoPacketBatch(Player var1, Collection<Player> var2) {
      if (var1 != null && var2 != null && !var2.isEmpty()) {
         try {
            ArrayList var3 = new ArrayList(var2.size());
            HashSet var4 = new HashSet();

            for (Player var6 : var2) {
               if (var6 != null
                  && var6.isOnline()
                  && var4.add(var6.getUniqueId())
                  && this.isPlayerEntryAllowed(var1, var6.getUniqueId())
                  && this.shouldSendPlayerInfo(var1.getUniqueId(), var6.getUniqueId(), true)) {
                  var3.add(this.buildPlayerInfo(var1, var6, var6.getUniqueId()));
               }
            }

            if (var3.isEmpty()) {
               return;
            }

            WrapperPlayServerPlayerInfoUpdate var8 = new WrapperPlayServerPlayerInfoUpdate(
               EnumSet.of(Action.ADD_PLAYER, Action.UPDATE_DISPLAY_NAME, Action.UPDATE_LISTED), var3
            );
            PacketEvents.getAPI().getPlayerManager().sendPacket(var1, var8);
         } catch (Exception var7) {
            this.plugin.getLogger().warning("[Tablist] Failed to send batch packet: " + var7.getMessage());
         }
      }
   }

   boolean isPlayerEntryAllowed(Player var1, UUID var2) {
      if (var1 != null && var2 != null) {
         if (this.isSyntheticTabEntry(var1.getUniqueId(), var2)) {
            return true;
         } else {
            UUID var3 = this.plugin.getBotDuelManager() != null ? this.plugin.getBotDuelManager().getBotEntityUuid(var1.getUniqueId()) : null;
            if (var2.equals(var3)) {
               return true;
            } else {
               Set var4 = this.visiblePlayersMap.get(var1.getUniqueId());
               if (var4 != null) {
                  return var4.contains(var2);
               } else if (var1.getUniqueId().equals(var2)) {
                  return true;
               } else {
                  TablistPlayerData var5 = this.playerData.get(var1.getUniqueId());
                  return var5 == null || !this.isRestrictedMatchContext(var5.getContext());
               }
            }
         }
      } else {
         return false;
      }
   }

   private boolean isSyntheticTabEntry(UUID var1, UUID var2) {
      Map var3 = this.socialFakeIdCache.get(var1);
      if (var3 != null && var3.containsValue(var2)) {
         return true;
      } else {
         Map var4 = this.duelFakeIdCache.get(var1);
         return var4 != null && var4.containsValue(var2);
      }
   }

   boolean shouldFilterPlayerInfo(Player var1) {
      TablistPlayerData var2 = var1 != null ? this.playerData.get(var1.getUniqueId()) : null;
      return var2 != null && this.isRestrictedMatchContext(var2.getContext());
   }

   private PlayerInfo buildPlayerInfo(Player var1, Player var2, UUID var3) {
      if (this.shouldHideFocusedOpponent(var1, var2)) {
         return new PlayerInfo(this.createUserProfile(var2), true, var2.getPing(), this.convertGameMode(var2.getGameMode()), Component.text("???"), null);
      } else {
         String var4 = this.realEntryNameColor(var1, var2);
         TablistManager.CachedPlayerInfo var5 = var4 == null ? this.playerInfoCache.get(var3) : null;
         if (var5 != null && System.currentTimeMillis() - var5.timestamp < 5000L) {
            return var5.info;
         } else {
            UserProfile var6 = this.createUserProfile(var2);
            String var7 = this.parsePlaceholders(var2, this.nameFormat);
            if (var4 != null && !var4.isBlank()) {
               var7 = var4 + this.stripLegacyColors(var7);
            }

            Component var8 = this.buildTabDisplayName(
               var2, this.parsePlaceholders(var2, this.prefixFormat), var7, this.parsePlaceholders(var2, this.suffixFormat)
            );
            PlayerInfo var9 = new PlayerInfo(var6, true, var2.getPing(), this.convertGameMode(var2.getGameMode()), var8, null);
            if (var4 == null) {
               this.playerInfoCache.put(var3, new TablistManager.CachedPlayerInfo(var6, var9, System.currentTimeMillis()));
            }

            return var9;
         }
      }
   }

   private boolean shouldHideFocusedOpponent(Player var1, Player var2) {
      if (var1 != null
         && var2 != null
         && !var1.equals(var2)
         && this.plugin
            .getPlayerSettingsManager()
            .Ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000void(
               var1
            )
         && this.plugin.getDuelManager() != null) {
         g var3 = this.plugin.getDuelManager().getMatch(var1.getUniqueId());
         return var3 != null
            && var2.getUniqueId()
               .equals(
                  var3.Ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000void(
                     var1.getUniqueId()
                  )
               );
      } else {
         return false;
      }
   }

   private String realEntryNameColor(Player var1, Player var2) {
      if (var1 != null && var2 != null) {
         UUID var3 = var2.getUniqueId();
         TablistContext var4 = this.getContext(var1);
         if (this.plugin.getDuelManager() != null) {
            g var5 = this.plugin.getDuelManager().getMatch(var1.getUniqueId());
            if (var5 != null
               && !var5.ÖO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000thissuper()
               )
             {
               if (var3.equals(
                  var5.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float()
               )) {
                  return this.duelBluePlayerColor;
               }

               if (var3.equals(
                  var5.Øo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000dosuper()
               )) {
                  return this.duelRedPlayerColor;
               }
            }
         }

         if (this.plugin.getPartyGameManager() != null) {
            d var6 = this.plugin.getPartyGameManager().getPlayerActiveGame(var1.getUniqueId());
            if (var6 == null) {
               return null;
            }

            if (var6.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int()
                  != null
               && var6.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int()
                  .contains(var3)) {
               return this.contextBluePlayerColor(var4);
            }

            if (var6.ÒÒ00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000newnew()
                  != null
               && var6.ÒÒ00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000newnew()
                  .contains(var3)) {
               return this.contextRedPlayerColor(var4);
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private UserProfile createUserProfile(Player var1) {
      return this.userProfileCache.get(var1.getUniqueId(), var2 -> this.buildUserProfile(var1));
   }

   private UserProfile buildUserProfile(Player var1) {
      ArrayList var2 = new ArrayList();

      for (ProfileProperty var4 : var1.getPlayerProfile().getProperties()) {
         if ("textures".equals(var4.getName())) {
            var2.add(new TextureProperty(var4.getName(), var4.getValue(), var4.getSignature()));
         }
      }

      return new UserProfile(var1.getUniqueId(), var1.getName(), var2);
   }

   private GameMode convertGameMode(org.bukkit.GameMode var1) {
      return switch (var1) {
         case SURVIVAL -> GameMode.SURVIVAL;
         case CREATIVE -> GameMode.CREATIVE;
         case ADVENTURE -> GameMode.ADVENTURE;
         case SPECTATOR -> GameMode.SPECTATOR;
         default -> throw new MatchException(null, null);
      };
   }

   private void showAllPlayers(Player var1) {
      UUID var2 = var1.getUniqueId();
      Set var3 = this.hiddenPlayers.get(var2);
      this.visiblePlayersMap.remove(var2);
      if (var3 != null && !var3.isEmpty()) {
         ArrayList var4 = new ArrayList(var3);
         var3.clear();
         ArrayList var5 = new ArrayList(var4.size());

         for (UUID var7 : var4) {
            Player var8 = Bukkit.getPlayer(var7);
            if (var8 != null && var8.isOnline()) {
               var5.add(var8);
            }
         }

         this.sendPlayerInfoPacketBatch(var1, var5);
      }
   }

   private void restoreVisiblePlayersAfterReset(Player var1, Collection<UUID> var2) {
      UUID var3 = var1.getUniqueId();
      LinkedHashSet var4 = new LinkedHashSet();
      Set var5 = this.hiddenPlayers.get(var3);
      this.visiblePlayersMap.remove(var3);
      if (var5 != null && !var5.isEmpty()) {
         var4.addAll(var5);
         var5.clear();
      }

      if (var2 != null && !var2.isEmpty()) {
         var4.addAll(var2);
      }

      if (!var4.isEmpty()) {
         ArrayList var6 = new ArrayList(var4.size());

         for (UUID var8 : var4) {
            Player var9 = Bukkit.getPlayer(var8);
            if (var9 != null && var9.isOnline()) {
               var6.add(var9);
            }
         }

         this.sendPlayerInfoPacketBatch(var1, var6);
      }
   }

   private void hideSelfFromSocialTab(Player var1) {
      this.hiddenPlayers.computeIfAbsent(var1.getUniqueId(), var0 -> ConcurrentHashMap.newKeySet()).add(var1.getUniqueId());
      this.sendPlayerInfoPacket(var1, var1, false);
   }

   private void restoreSelfInTab(Player var1) {
      Set var2 = this.hiddenPlayers.get(var1.getUniqueId());
      if (var2 != null && var2.remove(var1.getUniqueId())) {
         this.sendPlayerInfoPacket(var1, var1, true);
      }
   }

   private void showFriendsOnlyNow(Player var1) {
      if (var1 != null && var1.isOnline()) {
         this.removeSocialRows(var1);
         this.restoreSelfInTab(var1);
         Set var2 = this.plugin.getFriendManager() != null
            ? this.plugin
               .getFriendManager()
               .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                  var1.getUniqueId()
               )
            : Collections.emptySet();
         HashSet var3 = new HashSet(var2);
         var3.add(var1.getUniqueId());
         this.hideOtherPlayers(var1, var3);
      }
   }

   private void refreshLobbySocialViewNow(Player var1, boolean var2) {
      if (var1 != null && var1.isOnline()) {
         TablistPlayerData var3 = this.playerData.get(var1.getUniqueId());
         if (var3 != null && var3.getContext() == TablistContext.LOBBY_FRIENDS) {
            if (!this.shouldUseSocialLayout(var1)) {
               this.showFriendsOnlyNow(var1);
            } else {
               if (var2 || !this.socialFakeRows.containsKey(var1.getUniqueId())) {
                  this.hideOtherPlayers(var1, Collections.emptySet());
                  this.hideSelfFromSocialTab(var1);
               }

               if (var2) {
                  this.socialLayoutDataCache.invalidate(var1.getUniqueId());
               }

               TablistManager.SocialLayout var4 = this.socialLayoutDataCache.get(var1.getUniqueId(), var2x -> this.buildSocialLayout(var1));
               if (var2 || !this.socialSortTeams.containsKey(var1.getUniqueId()) || !var4.cacheKey().equals(this.socialLayoutCache.get(var1.getUniqueId()))) {
                  this.removeSocialRows(var1);
                  this.hideOtherPlayers(var1, var4.visiblePlayers());
                  if (var4.visiblePlayers().contains(var1.getUniqueId())) {
                     this.restoreSelfInTab(var1);
                  } else {
                     this.hideSelfFromSocialTab(var1);
                  }

                  ArrayList var5 = new ArrayList(var4.rows().size());
                  ArrayList var6 = new ArrayList(var4.rows().size());

                  for (int var7 = 0; var7 < var4.rows().size(); var7++) {
                     TablistManager.SocialRow var8 = var4.rows().get(var7);
                     if (!var8.isPlayer()) {
                        UUID var9 = this.getSocialFakeId(var1.getUniqueId(), var7);
                        var5.add(var9);
                        var6.add(this.buildLayoutRowInfo(var9, this.getSocialFakeName(var7), var8.text(), this.socialFakePing));
                     }
                  }

                  this.sendLayoutRows(var1, var6);
                  this.applySocialSortTeams(var1, var4);
                  this.socialFakeRows.put(var1.getUniqueId(), var5);
                  this.socialLayoutCache.put(var1.getUniqueId(), var4.cacheKey());
               }
            }
         }
      }
   }

   private String buildSocialLayoutCacheKey(List<TablistManager.SocialRow> var1, Set<UUID> var2) {
      ArrayList var3 = new ArrayList(var2.size());

      for (UUID var5 : var2) {
         var3.add(var5.toString());
      }

      var3.sort(String::compareTo);
      StringBuilder var7 = new StringBuilder(var1.size() * 24 + var3.size() * 36);

      for (TablistManager.SocialRow var6 : var1) {
         if (!var7.isEmpty()) {
            var7.append('\u0001');
         }

         var7.append(var6.isPlayer() ? "p:" : "f:");
         var7.append(var6.isPlayer() ? var6.playerId() : var6.text());
      }

      var7.append('\u0002');

      for (String var10 : var3) {
         var7.append(var10).append('\u0001');
      }

      return var7.toString();
   }

   private boolean shouldUseSocialLayout(Player var1) {
      return this.socialLayoutEnabled;
   }

   private TablistManager.SocialLayout buildSocialLayout(Player var1) {
      ArrayList var2 = new ArrayList(this.socialColumnRows);
      HashSet var3 = new HashSet();
      Party var4 = this.plugin.getPartyManager() != null ? this.plugin.getPartyManager().getPlayerParty(var1.getUniqueId()) : null;
      List var5 = this.getOnlinePartyMembers(var4);
      boolean var6 = var4 != null;
      HashSet var7 = new HashSet();
      if (var6) {
         for (Player var9 : var5) {
            var7.add(var9.getUniqueId());
         }
      }

      List var15 = this.getOnlineFriends(var1);
      if (var6 && !var7.isEmpty()) {
         var15.removeIf(var1x -> var7.contains(var1x.getUniqueId()));
      }

      int var16 = this.plugin.getFriendManager() != null
         ? this.plugin
            .getFriendManager()
            .õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int(
               var1.getUniqueId()
            )
         : 0;
      var2.add(
         TablistManager.SocialRow.fake(
            this.renderTabConfigString(var1, this.applySocialPlaceholders(this.socialFriendsHeaderFormat, null, var15.size(), var16, 0))
         )
      );
      this.addHeaderGap(var1, var2);

      for (Player var11 : var15) {
         if (var3.size() >= this.socialMaxFriends) {
            break;
         }

         var3.add(var11.getUniqueId());
         var2.add(TablistManager.SocialRow.player(var11.getUniqueId()));
      }

      if (var15.isEmpty() && !this.isBlankSocialLine(this.socialEmptyFriendsFormat)) {
         var2.add(TablistManager.SocialRow.fake(this.renderTabConfigString(var1, this.socialEmptyFriendsFormat)));
      }

      ArrayList var17 = new ArrayList(var6 ? this.socialPartyStartRow + this.socialColumnRows : this.socialColumnRows);
      var17.addAll(var2);
      if (var6) {
         this.fillRowsTo(var1, var17, this.socialPartyStartRow);
         ArrayList var18 = new ArrayList(this.socialColumnRows);
         var18.add(
            TablistManager.SocialRow.fake(this.renderTabConfigString(var1, this.applySocialPlaceholders(this.socialPartyHeaderFormat, null, 0, 0, var5.size())))
         );
         this.addHeaderGap(var1, var18);
         if (var5.isEmpty() && !this.isBlankSocialLine(this.socialEmptyPartyFormat)) {
            var18.add(TablistManager.SocialRow.fake(this.renderTabConfigString(var1, this.socialEmptyPartyFormat)));
         }

         int var12 = 0;

         for (Player var14 : var5) {
            if (var12 >= this.socialMaxParty) {
               break;
            }

            var3.add(var14.getUniqueId());
            var18.add(TablistManager.SocialRow.player(var14.getUniqueId()));
            var12++;
         }

         this.fillColumn(var1, var18);
         var17.addAll(var18);
      }

      return new TablistManager.SocialLayout(var17, var3, this.buildSocialLayoutCacheKey(var17, var3));
   }

   private void addHeaderGap(Player var1, List<TablistManager.SocialRow> var2) {
      if (!this.isBlankSocialLine(this.socialBlankFormat)) {
         for (int var3 = 0; var3 < this.socialHeaderGap && var2.size() < this.socialColumnRows; var3++) {
            var2.add(TablistManager.SocialRow.fake(this.renderTabConfigString(var1, this.socialBlankFormat)));
         }
      }
   }

   private boolean isBlankSocialLine(String var1) {
      return var1 == null || var1.trim().isEmpty();
   }

   private void fillColumn(Player var1, List<TablistManager.SocialRow> var2) {
      while (var2.size() < this.socialColumnRows) {
         var2.add(TablistManager.SocialRow.fake(this.renderTabConfigString(var1, this.socialBlankFormat)));
      }

      if (var2.size() > this.socialColumnRows) {
         var2.subList(this.socialColumnRows, var2.size()).clear();
      }
   }

   private List<Player> getOnlineFriends(Player var1) {
      if (this.plugin.getFriendManager() == null) {
         return Collections.emptyList();
      } else {
         ArrayList var2 = new ArrayList();

         for (UUID var4 : this.plugin
            .getFriendManager()
            .Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class(
               var1.getUniqueId()
            )) {
            Player var5 = Bukkit.getPlayer(var4);
            if (var5 != null && var5.isOnline() && !var5.getUniqueId().equals(var1.getUniqueId())) {
               var2.add(var5);
            }
         }

         var2.sort(Comparator.comparing(Player::getName, String.CASE_INSENSITIVE_ORDER));
         return var2;
      }
   }

   private List<Player> getOnlinePartyMembers(Party var1) {
      if (var1 == null) {
         return Collections.emptyList();
      } else {
         ArrayList var2 = new ArrayList();

         for (UUID var4 : var1.getMembers()) {
            Player var5 = Bukkit.getPlayer(var4);
            if (var5 != null && var5.isOnline()) {
               var2.add(var5);
            }
         }

         UUID var6 = var1.getOwner();
         var2.sort(
            Comparator.<Player, Boolean>comparing(var1x -> !var1x.getUniqueId().equals(var6)).thenComparing(Player::getName, String.CASE_INSENSITIVE_ORDER)
         );
         return var2;
      }
   }

   private String applySocialPlaceholders(String var1, Player var2, int var3, int var4, int var5) {
      String var6 = var2 != null ? var2.getName() : "";
      int var7 = var2 != null ? var2.getPing() : this.socialFakePing;
      return var1.replace("%player%", var6)
         .replace("%online%", String.valueOf(var3))
         .replace("%total%", String.valueOf(var4))
         .replace("%count%", String.valueOf(var5))
         .replace("%ping%", String.valueOf(var7));
   }

   private void fillRowsTo(Player var1, List<TablistManager.SocialRow> var2, int var3) {
      while (var2.size() < var3) {
         var2.add(TablistManager.SocialRow.fake(this.renderTabConfigString(var1, this.socialBlankFormat)));
      }
   }

   private void applySocialSortTeams(Player var1, TablistManager.SocialLayout var2) {
      if (var1 != null && var1.isOnline()) {
         Set var3 = this.viewerTeams.computeIfAbsent(var1.getUniqueId(), var0 -> ConcurrentHashMap.newKeySet());
         ArrayList var4 = new ArrayList(var2.rows().size());

         for (int var5 = 0; var5 < var2.rows().size(); var5++) {
            TablistManager.SocialRow var6 = var2.rows().get(var5);
            Object var8 = Component.empty();
            Object var9 = Component.empty();
            NamedTextColor var10 = NamedTextColor.WHITE;
            String var7;
            if (var6.isPlayer()) {
               Player var11 = Bukkit.getPlayer(var6.playerId());
               if (var11 == null || !var11.isOnline()) {
                  continue;
               }

               var7 = var11.getName();
               TablistManager.PlayerTeamData var12 = this.teamNameCache.get(var11.getUniqueId());
               if (var12 != null) {
                  if (var3.remove(var12.teamName)) {
                     this.sendTeamRemovePacket(var1, var12.teamName);
                  }

                  var8 = this.parseTagDisplayNameForViewer(var1, var12.tagPrefix);
                  var9 = this.parseTagDisplayNameForViewer(var1, var12.tagSuffix);
                  var10 = this.resolveTeamColor(var11.getUniqueId(), var12);
               }
            } else {
               var7 = this.getSocialFakeName(var5);
            }

            String var13 = this.getSocialSortTeamName(var5);
            if (var3.remove(var13)) {
               this.sendTeamRemovePacket(var1, var13);
            }

            this.sendTeamCreatePacket(var1, var13, (Component)var8, (Component)var9, Collections.singletonList(var7), var10);
            var3.add(var13);
            var4.add(var13);
         }

         this.socialSortTeams.put(var1.getUniqueId(), var4);
      }
   }

   private void applyDuelSocialLayout(Player var1, Player var2, Player var3) {
      if (this.duelSocialLayoutEnabled && var1 != null && var1.isOnline() && !this.duelSocialRows.isEmpty()) {
         Player var4 = var1.equals(var2) ? var3 : var2;
         ArrayList var5 = new ArrayList(this.duelSocialRows.size());
         ArrayList var6 = new ArrayList(this.duelSocialRows.size());

         for (int var7 = 0; var7 < this.duelSocialRows.size(); var7++) {
            UUID var8 = this.getDuelFakeId(var1.getUniqueId(), var7);
            var5.add(var8);
            String var9 = this.renderTabConfigString(var1, this.applyDuelLayoutPlaceholders(this.duelSocialRows.get(var7), var1, var4, var2, var3));
            var6.add(this.buildLayoutRowInfo(var8, this.getDuelFakeName(var7), var9, this.duelSocialFakePing));
         }

         this.sendLayoutRows(var1, var6);
         this.socialFakeRows.put(var1.getUniqueId(), var5);
      }
   }

   private String applyDuelLayoutPlaceholders(String var1, Player var2, Player var3, Player var4, Player var5) {
      if (var1 == null) {
         return "";
      } else {
         boolean var6 = var4 != null && var2 != null && var2.getUniqueId().equals(var4.getUniqueId());
         String var7 = var6 ? this.duelBluePlayerColor : this.duelRedPlayerColor;
         String var8 = var6 ? this.duelRedPlayerColor : this.duelBluePlayerColor;
         String var9 = var2 != null ? var2.getName() : "";
         String var10 = var3 != null
            ? this.plugin
               .getPlayerSettingsManager()
               .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                  var2, var3
               )
            : "";
         String var11 = var4 != null ? var4.getName() : "";
         String var12 = var5 != null ? var5.getName() : "";
         return var1.replace("%players%", "2")
            .replace("%player%", var9)
            .replace("%opponent%", var10)
            .replace("%player_colored%", this.colorName(var7, var9))
            .replace("%opponent_colored%", this.colorName(var8, var10))
            .replace("%blue_player%", this.colorName(this.duelBluePlayerColor, var11))
            .replace("%red_player%", this.colorName(this.duelRedPlayerColor, var12))
            .replace("%blue_name%", var11)
            .replace("%red_name%", var12);
      }
   }

   private void applyContextSocialLayout(Player var1, TablistContext var2, Map<String, String> var3) {
      TablistManager.ContextSocialLayout var4 = this.contextSocialLayouts != null ? this.contextSocialLayouts.get(var2) : null;
      if (var4 != null && var4.enabled() && var1 != null && var1.isOnline() && !var4.rows().isEmpty()) {
         ArrayList var5 = new ArrayList(var4.rows().size());
         ArrayList var6 = new ArrayList(var4.rows().size());

         for (int var7 = 0; var7 < var4.rows().size(); var7++) {
            UUID var8 = this.getDuelFakeId(var1.getUniqueId(), var7);
            var5.add(var8);
            String var9 = this.renderTabConfigString(var1, this.applyContextLayoutPlaceholders(var4.rows().get(var7), var3));
            var6.add(this.buildLayoutRowInfo(var8, this.getDuelFakeName(var7), var9, var4.ping()));
         }

         this.sendLayoutRows(var1, var6);
         this.socialFakeRows.put(var1.getUniqueId(), var5);
      }
   }

   private String applyContextLayoutPlaceholders(String var1, Map<String, String> var2) {
      if (var1 == null) {
         return "";
      } else {
         String var3 = var1;
         if (var2 != null) {
            for (Entry var5 : var2.entrySet()) {
               var3 = var3.replace("%" + (String)var5.getKey() + "%", (CharSequence)(var5.getValue() != null ? (CharSequence)var5.getValue() : ""));
            }
         }

         return var3;
      }
   }

   private PlayerInfo buildLayoutRowInfo(UUID var1, String var2, String var3, int var4) {
      return new PlayerInfo(new UserProfile(var1, var2), true, var4, GameMode.SURVIVAL, this.parseDisplayName(var3), null);
   }

   private void sendLayoutRows(Player var1, List<PlayerInfo> var2) {
      if (var1 != null && var2 != null && !var2.isEmpty()) {
         try {
            WrapperPlayServerPlayerInfoUpdate var3 = new WrapperPlayServerPlayerInfoUpdate(
               EnumSet.of(Action.ADD_PLAYER, Action.UPDATE_DISPLAY_NAME, Action.UPDATE_LISTED), var2
            );
            PacketEvents.getAPI().getPlayerManager().sendPacket(var1, var3);
         } catch (Exception var4) {
            this.plugin.getLogger().warning("[Tablist] Failed to send layout row: " + var4.getMessage());
         }
      }
   }

   private Map<String, String> partyFfaLayoutPlaceholders(Player var1, d var2) {
      Map var3 = this.baseLayoutPlaceholders(
         var1,
         var2 != null
            ? var2.öO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Stringsuper()
               .size()
            : 0
      );
      if (var2 != null) {
         var3.put(
            "kit",
            this.plugin
               .getDataManager()
               .ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000null(
                  var2.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new()
               )
         );
         var3.put(
            "fight_kitname",
            this.plugin
               .getDataManager()
               .ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000null(
                  var2.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new()
               )
         );
         var3.put(
            "party_ffa_alive",
            String.valueOf(
               var2.oÒ00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000supernew()
            )
         );
         var3.put(
            "party_size",
            String.valueOf(
               var2.öO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Stringsuper()
                  .size()
            )
         );
      }

      return var3;
   }

   private Map<String, String> teamLayoutPlaceholders(Player var1, Collection<UUID> var2, Collection<UUID> var3, d var4, TablistContext var5) {
      String var6 = this.contextBluePlayerColor(var5);
      String var7 = this.contextRedPlayerColor(var5);
      Map var8 = this.baseLayoutPlaceholders(var1, (var2 != null ? var2.size() : 0) + (var3 != null ? var3.size() : 0));
      var8.put("blue_players", this.coloredPlayerList(var2, var6));
      var8.put("red_players", this.coloredPlayerList(var3, var7));
      var8.put("blue_player", this.colorName(var6, this.firstPlayerName(var2)));
      var8.put("red_player", this.colorName(var7, this.firstPlayerName(var3)));
      var8.put("blue_name", this.firstPlayerName(var2));
      var8.put("red_name", this.firstPlayerName(var3));
      if (var4 != null) {
         var8.put(
            "kit",
            this.plugin
               .getDataManager()
               .ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000null(
                  var4.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new()
               )
         );
         var8.put(
            "fight_kitname",
            this.plugin
               .getDataManager()
               .ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000null(
                  var4.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new()
               )
         );
         var8.put("blue_alive", String.valueOf(this.countAlive((List<UUID>)(var2 != null ? new ArrayList<>(var2) : Collections.emptyList()), var4)));
         var8.put("blue_total", String.valueOf(var2 != null ? var2.size() : 0));
         var8.put("red_alive", String.valueOf(this.countAlive((List<UUID>)(var3 != null ? new ArrayList<>(var3) : Collections.emptyList()), var4)));
         var8.put("red_total", String.valueOf(var3 != null ? var3.size() : 0));
      }

      return var8;
   }

   private Map<String, String> eventLayoutPlaceholders(Player var1, Set<UUID> var2, UUID var3, UUID var4, TablistContext var5) {
      Map var6 = this.baseLayoutPlaceholders(var1, var2 != null ? var2.size() : 0);
      var6.put("blue_player", this.colorName(this.contextBluePlayerColor(var5), this.playerName(var3)));
      var6.put("red_player", this.colorName(this.contextRedPlayerColor(var5), this.playerName(var4)));
      var6.put("blue_name", this.playerName(var3));
      var6.put("red_name", this.playerName(var4));
      return var6;
   }

   private String contextBluePlayerColor(TablistContext var1) {
      TablistManager.ContextSocialLayout var2 = this.contextSocialLayouts != null ? this.contextSocialLayouts.get(var1) : null;
      return var2 != null ? var2.bluePlayerColor() : this.partyBluePlayerColor;
   }

   private String contextRedPlayerColor(TablistContext var1) {
      TablistManager.ContextSocialLayout var2 = this.contextSocialLayouts != null ? this.contextSocialLayouts.get(var1) : null;
      return var2 != null ? var2.redPlayerColor() : this.partyRedPlayerColor;
   }

   private Map<String, String> tournamentLayoutPlaceholders(Player var1, Set<UUID> var2) {
      return this.baseLayoutPlaceholders(var1, var2 != null ? var2.size() : 0);
   }

   private Map<String, String> baseLayoutPlaceholders(Player var1, int var2) {
      HashMap var3 = new HashMap();
      var3.put("players", String.valueOf(var2));
      var3.put("player", var1 != null ? var1.getName() : "");
      return var3;
   }

   private String coloredPlayerList(Collection<UUID> var1, String var2) {
      if (var1 != null && !var1.isEmpty()) {
         ArrayList var3 = new ArrayList();

         for (UUID var5 : var1) {
            String var6 = this.playerName(var5);
            if (!var6.isEmpty()) {
               var3.add(this.colorName(var2, var6));
            }
         }

         return String.join("&#7F7F7F, ", var3);
      } else {
         return "";
      }
   }

   private String firstPlayerName(Collection<UUID> var1) {
      if (var1 == null) {
         return "";
      } else {
         for (UUID var3 : var1) {
            String var4 = this.playerName(var3);
            if (!var4.isEmpty()) {
               return var4;
            }
         }

         return "";
      }
   }

   private String playerName(UUID var1) {
      Player var2 = var1 != null ? Bukkit.getPlayer(var1) : null;
      return var2 != null ? var2.getName() : "";
   }

   private String colorName(String var1, String var2) {
      if (var2 != null && !var2.isEmpty()) {
         return var1 != null && !var1.isBlank() ? var1 + var2 : var2;
      } else {
         return "";
      }
   }

   private String stripLegacyColors(String var1) {
      if (var1 != null && !var1.isEmpty()) {
         String var2 = var1.replaceAll("(?i)&#[0-9a-f]{6}", "").replaceAll("(?i)§x(§[0-9a-f]){6}", "");
         return var2.replaceAll("(?i)[&§][0-9a-fk-or]", "");
      } else {
         return "";
      }
   }

   private void removeSocialRows(Player var1) {
      UUID var2 = var1.getUniqueId();
      this.removeSocialSortTeams(var1);
      List var3 = this.socialFakeRows.remove(var2);
      this.socialLayoutCache.remove(var2);
      if (var3 != null && !var3.isEmpty()) {
         for (UUID var5 : var3) {
            try {
               PacketEvents.getAPI().getPlayerManager().sendPacket(var1, new WrapperPlayServerPlayerInfoRemove(new UUID[]{var5}));
            } catch (Exception var7) {
               this.plugin.getLogger().warning("[Tablist] Failed to remove social row: " + var7.getMessage());
            }
         }
      }
   }

   private void clearAllSocialRows() {
      for (Player var2 : Bukkit.getOnlinePlayers()) {
         this.removeSocialRows(var2);
      }

      this.socialFakeRows.clear();
      this.socialSortTeams.clear();
      this.socialLayoutCache.clear();
      this.socialFakeIdCache.clear();
      this.duelFakeIdCache.clear();
      this.socialLayoutDataCache.invalidateAll();
   }

   private void removeSocialSortTeams(Player var1) {
      if (var1 != null && var1.isOnline()) {
         List var2 = this.socialSortTeams.remove(var1.getUniqueId());
         Set var3 = this.viewerTeams.computeIfAbsent(var1.getUniqueId(), var0 -> ConcurrentHashMap.newKeySet());
         if (var2 != null) {
            for (String var5 : var2) {
               if (var3.remove(var5)) {
                  this.sendTeamRemovePacket(var1, var5);
               }
            }
         }

         if (this.sortingEnabled) {
            for (UUID var6 : this.visiblePlayersMap.getOrDefault(var1.getUniqueId(), Collections.emptySet())) {
               Player var7 = Bukkit.getPlayer(var6);
               TablistManager.PlayerTeamData var8 = this.teamNameCache.get(var6);
               if (var7 != null && var7.isOnline() && var8 != null) {
                  Component var9 = this.parseTagDisplayNameForViewer(var1, var8.tagPrefix);
                  Component var10 = this.parseTagDisplayNameForViewer(var1, var8.tagSuffix);
                  NamedTextColor var11 = this.resolveTeamColor(var6, var8);
                  this.ensureViewerTeam(var1, var7, var8, var9, var10, var11, this.focusNameTagVisibility(var1, var7));
               }
            }
         }
      }
   }

   private UUID getSocialFakeId(UUID var1, int var2) {
      return this.socialFakeIdCache
         .computeIfAbsent(var1, var0 -> new ConcurrentHashMap<>())
         .computeIfAbsent(var2, var3 -> this.buildFakeRowId("social", var1, var2));
   }

   private String getSocialFakeName(int var1) {
      return "scs" + this.twoDigits(var1);
   }

   private String getSocialSortTeamName(int var1) {
      return "00000sc" + this.threeDigits(var1);
   }

   private UUID getDuelFakeId(UUID var1, int var2) {
      return this.duelFakeIdCache
         .computeIfAbsent(var1, var0 -> new ConcurrentHashMap<>())
         .computeIfAbsent(var2, var3 -> this.buildFakeRowId("duel", var1, var2));
   }

   private UUID buildFakeRowId(String var1, UUID var2, int var3) {
      return UUID.nameUUIDFromBytes(("swiftcore-tab-" + var1 + ":" + var2 + ":" + var3).getBytes(StandardCharsets.UTF_8));
   }

   private String getDuelFakeName(int var1) {
      return "00scd" + this.twoDigits(var1);
   }

   private String twoDigits(int var1) {
      return var1 < 10 ? "0" + var1 : String.valueOf(var1);
   }

   private String threeDigits(int var1) {
      if (var1 < 10) {
         return "00" + var1;
      } else {
         return var1 < 100 ? "0" + var1 : String.valueOf(var1);
      }
   }

   public void onPlayerJoin(Player var1) {
      if (this.enabled) {
         if (Bukkit.isPrimaryThread()) {
            this.handleJoiningPlayerForRestrictedTabs(var1);
         } else {
            Bukkit.getScheduler().runTask(this.plugin, () -> this.handleJoiningPlayerForRestrictedTabs(var1));
         }

         Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
            if (var1.isOnline()) {
               try {
                  if (this.luckPermsListener != null && this.luckPermsListener.isEnabled()) {
                     this.luckPermsListener.cachePlayerRank(var1.getUniqueId());
                  }

                  this.placeholderCache.invalidateEventPlaceholders(var1.getUniqueId());
                  this.updatePlayerTabName(var1);
                  if (this.sortingEnabled) {
                     this.syncAllTeamsToPlayer(var1);
                     this.applyTeamSorting(var1);
                  }
               } catch (Exception var3) {
                  this.plugin.getLogger().warning("[Tablist] Failed to handle player join: " + var3.getMessage());
               }
            }
         }, 15L);
         Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
            if (var1.isOnline()) {
               try {
                  if (this.luckPermsListener != null && this.luckPermsListener.isEnabled()) {
                     this.luckPermsListener.cachePlayerRank(var1.getUniqueId());
                  }

                  this.placeholderCache.invalidateEventPlaceholders(var1.getUniqueId());
                  this.rankCache.remove(var1.getUniqueId());
                  this.updatePlayerTabName(var1);
                  if (this.sortingEnabled) {
                     this.applyTeamSorting(var1);
                  }
               } catch (Exception var3) {
                  this.plugin.getLogger().warning("[Tablist] Failed delayed tab name update: " + var3.getMessage());
               }
            }
         }, 60L);
      }
   }

   private void handleJoiningPlayerForRestrictedTabs(Player var1) {
      if (var1 != null && var1.isOnline()) {
         ArrayList var2 = new ArrayList();
         UUID var3 = var1.getUniqueId();
         this.restrictJoiningPlayerFromActiveViewers(var1);

         for (Entry var5 : this.visiblePlayersMap.entrySet()) {
            UUID var6 = (UUID)var5.getKey();
            Set var7 = (Set)var5.getValue();
            if (!var7.contains(var3)) {
               Player var8 = Bukkit.getPlayer(var6);
               if (var8 != null && var8.isOnline()) {
                  Set var9 = this.hiddenPlayers.computeIfAbsent(var6, var0 -> ConcurrentHashMap.newKeySet());
                  if (var9.add(var3)) {
                     this.sendPlayerInfoPacket(var8, var1, false);
                  }

                  TablistPlayerData var10 = this.playerData.get(var6);
                  if (this.socialLayoutEnabled
                     && var10 != null
                     && var10.getContext() == TablistContext.LOBBY_FRIENDS
                     && this.isSociallyRelevantJoin(var8, var3)) {
                     var2.add(var8);
                  }
               }
            }
         }

         if (!var2.isEmpty()) {
            for (Player var12 : var2) {
               this.socialLayoutDataCache.invalidate(var12.getUniqueId());
               this.refreshLobbySocialViewNow(var12, true);
            }
         }
      }
   }

   private boolean isSociallyRelevantJoin(Player var1, UUID var2) {
      if (this.plugin.getFriendManager() != null
         && this.plugin
            .getFriendManager()
            .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
               var1.getUniqueId()
            )
            .contains(var2)) {
         return true;
      } else {
         Party var3 = this.plugin.getPartyManager() != null ? this.plugin.getPartyManager().getPlayerParty(var1.getUniqueId()) : null;
         return var3 != null && var3.isMember(var2);
      }
   }

   private void restrictJoiningPlayerFromActiveViewers(Player var1) {
      UUID var2 = var1.getUniqueId();

      for (Entry var4 : this.playerData.entrySet()) {
         UUID var5 = (UUID)var4.getKey();
         if (!var5.equals(var2) && this.isRestrictedMatchContext(((TablistPlayerData)var4.getValue()).getContext())) {
            Set var6 = this.resolveActiveVisiblePlayers(var5, ((TablistPlayerData)var4.getValue()).getContext());
            if (var6 != null && !var6.isEmpty() && !var6.contains(var2)) {
               Player var7 = Bukkit.getPlayer(var5);
               if (var7 != null && var7.isOnline()) {
                  var6.add(var5);
                  this.visiblePlayersMap.put(var5, this.concurrentUuidSet(var6));
                  Set var8 = this.hiddenPlayers.computeIfAbsent(var5, var0 -> ConcurrentHashMap.newKeySet());
                  var8.add(var2);
                  this.sendPlayerInfoPacket(var7, var1, false);
               }
            }
         }
      }
   }

   private boolean isRestrictedMatchContext(TablistContext var1) {
      return this.isDuelContext(var1)
         || var1 == TablistContext.FFA
         || this.isPartyContext(var1)
         || var1 == TablistContext.SPECTATING
         || var1 == TablistContext.TOURNAMENT
         || var1 == TablistContext.EVENT
         || var1 == TablistContext.LOBBY_FRIENDS
         || var1 == TablistContext.TEAM_QUEUE;
   }

   private Set<UUID> concurrentUuidSet(Collection<UUID> var1) {
      KeySetView var2 = ConcurrentHashMap.newKeySet();
      var2.addAll(var1);
      return var2;
   }

   private boolean isDuelContext(TablistContext var1) {
      return var1 == TablistContext.DUEL
         || var1 == TablistContext.DUEL_BLOCK_DECAY
         || var1 == TablistContext.DUEL_FLOWER_CROWN
         || var1 == TablistContext.DUEL_TNT_TAG
         || var1 == TablistContext.DUEL_BEDWARS;
   }

   private boolean isPartyContext(TablistContext var1) {
      return var1 == TablistContext.PARTY_FFA
         || var1 == TablistContext.PARTY_SPLIT
         || var1 == TablistContext.PARTY_VS
         || var1 == TablistContext.PARTY_BLOCK_DECAY
         || var1 == TablistContext.PARTY_FLOWER_CROWN
         || var1 == TablistContext.PARTY_TNT_TAG
         || var1 == TablistContext.PARTY_BEDWARS;
   }

   private Set<UUID> resolveActiveVisiblePlayers(UUID var1, TablistContext var2) {
      if (this.isDuelContext(var2)) {
         g var13 = this.plugin.getDuelManager() != null ? this.plugin.getDuelManager().getMatch(var1) : null;
         if (var13 != null
            && !var13.ÖO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000thissuper()
            )
          {
            HashSet var16 = new HashSet();
            var16.add(
               var13.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float()
            );
            var16.add(
               var13.Øo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000dosuper()
            );
            return var16;
         } else {
            UUID var15 = this.plugin.getBotDuelManager() != null ? this.plugin.getBotDuelManager().getBotEntityUuid(var1) : null;
            return var15 != null ? Set.of(var1, var15) : null;
         }
      } else if (var2 == TablistContext.FFA) {
         String var12 = Optional.ofNullable(this.playerData.get(var1)).map(var0 -> var0.getPlaceholder("ffa_arena")).orElse("");
         if (var12 != null && !var12.isBlank()) {
            HashSet var14 = new HashSet<>(this.plugin.getFFAManager().getArenaPlayers(var12));

            for (Player var19 : this.plugin
               .getSpectatorManager()
               .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                  var12
               )) {
               var14.add(var19.getUniqueId());
            }

            return var14;
         } else {
            return null;
         }
      } else if (this.isPartyContext(var2)) {
         d var11 = this.plugin.getPartyGameManager().getPlayerActiveGame(var1);
         return var11 != null
            ? new HashSet<>(
               var11.öO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Stringsuper()
            )
            : null;
      } else if (var2 == TablistContext.SPECTATING) {
         c var10 = this.plugin
            .getSpectatorManager()
            .Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
               var1
            );
         if (var10 == null) {
            return null;
         } else {
            HashSet var4 = new HashSet();
            var4.add(var1);
            UUID var5 = var10.Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String();
            if (var5 != null) {
               g var6 = this.plugin.getDuelManager().getMatch(var5);
               if (var6 != null) {
                  var4.add(
                     var6.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float()
                  );
                  var4.add(
                     var6.Øo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000dosuper()
                  );
               } else if (this.plugin.getFFAManager().isInFFA(var5)) {
                  var4.addAll(
                     this.plugin
                        .getFFAManager()
                        .getArenaPlayers(
                           var10.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object()
                        )
                  );
               } else {
                  d var7 = this.plugin.getPartyGameManager().getPlayerActiveGame(var5);
                  if (var7 != null) {
                     var4.addAll(
                        var7.öO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Stringsuper()
                     );
                  } else {
                     var4.add(var5);
                  }
               }
            }

            for (Player var20 : this.plugin
               .getSpectatorManager()
               .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                  var10.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object()
               )) {
               var4.add(var20.getUniqueId());
            }

            return var4;
         }
      } else if (var2 == TablistContext.TOURNAMENT && this.plugin.getTournamentManager() != null) {
         org.lime.swiftCore.eb.g var9 = this.plugin
            .getTournamentManager()
            .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super();
         return var9 != null
               && this.plugin
                  .getTournamentManager()
                  .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
                     var1
                  )
            ? this.plugin
               .getTournamentManager()
               .Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String()
            : null;
      } else if (var2 == TablistContext.EVENT && this.plugin.getEventManager() != null) {
         org.lime.swiftCore.hb.d var8 = this.plugin
            .getEventManager()
            .Ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000void();
         return var8 != null
               && var8.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new()
                  .contains(var1)
            ? new HashSet<>(
               var8.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new()
            )
            : null;
      } else if (var2 == TablistContext.TEAM_QUEUE) {
         Set var3 = this.visiblePlayersMap.get(var1);
         return (Set<UUID>)(var3 != null ? new HashSet<>(var3) : Collections.singleton(var1));
      } else {
         return null;
      }
   }

   public void refreshLobbySocialViews() {
      if (this.enabled && this.socialLayoutEnabled) {
         this.socialLayoutDataCache.invalidateAll();
         this.socialLayoutCache.clear();

         for (Player var2 : Bukkit.getOnlinePlayers()) {
            TablistPlayerData var3 = this.playerData.get(var2.getUniqueId());
            if (var3 != null && var3.getContext() == TablistContext.LOBBY_FRIENDS) {
               this.refreshLobbySocialView(var2);
            }
         }
      }
   }

   private void syncAllTeamsToPlayer(Player var1) {
      for (Player var3 : Bukkit.getOnlinePlayers()) {
         if (!var3.equals(var1) && this.isPlayerEntryAllowed(var1, var3.getUniqueId())) {
            TablistManager.PlayerTeamData var4 = this.teamNameCache.get(var3.getUniqueId());
            if (var4 != null) {
               Component var5 = this.parseTagDisplayNameForViewer(var1, var4.tagPrefix);
               Component var6 = this.parseTagDisplayNameForViewer(var1, var4.tagSuffix);
               NamedTextColor var7 = this.resolveTeamColor(var3.getUniqueId(), var4);
               this.ensureViewerTeam(var1, var3, var4, var5, var6, var7, this.focusNameTagVisibility(var1, var3));
            }
         }
      }

      if (this.belowNameManager != null) {
         this.belowNameManager.syncViewer(var1);
      }
   }

   public void onPlayerQuit(Player var1) {
      if (this.enabled) {
         UUID var2 = var1.getUniqueId();
         TablistManager.PlayerTeamData var3 = this.teamNameCache.remove(var2);
         if (var3 != null && this.sortingEnabled) {
            for (Entry var5 : this.viewerTeams.entrySet()) {
               UUID var6 = (UUID)var5.getKey();
               if (!var6.equals(var2)) {
                  Set var7 = (Set)var5.getValue();
                  if (var7.remove(var3.teamName)) {
                     Player var8 = Bukkit.getPlayer(var6);
                     if (var8 != null && var8.isOnline()) {
                        this.sendTeamRemovePacket(var8, var3.teamName);
                     }
                  }
               }
            }
         }

         this.viewerTeams.remove(var2);
         this.visiblePlayersMap.remove(var2);
         this.hiddenPlayers.remove(var2);
         this.socialFakeRows.remove(var2);
         this.socialSortTeams.remove(var2);
         this.socialLayoutCache.remove(var2);
         this.playerInfoCache.remove(var2);
         this.rankCache.remove(var2);

         for (Set var11 : this.hiddenPlayers.values()) {
            var11.remove(var2);
         }

         for (Set var12 : this.visiblePlayersMap.values()) {
            var12.remove(var2);
         }

         this.refreshLobbySocialViews();
         if (this.tablistUpdater != null) {
            this.tablistUpdater.cleanup(var2);
         }

         if (this.belowNameManager != null) {
            this.belowNameManager.clearViewer(var1);
            this.belowNameManager.clearTarget(var1);
         }
      }
   }

   private int countAlive(List<UUID> var1, d var2) {
      int var3 = 0;

      for (UUID var5 : var1) {
         if (!var2.Oo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000returnsuper()
            .contains(var5)) {
            var3++;
         }
      }

      return var3;
   }

   public TablistPlayerData getData(Player var1) {
      return this.playerData.get(var1.getUniqueId());
   }

   public TablistPlayerData getData(UUID var1) {
      return this.playerData.get(var1);
   }

   public Set<UUID> getTrackedPlayerIds() {
      return Set.copyOf(this.playerData.keySet());
   }

   Set<UUID> getTrackedPlayerIdsView() {
      return this.playerData.keySet();
   }

   TablistRefreshCoordinator.Snapshot drainRefreshRequests() {
      return this.refreshCoordinator.drain();
   }

   public void requestHeaderFooterRefresh(UUID var1) {
      this.refreshCoordinator.requestHeaderFooter(var1);
   }

   public void requestIdentityRefresh(UUID var1) {
      this.refreshCoordinator.requestIdentity(var1);
   }

   public void requestSocialRefresh(UUID var1) {
      this.refreshCoordinator.requestSocial(var1);
   }

   public long getHeaderFooterUpdateAttempts() {
      return this.headerFooterUpdateAttempts.sum();
   }

   public long getHeaderFooterPacketsSent() {
      return this.headerFooterPacketsSent.sum();
   }

   public boolean isEnabled() {
      return this.enabled;
   }

   boolean isBelowNameEnabled() {
      return this.tagBelowNameFormat != null && !this.tagBelowNameFormat.isBlank();
   }

   Component buildBelowNameComponent(Player var1) {
      if (this.isBelowNameEnabled() && var1 != null) {
         String var2 = this.parsePlaceholders(var1, this.tagBelowNameFormat);
         return var2 != null && !var2.isBlank() ? this.parseTagDisplayName(var2) : null;
      } else {
         return null;
      }
   }

   public void refreshBelowName(Player var1) {
      if (this.belowNameManager != null) {
         this.belowNameManager.syncViewer(var1);
      }
   }

   public void clearBelowName(Player var1) {
      if (this.belowNameManager != null) {
         this.belowNameManager.clearViewer(var1);
      }
   }

   public void clearBelowNameForTarget(Player var1) {
      if (this.belowNameManager != null) {
         this.belowNameManager.clearTarget(var1);
      }
   }

   public TablistProvider getProvider() {
      return this.provider;
   }

   public LuckPermsListener getLuckPermsListener() {
      return this.luckPermsListener;
   }

   public PlaceholderCache getPlaceholderCache() {
      return this.placeholderCache;
   }

   public int getUpdateInterval() {
      return this.updateInterval;
   }

   public void updatePlayerTabName(Player var1) {
      this.updatePlayerTabName(var1, false);
   }

   private void updatePlayerTabName(Player var1, boolean var2) {
      if (this.enabled) {
         UUID var3 = var1.getUniqueId();
         TablistManager.PlayerRankCache var4 = this.rankCache.get(var3);
         String var5 = this.parsePlaceholders(var1, this.prefixFormat);
         if (this.plugin.getClanManager() != null
            && this.plugin
               .getClanManager()
               .oO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000do()
            )
          {
            var5 = this.plugin
                  .getClanManager()
                  .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
                     var1.getUniqueId()
                  )
               + var5;
         }

         String var6 = this.parsePlaceholders(var1, this.nameFormat);
         String var7 = this.parsePlaceholders(var1, this.suffixFormat);
         int var8 = this.getPlayerPriorityInternal(var1);
         if (var2 || var4 == null || !var4.prefix().equals(var5) || !var4.name().equals(var6) || !var4.suffix().equals(var7) || var4.priority() != var8) {
            this.rankCache.put(var3, new TablistManager.PlayerRankCache(var8, var5, var6, var7, System.currentTimeMillis()));
            this.playerInfoCache.remove(var3);
            Component var9 = this.buildTabDisplayName(var1, var5, var6, var7);
            Bukkit.getScheduler().runTask(this.plugin, () -> {
               if (var1.isOnline()) {
                  var1.playerListName(var9);
                  this.refreshContextualPlayerEntry(var1);
               }
            });
         }
      }
   }

   private void refreshContextualPlayerEntry(Player var1) {
      for (Player var3 : Bukkit.getOnlinePlayers()) {
         TablistPlayerData var4 = this.playerData.get(var3.getUniqueId());
         if (var4 != null && this.isRestrictedMatchContext(var4.getContext()) && this.isPlayerEntryAllowed(var3, var1.getUniqueId())) {
            this.sendPlayerInfoPacket(var3, var1, true);
         }
      }
   }

   private Component buildTabDisplayName(Player var1, String var2, String var3, String var4) {
      Component var5 = this.parseDisplayName(var2);
      Component var6 = this.parseDisplayName(var3);
      Component var7 = this.parseDisplayName(var4);
      return ((TextComponent)((TextComponent)Component.empty().append(var5)).append(var6)).append(var7);
   }

   public void applyTeamSorting(Player var1) {
      if (this.enabled && this.sortingEnabled) {
         UUID var2 = var1.getUniqueId();
         int var3 = this.getPlayerPriorityInternal(var1);
         String var4 = this.parsePlaceholders(var1, this.prefixFormat);
         String var5 = this.parsePlaceholders(var1, this.suffixFormat);
         String var6 = this.parsePlaceholders(var1, this.tagPrefixFormat);
         NamedTextColor var7 = this.parseNameColor(this.parsePlaceholders(var1, this.tagNameColorFormat));
         String var8 = this.parsePlaceholders(var1, this.tagSuffixFormat);
         String var9 = var1.getName().length() > 10 ? var1.getName().substring(0, 10) : var1.getName();
         String var10 = this.formatPriorityKey(var3) + "_" + var9.toLowerCase(Locale.ROOT);
         TablistManager.PlayerTeamData var11 = this.teamNameCache.get(var2);
         String var12 = var11 != null ? var11.teamName : null;
         boolean var13 = var11 == null
            || !var11.teamName.equals(var10)
            || !var11.prefix.equals(var4)
            || !var11.suffix.equals(var5)
            || !var11.tagPrefix.equals(var6)
            || !var11.tagSuffix.equals(var8)
            || !Objects.equals(var11.nameColor, var7);
         if (var13) {
            boolean var14 = var12 != null && !var12.equals(var10);
            TablistManager.PlayerTeamData var15 = new TablistManager.PlayerTeamData(var10, var4, var5, var6, var8, var7);
            this.teamNameCache.put(var2, var15);
            NamedTextColor var16 = this.resolveTeamColor(var2, var15);

            for (Player var18 : Bukkit.getOnlinePlayers()) {
               if (this.isPlayerEntryAllowed(var18, var2)) {
                  UUID var19 = var18.getUniqueId();
                  Set var20 = this.viewerTeams.computeIfAbsent(var19, var0 -> ConcurrentHashMap.newKeySet());
                  Map var21 = this.viewerTeamSignatures.computeIfAbsent(var19, var0 -> new ConcurrentHashMap<>());
                  NameTagVisibility var22 = this.focusNameTagVisibility(var18, var1);
                  if (var14 && var20.remove(var12)) {
                     this.sendTeamRemovePacket(var18, var12);
                     var21.remove(this.teamSignatureKey(var12, var2));
                  }

                  Component var23 = this.parseTagDisplayNameForViewer(var18, var6);
                  Component var24 = this.parseTagDisplayNameForViewer(var18, var8);
                  this.ensureViewerTeam(var18, var1, var15, var23, var24, var16, var22);
               }
            }

            if (this.belowNameManager != null) {
               this.belowNameManager.refreshTarget(var1);
            }
         }
      }
   }

   private String formatPriorityKey(int var1) {
      int var2 = Math.min(Math.max(var1, 0), 9999);
      if (var2 < 10) {
         return "000" + var2;
      } else if (var2 < 100) {
         return "00" + var2;
      } else {
         return var2 < 1000 ? "0" + var2 : String.valueOf(var2);
      }
   }

   private void rememberTeamSignatureRef(UUID var1, UUID var2, String var3) {
      this.playerTeamSignatureRefs.computeIfAbsent(var1, var0 -> ConcurrentHashMap.newKeySet()).add(new TablistManager.TeamSignatureRef(var2, var3));
   }

   private void ensureViewerTeam(
      Player var1, Player var2, TablistManager.PlayerTeamData var3, Component var4, Component var5, NamedTextColor var6, NameTagVisibility var7
   ) {
      UUID var8 = var1.getUniqueId();
      UUID var9 = var2.getUniqueId();
      Set var10 = this.viewerTeams.computeIfAbsent(var8, var0 -> ConcurrentHashMap.newKeySet());
      Map var11 = this.viewerTeamSignatures.computeIfAbsent(var8, var0 -> new ConcurrentHashMap<>());
      String var12 = this.teamSignatureKey(var3.teamName, var9);
      String var13 = this.teamSignature(var3.teamName, var3.tagPrefix, var3.tagSuffix, var6, var7, var2.getName());
      if (var10.contains(var3.teamName)) {
         if (!var13.equals(var11.get(var12))) {
            this.sendTeamUpdatePacket(var1, var3.teamName, var4, var5, var6, var7);
            var11.put(var12, var13);
            this.rememberTeamSignatureRef(var9, var8, var12);
         }
      } else {
         var10.add(var3.teamName);
         var11.put(var12, var13);
         this.rememberTeamSignatureRef(var9, var8, var12);
         this.sendTeamCreatePacket(var1, var3.teamName, var4, var5, Collections.singletonList(var2.getName()), var6, var7);
      }
   }

   private String teamSignatureKey(String var1, UUID var2) {
      return var1 + ":" + var2;
   }

   private String teamSignature(String var1, String var2, String var3, NamedTextColor var4, NameTagVisibility var5, String var6) {
      return var1 + var2 + var3 + var4 + var5 + var6;
   }

   public int getPlayerPriority(Player var1) {
      TablistManager.PlayerRankCache var2 = this.rankCache.get(var1.getUniqueId());
      return var2 != null && System.currentTimeMillis() - var2.timestamp < 5000L ? var2.priority : this.getPlayerPriorityInternal(var1);
   }

   private int getPlayerPriorityInternal(Player var1) {
      if (!this.sortingEnabled) {
         return 99;
      } else {
         long var2 = System.currentTimeMillis();
         Integer var4 = this.sortOrderCache.get(var1.getUniqueId());
         if (var4 != null && var2 < this.sortOrderCacheExpiresAt) {
            return var4;
         } else {
            if (var4 == null) {
               this.invalidateSortOrder();
            }

            this.rebuildSortOrder(var2);
            return this.sortOrderCache.getOrDefault(var1.getUniqueId(), 9999);
         }
      }
   }

   private synchronized void rebuildSortOrder(long var1) {
      if (var1 >= this.sortOrderCacheExpiresAt || this.sortOrderCache.isEmpty()) {
         ArrayList var3 = new ArrayList();
         HashMap var4 = new HashMap();

         for (Player var6 : Bukkit.getOnlinePlayers()) {
            String var7 = var6.getName();
            var4.put(var7, var6.getUniqueId());
            var3.add(
               new TabSortOrder.Entry(
                  var7,
                  this.getRawSortPriority(var6),
                  this.sortingMode == TabSortOrder.Mode.PLACEHOLDER ? this.parsePlaceholders(var6, this.sortingPlaceholder) : ""
               )
            );
         }

         List var8 = TabSortOrder.sorted(var3, this.sortingMode, this.sortingPlaceholderType, this.sortingPlaceholderDirection);
         this.sortOrderCache.clear();

         for (int var9 = 0; var9 < var8.size(); var9++) {
            UUID var10 = (UUID)var4.get(((TabSortOrder.Entry)var8.get(var9)).playerName());
            if (var10 != null) {
               this.sortOrderCache.put(var10, var9);
            }
         }

         this.sortOrderCacheExpiresAt = var1 + 500L;
      }
   }

   private int getRawSortPriority(Player var1) {
      if (this.luckPermsListener != null && this.luckPermsListener.isEnabled()) {
         return this.sortingMode == TabSortOrder.Mode.CONFIGURED_GROUPS
            ? this.getGroupPriority(this.luckPermsListener.getGroup(var1.getUniqueId()))
            : this.luckPermsListener.getPriority(var1.getUniqueId());
      } else {
         return this.sortingMode == TabSortOrder.Mode.CONFIGURED_GROUPS ? this.groupSorting.size() : 9999;
      }
   }

   private void invalidateSortOrder() {
      this.sortOrderCache.clear();
      this.sortOrderCacheExpiresAt = 0L;
   }

   private String parsePlaceholders(Player var1, String var2) {
      return this.renderTabConfigString(var1, var2);
   }

   private void sendTeamCreatePacket(Player var1, String var2, Component var3, Component var4, Collection<String> var5) {
      this.sendTeamCreatePacket(var1, var2, var3, var4, var5, NamedTextColor.WHITE);
   }

   private void sendTeamCreatePacket(Player var1, String var2, Component var3, Component var4, Collection<String> var5, NamedTextColor var6) {
      this.sendTeamCreatePacket(var1, var2, var3, var4, var5, var6, NameTagVisibility.ALWAYS);
   }

   private void sendTeamCreatePacket(
      Player var1, String var2, Component var3, Component var4, Collection<String> var5, NamedTextColor var6, NameTagVisibility var7
   ) {
      try {
         ScoreBoardTeamInfo var8 = new ScoreBoardTeamInfo(Component.empty(), var3, var4, var7, CollisionRule.ALWAYS, var6, OptionData.NONE);
         WrapperPlayServerTeams var9 = new WrapperPlayServerTeams(var2, TeamMode.CREATE, var8, var5);
         PacketEvents.getAPI().getPlayerManager().sendPacket(var1, var9);
      } catch (Exception var10) {
         this.plugin.getLogger().warning("[Tablist] Failed to send team create packet: " + var10.getMessage());
      }
   }

   private void sendTeamUpdatePacket(Player var1, String var2, Component var3, Component var4) {
      this.sendTeamUpdatePacket(var1, var2, var3, var4, NamedTextColor.WHITE);
   }

   private void sendTeamUpdatePacket(Player var1, String var2, Component var3, Component var4, NamedTextColor var5) {
      this.sendTeamUpdatePacket(var1, var2, var3, var4, var5, NameTagVisibility.ALWAYS);
   }

   private void sendTeamUpdatePacket(Player var1, String var2, Component var3, Component var4, NamedTextColor var5, NameTagVisibility var6) {
      try {
         ScoreBoardTeamInfo var7 = new ScoreBoardTeamInfo(Component.empty(), var3, var4, var6, CollisionRule.ALWAYS, var5, OptionData.NONE);
         WrapperPlayServerTeams var8 = new WrapperPlayServerTeams(var2, TeamMode.UPDATE, var7, new String[0]);
         PacketEvents.getAPI().getPlayerManager().sendPacket(var1, var8);
      } catch (Exception var9) {
         this.plugin.getLogger().warning("[Tablist] Failed to send team update packet: " + var9.getMessage());
      }
   }

   private NameTagVisibility focusNameTagVisibility(Player var1, Player var2) {
      if (var1 != null
         && var2 != null
         && !var1.equals(var2)
         && this.plugin
            .getPlayerSettingsManager()
            .Ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000void(
               var1
            )
         && this.plugin.getDuelManager() != null) {
         g var3 = this.plugin.getDuelManager().getMatch(var1.getUniqueId());
         if (var3 != null
            && var2.getUniqueId()
               .equals(
                  var3.Ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000void(
                     var1.getUniqueId()
                  )
               )) {
            return NameTagVisibility.NEVER;
         }
      }

      return NameTagVisibility.ALWAYS;
   }

   private void sendFocusedOpponentTeam(Player var1, Player var2) {
      if (var1 != null && var2 != null && var1.isOnline() && var2.isOnline()) {
         TablistManager.PlayerTeamData var3 = this.teamNameCache.get(var2.getUniqueId());
         if (var3 != null) {
            boolean var4 = this.shouldHideFocusedOpponent(var1, var2);
            Object var5 = var4 ? Component.empty() : this.parseTagDisplayNameForViewer(var1, var3.tagPrefix);
            Object var6 = var4 ? Component.empty() : this.parseTagDisplayNameForViewer(var1, var3.tagSuffix);
            NamedTextColor var7 = var4 ? NamedTextColor.WHITE : this.resolveTeamColor(var2.getUniqueId(), var3);
            NameTagVisibility var8 = var4 ? NameTagVisibility.NEVER : NameTagVisibility.ALWAYS;
            Set var9 = this.viewerTeams.computeIfAbsent(var1.getUniqueId(), var0 -> ConcurrentHashMap.newKeySet());
            Map var10 = this.viewerTeamSignatures.computeIfAbsent(var1.getUniqueId(), var0 -> new ConcurrentHashMap<>());
            String var11 = this.teamSignatureKey(var3.teamName, var2.getUniqueId());
            String var12 = this.teamSignature(var3.teamName, var3.tagPrefix, var3.tagSuffix, var7, var8, var2.getName());
            if (var9.contains(var3.teamName)) {
               if (var12.equals(var10.get(var11))) {
                  return;
               }

               this.sendTeamUpdatePacket(var1, var3.teamName, (Component)var5, (Component)var6, var7, var8);
            } else {
               this.sendTeamCreatePacket(var1, var3.teamName, (Component)var5, (Component)var6, Collections.singletonList(var2.getName()), var7, var8);
               var9.add(var3.teamName);
            }

            var10.put(var11, var12);
         }
      }
   }

   private void sendTeamRemovePacket(Player var1, String var2) {
      try {
         WrapperPlayServerTeams var3 = new WrapperPlayServerTeams(var2, TeamMode.REMOVE, (ScoreBoardTeamInfo)null, new String[0]);
         PacketEvents.getAPI().getPlayerManager().sendPacket(var1, var3);
      } catch (Exception var4) {
         this.plugin.getLogger().warning("[Tablist] Failed to send team remove packet: " + var4.getMessage());
      }
   }

   private void sendTeamRemoveEntitiesPacket(Player var1, String var2, Collection<String> var3) {
      try {
         WrapperPlayServerTeams var4 = new WrapperPlayServerTeams(var2, TeamMode.REMOVE_ENTITIES, (ScoreBoardTeamInfo)null, var3);
         PacketEvents.getAPI().getPlayerManager().sendPacket(var1, var4);
      } catch (Exception var5) {
         this.plugin.getLogger().warning("[Tablist] Failed to send team remove-entities packet: " + var5.getMessage());
      }
   }

   private void sendTeamAddEntitiesPacket(Player var1, String var2, Collection<String> var3) {
      try {
         WrapperPlayServerTeams var4 = new WrapperPlayServerTeams(var2, TeamMode.ADD_ENTITIES, (ScoreBoardTeamInfo)null, var3);
         PacketEvents.getAPI().getPlayerManager().sendPacket(var1, var4);
      } catch (Exception var5) {
         this.plugin.getLogger().warning("[Tablist] Failed to send team add-entities packet: " + var5.getMessage());
      }
   }

   private void rebuildGlowTeamsForViewer(Player var1) {
      if (var1 != null && var1.isOnline()) {
         ArrayList var2 = new ArrayList();
         ArrayList var3 = new ArrayList();

         for (Entry var5 : this.glowColorOverrides.entrySet()) {
            Player var6 = Bukkit.getPlayer((UUID)var5.getKey());
            if (var6 != null && var6.isOnline()) {
               if (var5.getValue() == NamedTextColor.BLUE) {
                  var2.add(var6.getName());
               } else if (var5.getValue() == NamedTextColor.RED) {
                  var3.add(var6.getName());
               }
            }
         }

         this.sendTeamRemovePacket(var1, "scg_blue");
         this.sendTeamRemovePacket(var1, "scg_red");
         if (!var2.isEmpty()) {
            this.sendTeamCreatePacket(var1, "scg_blue", Component.empty(), Component.empty(), var2, NamedTextColor.BLUE);
         }

         if (!var3.isEmpty()) {
            this.sendTeamCreatePacket(var1, "scg_red", Component.empty(), Component.empty(), var3, NamedTextColor.RED);
         }
      }
   }

   public void applyTeamGlow(Collection<UUID> var1, Collection<UUID> var2) {
      for (UUID var4 : var1) {
         this.glowColorOverrides.put(var4, NamedTextColor.BLUE);
      }

      for (UUID var11 : var2) {
         this.glowColorOverrides.put(var11, NamedTextColor.RED);
      }

      HashSet var10 = new HashSet(var1);
      var10.addAll(var2);
      if (this.enabled && this.sortingEnabled) {
         for (UUID var16 : var10) {
            this.teamNameCache.remove(var16);
         }

         for (UUID var17 : var10) {
            Player var21 = Bukkit.getPlayer(var17);
            if (var21 != null && var21.isOnline()) {
               this.applyTeamSorting(var21);
            }
         }
      } else {
         ArrayList var12 = new ArrayList();
         ArrayList var5 = new ArrayList();

         for (UUID var7 : var1) {
            Player var8 = Bukkit.getPlayer(var7);
            if (var8 != null && var8.isOnline()) {
               var12.add(var8.getName());
            }
         }

         for (UUID var23 : var2) {
            Player var25 = Bukkit.getPlayer(var23);
            if (var25 != null && var25.isOnline()) {
               var5.add(var25.getName());
            }
         }

         for (Player var24 : Bukkit.getOnlinePlayers()) {
            this.sendTeamRemovePacket(var24, "scg_blue");
            this.sendTeamRemovePacket(var24, "scg_red");
            if (!var12.isEmpty()) {
               this.sendTeamCreatePacket(var24, "scg_blue", Component.empty(), Component.empty(), var12, NamedTextColor.BLUE);
            }

            if (!var5.isEmpty()) {
               this.sendTeamCreatePacket(var24, "scg_red", Component.empty(), Component.empty(), var5, NamedTextColor.RED);
            }
         }
      }

      for (UUID var18 : var10) {
         Player var22 = Bukkit.getPlayer(var18);
         if (var22 != null && var22.isOnline()) {
            var22.setGlowing(true);
         }
      }

      this.refreshContextVisibleEntries(var10, var10);
   }

   public void removePlayerGlow(UUID var1) {
      this.removePlayerGlow(var1, null);
   }

   public void removePlayerGlow(UUID var1, Collection<UUID> var2) {
      NamedTextColor var3 = this.glowColorOverrides.remove(var1);
      Player var4 = Bukkit.getPlayer(var1);
      if (var4 != null && var4.isOnline()) {
         var4.setGlowing(false);
      }

      if (var3 != null) {
         if (this.enabled && this.sortingEnabled) {
            this.teamNameCache.remove(var1);
            if (var4 != null && var4.isOnline()) {
               this.applyTeamSorting(var4);
            }
         } else {
            Collection var5 = var2 != null ? var2 : null;
            if (var5 != null) {
               for (UUID var7 : var5) {
                  Player var8 = Bukkit.getPlayer(var7);
                  if (var8 != null && var8.isOnline()) {
                     this.rebuildGlowTeamsForViewer(var8);
                  }
               }
            } else {
               for (Player var10 : Bukkit.getOnlinePlayers()) {
                  this.rebuildGlowTeamsForViewer(var10);
               }
            }
         }
      }
   }

   public void removeAllTeamGlow(Collection<UUID> var1) {
      boolean var2 = false;

      for (UUID var4 : var1) {
         if (this.glowColorOverrides.remove(var4) != null) {
            var2 = true;
         }

         Player var5 = Bukkit.getPlayer(var4);
         if (var5 != null && var5.isOnline()) {
            var5.setGlowing(false);
         }
      }

      if (var2) {
         if (this.enabled && this.sortingEnabled) {
            for (UUID var9 : var1) {
               this.teamNameCache.remove(var9);
               Player var10 = Bukkit.getPlayer(var9);
               if (var10 != null && var10.isOnline()) {
                  this.applyTeamSorting(var10);
               }
            }
         } else {
            for (Player var8 : Bukkit.getOnlinePlayers()) {
               this.sendTeamRemovePacket(var8, "scg_blue");
               this.sendTeamRemovePacket(var8, "scg_red");
            }
         }
      }
   }

   public void invalidateRankCache(UUID var1) {
      this.rankCache.remove(var1);
   }

   public void clearRankCache() {
      this.rankCache.clear();
   }

   public void invalidateTeamCache(UUID var1) {
      this.teamNameCache.remove(var1);
   }

   public int getGroupPriority(String var1) {
      if (this.sortingEnabled && !this.groupSorting.isEmpty() && var1 != null) {
         int var2 = this.groupSorting.indexOf(var1.toLowerCase());
         return var2 >= 0 ? var2 : this.groupSorting.size();
      } else {
         return 99;
      }
   }

   public void onRankChange(Player var1) {
      if (this.enabled && var1 != null && var1.isOnline()) {
         UUID var2 = var1.getUniqueId();
         this.invalidateSortOrder();
         this.rankCache.remove(var2);
         this.teamNameCache.remove(var2);
         if (this.placeholderCache != null) {
            this.placeholderCache.invalidateEventPlaceholders(var2);
         }

         this.updatePlayerTabName(var1);
         this.applyTeamSorting(var1);
      }
   }

   private Component parseDisplayName(String var1) {
      return (Component)(var1 != null && !var1.isEmpty() ? this.displayNameCache.get(var1, this::parseDisplayNameInternal) : Component.empty());
   }

   private Component parseTagDisplayName(String var1) {
      Component var2 = this.parseDisplayName(this.stripNametagShadowTags(var1));
      return this.shadowEnabled && this.shadowColor != null ? var2.shadowColor(this.shadowColor) : var2;
   }

   private String stripNametagShadowTags(String var1) {
      return var1 != null && !var1.isEmpty() ? var1.replaceAll("(?i)</shadow>", "").replaceAll("(?i)<shadow(?::[^>]*)?>", "") : "";
   }

   private Component parseTagDisplayNameForViewer(Player var1, String var2) {
      return this.parseTagDisplayName(this.renderTabConfigString(var1, var2));
   }

   private String renderTabConfigString(Player var1, String var2) {
      if (var2 == null || var2.isEmpty()) {
         return "";
      } else if (var1 == null) {
         return var2;
      } else {
         TablistPlayerData var3 = this.playerData.computeIfAbsent(var1.getUniqueId(), TablistPlayerData::new);
         this.hiddenPlayers.computeIfAbsent(var1.getUniqueId(), var0 -> ConcurrentHashMap.newKeySet());
         String var4 = var2;
         if (this.provider != null) {
            var4 = this.provider.renderConfigString(var2, var1, var3);
         }

         return this.placeholderCache != null && var4.contains("%") ? this.placeholderCache.resolve(var1, var3, var4) : var4;
      }
   }

   private NamedTextColor resolveTeamColor(UUID var1, TablistManager.PlayerTeamData var2) {
      NamedTextColor var3 = this.glowColorOverrides.get(var1);
      if (var3 != null) {
         return var3;
      } else if (var2 != null && var2.nameColor != null) {
         return var2.nameColor;
      } else {
         NamedTextColor var4 = this.extractFirstLegacyColor(this.nameFormat);
         if (var4 != null) {
            return var4;
         } else {
            NamedTextColor var5 = this.extractLastLegacyColor(var2 == null ? "" : var2.tagPrefix);
            return var5 != null ? var5 : NamedTextColor.WHITE;
         }
      }
   }

   private NamedTextColor parseNameColor(String var1) {
      if (var1 != null && !var1.isEmpty()) {
         NamedTextColor var3 = this.extractFirstLegacyColor(var1);
         return var3 != null ? var3 : this.extractFirstMiniMessageColor(var1);
      } else {
         return null;
      }
   }

   private NamedTextColor extractFirstMiniMessageColor(String var1) {
      if (var1 != null && !var1.isEmpty()) {
         Matcher var2 = Pattern.compile("(?i)<([a-z_]+)>").matcher(var1);

         while (var2.find()) {
            NamedTextColor var3 = this.miniMessageColor(var2.group(1).toLowerCase(Locale.ROOT));
            if (var3 != null) {
               return var3;
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private NamedTextColor miniMessageColor(String var1) {
      return switch (var1) {
         case "black" -> NamedTextColor.BLACK;
         case "dark_blue" -> NamedTextColor.DARK_BLUE;
         case "dark_green" -> NamedTextColor.DARK_GREEN;
         case "dark_aqua" -> NamedTextColor.DARK_AQUA;
         case "dark_red" -> NamedTextColor.DARK_RED;
         case "dark_purple" -> NamedTextColor.DARK_PURPLE;
         case "gold" -> NamedTextColor.GOLD;
         case "gray", "grey" -> NamedTextColor.GRAY;
         case "dark_gray", "dark_grey" -> NamedTextColor.DARK_GRAY;
         case "blue" -> NamedTextColor.BLUE;
         case "green" -> NamedTextColor.GREEN;
         case "aqua" -> NamedTextColor.AQUA;
         case "red" -> NamedTextColor.RED;
         case "light_purple" -> NamedTextColor.LIGHT_PURPLE;
         case "yellow" -> NamedTextColor.YELLOW;
         case "white" -> NamedTextColor.WHITE;
         default -> null;
      };
   }

   private ShadowColor parseShadowColor(String var1) {
      if (var1 != null && !var1.isBlank()) {
         try {
            return ShadowColor.fromHexString(var1.trim());
         } catch (Exception var3) {
            return null;
         }
      } else {
         return null;
      }
   }

   private NamedTextColor extractFirstLegacyColor(String var1) {
      if (var1 != null && !var1.isEmpty()) {
         for (int var2 = 0; var2 < var1.length() - 1; var2++) {
            char var3 = var1.charAt(var2);
            if (var3 == '&' || var3 == 167) {
               char var4 = Character.toLowerCase(var1.charAt(var2 + 1));
               if ("0123456789abcdef".indexOf(var4) >= 0) {
                  return colorFromCode(var4);
               }
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private NamedTextColor extractLastLegacyColor(String var1) {
      if (var1 != null && !var1.isEmpty()) {
         char var2 = 0;

         for (int var3 = var1.length() - 2; var3 >= 0; var3--) {
            char var4 = var1.charAt(var3);
            if (var4 == '&' || var4 == 167) {
               char var5 = Character.toLowerCase(var1.charAt(var3 + 1));
               if ("0123456789abcdef".indexOf(var5) >= 0) {
                  var2 = var5;
                  break;
               }
            }
         }

         return var2 == 0 ? null : colorFromCode(var2);
      } else {
         return null;
      }
   }

   private static NamedTextColor colorFromCode(char var0) {
      return switch (var0) {
         case '0' -> NamedTextColor.BLACK;
         case '1' -> NamedTextColor.DARK_BLUE;
         case '2' -> NamedTextColor.DARK_GREEN;
         case '3' -> NamedTextColor.DARK_AQUA;
         case '4' -> NamedTextColor.DARK_RED;
         case '5' -> NamedTextColor.DARK_PURPLE;
         case '6' -> NamedTextColor.GOLD;
         case '7' -> NamedTextColor.GRAY;
         case '8' -> NamedTextColor.DARK_GRAY;
         case '9' -> NamedTextColor.BLUE;
         default -> null;
         case 'a' -> NamedTextColor.GREEN;
         case 'b' -> NamedTextColor.AQUA;
         case 'c' -> NamedTextColor.RED;
         case 'd' -> NamedTextColor.LIGHT_PURPLE;
         case 'e' -> NamedTextColor.YELLOW;
         case 'f' -> NamedTextColor.WHITE;
      };
   }

   private Component parseDisplayNameInternal(String var1) {
      var1 = this.stripUnmatchedShadowClosers(var1);

      try {
         return MINI_MESSAGE.deserialize(
            f.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
               var1
            )
         );
      } catch (Exception var3) {
         if (HEX_AMPERSAND_PATTERN.matcher(var1).find()) {
            return LEGACY_SERIALIZER.deserialize(this.convertAmpersandHexToLegacy(var1));
         } else {
            return !HEX_SECTION_PATTERN.matcher(var1).find() && !var1.contains("§")
               ? LEGACY_SERIALIZER.deserialize(var1.replace("&", "§"))
               : LEGACY_SERIALIZER.deserialize(var1);
         }
      }
   }

   private String stripUnmatchedShadowClosers(String var1) {
      if (var1 != null && !var1.isEmpty()) {
         Matcher var2 = Pattern.compile("(?i)<shadow(?::[^>]*)?>|</shadow>").matcher(var1);
         StringBuilder var3 = new StringBuilder(var1.length());
         int var4 = 0;

         for (int var5 = 0; var2.find(); var4 = var2.end()) {
            var3.append(var1, var4, var2.start());
            String var6 = var2.group();
            if (var6.regionMatches(true, 0, "</shadow>", 0, var6.length())) {
               if (var5 > 0) {
                  var3.append(var6);
                  var5--;
               }
            } else {
               var3.append(var6);
               var5++;
            }
         }

         return var3.append(var1, var4, var1.length()).toString();
      } else {
         return "";
      }
   }

   private String convertAmpersandHexToLegacy(String var1) {
      Matcher var2 = HEX_AMPERSAND_PATTERN.matcher(var1);
      StringBuilder var3 = new StringBuilder();

      while (var2.find()) {
         String var4 = var2.group(1);
         StringBuilder var5 = new StringBuilder("§x");

         for (char var9 : var4.toCharArray()) {
            var5.append('§').append(var9);
         }

         var2.appendReplacement(var3, var5.toString());
      }

      var2.appendTail(var3);
      return var3.toString().replace("&", "§");
   }

   private static record CachedPlayerInfo(UserProfile profile, PlayerInfo info, long timestamp) {
   }

   private static record ContextSocialLayout(boolean enabled, int ping, List<String> rows, String bluePlayerColor, String redPlayerColor) {
   }

   private static record PlayerInfoSendKey(UUID viewerId, UUID targetId) {
   }

   private static record PlayerRankCache(int priority, String prefix, String name, String suffix, long timestamp) {
   }

   private static record PlayerTeamData(String teamName, String prefix, String suffix, String tagPrefix, String tagSuffix, NamedTextColor nameColor) {
   }

   private static record SocialLayout(List<TablistManager.SocialRow> rows, Set<UUID> visiblePlayers, String cacheKey) {
   }

   private static record SocialRow(String text, UUID playerId) {
      static TablistManager.SocialRow fake(String var0) {
         return new TablistManager.SocialRow(var0, null);
      }

      static TablistManager.SocialRow player(UUID var0) {
         return new TablistManager.SocialRow(null, var0);
      }

      boolean isPlayer() {
         return this.playerId != null;
      }
   }

   private static record TeamSignatureRef(UUID viewerId, String signatureKey) {
   }
}
