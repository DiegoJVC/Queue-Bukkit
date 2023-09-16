package com.cobelpvp.queue.bukkit.jedis;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import com.cobelpvp.queue.bukkit.Queue;
import com.cobelpvp.queue.bukkit.util.BungeeUtil;
import com.cobelpvp.queue.independent.log.Logger;
import com.cobelpvp.queue.shared.jedis.JedisSubscriptionHandler;
import com.cobelpvp.queue.shared.jedis.JedisAction;
import com.cobelpvp.queue.shared.queue.QueuePlayer;
import com.cobelpvp.queue.shared.queue.QueuePlayerComparator;
import com.cobelpvp.queue.shared.queue.QueueRank;
import com.cobelpvp.queue.shared.server.ServerData;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.PriorityQueue;
import java.util.UUID;

public class PortalSubscriptionHandler implements JedisSubscriptionHandler {

    @Override
    public void handleMessage(JsonObject json) {
        JedisAction action = JedisAction.valueOf(json.get("action").getAsString());
        JsonObject data = json.get("data").isJsonNull() ? null : json.get("data").getAsJsonObject();

        if (data == null) {
            return;
        }

        switch (action) {
            case UPDATE: {
                final String name = data.get("name").getAsString();

                if (!Queue.getInstance().getPortalServer().isHub()) {
                    return;
                }

                ServerData serverData = ServerData.getByName(name);

                if (serverData == null) {
                    serverData = new ServerData(name);
                }

                serverData.setOnlinePlayers(data.get("online-players").getAsInt());
                serverData.setMaximumPlayers(data.get("maximum-players").getAsInt());
                serverData.setWhitelisted(data.get("whitelisted").getAsBoolean());
                serverData.setLastUpdate(System.currentTimeMillis());
            }
            break;
            case LIST: {
                if (!Queue.getInstance().getPortalServer().isHub()) {
                    return;
                }

                for (JsonElement e : data.get("queues").getAsJsonArray()) {
                    JsonObject queueJson = e.getAsJsonObject();
                    String name = queueJson.get("name").getAsString();

                    com.cobelpvp.queue.shared.queue.Queue queue = com.cobelpvp.queue.shared.queue.Queue.getByName(name);

                    if (queue == null) {
                        queue = new com.cobelpvp.queue.shared.queue.Queue(name);
                    }

                    PriorityQueue<QueuePlayer> players = new PriorityQueue<>(new QueuePlayerComparator());

                    for (JsonElement pe : queueJson.get("players").getAsJsonArray()) {
                        JsonObject player = pe.getAsJsonObject();
                        JsonObject rank = player.get("rank").getAsJsonObject();

                        QueueRank queueRank = new QueueRank();

                        queueRank.setName(rank.get("name").getAsString());
                        queueRank.setPriority(rank.get("priority").getAsInt());

                        QueuePlayer queuePlayer = new QueuePlayer();

                        queuePlayer.setUuid(UUID.fromString(player.get("uuid").getAsString()));
                        queuePlayer.setRank(queueRank);
                        queuePlayer.setInserted(player.get("inserted").getAsLong());

                        players.add(queuePlayer);
                    }

                    queue.setPlayers(players);
                    queue.setEnabled(queueJson.get("status").getAsBoolean());
                }
            }
            break;
            case TOGGLE: {
                com.cobelpvp.queue.shared.queue.Queue queue = com.cobelpvp.queue.shared.queue.Queue.getByName(data.get("queue").getAsString());

                if (queue == null) {
                    return;
                }

                queue.setEnabled(!queue.isEnabled());

                Logger.print("Changed status of `" + queue.getName() + "` to " + queue.isEnabled());
            }
            break;
            case ADDED_PLAYER: {
                com.cobelpvp.queue.shared.queue.Queue queue = com.cobelpvp.queue.shared.queue.Queue.getByName(data.get("queue").getAsString());

                if (queue == null) {
                    return;
                }

                if (!queue.isEnabled()) {
                    return;
                }

                JsonObject player = data.get("player").getAsJsonObject();
                JsonObject rank = player.get("rank").getAsJsonObject();

                QueueRank queueRank = new QueueRank();
                queueRank.setName(rank.get("name").getAsString());
                queueRank.setPriority(rank.get("priority").getAsInt());

                QueuePlayer queuePlayer = new QueuePlayer();
                queuePlayer.setUuid(UUID.fromString(player.get("uuid").getAsString()));
                queuePlayer.setRank(queueRank);
                queuePlayer.setInserted(player.get("inserted").getAsLong());
            }
            break;
            case REMOVED_PLAYER: {
                com.cobelpvp.queue.shared.queue.Queue queue = com.cobelpvp.queue.shared.queue.Queue.getByName(data.get("queue").getAsString());

                if (queue == null) {
                    return;
                }

                UUID uuid = UUID.fromString(data.get("player").getAsJsonObject().get("uuid").getAsString());

                queue.getPlayers().removeIf(queuePlayer -> queuePlayer.getUuid().equals(uuid));
            }
            break;
            case SEND_PLAYER_SERVER: {
                String server = data.get("server").getAsString();

                Player player;

                if (data.has("username")) {
                    player = Queue.getInstance().getServer().getPlayer(data.get("username").getAsString());
                } else {
                    player = Queue.getInstance().getServer().getPlayer(UUID.fromString(data.get("uuid").getAsString()));
                }

                if (player == null) {
                    return;
                }

                BungeeUtil.sendToServer(player, server);
            }
            break;
            case MESSAGE_PLAYER: {
                Player player = Queue.getInstance().getServer().getPlayer(data.get("uuid").getAsString());

                if (player == null) {
                    return;
                }

                player.sendMessage(ChatColor.translateAlternateColorCodes('&', data.get("message").getAsString()));
            }
            break;
        }
    }

}
