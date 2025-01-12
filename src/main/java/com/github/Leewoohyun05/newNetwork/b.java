package com.github.Leewoohyun05.newNetwork;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class b extends JavaPlugin implements Listener {
    private final Map<Player, Job> playerJobs = new HashMap<>();
    private static final int MAX_LEVEL = 15;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Job job = playerJobs.get(player);
        if (job != null) {
            job.giveExperience(1);
            job.handleLevelUp();
            job.applyLevelEffects(event);
        }
    }

    public void setPlayerJob(Player player, Job job) {
        playerJobs.put(player, job);
        player.sendMessage("직업이 " + job.getName() + "(으)로 설정되었습니다.");
    }

    public class Job {
        private final String name;
        private final Material tool;
        private int level = 0;
        private int experience = 0;

        public Job(String name, Material tool) {
            this.name = name;
            this.tool = tool;
        }

        public String getName() {
            return name;
        }

        public Material getTool() {
            return tool;
        }

        public int getLevel() {
            return level;
        }

        public void giveExperience(int amount) {
            experience += amount;
            checkLevelUp();
        }

        private void checkLevelUp() {
            int requiredExperience = getRequiredExperience(level);
            while (experience >= requiredExperience && level < MAX_LEVEL) {
                level++;
                experience -= requiredExperience;
                requiredExperience = getRequiredExperience(level);
                handleLevelUp();
            }
        }

        private void handleLevelUp() {
            Player player = getPlayer();
            if (player != null) {
                player.sendMessage("직업 레벨이 " + level + "으로 올랐습니다!");
            }
        }

        private void applyLevelEffects(BlockBreakEvent event) {
            Player player = getPlayer();
            if (player != null) {
                switch (name) {
                    case "광부":
                        if (level >= 10 || level >= 15) {
                            player.getInventory().addItem(new ItemStack(Material.DIAMOND_PICKAXE, 1));
                        }
                        break;
                    case "농부":
                        if (level >= 5) {
                            // 작물 자동 재배 코드
                        }
                        if (level >= 10 || level >= 15) {
                            // 작물 추가 드롭 코드
                        }
                        break;
                    case "목수":
                        if (level >= 15) {
                            // 나무 밑둥 전체 캐기 코드
                        }
                        break;
                }
            }
        }

        private Player getPlayer() {
            for (Map.Entry<Player, Job> entry : ((b) Objects.requireNonNull(getServer().getPluginManager().getPlugin("JobSystem"))).playerJobs.entrySet()) {
                if (entry.getValue() == this) {
                    return entry.getKey();
                }
            }
            return null;
        }

        private static int getRequiredExperience(int level) {
            return switch (level) {
                case 0 -> 100;
                case 1 -> 300;
                case 2 -> 700;
                case 3 -> 1300;
                case 4 -> 2100;
                case 5 -> 3000;
                case 6 -> 4000;
                case 7 -> 5200;
                case 8 -> 6500;
                case 9 -> 8000;
                case 10 -> 10000;
                case 11 -> 13000;
                case 12 -> 17000;
                case 13 -> 22000;
                case 14 -> 30000;
                default -> 0;
            };
        }
        @EventHandler
        public void onBlockBreak(BlockBreakEvent event) {
            Player player = event.getPlayer();
            Job job = playerJobs.get(player);

            if (job != null) {
                Material blockType = event.getBlock().getType();

                switch (job.getName()) {
                    case "농부":
                        // 자란 작물들만 경험치 부여
                        if (isFullyGrownCrop(blockType)) {
                            job.giveExperience(1); // 모든 자란 작물에서 1 경험치
                        }
                        break;
                    case "광부":
                        // 광부가 부수는 블록에 따른 경험치 설정
                        int experienceGained = getMiningExperience(blockType);
                        if (experienceGained > 0) {
                            job.giveExperience(experienceGained);
                        }
                        break;
                    case "목수":
                        // 모든 나무 원목 종류에서 경험치 부여
                        if (isWood(blockType)) {
                            job.giveExperience(1); // 모든 나무 원목에서 1 경험치
                        }
                        break;
                }

                job.handleLevelUp();
                job.applyLevelEffects(event);
            }
        }

        // 자란 작물인지 확인하는 메서드
        private boolean isFullyGrownCrop(Material blockType) {
            return switch (blockType) {
                case WHEAT, CARROTS, POTATOES, BEETROOTS, MELON, PUMPKIN, SUGAR_CANE, NETHER_WART, SWEET_BERRIES, COCOA_BEANS -> true;
                default -> false;
            };
        }

        // 나무 원목인지 확인하는 메서드
        private boolean isWood(Material blockType) {
            return switch (blockType) {
                case OAK_WOOD, SPRUCE_WOOD, BIRCH_WOOD, JUNGLE_WOOD, ACACIA_WOOD, DARK_OAK_WOOD -> true;
                default -> false;
            };
        }

        // 광부가 부수는 블록에 따른 경험치 설정
        private int getMiningExperience(Material blockType) {
            return switch (blockType) {
                case COAL_ORE -> 1;
                case DEEPSLATE_COAL_ORE -> 2;
                case IRON_ORE -> 2;
                case DEEPSLATE_IRON_ORE -> 3;
                case GOLD_ORE -> 3;
                case DEEPSLATE_GOLD_ORE -> 4;
                case LAPIS_ORE -> 2;
                case DEEPSLATE_LAPIS_ORE -> 3;
                case DIAMOND_ORE -> 5;
                case DEEPSLATE_DIAMOND_ORE -> 6;
                case EMERALD_ORE -> 4;
                case DEEPSLATE_EMERALD_ORE -> 5;
                default -> 0; // 해당하지 않는 블록은 경험치 없음
            };
        }

    }
}