package nl.requios.effortlessbuilding.utilities;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import nl.requios.effortlessbuilding.EffortlessBuilding;
import nl.requios.effortlessbuilding.capability.CapabilityHandler;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

//Common
public class BlockSet extends HashMap<BlockPos, BlockEntry> implements Iterable<BlockEntry> {
    public static boolean logging = true;

    public BlockPos firstPos;
    public BlockPos lastPos;
    public boolean skipFirst;

    public BlockSet() {
        super();
    }

    public BlockSet(BlockSet blockSet) {
        super(blockSet);
        this.firstPos = blockSet.firstPos;
        this.lastPos = blockSet.lastPos;
        this.skipFirst = blockSet.skipFirst;
    }

    public BlockSet(List<BlockEntry> blockEntries, BlockPos firstPos, BlockPos lastPos, boolean skipFirst) {
        super();
        for (BlockEntry blockEntry : blockEntries) {
            add(blockEntry);
        }
        this.firstPos = firstPos;
        this.lastPos = lastPos;
        this.skipFirst = skipFirst;
    }

    public void setStartPos(BlockEntry startPos) {
        clear();
        add(startPos);
        firstPos = startPos.blockPos;
        lastPos = startPos.blockPos;
    }

    public void add(BlockEntry blockEntry) {
        if (!containsKey(blockEntry.blockPos)) {
            //check if we are clientside
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                if (!ClientSide.isFull(this))
                    put(blockEntry.blockPos, blockEntry);
            });
            DistExecutor.unsafeRunWhenOn(Dist.DEDICATED_SERVER, () -> () -> {
                put(blockEntry.blockPos, blockEntry);
            });
        } else {
            if (logging) EffortlessBuilding.log("BlockSet already contains block at " + blockEntry.blockPos);
        }
    }

    public HashSet<BlockPos> getCoordinates() {
        return new HashSet<>(keySet());
    }

    public BlockEntry getFirstBlockEntry() {
        return get(firstPos);
    }

    public BlockEntry getLastBlockEntry() {
        return get(lastPos);
    }

    @NotNull
    @Override
    public Iterator<BlockEntry> iterator() {
        return this.values().iterator();
    }

    public static void encode(FriendlyByteBuf buf, BlockSet block) {
        buf.writeCollection(block.values().stream().filter(be -> !be.invalid).toList(), BlockEntry::encode);
        buf.writeBlockPos(block.firstPos);
        buf.writeBlockPos(block.lastPos);
        buf.writeBoolean(block.skipFirst);
    }

    public static BlockSet decode(FriendlyByteBuf buf) {
        return new BlockSet(
            buf.readList(BlockEntry::decode),
            buf.readBlockPos(),
            buf.readBlockPos(),
            buf.readBoolean());
    }

    @OnlyIn(Dist.CLIENT)
    public static class ClientSide {
        public static boolean isFull(BlockSet blockSet) {
            //Limit number of blocks you can place
            int limit = CapabilityHandler.getMaxBlocksPlacedAtOnce(net.minecraft.client.Minecraft.getInstance().player, false);
            if (blockSet.size() >= limit) {
                if (logging) EffortlessBuilding.log("BlockSet limit reached, not adding block.");
                return true;
            }
            return false;
        }
    }
}
