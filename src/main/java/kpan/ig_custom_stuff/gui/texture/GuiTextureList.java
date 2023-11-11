package kpan.ig_custom_stuff.gui.texture;

import kpan.ig_custom_stuff.gui.MyGuiSlot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.function.Supplier;

@SideOnly(Side.CLIENT)
public class GuiTextureList extends MyGuiSlot {
	private final GuiScreen owner;
	private final Supplier<Collection<ResourceLocation>> entrySupplier;

	public GuiTextureList(Minecraft mc, int width, int height, int top, int bottom, GuiScreen owner, Supplier<Collection<ResourceLocation>> entrySupplier) {
		super(mc, width, height, top, bottom, 18);
		this.owner = owner;
		this.entrySupplier = entrySupplier;
		refreshList();
	}

	public void refreshList() {
		selectedIndex = -1;
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
	public ResourceLocation getSelectedTextureId() {
		return selectedIndex >= 0 ? getListEntry(selectedIndex).textureId : null;
	}

	public class Entry implements IGuiListEntry {
		public final ResourceLocation textureId;
		public boolean isVisible = true;
		public Entry(ResourceLocation textureId) {
			this.textureId = textureId;
		}

		@Override
		public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks) {
			mc.fontRenderer.drawString(textureId.toString(), x + 16 + 3, y + 3, 0xFFFFFF);
			TextureAtlasSprite sprite = mc.getTextureMapBlocks().getAtlasSprite(textureId.toString());
			GlStateManager.bindTexture(mc.getTextureMapBlocks().getGlTextureId());
			owner.drawTexturedModalRect(x, y - 1, sprite, 16, 16);
		}

		@Override
		public void updatePosition(int slotIndex, int x, int y, float partialTicks) { }

		@Override
		public boolean isVisible() {
			return isVisible;
		}
	}

	public enum EnumSelectedTextureList {
		NOT_SELECTED,
		VANILLA_BLOCK,
		VANILLA_ITEM,
		MOD_BLOCK,
		MOD_ITEM,
	}
}