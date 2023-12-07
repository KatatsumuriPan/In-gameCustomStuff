package kpan.ig_custom_stuff.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;

import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
public abstract class MyGuiSlot {
	protected final Minecraft mc;
	public int width;
	public int height;
	/**
	 * The top of the slot container. Affects the overlays and scrolling.
	 */
	public int top;
	/**
	 * The bottom of the slot container. Affects the overlays and scrolling.
	 */
	public int bottom;
	public int right;
	public int left;
	/**
	 * The height of a slot.
	 */
	public final int slotHeight;
	/**
	 * The buttonID of the button used to scroll up
	 */
	private int scrollUpButtonID;
	/**
	 * The buttonID of the button used to scroll down
	 */
	private int scrollDownButtonID;
	protected int mouseX;
	protected int mouseY;
	protected boolean centerListVertically = true;
	/**
	 * Where the mouse was in the window when you first clicked to scroll
	 */
	protected int initialClickY = -2;
	/**
	 * What to multiply the amount you moved your mouse by (used for slowing down scrolling when over the items and not
	 * on the scroll bar)
	 */
	protected float scrollMultiplier;
	/**
	 * How far down this slot has been scrolled
	 */
	protected float amountScrolled;
	/**
	 * The element in the list that was selected
	 */
	protected int selectedIndex = -1;
	/**
	 * The time when this button was last clicked.
	 */
	protected long lastClicked;
	public boolean visible = true;
	/**
	 * Set to true if a selected element in this gui will show an outline box
	 */
	protected boolean showSelectionBox = true;
	protected boolean hasListHeader;
	public int headerPadding;
	protected boolean enabled = true;
	public final List<IGuiListEntry> listEntries;
	protected boolean isOverlaying = false;

	public MyGuiSlot(Minecraft mcIn, int width, int height, int topIn, int bottomIn, int slotHeightIn) {
		mc = mcIn;
		this.width = width;
		this.height = height;
		top = topIn;
		bottom = bottomIn;
		slotHeight = slotHeightIn;
		left = 0;
		right = width;
		listEntries = new ArrayList<>();
	}

	//設定

	public void setDimensions(int widthIn, int heightIn, int topIn, int bottomIn) {
		width = widthIn;
		height = heightIn;
		top = topIn;
		bottom = bottomIn;
		left = 0;
		right = widthIn;
	}

	public void setShowSelectionBox(boolean showSelectionBoxIn) {
		showSelectionBox = showSelectionBoxIn;
	}

	/**
	 * Sets hasListHeader and headerHeight. Params: hasListHeader, headerHeight. If hasListHeader is false headerHeight
	 * is set to 0.
	 */
	public void setHasListHeader(boolean hasListHeaderIn, int headerPaddingIn) {
		hasListHeader = hasListHeaderIn;
		headerPadding = headerPaddingIn;

		if (!hasListHeaderIn) {
			headerPadding = 0;
		}
	}

	/**
	 * Registers the IDs that can be used for the scrollbar's up/down buttons.
	 */
	public void registerScrollButtons(int scrollUpButtonIDIn, int scrollDownButtonIDIn) {
		scrollUpButtonID = scrollUpButtonIDIn;
		scrollDownButtonID = scrollDownButtonIDIn;
	}

	/**
	 * Scrolls the slot by the given amount. A positive value scrolls down, and a negative value scrolls up.
	 */
	public void scrollBy(int amount) {
		amountScrolled += (float) amount;
		bindAmountScrolled();
		initialClickY = -2;
	}

	public void setEnabled(boolean enabledIn) {
		enabled = enabledIn;
	}

	/**
	 * Sets the left and right bounds of the slot. Param is the left bound, right is calculated as left + width.
	 */
	public void setSlotXBoundsFromLeft(int leftIn) {
		left = leftIn;
		right = leftIn + width;
	}

	public void setIsOverlaying(boolean overlaying) {
		isOverlaying = overlaying;
	}

	//取得

	public int getEntryNum() {
		return listEntries.size();
	}
	public int getVisibleEntryNum() {
		return getEntryNum();
	}

	public int getVisibleSlotIndexFromScreenCoords(int posX, int posY) {
		int i = left + width / 2 - getListWidth() / 2;
		int j = left + width / 2 + getListWidth() / 2;
		int k = posY - top - headerPadding + (int) amountScrolled - 4;
		int l = k / slotHeight;
		return posX < getScrollBarX() && posX >= i && posX <= j && l >= 0 && k >= 0 && l < getVisibleEntryNum() ? l : -1;
	}

