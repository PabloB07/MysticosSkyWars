// Copyright (c) 2025 Bruno
package me.thebrunorm.skywars.luckyblock;

import me.thebrunorm.skywars.Skywars;
import me.thebrunorm.skywars.enums.ArenaStatus;
import me.thebrunorm.skywars.structures.Arena;
import me.thebrunorm.skywars.structures.SkywarsUser;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Detecta la rotura de LuckyBlocks dentro de arenas y dispara la sorpresa.
 */
public class LuckyBlockListener implements Listener {

	private LuckyBlockManager manager() {
		return Skywars.get().getLuckyBlockManager();
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onPlace(BlockPlaceEvent event) {
		final LuckyBlockManager manager = manager();
		if (manager == null || !manager.isEnabled())
			return;
		final ItemStack hand = event.getItemInHand();
		if (!manager.isLuckyItem(hand))
			return;
		manager.track(event.getBlockPlaced().getLocation());
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onBreak(BlockBreakEvent event) {
		final LuckyBlockManager manager = manager();
		if (manager == null || !manager.isEnabled())
			return;

		final Player player = event.getPlayer();
		final Arena arena = Skywars.get().getPlayerArena(player);
		if (arena == null || arena.getStatus() != ArenaStatus.PLAYING)
			return;
		final SkywarsUser user = arena.getUser(player);
		if (user == null || user.isSpectator())
			return;

		final Block block = event.getBlock();
		final Location loc = block.getLocation();
		if (!manager.isLuckyBlockAt(loc))
			return;
		if (block.getType() != manager.getBlockMaterial()) {
			// La posicion registrada ya no tiene un LuckyBlock (explosion,
			// mapa editado...): no tocar nada y mantener el registro.
			return;
		}

		// Consumir el bloque sin drops de vainilla y disparar la sorpresa.
		manager.consume(loc);
		event.setCancelled(true);
		block.setType(Material.AIR, false);

		manager.trigger(player, loc);
	}
}
