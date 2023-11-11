package kpan.ig_custom_stuff.resource;

import net.minecraft.util.ResourceLocation;

public class IdConverter {
	//BlockId			namespace:name
	//ItemId			namespace:name
	//***TextureId		namespace:blocks/path
	//					namespace:items/path
	//***ModelId		namespace:block/name
	//					namespace:item/name
	//***ResourceId		namespace:models/block/name.ext
	//					namespace:models/item/name.ext
	//***ModelName		namespace:name#variant
	//BlockModelFile	namespace:name

	//ModelLoaderRegistry.getModel	ModelIdまたはModelName

	public static ResourceLocation itemModelId2itemModelName(ResourceLocation itemModelId) {
		return new ResourceLocation(itemModelId.getNamespace(), itemModelId.getPath().substring("item/".length()));
	}

	public static ResourceLocation modelId2BlockModelFile(ResourceLocation blockModelId) {
		return new ResourceLocation(blockModelId.getNamespace(), blockModelId.getPath().substring("block/".length()));
	}
	public static ResourceLocation blockModelFile2modelId(ResourceLocation itemId) {
		return new ResourceLocation(itemId.getNamespace(), "block/" + itemId.getPath());
	}

	public static ResourceLocation resourceId2modelId(ResourceLocation resourceId) {
		return new ResourceLocation(resourceId.getNamespace(), resourceId.getPath().substring("models/".length()));
	}

	public static ResourceLocation itemModelId2ItemId(ResourceLocation itemModelId) {
		return new ResourceLocation(itemModelId.getNamespace(), itemModelId.getPath().substring("item/".length()));
	}
	public static ResourceLocation blockModelId2blockId(ResourceLocation blockModelId) {
		return new ResourceLocation(blockModelId.getNamespace(), blockModelId.getPath().substring("block/".length()));
	}
	public static ResourceLocation resourceId2itemId(ResourceLocation resourceId) {
		return itemModelId2ItemId(resourceId2modelId(resourceId));
	}
	public static ResourceLocation resourceId2blockId(ResourceLocation resourceId) {
		return blockModelId2blockId(resourceId2modelId(resourceId));
	}

	public static ResourceLocation itemId2ItemModelName(ResourceLocation itemId) {
		return itemId;
	}

	public static ResourceLocation itemId2ItemModelId(ResourceLocation itemId) {
		return new ResourceLocation(itemId.getNamespace(), "item/" + itemId.getPath());
	}
	public static ResourceLocation blockId2BlockModelId(ResourceLocation itemId) {
		return new ResourceLocation(itemId.getNamespace(), "block/" + itemId.getPath());
	}

	public static ResourceLocation blockId2ItemModelId(ResourceLocation itemId) {
		return new ResourceLocation(itemId.getNamespace(), "item/" + itemId.getPath());
	}
	public static ResourceLocation blockModelId2itemModelId(ResourceLocation blockModelId) {
		return new ResourceLocation(blockModelId.getNamespace(), "item/" + blockModelId.getPath().substring("block/".length()));
	}

	public static ResourceLocation blockModelId2itemModelName(ResourceLocation blockModelId) {
		return new ResourceLocation(blockModelId.getNamespace(), blockModelId.getPath().substring("block/".length()));
	}

	public static ResourceLocation textureId2resourceId(ResourceLocation textureId) {
		return new ResourceLocation(textureId.getNamespace(), "textures/" + textureId.getPath() + ".png");
	}

	public static boolean isItemModelId(ResourceLocation modelId) {
		return modelId.getPath().startsWith("item/");
	}
	public static boolean isBlockModelId(ResourceLocation modelId) {
		return modelId.getPath().startsWith("block/");
	}
	public static boolean isItemTextureId(ResourceLocation modelId) {
		return modelId.getPath().startsWith("items/");
	}
	public static boolean isBlockTextureId(ResourceLocation modelId) {
		return modelId.getPath().startsWith("blocks/");
	}
}
