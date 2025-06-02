package org.tp211.enderchest_portal;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class PlugMain extends JavaPlugin implements Listener {

    private boolean isPlayerOnEndChest(Player player) {
        Block block = player.getLocation().getBlock();
        return block.getType() == Material.ENDER_CHEST;
    }

    private boolean isPlayerOpeningOwnEndChest(Player player, Block block) {
        return isPlayerOnEndChest(player) && block.getLocation().equals(player.getLocation().getBlock().getLocation());
    }

    public boolean hasCompletedTheEnd(Player player) {
        // 获取 "结束了？" 成就
        Advancement advancement = Bukkit.getAdvancement(
                NamespacedKey.minecraft("story/enter_the_end")
        );

        if (advancement == null) {
            Bukkit.getLogger().warning("[ECP] Advancement Not Found!");
            return false;
        }

        AdvancementProgress progress = player.getAdvancementProgress(advancement);
        return progress.isDone(); // true = 已完成
    }


    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && block != null && block.getType() == Material.ENDER_CHEST) {
            if (isPlayerOnEndChest(player) && isPlayerOpeningOwnEndChest(player, block)) {
                if (hasCompletedTheEnd(player)) {
                    Location endPlatform = new Location(Bukkit.getWorld("world_the_end"), 0, 70, 0);
                    player.teleport(endPlatform);
                    player.sendMessage("Teleported to the End!");
                } else {
                    player.sendMessage("Please Finished <The End?> Advancement First!");
                }
            }
        }
    }
}
