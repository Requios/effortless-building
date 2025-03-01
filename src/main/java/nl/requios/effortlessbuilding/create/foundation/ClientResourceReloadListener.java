package nl.requios.effortlessbuilding.create.foundation;

import net.createmod.catnip.lang.LangNumberFormat;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

public class ClientResourceReloadListener implements ResourceManagerReloadListener {

	@Override
	public void onResourceManagerReload(ResourceManager resourceManager) {
//		CreateClient.invalidateRenderers();
//		SoundScapes.invalidateAll();
		LangNumberFormat.numberFormat.update();
	}

}
