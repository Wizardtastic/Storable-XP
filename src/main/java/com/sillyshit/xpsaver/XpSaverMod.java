package com.sillyshit.xpsaver;

import net.fabricmc.api.ModInitializer;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XpSaverMod implements ModInitializer {
    public static final String MOD_ID = "xp_saver";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final Item XP_STORAGE_BOTTLE = new XpStorageBottleItem(new Item.Settings().maxCount(1));

    @Override
    public void onInitialize() {
        Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "xp_storage_bottle"), XP_STORAGE_BOTTLE);
        LOGGER.info("XP Saver loaded!");
    }
}
