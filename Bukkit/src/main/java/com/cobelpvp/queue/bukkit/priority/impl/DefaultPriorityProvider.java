package com.cobelpvp.queue.bukkit.priority.impl;

import com.cobelpvp.queue.bukkit.util.MapUtil;
import com.cobelpvp.queue.bukkit.priority.PriorityProvider;
import com.cobelpvp.queue.bukkit.Queue;
import com.cobelpvp.queue.shared.queue.QueueRank;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class DefaultPriorityProvider implements PriorityProvider {

    private static final Queue plugin = Queue.getInstance();

    private QueueRank defaultPriority;
    private Map<String, QueueRank> priorities = new HashMap<>();

    public DefaultPriorityProvider() {
        FileConfiguration config = plugin.getMainConfig().getConfig();

        try {
            this.defaultPriority = new QueueRank("Default", 1);

            if (config.contains("priority.default")) {
                this.defaultPriority.setPriority(config.getInt("priority.default"));
            }

            if (config.contains("priority.ranks") && config.isConfigurationSection("priority.ranks")) {
                for (String rank : config.getConfigurationSection("priority.ranks").getKeys(false)) {
                    String path = "priority.ranks." + rank;

                    if (config.contains(path + ".priority") && config.contains(path + ".permission")) {
                        this.priorities.put(config.getString(path + ".permission"), new QueueRank(rank, config.getInt
                                (path + ".priority")));
                    }
                }
            }

            this.priorities = MapUtil.sortByValue(this.priorities);
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to configure default priority provider.");
            e.printStackTrace();
        }
    }

    @Override
    public QueueRank getPriority(Player player) {
        for (Map.Entry<String, QueueRank> entry : this.priorities.entrySet()) {
            if (player.hasPermission(entry.getKey())) {
                return entry.getValue();
            }
        }

        return this.defaultPriority;
    }

}
