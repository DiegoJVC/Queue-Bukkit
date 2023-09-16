package com.cobelpvp.queue.bukkit.command;

import com.google.gson.JsonObject;
import com.cobelpvp.atheneum.command.Command;
import com.cobelpvp.atheneum.command.Param;
import com.cobelpvp.queue.bukkit.Queue;
import com.cobelpvp.queue.shared.jedis.JedisAction;
import com.cobelpvp.queue.shared.jedis.JedisChannel;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ClearQueueCommand {

    @Command(names = {"clearqueue"}, permission = "op")
    public static void queueClearCommand(Player sender, @Param(name = "server") String server) {
        com.cobelpvp.queue.shared.queue.Queue queue = com.cobelpvp.queue.shared.queue.Queue.getByName(server);

        if (queue == null) {
            sender.sendMessage(ChatColor.RED + "Could not found this queue please try with another.");
            return;
        }

        queue.getPlayers().clear();

        JsonObject json = new JsonObject();
        json.addProperty("queue", queue.getName());

        Queue.getInstance().getPublisher().write(JedisChannel.BUKKIT, JedisAction.CLEAR_PLAYERS, json);

        sender.sendMessage(ChatColor.GOLD + "Cleared list of " + queue.getName());
    }
}
