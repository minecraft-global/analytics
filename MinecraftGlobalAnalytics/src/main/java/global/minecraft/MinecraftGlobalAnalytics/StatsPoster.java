package global.minecraft.MinecraftGlobalAnalytics;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.ConsoleCommandSender;

import java.io.UnsupportedEncodingException;
import java.time.LocalTime;

public class StatsPoster implements Runnable {
    private final Server server;

    private final StatsAggregator statsAggregator;
    private final AuthHolder authorization;

    private int lastHour;

    public StatsPoster(Server s, StatsAggregator sF, AuthHolder a) {
        super();

        server = s;

        statsAggregator = sF;
        authorization = a;

        lastHour = LocalTime.now().getHour();
    }

    @Override
    public void run() {
        int hourNow = LocalTime.now().getHour();

        if (hourNow != lastHour) {
            lastHour = hourNow;

            if (authorization.isDefault()) {
                server.getConsoleSender().sendMessage(ChatColor.GOLD + "[MinecraftGlobalAnalytics] Skipping stats post due to missing server token. Set the token in config.yml or via /settoken <token>.");
                return;
            }

            postStats();
        }
    }

    private void postStats() {
        String data = statsAggregator.fetchStats().toJsonString();
        HttpPost request = new HttpPost("https://api.minecraft.global/serverstats");

        request.addHeader("Authorization", authorization.get());
        request.addHeader("Content-Type", "application/json");

        try {
            request.setEntity(new StringEntity(data));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return;
        }

        try (CloseableHttpClient httpClient = HttpClients.createDefault(); CloseableHttpResponse response = httpClient.execute(request)) {
            int statusCode = response.getStatusLine().getStatusCode();

            ConsoleCommandSender consoleSender = server.getConsoleSender();

            switch (statusCode) {
                case 200: break;
                case 401:
                    consoleSender.sendMessage(ChatColor.RED + "[MinecraftGlobalAnalytics] Failed to post analytics to minecraft.global: Invalid server token specified in config.");
                    break;
                case 403:
                    consoleSender.sendMessage(ChatColor.RED + "[MinecraftGlobalAnalytics] Failed to post analytics to minecraft.global: This server does not have premium.");
                    break;
                default:
                    String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");
                    throw new Exception(String.format("Response status code was not 200 OK (Was %d): %s", statusCode, responseBody));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
