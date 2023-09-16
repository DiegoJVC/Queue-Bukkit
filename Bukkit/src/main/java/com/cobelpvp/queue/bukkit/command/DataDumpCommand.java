package com.cobelpvp.queue.bukkit.command;

import com.cobelpvp.atheneum.command.Command;
import com.cobelpvp.queue.shared.queue.Queue;
import com.cobelpvp.queue.shared.server.ServerData;
import org.bukkit.entity.Player;

public class DataDumpCommand {

    @Command(names = {"datadump"}, permission = "op")
    public static void dataDumpCommand(Player sender) {
        sender.sendMessage("Servers:");
        for (ServerData serverData : ServerData.getServers()) {
            StringBuilder builder = new StringBuilder("- ")
                    .append(serverData.getName())
                    .append(" (")
                    .append(serverData.isOnline())
                    .append(") (")
                    .append(serverData.getOnlinePlayers())
                    .append("/")
                    .append(serverData.getMaximumPlayers())
                    .append(")");

            sender.sendMessage(builder.toString());
        }
        sender.sendMessage("Queues:");
        for (Queue queue : Queue.getQueues()) {
            StringBuilder builder = new StringBuilder("- ")
                    .append(queue.getName())
                    .append(" (")
                    .append(queue.getPlayers().size())
                    .append(" in queue)");

            ServerData serverData = queue.getServerData();

            if (serverData == null) {
                builder
                        .append(" (offline)");
            } else {
                builder
                        .append(" (")
                        .append(serverData.isOnline())
                        .append(") (")
                        .append(serverData.isWhitelisted())
                        .append(") (")
                        .append(serverData.getOnlinePlayers())
                        .append("/")
                        .append(serverData.getMaximumPlayers())
                        .append(")");
            }

            builder.append(" (Enabled: " + (queue.isEnabled() ? "Yes" : "No") + ")");

            sender.sendMessage(builder.toString());
        }
    }
}
