package me.goldenkitten.shpii_addon.mixin;

import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(RecipeManager.class)
public interface RecipeManagerAccessor {
    // Read access
    @Accessor("recipes")
    Map<RecipeType<?>, Map<Identifier, Recipe<?>>> getRecipes();

    @Accessor("recipesById")
    Map<Identifier, Recipe<?>> getRecipesById();

    // Write access — needed to commit mutations
    @Accessor("recipes")
    void setRecipes(Map<RecipeType<?>, Map<Identifier, Recipe<?>>> recipes);

    @Accessor("recipesById")
    void setRecipesById(Map<Identifier, Recipe<?>> recipesById);
}