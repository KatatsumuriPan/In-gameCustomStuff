package kpan.ig_custom_stuff.util.handlers;

import kpan.ig_custom_stuff.ModMain;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

import java.util.List;


public class ClientEventHandler {

	public static long tick = 0;

	@SubscribeEvent
	public static void onTick(ClientTickEvent event) {
		if (event.phase == Phase.START) {
			WorldClient world = Minecraft.getMinecraft().world;
			if (world != null) {
				if (world.rand.nextInt(200) == 0) {
					List<EntityPlayer> players = world.playerEntities;
					if (!players.isEmpty()) {
						EntityPlayer player = players.get(world.rand.nextInt(players.size()));
						BlockPos pos = new BlockPos(player);
						int cx = pos.getX() >> 4;
						int cz = pos.getZ() >> 4;
						cx += world.rand.nextInt(3) - 1;
						cz += world.rand.nextInt(3) - 1;
						Chunk chunk = world.getChunk(cx, cz);
						ModMain.LOGGER.debug("Chunk skylight update:{},{}", cx, cz);
						chunk.generateSkylightMap();
						chunk.checkLight();
					}
				}
			}
		} else {
			tick++;
		}
	}
}
