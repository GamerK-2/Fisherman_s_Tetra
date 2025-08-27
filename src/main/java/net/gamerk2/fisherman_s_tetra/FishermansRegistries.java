package net.gamerk2.fisherman_s_tetra;

import net.gamerk2.fisherman_s_tetra.item.ModularFishingRodItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import se.mickelus.tetra.TetraMod;

public class FishermansRegistries {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, TetraMod.MOD_ID);

    public static void init(IEventBus bus) {
        ITEMS.register(bus);

        ITEMS.register(ModularFishingRodItem.identifier, ModularFishingRodItem::new);
    }
}
