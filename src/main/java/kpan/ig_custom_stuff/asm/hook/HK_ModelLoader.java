package kpan.ig_custom_stuff.asm.hook;

import kpan.ig_custom_stuff.resource.DynamicResourceLoader;
import kpan.ig_custom_stuff.resource.DynamicResourceManager.ClientCache;
import kpan.ig_custom_stuff.resource.ids.BlockTextureId;
import kpan.ig_custom_stuff.resource.ids.ItemTextureId;
import kpan.ig_custom_stuff.util.MyReflectionHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoaderRegistry;

import java.util.Set;

public class HK_ModelLoader {
	public static void postLoadVariantItemModels(Object self) {
		Set<ResourceLocation> textures = MyReflectionHelper.getPrivateStaticField(ModelLoaderRegistry.class, "textures");
		for (ItemTextureId itemTextureId : ClientCache.INSTANCE.itemTextureIds.keySet()) {
			textures.add(itemTextureId.toResourceLocation());
		}
		for (BlockTextureId blockTextureId : ClientCache.INSTANCE.blockTextureIds.keySet()) {
			textures.add(blockTextureId.toResourceLocation());
		}
	}

	public static void addDefaultTextures(Set<ResourceLocation> textures) {
		textures.addAll(DynamicResourceLoader.getDefaultTextureIds());
	}
}
