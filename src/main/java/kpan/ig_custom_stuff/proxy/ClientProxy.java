package kpan.ig_custom_stuff.proxy;

import kpan.ig_custom_stuff.ModTagsGenerated;
import kpan.ig_custom_stuff.resource.DynamicResourceLoader.ReloadResourceListener;
import kpan.ig_custom_stuff.resource.RemovedResourcesResourcePack;
import kpan.ig_custom_stuff.util.MyReflectionHelper;
import kpan.ig_custom_stuff.util.handlers.ClientEventHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;

import java.util.List;

@SuppressWarnings("unused")
public class ClientProxy extends CommonProxy {

	@Override
	public void registerOnlyClient() {
		MinecraftForge.EVENT_BUS.register(ClientEventHandler.class);
		((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener(ReloadResourceListener.PRE);
		List<IResourcePack> resourcePackList = MyReflectionHelper.getPrivateField(FMLClientHandler.instance(), "resourcePackList");
		resourcePackList.add(RemovedResourcesResourcePack.INSTANCE);
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

	@Override
	public void postRegisterOnlyClient() {
		((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener(ReloadResourceListener.POST);
	}
}
