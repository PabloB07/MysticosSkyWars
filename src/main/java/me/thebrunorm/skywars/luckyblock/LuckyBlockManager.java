// Copyright (c) 2025 Bruno
package me.thebrunorm.skywars.luckyblock;

import me.thebrunorm.skywars.Skywars;
import me.thebrunorm.skywars.singletons.ConfigurationUtils;
import me.thebrunorm.skywars.singletons.MessageUtils;
import me.thebrunorm.skywars.singletons.SkywarsUtils;
import me.thebrunorm.skywars.structures.Arena;
import me.thebrunorm.skywars.structures.SkywarsUser;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Gestiona los LuckyBlocks de MSkyWars: item, colocacion y sorpresas.
 */
public class LuckyBlockManager {

	public static final String PDC_KEY = "mskywars-luckyblock";

	private final Set<String> placedBlocks = ConcurrentHashMap.newKeySet();
	private final Map<LuckyBlockReward.Category, Boolean> enabledCategories = new HashMap<>();

	private boolean enabled = true;
	private boolean announceTitle = true;
	private boolean broadcastEpic = true;
	private Material blockMaterial = Material.SPONGE;
	private String itemName = "&e&lLuckyBlock";
	private List<String> itemLore = new ArrayList<>();
	private int perIsland = 3;
	private int giveOnStart = 0;

	public void load() {
		final org.bukkit.configuration.file.YamlConfiguration config =
				ConfigurationUtils.loadConfiguration("luckyblocks.yml", "luckyblocks.yml");

		this.enabled = config.getBoolean("categories.ITEMS", true)
				|| config.getBoolean("categories.MOBS", true)
				|| config.getBoolean("categories.EFFECTS", true)
				|| config.getBoolean("categories.BLOCKS", true)
				|| config.getBoolean("categories.TNT", true)
				|| config.getBoolean("categories.TRAPS", true)
				|| config.getBoolean("categories.SPECIAL", true);

		for (final LuckyBlockReward.Category category : LuckyBlockReward.Category.values()) {
			this.enabledCategories.put(category, config.getBoolean("categories." + category.name(), true));
		}

		final Material configured = Material.matchMaterial(config.getString("block", "SPONGE"));
		if (configured != null && configured.isBlock())
			this.blockMaterial = configured;

		this.itemName = config.getString("name", "&e&lLuckyBlock");
		this.itemLore = config.getStringList("lore");
		this.announceTitle = config.getBoolean("announce_title", true);
		this.broadcastEpic = config.getBoolean("broadcast_epic", true);

		this.perIsland = Skywars.get().getConfig().getInt("luckyblocks.per_island", 3);
		this.giveOnStart = Skywars.get().getConfig().getInt("luckyblocks.give_on_start", 0);
		if (!Skywars.get().getConfig().getBoolean("luckyblocks.enabled", true))
			this.enabled = false;

		Skywars.get().sendDebugMessage("LuckyBlocks: %s (material=%s, por isla=%s)",
				this.enabled ? "activados" : "desactivados", this.blockMaterial, this.perIsland);
	}

	public boolean isEnabled() {
		return this.enabled;
	}

	public Material getBlockMaterial() {
		return this.blockMaterial;
	}

	public int getGiveOnStart() {
		return this.giveOnStart;
	}

	// ---------- item ----------

	public NamespacedKey key() {
		return new NamespacedKey(Skywars.get(), PDC_KEY);
	}

	public ItemStack createItem(int amount) {
		final ItemStack item = new ItemStack(this.blockMaterial, Math.max(1, amount));
		final ItemMeta meta = item.getItemMeta();
		if (meta != null) {
			meta.setDisplayName(MessageUtils.color(this.itemName));
			final List<String> lore = new ArrayList<>();
			for (final String line : this.itemLore)
				lore.add(MessageUtils.color(line));
			if (!lore.isEmpty())
				meta.setLore(lore);
			meta.getPersistentDataContainer().set(key(), PersistentDataType.BYTE, (byte) 1);
			item.setItemMeta(meta);
		}
		return item;
	}

