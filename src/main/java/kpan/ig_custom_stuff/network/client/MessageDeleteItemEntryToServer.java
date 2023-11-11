package kpan.ig_custom_stuff.network.client;

import io.netty.buffer.ByteBuf;
import kpan.ig_custom_stuff.ModMain;
import kpan.ig_custom_stuff.network.MessageBase;
import kpan.ig_custom_stuff.network.MessageUtil;
import kpan.ig_custom_stuff.network.MyPacketHandler;
import kpan.ig_custom_stuff.network.server.MessageDeleteItemEntryToClient;
import kpan.ig_custom_stuff.registry.DynamicServerRegistryManager;
import kpan.ig_custom_stuff.registry.MCRegistryUtil;
import kpan.ig_custom_stuff.resource.DynamicResourceManager.Server;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.io.IOException;

public class MessageDeleteItemEntryToServer extends MessageBase {
	//デフォルトコンストラクタは必須
	public MessageDeleteItemEntryToServer() { }

	private ResourceLocation itemId;

	public MessageDeleteItemEntryToServer(ResourceLocation itemId) {
		this.itemId = itemId;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		itemId = new ResourceLocation(readString(buf));
	}

	@Override
	public void toBytes(ByteBuf buf) {
		writeString(buf, itemId.toString());
	}

	@Override
	public void doAction(MessageContext ctx) {
		EntityPlayerMP sender = ctx.getServerHandler().player;
		MinecraftServer server = sender.server;
		if (!MCRegistryUtil.isItemRegistered(itemId)) {
			sender.sendMessage(new TextComponentTranslation("registry_message.item.error.not_found", itemId));
			return;
		}

		try {
			DynamicServerRegistryManager.unregisterItem(itemId);
		} catch (IOException e) {
			try {
				ModMain.LOGGER.error("Failed to delete an item file", e);
				ModMain.LOGGER.error("Trying again...");
				DynamicServerRegistryManager.unregisterItem(itemId);
			} catch (IOException e2) {
				ModMain.LOGGER.error("Failed to delete an item file", e2);
				TextComponentTranslation component = new TextComponentTranslation("registry_message.item.error.io.delete.item", itemId);
				MessageUtil.sendToServerAndAllPlayers(server, component);
				return;//アイテム削除失敗は中止
			}
		}
		try {
			Server.INSTANCE.removeItemModel(itemId);
		} catch (IOException e) {
			try {
				ModMain.LOGGER.error("Failed to delete an item model file", e);
				ModMain.LOGGER.error("Trying again...");
				Server.INSTANCE.removeItemModel(itemId);
			} catch (IOException e2) {
				ModMain.LOGGER.error("Failed to delete an item model file", e2);
				TextComponentTranslation component = new TextComponentTranslation("registry_message.item.error.io.delete.item_model", itemId);
				MessageUtil.sendToServerAndAllPlayers(server, component);
			}
		}
		try {
			Server.INSTANCE.removeItemNameLang("en_us", itemId);
		} catch (IOException e) {
			try {
				ModMain.LOGGER.error("Failed to delete a lang file", e);
				ModMain.LOGGER.error("Trying again...");
				Server.INSTANCE.removeItemNameLang("en_us", itemId);
			} catch (IOException e2) {
				ModMain.LOGGER.error("Failed to delete a lang", e2);
				TextComponentTranslation component = new TextComponentTranslation("registry_message.item.error.io.delete.item_lang", itemId);
				MessageUtil.sendToServerAndAllPlayers(server, component);
			}
		}
		MyPacketHandler.sendToAllPlayers(new MessageDeleteItemEntryToClient(itemId));

		TextComponentTranslation component = new TextComponentTranslation("registry_message.item.delete.success", itemId, sender.getDisplayName());
		MessageUtil.sendToServerAndAllPlayers(server, component);
	}

}
