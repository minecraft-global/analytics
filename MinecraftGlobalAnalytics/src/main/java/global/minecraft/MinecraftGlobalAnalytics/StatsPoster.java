package global.minecraft.MinecraftGlobalAnalytics;

import org.apache.commons.io.FileUtils;
import org.bukkit.Server;
import org.bukkit.World;

import java.io.File;
import java.time.Instant;
import java.time.temporal.ChronoField;

public class StatsPoster implements Runnable {
    Server server;

    EventsListener eventsListener;
    TPSMeasurer tpsMeasurer;

    int lastHour;

    public StatsPoster(Server s, EventsListener eL, TPSMeasurer tP) {
        super();

        server = s;

        eventsListener = eL;
        tpsMeasurer = tP;

        lastHour = Instant.now().get(ChronoField.CLOCK_HOUR_OF_DAY);
    }

    @Override
    public void run() {
        if (Instant.now().get(ChronoField.CLOCK_HOUR_OF_DAY) != lastHour) {
            postStats();
        }
    }

    private void postStats() {
        float averageTPS = tpsMeasurer.getAverageTPS();
        tpsMeasurer.reset();

        long messagesSince = eventsListener.getMessagesSince();
        eventsListener.resetStatCounters();

        long memoryUsageBytes = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        long worldsStorageUsageBytes = 0;

        for (World world : server.getWorlds()) {
            worldsStorageUsageBytes += FileUtils.sizeOfDirectory(world.getWorldFolder());
        }
    }
}