	public boolean isLuckyItem(ItemStack item) {
		if (item == null || item.getType() != this.blockMaterial || !item.hasItemMeta())
			return false;
		return item.getItemMeta().getPersistentDataContainer().has(key(), PersistentDataType.BYTE);
	}

	// ---------- bloques colocados ----------

	static String blockKey(Location loc) {
		return loc.getWorld().getName() + ":" + loc.getBlockX() + ":" + loc.getBlockY() + ":" + loc.getBlockZ();
	}

	public void track(Location loc) {
		if (loc == null || loc.getWorld() == null)
			return;
		this.placedBlocks.add(blockKey(loc));
	}

	public boolean isLuckyBlockAt(Location loc) {
		return loc != null && loc.getWorld() != null && this.placedBlocks.contains(blockKey(loc));
	}

	public boolean consume(Location loc) {
		return loc != null && loc.getWorld() != null && this.placedBlocks.remove(blockKey(loc));
	}

	public void untrackWorld(World world) {
		if (world == null)
			return;
		this.placedBlocks.removeIf(key -> key.startsWith(world.getName() + ":"));
	}

	// ---------- generacion en arenas ----------

	private static final int[][] ISLAND_OFFSETS = {
			{2, 1, 0}, {-2, 1, 0}, {0, 1, 2}, {0, 1, -2}, {2, 1, 2}, {-2, 1, -2}
	};

	/**
	 * Activa los LuckyBlocks al empezar la partida.
	 *
	 * <p>Primero usa las posiciones registradas en la config del mapa
	 * (calculadas con /sw calculateluckyblocks o el menu de config,
	 * igual que los beacons para los spawns). Los bloques ya existen en
	 * la copia del mundo, solo se registran para detectar su rotura.</p>
	 *
	 * <p>Si el mapa no tiene posiciones registradas, genera bloques
	 * automaticamente en cada isla (luckyblocks.per_island).</p>
	 */
	public void spawnInArena(Arena arena) {
		if (!this.enabled || arena == null || arena.getWorld() == null)
			return;

		final HashMap<Integer, org.bukkit.util.Vector> registered = arena.getMap().getLuckyBlocks();
		if (registered != null && !registered.isEmpty()) {
			int tracked = 0;
			for (final org.bukkit.util.Vector vec : registered.values()) {
				final Location loc = arena.getVectorInArena(vec);
				if (loc == null || loc.getWorld() == null)
					continue;
				if (loc.getBlock().getType() != this.blockMaterial) {
					// Auto-reparacion: la copia del mundo no trae el bloque
					// (backup desactualizado o bloque consumido); el registro
					// manda y se coloca de nuevo.
					loc.getBlock().setType(this.blockMaterial, false);
					Skywars.get().sendDebugMessage(
							"Placed missing registered luckyblock for map %s at %s",
							arena.getMap().getName(), loc.toVector());
				}
				track(loc);
				tracked++;
			}
			Skywars.get().sendDebugMessage("Tracking %s registered luckyblocks for map %s", tracked,
					arena.getMap().getName());
			return;
		}

		final int count = Math.max(0, Math.min(this.perIsland, ISLAND_OFFSETS.length));
		for (final org.bukkit.util.Vector spawn : arena.getMap().getSpawns().values()) {
			final Location base = arena.getVectorInArena(spawn);
			if (base == null || base.getWorld() == null)
				continue;
			int placed = 0;
			for (int i = 0; i < ISLAND_OFFSETS.length && placed < count; i++) {
				final Location loc = findAirSpot(base, ISLAND_OFFSETS[i][0], ISLAND_OFFSETS[i][1], ISLAND_OFFSETS[i][2]);
				if (loc == null)
					continue;
				loc.getBlock().setType(this.blockMaterial, false);
				track(loc);
				placed++;
			}
		}
	}

