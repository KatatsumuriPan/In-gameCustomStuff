package kpan.ig_custom_stuff.gui.blockmodel;

import kpan.ig_custom_stuff.gui.IMyGuiScreen;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

@SideOnly(Side.CLIENT)
public class GuiAddBlockModel extends GuiScreen implements IMyGuiScreen {

	public final IMyGuiScreen parent;

	public GuiAddBlockModel(IMyGuiScreen parent) {
		this.parent = parent;
	}

	@Override
	public void initGui() {
		addButton(new GuiButton(0, width / 2 - 100, height - 28, 200, 20, I18n.format("gui.cancel")));


		addButton(new GuiButton(1, width / 2 - 100, 40, 200, 20, I18n.format("gui.ingame_custom_stuff.add_block_model.button_label.simple")));
		addButton(new GuiButton(2, width / 2 - 100, 80, 200, 20, I18n.format("gui.ingame_custom_stuff.add_block_model.button_label.slab")));

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
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		if (button.enabled) {
			switch (button.id) {
				case 0 -> mc.displayGuiScreen(null);
				case 1 -> mc.displayGuiScreen(GuiAddEditBlockModelNormal.add(parent, this));
				case 2 -> mc.displayGuiScreen(GuiAddEditBlockModelSlab.add(parent, this));
				default -> {
				}
			}
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		super.drawScreen(mouseX, mouseY, partialTicks);
		drawCenteredString(fontRenderer, I18n.format("gui.ingame_custom_stuff.add_block_model.title"), width / 2, 8 - 4, 16777215);
	}


}