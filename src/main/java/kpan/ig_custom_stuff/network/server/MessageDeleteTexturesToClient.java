package kpan.ig_custom_stuff.network.server;

import io.netty.buffer.ByteBuf;
import kpan.ig_custom_stuff.gui.texture.GuiTextureMenu;
import kpan.ig_custom_stuff.network.MessageBase;
import kpan.ig_custom_stuff.resource.DynamicResourceLoader;
import kpan.ig_custom_stuff.resource.DynamicResourceManager.ClientCache;
import kpan.ig_custom_stuff.resource.ids.BlockTextureId;
import kpan.ig_custom_stuff.resource.ids.ITextureId;
import kpan.ig_custom_stuff.resource.ids.ItemTextureId;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MessageDeleteTexturesToClient extends MessageBase {


	//デフォルトコンストラクタは必須
	public MessageDeleteTexturesToClient() { }

	private List<ItemTextureId> itemTextureIds;
	private List<BlockTextureId> blockTextureIds;

	public MessageDeleteTexturesToClient(List<ItemTextureId> itemTextureIds, List<BlockTextureId> blockTextureIds) {
		this.itemTextureIds = itemTextureIds;
		this.blockTextureIds = blockTextureIds;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		int count = readVarInt(buf);
		itemTextureIds = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			ItemTextureId id = ItemTextureId.formByteBuf(buf);
			itemTextureIds.add(id);
		}
		int count2 = readVarInt(buf);
		blockTextureIds = new ArrayList<>();
		for (int i = 0; i < count2; i++) {
			BlockTextureId id = BlockTextureId.formByteBuf(buf);
			blockTextureIds.add(id);
		}
	}
	@Override
	public void toBytes(ByteBuf buf) {
		writeVarInt(buf, itemTextureIds.size());
		for (ItemTextureId textureId : itemTextureIds) {
			textureId.writeTo(buf);
		}
		writeVarInt(buf, blockTextureIds.size());
		for (BlockTextureId textureId : blockTextureIds) {
			textureId.writeTo(buf);
		}
	}

	@Override
	public void doAction(MessageContext ctx) {
		Client.saveAndLoad(itemTextureIds, blockTextureIds);
	}

	private static class Client {
		public static void saveAndLoad(List<ResourceLocation> textureIds) {
		}
		public static void saveAndLoad(List<ItemTextureId> itemTextureIds, List<BlockTextureId> blockTextureIds) {
			List<ITextureId> textureIds = new ArrayList<>();
			try {
				for (ItemTextureId textureId : itemTextureIds) {
					ClientCache.INSTANCE.removeTexture(textureId);
					textureIds.add(textureId);
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			try {
				for (BlockTextureId textureId : blockTextureIds) {
					ClientCache.INSTANCE.removeTexture(textureId);
					textureIds.add(textureId);
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			DynamicResourceLoader.unregisterTextures(textureIds);
			DynamicResourceLoader.reloadAllChunks();
			GuiScreen screen = Minecraft.getMinecraft().currentScreen;
			if (screen instanceof GuiTextureMenu guiTextureMenu)
				guiTextureMenu.refreshList();
		}
	}
}
