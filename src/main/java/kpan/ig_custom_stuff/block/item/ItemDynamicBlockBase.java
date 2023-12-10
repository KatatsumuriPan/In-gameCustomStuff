package kpan.ig_custom_stuff.block.item;

import kpan.ig_custom_stuff.block.DynamicBlockBase;
import kpan.ig_custom_stuff.util.MyReflectionHelper;
import net.minecraft.item.ItemBlock;

public class ItemDynamicBlockBase extends ItemBlock {
	public ItemDynamicBlockBase(DynamicBlockBase block) {
		super(block);
		MyReflectionHelper.setPrivateField(Impl.class, this, "registryName", block.getRegistryName());
	}


}
