package kpan.ig_custom_stuff.network.server;

import io.netty.buffer.ByteBuf;
import kpan.ig_custom_stuff.gui.item.GuiItemMenu;
import kpan.ig_custom_stuff.item.ItemEntry;
import kpan.ig_custom_stuff.item.ItemLangEntry;
import kpan.ig_custom_stuff.item.model.ItemModelEntry;
import kpan.ig_custom_stuff.network.MessageBase;
import kpan.ig_custom_stuff.resource.DynamicResourceLoader;
import kpan.ig_custom_stuff.resource.ids.ItemId;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.io.IOException;
import java.util.Collections;

public class MessageEditItemEntryToClient extends MessageBase {


	//デフォルトコンストラクタは必須
	public MessageEditItemEntryToClient() { }

	private ItemEntry itemEntry;
	private ItemModelEntry itemModelEntry;
	private ItemLangEntry itemLangEntry;

	public MessageEditItemEntryToClient(ItemEntry itemEntry, ItemModelEntry itemModelEntry, ItemLangEntry itemLangEntry) {
		this.itemEntry = itemEntry;
		this.itemModelEntry = itemModelEntry;
		this.itemLangEntry = itemLangEntry;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		itemEntry = ItemEntry.fromByteBuf(buf);
		itemModelEntry = ItemModelEntry.fromByteBuf(buf);
		itemLangEntry = ItemLangEntry.fromByteBuf(buf);
	}
	@Override
	public void toBytes(ByteBuf buf) {
		itemEntry.writeTo(buf);
		itemModelEntry.writeTo(buf);
		itemLangEntry.writeTo(buf);
	}
	@Override
	public void doAction(MessageContext ctx) {
		try {
			itemEntry.update(true);
			itemModelEntry.update(itemEntry.itemId, true);
			itemLangEntry.update(itemEntry.itemId, true);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		Client.doAction(itemEntry.itemId);
	}

	private static class Client {
		public static void doAction(ItemId itemId) {
			DynamicResourceLoader.loadItemModels(Collections.singletonList(itemId));
			DynamicResourceLoader.reloadItemModelMesh(itemId);
			GuiScreen screen = Minecraft.getMinecraft().currentScreen;
			if (screen instanceof GuiItemMenu guiTextureMenu)
				guiTextureMenu.refreshList();
		}
	}
}
