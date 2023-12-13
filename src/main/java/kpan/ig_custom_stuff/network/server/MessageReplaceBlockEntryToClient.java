package kpan.ig_custom_stuff.network.server;

import io.netty.buffer.ByteBuf;
import kpan.ig_custom_stuff.block.BlockEntry;
import kpan.ig_custom_stuff.block.BlockLangEntry;
import kpan.ig_custom_stuff.block.BlockStateEntry;
import kpan.ig_custom_stuff.gui.block.GuiBlockMenu;
import kpan.ig_custom_stuff.network.MessageBase;
import kpan.ig_custom_stuff.resource.DynamicResourceLoader;
import kpan.ig_custom_stuff.resource.DynamicResourceManager.ClientCache;
import kpan.ig_custom_stuff.resource.IdConverter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.io.IOException;

public class MessageReplaceBlockEntryToClient extends MessageBase {


	//デフォルトコンストラクタは必須
	public MessageReplaceBlockEntryToClient() { }

	private BlockEntry blockEntry;
	private BlockStateEntry blockStateEntry;
	private BlockLangEntry blockLangEntry;

	public MessageReplaceBlockEntryToClient(BlockEntry blockEntry, BlockStateEntry blockStateEntry, BlockLangEntry blockLangEntry) {
		this.blockEntry = blockEntry;
		this.blockStateEntry = blockStateEntry;
		this.blockLangEntry = blockLangEntry;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		blockEntry = BlockEntry.fromByteBuf(buf);
		blockStateEntry = BlockStateEntry.fromByteBuf(buf);
		blockLangEntry = BlockLangEntry.fromByteBuf(buf);
	}
	@Override
	public void toBytes(ByteBuf buf) {
		blockEntry.writeTo(buf);
		blockStateEntry.writeTo(buf);
		blockLangEntry.writeTo(buf);
	}
	@Override
	public void doAction(MessageContext ctx) {
		try {
			blockEntry.update(true);
			blockStateEntry.update(blockEntry.blockId, true);
			blockLangEntry.update(blockEntry.blockId, true);
			Client.doAction(blockEntry.blockId, blockStateEntry);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static class Client {
		public static void doAction(ResourceLocation blockId, BlockStateEntry blockStateEntry) throws IOException {
			ClientCache.INSTANCE.setItemBlockModel(IdConverter.blockId2ItemModelId(blockId), blockStateEntry.blockStateModelEntry.blockModelId);
			DynamicResourceLoader.loadBlockResources(blockId);
			DynamicResourceLoader.reloadAllChunks();
			GuiScreen screen = Minecraft.getMinecraft().currentScreen;
			if (screen instanceof GuiBlockMenu guiBlockMenu)
				guiBlockMenu.refreshList();
		}
	}
}
