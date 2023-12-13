package kpan.ig_custom_stuff.network.server;

import io.netty.buffer.ByteBuf;
import kpan.ig_custom_stuff.gui.item.GuiItemMenu;
import kpan.ig_custom_stuff.network.MessageBase;
import kpan.ig_custom_stuff.registry.MCRegistryUtil;
import kpan.ig_custom_stuff.resource.DynamicResourceLoader;
import kpan.ig_custom_stuff.resource.DynamicResourceManager;
import kpan.ig_custom_stuff.resource.DynamicResourceManager.ClientCache;
import kpan.ig_custom_stuff.resource.RemovedResourcesResourcePack;
import kpan.ig_custom_stuff.resource.ids.ItemId;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.io.IOException;
import java.util.Collections;

public class MessageDeleteItemEntryToClient extends MessageBase {


	//デフォルトコンストラクタは必須
	public MessageDeleteItemEntryToClient() { }

	private ItemId itemId;

	public MessageDeleteItemEntryToClient(ItemId itemId) {
		this.itemId = itemId;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		itemId = ItemId.formByteBuf(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		itemId.writeTo(buf);
	}

	@Override
	public void doAction(MessageContext ctx) {
		MCRegistryUtil.removeItem(itemId, true);
		Client.doAction(itemId);
	}

	private static class Client {
		public static void doAction(ItemId itemId) {
			try {
				ClientCache.INSTANCE.removeItemModel(itemId);
				ClientCache.INSTANCE.removeItemNameLang("en_us", itemId);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			RemovedResourcesResourcePack.INSTANCE.addRemovedItem(itemId);
			DynamicResourceLoader.putLang(DynamicResourceManager.toTranslationKeyItem(itemId), DynamicResourceLoader.REMOVED_ITEM_NAME);
			DynamicResourceLoader.loadItemModels(Collections.singletonList(itemId));
			DynamicResourceLoader.reloadItemModelMesh(itemId);
			GuiScreen screen = Minecraft.getMinecraft().currentScreen;
			if (screen instanceof GuiItemMenu guiTextureMenu)
				guiTextureMenu.refreshList();
		}
	}

}
