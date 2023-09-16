package com.cobelpvp.queue.bukkit.command;

import com.google.gson.JsonObject;
import com.cobelpvp.atheneum.command.Command;
import com.cobelpvp.queue.bukkit.Queue;
import com.cobelpvp.queue.shared.jedis.JedisAction;
import com.cobelpvp.queue.shared.jedis.JedisChannel;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class LeaveQueueCommand {

    @Command(names = {"leavequeue"}, permission = "")
    public static void leaveQueueCommand(Player sender) {
        com.cobelpvp.queue.shared.queue.Queue queue = com.cobelpvp.queue.shared.queue.Queue.getByPlayer(sender.getUniqueId());

        if (queue == null) {
            sender.sendMessage(ChatColor.RED + "You are not currently in queue");
            return;
        }

        JsonObject data = new JsonObject();
        data.addProperty("uuid", sender.getUniqueId().toString());

        Queue.getInstance().getPublisher().write(JedisChannel.INDEPENDENT, JedisAction.REMOVE_PLAYER, data);

        sender.sendMessage(ChatColor.RED + "You successfully left the queue!");
    }
}
