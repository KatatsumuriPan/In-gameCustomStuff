package kpan.ig_custom_stuff.network.client;

import io.netty.buffer.ByteBuf;
import kpan.ig_custom_stuff.ModMain;
import kpan.ig_custom_stuff.block.BlockEntry;
import kpan.ig_custom_stuff.block.BlockLangEntry;
import kpan.ig_custom_stuff.block.BlockStateEntry;
import kpan.ig_custom_stuff.network.MessageBase;
import kpan.ig_custom_stuff.network.MessageUtil;
import kpan.ig_custom_stuff.network.MyPacketHandler;
import kpan.ig_custom_stuff.network.server.MessageRegisterBlockEntryToClient;
import kpan.ig_custom_stuff.registry.MCRegistryUtil;
import kpan.ig_custom_stuff.resource.DynamicResourceManager.Server;
import kpan.ig_custom_stuff.resource.IdConverter;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.io.IOException;

public class MessageRegisterBlockEntryToServer extends MessageBase {
	//デフォルトコンストラクタは必須
	public MessageRegisterBlockEntryToServer() { }

	private BlockEntry blockEntry;
	private BlockStateEntry blockStateEntry;
	private BlockLangEntry blockLangEntry;

	public MessageRegisterBlockEntryToServer(BlockEntry blockEntry, BlockStateEntry blockStateEntry, BlockLangEntry blockLangEntry) {
		this.blockEntry = blockEntry;
		this.blockStateEntry = blockStateEntry;
		this.blockLangEntry = blockLangEntry;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		blockEntry = BlockEntry.fromByteBuf(buf);
		blockStateEntry = BlockStateEntry.fromByteBuf(buf);
		blockLangEntry = BlockLangEntry.fromByteBuf(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		blockEntry.writeTo(buf);
		blockStateEntry.writeTo(buf);
		blockLangEntry.writeTo(buf);
	}

	@Override
	public void doAction(MessageContext ctx) {
		EntityPlayerMP sender = ctx.getServerHandler().player;
		MinecraftServer server = sender.server;
		ResourceLocation blockId = blockEntry.blockId;
		if (MCRegistryUtil.getBlockIdErrorMessage(blockId) != null) {
			ModMain.LOGGER.info("INVALID PACKET:blockId \"" + blockId + "\" is invalid(" + MCRegistryUtil.getBlockIdErrorMessage(blockId) + ")");
			if (MCRegistryUtil.isBlockRegistered(blockId))
				sender.sendMessage(new TextComponentTranslation("registry_message.block.error.already_exists", blockId));
			return;
		}

		try {
			blockEntry.register(false);
		} catch (IOException e) {
			try {
				ModMain.LOGGER.error("Failed to save a block file", e);
				ModMain.LOGGER.error("Trying again...");
				blockStateEntry.register(blockEntry.blockId, false);
			} catch (IOException e2) {
				ModMain.LOGGER.error("Failed to save a block file", e2);
				TextComponentTranslation component = new TextComponentTranslation("registry_message.block.error.io.register.block", blockId);
				MessageUtil.sendToServerAndAllPlayers(server, component);
				return;//ブロック追加失敗は中止
			}
		}
		try {
			blockStateEntry.register(blockEntry.blockId, false);
		} catch (IOException e) {
			try {
				ModMain.LOGGER.error("Failed to save a blockstate file", e);
				ModMain.LOGGER.error("Trying again...");
				blockStateEntry.register(blockEntry.blockId, false);
			} catch (IOException e2) {
				ModMain.LOGGER.error("Failed to save a blockstate file", e2);
				TextComponentTranslation component = new TextComponentTranslation("registry_message.block.error.io.register.block_state", blockId);
				MessageUtil.sendToServerAndAllPlayers(server, component);
			}
		}
		try {
			blockLangEntry.register(blockEntry.blockId, false);
		} catch (IOException e) {
			try {
				ModMain.LOGGER.error("Failed to save a lang file", e);
				ModMain.LOGGER.error("Trying again...");
				blockLangEntry.register(blockEntry.blockId, false);
			} catch (IOException e2) {
				ModMain.LOGGER.error("Failed to save a lang file", e2);
				TextComponentTranslation component = new TextComponentTranslation("registry_message.block.error.io.register.block_lang", blockId);
				MessageUtil.sendToServerAndAllPlayers(server, component);
			}
		}
		try {
			Server.INSTANCE.setItemBlockModel(IdConverter.blockId2ItemModelId(blockEntry.blockId), blockStateEntry.blockModelId);
		} catch (IOException e) {
			try {
				ModMain.LOGGER.error("Failed to save an item block model file", e);
				ModMain.LOGGER.error("Trying again...");
				Server.INSTANCE.setItemBlockModel(IdConverter.blockId2ItemModelId(blockEntry.blockId), blockStateEntry.blockModelId);
			} catch (IOException e2) {
				ModMain.LOGGER.error("Failed to save an item block model file", e2);
				TextComponentTranslation component = new TextComponentTranslation("registry_message.block.error.io.register.item_block_model", blockId);
				MessageUtil.sendToServerAndAllPlayers(server, component);
			}
		}
		MyPacketHandler.sendToAllPlayers(new MessageRegisterBlockEntryToClient(blockEntry, blockStateEntry, blockLangEntry));

		TextComponentTranslation component = new TextComponentTranslation("registry_message.block.register.success", blockId, sender.getDisplayName());
		MessageUtil.sendToServerAndAllPlayers(server, component);
	}

}
