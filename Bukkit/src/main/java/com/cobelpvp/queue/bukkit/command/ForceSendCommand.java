package com.cobelpvp.queue.bukkit.command;

import com.google.gson.JsonObject;
import com.cobelpvp.atheneum.command.Command;
import com.cobelpvp.atheneum.command.Param;
import com.cobelpvp.queue.bukkit.Queue;
import com.cobelpvp.queue.shared.jedis.JedisAction;
import com.cobelpvp.queue.shared.jedis.JedisChannel;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ForceSendCommand {

    @Command(names = {"forcesend"}, permission = "op")
    public static void forceSendCommand(CommandSender sender, @Param(name = "target") Player target, @Param(name = "server") String server) {
        JsonObject json = new JsonObject();
        json.addProperty("username", target.getName());
        json.addProperty("server", server);

        Queue.getInstance().getPublisher().write(JedisChannel.BUKKIT, JedisAction.SEND_PLAYER_SERVER, json);

        sender.sendMessage(ChatColor.GREEN + "If a player with that username is online, they will be sent to `" + target.getName() + "`.");
    }
}
