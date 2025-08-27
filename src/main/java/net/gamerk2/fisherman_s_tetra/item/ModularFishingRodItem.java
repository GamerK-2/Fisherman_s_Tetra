package net.gamerk2.fisherman_s_tetra.item;

import net.gamerk2.fisherman_s_tetra.FishermanConfigHandler;
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
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.registries.ObjectHolder;
import org.jetbrains.annotations.NotNull;
import se.mickelus.mutil.network.PacketHandler;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.items.modular.ItemModularHandheld;
import se.mickelus.tetra.module.SchematicRegistry;
import se.mickelus.tetra.module.schematic.RepairSchematic;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class ModularFishingRodItem extends ItemModularHandheld {
    public static final String rodkey = "fishingrod/rod";

    public static final String reelkey = "fishingrod/reel";

    public static final String identifier = "modular_fishingrod";

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

        IModularItem.putModuleInSlot(itemStack, rodkey, "fishingrod/rod", "fishingrod/rod_material", "rod/stick");
        IModularItem.putModuleInSlot(itemStack, reelkey, "fishingrod/reel", "fishingrod/reel_material", "reel/oak");

        IModularItem.updateIdentifier(itemStack);
        return itemStack;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level pLevel, Player pPlayer, @NotNull InteractionHand pHand) {
        ItemStack itemstack = pPlayer.getItemInHand(pHand);
        Level world = pPlayer.level();
        if (pPlayer.fishing != null) {
            if (!pLevel.isClientSide) {
                if (!world.isClientSide) {
                    this.applyDamage(1, itemstack, pPlayer);
                }
                int i = pPlayer.fishing.retrieve(itemstack);
                // if the hooked object was a fish, this will tick the honing progression
                if (itemstack.getItem() instanceof IModularItem && i == 1) {
                    tickHoningProgression(pPlayer, itemstack, 2);
                }
            }

            pLevel.playSound(null, pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(), SoundEvents.FISHING_BOBBER_RETRIEVE, SoundSource.NEUTRAL, 1.0F, 0.4F / (pLevel.getRandom().nextFloat() * 0.4F + 0.8F));
            pPlayer.gameEvent(GameEvent.ITEM_INTERACT_FINISH);

        } else {
            pLevel.playSound(null, pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(), SoundEvents.FISHING_BOBBER_THROW, SoundSource.NEUTRAL, 0.5F, 0.4F / (pLevel.getRandom().nextFloat() * 0.4F + 0.8F));
            if (!pLevel.isClientSide) {
                int k = EnchantmentHelper.getFishingSpeedBonus(itemstack);
                int j = EnchantmentHelper.getFishingLuckBonus(itemstack);
                pLevel.addFreshEntity(new FishingHook(pPlayer, pLevel, j, k));
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
}
