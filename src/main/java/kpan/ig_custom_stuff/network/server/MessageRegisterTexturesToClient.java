package kpan.ig_custom_stuff.network.server;

import io.netty.buffer.ByteBuf;
import kpan.ig_custom_stuff.gui.texture.GuiTextureMenu;
import kpan.ig_custom_stuff.network.MessageBase;
import kpan.ig_custom_stuff.resource.DynamicResourceLoader;
import kpan.ig_custom_stuff.resource.DynamicResourceManager.ClientCache;
import kpan.ig_custom_stuff.resource.TextureAnimationEntry;
import kpan.ig_custom_stuff.resource.ids.BlockTextureId;
import kpan.ig_custom_stuff.resource.ids.ITextureId;
import kpan.ig_custom_stuff.resource.ids.ItemTextureId;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.jetbrains.annotations.Nullable;
import scala.Tuple2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class MessageRegisterTexturesToClient extends MessageBase {


	//デフォルトコンストラクタは必須
	public MessageRegisterTexturesToClient() { }

	private Map<ItemTextureId, Tuple2<byte[], @Nullable TextureAnimationEntry>> itemTextures;
	private Map<BlockTextureId, Tuple2<byte[], @Nullable TextureAnimationEntry>> blockTextures;

	public MessageRegisterTexturesToClient(Map<ItemTextureId, Tuple2<byte[], @Nullable TextureAnimationEntry>> itemTextures, Map<BlockTextureId, Tuple2<byte[], @Nullable TextureAnimationEntry>> blockTextures) {
		this.itemTextures = itemTextures;
		this.blockTextures = blockTextures;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		int count = readVarInt(buf);
		itemTextures = new HashMap<>();
		for (int i = 0; i < count; i++) {
			ItemTextureId itemTextureId = ItemTextureId.formByteBuf(buf);
			int length = readVarInt(buf);
			byte[] data = new byte[length];
			buf.readBytes(data);
			@Nullable TextureAnimationEntry animationEntry = null;
			if (buf.readBoolean())
				animationEntry = TextureAnimationEntry.fromByteBuf(buf);
			itemTextures.put(itemTextureId, Tuple2.apply(data, animationEntry));
		}
		int count2 = readVarInt(buf);
		blockTextures = new HashMap<>();
		for (int i = 0; i < count2; i++) {
			BlockTextureId blockTextureId = BlockTextureId.formByteBuf(buf);
			int length = readVarInt(buf);
			byte[] data = new byte[length];
			buf.readBytes(data);
			@Nullable TextureAnimationEntry animationEntry = null;
			if (buf.readBoolean())
				animationEntry = TextureAnimationEntry.fromByteBuf(buf);
			blockTextures.put(blockTextureId, Tuple2.apply(data, animationEntry));
		}
	}
	@Override
	public void toBytes(ByteBuf buf) {
		writeVarInt(buf, itemTextures.size());
		for (Entry<ItemTextureId, Tuple2<byte[], @Nullable TextureAnimationEntry>> entry : itemTextures.entrySet()) {
			entry.getKey().writeTo(buf);
			writeVarInt(buf, entry.getValue()._1.length);
			buf.writeBytes(entry.getValue()._1);
			if (entry.getValue()._2 != null) {
				buf.writeBoolean(true);
				entry.getValue()._2.writeTo(buf);
			} else {
				buf.writeBoolean(false);
			}
		}
		writeVarInt(buf, blockTextures.size());
		for (Entry<BlockTextureId, Tuple2<byte[], @Nullable TextureAnimationEntry>> entry : blockTextures.entrySet()) {
			entry.getKey().writeTo(buf);
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
		Client.saveAndLoad(itemTextures, blockTextures);
	}

	private static class Client {
		public static void saveAndLoad(Map<ItemTextureId, Tuple2<byte[], TextureAnimationEntry>> itemTextures, Map<BlockTextureId, Tuple2<byte[], TextureAnimationEntry>> blockTextures) {
			List<ITextureId> textureIds = new ArrayList<>();
			for (Entry<ItemTextureId, Tuple2<byte[], TextureAnimationEntry>> entry : itemTextures.entrySet()) {
				try {
					ClientCache.INSTANCE.addTexture(entry.getKey(), entry.getValue()._1, entry.getValue()._2);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				textureIds.add(entry.getKey());
			}
			for (Entry<BlockTextureId, Tuple2<byte[], TextureAnimationEntry>> entry : blockTextures.entrySet()) {
				try {
					ClientCache.INSTANCE.addTexture(entry.getKey(), entry.getValue()._1, entry.getValue()._2);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				textureIds.add(entry.getKey());
			}
			DynamicResourceLoader.loadTexturesDynamic(textureIds);
			DynamicResourceLoader.reloadAllChunks();
			GuiScreen screen = Minecraft.getMinecraft().currentScreen;
			if (screen instanceof GuiTextureMenu guiTextureMenu)
				guiTextureMenu.refreshList();

		}
	}
}
