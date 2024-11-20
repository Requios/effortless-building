package nl.requios.effortlessbuilding.utilities;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import nl.requios.effortlessbuilding.EffortlessBuilding;
import nl.requios.effortlessbuilding.attachment.AttachmentHandler;
import nl.requios.effortlessbuilding.attachment.PowerLevel;
import nl.requios.effortlessbuilding.network.message.PowerLevelPacket;

public class PowerLevelCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("powerlevel")
                .requires(cs -> cs.hasPermission(2))
                .then(Commands.literal("query").executes(ctx -> {

                    //Get your own power level
                    logPowerLevel(ctx.getSource(), ctx.getSource().getPlayerOrException());
                    return 0;

                }).then(Commands.argument("target", EntityArgument.player()).executes(ctx -> {

                    //Get power level of some player
                    Player player = EntityArgument.getPlayer(ctx, "target");
                    logPowerLevel(ctx.getSource(), player);
                    return 0;

                })))
                .then(Commands.literal("set")
                .then(Commands.argument("target", EntityArgument.player())
                .then(Commands.argument("value", IntegerArgumentType.integer(0, PowerLevel.MAX_POWER_LEVEL)).executes(ctx -> {

                    //Set power level
                    setPowerLevel(ctx.getSource(), EntityArgument.getPlayer(ctx, "target"), ctx.getArgument("value", Integer.class));
                    return 0;

                })))));
    }

    private static void logPowerLevel(CommandSourceStack source, Player player) {
        int powerLevel = AttachmentHandler.getPowerLevel(player);
        source.sendSuccess(() -> Component.translatable("effortlessbuilding.commands.powerlevel", player.getDisplayName(), powerLevel), false);
    }

    private static void setPowerLevel(CommandSourceStack source, Player player, int powerLevel) throws CommandSyntaxException {
        PowerLevel powerCap = player.getData(EffortlessBuilding.POWER_LEVEL);
        powerCap.setPowerLevel(powerLevel);
        ((ServerPlayer)player).connection.send(new PowerLevelPacket(powerLevel));

        source.sendSuccess(() -> Component.translatable("effortlessbuilding.commands.powerlevel.success", player.getDisplayName(), powerLevel), true);
    }
}
