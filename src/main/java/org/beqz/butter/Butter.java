package org.beqz.butter;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Jonathan on 17/05/2014.
 */
public class Butter extends JavaPlugin implements Listener
{
    private ButterClient client;
    private Map<String, Object> playerHandles;

    public Butter()
    {
        client = new ButterClient(this);
    }

    @Override
    public void onEnable()
    {
        this.saveDefaultConfig();

        getServer().getPluginManager().registerEvents(this, this);

        playerHandles = this.getConfig().getConfigurationSection("playerHandles").getValues(true);
    }

    @Override
    public void onDisable()
    {
        this.getConfig().createSection("playerHandles", playerHandles);
        this.saveConfig();
    }

    // TODO tweet when player joins the server
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        Player player = event.getPlayer();
        client.TweetPlayerJoin(player);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if (cmd.getName().equalsIgnoreCase("butter"))
        {
            // TODO player can register their twitter handle
            if (args.length == 2 && args[0].equalsIgnoreCase("register"))
            {
                if (sender instanceof Player)
                {
                    RegisterUser(((Player) sender), args[1]);
                    sender.sendMessage(String.format("%s has been registered successfully", args[1]));
                    return true;
                }
                sender.sendMessage("You must be a player to register a Twitter handle");
                return false;
            }

            // TODO register plugin with OAuth
            if (args.length == 1 && args[0].equalsIgnoreCase("auth"))
            {
                if (sender.isOp() && sender instanceof Conversable)
                {
                    return client.Authenticate(sender);
                }
                sender.sendMessage("You do not have permission");
                return false;
            }

            sender.sendMessage("Unknown command");
        }

        return false;
    }

    private void RegisterUser(Player player, String handle)
    {
        if(!handle.startsWith("@"))
        {
            handle = String.format("@%s", handle);
        }

        playerHandles.put(player.getUniqueId().toString(), handle);
    }
}