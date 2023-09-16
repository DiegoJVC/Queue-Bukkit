package com.cobelpvp.queue.independent;

import com.cobelpvp.queue.independent.jedis.PortalSubscriptionHandler;
import com.cobelpvp.queue.independent.thread.BroadcastThread;
import com.cobelpvp.queue.independent.thread.QueueThread;
import com.cobelpvp.queue.independent.file.Config;
import com.cobelpvp.queue.shared.jedis.JedisChannel;
import com.cobelpvp.queue.shared.jedis.JedisPublisher;
import com.cobelpvp.queue.shared.jedis.JedisSettings;
import com.cobelpvp.queue.shared.jedis.JedisSubscriber;
import com.cobelpvp.queue.shared.queue.Queue;

import lombok.Getter;

@Getter
public class cQueue {

    @Getter
    private static cQueue instance;

    private Config config;

    private JedisSubscriber subscriber;
    private JedisPublisher publisher;

    private cQueue() {
        this.config = new Config();

        for (String name : this.config.getQueues()) {
            Queue.getQueues().add(new Queue(name));
            System.out.println("[cQueue] " + name + " QUEUE HAS LOADED!");
        }

        JedisSettings settings = new JedisSettings(
                this.config.getRedisHost(),
                this.config.getRedisPort(),
                this.config.getRedisPassword().equals("") ? null : this.config.getRedisPassword()
        );

        this.subscriber = new JedisSubscriber(JedisChannel.INDEPENDENT, settings, new PortalSubscriptionHandler());
        this.publisher = new JedisPublisher(settings);
        this.publisher.start();

        new QueueThread().start();
        new BroadcastThread().start();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                if (!settings.getJedisPool().isClosed()) {
                    settings.getJedisPool().close();
                }
            }
        });
    }

    public static void main() {
        instance = new cQueue();
    }

}
