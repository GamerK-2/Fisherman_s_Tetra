package net.gamerk2.fisherman_s_tetra.item;

import com.google.common.collect.ImmutableList;
import net.gamerk2.fisherman_s_tetra.FishermanConfigHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.registries.ObjectHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import se.mickelus.mutil.network.PacketHandler;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.items.modular.ItemModularHandheld;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.SchematicRegistry;
import se.mickelus.tetra.module.data.ModuleModel;
import se.mickelus.tetra.module.schematic.RepairSchematic;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Collectors;

@ParametersAreNonnullByDefault
public class ModularFishingRodItem extends ItemModularHandheld {
    public static final String rodkey = "fishingrod/rod";

    public static final String reelkey = "fishingrod/reel";

    public static final String identifier = "modular_fishingrod";

    // fishing line resource location
    protected ModuleModel fishingLineModel0 = new ModuleModel("static", new ResourceLocation(TetraMod.MOD_ID, "item/module/fishingrod/line_0"));

    @ObjectHolder(registryName = "item", value = "tetra:modular_fishingrod")
    public static ModularFishingRodItem instance;

    public ModularFishingRodItem() {
        super(new Item.Properties().stacksTo(1));

        majorModuleKeys = new String[]{rodkey};
        minorModuleKeys = new String[]{reelkey};

        requiredModules = new String[]{rodkey};

        updateConfig(FishermanConfigHandler.honeFishingRodBase.get(), FishermanConfigHandler.honeFishingRodIntegrityMultiplier.get());

        SchematicRegistry.instance.registerSchematic(new RepairSchematic(this, identifier));
    }

    public void updateConfig(int honebase, int honeIntergrityMultiplier) {
        this.honeBase = honebase;
        this.honeIntegrityMultiplier = honeIntergrityMultiplier;
    }

    @Override
    public void commonInit(PacketHandler packetHandler) {
        DataManager.instance.synergyData.onReload(() -> synergies = DataManager.instance.synergyData.getOrdered("fishingrod/"));
    }

    public static ItemStack setupFishingRod() {
        ItemStack itemStack = new ItemStack(instance);

        IModularItem.putModuleInSlot(itemStack, rodkey, "fishingrod/basic_rod", "fishingrod/basic_rod_material", "basic_rod/oak");
        IModularItem.putModuleInSlot(itemStack, reelkey, "fishingrod/basic_reel", "fishingrod/basic_reel_material", "basic_reel/oak");

        IModularItem.updateIdentifier(itemStack);
        return itemStack;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level pLevel, Player pPlayer, @NotNull InteractionHand pHand) {
        ItemStack itemstack = pPlayer.getItemInHand(pHand);
        Level world = pPlayer.level();
        if (pPlayer.fishing != null) {
            if (!pLevel.isClientSide) {
                int i = pPlayer.fishing.retrieve(itemstack);
                // damage the fishing rod by 1 durability when reeling in something (even if nothing was caught)
                if (!world.isClientSide) {
                    this.applyDamage(i, itemstack, pPlayer);
                }
                // if the hooked object was an item, this will tick the honing progression
                if (itemstack.getItem() instanceof IModularItem && i == 1) {
                    tickHoningProgression(pPlayer, itemstack, 2);
                }
            }

            pLevel.playSound(null, pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(), SoundEvents.FISHING_BOBBER_RETRIEVE, SoundSource.NEUTRAL, 1.0F, 0.4F / (pLevel.getRandom().nextFloat() * 0.4F + 0.8F));
            pPlayer.gameEvent(GameEvent.ITEM_INTERACT_FINISH);

        } else {
            pLevel.playSound(null, pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(), SoundEvents.FISHING_BOBBER_THROW, SoundSource.NEUTRAL, 0.5F, 0.4F / (pLevel.getRandom().nextFloat() * 0.4F + 0.8F));
            if (!pLevel.isClientSide) {
                int luck = this.getEnchantmentLevel(itemstack, Enchantments.FISHING_LUCK);
                int speed = this.getEnchantmentLevel(itemstack, Enchantments.FISHING_SPEED);
                pLevel.addFreshEntity(new FishingHook(pPlayer, pLevel, luck, speed));
            }

            pPlayer.awardStat(Stats.ITEM_USED.get(this));
            pPlayer.gameEvent(GameEvent.ITEM_INTERACT_START);
        }

        return InteractionResultHolder.sidedSuccess(itemstack, pLevel.isClientSide());
    }

    @Override
    public boolean canPerformAction(ItemStack stack, net.minecraftforge.common.ToolAction toolAction) {
        return net.minecraftforge.common.ToolActions.DEFAULT_FISHING_ROD_ACTIONS.contains(toolAction);
    }


    private String getCastVariant(@Nullable LivingEntity entity, ItemStack itemStack) {
        if (entity instanceof Player player) {
            if (player.fishing != null && itemStack.getItem() instanceof ModularFishingRodItem && player.getMainHandItem() == itemStack) {
                return "cast";
            }
        }
        return "uncast";
    }

    @Override
    public String getModelCacheKey(ItemStack itemStack, LivingEntity entity) {
        return super.getModelCacheKey(itemStack, entity) + ":" + getCastVariant(entity, itemStack);
    }

    @Override
    public ImmutableList<ModuleModel> getModels(ItemStack itemStack, @Nullable LivingEntity entity) {
        String modelType = getCastVariant(entity, itemStack);

        ImmutableList<ModuleModel> models = getAllModules(itemStack).stream()
                .sorted(Comparator.comparing(ItemModule::getRenderLayer))
                .flatMap(itemModule -> Arrays.stream(itemModule.getModels(itemStack)))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(ModuleModel::getRenderLayer))
                .filter(model -> model.type.equals(modelType) || model.type.equals("static"))
                .collect(Collectors.collectingAndThen(Collectors.toList(), ImmutableList::copyOf));

        if (modelType.equals("uncast")) {
            return ImmutableList.<ModuleModel>builder()
                    .addAll(models)
                    .add(fishingLineModel0)
                    .build();
        }
        return models;
    }
}
