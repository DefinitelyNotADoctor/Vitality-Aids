package net.vitality_aids.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.vitality_aids.VitalityAids;
import net.vitality_aids.effects.HemorrhageEffect;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import net.vitality_aids.config.VitalityAidsConfig;

    public class CustomMedicineItem extends Item {
        private final VitalityAidsConfig.MedicineEntry configEntry;

        public CustomMedicineItem(VitalityAidsConfig.MedicineEntry configEntry, Settings settings) {
            super(settings);
            this.configEntry = configEntry;
        }

        // --- Item Usage Logic ---
        // This method is called when the player starts using the item (holding right-click)
        @Override
        public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
            ItemStack itemStack = user.getStackInHand(hand);

            // Prevent use if on cooldown. This applies to both self-use and use-on-entity initiation.
            if (user.getItemCooldownManager().isCoolingDown(this)) {
                return TypedActionResult.pass(itemStack);
            }

            // Set the current hand to initiate the use action (animation + call to finishUsing later)
            user.setCurrentHand(hand);
            return TypedActionResult.consume(itemStack);
        }
        // Define the action performed when using the item (e.g., eating, drinking)
        @Override
        public UseAction getUseAction(ItemStack stack) {
            try {
                // Convert config string to UseAction enum. Default to EAT if invalid.
                return UseAction.valueOf(configEntry.useAction.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                VitalityAids.LOGGER.warn("Invalid UseAction '{}' in config for medicine '{}'. Defaulting to EAT. Error: {}",
                        configEntry.useAction, configEntry.id, e.getMessage());
                return UseAction.EAT;
            }
        }

        // Define how long it takes to use the item (application time)
        @Override
        public int getMaxUseTime(ItemStack stack, LivingEntity user) {
            return this.configEntry.applicationTimeTicks;
        }

        /**
         * Called when a player right-clicks an entity while holding this item.
         * This handles applying the medicine to other entities.
         *
         * @param stack The ItemStack of the medicine being used.
         * @param user The player using the item.
         * @param target The LivingEntity being targeted.
         * @param hand The hand the item is in.
         * @return ActionResult.SUCCESS if applied, ActionResult.PASS otherwise.
         */
        @Override
        public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity target, Hand hand) {
            // Only proceed on the server side to prevent desync
            if (user.getWorld().isClient()) {
                return ActionResult.PASS;
            }

            // Prevent use if the item is on cooldown for the player
            if (user.getItemCooldownManager().isCoolingDown(this)) {
                return ActionResult.PASS;
            }
            if (!configEntry.canApplyToOthers) {
                return ActionResult.PASS;
            }

            user.setCurrentHand(hand);


            // Apply effects directly to the target entity
            applyMedicineEffects(stack, user.getWorld(), target, user); // Pass the user as the source

            // Handle item stack reduction (consume one item)
            if (!user.getAbilities().creativeMode) {
                stack.decrement(1);
            }

            // Apply cooldown for the player
            if (configEntry.cooldownTicks > 0) {
                user.getItemCooldownManager().set(this, configEntry.cooldownTicks);
            }

            return ActionResult.SUCCESS;
        }

        /**
         * Shared method to apply all configured medicine effects (healing, clearing, giving effects)
         * to a target LivingEntity.
         *
         * @param stack The ItemStack being used.
         * @param world The world.
         * @param target The LivingEntity to apply effects to.
         * @param source The LivingEntity that is using the item (e.g., the player).
         */
        private void applyMedicineEffects(ItemStack stack, World world, LivingEntity target, LivingEntity source) {
            // Only apply effects on the server side
            if (world.isClient()) {
                return;
            }

            // Apply healing to the target
            if (this.configEntry.healingAmount > 0) {
                target.heal(this.configEntry.healingAmount);
            }

            // Clear specific effects from the target
            for (String effectId : this.configEntry.clearsEffects) {
                Identifier id = Identifier.of(effectId);

                Optional<RegistryEntry.Reference<StatusEffect>> optEntry =
                        Registries.STATUS_EFFECT.getEntry(id);

                if (optEntry.isPresent()) {
                    target.removeStatusEffect(optEntry.get());
                } else {
                    VitalityAids.LOGGER.warn("Attempted to clear unknown status effect ID: {}", effectId);
                }
            }


            // Clear Hemorrhage from the target if configured
            if (this.configEntry.clearsHemorrhage) {
                if (target.hasStatusEffect(HemorrhageEffect.HEMORRHAGE)) {
                    target.removeStatusEffect(HemorrhageEffect.HEMORRHAGE);
                }
            }


            // Give specific effects to the target
            for (Map.Entry<String, VitalityAidsConfig.EffectParameters> entry : this.configEntry.givesEffects.entrySet()) {
                Identifier id = Identifier.of(entry.getKey());

                Optional<RegistryEntry.Reference<StatusEffect>> optEntry = Registries.STATUS_EFFECT.getEntry(id);

                if (optEntry.isPresent()) {
                    RegistryEntry<StatusEffect> effectEntry = optEntry.get();

                    int duration = entry.getValue().durationTicks;
                    int amplifier = entry.getValue().amplifier;

                    // Add status effect to the target
                    target.addStatusEffect(new StatusEffectInstance(effectEntry, duration, amplifier, false, true));
                } else {
                    VitalityAids.LOGGER.warn("Attempted to give unknown status effect ID: {}", entry.getKey());
                }
            }

}
            // This method is called when the item application is finished (after maxUseTime)
        @Override
            public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
            // Only apply effects on the server side to prevent desync
            if (!world.isClient()) {
                // Apply healing
                if (this.configEntry.healingAmount > 0) {
                    user.heal(this.configEntry.healingAmount);
                }

                // Clear specific effects from the target
                for (String effectId : this.configEntry.clearsEffects) {
                    Identifier id = Identifier.of(effectId);

                    Optional<RegistryEntry.Reference<StatusEffect>> optEntry =
                            Registries.STATUS_EFFECT.getEntry(id);

                    if (optEntry.isPresent()) {
                        user.removeStatusEffect(optEntry.get());
                    } else {
                        VitalityAids.LOGGER.warn("Attempted to clear unknown status effect ID: {}", effectId);
                    }
                }

                // Clear Hemorrhage if configuredf
                if (this.configEntry.clearsHemorrhage) {
                    if (user.hasStatusEffect(HemorrhageEffect.HEMORRHAGE))
                    user.removeStatusEffect(HemorrhageEffect.HEMORRHAGE); // Use your registered RegistryEntry<StatusEffect>
                }

// Apply configured effects
                for (Map.Entry<String, VitalityAidsConfig.EffectParameters> entry : this.configEntry.givesEffects.entrySet()) {
                    Identifier id = Identifier.of(entry.getKey());

                    Optional<RegistryEntry.Reference<StatusEffect>> optEntry = Registries.STATUS_EFFECT.getEntry(id);

                    if (optEntry.isPresent()) {
                        RegistryEntry<StatusEffect> effectEntry = optEntry.get();

                        int duration = entry.getValue().durationTicks;
                        int amplifier = entry.getValue().amplifier;

                        user.addStatusEffect(new StatusEffectInstance(effectEntry, duration, amplifier, false, true));
                    } else {
                        VitalityAids.LOGGER.warn("Attempted to apply unknown status effect ID: {}", entry.getKey());
                    }
                }


                // Handle item stack reduction (consume one item)
                PlayerEntity p = (PlayerEntity) user;
                if (!p.getAbilities().creativeMode) {
                    stack.decrement(1);
                }

                // Apply cooldown (if applicable)
                if (user instanceof net.minecraft.entity.player.PlayerEntity player) {
                    if (this.configEntry.cooldownTicks > 0) {
                        player.getItemCooldownManager().set(this, this.configEntry.cooldownTicks);
                    }
                }
            }
            // --- NEW: Particle burst on finish ---
            if (world.isClient()) {
                Identifier id = Identifier.of(configEntry.finishUseParticleId);
                Optional<RegistryEntry.Reference<ParticleType<?>>> optEntry = Registries.PARTICLE_TYPE.getEntry(id);

                RegistryEntry<ParticleType<?>> particleEntry = optEntry
                        .map(entry -> (RegistryEntry<ParticleType<?>>) entry)
                        .orElse(Registries.PARTICLE_TYPE.getEntry(ParticleTypes.TOTEM_OF_UNDYING));

                if (particleEntry.value() instanceof ParticleEffect particleEffect) {
                    for (int i = 0; i < configEntry.finishUseParticleCount; i++) {
                        double x = user.getX() + (world.random.nextDouble() - 0.5) * user.getWidth();
                        double y = user.getY() + world.random.nextDouble() * user.getHeight() / 2.0;
                        double z = user.getZ() + (world.random.nextDouble() - 0.5) * user.getWidth();
                        world.addParticle(particleEffect, x, y, z, 0.0, 0.0, 0.0);
                    }
                } else {
                    VitalityAids.LOGGER.warn("Invalid particle ID for finishUseParticleId: {}", configEntry.finishUseParticleId);
                }
            }
