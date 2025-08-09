package net.vitality_aids.item;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;


public class ModItemGroups {
    // 1. Define the RegistryKey for your custom item group
    public static final RegistryKey<ItemGroup> VITALITY_AIDS_GROUP_KEY = RegistryKey.of(
            Registries.ITEM_GROUP.getKey(),
            new Identifier("vitality_aids", "vitality_aids_group")
    );

    // 2. Register the actual ItemGroup instance using the key
    public static final ItemGroup VITALITY_AIDS_GROUP = Registry.register(
            Registries.ITEM_GROUP,
            VITALITY_AIDS_GROUP_KEY,
            FabricItemGroup.builder()
                    .icon(() -> new ItemStack(Items.POTION))
                    .displayName(Text.translatable("itemGroup.vitality_aids.vitality_aids_group"))
                    .build()
    );

    // Call this method in your main mod class's onInitialize()
    public static void registerItemGroups() {

    }
}
