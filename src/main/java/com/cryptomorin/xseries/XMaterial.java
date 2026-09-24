package com.cryptomorin.xseries;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Reemplazo interno minimo de XSeries, sin parseo de versiones del servidor.
 *
 * <p>XSeries 9.x falla en servidores modernos (Paper 26.x) porque su bloque
 * estatico intenta parsear la version del servidor y lanza
 * {@code IllegalArgumentException}, rompiendo el enable del plugin. Esta clase
 * expone solo la API que usa MSkyWars y delega en {@link Material} de Bukkit,
 * con alias para nombres legacy de configs viejas (BED, etc.).</p>
 */
public final class XMaterial {

	private static final Map<String, String> LEGACY_ALIASES = new HashMap<>();

	static {
		// Camas y colores legacy
		LEGACY_ALIASES.put("BED", "RED_BED");
		// Comparador (1.12) -> nombre moderno
		LEGACY_ALIASES.put("REDSTONE_COMPARATOR", "COMPARATOR");
		// Bloques renombrados en el aplanamiento 1.13
		LEGACY_ALIASES.put("WORKBENCH", "CRAFTING_TABLE");
		LEGACY_ALIASES.put("STATIONARY_WATER", "WATER");
		LEGACY_ALIASES.put("STATIONARY_LAVA", "LAVA");
		LEGACY_ALIASES.put("WOOD", "OAK_PLANKS");
		LEGACY_ALIASES.put("WOOL", "WHITE_WOOL");
		LEGACY_ALIASES.put("STAINED_GLASS", "WHITE_STAINED_GLASS");
		LEGACY_ALIASES.put("STAINED_GLASS_PANE", "WHITE_STAINED_GLASS_PANE");
		LEGACY_ALIASES.put("THIN_GLASS", "GLASS_PANE");
		LEGACY_ALIASES.put("FENCE", "OAK_FENCE");
		LEGACY_ALIASES.put("FENCE_GATE", "OAK_FENCE_GATE");
		LEGACY_ALIASES.put("WOOD_PLATE", "OAK_PRESSURE_PLATE");
		LEGACY_ALIASES.put("STONE_PLATE", "STONE_PRESSURE_PLATE");
		LEGACY_ALIASES.put("IRON_PLATE", "HEAVY_WEIGHTED_PRESSURE_PLATE");
		LEGACY_ALIASES.put("GOLD_PLATE", "LIGHT_WEIGHTED_PRESSURE_PLATE");
		LEGACY_ALIASES.put("SIGN", "OAK_SIGN");
		LEGACY_ALIASES.put("WALL_SIGN", "OAK_WALL_SIGN");
		LEGACY_ALIASES.put("BED_BLOCK", "RED_BED");
		LEGACY_ALIASES.put("DIODE", "REPEATER");
		LEGACY_ALIASES.put("DIODE_BLOCK_OFF", "REPEATER");
		LEGACY_ALIASES.put("DIODE_BLOCK_ON", "REPEATER");
		LEGACY_ALIASES.put("REDSTONE_COMPARATOR_OFF", "COMPARATOR");
		LEGACY_ALIASES.put("REDSTONE_COMPARATOR_ON", "COMPARATOR");
		LEGACY_ALIASES.put("SKULL", "SKELETON_SKULL");
		LEGACY_ALIASES.put("SKULL_ITEM", "SKELETON_SKULL");
		LEGACY_ALIASES.put("MONSTER_EGG", "INFESTED_STONE");
		LEGACY_ALIASES.put("MONSTER_EGGS", "INFESTED_STONE");
		LEGACY_ALIASES.put("SMOOTH_BRICK", "STONE_BRICKS");
		LEGACY_ALIASES.put("SMOOTH_STAIRS", "SMOOTH_STONE_STAIRS");
		LEGACY_ALIASES.put("IRON_FENCE", "IRON_BARS");
		LEGACY_ALIASES.put("THIN_GLASS", "GLASS_PANE");
		LEGACY_ALIASES.put("WEB", "COBWEB");
		LEGACY_ALIASES.put("LONG_GRASS", "SHORT_GRASS");
		LEGACY_ALIASES.put("DOUBLE_STEP", "STONE_SLAB");
		LEGACY_ALIASES.put("STEP", "STONE_SLAB");
		LEGACY_ALIASES.put("WOOD_STEP", "OAK_SLAB");
		LEGACY_ALIASES.put("WOOD_DOUBLE_STEP", "OAK_SLAB");
		LEGACY_ALIASES.put("HUGE_MUSHROOM_1", "BROWN_MUSHROOM_BLOCK");
		LEGACY_ALIASES.put("HUGE_MUSHROOM_2", "RED_MUSHROOM_BLOCK");
		LEGACY_ALIASES.put("STAINED_CLAY", "TERRACOTTA");
		LEGACY_ALIASES.put("HARD_CLAY", "TERRACOTTA");
		LEGACY_ALIASES.put("SULPHUR", "GUNPOWDER");
		LEGACY_ALIASES.put("PORK", "PORKCHOP");
		LEGACY_ALIASES.put("GRILLED_PORK", "COOKED_PORKCHOP");
		LEGACY_ALIASES.put("RAW_FISH", "COD");
		LEGACY_ALIASES.put("COOKED_FISH", "COOKED_COD");
		LEGACY_ALIASES.put("GOLD_SWORD", "GOLDEN_SWORD");
		LEGACY_ALIASES.put("GOLD_AXE", "GOLDEN_AXE");
		LEGACY_ALIASES.put("GOLD_PICKAXE", "GOLDEN_PICKAXE");
		LEGACY_ALIASES.put("GOLD_SPADE", "GOLDEN_SHOVEL");
		LEGACY_ALIASES.put("GOLD_HOE", "GOLDEN_HOE");
		LEGACY_ALIASES.put("GOLD_HELMET", "GOLDEN_HELMET");
		LEGACY_ALIASES.put("GOLD_CHESTPLATE", "GOLDEN_CHESTPLATE");
		LEGACY_ALIASES.put("GOLD_LEGGINGS", "GOLDEN_LEGGINGS");
		LEGACY_ALIASES.put("GOLD_BOOTS", "GOLDEN_BOOTS");
		LEGACY_ALIASES.put("GOLD_BARDING", "GOLDEN_HORSE_ARMOR");
		LEGACY_ALIASES.put("IRON_BARDING", "IRON_HORSE_ARMOR");
		LEGACY_ALIASES.put("DIAMOND_BARDING", "DIAMOND_HORSE_ARMOR");
		LEGACY_ALIASES.put("LEASH", "LEAD");
		LEGACY_ALIASES.put("EXP_BOTTLE", "EXPERIENCE_BOTTLE");
		LEGACY_ALIASES.put("FIREBALL", "FIRE_CHARGE");
		LEGACY_ALIASES.put("SNOWBALL", "SNOWBALL");
	}

