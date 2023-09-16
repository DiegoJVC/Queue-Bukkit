package com.cobelpvp.queue.bukkit.server;

import com.google.gson.JsonObject;
import com.cobelpvp.queue.bukkit.Queue;
import lombok.Getter;

@Getter
public class Server {

    private String name;
    private boolean hub;

    public String getName() {
        return name;
    }

    public boolean isHub() {
        return hub;
    }

    public Server(String name, boolean hub) {
        this.name = name;
        this.hub = hub;
    }

    public JsonObject getServerData() {
        JsonObject object = new JsonObject();
        object.addProperty("name", this.name);
        object.addProperty("online-players", Queue.getInstance().getServer().getOnlinePlayers().size());
        object.addProperty("maximum-players", Queue.getInstance().getServer().getMaxPlayers());
        object.addProperty("whitelisted", Queue.getInstance().getServer().hasWhitelist());
        return object;
    }

}
