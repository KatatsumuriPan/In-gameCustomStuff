package kpan.ig_custom_stuff.gui.blockmodel;

import kpan.ig_custom_stuff.gui.IMyGuiScreen;
import kpan.ig_custom_stuff.gui.MyGuiList;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

@SideOnly(Side.CLIENT)
public class GuiAddBlockModel extends GuiScreen implements IMyGuiScreen {

	public final IMyGuiScreen parent;
	private MyGuiList guiList;

	public GuiAddBlockModel(IMyGuiScreen parent) {
		this.parent = parent;
	}

	@Override
	public void initGui() {
		addButton(new GuiButton(0, width / 2 - 100, height - 28, 200, 20, I18n.format("gui.cancel")));

		guiList = new MyGuiList(this, mc, width, height, 30, height - 30);
		guiList.setViewWidth(200);
		guiList.addButton("gui.ingame_custom_stuff.add_block_model.button_label.simple", () -> mc.displayGuiScreen(GuiAddEditBlockModelNormal.add(parent, GuiAddBlockModel.this)));
		guiList.addButton("gui.ingame_custom_stuff.add_block_model.button_label.slab", () -> mc.displayGuiScreen(GuiAddEditBlockModelSlab.add(parent, GuiAddBlockModel.this)));
		guiList.addButton("gui.ingame_custom_stuff.add_block_model.button_label.stair", () -> mc.displayGuiScreen(GuiAddEditBlockModelStair.add(parent, GuiAddBlockModel.this)));
		guiList.initGui();

	}

	@Override
	public void redisplay() {
		mc.displayGuiScreen(this);
	}


	@Override
	protected void keyTyped(char typedChar, int keyCode) {
		if (keyCode == Keyboard.KEY_ESCAPE) {
			mc.displayGuiScreen(null);
		}
		guiList.keyTyped(typedChar, keyCode);
	}

	@Override
	public void updateScreen() {
		super.updateScreen();
		guiList.updateScreen();
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		guiList.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	public void handleMouseInput() throws IOException {
		super.handleMouseInput();
		guiList.handleMouseInput();
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		if (button.enabled) {
			if (button.id == 0) {
				mc.displayGuiScreen(null);
			}
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		guiList.drawScreen(mouseX, mouseY, partialTicks);
		super.drawScreen(mouseX, mouseY, partialTicks);
		drawCenteredString(fontRenderer, I18n.format("gui.ingame_custom_stuff.add_block_model.title"), width / 2, 8 - 4, 16777215);
		guiList.drawScreenPost(mouseX, mouseY, partialTicks);
	}


}