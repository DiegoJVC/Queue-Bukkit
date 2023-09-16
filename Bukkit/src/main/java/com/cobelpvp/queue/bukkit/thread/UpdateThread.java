package com.cobelpvp.queue.bukkit.thread;

import com.cobelpvp.queue.bukkit.Queue;
import com.cobelpvp.queue.shared.jedis.JedisAction;
import com.cobelpvp.queue.shared.jedis.JedisChannel;

public class UpdateThread extends Thread {

    @Override
    public void run() {
        while (true) {
            Queue.getInstance().getPublisher().write(JedisChannel.INDEPENDENT, JedisAction.UPDATE, Queue.getInstance().getPortalServer().getServerData());
            Queue.getInstance().getPublisher().write(JedisChannel.BUKKIT, JedisAction.UPDATE, Queue.getInstance().getPortalServer().getServerData());

            try {
                Thread.sleep(2500L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
