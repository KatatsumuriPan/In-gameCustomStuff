package kpan.ig_custom_stuff.network.server;

import io.netty.buffer.ByteBuf;
import kpan.ig_custom_stuff.gui.texture.GuiTextureMenu;
import kpan.ig_custom_stuff.network.MessageBase;
import kpan.ig_custom_stuff.resource.DynamicResourceLoader;
import kpan.ig_custom_stuff.resource.DynamicResourceManager.ClientCache;
import kpan.ig_custom_stuff.resource.TextureAnimationEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.jetbrains.annotations.Nullable;
import scala.Tuple2;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class MessageReplaceTexturesToClient extends MessageBase {


	//デフォルトコンストラクタは必須
	public MessageReplaceTexturesToClient() { }

	private Map<ResourceLocation, Tuple2<byte[], @Nullable TextureAnimationEntry>> files;

	public MessageReplaceTexturesToClient(Map<ResourceLocation, Tuple2<byte[], TextureAnimationEntry>> files) {
		this.files = files;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		int count = readVarInt(buf);
		files = new HashMap<>();
		for (int i = 0; i < count; i++) {
			ResourceLocation path = new ResourceLocation(readString(buf));
			int length = readVarInt(buf);
			byte[] data = new byte[length];
			buf.readBytes(data);
			@Nullable TextureAnimationEntry animationEntry = null;
			if (buf.readBoolean())
				animationEntry = TextureAnimationEntry.fromByteBuf(buf);
			files.put(path, Tuple2.apply(data, animationEntry));
		}
	}
	@Override
	public void toBytes(ByteBuf buf) {
		writeVarInt(buf, files.size());
		for (Entry<ResourceLocation, Tuple2<byte[], @Nullable TextureAnimationEntry>> entry : files.entrySet()) {
			writeString(buf, entry.getKey().toString());
			writeVarInt(buf, entry.getValue()._1.length);
			buf.writeBytes(entry.getValue()._1);
			if (entry.getValue()._2 != null) {
				buf.writeBoolean(true);
				entry.getValue()._2.writeTo(buf);
			} else {
				buf.writeBoolean(false);
			}
		}
	}

	@Override
	public void doAction(MessageContext ctx) {
		Client.saveAndLoad(files);
	}

	private static class Client {
		public static void saveAndLoad(Map<ResourceLocation, Tuple2<byte[], @Nullable TextureAnimationEntry>> files) {
			for (Entry<ResourceLocation, Tuple2<byte[], @Nullable TextureAnimationEntry>> entry : files.entrySet()) {
				try {
					ClientCache.INSTANCE.replaceTexture(entry.getKey(), entry.getValue()._1, entry.getValue()._2);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
			DynamicResourceLoader.loadTexturesDynamic(files.keySet());
			DynamicResourceLoader.reloadAllChunks();
			GuiScreen screen = Minecraft.getMinecraft().currentScreen;
			if (screen instanceof GuiTextureMenu guiTextureMenu)
				guiTextureMenu.refreshList();
		}
	}
}
