package me.goldenkitten.shpii_addon.utils;

import me.goldenkitten.shpii_addon.SHPIIAddon;
import me.goldenkitten.shpii_addon.mixin.RecipeManagerAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Utils {
    /**
     * Injects a recipe into the RecipeManager with optional overwriting behavior.
     *
     * @param recipeManager The recipe manager to inject into
     * @param accessor The accessor mixin for internal maps
     * @param customRecipe The recipe to inject
     * @param allowOverwrite Whether to allow overwriting an existing recipe
     */
    public static void injectSafe(
            RecipeManager recipeManager,
            RecipeManagerAccessor accessor,
            CraftingRecipe customRecipe,
            boolean allowOverwrite
    ) {
        Identifier id = customRecipe.getId();

        Map<RecipeType<?>, Map<Identifier, Recipe<?>>> recipesMap = accessor.getRecipes();
        Map<Identifier, Recipe<?>> byIdMap = accessor.getRecipesById();

        // Ensure a crafting recipe map exists
        Map<Identifier, Recipe<?>> craftingMap = recipesMap.computeIfAbsent(RecipeType.CRAFTING, k -> new HashMap<>());

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
        accessor.setRecipes(recipesMap);
        accessor.setRecipesById(byIdMap);
        SHPIIAddon.LOGGER.info("Injected Stormcaller's Remnant recipe: {} (overwrite: {})", id, allowOverwrite);
    }

    public static void announce(ServerPlayerEntity sourcePlayer, double radius, String message, SoundEvent soundEvent, boolean playSound) {
        try (ServerWorld world = sourcePlayer.getServerWorld()) {
            Vec3d sourcePos = sourcePlayer.getPos();
            if (soundEvent == null) {
                soundEvent = SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER;
            }
            for (ServerPlayerEntity target : world.getPlayers()) {
                if (target.getPos().isInRange(sourcePos, radius)) {
                    // Styled message
                    target.sendMessage(getLightningMessage(message), false);

                    if (playSound) {
                        // Optional sound effect
                        target.playSound(soundEvent, 1.0F, 1.0F);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
