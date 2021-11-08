package global.minecraft.MinecraftGlobalAnalytics;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.UnsupportedEncodingException;
import java.time.LocalTime;

public class StatsPoster implements Runnable {
    private final String authorization;
    private final StatsFetcher statsFetcher;

    private int lastHour;

    public StatsPoster(StatsFetcher s, String a) {
        super();

        authorization = a;
        statsFetcher = s;

        lastHour = LocalTime.now().getHour();
    }

    @Override
    public void run() {
        int hourNow = LocalTime.now().getHour();

        if (hourNow != lastHour) {
            System.out.printf("Posting stats (hour: %d - %d)%n", hourNow, lastHour);
            postStats();
            lastHour = hourNow;
        }
    }

    private void postStats() {
        String data = statsFetcher.fetchStats().toJsonString();
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
