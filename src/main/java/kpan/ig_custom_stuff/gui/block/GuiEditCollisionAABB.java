package kpan.ig_custom_stuff.gui.block;

import kpan.ig_custom_stuff.block.BlockCollisionAABB;
import kpan.ig_custom_stuff.gui.IMyGuiScreen;
import kpan.ig_custom_stuff.gui.MyGuiList;
import kpan.ig_custom_stuff.gui.MyGuiList.LabeledButtonEntry;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.config.HoverChecker;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@SideOnly(Side.CLIENT)
public class GuiEditCollisionAABB extends GuiScreen implements IMyGuiScreen {

	private final IMyGuiScreen parent;
	private GuiButton doneButton;
	private MyGuiList guiList;
	private final List<BlockCollisionAABB> beforeBlockCollisionAABBs;
	private final List<BlockCollisionAABB> blockCollisionAABBs;
	private final Consumer<List<BlockCollisionAABB>> onCompleted;
	private int infoLeft;
	private int infoWidth;
	private int lastMouseX = -1;
	private int lastMouseY = -1;
	private float rotateYaw = 10;
	private float rotatePitch = 30;


	public GuiEditCollisionAABB(IMyGuiScreen parent, List<BlockCollisionAABB> beforeBlockCollisionAABBs, Consumer<List<BlockCollisionAABB>> onCompleted, BlockStateContainer modelCache) {
		this.parent = parent;
		this.beforeBlockCollisionAABBs = beforeBlockCollisionAABBs;
		blockCollisionAABBs = new ArrayList<>(beforeBlockCollisionAABBs);
		this.onCompleted = onCompleted;
	}

	@Override
	public void initGui() {
		Keyboard.enableRepeatEvents(true);
		doneButton = addButton(new GuiButton(0, width / 2 - 155, height - 28, 150, 20, I18n.format("gui.done")));
		addButton(new GuiButton(1, width / 2 + 5, height - 28, 150, 20, I18n.format("gui.cancel")));
		infoLeft = (int) (width * 0.6);
		infoWidth = width - infoLeft;
		guiList = new MyGuiList(this, mc, infoLeft, height, 30, height - 30);
		guiList.setShowSelectionBox(true);
		refreshList();

		checkValid();
	}

	public void refreshList() {
		guiList.listEntries.clear();
		for (int i = 0; i < blockCollisionAABBs.size(); i++) {
			guiList.listEntries.add(new AABBEntry(guiList, "" + i, this));
		}
		guiList.initGui();
		guiList.controlX += fontRenderer.getStringWidth(BlockCollisionAABB.FULL_BLOCK.toString()) + 10;
		guiList.controlWidth -= fontRenderer.getStringWidth(BlockCollisionAABB.FULL_BLOCK.toString()) + 10;
	}

	@Override
	public void onGuiClosed() {
		Keyboard.enableRepeatEvents(false);
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		switch (button.id) {
			case 0 -> {
				onCompleted.accept(blockCollisionAABBs);
				parent.redisplay();
			}
			case 1 -> parent.redisplay();
		}
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) {
		guiList.keyTyped(typedChar, keyCode);
		checkValid();
	}

	@Override
	public void handleMouseInput() throws IOException {
		super.handleMouseInput();
		guiList.handleMouseInput();
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		if (guiList.mouseClicked(mouseX, mouseY, mouseButton))
			return;
		super.mouseClicked(mouseX, mouseY, mouseButton);
		if (mouseX >= infoLeft && mouseX <= width && mouseY >= 0 && mouseY <= infoLeft) {
			lastMouseX = mouseX;
			lastMouseY = mouseY;
		}
	}

	@Override
	protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
		super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
		if (lastMouseX == -1)
			return;
		rotateYaw += (mouseX - lastMouseX);
		rotatePitch += (mouseY - lastMouseY);
		rotatePitch = MathHelper.clamp(rotatePitch, -90, 90);
		lastMouseX = mouseX;
		lastMouseY = mouseY;
	}

	@Override
	protected void mouseReleased(int mouseX, int mouseY, int state) {
		super.mouseReleased(mouseX, mouseY, state);
		lastMouseX = -1;
		lastMouseY = -1;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		guiList.drawScreen(mouseX, mouseY, partialTicks);
		drawCenteredString(fontRenderer, I18n.format("gui.ingame_custom_stuff.edit_block_model_face.title"), infoLeft / 2, 20, 0xffffffff);

		Gui.drawRect(infoLeft, 0, width, height, 0);
		Gui.drawRect(infoLeft, 0, width, infoLeft, -1);
//		RenderUtil.renderModel(infoLeft, 80, Math.min(width - 10 - 250, height - 30 - 80) / 16f, rotateYaw, rotatePitch, modelCache);

		super.drawScreen(mouseX, mouseY, partialTicks);
		guiList.drawScreenPost(mouseX, mouseY, partialTicks);
	}

	@Override
	public void updateScreen() {
		super.updateScreen();
		guiList.updateScreen();
	}

	@Override
	public void redisplay() {
		mc.displayGuiScreen(this);
	}


	private void checkValid() {
		doneButton.enabled = true;
	}


	private static class AABBEntry extends LabeledButtonEntry {

		private final GuiEditCollisionAABB owner;

		private AABBEntry(MyGuiList myGuiList, String name, GuiEditCollisionAABB owner) {
			super(myGuiList, name);
			this.owner = owner;
			btnValue.displayString = "Remove";
		}

		@Override
		public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partial) {

			if (drawLabel) {
				String label = (!isValidValue ? TextFormatting.RED.toString() : TextFormatting.WHITE.toString()) + name
						+ TextFormatting.RESET + owner.blockCollisionAABBs.get(slotIndex);
				mc.fontRenderer.drawString(
						label,
						myGuiList.labelX,
						y + slotHeight / 2 - mc.fontRenderer.FONT_HEIGHT / 2,
						16777215);
			}

			if (tooltipHoverChecker == null)
				tooltipHoverChecker = new HoverChecker(y, y + slotHeight, x, myGuiList.controlX - 8, 800);
			else
				tooltipHoverChecker.updateBounds(y, y + slotHeight, x, myGuiList.controlX - 8);
			btnValue.width = myGuiList.controlWidth;
			btnValue.x = myGuiList.controlX;
			btnValue.y = y;
			btnValue.drawButton(mc, mouseX, mouseY, partial);
		}

		@Override
		public void updateValueButtonText() { }
		@Override
		public void valueButtonPressed(int slotIndex) {
			owner.blockCollisionAABBs.remove(slotIndex);
			owner.refreshList();
		}
	}

}