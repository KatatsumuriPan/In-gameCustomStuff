package kpan.ig_custom_stuff.network.client;

import io.netty.buffer.ByteBuf;
import kpan.ig_custom_stuff.ModMain;
import kpan.ig_custom_stuff.item.ItemEntry;
import kpan.ig_custom_stuff.item.ItemLangEntry;
import kpan.ig_custom_stuff.item.model.ItemModelEntry;
import kpan.ig_custom_stuff.network.MessageBase;
import kpan.ig_custom_stuff.network.MessageUtil;
import kpan.ig_custom_stuff.network.MyPacketHandler;
import kpan.ig_custom_stuff.network.server.MessageRegisterItemEntryToClient;
import kpan.ig_custom_stuff.registry.MCRegistryUtil;
import kpan.ig_custom_stuff.resource.ids.ItemId;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.io.IOException;

public class MessageRegisterItemEntryToServer extends MessageBase {
	//デフォルトコンストラクタは必須
	public MessageRegisterItemEntryToServer() { }

	private ItemEntry itemEntry;
	private ItemModelEntry itemModelEntry;
	private ItemLangEntry itemLangEntry;

	public MessageRegisterItemEntryToServer(ItemEntry itemEntry, ItemModelEntry itemModelEntry, ItemLangEntry itemLangEntry) {
		this.itemEntry = itemEntry;
		this.itemModelEntry = itemModelEntry;
		this.itemLangEntry = itemLangEntry;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		itemEntry = ItemEntry.fromByteBuf(buf);
		itemModelEntry = ItemModelEntry.fromByteBuf(buf);
		itemLangEntry = ItemLangEntry.fromByteBuf(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		itemEntry.writeTo(buf);
		itemModelEntry.writeTo(buf);
		itemLangEntry.writeTo(buf);
	}

	@Override
	public void doAction(MessageContext ctx) {
		EntityPlayerMP sender = ctx.getServerHandler().player;
		MinecraftServer server = sender.server;
		ItemId itemId = itemEntry.itemId;
		if (MCRegistryUtil.getItemIdErrorMessage(itemId) != null) {
			ModMain.LOGGER.info("INVALID PACKET:itemId \"" + itemId + "\" is invalid(" + MCRegistryUtil.getItemIdErrorMessage(itemId) + ")");
			if (MCRegistryUtil.isItemRegistered(itemId))
				sender.sendMessage(new TextComponentTranslation("registry_message.item.error.already_exists", itemId));
			else if (MCRegistryUtil.isRemovedBlock(itemId.toBlockId()))
				sender.sendMessage(new TextComponentTranslation("registry_message.item.error.need_relaunch", itemId));
			return;
		}

		try {
			itemEntry.register(false);
		} catch (IOException e) {
			try {
				ModMain.LOGGER.error("Failed to save an item file", e);
				ModMain.LOGGER.error("Trying again...");
				itemEntry.register(false);
			} catch (IOException e2) {
				ModMain.LOGGER.error("Failed to save an item file", e2);
				TextComponentTranslation component = new TextComponentTranslation("registry_message.item.error.io.delete.item", itemId);
				MessageUtil.sendToServerAndAllPlayers(server, component);
				return;//アイテム削除失敗は中止
			}
		}
		try {
			itemModelEntry.register(itemEntry.itemId, false);
		} catch (IOException e) {
			try {
				ModMain.LOGGER.error("Failed to save an item model file", e);
				ModMain.LOGGER.error("Trying again...");
				itemModelEntry.register(itemEntry.itemId, false);
			} catch (IOException e2) {
				ModMain.LOGGER.error("Failed to save an item model file", e2);
				TextComponentTranslation component = new TextComponentTranslation("registry_message.item.error.io.delete.item_model", itemId);
				MessageUtil.sendToServerAndAllPlayers(server, component);
			}
		}
		try {
			itemLangEntry.register(itemEntry.itemId, false);
		} catch (IOException e) {
			try {
				ModMain.LOGGER.error("Failed to save a lang file", e);
				ModMain.LOGGER.error("Trying again...");
				itemLangEntry.register(itemEntry.itemId, false);
			} catch (IOException e2) {
				ModMain.LOGGER.error("Failed to save a lang", e2);
				TextComponentTranslation component = new TextComponentTranslation("registry_message.item.error.io.delete.item_lang", itemId);
				MessageUtil.sendToServerAndAllPlayers(server, component);
			}
		}

		MyPacketHandler.sendToAllPlayers(new MessageRegisterItemEntryToClient(itemEntry, itemModelEntry, itemLangEntry));

		TextComponentTranslation component = new TextComponentTranslation("registry_message.item.register.success", itemId, sender.getDisplayName());
		server.sendMessage(component);
		for (EntityPlayerMP player : server.getPlayerList().getPlayers()) {
			player.sendMessage(component);
		}
	}

}
