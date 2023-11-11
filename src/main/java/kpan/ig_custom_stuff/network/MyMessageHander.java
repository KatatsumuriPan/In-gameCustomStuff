package kpan.ig_custom_stuff.network;

import kpan.ig_custom_stuff.network.client.MessageDeleteBlockEntryToServer;
import kpan.ig_custom_stuff.network.client.MessageDeleteBlockModelsToServer;
import kpan.ig_custom_stuff.network.client.MessageDeleteItemEntryToServer;
import kpan.ig_custom_stuff.network.client.MessageDeleteTexturesToServer;
import kpan.ig_custom_stuff.network.client.MessageRegisterBlockEntryToServer;
import kpan.ig_custom_stuff.network.client.MessageRegisterBlockModelsToServer;
import kpan.ig_custom_stuff.network.client.MessageRegisterItemEntryToServer;
import kpan.ig_custom_stuff.network.client.MessageRegisterTexturesToServer;
import kpan.ig_custom_stuff.network.client.MessageReplaceBlockEntryToServer;
import kpan.ig_custom_stuff.network.client.MessageReplaceBlockModelsToServer;
import kpan.ig_custom_stuff.network.client.MessageReplaceItemEntryToServer;
import kpan.ig_custom_stuff.network.client.MessageReplaceTexturesToServer;
import kpan.ig_custom_stuff.network.client.MessageUpdateChunkLightToServer;
import kpan.ig_custom_stuff.network.server.MessageDeleteBlockEntryToClient;
import kpan.ig_custom_stuff.network.server.MessageDeleteBlockModelsToClient;
import kpan.ig_custom_stuff.network.server.MessageDeleteItemEntryToClient;
import kpan.ig_custom_stuff.network.server.MessageDeleteTexturesToClient;
import kpan.ig_custom_stuff.network.server.MessageEditItemEntryToClient;
import kpan.ig_custom_stuff.network.server.MessageRegisterBlockEntryToClient;
import kpan.ig_custom_stuff.network.server.MessageRegisterBlockModelsToClient;
import kpan.ig_custom_stuff.network.server.MessageRegisterItemEntryToClient;
import kpan.ig_custom_stuff.network.server.MessageRegisterTexturesToClient;
import kpan.ig_custom_stuff.network.server.MessageReplaceBlockEntryToClient;
import kpan.ig_custom_stuff.network.server.MessageReplaceBlockModelsToClient;
import kpan.ig_custom_stuff.network.server.MessageReplaceTexturesToClient;
import kpan.ig_custom_stuff.network.server.MessageSyncResourcePack;
import kpan.ig_custom_stuff.network.server.MessageUpdateChunkLightToClient;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;

public class MyMessageHander implements IMessageHandler<MessageBase, MessageBase> {
	private final ArrayList<Pair<Class<? extends MessageBase>, Side>> messages = new ArrayList<>();

	public MyMessageHander() {
		sendToServer(MessageReplaceItemEntryToServer.class);
		sendToServer(MessageRegisterBlockEntryToServer.class);
		sendToServer(MessageRegisterBlockModelsToServer.class);
		sendToServer(MessageRegisterItemEntryToServer.class);
		sendToServer(MessageRegisterTexturesToServer.class);
		sendToServer(MessageReplaceBlockEntryToServer.class);
		sendToServer(MessageReplaceBlockModelsToServer.class);
		sendToServer(MessageReplaceTexturesToServer.class);
		sendToServer(MessageDeleteBlockEntryToServer.class);
		sendToServer(MessageDeleteBlockModelsToServer.class);
		sendToServer(MessageDeleteItemEntryToServer.class);
		sendToServer(MessageDeleteTexturesToServer.class);
		sendToServer(MessageUpdateChunkLightToServer.class);

		sendToClient(MessageEditItemEntryToClient.class);
		sendToClient(MessageRegisterBlockEntryToClient.class);
		sendToClient(MessageRegisterBlockModelsToClient.class);
		sendToClient(MessageRegisterItemEntryToClient.class);
		sendToClient(MessageRegisterTexturesToClient.class);
		sendToClient(MessageReplaceBlockEntryToClient.class);
		sendToClient(MessageReplaceBlockModelsToClient.class);
		sendToClient(MessageReplaceTexturesToClient.class);
		sendToClient(MessageDeleteBlockEntryToClient.class);
		sendToClient(MessageDeleteBlockModelsToClient.class);
		sendToClient(MessageDeleteItemEntryToClient.class);
		sendToClient(MessageDeleteTexturesToClient.class);
		sendToClient(MessageSyncResourcePack.class);
		sendToClient(MessageUpdateChunkLightToClient.class);
	}

	public int register(SimpleNetworkWrapper wrapper, int messageid) {
		for (Pair<Class<? extends MessageBase>, Side> pair : messages) {
			wrapper.registerMessage(this, pair.getLeft(), messageid++, pair.getRight());
		}
		return messageid;
	}

	private void addToList(Class<? extends MessageBase> message, Side send_to) {
		messages.add(Pair.of(message, send_to));
	}
	private void sendToServer(Class<? extends MessageBase> message) {
		addToList(message, Side.SERVER);
	}
	private void sendToClient(Class<? extends MessageBase> message) {
		addToList(message, Side.CLIENT);
	}
	@SuppressWarnings("unused")
	private void sendBoth(Class<? extends MessageBase> message) {
		sendToServer(message);
		sendToClient(message);
	}

	@Override
	public MessageBase onMessage(MessageBase message, MessageContext ctx) {
		//メインスレッドで実行してもらう
		MessageUtil.addScheduledTask(message, ctx);
		return null;
	}

}
