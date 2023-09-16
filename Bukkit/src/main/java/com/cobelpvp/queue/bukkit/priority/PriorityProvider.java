package com.cobelpvp.queue.bukkit.priority;

import com.cobelpvp.queue.shared.queue.QueueRank;
import org.bukkit.entity.Player;

public interface PriorityProvider {

    QueueRank getPriority(Player player);

}
