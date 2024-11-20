package nl.requios.effortlessbuilding.create.foundation.item;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class TagDependentIngredientItem extends Item {

	private TagKey<Item> tag;

	public TagDependentIngredientItem(Properties properties, TagKey<Item> tag) {
		super(properties);
		this.tag = tag;
	}

	public boolean shouldHide() {
		return BuiltInRegistries.ITEM.getTag(tag).isEmpty() || BuiltInRegistries.ITEM.getTag(tag).get().size() == 0;
	}

}
