package net.vitality_aids.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;
import net.vitality_aids.VitalityAids;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

// This class defines the structure of our mod's configuration file.
public class VitalityAidsConfig {
    // Gson instance for reading/writing JSON
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create();
    // Path to the config file
    private static final File CONFIG_FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), VitalityAids.MOD_ID + ".json");

    // --- Global Settings ---
    @Expose // This annotation ensures the field is included in JSON serialization
    @SerializedName("global_settings") // Custom name for the field in JSON
    public GlobalSetting globalSettings = new GlobalSetting();

    // --- General Hemorrhage Settings ---
    @Expose // This annotation ensures the field is included in JSON serialization
    @SerializedName("hemorrhage_settings") // Custom name for the field in JSON
    public HemorrhageSettings hemorrhageSettings = new HemorrhageSettings();

    // --- Medicines Definition ---
    @Expose
    @SerializedName("medicines")
    public List<MedicineEntry> medicines = new ArrayList<>();

    // --- New Inner Class for Effect Parameters ---
    public static class EffectParameters {
        @Expose
        @SerializedName("duration_ticks") // Duration in ticks (20 ticks = 1 second)
        public int durationTicks;

        @Expose
        @SerializedName("amplifier") // Amplifier (0-indexed, so level 1 is 0, level 2 is 1, etc.)
        public int amplifier;

        // Default constructor for Gson
        public EffectParameters() {}

        // Constructor for convenience when setting default values
        public EffectParameters(int durationTicks, int amplifier) {
            this.durationTicks = durationTicks;
            this.amplifier = amplifier;
        }
    }

    // Inner class to hold Hemorrhage configuration
    public static class GlobalSetting {

        @Expose
        @SerializedName("natural_regeneration_enabled")
        public boolean naturalRegenerationEnabled = false;


        @Expose
        @SerializedName("disable_milk_clears_effects")
        public boolean disableMilkClearsEffects = true;

    }
        // Inner class to hold Hemorrhage configuration
    public static class HemorrhageSettings {

        @Expose
        @SerializedName("hemorrhage_effect_enabled")
        public boolean hemorrhageEnabled = true;


        @Expose
        @SerializedName("hemorrhage_prevent_food_healing_enabled")
        public boolean hemorrhagePreventFoodHealEnabled = true;

        @Expose
        @SerializedName("damage_threshold_percent_single_hit") // If player takes > this % of max health, activate
        public double damageThresholdPercentSingleHit = 0.5; // 50%

        @Expose
        @SerializedName("damage_at_low_health_threshold_percent") // If player takes damage below this % health
        public double damageAtLowHealthThresholdPercent = 0.25; // 25%

        @Expose
        @SerializedName("low_health_activation_chance_player") // Chance for player activation at low health
        public double lowHealthActivationChancePlayer = 0.5; // 50%

        @Expose
        @SerializedName("low_health_activation_chance_mob") // Chance for mob activation at low health
        public double lowHealthActivationChanceMob = 0.2; // 20%

        @Expose
        @SerializedName("damage_per_tick") // Damage dealt by hemorrhage each tick
        public double damagePerTick = 0.1;

        @Expose
        @SerializedName("mining_fatigue_level") // Level of mining fatigue during hemorrhage
        public int miningFatigueLevel = 1;

        @Expose
        @SerializedName("weakness_level") // Level of weakness during hemorrhage
        public int weaknessLevel = 1;

        @Expose
        @SerializedName("effect_duration_in_ticks") // Level of weakness during hemorrhage
        public int duration = 9999;

        @Expose
        @SerializedName("effect_amplifier") // Level of weakness during hemorrhage
        public int amplifier = 0;

        @Expose
        @SerializedName("effect_particles") // Level of weakness during hemorrhage
        public boolean particles = true;

        @Expose
        @SerializedName("effect_ambient") // Level of weakness during hemorrhage
        public boolean ambient = false;

        @Expose
        @SerializedName("effect_icon") // Level of weakness during hemorrhage
        public boolean icon = true;

    }

    // Inner class to define each medicine item
    public static class MedicineEntry {
        @Expose
        @SerializedName("id") // The unique ID for the item (e.g., "basic_bandage")
        public String id;

        @Expose
        @SerializedName("max_stack_count") // The max amount per stack
        public int maxStackCount = 16;

        @Expose
        @SerializedName("texture_path") // Path to the item's texture (e.g., "item/basic_bandage")
        public String texturePath;

        @Expose
        @SerializedName("healing_amount") // How much health it restores
        public float healingAmount;

        @Expose
        @SerializedName("clears_hemorrhage") // Whether it clears the hemorrhage effect
        public boolean clearsHemorrhage;

        @Expose
        @SerializedName("can_apply_to_others") // Whether it can be applied to others
        public boolean canApplyToOthers = true;

        @Expose
        @SerializedName("cooldown_ticks") // Cooldown after using (in ticks, 20 ticks = 1 second)
        public int cooldownTicks;

        @Expose
        @SerializedName("application_time_ticks") // Time it takes to apply (in ticks)
        public int applicationTimeTicks;

        @Expose
        @SerializedName("slowness_level_on_apply") // Slowness effect level during application
        public int slownessLevelOnApply;

        @Expose
        @SerializedName("use_action") // "EAT", "DRINK", "NONE"
        public String useAction = "EAT";

        @Expose
        @SerializedName("usage_tick_particle_id") // Particle ID during application (e.g., "minecraft:happy_villager")
        public String usageTickParticleId = "minecraft:happy_villager";

        @Expose
        @SerializedName("usage_tick_particle_frequency") // Lower = more particles (e.g., 5 means 1/5 chance per tick)
        public int usageTickParticleFrequency = 5;

        @Expose
        @SerializedName("finish_use_particle_id") // Particle ID on completion (e.g., "minecraft:heart")
        public String finishUseParticleId = "minecraft:heart";

        @Expose
        @SerializedName("finish_use_particle_count") // Number of particles on completion
        public int finishUseParticleCount = 10;

        @Expose
        @SerializedName("gives_effects") // Optional effects the medicine gives (e.g., "minecraft:regeneration": 1)
        public Map<String, EffectParameters> givesEffects = Collections.emptyMap();

        @Expose
        @SerializedName("clears_effects") // Optional effects the medicine clears (e.g., "minecraft:poison")
        public List<String> clearsEffects = Collections.emptyList();

        @Expose
        @SerializedName("tooltip_lines") // Custom tooltip lines, supports Minecraft's formatting codes
        public List<String> tooltipLines = Collections.emptyList();

        // Constructor for Gson (empty)
        public MedicineEntry() {}

        // Constructor for default values
        public MedicineEntry(String id, String texturePath, int maxStackCount, float healingAmount, boolean clearsHemorrhage, boolean canApplyToOthers, int cooldownTicks, int applicationTimeTicks, int slownessLevelOnApply,
                             String useAction, String usageTickParticleId, int usageTickParticleFrequency,
                             String finishUseParticleId, int finishUseParticleCount,
                             Map<String, EffectParameters> givesEffects, List<String> clearsEffects, List<String> tooltipLines) {
            this.id = id;
            this.texturePath = texturePath;
            this.maxStackCount = maxStackCount;
            this.healingAmount = healingAmount;
            this.clearsHemorrhage = clearsHemorrhage;
            this.canApplyToOthers = canApplyToOthers;
            this.cooldownTicks = cooldownTicks;
            this.applicationTimeTicks = applicationTimeTicks;
            this.slownessLevelOnApply = slownessLevelOnApply;
            this.useAction = useAction;
            this.usageTickParticleId = usageTickParticleId;
            this.usageTickParticleFrequency = usageTickParticleFrequency;
            this.finishUseParticleId = finishUseParticleId;
            this.finishUseParticleCount = finishUseParticleCount;
            this.givesEffects = givesEffects;
            this.clearsEffects = clearsEffects;
            this.tooltipLines = tooltipLines;
        }
    }

    // Loads the config from file, or creates a default if not found
    public static VitalityAidsConfig load() {
        VitalityAidsConfig config = new VitalityAidsConfig();
        if (CONFIG_FILE.exists()) {
            try (Reader reader = new FileReader(CONFIG_FILE)) {
                config = GSON.fromJson(reader, VitalityAidsConfig.class);
                VitalityAids.LOGGER.info("Loaded config from {}", CONFIG_FILE.getAbsolutePath());
            } catch (IOException e) {
                VitalityAids.LOGGER.error("Failed to load config, using default: {}", e.getMessage());
            }
        } else {
            VitalityAids.LOGGER.warn("Config file not found, creating default at {}", CONFIG_FILE.getAbsolutePath());
            config.save(); // Save the default config
        }
        // Ensure that if a config was loaded, it still has default entries if none are defined
        if (config.medicines.isEmpty()) {
            config.addDefaultMedicines();
        }
        return config;
    }

    // Saves the current config to file
    public void save() {
        try (Writer writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(this, writer);
            VitalityAids.LOGGER.info("Saved config to {}", CONFIG_FILE.getAbsolutePath());
        } catch (IOException e) {
            VitalityAids.LOGGER.error("Failed to save config: {}", e.getMessage());
        }
    }

    // Adds default medicine entries to the config
    private void addDefaultMedicines() {
        // Bandage
        medicines.add(new MedicineEntry(
                "bandage",
                "item/bandage",
                16, // max_stack_count
                2.0f, // 1 heart
                true, // Clears Hemorrhage
                true, // Can apply to others
                200, // Cooldown in ticks
                100,  // Application time in ticks
                2, // Slowness II
                "CROSSBOW", // Unique wrapping animation
                "minecraft:instant_effect", // Green particles during application
                4, // Particle frequency
                "minecraft:heart",    // Heart particles on finish
                6, // Particle count
                Collections.emptyMap(), // No additional effects given
                Collections.singletonList("minecraft:mining_fatigue"), // Now clears Mining Fatigue
                List.of("§7A basic vital item, the bandage stops bleeding and restore health.", "§aHeals 1 heart.", "§bClears Mining Fatigue.")
        ));

        // Slime Plaster
        medicines.add(new MedicineEntry(
                "slime_plaster",
                "item/slime_plaster",
                16, // max_stack_count
                3.0f, // 1.5 hearts
                true, // Clears Hemorrhage
                true, // Can apply to others
                160, // Cooldown in ticks
                60,  // Application time in ticks
                1, // Slowness I (minimal)
                "CROSSBOW", // Eating animation
                "minecraft:slime", // Slime particles during application
                2, // Particle frequency
                "minecraft:heart", // Heart particles on finish
                5, // Particle count
                Collections.emptyMap(),
                Collections.emptyList(),
                List.of("§7Made from the sticky slime of slimes this plaster is highly effective in severe conditions.", "§aHeals 1.5 hearts.")
        ));

        // Kelp Plaster
        medicines.add(new MedicineEntry(
                "kelp_plaster",
                "item/kelp_plaster",
                64, // max_stack_count
                2.0f, // 1 heart
                false, // Does NOT clear Hemorrhage
                true, // Can apply to others
                100, // Cooldown in ticks (very short)
                40,  // Application time in ticks (very quick)
                1, // Slowness I (minimal)
                "CROSSBOW", // Eating animation
                "minecraft:bubble_pop", // Watery particles
                1, // Very frequent particles
                "minecraft:heart", // Heart particles on finish
                4, // Particle count
                Collections.emptyMap(),
                Collections.singletonList("minecraft:levitation"), // Now clears Levitation
                List.of("§7A simple and natural remedy known for its quick application.", "§aHeals 1 heart.", "§bClears Levitation.")
        ));

        // Fermented Spider Eye Serum
        medicines.add(new MedicineEntry(
                "fermented_spider_eye_serum",
                "item/fermented_spider_eye_serum",
                8, // max_stack_count (more potent, less stackable)
                0.0f, // No direct healing
                false, // Does NOT clear Hemorrhage
                true, // Can apply to others
                280, // Cooldown in ticks (14 seconds)
                40,  // Application time in ticks
                3, // Slowness III
                "DRINK", // Drinking animation
                "minecraft:effect", // Purple effect particles
                2, // Particle frequency
                "minecraft:witch", // Witchy/dark particles on finish
                10, // Particle count
                Map.of("minecraft:absorption", new EffectParameters(20 * 60 * 2, 0)), // Absorption I for 2 minutes
                Collections.singletonList("minecraft:wither"), // Clears Wither effect
                List.of("§7This serum uses potent properties of fermented spider eyes.", "§bClears Wither.", "§6Grants Absorption.")
        ));

        // Mushroom Tincture
        medicines.add(new MedicineEntry(
                "mushroom_tincture",
                "item/mushroom_tincture",
                16, // max_stack_count
                2.0f, // 1 heart
                true, // Clears Hemorrhage
                true, // Can apply to others
                280, // Cooldown in ticks (14 seconds)
                100,  // Application time in ticks
                3, // Slowness III
                "CROSSBOW", // Drinking animation
                "minecraft:instant_effect", // Green cross particles
                4, // Particle frequency
                "minecraft:happy_villager", // Happy villager particles on finish
                8, // Particle count
                Collections.emptyMap(),
                List.of("minecraft:weakness", VitalityAids.MOD_ID + ":hemorrhage", "minecraft:blindness"), // Clears Weakness, Hemorrhage, and now Blindness
                List.of("§7Extracted from mushrooms, this tincture is a potent cure for both weakness and hemorrhage.", "§aHeals 1 heart.", "§bClears Weakness, Hemorrhage & Blindness.")
        ));

        // Glow Berry Poultice
        medicines.add(new MedicineEntry(
                "glow_berry_poultice",
                "item/glow_berry_poultice",
                16, // max_stack_count
                0.0f, // No direct healing, focuses on buffs
                false, // Does NOT clear Hemorrhage
                true, // Can apply to others
                200, // Cooldown in ticks (10 seconds)
                80,  // Application time in ticks
                2, // Slowness II
                "CROSSBOW", // Eating animation
                "minecraft:glow", // Glow particles during application
                2, // Particle frequency
                "minecraft:glow", // Glow particles on finish
                15, // Particle count
                Map.of(
                        "minecraft:glowing", new EffectParameters(20 * 60 * 3, 0), // Glowing for 3 minutes
                        "minecraft:regeneration", new EffectParameters(20 * 10, 0) // Regeneration I for 10 seconds
                ),
                Collections.singletonList("minecraft:darkness"), // Now clears Darkness
                List.of("§7Made from glow berries, this poultice not only illuminates the user but also promotes rapid healing.", "§eGrants Glowing & Regeneration.", "§bClears Darkness.")
        ));

        // Herbal Salve
        medicines.add(new MedicineEntry(
                "herbal_salve",
                "item/herbal_salve",
                16, // max_stack_count
                0.0f, // No direct healing, focuses on cleansing
                false, // Does NOT clear Hemorrhage
                true, // Can apply to others
                240, // Cooldown in ticks (12 seconds)
                120,  // Application time in ticks
                2, // Slowness II
                "CROSSBOW", // Eating animation
                "minecraft:spore_blossom_ambient", // Green leafy particles
                3, // Particle frequency
                "minecraft:totem_of_undying", // Cleansing particles on finish
                10, // Particle count
                Collections.emptyMap(),
                List.of("minecraft:slowness", "minecraft:nausea"), // Clears Slowness and Nausea
                List.of("§7This salve, made from a blend of forest herbs, is a remedy for those who suffer from effects like slowness and nausea.", "§bClears Slowness & Nausea.")
        ));

        // Honey Ointment
        medicines.add(new MedicineEntry(
                "honey_ointment",
                "item/honey_ointment",
                16, // max_stack_count
                5.0f, // 2.5 hearts
                false, // Does NOT clear Hemorrhage
                true, // Can apply to others
                220, // Cooldown in ticks (11 seconds)
                60,  // Application time in ticks
                2, // Slowness II
                "CROSSBOW", // Eating animation
                "minecraft:dripping_honey", // Honey particles
                2, // Particle frequency
                "minecraft:happy_villager", // Happy villager/positive particles
                12, // Particle count
                Map.of(
                        "minecraft:regeneration", new EffectParameters(20 * 15, 0), // Regeneration I for 15 seconds
                        "minecraft:speed", new EffectParameters(20 * 15, 0) // Speed I for 15 seconds
                ),
                Collections.singletonList("minecraft:hunger"), // Now clears Hunger
                List.of("§7Honey, nature's miracle, is mixed with Ink Sac to create this ointment.", "§7It not only promotes healing but also energizes the user.", "§aHeals 2.5 hearts.", "§9Grants Regeneration & Speed.", "§bClears Hunger.")
        ));

        // Spider Eye Antiseptic
        medicines.add(new MedicineEntry(
                "spider_eye_antiseptic",
                "item/spider_eye_antiseptic",
                16, // max_stack_count
                2.0f, // 1 heart
                false, // Does NOT clear Hemorrhage
                true, // Can apply to others
                320, // Cooldown in ticks (16 seconds)
                40,  // Application time in ticks
                3, // Slowness III
                "CROSSBOW", // Drinking animation
                "minecraft:witch", // Witchy/dark particles
                3, // Particle frequency
                "minecraft:instant_effect", // Green cross for cleansing
                8, // Particle count
                Collections.emptyMap(),
                Collections.singletonList("minecraft:poison"), // Clears Poison effect
                List.of("§7Despite its gruesome ingredients, this antiseptic is highly effective.", "§7Made from spider eyes, it neutralizes poisons and restores health.", "§aHeals 1 heart.", "§bClears Poison.")
        ));
        VitalityAids.LOGGER.info("Added default medicine entries to config.");
        save();
    }
}