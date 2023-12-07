package kpan.ig_custom_stuff.network.client;

import io.netty.buffer.ByteBuf;
import kpan.ig_custom_stuff.network.MessageBase;
import kpan.ig_custom_stuff.network.MessageUtil;
import kpan.ig_custom_stuff.network.MyPacketHandler;
import kpan.ig_custom_stuff.network.server.MessageUpdateChunkLightToClient;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageUpdateChunkLightToServer extends MessageBase {
	//デフォルトコンストラクタは必須
	public MessageUpdateChunkLightToServer() { }

	private int chunkX;
	private int chunkZ;

	public MessageUpdateChunkLightToServer(int chunkX, int chunkZ) {
		this.chunkX = chunkX;
		this.chunkZ = chunkZ;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		chunkX = buf.readInt();
		chunkZ = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(chunkX);
		buf.writeInt(chunkZ);
	}

	@Override
	public void doAction(MessageContext ctx) {
		EntityPlayerMP sender = ctx.getServerHandler().player;
		MinecraftServer server = sender.server;

		Chunk chunk = sender.world.getChunk(chunkX, chunkZ);
		chunk.generateSkylightMap();
		chunk.checkLight();
		MyPacketHandler.sendToAllPlayers(new MessageUpdateChunkLightToClient(sender.dimension, chunkX, chunkZ));
		ITextComponent component = new TextComponentTranslation("ingame_custom_stuff.update_chunk_light", chunkX, chunkZ);
		MessageUtil.sendToServerAndAllPlayers(server, component);
	}

}