	private Location findAirSpot(Location base, int dx, int dy, int dz) {
		Location loc = base.clone().add(dx, dy, dz);
		for (int i = 0; i < 6; i++) {
			final Block block = loc.getBlock();
			if (block.getType().isAir()) {
				// no flotando en el vacio: exige algo solido debajo o al lado
				final Block below = loc.clone().add(0, -1, 0).getBlock();
				if (!below.getType().isAir())
					return loc;
				loc = loc.clone().add(0, 1, 0);
				continue;
			}
			loc = loc.clone().add(0, 1, 0);
		}
		return null;
	}

	// ---------- sorpresa ----------

	/** Sortea y ejecuta una sorpresa para el jugador. */
	public void trigger(Player player, Location loc) {
		if (!this.enabled || player == null || loc == null)
			return;

		final List<LuckyBlockReward> pool = new ArrayList<>();
		int total = 0;
		for (final LuckyBlockReward reward : LuckyBlockReward.values()) {
			if (!this.enabledCategories.getOrDefault(reward.category, true))
				continue;
			pool.add(reward);
			total += reward.weight;
		}
		if (pool.isEmpty())
			return;

		int roll = ThreadLocalRandom.current().nextInt(total);
		LuckyBlockReward picked = pool.get(0);
		for (final LuckyBlockReward reward : pool) {
			roll -= reward.weight;
			if (roll < 0) {
				picked = reward;
				break;
			}
		}

		final String rewardName = MessageUtils.get(picked.langKey());
		try {
			picked.apply(player, loc.clone());
		} catch (final Exception e) {
			Skywars.get().getLogger().warning("MSkyWars LuckyBlock fallo la sorpresa " + picked.name() + ": " + e.getMessage());
			return;
		}

		if (this.announceTitle) {
			final String subtitle = switch (picked.mood) {
				case GOOD -> MessageUtils.get("luckyblock.mood_good");
				case BAD -> MessageUtils.get("luckyblock.mood_bad");
				case EPIC -> MessageUtils.get("luckyblock.mood_epic");
				default -> MessageUtils.get("luckyblock.mood_fun");
			};
			Skywars.get().NMS().sendTitle(player, rewardName, subtitle, 5, 50, 10);
		}
		player.sendMessage(MessageUtils.get("luckyblock.opened", rewardName));

		final String soundPath = switch (picked.mood) {
			case BAD -> "sounds.LUCKYBLOCK_BAD";
			case GOOD, EPIC -> "sounds.LUCKYBLOCK_GOOD";
			default -> "sounds.LUCKYBLOCK_MOB";
		};
		SkywarsUtils.playSoundsFromConfig(player, soundPath);

		if (picked.mood == LuckyBlockReward.Mood.EPIC && this.broadcastEpic) {
			final Arena arena = Skywars.get().getPlayerArena(player);
			if (arena != null)
				arena.broadcastMessage(MessageUtils.get("luckyblock.epic_broadcast", player.getName(), rewardName));
		}
	}

	// ---------- helpers usados por las recompensas ----------

	static void give(Player player, ItemStack... items) {
		final Map<Integer, ItemStack> leftover = player.getInventory().addItem(items);
		for (final ItemStack item : leftover.values()) {
			if (item != null)
				player.getWorld().dropItemNaturally(player.getLocation(), item);
		}
	}

	static ItemStack enchanted(Material material, Enchantment enchantment, int level) {
		final ItemStack item = new ItemStack(material);
		item.addUnsafeEnchantment(enchantment, level);
		return item;
	}

	static void effect(Player player, PotionEffectType type, int ticks, int amplifier) {
		player.addPotionEffect(new PotionEffect(type, ticks, amplifier, false, true, true));
	}

	static boolean isNegative(PotionEffectType type) {
		return type == PotionEffectType.POISON || type == PotionEffectType.WITHER
				|| type == PotionEffectType.SLOWNESS || type == PotionEffectType.BLINDNESS
				|| type == PotionEffectType.NAUSEA || type == PotionEffectType.WEAKNESS
				|| type == PotionEffectType.HUNGER || type == PotionEffectType.LEVITATION;
	}

