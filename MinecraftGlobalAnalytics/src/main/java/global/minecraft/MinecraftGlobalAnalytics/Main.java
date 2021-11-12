package global.minecraft.MinecraftGlobalAnalytics;

import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public final class Main extends JavaPlugin {
    private AuthHolder authorization;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        Server server = getServer();
        PluginManager pluginManager = server.getPluginManager();

        FileConfiguration config = getConfig();
        authorization = new AuthHolder(config.getString("serverToken"));

        if (authorization.get().equals(config.getDefaults().getString("serverToken"))) {
            authorization.setDefaulted(true);
            server.getConsoleSender().sendMessage(ChatColor.YELLOW + "MinecraftGlobalAnalytics server token not set, please set token via /settoken <token>");
        }

        BukkitScheduler scheduler = server.getScheduler();
        EventsListener eventsListener = new EventsListener();
        TPSMeasurer tpsMeasurer = new TPSMeasurer();
        StatsFetcher statsFetcher = new StatsFetcher(server, eventsListener, tpsMeasurer);
        StatsPoster statsPoster = new StatsPoster(server, statsFetcher, authorization);

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
