package net.vitality_aids.effects;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.vitality_aids.VitalityAids;
import net.vitality_aids.config.VitalityAidsConfig;

// This class defines the custom Hemorrhage status effect.
public class HemorrhageEffect extends StatusEffect {
    // Singleton instance for easy access
    public static final HemorrhageEffect INSTANCE = new HemorrhageEffect();

    private HemorrhageEffect() {
        // Hemorrhage is harmful (HARMFUL) and does not show particles (NONE).
        // It's considered a negative effect.
        super(StatusEffectCategory.HARMFUL, 0x8B0000); // Dark red color
    }

    // This method is called every tick while the entity has the effect.
    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier) {
        // Only apply effects on the server side
        if (!entity.getWorld().isClient()) {
            VitalityAidsConfig.HemorrhageSettings settings = VitalityAids.CONFIG.hemorrhageSettings;

            // Apply small amount of damage each tick
            entity.damage(entity.getWorld().getDamageSources().magic(), (float) settings.damagePerTick);

            // Apply Mining Fatigue
            StatusEffect miningFatigue = Registries.STATUS_EFFECT.get(new Identifier("minecraft:mining_fatigue"));
            if (miningFatigue != null) {
                entity.addStatusEffect(new StatusEffectInstance(miningFatigue, 2, settings.miningFatigueLevel - 1, true, false, false));
            }

            // Apply Weakness
            StatusEffect weakness = Registries.STATUS_EFFECT.get(new Identifier("minecraft:weakness"));
            if (weakness != null) {
                entity.addStatusEffect(new StatusEffectInstance(weakness, 2, settings.weaknessLevel - 1, true, false, false));
            }
        }
    }

    // Allow the effect to tick continuously (every tick)
    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true; // Apply update effect every tick
    }

    // Method to prevent healing from food (will be handled via Mixin or Event)
    // This specific method isn't for blocking healing, but for effects applied.
    // The "cant heal from eating food" will require a Mixin.
}
