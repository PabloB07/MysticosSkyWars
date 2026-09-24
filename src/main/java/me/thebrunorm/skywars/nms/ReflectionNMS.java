// Copyright (c) 2025 Bruno
package me.thebrunorm.skywars.nms;

import me.thebrunorm.skywars.Skywars;
import me.thebrunorm.skywars.singletons.MessageUtils;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

/**
 * Implementacion moderna del puente NMS usando solo la API de Bukkit/Paper.
 *
 * <p>En Paper 1.21.x los paquetes NMS versionados
 * ({@code org.bukkit.craftbukkit.v1_XX_RY}) ya no existen, asi que la vieja
 * implementacion por reflexion dejaba de funcionar. Esta clase mantiene las
 * mismas firmas ({@link NMS}) pero delega en los metodos modernos:
 * {@link Player#sendTitle}, actionbar por {@code spigot()} y
 * {@link Player#setPlayerListHeaderFooter}.</p>
 */
public class ReflectionNMS implements NMS {

	String prefix = "&7[&cMSkyWars-NMS-Debug&7]";

	public void sendParticles(Location loc, String particle, int amount) {
		if (loc == null || loc.getWorld() == null)
			return;
		try {
			final Particle effect = Particle.valueOf(particle);
			loc.getWorld().spawnParticle(effect, loc, Math.max(0, amount));
		} catch (final IllegalArgumentException e) {
			Skywars.get().sendDebugMessage("Could not spawn particles: unknown particle %s", particle);
		} catch (final Exception e) {
			Skywars.get().sendDebugMessage("Could not spawn particles.");
		}
	}

	public void sendParticles(Player player, String particle, int amount) {
		if (player == null)
			return;
		this.sendParticles(player.getLocation(), particle, amount);
	}

	@Override
	public void sendParticles(Player player, Location loc, String particle, int amount) {
		if (player == null)
			return;
		try {
			final Particle effect = Particle.valueOf(particle);
			player.spawnParticle(effect, loc, Math.max(0, amount));
		} catch (final IllegalArgumentException e) {
			this.sendParticles(loc, particle, amount);
		} catch (final Exception e) {
			Skywars.get().sendDebugMessage("Could not spawn particles.");
		}
	}

	@Override
	public void sendActionbar(Player player, String text) {
		if (player == null)
			return;
		try {
			player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
					TextComponent.fromLegacy(MessageUtils.color(text)));
		} catch (final Exception e) {
			Skywars.get().sendDebugMessage("Could not send actionbar.");
		}
	}

	@Override
	public void sendTitle(Player player, String title) {
		this.sendTitle(player, title, "");
	}

	@Override
	public void sendTitle(Player player, String title, String subtitle) {
		this.sendTitle(player, title, subtitle, 10, 70, 20);
	}

	@Override
	public void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
		if (player == null)
			return;
		try {
			player.sendTitle(MessageUtils.color(title), MessageUtils.color(subtitle), fadeIn, stay, fadeOut);
		} catch (final Exception e) {
			Skywars.get().sendDebugMessage("Could not send title.");
		}
	}

	public void sendTablist(Player player, String header, String footer) {
		if (player == null)
			return;
		try {
			player.setPlayerListHeaderFooter(MessageUtils.color(header), MessageUtils.color(footer));
		} catch (final Exception e) {
			Skywars.get().sendDebugMessage("Could not send tablist.");
		}
	}

	public void sendPacket(Player player, Object packet) {
		// Sin soporte: la API moderna no necesita paquetes crudos.
		Skywars.get().sendDebugMessage("sendPacket() ya no esta soportado en Paper 1.21.x.");
	}
}
