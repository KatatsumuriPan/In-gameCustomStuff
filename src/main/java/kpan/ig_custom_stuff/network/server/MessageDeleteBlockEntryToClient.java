package kpan.ig_custom_stuff.network.server;

import io.netty.buffer.ByteBuf;
import kpan.ig_custom_stuff.gui.block.GuiBlockMenu;
import kpan.ig_custom_stuff.network.MessageBase;
import kpan.ig_custom_stuff.registry.MCRegistryUtil;
import kpan.ig_custom_stuff.resource.DynamicResourceLoader;
import kpan.ig_custom_stuff.resource.DynamicResourceManager;
import kpan.ig_custom_stuff.resource.DynamicResourceManager.ClientCache;
import kpan.ig_custom_stuff.resource.IdConverter;
import kpan.ig_custom_stuff.resource.RemovedResourcesResourcePack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.io.IOException;

public class MessageDeleteBlockEntryToClient extends MessageBase {


	//デフォルトコンストラクタは必須
	public MessageDeleteBlockEntryToClient() { }

	private ResourceLocation blockId;

	public MessageDeleteBlockEntryToClient(ResourceLocation blockId) {
		this.blockId = blockId;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		blockId = new ResourceLocation(readString(buf));
	}

	@Override
	public void toBytes(ByteBuf buf) {
		writeString(buf, blockId.toString());
	}

	@Override
	public void doAction(MessageContext ctx) {
		MCRegistryUtil.removeBlock(blockId, true);
		Client.doAction(blockId);
	}

	private static class Client {
		public static void doAction(ResourceLocation blockId) {
			try {
				ClientCache.INSTANCE.removeBlockState(blockId);
				ClientCache.INSTANCE.removeItemBlockModel(IdConverter.blockId2ItemModelId(blockId));
				ClientCache.INSTANCE.removeBlockNameLang("en_us", blockId);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			RemovedResourcesResourcePack.INSTANCE.addRemovedBlock(blockId);
			DynamicResourceLoader.putLang(DynamicResourceManager.toTranslationKeyBlock(blockId), DynamicResourceLoader.REMOVED_BLOCK_NAME);
			DynamicResourceLoader.loadBlockResources(blockId);
			DynamicResourceLoader.reloadAllChunks();
			GuiScreen screen = Minecraft.getMinecraft().currentScreen;
			if (screen instanceof GuiBlockMenu guiBlockMenu)
				guiBlockMenu.refreshList();
		}
	}

}
