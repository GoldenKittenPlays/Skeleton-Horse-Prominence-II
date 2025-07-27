package me.goldenkitten.shpii_addon.utils;

import me.goldenkitten.shpii_addon.SHPIIAddon;
import me.goldenkitten.shpii_addon.mixin.RecipeManagerAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;

public class Utils {
    /**
     * Injects a recipe into the RecipeManager with optional overwriting behavior.
     *
     * @param accessor The accessor mixin for internal maps
     * @param customRecipe The recipe to inject
     * @param allowOverwrite Whether to allow overwriting an existing recipe
     */
    public static void injectSafe(
            RecipeManagerAccessor accessor,
            CraftingRecipe customRecipe,
            boolean allowOverwrite
    ) {
        Identifier id = customRecipe.getId();

        // Clone mutable copies of the recipe maps to avoid mutating immutable maps
        Map<RecipeType<?>, Map<Identifier, Recipe<?>>> recipesMap = new HashMap<>(accessor.getRecipes());
        Map<Identifier, Recipe<?>> byIdMap = new HashMap<>(accessor.getRecipesById());

        // Ensure there's a mutable submap for crafting recipes
        Map<Identifier, Recipe<?>> craftingMap =
                new HashMap<>(recipesMap.getOrDefault(RecipeType.CRAFTING, new HashMap<>()));

        Recipe<?> existingRecipe = byIdMap.get(id);

        if (existingRecipe != null) {
            if (!allowOverwrite) {
                SHPIIAddon.LOGGER.warn("Recipe with ID {} already exists; skipping injection.", id);
                return;
            } else {
                SHPIIAddon.LOGGER.warn("Overwriting existing recipe with ID {}.", id);
            }
        }

        // Inject or overwrite
        craftingMap.put(id, customRecipe);
        byIdMap.put(id, customRecipe);
        recipesMap.put(RecipeType.CRAFTING, craftingMap);

        // Reassign the full maps using accessors — this is crucial
        accessor.setRecipes(recipesMap);
        accessor.setRecipesById(byIdMap);

        SHPIIAddon.LOGGER.info("✅ Injected Stormcaller's Remnant recipe: {} (overwrite: {})", id, allowOverwrite);
    }

    public static Text getLightningMessage(String message) {
        return Text.literal("§6§l⚡ " + message + " ⚡");
    }

    public static BlockPos findSafeTeleportPos(ServerWorld world, BlockPos origin, int maxHeight) {
        for (int y = origin.getY(); y < maxHeight; y++) {
            BlockPos checkPos = new BlockPos(origin.getX(), y, origin.getZ());
            BlockState floor = world.getBlockState(checkPos.down());
            BlockState body = world.getBlockState(checkPos);
            BlockState head = world.getBlockState(checkPos.up());

            if (floor.isSolidBlock(world, checkPos.down()) &&
                    body.isAir() &&
                    head.isAir()) {
                return checkPos;
            }
        }
        return origin;
    }
}
