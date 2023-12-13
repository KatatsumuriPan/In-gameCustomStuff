package kpan.ig_custom_stuff.resource;

import net.minecraft.util.ResourceLocation;

public class IdConverter {
	//BlockId			namespace:name
	//ItemId			namespace:name
	//BlockStateId		namespace:name
	//***TextureId		namespace:blocks/path
	//					namespace:items/path
	//***ModelId		namespace:block/path
	//					namespace:item/name
	//***ResourceId		namespace:models/block/name.ext
	//					namespace:models/item/name.ext
	//***ModelName		namespace:name#variant
	//BlockModelFile	namespace:name

	//ModelLoaderRegistry.getModel	ModelIdまたはModelName


	public static ResourceLocation modelId2BlockModelFile(ResourceLocation blockModelId) {
		return new ResourceLocation(blockModelId.getNamespace(), blockModelId.getPath().substring("block/".length()));
	}
	public static ResourceLocation blockModelFile2modelId(ResourceLocation itemId) {
		return new ResourceLocation(itemId.getNamespace(), "block/" + itemId.getPath());
	}


	public static ResourceLocation itemId2ItemModelName(ResourceLocation itemId) {
		return itemId;
	}


	public static ResourceLocation blockId2ItemModelId(ResourceLocation itemId) {
		return new ResourceLocation(itemId.getNamespace(), "item/" + itemId.getPath());
	}

}
