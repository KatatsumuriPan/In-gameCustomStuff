package kpan.ig_custom_stuff.gui.block;

import kpan.ig_custom_stuff.gui.MyGuiSlot;
import kpan.ig_custom_stuff.registry.MCRegistryUtil;
import kpan.ig_custom_stuff.resource.ids.BlockId;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.function.Predicate;

@SideOnly(Side.CLIENT)
public class GuiBlockList extends MyGuiSlot {
	private final GuiScreen owner;

	public GuiBlockList(Minecraft mc, int width, int height, int top, int bottom, GuiScreen owner) {
		super(mc, width, height, top, bottom, 18);
		this.owner = owner;
		refreshList();
	}

	public void refreshList() {
		listEntries.clear();
		for (BlockId id : MCRegistryUtil.getBlockIds()) {
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
	protected int getScrollBarX() {
		return width - 6;
	}

	@Nullable
	public BlockId getSelectedBlockId() {
		return selectedIndex >= 0 ? getListEntry(selectedIndex).blockId : null;
	}

	public class Entry implements IGuiListEntry {
		public final BlockId blockId;
		public boolean isVisible = true;
		public Entry(BlockId blockId) {
			this.blockId = blockId;
		}

		@Override
		public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks) {
			mc.fontRenderer.drawString(blockId.toString(), x + 16 + 3, y + 3, 0xFFFFFF);
			Block block = Block.REGISTRY.getObject(blockId.toResourceLocation());
			if (block != Blocks.AIR) {
				RenderHelper.enableGUIStandardItemLighting();
				Minecraft.getMinecraft().getRenderItem().renderItemIntoGUI(new ItemStack(block, 1), x, y - 1);
			}
		}

		@Override
		public void updatePosition(int slotIndex, int x, int y, float partialTicks) { }

		@Override
		public boolean isVisible() {
			return isVisible;
		}
	}
}