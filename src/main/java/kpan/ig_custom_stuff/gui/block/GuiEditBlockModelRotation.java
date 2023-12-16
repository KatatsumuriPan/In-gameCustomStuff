package kpan.ig_custom_stuff.gui.block;

import kpan.ig_custom_stuff.gui.IMyGuiScreen;
import kpan.ig_custom_stuff.gui.MyGuiList;
import kpan.ig_custom_stuff.resource.DynamicResourceLoader.SingleBlockModelLoader;
import kpan.ig_custom_stuff.resource.ids.BlockModelId;
import kpan.ig_custom_stuff.util.RenderUtil;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.Arrays;
import java.util.function.BiConsumer;

@SideOnly(Side.CLIENT)
public class GuiEditBlockModelRotation extends GuiScreen implements IMyGuiScreen {

	private final IMyGuiScreen parent;
	private GuiButton doneButton;
	private MyGuiList guiList;
	private final BiConsumer<Integer, Integer> onCompleted;
	private final BlockModelId modelId;
	private int rotationX;
	private int rotationY;
	private int drawLeft;
	private int drawWidth;


	public GuiEditBlockModelRotation(IMyGuiScreen parent, BlockModelId modelId, int rotationX, int rotationY, BiConsumer<Integer, Integer> onCompleted) {
		this.parent = parent;
		this.onCompleted = onCompleted;
		this.modelId = modelId;
		this.rotationX = rotationX;
		this.rotationY = rotationY;
	}

	@Override
	public void initGui() {
		Keyboard.enableRepeatEvents(true);
		doneButton = addButton(new GuiButton(0, width / 2 - 155, height - 28, 150, 20, I18n.format("gui.done")));
		addButton(new GuiButton(1, width / 2 + 5, height - 28, 150, 20, I18n.format("gui.cancel")));

		guiList = new MyGuiList(this, mc, (int) (width * 0.8), height, 30, height - 30);
		guiList.addValuesButton("gui.ingame_custom_stuff.edit_block_model_rotation.rotation_x", rotationX, r -> r + "", Arrays.asList(0, 90, 180, 270), this::setRotationX);
		guiList.addValuesButton("gui.ingame_custom_stuff.edit_block_model_rotation.rotation_y", rotationY, r -> r + "", Arrays.asList(0, 90, 180, 270), this::setRotationY);
		guiList.initGui();

		drawLeft = guiList.width;
		drawWidth = width - drawLeft;

		checkValid();
	}

	@Override
	public void onGuiClosed() {
		Keyboard.enableRepeatEvents(false);
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		switch (button.id) {
			case 0 -> {
				onCompleted.accept(rotationX, rotationY);
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
		if (!guiList.mouseClicked(mouseX, mouseY, mouseButton))
			super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		{
			int w = Math.min(height, drawWidth);
			int l = drawLeft + ((drawWidth - w) / 2);
			Gui.drawRect(drawLeft, 0, width, height, 0xFF000000);
			Gui.drawRect(l, 0, l + w, w, -1);
			IBakedModel model = SingleBlockModelLoader.getModel(modelId);
			if (model != null)
				RenderUtil.renderModel(l, 0, w / 16f, 30 - rotationY, 20, rotationX, model);
		}
		guiList.drawScreen(mouseX, mouseY, partialTicks);
		drawCenteredString(fontRenderer, I18n.format("gui.ingame_custom_stuff.edit_block_model_rotation.title"), width / 2, 20, 0xffffffff);
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

	private void setRotationX(int rotationX) {
		this.rotationX = rotationX;
	}
	private void setRotationY(int rotationY) {
		this.rotationY = rotationY;
	}

}