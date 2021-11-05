package global.minecraft.MinecraftGlobalAnalytics;

import org.bukkit.Server;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public final class Main extends JavaPlugin {
    @Override
    public void onEnable() {
        this.saveDefaultConfig();

        Server server = getServer();
        BukkitScheduler scheduler = server.getScheduler();
        EventsListener eventsListener = new EventsListener();
        TPSMeasurer tpsMeasurer = new TPSMeasurer();
        StatsPoster statsPoster = new StatsPoster(server, this.getConfig().getString("serverToken"), eventsListener, tpsMeasurer);

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
