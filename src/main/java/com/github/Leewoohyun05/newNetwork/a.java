package com.github.Leewoohyun05.newNetwork;

import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldguard.protection.regions.RegionType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionContainer;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;

import java.util.*;

public class a extends JavaPlugin implements Listener {
    private final Map<String, Nation> nations = new HashMap<>();
    private final Set<Player> pioneers = new HashSet<>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (isPioneer(player)) {
            pioneers.add(player);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.BEACON) {
            String playerNation = getPlayerNation(player);
            if (isPioneer(player) && !hasNation(player) && playerNation == null) {
                isNationNameValid(playerNation);
            }
            player.sendMessage(ChatColor.RED + "국가 생성에 실패했습니다.");
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        if (isNationBlock(block.getLocation())) {
            if (!isNationMember(player, block.getLocation())) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "국가 영토를 파괴할 수 없습니다.");
                broadcastNationAttack(player, block.getLocation());
            }
        }
    }

    private boolean isPioneer(Player player) {
        return player.hasPermission("a.pioneer");
    }

    private boolean hasNation(Player player) {
        return nations.containsValue(player.getName());
    }

    private String getPlayerNation(Player player) {
        for (Map.Entry<String, Nation> entry : nations.entrySet()) {
            if (entry.getValue().founder().equals(player)) {
                return entry.getKey();
            }
        }
        return null;
    }

    private boolean isNationBlock(Location location) {
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regionManager = ((com.sk89q.worldguard.protection.regions.RegionContainer) container).get(BukkitAdapter.adapt(location.getWorld()));
        if (regionManager == null) return false;
        for (ProtectedRegion region : regionManager.getRegions().values()) {
            if (region.contains(BlockVector3.at(location.getX(), location.getY(), location.getZ()))) {
                return true;
            }
        }
        return false;
    }

    private boolean isNationMember(Player player, Location location) {
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regionManager = ((com.sk89q.worldguard.protection.regions.RegionContainer) container).get(BukkitAdapter.adapt(location.getWorld()));
        if (regionManager == null) return false;
        for (ProtectedRegion region : regionManager.getRegions().values()) {
            if (region.contains(BlockVector3.at(location.getX(), location.getY(), location.getZ()))) {
                return region.isMember(String.valueOf(player.getUniqueId()));
            }
        }
        return false;
    }

    private void createNation(Player player, String name) {
        long createdAt = System.currentTimeMillis();
        Nation nation = new Nation(name, player, createdAt);
        nations.put(name, nation);
        createNationRegion(player, name, createdAt);
        player.sendMessage(ChatColor.GREEN + "국가가 성공적으로 생성되었습니다.");
        broadcastNationCreation(player, name);
    }

    private void isNationNameValid(String name) {
    }

    private void createNationRegion(Player player, String name, long createdAt) {
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regionManager = ((com.sk89q.worldguard.protection.regions.RegionContainer) container).get(BukkitAdapter.adapt(player.getWorld()));
        if (regionManager == null) return;

        BlockVector3 min = BlockVector3.at(player.getLocation().getBlockX() - 5, player.getLocation().getBlockY() - 5, player.getLocation().getBlockZ() - 5);
        BlockVector3 max = BlockVector3.at(player.getLocation().getBlockX() + 5, player.getLocation().getBlockY() + 5, player.getLocation().getBlockZ() + 5);
        String regionName = getNationRegionName(createdAt);

        ProtectedRegion region = new MyProtectedRegion(regionName, min, max);
        region.getOwners().addPlayer(player.getUniqueId());
        regionManager.addRegion(region);
    }

    private String getNationRegionName(long createdAt) {
        return "nation-" + createdAt;
    }

    private void broadcastNationCreation(Player player, String name) {
        Bukkit.broadcastMessage(ChatColor.GREEN + player.getName() + "님이 " + name + " 국가를 창설했습니다.");
    }

    private void broadcastNationAttack(Player player, Location location) {
        Bukkit.broadcastMessage(ChatColor.RED + player.getName() + "님이 " + location + " 국가 영토를 공격했습니다.");
    }

    private static abstract class MyProtectedRegion extends ProtectedRegion {
        public MyProtectedRegion(String regionName, BlockVector3 min, BlockVector3 max) {
            super(regionName, min, max);
        }

        @Override
        public boolean isPhysicalArea() {
            return false;
        }

        @Override
        public List<BlockVector2> getPoints() {
            return List.of();
        }

        @Override
        public int volume() {
            return 0;
        }

        @Override
        public boolean contains(BlockVector3 blockVector3) {
            return false;
        }

        @Override
        public RegionType getType() {
            return null;
        }
    }
}

record Nation(String name, Player founder, long createdAt) {
}
