package kpan.ig_custom_stuff.proxy;

import kpan.ig_custom_stuff.ModTagsGenerated;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;

@SuppressWarnings("unused")
public class ClientProxy extends CommonProxy {

	@SuppressWarnings("RedundantMethodOverride")
	@Override
	public void registerOnlyClient() {
	}

	@Override
	public boolean hasClientSide() { return true; }


	@Override
	public void registerSingleModel(Item item, int meta, String id) {
		ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(item.getRegistryName(), id));
	}

	@Override
	public void registerMultiItemModel(Item item, int meta, String filename, String id) {
		ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(new ResourceLocation(ModTagsGenerated.MODID, filename), id));
	}
}
