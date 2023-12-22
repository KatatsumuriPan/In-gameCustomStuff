package kpan.ig_custom_stuff.network.client;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import kpan.ig_custom_stuff.ModMain;
import kpan.ig_custom_stuff.block.model.BlockModelEntryBase;
import kpan.ig_custom_stuff.network.MessageBase;
import kpan.ig_custom_stuff.network.MessageUtil;
import kpan.ig_custom_stuff.network.MyPacketHandler;
import kpan.ig_custom_stuff.network.server.MessageReplaceBlockModelsToClient;
import kpan.ig_custom_stuff.resource.DynamicResourceManager.Server;
import kpan.ig_custom_stuff.resource.ids.BlockModelId;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class MessageReplaceBlockModelsToServer extends MessageBase {
	//デフォルトコンストラクタは必須
	public MessageReplaceBlockModelsToServer() { }

	private Map<BlockModelId, BlockModelEntryBase> modelId2Entry;

	public MessageReplaceBlockModelsToServer(Map<BlockModelId, BlockModelEntryBase> modelId2Entry) {
		this.modelId2Entry = modelId2Entry;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		int count = readVarInt(buf);
		modelId2Entry = new HashMap<>();
		for (int i = 0; i < count; i++) {
			BlockModelId modelId = BlockModelId.formByteBuf(buf);
			BlockModelEntryBase blockModelEntry = BlockModelEntryBase.fromByteBuf(buf);
			modelId2Entry.put(modelId, blockModelEntry);
		}
	}
	@Override
	public void toBytes(ByteBuf buf) {
		writeVarInt(buf, modelId2Entry.size());
		for (Entry<BlockModelId, BlockModelEntryBase> e : modelId2Entry.entrySet()) {
			e.getKey().writeTo(buf);
			e.getValue().writeTo(buf);
		}
	}

	@Override
	public void doAction(MessageContext ctx) {
		EntityPlayerMP sender = ctx.getServerHandler().player;
		MinecraftServer server = sender.server;

		if (modelId2Entry.isEmpty()) {
			sender.server.sendMessage(new TextComponentString("INVALID PACKET:no block models uploaded"));
			return;
		}

		Map<BlockModelId, BlockModelEntryBase> succeeded = new Object2ObjectArrayMap<>();
		for (Entry<BlockModelId, BlockModelEntryBase> entry : modelId2Entry.entrySet()) {
			try {
				if (Server.INSTANCE.replaceBlockModel(entry.getKey(), entry.getValue()))
					succeeded.put(entry.getKey(), entry.getValue());
				else {
					TextComponentTranslation component = new TextComponentTranslation("registry_message.block_model.error.not_found", entry.getKey());
					MessageUtil.sendToServerAndAllPlayers(server, component);
				}
			} catch (IOException e) {
				ModMain.LOGGER.error("Failed to save a block model file", e);
				TextComponentTranslation component = new TextComponentTranslation("registry_message.block_model.update.failed", entry.getKey());
				MessageUtil.sendToServerAndAllPlayers(server, component);
			}
		}

		if (succeeded.isEmpty()) {
			return;
		}

		MyPacketHandler.sendToAllPlayers(new MessageReplaceBlockModelsToClient(succeeded));
		ITextComponent component;
		if (succeeded.size() == 1) {
			component = new TextComponentTranslation("registry_message.block_model.update.success", succeeded.keySet().iterator().next(), sender.getDisplayName());
		} else {
			component = new TextComponentTranslation("registry_message.block_model.update.success.multiple", succeeded.size(), sender.getDisplayName());
		}
		MessageUtil.sendToServerAndAllPlayers(server, component);
	}

}
