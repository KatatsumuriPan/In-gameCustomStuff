package kpan.ig_custom_stuff.network.client;

import io.netty.buffer.ByteBuf;
import kpan.ig_custom_stuff.ModMain;
import kpan.ig_custom_stuff.network.MessageBase;
import kpan.ig_custom_stuff.network.MessageUtil;
import kpan.ig_custom_stuff.network.MyPacketHandler;
import kpan.ig_custom_stuff.network.server.MessageDeleteBlockEntryToClient;
import kpan.ig_custom_stuff.registry.DynamicServerRegistryManager;
import kpan.ig_custom_stuff.registry.MCRegistryUtil;
import kpan.ig_custom_stuff.resource.DynamicResourceManager.Server;
import kpan.ig_custom_stuff.resource.ids.BlockId;
import kpan.ig_custom_stuff.resource.ids.BlockStateId;
import kpan.ig_custom_stuff.resource.ids.ItemModelId;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.io.IOException;

public class MessageDeleteBlockEntryToServer extends MessageBase {
	//デフォルトコンストラクタは必須
	public MessageDeleteBlockEntryToServer() { }

	private BlockId blockId;

	public MessageDeleteBlockEntryToServer(BlockId blockId) {
		this.blockId = blockId;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		blockId = BlockId.formByteBuf(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		blockId.writeTo(buf);
	}

	@Override
	public void doAction(MessageContext ctx) {
		EntityPlayerMP sender = ctx.getServerHandler().player;
		MinecraftServer server = sender.server;
		if (!MCRegistryUtil.isBlockRegistered(blockId)) {
			sender.sendMessage(new TextComponentTranslation("registry_message.block.error.not_found", blockId));
			return;
		}

		try {
			DynamicServerRegistryManager.unregisterBlock(blockId);
		} catch (IOException e) {
			try {
				ModMain.LOGGER.error("Failed to delete a block file", e);
				ModMain.LOGGER.error("Trying again...");
				DynamicServerRegistryManager.unregisterBlock(blockId);
			} catch (IOException e2) {
				ModMain.LOGGER.error("Failed to delete a block file", e2);
				TextComponentTranslation component = new TextComponentTranslation("registry_message.block.error.io.delete.block", blockId);
				MessageUtil.sendToServerAndAllPlayers(server, component);
				return;//ブロック削除失敗は中止
			}
		}
		BlockStateId blockStateId = blockId.toBlockStateId();
		try {
			Server.INSTANCE.removeBlockState(blockStateId);
		} catch (IOException e) {
			try {
				ModMain.LOGGER.error("Failed to delete a block state file", e);
				ModMain.LOGGER.error("Trying again...");
				Server.INSTANCE.removeBlockState(blockStateId);
			} catch (IOException e2) {
				ModMain.LOGGER.error("Failed to delete a block state file", e2);
				TextComponentTranslation component = new TextComponentTranslation("registry_message.block.error.io.delete.block_state", blockId);
				MessageUtil.sendToServerAndAllPlayers(server, component);
			}
		}
		try {
			Server.INSTANCE.removeBlockNameLang("en_us", blockId);
		} catch (IOException e) {
			try {
				ModMain.LOGGER.error("Failed to delete a lang file", e);
				ModMain.LOGGER.error("Trying again...");
				Server.INSTANCE.removeBlockNameLang("en_us", blockId);
			} catch (IOException e2) {
				ModMain.LOGGER.error("Failed to delete a lang file", e2);
				TextComponentTranslation component = new TextComponentTranslation("registry_message.block.error.io.delete.block_lang", blockId);
				MessageUtil.sendToServerAndAllPlayers(server, component);
			}
		}
		ItemModelId itemModelId = blockId.toItemId().toModelId();
		try {
			Server.INSTANCE.removeItemBlockModel(itemModelId);
		} catch (IOException e) {
			try {
				ModMain.LOGGER.error("Failed to delete an item block model file", e);
				ModMain.LOGGER.error("Trying again...");
				Server.INSTANCE.removeItemBlockModel(itemModelId);
			} catch (IOException e2) {
				ModMain.LOGGER.error("Failed to delete an item block model file", e2);
				TextComponentTranslation component = new TextComponentTranslation("registry_message.block.error.io.delete.item_block_model", blockId);
				MessageUtil.sendToServerAndAllPlayers(server, component);
			}
		}
		MyPacketHandler.sendToAllPlayers(new MessageDeleteBlockEntryToClient(blockId));

		TextComponentTranslation component = new TextComponentTranslation("registry_message.block.delete.success", blockId, sender.getDisplayName());
		MessageUtil.sendToServerAndAllPlayers(server, component);
	}

}
