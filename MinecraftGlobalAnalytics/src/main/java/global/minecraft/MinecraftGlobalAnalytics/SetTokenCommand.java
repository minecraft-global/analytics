package global.minecraft.MinecraftGlobalAnalytics;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class SetTokenCommand implements CommandExecutor {
    private final JavaPlugin plugin;
    private final AuthHolder authorization;

    public SetTokenCommand(JavaPlugin p, AuthHolder a) {
        plugin = p;
        authorization = a;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof ConsoleCommandSender)) {
            sender.sendMessage(ChatColor.RED + "You must execute this command in the console.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "You must specify the server token.");
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Invalid server token.");
            return true;
        }

        plugin.getConfig().set("serverToken", args[0]);
        plugin.saveConfig();

        authorization.setDefaulted(false);
        authorization.set(args[0]);

        sender.sendMessage(ChatColor.GREEN + "Successfully set server token.");

        return true;
    }
}
