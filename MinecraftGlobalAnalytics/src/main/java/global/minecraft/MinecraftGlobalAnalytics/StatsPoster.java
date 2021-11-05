package global.minecraft.MinecraftGlobalAnalytics;

import com.sun.management.OperatingSystemMXBean;
import org.apache.commons.io.FileUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.bukkit.Server;
import org.bukkit.World;

import java.io.UnsupportedEncodingException;
import java.lang.management.ManagementFactory;
import java.time.Instant;
import java.time.temporal.ChronoField;

public class StatsPoster implements Runnable {
    private final Server server;

    private final String authorization;

    private final EventsListener eventsListener;
    private final TPSMeasurer tpsMeasurer;

    private long lastSeconds;

    public StatsPoster(Server s, String a, EventsListener eL, TPSMeasurer tP) {
        super();

        server = s;

        authorization = a;

        eventsListener = eL;
        tpsMeasurer = tP;

        lastSeconds = Instant.now().getEpochSecond();
    }

    @Override
    public void run() {
        long currentSeconds = Instant.now().getEpochSecond();

        if (lastSeconds + 3600 >= currentSeconds) {
            postStats();
            lastSeconds = currentSeconds;
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
        HttpPost request = new HttpPost("https://v2api.minecraft.global/serverstats");

        request.addHeader("Authorization", authorization);
        request.addHeader("Content-Type", "application/json");

        try {
            request.setEntity(new StringEntity(data));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return;
        }

        try (CloseableHttpClient httpClient = HttpClients.createDefault(); CloseableHttpResponse response = httpClient.execute(request)) {
            assert response.getStatusLine().getStatusCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
