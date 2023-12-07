package kpan.ig_custom_stuff.item;

import net.minecraft.item.Item;

import java.util.ArrayList;

public class ItemInit {

	public static final ArrayList<Item> ITEMS = new ArrayList<>();

	//	public static final ItemBase APPLE = new ItemBase("apple_test", CreativeTabs.FOOD);
	public static final ItemBase REGISTRY_BOOK = new ItemRegistryBook("registry_book");
}
