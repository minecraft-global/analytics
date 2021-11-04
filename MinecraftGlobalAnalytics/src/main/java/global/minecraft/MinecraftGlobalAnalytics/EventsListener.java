package global.minecraft.MinecraftGlobalAnalytics;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class EventsListener implements Listener {
    private long messagesSince = 0;

    @EventHandler
    public void onAsyncPlayerChatEvent(AsyncPlayerChatEvent event) {
        if (!event.isCancelled()) messagesSince += 1;
    }

    public long getMessagesSince() {
        return messagesSince;
    }

    public void resetStatCounters() {
        messagesSince = 0;
    }
}
