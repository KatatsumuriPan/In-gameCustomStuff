package kpan.ig_custom_stuff.network.server;

import io.netty.buffer.ByteBuf;
import kpan.ig_custom_stuff.gui.texture.GuiTextureMenu;
import kpan.ig_custom_stuff.network.MessageBase;
import kpan.ig_custom_stuff.resource.DynamicResourceLoader;
import kpan.ig_custom_stuff.resource.DynamicResourceManager.ClientCache;
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

	private List<ResourceLocation> textureIds;

	public MessageDeleteTexturesToClient(List<ResourceLocation> textureIds) {
		this.textureIds = textureIds;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		int count = readVarInt(buf);
		textureIds = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			ResourceLocation id = new ResourceLocation(readString(buf));
			textureIds.add(id);
		}
	}
	@Override
	public void toBytes(ByteBuf buf) {
		writeVarInt(buf, textureIds.size());
		for (ResourceLocation textureId : textureIds) {
			writeString(buf, textureId.toString());
		}
	}

	@Override
	public void doAction(MessageContext ctx) {
		Client.saveAndLoad(textureIds);
	}

	private static class Client {
		public static void saveAndLoad(List<ResourceLocation> textureIds) {
			try {
				for (ResourceLocation textureId : textureIds) {
					ClientCache.INSTANCE.removeTexture(textureId);
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
