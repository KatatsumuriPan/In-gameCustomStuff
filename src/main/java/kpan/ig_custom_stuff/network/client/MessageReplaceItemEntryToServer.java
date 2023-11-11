package kpan.ig_custom_stuff.network.client;

import io.netty.buffer.ByteBuf;
import kpan.ig_custom_stuff.ModMain;
import kpan.ig_custom_stuff.item.ItemEntry;
import kpan.ig_custom_stuff.item.ItemLangEntry;
import kpan.ig_custom_stuff.item.model.ItemModelEntry;
import kpan.ig_custom_stuff.network.MessageBase;
import kpan.ig_custom_stuff.network.MessageUtil;
import kpan.ig_custom_stuff.network.MyPacketHandler;
import kpan.ig_custom_stuff.network.server.MessageEditItemEntryToClient;
import kpan.ig_custom_stuff.registry.MCRegistryUtil;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.io.IOException;

public class MessageReplaceItemEntryToServer extends MessageBase {
	//デフォルトコンストラクタは必須
	public MessageReplaceItemEntryToServer() { }

	private ItemEntry itemEntry;
	private ItemModelEntry itemModelEntry;
	private ItemLangEntry itemLangEntry;

	public MessageReplaceItemEntryToServer(ItemEntry itemEntry, ItemModelEntry itemModelEntry, ItemLangEntry itemLangEntry) {
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
		ResourceLocation itemId = itemEntry.itemId;
		if (!MCRegistryUtil.isItemRegistered(itemId)) {
			sender.sendMessage(new TextComponentTranslation("registry_message.item.error.not_found", itemId));
			return;
		}

		try {
			itemEntry.update(false);
		} catch (IOException e) {
			try {
				ModMain.LOGGER.error("Failed to save an item file", e);
				ModMain.LOGGER.error("Trying again...");
				itemEntry.update(false);
			} catch (IOException e2) {
				ModMain.LOGGER.error("Failed to save an item file", e2);
				TextComponentTranslation component = new TextComponentTranslation("registry_message.item.error.io.update.item", itemId);
				MessageUtil.sendToServerAndAllPlayers(server, component);
				return;//アイテム削除失敗は中止
			}
		}
		try {
			itemModelEntry.update(itemEntry.itemId, false);
		} catch (IOException e) {
			try {
				ModMain.LOGGER.error("Failed to save an item model file", e);
				ModMain.LOGGER.error("Trying again...");
				itemModelEntry.update(itemEntry.itemId, false);
			} catch (IOException e2) {
				ModMain.LOGGER.error("Failed to save an item model file", e2);
				TextComponentTranslation component = new TextComponentTranslation("registry_message.item.error.io.update.item_model", itemId);
				MessageUtil.sendToServerAndAllPlayers(server, component);
			}
		}
		try {
			itemLangEntry.update(itemEntry.itemId, false);
		} catch (IOException e) {
			try {
				ModMain.LOGGER.error("Failed to save a lang file", e);
				ModMain.LOGGER.error("Trying again...");
				itemLangEntry.update(itemEntry.itemId, false);
			} catch (IOException e2) {
				ModMain.LOGGER.error("Failed to save a lang", e2);
				TextComponentTranslation component = new TextComponentTranslation("registry_message.item.error.io.update.item_lang", itemId);
				MessageUtil.sendToServerAndAllPlayers(server, component);
			}
		}

		MyPacketHandler.sendToAllPlayers(new MessageEditItemEntryToClient(itemEntry, itemModelEntry, itemLangEntry));

		TextComponentTranslation component = new TextComponentTranslation("registry_message.item.update.success", itemId, sender.getDisplayName());
		server.sendMessage(component);
		for (EntityPlayerMP player : server.getPlayerList().getPlayers()) {
			player.sendMessage(component);
		}
	}

}
