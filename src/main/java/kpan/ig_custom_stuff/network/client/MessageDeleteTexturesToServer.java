package kpan.ig_custom_stuff.network.client;

import io.netty.buffer.ByteBuf;
import kpan.ig_custom_stuff.ModMain;
import kpan.ig_custom_stuff.network.MessageBase;
import kpan.ig_custom_stuff.network.MessageUtil;
import kpan.ig_custom_stuff.network.MyPacketHandler;
import kpan.ig_custom_stuff.network.server.MessageDeleteTexturesToClient;
import kpan.ig_custom_stuff.resource.DynamicResourceManager.Server;
import kpan.ig_custom_stuff.resource.ids.BlockTextureId;
import kpan.ig_custom_stuff.resource.ids.ItemTextureId;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MessageDeleteTexturesToServer extends MessageBase {
	//デフォルトコンストラクタは必須
	public MessageDeleteTexturesToServer() { }

	private List<ItemTextureId> itemTextureIds;
	private List<BlockTextureId> blockTextureIds;

	public MessageDeleteTexturesToServer(List<ItemTextureId> itemTextureIds, List<BlockTextureId> blockTextureIds) {
		this.itemTextureIds = itemTextureIds;
		this.blockTextureIds = blockTextureIds;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		int count = readVarInt(buf);
		itemTextureIds = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			ItemTextureId id = ItemTextureId.formByteBuf(buf);
			itemTextureIds.add(id);
		}
		int count2 = readVarInt(buf);
		blockTextureIds = new ArrayList<>();
		for (int i = 0; i < count2; i++) {
			BlockTextureId id = BlockTextureId.formByteBuf(buf);
			blockTextureIds.add(id);
		}
	}
	@Override
	public void toBytes(ByteBuf buf) {
		writeVarInt(buf, itemTextureIds.size());
		for (ItemTextureId textureId : itemTextureIds) {
			textureId.writeTo(buf);
		}
		writeVarInt(buf, blockTextureIds.size());
		for (BlockTextureId textureId : blockTextureIds) {
			textureId.writeTo(buf);
		}
	}

	@Override
	public void doAction(MessageContext ctx) {
		EntityPlayerMP sender = ctx.getServerHandler().player;
		MinecraftServer server = sender.server;

		if (itemTextureIds.isEmpty() && blockTextureIds.isEmpty()) {
			sender.server.sendMessage(new TextComponentString("INVALID PACKET:no textures be removing"));
			return;
		}

		List<ItemTextureId> succeededItems = new ArrayList<>();
		List<BlockTextureId> succeededBlocks = new ArrayList<>();
		for (ItemTextureId textureId : itemTextureIds) {
			try {
				if (Server.INSTANCE.removeTexture(textureId))
					succeededItems.add(textureId);
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
		for (BlockTextureId textureId : blockTextureIds) {
			try {
				if (Server.INSTANCE.removeTexture(textureId))
					succeededBlocks.add(textureId);
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

		if (!succeededItems.isEmpty() || !succeededBlocks.isEmpty()) {
			MyPacketHandler.sendToAllPlayers(new MessageDeleteTexturesToClient(succeededItems, succeededBlocks));
			TextComponentTranslation component;
			if (succeededItems.size() + succeededBlocks.size() == 1) {
				if (succeededItems.size() == 1)
					component = new TextComponentTranslation("registry_message.texture.delete.success", succeededItems.get(0), sender.getDisplayName());
				else
					component = new TextComponentTranslation("registry_message.texture.delete.success", succeededBlocks.get(0), sender.getDisplayName());
			} else {
				component = new TextComponentTranslation("registry_message.texture.delete.success.multiple", succeededItems.size() + succeededBlocks.size(), sender.getDisplayName());
			}
			MessageUtil.sendToServerAndAllPlayers(server, component);
		}

	}

}
