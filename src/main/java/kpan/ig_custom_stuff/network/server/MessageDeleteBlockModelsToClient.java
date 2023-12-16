package kpan.ig_custom_stuff.network.server;

import io.netty.buffer.ByteBuf;
import kpan.ig_custom_stuff.gui.blockmodel.GuiBlockModelMenu;
import kpan.ig_custom_stuff.network.MessageBase;
import kpan.ig_custom_stuff.resource.DynamicResourceLoader;
import kpan.ig_custom_stuff.resource.DynamicResourceLoader.SingleBlockModelLoader;
import kpan.ig_custom_stuff.resource.DynamicResourceManager.ClientCache;
import kpan.ig_custom_stuff.resource.ids.BlockModelId;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MessageDeleteBlockModelsToClient extends MessageBase {


	//デフォルトコンストラクタは必須
	public MessageDeleteBlockModelsToClient() { }

	private List<BlockModelId> modelIds;

	public MessageDeleteBlockModelsToClient(List<BlockModelId> modelIds) {
		this.modelIds = modelIds;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		int count = readVarInt(buf);
		modelIds = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			BlockModelId id = BlockModelId.formByteBuf(buf);
			modelIds.add(id);
		}
	}
	@Override
	public void toBytes(ByteBuf buf) {
		writeVarInt(buf, modelIds.size());
		for (BlockModelId modelId : modelIds) {
			modelId.writeTo(buf);
		}
	}

	@Override
	public void doAction(MessageContext ctx) {
		Client.saveAndLoad(modelIds);
	}

	private static class Client {
		public static void saveAndLoad(List<BlockModelId> modelIds) {
			for (BlockModelId modelId : modelIds) {
				try {
					ClientCache.INSTANCE.removeBlockModel(modelId);
					SingleBlockModelLoader.remove(modelId);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
			DynamicResourceLoader.reloadAllChunks();
			GuiScreen screen = Minecraft.getMinecraft().currentScreen;
			if (screen instanceof GuiBlockModelMenu guiBlockModelMenu)
				guiBlockModelMenu.refreshList();
		}
	}
}
