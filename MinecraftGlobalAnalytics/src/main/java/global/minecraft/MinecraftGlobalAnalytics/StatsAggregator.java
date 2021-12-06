package global.minecraft.MinecraftGlobalAnalytics;

import com.sun.management.OperatingSystemMXBean;
import org.apache.commons.io.FileUtils;
import org.bukkit.Server;
import org.bukkit.World;

import java.lang.management.ManagementFactory;

public class StatsAggregator {
    private final Server server;
    private final EventsListener eventsListener;
    private final TPSMeasurer tpsMeasurer;

    public StatsAggregator(Server s, EventsListener eL, TPSMeasurer tP) {
        server = s;
        eventsListener = eL;
        tpsMeasurer = tP;
    }

    public StatsRecord fetchStats() {
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
}
