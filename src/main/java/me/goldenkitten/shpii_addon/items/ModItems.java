package me.goldenkitten.shpii_addon.items;

import me.goldenkitten.shpii_addon.SHPIIAddon;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItems {

    public static final Item STORMCALLERS_SIGIL = register(
            // Ignore the food component for now, we'll cover it later in the food section.
            new StormCallersSigil(new FabricItemSettings().fireproof().maxCount(1)),
            "stormcallers_sigil"
    );
    public static final Item STORMCALLERS_REMNANT = register(
            // Ignore the food component for now, we'll cover it later in the food section.
            new StormCallersSigil(new FabricItemSettings().fireproof().maxCount(4)),
            "stormcallers_remnant"
    );

    public static Item register(Item item, String id) {
        Identifier itemID = new Identifier(SHPIIAddon.MOD_ID, id);
        return Registry.register(Registries.ITEM, itemID, item);
    }

    public static void initialize() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT)
                .register((itemGroup) -> itemGroup.add(ModItems.STORMCALLERS_REMNANT));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT)
                .register((itemGroup) -> itemGroup.add(ModItems.STORMCALLERS_SIGIL));
    }
}
