package com.cobelpvp.queue.bukkit.thread;

import com.cobelpvp.queue.bukkit.Queue;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ReminderThread extends Thread {

    @Override
    public void run() {
        while (true) {
            for (Player player : Queue.getInstance().getServer().getOnlinePlayers()) {
                com.cobelpvp.queue.shared.queue.Queue queue = com.cobelpvp.queue.shared.queue.Queue.getByPlayer(player.getUniqueId());

                if (queue != null) {
                    for (String message : Queue.getInstance().getLanguage().getReminder(player, queue)) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                    }
                }
            }

            try {
                Thread.sleep(25000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