	static Location safeSpot(Location loc) {
		final Location spot = loc.clone();
		for (int i = 0; i < 4; i++) {
			if (spot.getBlock().getType().isAir() && spot.clone().add(0, 1, 0).getBlock().getType().isAir())
				return spot;
			spot.add(0, 1, 0);
		}
		return loc.clone();
	}

	static void attacking(Player player, Location loc, EntityType type) {
		final Location spot = safeSpot(loc);
		final Entity entity = spot.getWorld().spawnEntity(spot, type);
		if (entity instanceof Creeper creeper && ThreadLocalRandom.current().nextInt(4) == 0)
			creeper.setPowered(true);
		if (entity instanceof Monster monster)
			monster.setTarget(player);
	}

	static void spawnAllyGolem(Player player, Location loc) {
		final Location spot = safeSpot(loc);
		final IronGolem golem = (IronGolem) spot.getWorld().spawnEntity(spot, EntityType.IRON_GOLEM);
		golem.setPlayerCreated(true);
		golem.setCustomName(MessageUtils.color("&bProtector de " + player.getName()));
		golem.setCustomNameVisible(true);
	}

	static void placeLootChest(Location loc, ItemStack... loot) {
		final Location spot = safeSpot(loc);
		spot.getBlock().setType(Material.CHEST, false);
		if (spot.getBlock().getState() instanceof Chest chest) {
			for (final ItemStack item : loot)
				chest.getBlockInventory().addItem(item);
			chest.update();
		}
	}

	static void buildTower(Location loc, Material... materials) {
		Location base = safeSpot(loc);
		for (int i = 0; i < materials.length; i++) {
			final Block block = base.clone().add(0, i, 0).getBlock();
			if (block.getType().isAir())
				block.setType(materials[i], true);
		}
	}

	static void buildCage(Location center, Material material) {
		for (int x = -1; x <= 1; x++) {
			for (int y = 0; y <= 2; y++) {
				for (int z = -1; z <= 1; z++) {
					final boolean wall = x == -1 || x == 1 || z == -1 || z == 1 || y == 0 || y == 2;
					if (!wall)
						continue;
					final Block block = center.clone().add(x, y, z).getBlock();
					if (block.getType().isAir() || block.getType() == Material.WATER)
						block.setType(material, true);
				}
			}
		}
	}

	static void trapWebs(Location center) {
		final Block feet = center.getBlock();
		if (feet.getType().isAir())
			feet.setType(Material.COBWEB, true);
		for (int i = 0; i < 3; i++) {
			final Location around = center.clone().add(
					ThreadLocalRandom.current().nextInt(-2, 3), 0,
					ThreadLocalRandom.current().nextInt(-2, 3));
			if (around.getBlock().getType().isAir())
				around.getBlock().setType(Material.COBWEB, true);
		}
	}

	static void trapLava(Player player, Location center) {
		player.setFireTicks(100);
		final Block above = center.clone().add(0, 3, 0).getBlock();
		if (above.getType().isAir())
			above.setType(Material.LAVA, true);
	}

	static void openPit(Location center) {
		for (int x = -1; x <= 1; x++) {
			for (int z = -1; z <= 1; z++) {
				for (int y = -1; y >= -5; y--) {
					final Block block = center.clone().add(x, y, z).getBlock();
					if (block.getType() == Material.BEDROCK)
						break;
					block.setType(Material.AIR, false);
				}
			}
		}
	}

	static void igniteTnt(Location loc, int amount, int fuseTicks) {
		final World world = loc.getWorld();
		for (int i = 0; i < amount; i++) {
			final Location spot = safeSpot(loc).add(
					ThreadLocalRandom.current().nextDouble(-1.5, 1.5), 0.5,
					ThreadLocalRandom.current().nextDouble(-1.5, 1.5));
			world.spawn(spot, TNTPrimed.class, tnt -> tnt.setFuseTicks(fuseTicks));
		}
	}

	/** La sorpresa sigue viva solo si el jugador sigue en una arena. */
	static boolean stillPlaying(Player player) {
		return player != null && player.isOnline() && Skywars.get().getPlayerArena(player) != null;
	}