	// Constantes usadas directamente en el codigo.
	public static final XMaterial AIR = of(Material.AIR);
	public static final XMaterial CHEST = of(Material.CHEST);
	public static final XMaterial BEACON = of(Material.BEACON);
	public static final XMaterial GLASS = of(Material.GLASS);
	public static final XMaterial BEDROCK = of(Material.BEDROCK);
	public static final XMaterial RED_STAINED_GLASS = of(Material.RED_STAINED_GLASS);
	public static final XMaterial GREEN_STAINED_GLASS = of(Material.GREEN_STAINED_GLASS);
	public static final XMaterial LIME_STAINED_GLASS = of(Material.LIME_STAINED_GLASS);
	public static final XMaterial FIREWORK_STAR = of(Material.FIREWORK_STAR);
	public static final XMaterial BOW = of(Material.BOW);
	public static final XMaterial SADDLE = of(Material.SADDLE);
	public static final XMaterial WOODEN_AXE = of(Material.WOODEN_AXE);
	public static final XMaterial WRITABLE_BOOK = of(Material.WRITABLE_BOOK);
	public static final XMaterial BARRIER = of(Material.BARRIER);
	public static final XMaterial PAPER = of(Material.PAPER);
	public static final XMaterial COMPASS = of(Material.COMPASS);
	public static final XMaterial NETHER_STAR = of(Material.NETHER_STAR);
	public static final XMaterial CLOCK = of(Material.CLOCK);
	public static final XMaterial SUNFLOWER = of(Material.SUNFLOWER);
	public static final XMaterial ENDER_PEARL = of(Material.ENDER_PEARL);
	public static final XMaterial ENDER_CHEST = of(Material.ENDER_CHEST);

	private final Material material;

	private XMaterial(Material material) {
		this.material = material;
	}

	private static XMaterial of(Material material) {
		return new XMaterial(material);
	}

	/**
	 * Busca un material por nombre moderno o legacy. Nunca lanza excepciones.
	 */
	public static Optional<XMaterial> matchXMaterial(String name) {
		if (name == null)
			return Optional.empty();
		String key = name.trim().toUpperCase(Locale.ENGLISH).replace(' ', '_').replace('-', '_');
		if (key.startsWith("MINECRAFT:"))
			key = key.substring("MINECRAFT:".length());
		final String modern = LEGACY_ALIASES.getOrDefault(key, key);
		Material mat = Material.matchMaterial(modern);
		if (mat == null) {
			try {
				mat = Material.valueOf(modern);
			} catch (final IllegalArgumentException ignored) {
				return Optional.empty();
			}
		}
		return Optional.of(new XMaterial(mat));
	}

	/**
	 * Como el valueOf del enum original: lanza IllegalArgumentException si no existe.
	 */
	public static XMaterial valueOf(String name) {
		return matchXMaterial(name)
				.orElseThrow(() -> new IllegalArgumentException("Unknown material: " + name));
	}

	public Material parseMaterial() {
		return this.material;
	}

	public ItemStack parseItem() {
		return new ItemStack(this.material);
	}

	@Override
	public String toString() {
		return this.material.name();
	}
}
