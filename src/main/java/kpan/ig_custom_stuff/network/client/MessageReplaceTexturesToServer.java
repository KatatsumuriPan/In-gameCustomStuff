package kpan.ig_custom_stuff.network.client;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import kpan.ig_custom_stuff.ModMain;
import kpan.ig_custom_stuff.network.MessageBase;
import kpan.ig_custom_stuff.network.MessageUtil;
import kpan.ig_custom_stuff.network.MyPacketHandler;
import kpan.ig_custom_stuff.network.server.MessageReplaceTexturesToClient;
import kpan.ig_custom_stuff.resource.DynamicResourceManager.Server;
import kpan.ig_custom_stuff.resource.TextureAnimationEntry;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
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

public class MessageReplaceTexturesToServer extends MessageBase {
	//デフォルトコンストラクタは必須
	public MessageReplaceTexturesToServer() { }

	private Map<ResourceLocation, Tuple2<byte[], @Nullable TextureAnimationEntry>> files;

	public MessageReplaceTexturesToServer(Map<ResourceLocation, Tuple2<byte[], TextureAnimationEntry>> files) {
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
		EntityPlayerMP sender = ctx.getServerHandler().player;
		MinecraftServer server = sender.server;

		if (files.isEmpty()) {
			sender.server.sendMessage(new TextComponentString("INVALID PACKET:no textures uploaded"));
			return;
		}

		Map<ResourceLocation, Tuple2<byte[], @Nullable TextureAnimationEntry>> succeeded = new Object2ObjectArrayMap<>();
		for (Entry<ResourceLocation, Tuple2<byte[], @Nullable TextureAnimationEntry>> entry : files.entrySet()) {
			try {
				if (Server.INSTANCE.replaceTexture(entry.getKey(), entry.getValue()._1, entry.getValue()._2))
					succeeded.put(entry.getKey(), entry.getValue());
				else {
					TextComponentTranslation component = new TextComponentTranslation("registry_message.texture.error.not_found", entry.getKey());
					MessageUtil.sendToServerAndAllPlayers(server, component);
				}
			} catch (IOException e) {
				ModMain.LOGGER.error("Failed to save a texture file", e);
				TextComponentTranslation component = new TextComponentTranslation("registry_message.texture.update.failed", entry.getKey());
				MessageUtil.sendToServerAndAllPlayers(server, component);
			}
		}

		if (succeeded.isEmpty())
			return;

		MyPacketHandler.sendToAllPlayers(new MessageReplaceTexturesToClient(succeeded));
		ITextComponent component;
		if (succeeded.size() == 1) {
			component = new TextComponentTranslation("registry_message.texture.update.success", succeeded.keySet().iterator().next(), sender.getDisplayName());
		} else {
			component = new TextComponentTranslation("registry_message.texture.update.success.multiple", succeeded.size(), sender.getDisplayName());
		}
		MessageUtil.sendToServerAndAllPlayers(server, component);

	}

}
