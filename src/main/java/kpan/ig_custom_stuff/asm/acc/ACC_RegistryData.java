package kpan.ig_custom_stuff.asm.acc;

import kpan.ig_custom_stuff.asm.core.adapters.MixinAccessorAdapter.NewField;
import kpan.ig_custom_stuff.block.BlockEntry;
import kpan.ig_custom_stuff.item.ItemEntry;
import net.minecraft.util.ResourceLocation;

import java.util.List;

public interface ACC_RegistryData {

	@NewField
	List<ItemEntry> get_itemEntries();
	@NewField
	void set_itemEntries(List<ItemEntry> value);

	@NewField
	List<ResourceLocation> get_removedItems();
	@NewField
	void set_removedItems(List<ResourceLocation> value);

	@NewField
	List<BlockEntry> get_blockEntries();
	@NewField
	void set_blockEntries(List<BlockEntry> value);

	@NewField
	List<ResourceLocation> get_removedBlocks();
	@NewField
	void set_removedBlocks(List<ResourceLocation> value);
}