	static void rainTnt(Player player, Location loc, int amount) {
		final World world = loc.getWorld();
		for (int i = 0; i < amount; i++) {
			final int delay = i * 10;
			Bukkit.getScheduler().runTaskLater(Skywars.get(), () -> {
				if (!stillPlaying(player))
					return;
				final Location spot = loc.clone().add(
						ThreadLocalRandom.current().nextDouble(-3, 3), 8,
						ThreadLocalRandom.current().nextDouble(-3, 3));
				world.spawn(spot, TNTPrimed.class, tnt -> tnt.setFuseTicks(30));
			}, delay);
		}
	}

	static void rainAnvils(Player player, Location center, int amount) {
		final World world = center.getWorld();
		for (int i = 0; i < amount; i++) {
			final int delay = i * 8;
			Bukkit.getScheduler().runTaskLater(Skywars.get(), () -> {
				if (!stillPlaying(player))
					return;
				final Location spot = center.clone().add(
						ThreadLocalRandom.current().nextDouble(-2, 2), 9,
						ThreadLocalRandom.current().nextDouble(-2, 2));
				final FallingBlock anvil = world.spawnFallingBlock(spot, Material.ANVIL.createBlockData());
				anvil.setDropItem(false);
				anvil.setHurtEntities(true);
			}, delay);
		}
	}

	static void strike(Player player) {
		final Location loc = player.getLocation();
		loc.getWorld().strikeLightning(loc);
		player.setFireTicks(60);
	}

	static void trollSmoke(Location loc) {
		loc.getWorld().spawnParticle(org.bukkit.Particle.CLOUD, safeSpot(loc).add(0, 1, 0), 40, 0.5, 0.5, 0.5, 0.02);
	}

	static void celebrate(Player player, Location loc, int amount) {
		for (int i = 0; i < amount; i++) {
			final int delay = i * 6;
			Bukkit.getScheduler().runTaskLater(Skywars.get(), () -> {
				if (!stillPlaying(player))
					return;
				SkywarsUtils.spawnRandomFirework(safeSpot(loc).add(
						ThreadLocalRandom.current().nextDouble(-3, 3), 1,
						ThreadLocalRandom.current().nextDouble(-3, 3)));
			}, delay);
		}
	}

	static void wildTeleport(Player player) {
		final Location from = player.getLocation();
		final Location to = from.clone().add(
				ThreadLocalRandom.current().nextDouble(-15, 15),
				ThreadLocalRandom.current().nextDouble(5, 12),
				ThreadLocalRandom.current().nextDouble(-15, 15));
		to.setY(Math.min(to.getWorld().getMaxHeight() - 2, Math.max(1, to.getY())));
		effect(player, PotionEffectType.SLOW_FALLING, 20 * 15, 0);
		player.teleport(to);
	}

	static void swapWithRandom(Player player) {
		final Arena arena = Skywars.get().getPlayerArena(player);
		if (arena == null)
			return;
		final List<SkywarsUser> candidates = arena.getAlivePlayers().stream()
				.filter(user -> !user.getPlayer().equals(player))
				.toList();
		if (candidates.isEmpty()) {
			wildTeleport(player);
			return;
		}
		final Player other = candidates.get(ThreadLocalRandom.current().nextInt(candidates.size())).getPlayer();
		final Location mine = player.getLocation().clone();
		final Location theirs = other.getLocation().clone();
		player.teleport(theirs);
		other.teleport(mine);
		other.sendMessage(MessageUtils.get("luckyblock.swapped", player.getName()));
	}

	static void launch(Player player) {
		player.setVelocity(new Vector(0, 2.2, 0));
		effect(player, PotionEffectType.SLOW_FALLING, 20 * 12, 0);
	}

	/** Limpia los registros de un mundo (al terminar una arena). */
	public void cleanupArena(Arena arena) {
		if (arena == null || arena.getWorld() == null)
			return;
		untrackWorld(arena.getWorld());
	}
}
