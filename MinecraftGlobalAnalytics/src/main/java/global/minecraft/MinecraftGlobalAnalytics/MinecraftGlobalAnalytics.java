package global.minecraft.MinecraftGlobalAnalytics;

import com.sun.org.glassfish.external.statistics.Stats;
import org.bukkit.Server;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public final class MinecraftGlobalAnalytics extends JavaPlugin {
    @Override
    public void onEnable() {
        this.saveDefaultConfig();

        Server server = getServer();
        BukkitScheduler scheduler = server.getScheduler();
        EventsListener eventsListener = new EventsListener();
        TPSMeasurer tpsMeasurer = new TPSMeasurer();
        StatsPoster statsPoster = new StatsPoster(server, this.getConfig().getString("serverToken"), eventsListener, tpsMeasurer);

        server.getPluginManager().registerEvents(eventsListener, this);

        scheduler.scheduleSyncRepeatingTask(this, tpsMeasurer, 0, 1);
        scheduler.scheduleSyncRepeatingTask(this, statsPoster, 0, 20*60);
    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
    }
}
