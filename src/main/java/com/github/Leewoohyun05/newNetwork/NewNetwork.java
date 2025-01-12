package com.github.Leewoohyun05.newNetwork;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.http.WebSocket;

public final class NewNetwork extends JavaPlugin implements WebSocket.Listener {

    @Override
    public void onEnable() {
        getLogger().info(ChatColor.GREEN + "--------------------------------");
        getLogger().info(ChatColor.GREEN + "      NewNetwork System On      ");
        getLogger().info(ChatColor.GREEN + "--------------------------------");

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
