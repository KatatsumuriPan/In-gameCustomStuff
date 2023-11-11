package kpan.ig_custom_stuff.network.client;

import io.netty.buffer.ByteBuf;
import kpan.ig_custom_stuff.ModMain;
import kpan.ig_custom_stuff.network.MessageBase;
import kpan.ig_custom_stuff.network.MessageUtil;
import kpan.ig_custom_stuff.network.MyPacketHandler;
import kpan.ig_custom_stuff.network.server.MessageDeleteBlockModelsToClient;
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

public class MessageDeleteBlockModelsToServer extends MessageBase {
	//デフォルトコンストラクタは必須
	public MessageDeleteBlockModelsToServer() { }

	private List<ResourceLocation> modelIds;

	public MessageDeleteBlockModelsToServer(List<ResourceLocation> modelIds) {
		this.modelIds = modelIds;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		int count = readVarInt(buf);
		modelIds = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			ResourceLocation id = new ResourceLocation(readString(buf));
			modelIds.add(id);
		}
	}
	@Override
	public void toBytes(ByteBuf buf) {
		writeVarInt(buf, modelIds.size());
		for (ResourceLocation textureId : modelIds) {
			writeString(buf, textureId.toString());
		}
	}

	@Override
	public void doAction(MessageContext ctx) {
		EntityPlayerMP sender = ctx.getServerHandler().player;
		MinecraftServer server = sender.server;

		if (modelIds.isEmpty()) {
			sender.server.sendMessage(new TextComponentString("INVALID PACKET:no block models are removing"));
			return;
		}

		List<ResourceLocation> succeeded = new ArrayList<>();
		for (ResourceLocation modelId : modelIds) {
			try {
				if (Server.INSTANCE.removeBlockModel(modelId))
					succeeded.add(modelId);
				else {
					TextComponentTranslation component = new TextComponentTranslation("registry_message.block_model.error.not_found", modelId);
					MessageUtil.sendToServerAndAllPlayers(server, component);
				}
			} catch (IOException e) {
				ModMain.LOGGER.error("Failed to delete a block model file", e);
				TextComponentTranslation component = new TextComponentTranslation("registry_message.block_model.delete.failed", modelId);
				MessageUtil.sendToServerAndAllPlayers(server, component);
			}
		}

		if (!succeeded.isEmpty()) {
			MyPacketHandler.sendToAllPlayers(new MessageDeleteBlockModelsToClient(succeeded));
			TextComponentTranslation component;
			if (succeeded.size() == 1) {
				component = new TextComponentTranslation("registry_message.block_model.delete.success", succeeded.get(0), sender.getDisplayName());
			} else {
				component = new TextComponentTranslation("registry_message.block_model.delete.success.multiple", succeeded.size(), sender.getDisplayName());
			}
			MessageUtil.sendToServerAndAllPlayers(server, component);
		}
	}

}
