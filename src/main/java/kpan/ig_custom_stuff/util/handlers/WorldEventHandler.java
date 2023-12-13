package kpan.ig_custom_stuff.util.handlers;

import kpan.ig_custom_stuff.ModMain;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber
public class WorldEventHandler {

	@SubscribeEvent
	public static void onTick(WorldTickEvent event) {
		//server限定
		if (event.phase == Phase.START) {
			if (event.world.rand.nextInt(200) == 0) {
				List<EntityPlayer> players = event.world.playerEntities;
				if (!players.isEmpty()) {
					EntityPlayer player = players.get(event.world.rand.nextInt(players.size()));
					BlockPos pos = new BlockPos(player);
					int cx = pos.getX() >> 4;
					int cz = pos.getZ() >> 4;
					cx += event.world.rand.nextInt(3) - 1;
					cz += event.world.rand.nextInt(3) - 1;
					Chunk chunk = event.world.getChunk(cx, cz);
					updateChunkLight(chunk);
				}
			}
			if (event.world.rand.nextInt(200) == 0) {
				ArrayList<Chunk> chunks = new ArrayList<>(((ChunkProviderServer) event.world.getChunkProvider()).loadedChunks.values());
				int size = chunks.size();
				if (size > 0)
					updateChunkLight(chunks.get(event.world.rand.nextInt(size)));
			}
		}
	}
	private static void updateChunkLight(Chunk chunk) {
		ModMain.LOGGER.debug("Chunk skylight update:{},{}", chunk.x, chunk.z);
		chunk.generateSkylightMap();
		chunk.checkLight();
	}
}
