package global.minecraft.MinecraftGlobalAnalytics;

import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public final class Main extends JavaPlugin {
    @Override
    public void onEnable() {
        saveDefaultConfig();

        Server server = getServer();

        FileConfiguration config = getConfig();
        String authorization = config.getString("serverToken");

        if (authorization.equals(config.getDefaults().getString("serverToken"))) {
            server.getConsoleSender().sendMessage(ChatColor.RED + "Please set the server token in plugins/MinecraftGlobalAnalytics/config.yml.");
            server.shutdown();
            return;
        }

        BukkitScheduler scheduler = server.getScheduler();
        EventsListener eventsListener = new EventsListener();
        TPSMeasurer tpsMeasurer = new TPSMeasurer();
        StatsFetcher statsFetcher = new StatsFetcher(server, eventsListener, tpsMeasurer);
        StatsPoster statsPoster = new StatsPoster(statsFetcher, authorization);

        server.getPluginManager().registerEvents(eventsListener, this);

        // we wait two minutes before starting these tasks
        scheduler.scheduleSyncRepeatingTask(this, tpsMeasurer, 2*20*60, 1);
        scheduler.scheduleSyncRepeatingTask(this, statsPoster, 2*20*60, 100);
    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
    }
}
