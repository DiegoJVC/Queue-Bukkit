package com.cobelpvp.queue.independent.thread;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.cobelpvp.queue.independent.cQueue;
import com.cobelpvp.queue.shared.jedis.JedisAction;
import com.cobelpvp.queue.shared.jedis.JedisChannel;
import com.cobelpvp.queue.shared.queue.Queue;
import com.cobelpvp.queue.shared.queue.QueuePlayer;

public class BroadcastThread extends Thread {

    @Override
    public void run() {
        while (true) {
            JsonArray queues = new JsonArray();

            for (Queue queue : Queue.getQueues()) {
                JsonArray players = new JsonArray();

                for (QueuePlayer player : queue.getPlayers()) {
                    JsonObject rank = new JsonObject();
                    rank.addProperty("name", player.getRank().getName());
                    rank.addProperty("priority", player.getRank().getPriority());

                    JsonObject json = new JsonObject();
                    json.addProperty("uuid", player.getUuid().toString());
                    json.addProperty("inserted", player.getInserted());
                    json.add("rank", rank);

                    players.add(json);
                }

                JsonObject json = new JsonObject();
                json.addProperty("name", queue.getName());
                json.addProperty("status", queue.isEnabled());
                json.add("players", players);
            }

            JsonObject json = new JsonObject();

            json.add("queues", queues);

            try {
                cQueue.getInstance().getPublisher().write(JedisChannel.BUKKIT, JedisAction.LIST, json);
            } catch (Exception e) {
                System.out.println("[cQueue-bukkit] WARNING: error caused while publish jedis action. JedisAction.LIST");
            }

            try {
                Thread.sleep(5000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
