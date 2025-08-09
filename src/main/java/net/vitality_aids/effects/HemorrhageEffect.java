package net.vitality_aids.effects;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.vitality_aids.VitalityAids;
import net.vitality_aids.config.VitalityAidsConfig;

// This class defines the custom Hemorrhage status effect.
public class HemorrhageEffect extends StatusEffect {

    public static final RegistryEntry<StatusEffect> HEMORRHAGE = Registry.registerReference(
            Registries.STATUS_EFFECT,
            Identifier.of(VitalityAids.MOD_ID, "hemorrhage"),
            new HemorrhageEffect()
    );

    private HemorrhageEffect() {
        // Hemorrhage is harmful (HARMFUL) and does not show particles (NONE).
        // It's considered a negative effect.
        super(StatusEffectCategory.HARMFUL, 0x8B0000); // Dark red color
    }

    // This method is called every tick while the entity has the effect.
    @Override
    public boolean applyUpdateEffect(LivingEntity entity, int amplifier) {
        // Only apply effects on the server side
        if (!entity.getWorld().isClient()) {
            VitalityAidsConfig.HemorrhageSettings settings = VitalityAids.CONFIG.hemorrhageSettings;

            // Apply small amount of damage each tick
            entity.damage(entity.getWorld().getDamageSources().magic(), (float) settings.damagePerTick);

            // Apply Mining Fatigue
            if (settings.miningFatigueLevel > 0) {
                entity.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.MINING_FATIGUE, // Use direct RegistryEntry for vanilla effect
                        2, // Duration of 2 ticks to ensure it's reapplied often
                        settings.miningFatigueLevel - 1, // Amplifier is 0-indexed
                        true, // Ambient
                        false, // No particles
                        false // No icon (optional, depends on your preference for these minor debuffs)
                ));
            }

            // Apply Weakness
            // Use StatusEffects.WEAKNESS directly, which is already a RegistryEntry<StatusEffect>
            if (settings.weaknessLevel > 0) {
                entity.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.WEAKNESS, // Use direct RegistryEntry for vanilla effect
                        2, // Duration of 2 ticks
                        settings.weaknessLevel - 1, // Amplifier is 0-indexed
                        true, // Ambient
                        false, // No particles
                        false // No icon
                ));
            }
        }
        return true;
    }

    // Allow the effect to tick continuously (every tick)
    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true; // Apply update effect every tick
    }

    public static void register() {
        // Just accessing the static field triggers registration
        var ignored = HEMORRHAGE;
    }

}
