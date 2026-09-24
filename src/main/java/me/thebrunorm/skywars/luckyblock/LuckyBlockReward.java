// Copyright (c) 2025 Bruno
package me.thebrunorm.skywars.luckyblock;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import static me.thebrunorm.skywars.luckyblock.LuckyBlockManager.*;

/**
 * Todas las sorpresas posibles de un LuckyBlock.
 *
 * <p>Cada recompensa tiene una categoria (activable en luckyblocks.yml),
 * un humor (GOOD/BAD/FUN para titulo y sonido) y un peso para el sorteo.</p>
 */
public enum LuckyBlockReward {

	// ---------- ITEMS ----------
	DIAMOND_GEAR(Category.ITEMS, Mood.GOOD, 8) {
		@Override
		void apply(Player player, Location loc) {
			give(player, enchanted(Material.DIAMOND_SWORD, Enchantment.SHARPNESS, 2),
					new ItemStack(Material.DIAMOND_HELMET),
					new ItemStack(Material.DIAMOND_CHESTPLATE),
					new ItemStack(Material.DIAMOND_LEGGINGS),
					new ItemStack(Material.DIAMOND_BOOTS));
		}
	},
	IRON_KIT(Category.ITEMS, Mood.GOOD, 10) {
		@Override
		void apply(Player player, Location loc) {
			give(player, new ItemStack(Material.IRON_SWORD),
					new ItemStack(Material.BOW),
					new ItemStack(Material.ARROW, 32),
					new ItemStack(Material.IRON_HELMET),
					new ItemStack(Material.IRON_CHESTPLATE),
					new ItemStack(Material.IRON_LEGGINGS),
					new ItemStack(Material.IRON_BOOTS),
					new ItemStack(Material.GOLDEN_APPLE, 2));
		}
	},
	GOLDEN_FEAST(Category.ITEMS, Mood.GOOD, 9) {
		@Override
		void apply(Player player, Location loc) {
			give(player, new ItemStack(Material.GOLDEN_APPLE, 5),
					new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 1),
					new ItemStack(Material.COOKED_BEEF, 16));
		}
	},
	PEARL_PARTY(Category.ITEMS, Mood.GOOD, 8) {
		@Override
		void apply(Player player, Location loc) {
			give(player, new ItemStack(Material.ENDER_PEARL, 8),
					new ItemStack(Material.FISHING_ROD),
					new ItemStack(Material.WATER_BUCKET));
		}
	},
	TOTEM_GIFT(Category.ITEMS, Mood.GOOD, 4) {
		@Override
		void apply(Player player, Location loc) {
			give(player, new ItemStack(Material.TOTEM_OF_UNDYING),
					new ItemStack(Material.SHIELD));
		}
	},
	BONUS_CHEST(Category.ITEMS, Mood.GOOD, 6) {
		@Override
		void apply(Player player, Location loc) {
			placeLootChest(loc, new ItemStack(Material.DIAMOND, 6),
					new ItemStack(Material.GOLD_INGOT, 12),
					new ItemStack(Material.EXPERIENCE_BOTTLE, 32),
					new ItemStack(Material.GOLDEN_APPLE, 3));
		}
	},

	// ---------- EFFECTS ----------
	ELIXIR(Category.EFFECTS, Mood.GOOD, 8) {
		@Override
		void apply(Player player, Location loc) {
			effect(player, PotionEffectType.SPEED, 20 * 60, 1);
			effect(player, PotionEffectType.REGENERATION, 20 * 20, 1);
			effect(player, PotionEffectType.ABSORPTION, 20 * 60, 1);
		}
	},
	GHOST(Category.EFFECTS, Mood.GOOD, 6) {
		@Override
		void apply(Player player, Location loc) {
			effect(player, PotionEffectType.INVISIBILITY, 20 * 30, 0);
			effect(player, PotionEffectType.SPEED, 20 * 30, 0);
		}
	},
	BERSERKER(Category.EFFECTS, Mood.GOOD, 6) {
		@Override
		void apply(Player player, Location loc) {
			effect(player, PotionEffectType.STRENGTH, 20 * 30, 0);
			effect(player, PotionEffectType.RESISTANCE, 20 * 30, 0);
		}
	},
	SECOND_WIND(Category.EFFECTS, Mood.GOOD, 7) {
		@Override
		void apply(Player player, Location loc) {
			player.setHealth(Math.min(player.getMaxHealth(), player.getHealth() + 14));
			player.setFoodLevel(20);
			player.setFireTicks(0);
			for (final PotionEffect active : player.getActivePotionEffects()) {
				if (isNegative(active.getType()))
					player.removePotionEffect(active.getType());
			}
			effect(player, PotionEffectType.REGENERATION, 20 * 10, 1);
		}
	},
	VENOM(Category.EFFECTS, Mood.BAD, 7) {
		@Override
		void apply(Player player, Location loc) {
			effect(player, PotionEffectType.POISON, 20 * 10, 1);
			effect(player, PotionEffectType.WITHER, 20 * 5, 0);
			effect(player, PotionEffectType.SLOWNESS, 20 * 10, 1);
		}
	},
	DARKNESS(Category.EFFECTS, Mood.BAD, 6) {
		@Override
		void apply(Player player, Location loc) {
			effect(player, PotionEffectType.BLINDNESS, 20 * 15, 0);
			effect(player, PotionEffectType.NAUSEA, 20 * 15, 0);
			effect(player, PotionEffectType.SLOWNESS, 20 * 15, 1);
		}
	},

	// ---------- MOBS ----------
	ZOMBIE_SWARM(Category.MOBS, Mood.BAD, 8) {
		@Override
		void apply(Player player, Location loc) {
			for (int i = 0; i < 4; i++)
				attacking(player, loc, EntityType.ZOMBIE);
			for (int i = 0; i < 2; i++)
				attacking(player, loc, EntityType.SKELETON);
		}
	},
	CREEPER_HUG(Category.MOBS, Mood.BAD, 7) {
		@Override
		void apply(Player player, Location loc) {
			for (int i = 0; i < 3; i++)
				attacking(player, loc, EntityType.CREEPER);
		}
	},
	BLAZE_PAIR(Category.MOBS, Mood.BAD, 5) {
		@Override
		void apply(Player player, Location loc) {
			for (int i = 0; i < 2; i++)
				attacking(player, loc, EntityType.BLAZE);
		}
	},
	WITCH_HOUR(Category.MOBS, Mood.BAD, 5) {
		@Override
		void apply(Player player, Location loc) {
			for (int i = 0; i < 2; i++)
				attacking(player, loc, EntityType.WITCH);
			for (int i = 0; i < 2; i++)
				attacking(player, loc, EntityType.SPIDER);
		}
	},
	GOLEM_GUARDIAN(Category.MOBS, Mood.GOOD, 4) {
		@Override
		void apply(Player player, Location loc) {
			spawnAllyGolem(player, loc);
		}
	},

	// ---------- BLOCKS ----------
	RICH_TOWER(Category.BLOCKS, Mood.GOOD, 6) {
		@Override
		void apply(Player player, Location loc) {
			buildTower(loc, Material.DIAMOND_BLOCK, Material.EMERALD_BLOCK, Material.GOLD_BLOCK);
		}
	},
	OBSIDIAN_CAGE(Category.BLOCKS, Mood.BAD, 7) {
		@Override
		void apply(Player player, Location loc) {
			buildCage(player.getLocation(), Material.OBSIDIAN);
		}
	},
	COBWEB_TRAP(Category.BLOCKS, Mood.BAD, 7) {
		@Override
		void apply(Player player, Location loc) {
			trapWebs(player.getLocation());
		}
	},
	LAVA_TRAP(Category.BLOCKS, Mood.BAD, 6) {
		@Override
		void apply(Player player, Location loc) {
			trapLava(player, player.getLocation());
		}
	},
	TRAPDOOR(Category.BLOCKS, Mood.BAD, 5) {
		@Override
		void apply(Player player, Location loc) {
			openPit(player.getLocation());
		}
	},

	// ---------- TNT ----------
	TNT_SURPRISE(Category.TNT, Mood.BAD, 8) {
		@Override
		void apply(Player player, Location loc) {
			igniteTnt(loc, 3, 40);
		}
	},
	TNT_RAIN(Category.TNT, Mood.BAD, 4) {
		@Override
		void apply(Player player, Location loc) {
			rainTnt(player, loc, 6);
		}
	},

	// ---------- TRAPS ----------
	ANVIL_RAIN(Category.TRAPS, Mood.BAD, 6) {
		@Override
		void apply(Player player, Location loc) {
			rainAnvils(player, player.getLocation(), 5);
		}
	},
	WRATH_LIGHTNING(Category.TRAPS, Mood.BAD, 6) {
		@Override
		void apply(Player player, Location loc) {
			strike(player);
		}
	},
	EMPTY_TROLL(Category.TRAPS, Mood.FUN, 5) {
		@Override
		void apply(Player player, Location loc) {
			trollSmoke(loc);
		}
	},

	// ---------- SPECIAL ----------
	JACKPOT(Category.SPECIAL, Mood.EPIC, 2) {
		@Override
		void apply(Player player, Location loc) {
			give(player, new ItemStack(Material.DIAMOND, 16),
					new ItemStack(Material.GOLD_INGOT, 32),
					new ItemStack(Material.NETHERITE_SCRAP, 2),
					new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 2),
					new ItemStack(Material.TOTEM_OF_UNDYING));
			player.giveExpLevels(15);
			celebrate(player, loc, 6);
		}
	},
	WILD_TELEPORT(Category.SPECIAL, Mood.FUN, 5) {
		@Override
		void apply(Player player, Location loc) {
			wildTeleport(player);
		}
	},
	SWAP(Category.SPECIAL, Mood.FUN, 4) {
		@Override
		void apply(Player player, Location loc) {
			swapWithRandom(player);
		}
	},
	MOON_LAUNCH(Category.SPECIAL, Mood.FUN, 5) {
		@Override
		void apply(Player player, Location loc) {
			launch(player);
		}
	};

	final Category category;
	final Mood mood;
	final int weight;

	LuckyBlockReward(Category category, Mood mood, int weight) {
		this.category = category;
		this.mood = mood;
		this.weight = weight;
	}

	/** Ejecuta la sorpresa sobre el jugador que rompio el bloque. */
	abstract void apply(Player player, Location loc);

	/** Clave para el nombre en luckyblocks/lang: luckyblock.rewards.<ID>. */
	public String langKey() {
		return "luckyblock.rewards." + this.name();
	}

	public enum Category {
		ITEMS, MOBS, EFFECTS, BLOCKS, TNT, TRAPS, SPECIAL
	}

	public enum Mood {
		GOOD, BAD, FUN, EPIC
	}
}
