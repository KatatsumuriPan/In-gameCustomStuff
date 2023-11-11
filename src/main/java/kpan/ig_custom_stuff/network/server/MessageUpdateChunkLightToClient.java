package kpan.ig_custom_stuff.network.server;

import io.netty.buffer.ByteBuf;
import kpan.ig_custom_stuff.ModMain;
import kpan.ig_custom_stuff.network.MessageBase;
import kpan.ig_custom_stuff.util.MyByteBufUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageUpdateChunkLightToClient extends MessageBase {


	//デフォルトコンストラクタは必須
	public MessageUpdateChunkLightToClient() { }

	private int dimension;
	private int chunkX;
	private int chunkZ;

	public MessageUpdateChunkLightToClient(int dimension, int chunkX, int chunkZ) {
		this.dimension = dimension;
		this.chunkX = chunkX;
		this.chunkZ = chunkZ;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		dimension = MyByteBufUtil.readVarInt(buf);
		chunkX = buf.readInt();
		chunkZ = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		MyByteBufUtil.writeVarInt(buf, dimension);
		buf.writeInt(chunkX);
		buf.writeInt(chunkZ);
	}

	@Override
	public void doAction(MessageContext ctx) {
		Client.doAction(dimension, chunkX, chunkZ);
	}

	private static class Client {
		public static void doAction(int dimension, int chunkX, int chunkZ) {
			WorldClient world = Minecraft.getMinecraft().world;
			if (world == null)
				return;
			if (Minecraft.getMinecraft().player.dimension != dimension)
				return;
			Chunk chunk = world.getChunkProvider().getLoadedChunk(chunkX, chunkZ);
			if (chunk != null) {
				ModMain.LOGGER.debug("Chunk skylight update:{},{}", chunkX, chunkZ);
				chunk.generateSkylightMap();
				chunk.checkLight();
			}
		}
	}

}
