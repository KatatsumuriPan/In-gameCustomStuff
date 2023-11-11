package kpan.ig_custom_stuff.network.client;

import io.netty.buffer.ByteBuf;
import kpan.ig_custom_stuff.ModMain;
import kpan.ig_custom_stuff.network.MessageBase;
import kpan.ig_custom_stuff.network.MessageUtil;
import kpan.ig_custom_stuff.network.MyPacketHandler;
import kpan.ig_custom_stuff.network.server.MessageDeleteTexturesToClient;
import kpan.ig_custom_stuff.resource.DynamicResourceManager.Server;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MessageDeleteTexturesToServer extends MessageBase {
	//デフォルトコンストラクタは必須
	public MessageDeleteTexturesToServer() { }

	private List<ResourceLocation> textureIds;

	public MessageDeleteTexturesToServer(List<ResourceLocation> textureIds) {
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
		EntityPlayerMP sender = ctx.getServerHandler().player;
		MinecraftServer server = sender.server;

		if (textureIds.isEmpty()) {
			sender.server.sendMessage(new TextComponentString("INVALID PACKET:no textures be removing"));
			return;
		}

		List<ResourceLocation> succeeded = new ArrayList<>();
		for (ResourceLocation textureId : textureIds) {
			try {
				if (Server.INSTANCE.removeTexture(textureId))
					succeeded.add(textureId);
				else {
					TextComponentTranslation component = new TextComponentTranslation("registry_message.texture.error.not_found", textureId);
					MessageUtil.sendToServerAndAllPlayers(server, component);
				}
			} catch (IOException e) {
				ModMain.LOGGER.error("Failed to delete a texture file", e);
				TextComponentTranslation component = new TextComponentTranslation("registry_message.texture.delete.failed", textureId);
				MessageUtil.sendToServerAndAllPlayers(server, component);
			}
		}

		if (!succeeded.isEmpty()) {
			MyPacketHandler.sendToAllPlayers(new MessageDeleteTexturesToClient(succeeded));
			TextComponentTranslation component;
			if (succeeded.size() == 1) {
				component = new TextComponentTranslation("registry_message.texture.delete.success", succeeded.get(0), sender.getDisplayName());
			} else {
				component = new TextComponentTranslation("registry_message.texture.delete.success.multiple", succeeded.size(), sender.getDisplayName());
			}
			MessageUtil.sendToServerAndAllPlayers(server, component);
		}

	}

}
