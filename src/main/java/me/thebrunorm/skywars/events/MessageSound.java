/* (C) 2021 Bruno */
package me.thebrunorm.skywars.events;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import me.thebrunorm.skywars.Skywars;
import me.thebrunorm.skywars.singletons.SkywarsUtils;

public class MessageSound implements Listener {

	@EventHandler
	void onMessage(AsyncPlayerChatEvent event) {
		final String soundName = Skywars.get().getConfig().getString("messageSounds.sound");
		if (soundName == null || soundName.isEmpty())
			return;
		// playSound acepta alias clasicos (ITEM_PICKUP) y nombres modernos (ENTITY_ITEM_PICKUP).
		final float random = (float) (Math.random() + 0.5f);
		for (final Player player : Bukkit.getOnlinePlayers()) {
			SkywarsUtils.playSound(player, soundName + "; 1; " + random);
		}
	}

}
