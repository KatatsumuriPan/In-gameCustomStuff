package kpan.ig_custom_stuff.gui.blockmodel;

import kpan.ig_custom_stuff.gui.MyGuiSlot;
import kpan.ig_custom_stuff.resource.DynamicResourceLoader.SingleBlockModelLoader;
import kpan.ig_custom_stuff.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.function.Supplier;

@SideOnly(Side.CLIENT)
public class GuiBlockModelList extends MyGuiSlot {
	private final Supplier<Collection<ResourceLocation>> entrySupplier;

	public GuiBlockModelList(Minecraft mc, int width, int height, int top, int bottom, Supplier<Collection<ResourceLocation>> entrySupplier) {
		super(mc, width, height, top, bottom, 18);
		this.entrySupplier = entrySupplier;
		refreshList();
	}

	public void refreshList() {
		listEntries.clear();
		for (ResourceLocation id : entrySupplier.get()) {
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
	public void drawScreen(int mouseXIn, int mouseYIn, float partialTicks) {
		super.drawScreen(mouseXIn, mouseYIn, partialTicks);
		if (visible) {
			if (listEntries.isEmpty())
				mc.fontRenderer.drawString(I18n.format("gui.empty..."), 20, top + 10, 0xFFA0A0A0);
		}
	}

	@Override
	public Entry getListEntry(int index) {
		return (Entry) listEntries.get(index);
	}

	@Override
	protected int getScrollBarX() {
		return width - 6;
	}

	@Nullable
	public ResourceLocation getSelectedModelId() {
		return selectedIndex >= 0 ? getListEntry(selectedIndex).modelId : null;
	}

	public class Entry implements IGuiListEntry {
		public final ResourceLocation modelId;
		public boolean isVisible = true;
		public Entry(ResourceLocation modelId) {
			this.modelId = modelId;
		}

		@Override
		public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks) {
			mc.fontRenderer.drawString(modelId.toString(), x + 16 + 3, y + 3, 0xFFFFFF);
			RenderUtil.renderModel(x, y - 1, 1, SingleBlockModelLoader.getModel(modelId));
		}

		@Override
		public void updatePosition(int slotIndex, int x, int y, float partialTicks) { }

		@Override
		public boolean isVisible() {
			return isVisible;
		}
	}

}