package com.cobelpvp.queue.independent.thread;

import com.google.gson.JsonObject;
import com.cobelpvp.queue.independent.cQueue;
import com.cobelpvp.queue.shared.jedis.JedisChannel;
import com.cobelpvp.queue.shared.queue.Queue;
import com.cobelpvp.queue.shared.queue.QueuePlayer;
import com.cobelpvp.queue.shared.jedis.JedisAction;
import com.cobelpvp.queue.shared.server.ServerData;

public class QueueThread extends Thread {

    private static final Long SEND_DELAY = 500L;

    @Override
    public void run() {
        while (true) {
            for (Queue queue : Queue.getQueues()) {
                ServerData serverData = queue.getServerData();

                if (serverData == null) {
                    continue;
                }

                if (!queue.isEnabled()) {
                    continue;
                }

                if (!serverData.isOnline()) {
                    continue;
                }

                if (serverData.isWhitelisted()) {
                    continue;
                }

                if (serverData.getOnlinePlayers() >= serverData.getMaximumPlayers()) {
                    continue;
                }

                QueuePlayer next = queue.getPlayers().poll();

                if (next != null) {
                    JsonObject data = new JsonObject();
                    data.addProperty("server", queue.getName());
                    data.addProperty("uuid", next.getUuid().toString());

                    cQueue.getInstance().getPublisher().write(JedisChannel.BUKKIT, JedisAction.SEND_PLAYER_SERVER, data);
                }
            }

            try {
                Thread.sleep(SEND_DELAY);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