	public int getMaxScroll() {
		return Math.max(0, getVisibleContentHeight() - (bottom - top - 4));
	}

	public int getAmountScrolled() {
		return (int) amountScrolled;
	}

	public boolean isMouseYWithinSlotBounds(int p_148141_1_) {
		return p_148141_1_ >= top && p_148141_1_ <= bottom && mouseX >= left && mouseX <= right;
	}

	public boolean getEnabled() {
		return enabled;
	}

	/**
	 * Gets the width of the list
	 */
	public int getListWidth() {
		return width;
	}

	public int getSlotHeight() {
		return slotHeight;
	}

	public IGuiListEntry getListEntry(int index) {
		return listEntries.get(index);
	}

	public boolean isOverlaying() {
		return isOverlaying;
	}

	protected abstract int getScrollBarX();


	//呼び出し必須

	public void actionPerformed(GuiButton button) {
		if (!visible)
			return;
		if (button.enabled) {
			if (button.id == scrollUpButtonID) {
				amountScrolled -= (float) (slotHeight * 2 / 3);
				initialClickY = -2;
				bindAmountScrolled();
			} else if (button.id == scrollDownButtonID) {
				amountScrolled += (float) (slotHeight * 2 / 3);
				initialClickY = -2;
				bindAmountScrolled();
			}
		}
	}

