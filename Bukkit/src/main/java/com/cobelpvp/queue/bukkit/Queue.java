package com.cobelpvp.queue.bukkit;

import com.cobelpvp.queue.bukkit.config.FileConfig;
import com.cobelpvp.queue.bukkit.config.Language;
import com.cobelpvp.queue.bukkit.jedis.PortalSubscriptionHandler;
import com.cobelpvp.queue.bukkit.priority.impl.DefaultPriorityProvider;
import com.cobelpvp.queue.bukkit.server.Server;
import com.cobelpvp.queue.bukkit.thread.ReminderThread;
import com.cobelpvp.queue.bukkit.thread.UpdateThread;
import com.cobelpvp.queue.bukkit.priority.PriorityProvider;
import com.cobelpvp.queue.independent.cQueue;
import com.cobelpvp.queue.shared.jedis.JedisChannel;
import com.cobelpvp.queue.shared.jedis.JedisPublisher;
import com.cobelpvp.queue.shared.jedis.JedisSettings;
import com.cobelpvp.queue.shared.jedis.JedisSubscriber;

import lombok.Getter;
import lombok.Setter;

import com.cobelpvp.atheneum.command.TeamsCommandHandler;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class Queue extends JavaPlugin {

    @Getter
    private static Queue instance;

    private FileConfig mainConfig;
    private Language language;

    private JedisSettings settings;
    private JedisPublisher publisher;
    private JedisSubscriber subscriber;

    private Server portalServer;

    @Setter
    private PriorityProvider priorityProvider;

    public PriorityProvider getPriorityProvider() {
        return priorityProvider;
    }

    public static Queue getInstance() {
        return instance;
    }

    public FileConfig getMainConfig() {
        return mainConfig;
    }

    public Language getLanguage() {
        return language;
    }

    public JedisSettings getSettings() {
        return settings;
    }

    public JedisPublisher getPublisher() {
        return publisher;
    }

    public JedisSubscriber getSubscriber() {
        return subscriber;
    }

    public Server getPortalServer() {
        return portalServer;
    }

    @Override
    public void onEnable() {
        instance = this;

        // config
        this.mainConfig = new FileConfig(this, "config.yml");

        // language
        this.language = new Language();
        this.language.load();

        // portal server
        this.portalServer = new Server(
                this.mainConfig.getConfig().getString("server.id"),
                this.mainConfig.getConfig().getBoolean("server.hub")
        );

        // redis settings
        this.settings = new JedisSettings(
                this.mainConfig.getConfig().getString("redis.host"),
                this.mainConfig.getConfig().getInt("redis.port"),
                !this.mainConfig.getConfig().contains("redis.password") ? null : this.mainConfig.getConfig().getString("redis.password")
        );

        // redis start
        this.subscriber = new JedisSubscriber(JedisChannel.BUKKIT, this.settings, new PortalSubscriptionHandler());
        this.publisher = new JedisPublisher(this.settings);
        this.publisher.start();

        // priority start
        this.priorityProvider = new DefaultPriorityProvider();

        // Start threads
        new UpdateThread().start();
        new ReminderThread().start();

        // Register commands
        TeamsCommandHandler.registerAll(this);

        // Register plugin message channels
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        cQueue.main();
    }

    @Override
    public void onDisable() {
        if (this.settings.getJedisPool() != null && !this.settings.getJedisPool().isClosed()) {
            this.settings.getJedisPool().close();
        }
    }
}
