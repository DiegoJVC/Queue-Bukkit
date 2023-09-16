package com.cobelpvp.queue.independent.jedis;

import com.google.gson.JsonObject;

import com.cobelpvp.queue.independent.cQueue;
import com.cobelpvp.queue.independent.log.Logger;
import com.cobelpvp.queue.shared.jedis.JedisAction;
import com.cobelpvp.queue.shared.jedis.JedisChannel;
import com.cobelpvp.queue.shared.jedis.JedisSubscriptionHandler;
import com.cobelpvp.queue.shared.queue.Queue;
import com.cobelpvp.queue.shared.queue.QueuePlayer;
import com.cobelpvp.queue.shared.queue.QueueRank;
import com.cobelpvp.queue.shared.server.ServerData;

import java.util.Iterator;
import java.util.UUID;

public class PortalSubscriptionHandler implements JedisSubscriptionHandler {

    public void handleMessage(JsonObject json) {
        JedisAction action = JedisAction.valueOf(json.get("action").getAsString());
        JsonObject data = json.get("data").isJsonNull() ? null : json.get("data").getAsJsonObject();

        if (data == null) {
            return;
        }

        switch (action) {
            case UPDATE: {
                String name = data.get("name").getAsString();
                ServerData serverData = ServerData.getByName(name);
                Queue queue = Queue.getByName(name);

                if (serverData == null) {
                    if (queue != null) {
                        queue.setEnabled(true);

                        Logger.print("Initiated queue `" + name + "`");
                    }

                    serverData = new ServerData(name);

                    Logger.print("Initiated server data `" + name + "`");
                }

                serverData.setOnlinePlayers(data.get("online-players").getAsInt());
                serverData.setMaximumPlayers(data.get("maximum-players").getAsInt());
                serverData.setWhitelisted(data.get("whitelisted").getAsBoolean());
                serverData.setLastUpdate(System.currentTimeMillis());
            }
            break;
            case CLEAR_PLAYERS: {
                Queue queue = Queue.getByName(data.get("queue").getAsString());

                if (queue == null) {
                    return;
                }

                queue.getPlayers().clear();

                Logger.print("Cleared players of `" + queue.getName() + "`");
            }
            break;
            case TOGGLE: {
                Queue queue = Queue.getByName(data.get("queue").getAsString());

                if (queue == null) {
                    return;
                }

                queue.setEnabled(!queue.isEnabled());

                Logger.print("Changed status of `" + queue.getName() + "` to " + queue.isEnabled());
            }
            break;
            case ADD_PLAYER: {
                Queue queue = Queue.getByName(data.get("queue").getAsString());

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
                queuePlayer.setInserted(System.currentTimeMillis());

                queue.getPlayers().add(queuePlayer);

                player.addProperty("inserted", queuePlayer.getInserted());

                data.add("player", player);

                cQueue.getInstance().getPublisher().write(JedisChannel.BUKKIT, JedisAction.ADDED_PLAYER, data);
            }
            break;
            case REMOVE_PLAYER: {
                UUID uuid = UUID.fromString(data.get("uuid").getAsString());

                Queue queue = Queue.getByPlayer(uuid);

                if (queue == null) {
                    return;
                }

                QueuePlayer queuePlayer = null;

                Iterator<QueuePlayer> iterator = queue.getPlayers().iterator();

                while (iterator.hasNext()) {
                    QueuePlayer other = iterator.next();

                    if (other.getUuid().equals(uuid)) {
                        queuePlayer = other;

                        iterator.remove();
                    }
                }

                if (queuePlayer == null) {
                    return;
                }

                JsonObject rank = new JsonObject();
                rank.addProperty("name", queuePlayer.getRank().getName());
                rank.addProperty("priority", queuePlayer.getRank().getPriority());

                JsonObject player = new JsonObject();
                player.addProperty("uuid", queuePlayer.getUuid().toString());
                player.addProperty("inserted", queuePlayer.getInserted());
                player.add("rank", rank);

                JsonObject responseData = new JsonObject();
                responseData.addProperty("queue", queue.getName());
                responseData.add("player", player);

                cQueue.getInstance().getPublisher().write(JedisChannel.BUKKIT, JedisAction.REMOVED_PLAYER, responseData);
            }
            break;
            case SEND_PLAYER_HUB: {
                String uuid = data.get("uuid").getAsString();

                ServerData hub = null;

                for (String loopHub : cQueue.getInstance().getConfig().getHubs()) {
                    ServerData serverData = ServerData.getByName(loopHub);

                    if (serverData != null) {
                        if (hub == null || serverData.getOnlinePlayers() < hub.getOnlinePlayers()) {
                            hub = serverData;
                        }
                    }
                }

                if (hub == null) {
                    JsonObject responseData = new JsonObject();
                    responseData.addProperty("uuid", uuid);
                    responseData.addProperty("message", "&cThere are no hubs to send you to.");

                    cQueue.getInstance().getPublisher().write(JedisChannel.BUKKIT, JedisAction.MESSAGE_PLAYER,
                            responseData);
                } else {
                    JsonObject responseData = new JsonObject();
                    responseData.addProperty("uuid", uuid);
                    responseData.addProperty("server", hub.getName());

                    cQueue.getInstance().getPublisher().write(JedisChannel.BUKKIT, JedisAction.SEND_PLAYER_SERVER,
                            responseData);
                }
            }
            break;
        }
    }

}
