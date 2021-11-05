package global.minecraft.MinecraftGlobalAnalytics;

import com.sun.management.OperatingSystemMXBean;
import org.apache.commons.io.FileUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.bukkit.Server;
import org.bukkit.World;

import java.io.UnsupportedEncodingException;
import java.lang.management.ManagementFactory;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

public class StatsPoster implements Runnable {
    private final Server server;

    private final String authorization;

    private final EventsListener eventsListener;
    private final TPSMeasurer tpsMeasurer;

    private int lastHour = -1;

    public StatsPoster(Server s, String a, EventsListener eL, TPSMeasurer tP) {
        super();

        server = s;

        authorization = a;

        eventsListener = eL;
        tpsMeasurer = tP;
    }

    @Override
    public void run() {
        int hourNow = LocalTime.now().getHour();

        if (hourNow != lastHour) {
            postStats();
            lastHour = hourNow;
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
            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode != 200) {
                String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");
                throw new Exception(String.format("Response status code was not 200 OK (Was %d): %s", statusCode, responseBody));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
