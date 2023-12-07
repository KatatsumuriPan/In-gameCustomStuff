package kpan.ig_custom_stuff.asm.hook;

import kpan.ig_custom_stuff.resource.DynamicResourceLoader;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;

public class HK_ModelLoaderRegistry {
	public static void onModelLoaded(ResourceLocation location, IModel model) {
		DynamicResourceLoader.addTextureDependencies(location, model.getTextures());
	}
}
