package me.goldenkitten.shpii_addon;

import me.goldenkitten.shpii_addon.items.ModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.data.server.recipe.RecipeJsonProvider;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.function.Consumer;

public class StormcallersRemnantRecipeProvider extends FabricRecipeProvider {
    public StormcallersRemnantRecipeProvider(FabricDataOutput generator) {
        super(generator);
    }

    @Override
    public void generate(Consumer<RecipeJsonProvider> exporter) {
        boolean archon = FabricLoader.getInstance().isModLoaded("archon");
        boolean mcdw = FabricLoader.getInstance().isModLoaded("mcdw");
        boolean modsLoaded = archon && mcdw;

        RecipeCategory category = RecipeCategory.MISC; // Or COMBAT if you're feeling dramatic
        ItemConvertible resultItem = ModItems.STORMCALLERS_REMNANT;

        ShapedRecipeJsonBuilder builder = ShapedRecipeJsonBuilder.create(category, resultItem)
                .group("goldenkitten")
                .pattern("  #")
                .pattern("LD ")
                .pattern(modsLoaded ? "KL " : "PL ")
                .input('#', Items.TOTEM_OF_UNDYING)
                .input('L', modsLoaded
                        ? Registries.ITEM.get(new Identifier("archon", "thunder_staff"))
                        : Items.LIGHTNING_ROD)
                .input('D', modsLoaded
                        ? Registries.ITEM.get(new Identifier("archon", "diamond_mana_catalyst"))
                        : Items.DIAMOND_BLOCK)
                .input(modsLoaded ? 'K' : 'P', modsLoaded
                        ? Registries.ITEM.get(new Identifier("mcdw", "crossbow_lightning_harp_crossbow"))
                        : Items.DAYLIGHT_DETECTOR);

        builder.offerTo(exporter, new Identifier("shpii_addon", "stormcallers_remnant"));
    }
}
