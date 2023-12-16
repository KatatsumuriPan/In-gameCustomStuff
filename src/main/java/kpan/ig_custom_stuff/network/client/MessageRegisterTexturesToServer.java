package kpan.ig_custom_stuff.network.client;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import kpan.ig_custom_stuff.ModMain;
import kpan.ig_custom_stuff.network.MessageBase;
import kpan.ig_custom_stuff.network.MessageUtil;
import kpan.ig_custom_stuff.network.MyPacketHandler;
import kpan.ig_custom_stuff.network.server.MessageRegisterTexturesToClient;
import kpan.ig_custom_stuff.resource.DynamicResourceManager.Server;
import kpan.ig_custom_stuff.resource.TextureAnimationEntry;
import kpan.ig_custom_stuff.resource.ids.BlockTextureId;
import kpan.ig_custom_stuff.resource.ids.ItemTextureId;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.jetbrains.annotations.Nullable;
import scala.Tuple2;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class MessageRegisterTexturesToServer extends MessageBase {
	//デフォルトコンストラクタは必須
	public MessageRegisterTexturesToServer() { }

	private Map<ItemTextureId, Tuple2<byte[], @Nullable TextureAnimationEntry>> itemTextures;
	private Map<BlockTextureId, Tuple2<byte[], @Nullable TextureAnimationEntry>> blockTextures;

	public MessageRegisterTexturesToServer(Map<ItemTextureId, Tuple2<byte[], @Nullable TextureAnimationEntry>> itemTextures, Map<BlockTextureId, Tuple2<byte[], @Nullable TextureAnimationEntry>> blockTextures) {
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
		EntityPlayerMP sender = ctx.getServerHandler().player;
		MinecraftServer server = sender.server;

		if (itemTextures.isEmpty() && blockTextures.isEmpty()) {
			sender.server.sendMessage(new TextComponentString("INVALID PACKET:no textures uploaded"));
			return;
		}

		Map<ItemTextureId, Tuple2<byte[], @Nullable TextureAnimationEntry>> succeededItem = new Object2ObjectArrayMap<>();
		Map<BlockTextureId, Tuple2<byte[], @Nullable TextureAnimationEntry>> succeededBlock = new Object2ObjectArrayMap<>();
		for (Entry<ItemTextureId, Tuple2<byte[], @Nullable TextureAnimationEntry>> entry : itemTextures.entrySet()) {
			try {
				if (Server.INSTANCE.addTexture(entry.getKey(), entry.getValue()._1, entry.getValue()._2))
					succeededItem.put(entry.getKey(), entry.getValue());
				else {
					TextComponentTranslation component = new TextComponentTranslation("registry_message.texture.error.already_exists", entry.getKey());
					MessageUtil.sendToServerAndAllPlayers(server, component);
				}
			} catch (IOException e) {
				ModMain.LOGGER.error("Failed to save a texture file", e);
				TextComponentTranslation component = new TextComponentTranslation("registry_message.texture.register.failed", entry.getKey());
				MessageUtil.sendToServerAndAllPlayers(server, component);
			}
		}
		for (Entry<BlockTextureId, Tuple2<byte[], @Nullable TextureAnimationEntry>> entry : blockTextures.entrySet()) {
			try {
				if (Server.INSTANCE.addTexture(entry.getKey(), entry.getValue()._1, entry.getValue()._2))
					succeededBlock.put(entry.getKey(), entry.getValue());
				else {
					TextComponentTranslation component = new TextComponentTranslation("registry_message.texture.error.already_exists", entry.getKey());
					MessageUtil.sendToServerAndAllPlayers(server, component);
				}
			} catch (IOException e) {
				ModMain.LOGGER.error("Failed to save a texture file", e);
				TextComponentTranslation component = new TextComponentTranslation("registry_message.texture.register.failed", entry.getKey());
				MessageUtil.sendToServerAndAllPlayers(server, component);
			}
		}

		if (succeededItem.isEmpty() && succeededBlock.isEmpty())
			return;

		MyPacketHandler.sendToAllPlayers(new MessageRegisterTexturesToClient(succeededItem, succeededBlock));
		ITextComponent component;
		if (succeededItem.size() + succeededBlock.size() == 1) {
			if (succeededItem.size() == 1)
				component = new TextComponentTranslation("registry_message.texture.register.success", succeededItem.keySet().iterator().next(), sender.getDisplayName());
			else
				component = new TextComponentTranslation("registry_message.texture.register.success", succeededBlock.keySet().iterator().next(), sender.getDisplayName());
		} else {
			component = new TextComponentTranslation("registry_message.texture.register.success.multiple", succeededItem.size() + succeededBlock.size(), sender.getDisplayName());
		}
		MessageUtil.sendToServerAndAllPlayers(server, component);

	}

}
