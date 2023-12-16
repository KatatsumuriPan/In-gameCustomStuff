package kpan.ig_custom_stuff.network.server;

import io.netty.buffer.ByteBuf;
import kpan.ig_custom_stuff.block.model.BlockModelEntryBase;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class MessageReplaceBlockModelsToClient extends MessageBase {


	//デフォルトコンストラクタは必須
	public MessageReplaceBlockModelsToClient() { }

	private Map<BlockModelId, BlockModelEntryBase> modelId2Entry;

	public MessageReplaceBlockModelsToClient(Map<BlockModelId, BlockModelEntryBase> modelId2Entry) {
		this.modelId2Entry = modelId2Entry;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		int count = readVarInt(buf);
		modelId2Entry = new HashMap<>();
		for (int i = 0; i < count; i++) {
			BlockModelId modelId = BlockModelId.formByteBuf(buf);
			BlockModelEntryBase blockModelEntry = BlockModelEntryBase.fromByteBuf(buf);
			modelId2Entry.put(modelId, blockModelEntry);
		}
	}
	@Override
	public void toBytes(ByteBuf buf) {
		writeVarInt(buf, modelId2Entry.size());
		for (Entry<BlockModelId, BlockModelEntryBase> e : modelId2Entry.entrySet()) {
			e.getKey().writeTo(buf);
			e.getValue().writeTo(buf);
		}
	}

	@Override
	public void doAction(MessageContext ctx) {
		Client.saveAndLoad(modelId2Entry);
	}

	private static class Client {
		public static void saveAndLoad(Map<BlockModelId, BlockModelEntryBase> files) {
			for (Entry<BlockModelId, BlockModelEntryBase> entry : files.entrySet()) {
				try {
					ClientCache.INSTANCE.replaceBlockModel(entry.getKey(), entry.getValue());
					SingleBlockModelLoader.loadBlockModel(entry.getKey());
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
			for (BlockModelId modelId : files.keySet()) {
				DynamicResourceLoader.reloadBlockModelDependants(modelId);
			}
			DynamicResourceLoader.reloadAllChunks();
			GuiScreen screen = Minecraft.getMinecraft().currentScreen;
			if (screen instanceof GuiBlockModelMenu guiBlockModelMenu)
				guiBlockModelMenu.refreshList();
		}
	}
}
