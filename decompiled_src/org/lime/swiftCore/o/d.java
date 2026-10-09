package org.lime.swiftCore.o;

import org.lime.swiftCore.SwiftCore;
import org.lime.swiftCore.kit.QueueManager;

/**
 * Dual / legacy PlaceholderExpansion providing backwards-compatible
 * support for %swiftcore_<placeholder>% while FlixCore provides %flixcore_<placeholder>%.
 */
public class d extends c {
   public d(SwiftCore var1, QueueManager var2) {
      super(var1, var2);
   }

   @Override
   public String getIdentifier() {
      return "swiftcore";
   }
}
