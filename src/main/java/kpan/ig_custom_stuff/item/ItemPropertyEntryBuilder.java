package kpan.ig_custom_stuff.item;

import net.minecraft.creativetab.CreativeTabs;

public class ItemPropertyEntryBuilder {

	public CreativeTabs creativeTab;


	public ItemPropertyEntryBuilder(ItemPropertyEntry propertyEntry) {
		creativeTab = propertyEntry.creativeTab;
	}
	public ItemPropertyEntry build() {
		return new ItemPropertyEntry(creativeTab);
	}

	public CreativeTabs getCreativeTab() {
		return creativeTab;
	}
	public void setCreativeTab(CreativeTabs creativeTab) {
		this.creativeTab = creativeTab;
	}
}
