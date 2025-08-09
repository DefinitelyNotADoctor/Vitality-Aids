package net.vitality_aids.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.passive.GolemEntity; // For Golem check, if needed
import net.minecraft.entity.player.PlayerEntity; // For Player specific logic
import net.minecraft.world.World; // To check if it's client or server side
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.vitality_aids.VitalityAids;
import net.vitality_aids.config.VitalityAidsConfig;
import net.vitality_aids.effects.HemorrhageEffect;

@Mixin(LivingEntity.class)
public abstract class LivingEntityDamageMixin {

    // Inject before the actual damage application to check conditions
    // Use an @Inject with a CallbackInfoReturnable to potentially modify the return value (though not strictly needed here)
    // or simply check conditions before damage is applied.
    @Inject(method = "damage", at = @At("HEAD"))
    private void vitalityAids$onDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        // Cast 'this' to LivingEntity for easier access
        LivingEntity entity = (LivingEntity) (Object) this;
        World world = entity.getWorld();

        // Only apply logic on the server side
        if (world.isClient()) {
            return;
        }

        // Get config settings
        VitalityAidsConfig.HemorrhageSettings hemorrhageSettings = VitalityAids.CONFIG.hemorrhageSettings;

        if(!hemorrhageSettings.hemorrhageEnabled)
        {
            return;
        }
        // Don't apply Hemorrhage if the entity already has it
        if (entity.hasStatusEffect(HemorrhageEffect.INSTANCE)) {
            return;
        }

        // Calculate current health percentage and damage percentage
        float maxHealth = entity.getMaxHealth();
        float currentHealth = entity.getHealth();

        // Calculate health percentage BEFORE damage is applied
        double healthPercentBeforeDamage = currentHealth / maxHealth;
        double damagePercentOfMaxHealth = amount / maxHealth;

        // --- Hemorrhage Activation Logic ---

        // Condition 1: Taken more than half max health damage from a single attack
        if (damagePercentOfMaxHealth > hemorrhageSettings.damageThresholdPercentSingleHit) {
            // Player: Always activate if this condition is met
            if (entity instanceof PlayerEntity) {
                applyHemorrhage(entity);
                return;
            } else {
                // Other Living Entities: 50% chance to activate
                if (entity.getRandom().nextDouble() < 0.5) { // Fixed 50% as per requirements
                    applyHemorrhage(entity);
                    return;
                }
            }
        }

        // Condition 2: Taken damage while have 25% health or less
        // This check should happen *after* the initial damage threshold check
        if (healthPercentBeforeDamage <= hemorrhageSettings.damageAtLowHealthThresholdPercent) {
            if (entity instanceof PlayerEntity) {
                // Player: 50% chance to activate
                if (entity.getRandom().nextDouble() < hemorrhageSettings.lowHealthActivationChancePlayer) {
                    applyHemorrhage(entity);
                    return;
                }
            } else if (!isUndead(entity)) { // Only for non-undead entities
                // Other Living Entities (excluding Undead): 20% chance to activate
                if (entity.getRandom().nextDouble() < hemorrhageSettings.lowHealthActivationChanceMob) {
                    applyHemorrhage(entity);
                    return;
                }
            }
        }
    }

    // Helper method to apply the Hemorrhage effect
    private void applyHemorrhage(LivingEntity entity) {
        VitalityAidsConfig.HemorrhageSettings hemorrhageSettings = VitalityAids.CONFIG.hemorrhageSettings;

        if(!hemorrhageSettings.hemorrhageEnabled)
        {
            return;
        }
        // Duration of Hemorrhage (e.g., 20 seconds = 20 * 20 ticks)
        // You might want to make this configurable
        int duration = hemorrhageSettings.duration;//20 * 20;
        // Amplifier (level of Hemorrhage effect, 0 for level 1)
        int amplifier = hemorrhageSettings.amplifier;
        // Show particles, ambient, show icon (false for now for less visual clutter)
        boolean showParticles = hemorrhageSettings.particles;
        boolean ambient = hemorrhageSettings.ambient;
        boolean showIcon = hemorrhageSettings.icon;

        entity.addStatusEffect(new StatusEffectInstance(HemorrhageEffect.INSTANCE, duration, amplifier, ambient, showParticles, showIcon));
        VitalityAids.LOGGER.debug("Applied Hemorrhage to {}", entity.getName().getString());
    }

    // Helper method to check if an entity is undead (e.g., Zombies, Skeletons)
    // This is a basic check. More comprehensive checks might involve LivingEntity.isUndead() or specific tags.
    private boolean isUndead(LivingEntity entity) {
        // Minecraft's isUndead() method is public and handles many common undead types
        return entity.isUndead();
        // You might need to add specific checks for modded undead entities here if isUndead() doesn't cover them.
        // Example: return entity.isUndead() || entity instanceof UndeadHorseEntity;
    }
}
