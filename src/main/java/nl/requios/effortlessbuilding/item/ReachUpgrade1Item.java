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
import nl.requios.effortlessbuilding.CommonConfig;
import nl.requios.effortlessbuilding.EffortlessBuilding;
import nl.requios.effortlessbuilding.attachment.AttachmentHandler;
import nl.requios.effortlessbuilding.attachment.PowerLevel;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ReachUpgrade1Item extends Item {

	public ReachUpgrade1Item() {
		super(new Item.Properties().stacksTo(1));
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		PowerLevel powerLevel = player.getData(EffortlessBuilding.POWER_LEVEL);
		if (powerLevel != null) {
			int currentLevel = powerLevel.getPowerLevel();
			if (currentLevel == 0) {
				if (!world.isClientSide) {
					powerLevel.increasePowerLevel();
					player.setData(EffortlessBuilding.POWER_LEVEL, powerLevel);
					EffortlessBuilding.log(player, "Upgraded power level to " + powerLevel.getPowerLevel());

					stack.shrink(1);

					world.playSound((Player) null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1f, 1f);

					AttachmentHandler.syncToClient(player);
				}
				return InteractionResultHolder.sidedSuccess(stack, world.isClientSide());
			} else if (currentLevel > 0) {
				if (!world.isClientSide && hand == InteractionHand.MAIN_HAND) {
					EffortlessBuilding.log(player, "Already used this upgrade! Current power level is " + powerLevel.getPowerLevel() + ".");

					world.playSound((Player) null, player.blockPosition(), SoundEvents.ARMOR_EQUIP_LEATHER.value(), SoundSource.PLAYERS, 1f, 1f);
				}
			}
		}

		return InteractionResultHolder.fail(player.getItemInHand(hand));
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag tooltipFlag) {
		tooltip.add(Component.literal(ChatFormatting.GRAY + "Consume to increase reach to " + ChatFormatting.BLUE + CommonConfig.reach.level1.get()));
	}

}
