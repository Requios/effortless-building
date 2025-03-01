package nl.requios.effortlessbuilding.create.foundation.block.render;

import net.minecraft.resources.ResourceLocation;
import nl.requios.effortlessbuilding.create.foundation.block.render.ConnectedTextureBehaviour.CTContext;
import nl.requios.effortlessbuilding.create.foundation.block.render.ConnectedTextureBehaviour.ContextRequirement;

public interface CTType {
	ResourceLocation getId();

	int getSheetSize();

	ContextRequirement getContextRequirement();

	int getTextureIndex(CTContext context);
}
