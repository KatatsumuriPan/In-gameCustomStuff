package kpan.ig_custom_stuff.util.handlers;

import kpan.ig_custom_stuff.network.MyPacketHandler;
import kpan.ig_custom_stuff.network.server.MessageSyncResourcePack;
import kpan.ig_custom_stuff.resource.DynamicResourceManager.Server;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ServerConnectionFromClientEvent;

@EventBusSubscriber
public class ServerEventHandler {

	@SubscribeEvent
	public static void onConnect(ServerConnectionFromClientEvent event) {
		EntityPlayerMP player = ((NetHandlerPlayServer) event.getHandler()).player;
		MyPacketHandler.sendToPlayer(new MessageSyncResourcePack(Server.resourcePackDir), player);
	}
}
