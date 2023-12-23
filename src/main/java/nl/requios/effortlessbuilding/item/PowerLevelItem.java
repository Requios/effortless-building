package nl.requios.effortlessbuilding.item;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import nl.requios.effortlessbuilding.EffortlessBuilding;
import nl.requios.effortlessbuilding.capability.CapabilityHandler;
import nl.requios.effortlessbuilding.capability.IPowerLevel;
import nl.requios.effortlessbuilding.create.foundation.item.TooltipHelper;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PowerLevelItem extends Item {
    public PowerLevelItem() {
        super(new Item.Properties());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        IPowerLevel powerLevel = player.getCapability(CapabilityHandler.POWER_LEVEL_CAPABILITY).orElse(null);
        if (powerLevel != null) {
            if (powerLevel.canIncreasePowerLevel()) {
                if (!world.isClientSide) {
                    powerLevel.increasePowerLevel();
                    EffortlessBuilding.log(player, "Upgraded power level to " + powerLevel.getPowerLevel());

                    stack.shrink(1);

                    world.playSound((Player) null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1f, 1f);

                    CapabilityHandler.syncToClient(player);
                }

                return InteractionResultHolder.sidedSuccess(stack, world.isClientSide());
            } else {
                if (!world.isClientSide) {
                    EffortlessBuilding.log(player, "Already reached maximum power level!");

                    world.playSound((Player) null, player.blockPosition(), SoundEvents.ARMOR_EQUIP_LEATHER, SoundSource.PLAYERS, 1f, 1f);
                }

                return InteractionResultHolder.fail(player.getItemInHand(hand));
            }
        }

        return super.use(world, player, hand);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag isAdvanced) {
        tooltip.addAll(TooltipHelper.cutTextComponent(Component.translatable(getDescriptionId() + ".desc"), ChatFormatting.GRAY, ChatFormatting.GRAY));
        tooltip.addAll(TooltipHelper.cutTextComponent(Component.translatable("key.effortlessbuilding.upgrade_power_level"), ChatFormatting.BLUE, ChatFormatting.BLUE));
    }
}
