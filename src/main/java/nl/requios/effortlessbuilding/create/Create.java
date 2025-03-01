package nl.requios.effortlessbuilding.create;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import nl.requios.effortlessbuilding.EffortlessBuilding;
import org.slf4j.Logger;

public class Create {
    public static final String ID = EffortlessBuilding.MODID;

    public static final Logger LOGGER = LogUtils.getLogger();

    public static ResourceLocation asResource(String path) {
        return ResourceLocation.fromNamespaceAndPath(EffortlessBuilding.MODID, path);
    }
}
