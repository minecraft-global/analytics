package global.minecraft.MinecraftGlobalAnalytics;

import com.sun.management.OperatingSystemMXBean;
import org.apache.commons.io.FileUtils;
import org.bukkit.Server;
import org.bukkit.World;

import java.lang.management.ManagementFactory;
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

    private StatsRecord fetchStats() {
        OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
        float systemCpuLoad = (float) osBean.getSystemCpuLoad();

        long memoryUsageBytes = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        float averageTPS = tpsMeasurer.getAverageTPS();
        long messagesSince = eventsListener.getMessagesSince();

        tpsMeasurer.reset();
        eventsListener.resetStatCounters();

        long worldsStorageUsageBytes = 0;

        for (World world : server.getWorlds()) {
            worldsStorageUsageBytes += FileUtils.sizeOfDirectory(world.getWorldFolder());
        }

        return new StatsRecord(systemCpuLoad, memoryUsageBytes, averageTPS, worldsStorageUsageBytes, messagesSince);
    }

    private void postStats() {
        String data = fetchStats().toJsonString();
    }
}
