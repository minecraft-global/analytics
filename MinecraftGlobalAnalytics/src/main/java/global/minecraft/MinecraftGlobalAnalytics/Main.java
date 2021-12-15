package global.minecraft.MinecraftGlobalAnalytics;

import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.Random;

public final class Main extends JavaPlugin {
    @Override
    public void onEnable() {
        saveDefaultConfig();

        Server server = getServer();
        PluginManager pluginManager = server.getPluginManager();

        FileConfiguration config = getConfig();
        AuthHolder authorization = new AuthHolder(config.getString("serverToken"));

        if (authorization.get().equals(config.getDefaults().getString("serverToken"))) {
            authorization.setDefaulted(true);
            server.getConsoleSender().sendMessage(ChatColor.GOLD + "[MinecraftGlobalAnalytics] server token not set, please set token via /settoken <token>");
        }

        if (config.getInt("minuteOffset") < 1 || config.getInt("minuteOffset") > 59) {
            config.set("minuteOffset", (new Random()).nextInt(60));
            saveConfig();
        }

        BukkitScheduler scheduler = server.getScheduler();
        EventsListener eventsListener = new EventsListener();
        TPSMeasurer tpsMeasurer = new TPSMeasurer();
        StatsAggregator statsAggregator = new StatsAggregator(server, eventsListener, tpsMeasurer);
        StatsPoster statsPoster = new StatsPoster(server, statsAggregator, authorization, config.getInt("minuteOffset"));

        pluginManager.registerEvents(eventsListener, this);

        // we wait two minutes before starting these tasks
        scheduler.scheduleSyncRepeatingTask(this, tpsMeasurer, 2*20*60, 1);
        scheduler.scheduleSyncRepeatingTask(this, statsPoster, 2*20*60, 100);

        // register settoken command
        getCommand("settoken").setExecutor(new SetTokenCommand(this, authorization));
    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
    }
}
