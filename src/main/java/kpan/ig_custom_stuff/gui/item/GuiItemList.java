package kpan.ig_custom_stuff.gui.item;

import kpan.ig_custom_stuff.gui.MyGuiSlot;
import kpan.ig_custom_stuff.registry.MCRegistryUtil;
import kpan.ig_custom_stuff.resource.ids.ItemId;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.function.Predicate;

@SideOnly(Side.CLIENT)
public class GuiItemList extends MyGuiSlot {

	public GuiItemList(Minecraft mc, int width, int height, int top, int bottom) {
		super(mc, width, height, top, bottom, 18);
		refreshList();
	}

	public void refreshList() {
		listEntries.clear();
		for (ItemId id : MCRegistryUtil.getItemIds()) {
			listEntries.add(new Entry(id));
		}
	}

	public void applyVisiblePredicate(@Nullable Predicate<Entry> visiblePredicate) {
		if (visiblePredicate == null) {
			listEntries.forEach(e -> ((Entry) e).isVisible = true);
		} else {
			listEntries.forEach(e -> ((Entry) e).isVisible = visiblePredicate.test((Entry) e));
		}
	}

	@Override
	public Entry getListEntry(int index) {
		return (Entry) listEntries.get(index);
	}

	@Override
	public void drawScreen(int mouseXIn, int mouseYIn, float partialTicks) {
		super.drawScreen(mouseXIn, mouseYIn, partialTicks);
		if (visible) {
			if (listEntries.isEmpty())
				mc.fontRenderer.drawString(I18n.format("gui.empty..."), 20, top + 10, 0xFFA0A0A0);
		}
	}


	@Override
	protected int getScrollBarX() {
		return width - 6;
	}


	@Nullable
	public ItemId getSelectedItemId() {
		return selectedIndex >= 0 ? getListEntry(selectedIndex).itemId : null;
	}

	public class Entry implements IGuiListEntry {
		public final ItemId itemId;
		public boolean isVisible = true;
		public Entry(ItemId itemId) {
			this.itemId = itemId;
		}

		@Override
		public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks) {
			mc.fontRenderer.drawString(itemId.toString(), x + 16 + 3, y + 3, 0xFFFFFF);
			Item item = Item.REGISTRY.getObject(itemId.toResourceLocation());
			if (item != null)
				Minecraft.getMinecraft().getRenderItem().renderItemIntoGUI(new ItemStack(item, 1), x, y - 1);
		}

		@Override
		public void updatePosition(int slotIndex, int x, int y, float partialTicks) { }

		@Override
		public boolean isVisible() {
			return isVisible;
		}
	}
}