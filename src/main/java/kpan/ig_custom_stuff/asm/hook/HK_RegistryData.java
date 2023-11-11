package kpan.ig_custom_stuff.asm.hook;

import io.netty.buffer.ByteBuf;
import kpan.ig_custom_stuff.asm.acc.ACC_RegistryData;
import kpan.ig_custom_stuff.block.BlockEntry;
import kpan.ig_custom_stuff.item.ItemEntry;
import kpan.ig_custom_stuff.util.MyByteBufUtil;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.handshake.FMLHandshakeMessage.RegistryData;

import java.util.ArrayList;
import java.util.List;

public class HK_RegistryData {

	public static void fromBytes(RegistryData self, ByteBuf buf) {
		{
			int count = MyByteBufUtil.readVarInt(buf);
			List<ItemEntry> itemEntries = new ArrayList<>(count);
			for (int i = 0; i < count; i++) {
				ItemEntry itemEntry = ItemEntry.fromByteBuf(buf);
				itemEntries.add(itemEntry);
			}
			((ACC_RegistryData) self).set_itemEntries(itemEntries);
		}
		{
			int count = MyByteBufUtil.readVarInt(buf);
			List<ResourceLocation> removedItems = new ArrayList<>(count);
			for (int i = 0; i < count; i++) {
				ResourceLocation id = new ResourceLocation(MyByteBufUtil.readString(buf));
				removedItems.add(id);
			}
			((ACC_RegistryData) self).set_removedItems(removedItems);
		}
		{
			int count = MyByteBufUtil.readVarInt(buf);
			List<BlockEntry> blockEntries = new ArrayList<>(count);
			for (int i = 0; i < count; i++) {
				BlockEntry blockEntry = BlockEntry.fromByteBuf(buf);
				blockEntries.add(blockEntry);
			}
			((ACC_RegistryData) self).set_blockEntries(blockEntries);
		}
		{
			int count = MyByteBufUtil.readVarInt(buf);
			List<ResourceLocation> removeBlocks = new ArrayList<>(count);
			for (int i = 0; i < count; i++) {
				ResourceLocation id = new ResourceLocation(MyByteBufUtil.readString(buf));
				removeBlocks.add(id);
			}
			((ACC_RegistryData) self).set_removedBlocks(removeBlocks);
		}
	}

	public static void toBytes(RegistryData self, ByteBuf buf) {
		List<ItemEntry> itemEntries = ((ACC_RegistryData) self).get_itemEntries();
		MyByteBufUtil.writeVarInt(buf, itemEntries.size());
		for (ItemEntry itemEntry : itemEntries) {
			itemEntry.writeTo(buf);
		}
		List<ResourceLocation> removedItems = ((ACC_RegistryData) self).get_removedItems();
		MyByteBufUtil.writeVarInt(buf, removedItems.size());
		for (ResourceLocation itemId : removedItems) {
			MyByteBufUtil.writeString(buf, itemId.toString());
		}
		List<BlockEntry> blockEntries = ((ACC_RegistryData) self).get_blockEntries();
		MyByteBufUtil.writeVarInt(buf, blockEntries.size());
		for (BlockEntry blockEntry : blockEntries) {
			blockEntry.writeTo(buf);
		}
		List<ResourceLocation> removedBlocks = ((ACC_RegistryData) self).get_removedBlocks();
		MyByteBufUtil.writeVarInt(buf, removedBlocks.size());
		for (ResourceLocation blockId : removedBlocks) {
			MyByteBufUtil.writeString(buf, blockId.toString());
		}
	}
}