	public void drawScreen(int mouseXIn, int mouseYIn, float partialTicks) {
		if (!visible)
			return;
		mouseX = mouseXIn;
		mouseY = mouseYIn;
		drawBackground();
		int scrollBarLeft = getScrollBarX();
		int scrollBarRight = scrollBarLeft + 6;
		bindAmountScrolled();
		GlStateManager.disableLighting();
		GlStateManager.disableFog();
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		// Forge: background rendering moved into separate method.
		drawContainerBackground(tessellator);
		int l = left + width / 2 - getListWidth() / 2 + 2;
		int firstScrolledY = top + 4 - (int) amountScrolled;

		if (hasListHeader) {
			drawListHeader(l, firstScrolledY, tessellator);
		}

		drawSelectionBox(l, firstScrolledY, mouseXIn, mouseYIn, partialTicks);
		GlStateManager.disableDepth();
		overlayBackground(0, top, 255, 255);
		overlayBackground(bottom, height, 255, 255);
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
		GlStateManager.disableAlpha();
		GlStateManager.shadeModel(7425);
		GlStateManager.disableTexture2D();
		bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
		bufferbuilder.pos(left, top + 4, 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 0).endVertex();
		bufferbuilder.pos(right, top + 4, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 0).endVertex();
		bufferbuilder.pos(right, top, 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 255).endVertex();
		bufferbuilder.pos(left, top, 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 255).endVertex();
		tessellator.draw();
		bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
		bufferbuilder.pos(left, bottom, 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 255).endVertex();
		bufferbuilder.pos(right, bottom, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
		bufferbuilder.pos(right, bottom - 4, 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 0).endVertex();
		bufferbuilder.pos(left, bottom - 4, 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 0).endVertex();
		tessellator.draw();
		int maxScroll = getMaxScroll();

		if (maxScroll > 0) {
			int k1 = (bottom - top) * (bottom - top) / getVisibleContentHeight();
			k1 = MathHelper.clamp(k1, 32, bottom - top - 8);
			int l1 = (int) amountScrolled * (bottom - top - k1) / maxScroll + top;

			if (l1 < top) {
				l1 = top;
			}

			bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
			bufferbuilder.pos(scrollBarLeft, bottom, 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 255).endVertex();
			bufferbuilder.pos(scrollBarRight, bottom, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
			bufferbuilder.pos(scrollBarRight, top, 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 255).endVertex();
			bufferbuilder.pos(scrollBarLeft, top, 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 255).endVertex();
			tessellator.draw();
			bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
			bufferbuilder.pos(scrollBarLeft, l1 + k1, 0.0D).tex(0.0D, 1.0D).color(128, 128, 128, 255).endVertex();
			bufferbuilder.pos(scrollBarRight, l1 + k1, 0.0D).tex(1.0D, 1.0D).color(128, 128, 128, 255).endVertex();
			bufferbuilder.pos(scrollBarRight, l1, 0.0D).tex(1.0D, 0.0D).color(128, 128, 128, 255).endVertex();
			bufferbuilder.pos(scrollBarLeft, l1, 0.0D).tex(0.0D, 0.0D).color(128, 128, 128, 255).endVertex();
			tessellator.draw();
			bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
			bufferbuilder.pos(scrollBarLeft, l1 + k1 - 1, 0.0D).tex(0.0D, 1.0D).color(192, 192, 192, 255).endVertex();
			bufferbuilder.pos(scrollBarRight - 1, l1 + k1 - 1, 0.0D).tex(1.0D, 1.0D).color(192, 192, 192, 255).endVertex();
			bufferbuilder.pos(scrollBarRight - 1, l1, 0.0D).tex(1.0D, 0.0D).color(192, 192, 192, 255).endVertex();
			bufferbuilder.pos(scrollBarLeft, l1, 0.0D).tex(0.0D, 0.0D).color(192, 192, 192, 255).endVertex();
			tessellator.draw();
		}

		renderDecorations(mouseXIn, mouseYIn);
		GlStateManager.enableTexture2D();
		GlStateManager.shadeModel(7424);
		GlStateManager.enableAlpha();
		GlStateManager.disableBlend();
	}

	public void handleMouseInput() {
		if (!visible)
			return;
		if (isMouseYWithinSlotBounds(mouseY)) {
			if (Mouse.getEventButton() == 0 && Mouse.getEventButtonState() && mouseY >= top && mouseY <= bottom) {
				int l = 0;
				int r = getScrollBarX();
				int y = mouseY - top - headerPadding + (int) amountScrolled - 4;
				int visibleIndex = y / slotHeight;

				if (visibleIndex < getVisibleEntryNum() && mouseX >= l && mouseX <= r && visibleIndex >= 0 && y >= 0) {
					visibleElementClicked(visibleIndex, false, mouseX, mouseY);
					selectedIndex = toEntryIndex(visibleIndex);
				} else if (mouseX >= l && mouseX <= r && y < 0) {
					clickedHeader(mouseX - l, mouseY - top + (int) amountScrolled - 4);
				}
			}
		}

		if (Mouse.isButtonDown(0) && getEnabled()) {
			if (initialClickY == -1) {
				if (isMouseYWithinSlotBounds(mouseY)) {
					boolean flag1 = true;

					if (mouseY >= top && mouseY <= bottom) {
						int l = 0;
						int r = getScrollBarX();
						int y = mouseY - top - headerPadding + (int) amountScrolled - 4;
						int visibleIndex = y / slotHeight;

						if (visibleIndex < getVisibleEntryNum() && mouseX >= l && mouseX <= r && visibleIndex >= 0 && y >= 0) {
							boolean flag = toEntryIndex(visibleIndex) == selectedIndex && Minecraft.getSystemTime() - lastClicked < 250L;
							visibleElementClicked(visibleIndex, flag, mouseX, mouseY);
							selectedIndex = toEntryIndex(visibleIndex);
							lastClicked = Minecraft.getSystemTime();
						} else if (mouseX >= l && mouseX <= r && y < 0) {
							clickedHeader(mouseX - l, mouseY - top + (int) amountScrolled - 4);
							flag1 = false;
						}

						int i3 = getScrollBarX();
						int j1 = i3 + 6;

						if (mouseX >= i3 && mouseX <= j1) {
							scrollMultiplier = -1.0F;
							int k1 = getMaxScroll();

							if (k1 < 1) {
								k1 = 1;
							}

							int l1 = (int) ((float) ((bottom - top) * (bottom - top)) / (float) getVisibleContentHeight());
							l1 = MathHelper.clamp(l1, 32, bottom - top - 8);
							scrollMultiplier /= (float) (bottom - top - l1) / (float) k1;
						} else {
							scrollMultiplier = 1.0F;
						}

						if (flag1) {
							initialClickY = mouseY;
						} else {
							initialClickY = -2;
						}
					} else {
						initialClickY = -2;
					}
				}
			} else if (initialClickY >= 0) {
				amountScrolled -= (float) (mouseY - initialClickY) * scrollMultiplier;
				initialClickY = mouseY;
			}
		} else {
			initialClickY = -1;
		}

		int i2 = Mouse.getEventDWheel();

		if (i2 != 0) {
			if (i2 > 0) {
				i2 = -1;
			} else {
				i2 = 1;
			}

			amountScrolled += (float) (i2 * slotHeight / 2);
		}

		for (IGuiListEntry entry : listEntries) {
			entry.handleMouseInput();
		}
	}

	public boolean mouseClicked(int mouseX, int mouseY, int mouseEvent) {
		if (!visible)
			return false;
		for (IGuiListEntry entry : listEntries) {
			if (entry.preMouseClicked(mouseX, mouseY, mouseEvent))
				return true;
		}
		if (isMouseYWithinSlotBounds(mouseY)) {
			int visibleIndex = getVisibleSlotIndexFromScreenCoords(mouseX, mouseY);

			if (visibleIndex >= 0) {
				int x = left + width / 2 - getListWidth() / 2 + 2;
				int y = top + 4 - getAmountScrolled() + visibleIndex * slotHeight + headerPadding;
				int rx = mouseX - x;
				int ry = mouseY - y;

				int index = toEntryIndex(visibleIndex);
				if (index >= 0) {//直前でgetVisibleSlotIndexFromScreenCoordsを呼んでるので必ず満たすはず
					IGuiListEntry entry = getListEntry(index);
					if (entry.mousePressed(index, mouseX, mouseY, mouseEvent, rx, ry)) {
						setEnabled(false);
						return true;
					}
				}
			}
		}

		return false;
	}

	public boolean mouseReleased(int mouseX, int mouseY, int mouseEvent) {
		if (!visible)
			return false;
		int x = left + width / 2 - getListWidth() / 2 + 2;
		int y = top + 4 - getAmountScrolled() + headerPadding;
		int rx = mouseX - x;
		for (int i = 0; i < getEntryNum(); ++i) {
			int ry = mouseY - y;
			IGuiListEntry entry = getListEntry(i);
			entry.mouseReleased(i, mouseX, mouseY, mouseEvent, rx, ry);
			if (entry.isVisible())
				y += slotHeight;
		}

		setEnabled(true);
		return false;
	}

	public void keyTyped(char eventChar, int eventKey) {
		if (!visible)
			return;
		for (IGuiListEntry entry : listEntries) {
			entry.keyTyped(eventChar, eventKey);
		}
	}

	public void updateScreen() {
		if (!visible)
			return;
		for (IGuiListEntry entry : listEntries) {
			entry.updateScreen();
		}
	}

	//内部

	protected void visibleElementClicked(int visibleIndex, boolean isDoubleClick, int mouseX, int mouseY) {
	}

	protected int toEntryIndex(int visibleIndex) {
		for (int i = 0; i < getEntryNum(); i++) {
			IGuiListEntry entry = getListEntry(i);
			if (!entry.isVisible())
				continue;
			if (visibleIndex == 0)
				return i;
			visibleIndex--;
		}
		return -1;
	}

	/**
	 * Returns true if the element passed in is currently selected
	 */
	protected boolean isSelected(int slotIndex) {
		return slotIndex == selectedIndex;
	}

	/**
	 * Return the height of the content being scrolled
	 */
	protected int getVisibleContentHeight() {
		int height = headerPadding;
		for (int i = 0; i < getEntryNum(); i++) {
			if (getListEntry(i).isVisible())
				height += slotHeight;
		}
		return height;
	}

	protected void drawBackground() {
	}

	protected void updateItemPos(int entryID, int insideLeft, int yPos, float partialTicks) {
		getListEntry(entryID).updatePosition(entryID, insideLeft, yPos, partialTicks);
	}

	/**
	 * Handles drawing a list's header row.
	 */
	protected void drawListHeader(int insideLeft, int insideTop, Tessellator tessellatorIn) {
	}

	protected void clickedHeader(int p_148132_1_, int p_148132_2_) {
	}

	protected void renderDecorations(int mouseXIn, int mouseYIn) {
	}

	/**
	 * Stop the thing from scrolling out of bounds
	 */
	protected void bindAmountScrolled() {
		amountScrolled = MathHelper.clamp(amountScrolled, 0.0F, (float) getMaxScroll());
	}

	/**
	 * Draws the selection box around the selected slot element.
	 */
	protected void drawSelectionBox(int insideLeft, int insideTop, int mouseXIn, int mouseYIn, float partialTicks) {
		int num = getEntryNum();
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();

		int y = insideTop + headerPadding;
		int visibleIndex = 0;
		for (int i = 0; i < num; ++i) {
			IGuiListEntry entry = getListEntry(i);
			if (y > bottom || y + height < top) {
				updateItemPos(i, insideLeft, y, partialTicks);
			}

			if (!entry.isVisible())
				continue;

			int height = slotHeight - 4;

			if (showSelectionBox && isSelected(i)) {
				int i1 = left + (width / 2 - getListWidth() / 2);
				int j1 = left + width / 2 + getListWidth() / 2;
				GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
				GlStateManager.disableTexture2D();
				bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
				bufferbuilder.pos(i1, y + height + 2, 0.0D).tex(0.0D, 1.0D).color(128, 128, 128, 255).endVertex();
				bufferbuilder.pos(j1, y + height + 2, 0.0D).tex(1.0D, 1.0D).color(128, 128, 128, 255).endVertex();
				bufferbuilder.pos(j1, y - 2, 0.0D).tex(1.0D, 0.0D).color(128, 128, 128, 255).endVertex();
				bufferbuilder.pos(i1, y - 2, 0.0D).tex(0.0D, 0.0D).color(128, 128, 128, 255).endVertex();
				bufferbuilder.pos(i1 + 1, y + height + 1, 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 255).endVertex();
				bufferbuilder.pos(j1 - 1, y + height + 1, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
				bufferbuilder.pos(j1 - 1, y - 1, 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 255).endVertex();
				bufferbuilder.pos(i1 + 1, y - 1, 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 255).endVertex();
				tessellator.draw();
				GlStateManager.enableTexture2D();
			}

			entry.drawEntry(i, insideLeft, y, getListWidth(), height, mouseXIn, mouseYIn, isMouseYWithinSlotBounds(mouseYIn) && getVisibleSlotIndexFromScreenCoords(mouseXIn, mouseYIn) == visibleIndex, partialTicks);
			y += slotHeight;
			visibleIndex++;
		}
	}

	/**
	 * Overlays the background to hide scrolled items
	 */
	protected void overlayBackground(int startY, int endY, int startAlpha, int endAlpha) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		mc.getTextureManager().bindTexture(Gui.OPTIONS_BACKGROUND);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		float f = 32.0F;
		bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
		bufferbuilder.pos(left, endY, 0.0D).tex(0.0D, (float) endY / 32.0F).color(64, 64, 64, endAlpha).endVertex();
		bufferbuilder.pos(left + width, endY, 0.0D).tex((float) width / 32.0F, (float) endY / 32.0F).color(64, 64, 64, endAlpha).endVertex();
		bufferbuilder.pos(left + width, startY, 0.0D).tex((float) width / 32.0F, (float) startY / 32.0F).color(64, 64, 64, startAlpha).endVertex();
		bufferbuilder.pos(left, startY, 0.0D).tex(0.0D, (float) startY / 32.0F).color(64, 64, 64, startAlpha).endVertex();
		tessellator.draw();
	}

	protected void drawContainerBackground(Tessellator tessellator) {
		BufferBuilder buffer = tessellator.getBuffer();
		mc.getTextureManager().bindTexture(Gui.OPTIONS_BACKGROUND);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		float f = 32.0F;
		buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
		buffer.pos(left, bottom, 0.0D).tex((float) left / f, (float) (bottom + (int) amountScrolled) / f).color(32, 32, 32, 255).endVertex();
		buffer.pos(right, bottom, 0.0D).tex((float) right / f, (float) (bottom + (int) amountScrolled) / f).color(32, 32, 32, 255).endVertex();
		buffer.pos(right, top, 0.0D).tex((float) right / f, (float) (top + (int) amountScrolled) / f).color(32, 32, 32, 255).endVertex();
		buffer.pos(left, top, 0.0D).tex((float) left / f, (float) (top + (int) amountScrolled) / f).color(32, 32, 32, 255).endVertex();
		tessellator.draw();
	}

	@SideOnly(Side.CLIENT)
	public interface IGuiListEntry {
		void updatePosition(int slotIndex, int x, int y, float partialTicks);

		void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks);

		/**
		 * Called when the mouse is clicked within this entry. Returning true means that something within this entry was
		 * clicked and the list should not be dragged.
		 */
		default boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY) {
			return false;
		}

		default void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY) { }
		default boolean isVisible() { return true; }
		default void keyTyped(char eventChar, int eventKey) { }
		default void updateScreen() { }
		default boolean preMouseClicked(int mouseX, int mouseY, int mouseEvent) { return false; }
		default void handleMouseInput() { }
	}
}