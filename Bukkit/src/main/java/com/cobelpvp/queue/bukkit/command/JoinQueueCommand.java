package com.cobelpvp.queue.bukkit.command;

import com.google.gson.JsonObject;
import com.cobelpvp.atheneum.command.Command;
import com.cobelpvp.atheneum.command.Param;
import com.cobelpvp.queue.bukkit.Queue;
import com.cobelpvp.queue.shared.jedis.JedisAction;
import com.cobelpvp.queue.shared.jedis.JedisChannel;
import com.cobelpvp.queue.shared.queue.QueueRank;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class JoinQueueCommand {

    @Command(names = {"joinqueue"}, permission = "")
    public static void joinQueueCommand(Player sender, @Param(name = "server") String server) {
        com.cobelpvp.queue.shared.queue.Queue queue = com.cobelpvp.queue.shared.queue.Queue.getByPlayer(sender.getUniqueId());

        if (queue != null) {
            sender.sendMessage(ChatColor.RED + "You cannot join to another queue, because you already in a queue.");
            return;
        }

        queue = com.cobelpvp.queue.shared.queue.Queue.getByName(server);

        if (queue == null) {
            sender.sendMessage(ChatColor.RED + "Could not found this queue please try with another.");
            return;
        }

        if (!queue.isEnabled()) {
            sender.sendMessage(ChatColor.RED + "This queue is not currently open, please try again later.");
            return;
        }

        QueueRank queueRank = Queue.getInstance().getPriorityProvider().getPriority(sender);

        JsonObject rank = new JsonObject();
        rank.addProperty("name", queueRank.getName());
        rank.addProperty("priority", queueRank.getPriority());

        JsonObject player = new JsonObject();
        player.addProperty("uuid", sender.getUniqueId().toString());
        player.add("rank", rank);

        JsonObject data = new JsonObject();
        data.addProperty("queue", queue.getName());
        data.add("player", player);

        Queue.getInstance().getPublisher().write(JedisChannel.INDEPENDENT, JedisAction.ADD_PLAYER, data);

        sender.sendMessage(ChatColor.GREEN + "You have been added to " + queue.getName() + " queue!");
    }
}
