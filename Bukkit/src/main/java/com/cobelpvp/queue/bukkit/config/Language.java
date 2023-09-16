package com.cobelpvp.queue.bukkit.config;

import com.cobelpvp.queue.bukkit.Queue;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class Language {

    private List<String> reminder;

    private List<String> added;

    private List<String> removed;

    public void load() {
        FileConfiguration config = Queue.getInstance().getMainConfig().getConfig();

        if (config.contains("language.reminder")) {
            this.reminder = config.getStringList("language.reminder");
        }

        if (config.contains("language.added")) {
            this.added = config.getStringList("language.added");
        }

        if (config.contains("language.removed")) {
            this.removed = config.getStringList("language.removed");
        }
    }

    public List<String> getReminder(Player player, com.cobelpvp.queue.shared.queue.Queue queue) {
        List<String> translated = new ArrayList<>();

        for (String line : this.reminder) {
            translated.add(ChatColor.translateAlternateColorCodes('&', line
                    .replace("{position}", queue.getPosition(player.getUniqueId()) + "")
                    .replace("{total}", queue.getPlayers().size() + "")
                    .replace("{queue}", queue.getName()))
            );
        }

        return translated;
    }

    public List<String> getAdded(Player player, com.cobelpvp.queue.shared.queue.Queue queue) {
        List<String> translated = new ArrayList<>();

        for (String line : this.added) {
            translated.add(ChatColor.translateAlternateColorCodes('&', line
                    .replace("{position}", queue.getPosition(player.getUniqueId()) + "")
                    .replace("{total}", queue.getPlayers().size() + "")
                    .replace("{queue}", queue.getName()))
            );
        }

        return translated;
    }

    public List<String> getRemoved(com.cobelpvp.queue.shared.queue.Queue queue) {
        List<String> translated = new ArrayList<>();

        for (String line : this.removed) {
            translated.add(ChatColor.translateAlternateColorCodes('&', line
                    .replace("{queue}", queue.getName()))
            );
        }

        return translated;
    }

}
