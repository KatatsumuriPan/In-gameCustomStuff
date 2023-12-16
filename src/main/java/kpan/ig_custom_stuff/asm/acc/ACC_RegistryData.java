package kpan.ig_custom_stuff.asm.acc;

import kpan.ig_custom_stuff.asm.core.adapters.MixinAccessorAdapter.NewField;
import kpan.ig_custom_stuff.block.BlockEntry;
import kpan.ig_custom_stuff.item.ItemEntry;
import kpan.ig_custom_stuff.resource.ids.BlockId;
import kpan.ig_custom_stuff.resource.ids.ItemId;

import java.util.List;

public interface ACC_RegistryData {

	@NewField
	List<ItemEntry> get_itemEntries();
	@NewField
	void set_itemEntries(List<ItemEntry> value);

	@NewField
	List<ItemId> get_removedItems();
	@NewField
	void set_removedItems(List<ItemId> value);

	@NewField
	List<BlockEntry> get_blockEntries();
	@NewField
	void set_blockEntries(List<BlockEntry> value);

	@NewField
	List<BlockId> get_removedBlocks();
	@NewField
	void set_removedBlocks(List<BlockId> value);
}
