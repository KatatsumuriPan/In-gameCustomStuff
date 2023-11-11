package kpan.ig_custom_stuff.network.server;

import io.netty.buffer.ByteBuf;
import kpan.ig_custom_stuff.gui.item.GuiItemMenu;
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
import java.util.Collections;

public class MessageDeleteItemEntryToClient extends MessageBase {


	//デフォルトコンストラクタは必須
	public MessageDeleteItemEntryToClient() { }

	private ResourceLocation itemId;

	public MessageDeleteItemEntryToClient(ResourceLocation itemId) {
		this.itemId = itemId;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		itemId = new ResourceLocation(readString(buf));
	}

	@Override
	public void toBytes(ByteBuf buf) {
		writeString(buf, itemId.toString());
	}

	@Override
	public void doAction(MessageContext ctx) {
		MCRegistryUtil.removeItem(itemId, true);
		Client.doAction(itemId);
	}

	private static class Client {
		public static void doAction(ResourceLocation itemId) {
			try {
				ClientCache.INSTANCE.removeItemModel(itemId);
				ClientCache.INSTANCE.removeItemNameLang("en_us", itemId);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			RemovedResourcesResourcePack.INSTANCE.addRemovedItem(itemId);
			DynamicResourceLoader.putLang(DynamicResourceManager.toTranslationKeyItem(itemId), DynamicResourceLoader.REMOVED_ITEM_NAME);
			DynamicResourceLoader.loadItemModels(Collections.singletonList(IdConverter.itemId2ItemModelName(itemId)));
			DynamicResourceLoader.reloadItemModelMesh(itemId);
			GuiScreen screen = Minecraft.getMinecraft().currentScreen;
			if (screen instanceof GuiItemMenu guiTextureMenu)
				guiTextureMenu.refreshList();
		}
	}

}
