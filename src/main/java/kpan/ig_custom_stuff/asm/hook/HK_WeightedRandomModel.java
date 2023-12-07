package kpan.ig_custom_stuff.asm.hook;

import kpan.ig_custom_stuff.resource.DynamicResourceLoader;
import net.minecraft.client.renderer.block.model.Variant;
import net.minecraft.client.renderer.block.model.VariantList;
import net.minecraft.util.ResourceLocation;

import java.util.stream.Collectors;

public class HK_WeightedRandomModel {

	public static void onWeightedRandomModelConstructed(ResourceLocation parent, VariantList variants) {
		String path = parent.getPath();
		int index = path.indexOf('#');
		if (index >= 0)
			path = path.substring(0, index);
		DynamicResourceLoader.addBlockModelDependency(new ResourceLocation(parent.getNamespace(), path), variants.getVariantList().stream().map(Variant::getModelLocation).collect(Collectors.toList()));
	}
}
