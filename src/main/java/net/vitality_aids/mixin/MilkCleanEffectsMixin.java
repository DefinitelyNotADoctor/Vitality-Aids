package net.vitality_aids.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MilkBucketItem;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.vitality_aids.VitalityAids; // For config access and logging

@Mixin(MilkBucketItem.class)
public class MilkCleanEffectsMixin {

    // Redirect the call to LivingEntity.clearStatusEffects()
    // This allows us to conditionally prevent that method from being called.
    @Redirect(
            method = "finishUsing(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;)Lnet/minecraft/item/ItemStack;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;clearStatusEffects()Z" // Target the clearStatusEffects method
            )
    )
    // IMPORTANT: Added ItemStack stack parameter as required by the error message.
    private boolean vitalityAids$onClearStatusEffects(LivingEntity instance, ItemStack stack) {
        // 'instance' refers to the LivingEntity object on which clearStatusEffects() would be called.
        // 'stack' refers to the ItemStack (milk bucket) being used.

        // Check if the feature is disabled in the config
        if (!VitalityAids.CONFIG.globalSettings.disableMilkClearsEffects) {
            // If the feature is disabled, allow the original method to be called.
            VitalityAids.LOGGER.debug("Milk clears effects (disabled by config).");
            return instance.clearStatusEffects(); // Call the original method
        } else {
            // If the feature is enabled (meaning we want to disable milk clearing effects)
            VitalityAids.LOGGER.debug("Milk prevented from clearing effects (enabled by config).");
            return false; // Return false, indicating that no effects were cleared (prevents original call)
        }
    }
}
