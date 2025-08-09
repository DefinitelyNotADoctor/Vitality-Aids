package net.vitality_aids.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.player.PlayerEntity;
import net.vitality_aids.VitalityAids;
import net.vitality_aids.config.VitalityAidsConfig;
import net.vitality_aids.effects.HemorrhageEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class) // Only Players heal from food, so targeting PlayerEntity is sufficient
public abstract class PlayerEntityFoodHealingMixin {

    // This inject targets the return of the 'canFoodHeal' method.
    // It will change the returned value to false if the entity is a player,
    // has the Hemorrhage effect, and the config allows this prevention.
    @Inject(
            method = "canFoodHeal",
            at = @At("RETURN"), // Inject right before the method returns
            cancellable = true // Allows us to change the return value
    )
    private void vitalityAids$preventFoodHealIfHemorrhaging(CallbackInfoReturnable<Boolean> cir) {
        LivingEntity entity = (LivingEntity)(Object)this; // Cast 'this' to LivingEntity

        // Check config first for overall Hemorrhage system enable and this specific feature enable
        if (!VitalityAids.CONFIG.hemorrhageSettings.hemorrhageEnabled ||
                !VitalityAids.CONFIG.hemorrhageSettings.hemorrhagePreventFoodHealEnabled) {
            return; // If disabled in config, do nothing
        }

        // Only apply this logic to PlayerEntities
        if (entity instanceof PlayerEntity) {
            // Check if the player has the Hemorrhage effect
            if (entity.hasStatusEffect(HemorrhageEffect.INSTANCE)) {
                // If they have Hemorrhage, set the return value of canFoodHeal() to false.
                // This means the game will now think this player cannot heal from food.
                cir.setReturnValue(false);
                // No need for cir.cancel() here when using @At("RETURN") because we're just
                // changing the return value of the already-executed original method.
                // However, adding cir.cancel() explicitly doesn't hurt and clarifies intent for some Mixin versions.
                // For `@At("RETURN")` and `cancellable = true`, setting the return value is enough.
            }
        }
    }
}
