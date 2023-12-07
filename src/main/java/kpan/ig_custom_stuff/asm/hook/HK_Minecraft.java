package kpan.ig_custom_stuff.asm.hook;

import kpan.ig_custom_stuff.resource.DynamicResourceLoader;
import kpan.ig_custom_stuff.resource.DynamicResourceManager.ClientCache;
import net.minecraft.world.World;

public class HK_Minecraft {

	public static void onUnload(World nextWorld) {
		if (nextWorld != null)
			return;
		DynamicResourceLoader.unregisterTextures(ClientCache.INSTANCE.blockTextureIds.keySet());
		DynamicResourceLoader.unregisterTextures(ClientCache.INSTANCE.itemTextureIds.keySet());
		DynamicResourceLoader.SingleBlockModelLoader.unloadAll();
		ClientCache.INSTANCE.unload();
	}
}
