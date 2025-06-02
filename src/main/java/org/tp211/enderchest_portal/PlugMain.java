package org.tp211.enderchest_portal;

import org.bukkit.*;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class PlugMain extends JavaPlugin implements Listener {
    // 检测成就
    private boolean enableAdvancementCheck;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        enableAdvancementCheck = getOrWriteDefaultConfigBoolean("enable-advancement-check", true);
        loadDefaultMessages();

        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("[ECP] Enabled");
    }

    private void loadDefaultMessages() {
        if (!getConfig().isSet("messages.teleported")) {
            getConfig().set("messages.teleported", "§a你被传送到了末地平台！");
        }
        if (!getConfig().isSet("messages.no_advancement")) {
            getConfig().set("messages.no_advancement", "§c你还未完成进入末地的成就！");
        }
        if (!getConfig().isSet("messages.reload_no_permission")) {
            getConfig().set("messages.reload_no_permission", "§c你没有权限重载配置！");
        }
        if (!getConfig().isSet("messages.reload_done")) {
            getConfig().set("messages.reload_done", "§a配置已重载！");
        }
        saveConfig();
    }

    private boolean getOrWriteDefaultConfigBoolean(String path, boolean defaultValue) {
        if (!getConfig().isSet(path)) {
            getConfig().set(path, defaultValue);
            saveConfig();
        }
        return getConfig().getBoolean(path, defaultValue);
    }

    private boolean isPlayerOnEndChest(Player player) {
        Block block = player.getLocation().getBlock();
        return block.getType() == Material.ENDER_CHEST;
    }

    private boolean isPlayerOpeningOwnEndChest(Player player, Block block) {
        return isPlayerOnEndChest(player) && block.getLocation().equals(player.getLocation().getBlock().getLocation());
    }

    public boolean hasCompletedTheEnd(Player player) {
        if (!enableAdvancementCheck) return true; // 熔断

        Advancement advancement = Bukkit.getAdvancement(NamespacedKey.minecraft("story/enter_the_end"));
        if (advancement == null) {
            Bukkit.getLogger().warning("[ECP] Advancement Not Found!");
            return false;
        }

        AdvancementProgress progress = player.getAdvancementProgress(advancement);
        return progress.isDone();
    }

    private Location getEndPlatformForPlayer(Player player) {
        UUID currentWorldUUID = player.getWorld().getUID();
        long highBits = currentWorldUUID.getMostSignificantBits();

        for (World world : Bukkit.getWorlds()) {
            if (world.getEnvironment() == World.Environment.THE_END &&
                    world.getUID().getMostSignificantBits() == highBits) {
                return new Location(world, 0, 70, 0); // 末地默认位置
            }
        }

        return null;
    }

    public String getMessage(String key) {
        return getConfig().getString("messages." + key, "§c[消息缺失: " + key + "]");
    }

    public void sendMessage(Player player, String key) {
        player.sendMessage(getMessage(key));
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && block != null && block.getType() == Material.ENDER_CHEST) {
            if (isPlayerOnEndChest(player) && isPlayerOpeningOwnEndChest(player, block)) {
                if (hasCompletedTheEnd(player)) {
                    Location endLoc = getEndPlatformForPlayer(player);
                    if (endLoc != null) {
                        player.teleport(endLoc);
                        sendMessage(player, "teleported");
                    } else {
                        player.sendMessage("§c未找到末地平台！");
                    }
                } else {
                    sendMessage(player, "no_advancement");
                }
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("ecp")) {
            if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                if (!sender.hasPermission("ecp.reload")) {
                    sender.sendMessage(getMessage("reload_no_permission"));
                    return true;
                }
                reloadConfig();
                enableAdvancementCheck = getOrWriteDefaultConfigBoolean("enable-advancement-check", true);
                loadDefaultMessages();
                sender.sendMessage(getMessage("reload_done"));
                return true;
            }
        }
        return false;
    }
}