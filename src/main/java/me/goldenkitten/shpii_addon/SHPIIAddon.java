package me.goldenkitten.shpii_addon;

import me.goldenkitten.shpii_addon.items.ModItems;
import me.goldenkitten.shpii_addon.mixin.RecipeManagerAccessor;
import me.goldenkitten.shpii_addon.utils.Utils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.*;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SHPIIAddon implements ModInitializer {
	public static final String MOD_ID = "shpii_addon";
	public static final String MOD_MSG = "[GoldenKitten]: ";
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		ModItems.initialize();
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			injectStormcallerRecipe(server.getRecipeManager());
			LOGGER.info("Stormcaller’s Remnant injected on server start.");
		});

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			ServerPlayerEntity player = handler.getPlayer();
			Identifier advId = new Identifier("shpii_addon", "stormcallers_remnant");
			Advancement adv = server.getAdvancementLoader().get(advId);
			if (adv != null) {
				AdvancementProgress progress = player.getAdvancementTracker().getProgress(adv);
				if (!progress.isDone()) {
					for (String criterion : progress.getUnobtainedCriteria()) {
						player.getAdvancementTracker().grantCriterion(adv, criterion);
					}
				}
			} else {
				SHPIIAddon.LOGGER.warn("Missing advancement for recipe unlock: {}", advId);
			}
		});
		LOGGER.info(MOD_MSG + MOD_ID + " initialized!");
	}

	public static void injectStormcallerRecipe(RecipeManager manager) {
		boolean archon = FabricLoader.getInstance().isModLoaded("archon");
		boolean mcdw = FabricLoader.getInstance().isModLoaded("mcdw");

		boolean modsLoaded = archon && mcdw;

		// Ingredients
		Ingredient totem = Ingredient.ofItems(Items.TOTEM_OF_UNDYING);
		Ingredient l = modsLoaded
				? Ingredient.ofItems(Registries.ITEM.get(new Identifier("archon", "thunder_staff")))
				: Ingredient.ofItems(Items.LIGHTNING_ROD);
		Ingredient d = modsLoaded
				? Ingredient.ofItems(Registries.ITEM.get(new Identifier("archon", "diamond_mana_catalyst")))
				: Ingredient.ofItems(Items.DIAMOND_BLOCK);
		Ingredient kOrP = modsLoaded
				? Ingredient.ofItems(Registries.ITEM.get(new Identifier("mcdw", "crossbow_lightning_harp_crossbow")))
				: Ingredient.ofItems(Items.DAYLIGHT_DETECTOR);

		// Pattern
		DefaultedList<Ingredient> inputs = DefaultedList.ofSize(9, Ingredient.EMPTY);
		inputs.set(2, totem);  // Slot index refers to row-major order in 3x3
		inputs.set(3, l);
		inputs.set(4, d);
		inputs.set(6, kOrP);
		inputs.set(7, l);

		// Result
		ItemStack result = new ItemStack(ModItems.STORMCALLERS_REMNANT);

		// Build the recipe
		ShapedRecipe recipe = new ShapedRecipe(
				new Identifier("shpii_addon", "stormcallers_remnant"),
				"goldenkitten",
				CraftingRecipeCategory.MISC,
				3,
				3,
				inputs,
				result
		);
		RecipeManagerAccessor accessor = (RecipeManagerAccessor) manager;
		Utils.injectSafe(accessor, recipe, true);
	}
}