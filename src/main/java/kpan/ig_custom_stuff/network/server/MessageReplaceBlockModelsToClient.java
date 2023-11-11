package kpan.ig_custom_stuff.network.server;

import io.netty.buffer.ByteBuf;
import kpan.ig_custom_stuff.block.BlockModelEntry;
import kpan.ig_custom_stuff.gui.blockmodel.GuiBlockModelMenu;
import kpan.ig_custom_stuff.network.MessageBase;
import kpan.ig_custom_stuff.resource.DynamicResourceLoader;
import kpan.ig_custom_stuff.resource.DynamicResourceLoader.SingleBlockModelLoader;
import kpan.ig_custom_stuff.resource.DynamicResourceManager.ClientCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class MessageReplaceBlockModelsToClient extends MessageBase {


	//デフォルトコンストラクタは必須
	public MessageReplaceBlockModelsToClient() { }

	private Map<ResourceLocation, BlockModelEntry> modelId2Entry;

	public MessageReplaceBlockModelsToClient(Map<ResourceLocation, BlockModelEntry> modelId2Entry) {
		this.modelId2Entry = modelId2Entry;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		int count = readVarInt(buf);
		modelId2Entry = new HashMap<>();
		for (int i = 0; i < count; i++) {
			ResourceLocation modelId = new ResourceLocation(readString(buf));
			BlockModelEntry blockModelEntry = BlockModelEntry.fromByteBuf(buf);
			modelId2Entry.put(modelId, blockModelEntry);
		}
	}
	@Override
	public void toBytes(ByteBuf buf) {
		writeVarInt(buf, modelId2Entry.size());
		for (Entry<ResourceLocation, BlockModelEntry> e : modelId2Entry.entrySet()) {
			writeString(buf, e.getKey().toString());
			e.getValue().writeTo(buf);
		}
	}

	@Override
	public void doAction(MessageContext ctx) {
		Client.saveAndLoad(modelId2Entry);
	}

	private static class Client {
		public static void saveAndLoad(Map<ResourceLocation, BlockModelEntry> files) {
			for (Entry<ResourceLocation, BlockModelEntry> entry : files.entrySet()) {
				try {
					ClientCache.INSTANCE.replaceBlockModel(entry.getKey(), entry.getValue());
					SingleBlockModelLoader.loadBlockModel(entry.getKey());
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
			for (ResourceLocation modelId : files.keySet()) {
				DynamicResourceLoader.reloadBlockModelDependants(modelId);
			}
			DynamicResourceLoader.reloadAllChunks();
			GuiScreen screen = Minecraft.getMinecraft().currentScreen;
			if (screen instanceof GuiBlockModelMenu guiBlockModelMenu)
				guiBlockModelMenu.refreshList();
		}
	}
}
