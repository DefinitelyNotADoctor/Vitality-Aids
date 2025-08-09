package net.vitality_aids;



import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameRules;
import net.vitality_aids.config.VitalityAidsConfig;
import net.vitality_aids.item.CustomMedicineItem;
import net.vitality_aids.item.ModItemGroups;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class VitalityAids implements ModInitializer {
	public static final String MOD_ID = "vitality_aids";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static VitalityAidsConfig CONFIG;

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Vitality Aids!");

		// 1. Initialize Configuration
		CONFIG = VitalityAidsConfig.load();

		// 2. Register custom status effects
		Registry.register(Registries.STATUS_EFFECT, new Identifier(MOD_ID, "hemorrhage"), net.vitality_aids.effects.HemorrhageEffect.INSTANCE);

		// 3. Register custom item groups (call this to ensure your group is registered)
		ModItemGroups.registerItemGroups();

		// 4. Register items from config
		registerConfiguredMedicines();

		VitalityAidsConfig.HemorrhageSettings hemorrhageSettings = VitalityAids.CONFIG.hemorrhageSettings;

		// 5. Set the natural_regeneration game rule to its flag on server start
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			// Get the game rules for the overworld (main world)
			// You might want to apply this to all dimensions if necessary, but overworld is most common.
			GameRules gameRules = server.getGameRules();

			// Get the desired state from the config
			boolean desiredNaturalRegenState = CONFIG.globalSettings.naturalRegenerationEnabled;
			// Get the current state of the game rule
			boolean currentNaturalRegenState = gameRules.getBoolean(GameRules.NATURAL_REGENERATION);

			// Only change the game rule if the current state doesn't match the desired state from config
			if (currentNaturalRegenState != desiredNaturalRegenState) {
				gameRules.get(GameRules.NATURAL_REGENERATION).set(desiredNaturalRegenState, server);
				LOGGER.info("Vitality Aids: Set game rule 'natural_regeneration' to {}. (Previously was {})", desiredNaturalRegenState, currentNaturalRegenState);
			} else {
				LOGGER.info("Vitality Aids: Game rule 'natural_regeneration' is already {}. No change needed.", currentNaturalRegenState);
			}
		});

		LOGGER.info("Vitality Aids initialized!");
	}

	private void registerConfiguredMedicines() {
		for (VitalityAidsConfig.MedicineEntry medicineEntry : CONFIG.medicines) {
				int maxCount = medicineEntry.maxStackCount;
				if(maxCount > 64)
					maxCount = 64;

			Item medicineItem = new CustomMedicineItem(medicineEntry, new FabricItemSettings().maxCount(maxCount));
			Registry.register(Registries.ITEM, new Identifier(MOD_ID, medicineEntry.id), medicineItem);
			LOGGER.info("Registered medicine: {}", medicineEntry.id);

			ItemGroupEvents.modifyEntriesEvent(ModItemGroups.VITALITY_AIDS_GROUP_KEY).register(entries -> entries.add(medicineItem));
		}
	}
}
