package kpan.ig_custom_stuff.proxy;

import net.minecraft.item.Item;

@SuppressWarnings("unused")
public class CommonProxy {
	public void registerOnlyClient() { }

	public boolean hasClientSide() { return false; }

	public void registerSingleModel(Item item, int meta, String id) { }

	public void registerMultiItemModel(Item item, int meta, String filename, String id) { }

	public void postRegisterOnlyClient() { }
}
