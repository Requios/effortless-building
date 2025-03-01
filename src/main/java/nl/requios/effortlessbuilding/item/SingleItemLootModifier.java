package nl.requios.effortlessbuilding.item;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

//Adds a single item with a chance to any loot tables. Specify loot tables in the JSON file.
//Add JSON files to resources/data/effortlessbuilding/loot_modifiers, and list them in resources/data/forge/loot_modifiers/global_loot_modifiers.json
//https://forge.gemwire.uk/wiki/Dynamic_Loot_Modification
//https://forums.minecraftforge.net/topic/112960-1182-solved-adding-modded-items-to-existing-vanilla-loot-tables/
//https://mcreator.net/wiki/minecraft-vanilla-loot-tables-list#toc-index-1
public class SingleItemLootModifier extends LootModifier {

    public static final Supplier<MapCodec<SingleItemLootModifier>> CODEC = Suppliers.memoize(() ->
            RecordCodecBuilder.mapCodec(instance -> codecStart(instance).and(
                    instance.group(
                            Codec.FLOAT.fieldOf("chance").forGetter(m -> m.chance),
                            ItemStack.SINGLE_ITEM_CODEC.fieldOf("item").forGetter(m -> m.stack)
                    )).apply(instance, SingleItemLootModifier::new)
            ));

    private final float chance;
    private final ItemStack stack;

    public SingleItemLootModifier(LootItemCondition[] conditionsIn, float chance, ItemStack stack) {
        super(conditionsIn);
        this.chance = chance;
        this.stack = stack;
    }

    @NotNull
    @Override
    public ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        //
        // Additional conditions can be checked, though as much as possible should be parameterized via JSON data.
        // It is better to write a new ILootCondition implementation than to do things here.
        //
        //with chance, add an item
        if (context.getRandom().nextFloat() < chance) {
            generatedLoot.add(stack.copy());
        }
        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC.get();
    }
}