// --- END NEW ---
            return stack;

}
            // --- Tooltip Logic ---
        @Override
        public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {


            // Add application time to tooltip
            if (configEntry.applicationTimeTicks > 0) {
                float applicationTimeSeconds = configEntry.applicationTimeTicks / 20.0f;
                // Changed "%.1f" to "%.0f" to display as integer
                tooltip.add(Text.translatable("tooltip.vitality_aids.application_time", String.format("%.0f", applicationTimeSeconds)).formatted(net.minecraft.util.Formatting.YELLOW));
            }

            // Add cooldown to tooltip
            if (configEntry.cooldownTicks > 0) {
                float cooldownSeconds = configEntry.cooldownTicks / 20.0f;
                // Changed "%.1f" to "%.0f" to display as integer
                tooltip.add(Text.translatable("tooltip.vitality_aids.cooldown_time", String.format("%.0f", cooldownSeconds)).formatted(net.minecraft.util.Formatting.DARK_GREEN));
            }

            // Add custom tooltip lines from config
            for (String line : this.configEntry.tooltipLines) {
                tooltip.add(Text.literal(line));
            }
            super.appendTooltip(stack, context, tooltip, type);
        }
        @Override
        public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
            super.usageTick(world, user, stack, remainingUseTicks); // Retain base behavior

            if (world.isClient() && configEntry.usageTickParticleFrequency > 0) {
                if (world.random.nextInt(configEntry.usageTickParticleFrequency) == 0) {
                    Identifier id = Identifier.of(configEntry.usageTickParticleId);
                    Optional<RegistryEntry.Reference<ParticleType<?>>> optEntry = Registries.PARTICLE_TYPE.getEntry(id);

                    RegistryEntry<ParticleType<?>> particleEntry = optEntry
                            .map(entry -> (RegistryEntry<ParticleType<?>>) entry)
                            .orElse(Registries.PARTICLE_TYPE.getEntry(ParticleTypes.INSTANT_EFFECT));

                    if (particleEntry.value() instanceof ParticleEffect particleEffect) {
                        double x = user.getX() + (world.random.nextDouble() - 0.5) * user.getWidth();
                        double y = user.getY() + world.random.nextDouble() * user.getHeight();
                        double z = user.getZ() + (world.random.nextDouble() - 0.5) * user.getWidth();
                        world.addParticle(particleEffect, x, y, z, 0.0, 0.0, 0.0);
                    } else {
                        VitalityAids.LOGGER.warn("Invalid particle type for usageTickParticleId: {}", configEntry.usageTickParticleId);
                    }
                }
            }
        }


        // Handle stopping usage prematurely
        @Override
        public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
            // No specific action needed here unless you want to cancel effects or return items
            // For now, it simply stops the consumption
        }
    }
