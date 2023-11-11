package kpan.ig_custom_stuff.network;

import kpan.ig_custom_stuff.ModTagsGenerated;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

public class MyPacketHandler {
	private static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(ModTagsGenerated.MODID);
	private static int messageId = 0;

	public static void registerMessages() {
		messageId = new MyMessageHander().register(INSTANCE, messageId);
	}

	public static void sendToServer(IMessage message) {
		INSTANCE.sendToServer(message);
	}

	public static void sendToPlayer(IMessage message, EntityPlayerMP player) {
		INSTANCE.sendTo(message, player);
	}
	public static void sendToAllPlayers(IMessage message) {
		INSTANCE.sendToAll(message);
	}

}
