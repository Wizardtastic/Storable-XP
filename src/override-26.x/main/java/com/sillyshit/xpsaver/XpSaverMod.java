package com.sillyshit.xpsaver;

import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XpSaverMod implements ModInitializer {
    public static final String MOD_ID = "xp_saver";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final Item XP_STORAGE_BOTTLE = new XpStorageBottleItem(new Item.Properties().stacksTo(1).setId(ResourceKey.create(BuiltInRegistries.ITEM.key(), ResourceLocation.fromNamespaceAndPath(MOD_ID, "xp_storage_bottle"))));

    @Override
    public void onInitialize() {
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(MOD_ID, "xp_storage_bottle"), XP_STORAGE_BOTTLE);
        LOGGER.info("XP Saver loaded!");
    }
}
