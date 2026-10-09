package org.lime.swiftCore.tablist;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType.Play.Server;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoUpdate;
import java.util.List;
import org.bukkit.entity.Player;

final class TablistPlayerInfoFilter extends PacketListenerAbstract {
   private final TablistManager manager;

   TablistPlayerInfoFilter(TablistManager var1) {
      super(PacketListenerPriority.MONITOR);
      this.manager = var1;
   }

   public void onPacketSend(PacketSendEvent var1) {
      if (var1.getPacketType() == Server.PLAYER_INFO_UPDATE) {
         Player var2 = (Player)var1.getPlayer();
         if (var2 != null && this.manager.shouldFilterPlayerInfo(var2)) {
            WrapperPlayServerPlayerInfoUpdate var3 = new WrapperPlayServerPlayerInfoUpdate(var1);
            List var4 = var3.getEntries();
            List var5 = var4.stream().filter(var2x -> this.manager.isPlayerEntryAllowed(var2, var2x.getProfileId())).toList();
            if (var5.size() != var4.size()) {
               if (var5.isEmpty()) {
                  var1.setCancelled(true);
               } else {
                  var3.setEntries(var5);
                  var1.markForReEncode(true);
               }
            }
         }
      }
   }
}
