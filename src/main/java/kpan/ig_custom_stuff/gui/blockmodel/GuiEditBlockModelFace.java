package kpan.ig_custom_stuff.gui.blockmodel;

import kpan.ig_custom_stuff.block.TextureUV;
import kpan.ig_custom_stuff.block.model.BlockModelTextureEntry;
import kpan.ig_custom_stuff.gui.IMyGuiScreen;
import kpan.ig_custom_stuff.gui.MyGuiList;
import kpan.ig_custom_stuff.util.RenderUtil;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.Arrays;
import java.util.function.Consumer;

@SideOnly(Side.CLIENT)
public class GuiEditBlockModelFace extends GuiScreen implements IMyGuiScreen {

	private final IMyGuiScreen parent;
	private GuiButton doneButton;
	private MyGuiList guiList;
	private final BlockModelTextureEntry beforeBlockModelTextureEntry;
	private final Consumer<BlockModelTextureEntry> onCompleted;
	private int textureSize;
	private TextureUV uv;
	private int rotation;


	public GuiEditBlockModelFace(IMyGuiScreen parent, BlockModelTextureEntry beforeBlockModelTextureEntry, Consumer<BlockModelTextureEntry> onCompleted) {
		this.parent = parent;
		this.beforeBlockModelTextureEntry = beforeBlockModelTextureEntry;
		this.onCompleted = onCompleted;
		uv = beforeBlockModelTextureEntry.uv;
		rotation = beforeBlockModelTextureEntry.rotation;
	}

	@Override
	public void initGui() {
		Keyboard.enableRepeatEvents(true);
		doneButton = addButton(new GuiButton(0, width / 2 - 155, height - 28, 150, 20, I18n.format("gui.done")));
		addButton(new GuiButton(1, width / 2 + 5, height - 28, 150, 20, I18n.format("gui.cancel")));

		guiList = new MyGuiList(this, mc, (int) (width * 0.8), height, 30, height - 30);
		guiList.addFloatButton("gui.ingame_custom_stuff.edit_block_model_face.min_u", uv.minU, 0, 16, this::setMinU);
		guiList.addFloatButton("gui.ingame_custom_stuff.edit_block_model_face.min_v", uv.minV, 0, 16, this::setMinV);
		guiList.addFloatButton("gui.ingame_custom_stuff.edit_block_model_face.max_u", uv.maxU, 0, 16, this::setMaxU);
		guiList.addFloatButton("gui.ingame_custom_stuff.edit_block_model_face.max_v", uv.maxV, 0, 16, this::setMaxV);
		guiList.addValuesButton("gui.ingame_custom_stuff.edit_block_model_face.rotation", rotation, r -> r + "", Arrays.asList(0, 90, 180, 270), this::setRotation);
		guiList.initGui();

		textureSize = width - guiList.width;

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
				onCompleted.accept(new BlockModelTextureEntry(beforeBlockModelTextureEntry.textureId, uv, rotation));
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
		guiList.drawScreen(mouseX, mouseY, partialTicks);
		drawCenteredString(fontRenderer, I18n.format("gui.ingame_custom_stuff.edit_block_model_face.title"), width / 2, 20, 0xffffffff);
		super.drawScreen(mouseX, mouseY, partialTicks);
		guiList.drawScreenPost(mouseX, mouseY, partialTicks);

		TextureAtlasSprite sprite = mc.getTextureMapBlocks().getAtlasSprite(beforeBlockModelTextureEntry.textureId.toString());
		drawTexture(guiList.width, height / 2 - textureSize, sprite);
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

	private void setMinU(float value) {
		uv = uv.withMinU(value);
	}
	private void setMinV(float value) {
		uv = uv.withMinV(value);
	}
	private void setMaxU(float value) {
		uv = uv.withMaxU(value);
	}
	private void setMaxV(float value) {
		uv = uv.withMaxV(value);
	}
	private void setRotation(int rotation) {
		this.rotation = rotation;
	}

	private void drawTexture(int x, int y, TextureAtlasSprite sprite) {
		Gui.drawRect(x, y, x + textureSize, y + textureSize, -1);
		GlStateManager.bindTexture(mc.getTextureMapBlocks().getGlTextureId());
		RenderUtil.drawTexturedModalRect(x, y, zLevel, sprite, uv.minU, uv.minV, uv.maxU, uv.maxV, rotation, textureSize, textureSize);
	}
}