package global.minecraft.MinecraftGlobalAnalytics;

import com.sun.management.OperatingSystemMXBean;
import org.apache.commons.io.FileUtils;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.bukkit.Server;
import org.bukkit.World;

import java.lang.management.ManagementFactory;
import java.time.Instant;
import java.time.temporal.ChronoField;

public class StatsPoster implements Runnable {
    private final Server server;

    private final String authorization;

    private final EventsListener eventsListener;
    private final TPSMeasurer tpsMeasurer;

    private int lastHour;

    public StatsPoster(Server s, String a, EventsListener eL, TPSMeasurer tP) {
        super();

        server = s;

        authorization = a;

        eventsListener = eL;
        tpsMeasurer = tP;

        lastHour = Instant.now().get(ChronoField.CLOCK_HOUR_OF_DAY);
    }

    @Override
    public void run() {
        int currentHour = Instant.now().get(ChronoField.CLOCK_HOUR_OF_DAY);

        if (currentHour != lastHour) {
            postStats();
            lastHour = currentHour;
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

        CloseableHttpClient httpClient = HttpClients.createDefault();

        try {
            HttpPost request = new HttpPost("https://v2api.minecraft.global/server/{server_id}/stats");
            request.addHeader("Authorization", authorization);
        }
    }
}
