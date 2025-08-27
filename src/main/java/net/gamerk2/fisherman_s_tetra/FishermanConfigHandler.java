package net.gamerk2.fisherman_s_tetra;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.gamerk2.fisherman_s_tetra.item.ModularFishingRodItem;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.loading.FMLPaths;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class FishermanConfigHandler {

    private static final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
    public static ForgeConfigSpec spec;

    public static ForgeConfigSpec.IntValue honeFishingRodBase;
    public static ForgeConfigSpec.IntValue honeFishingRodIntegrityMultiplier;

    public static void setup() {
        CommentedFileConfig configData = CommentedFileConfig.builder(FMLPaths.CONFIGDIR.get().resolve("fisherman_s_tetra.toml")).sync().autosave().preserveInsertionOrder().writingMode(WritingMode.REPLACE).build();
        configData.load();
        spec.setConfig(configData);
    }

    @SubscribeEvent
    public static void onLoad(ModConfigEvent.Loading configEvent) {
        onConfigLoad();
    }

    @SubscribeEvent
    public static void onReload(ModConfigEvent.Reloading configEvent) {
        onConfigLoad();
    }

    private static void onConfigLoad() {
        ModularFishingRodItem.instance.updateConfig((Integer) honeFishingRodBase.get(), (Integer) honeFishingRodIntegrityMultiplier.get());
    }

    static {
        builder.comment("Allows tetra items to \"level up\" after being used a certain amount of times, allowing the player to choose from different ways to \"hone\" 1 module on the item. Major modules also settle after some time, increasing its integrity").push("module_progression");
        honeFishingRodBase = builder.comment("The base value for number of uses required before a fishing rod can be honed").defineInRange("hone_fishingrod", 110, Integer.MIN_VALUE, Integer.MAX_VALUE);
        honeFishingRodIntegrityMultiplier = builder.comment("Integrity multiplier for fishing rod honing, a value of 2 would cause a fishing rod which uses 3 integrity to require 2*3 times as many uses before it can be honed").defineInRange("hone_fishingrod_integrity_multiplier", 65, Integer.MIN_VALUE, Integer.MAX_VALUE);

        spec = builder.build();
    }
}
