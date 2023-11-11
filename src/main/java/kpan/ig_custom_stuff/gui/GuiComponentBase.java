package kpan.ig_custom_stuff.gui;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiLabel;

import java.io.IOException;
import java.util.List;

public abstract class GuiComponentBase extends Gui {

	public final Minecraft mc;
	public final int x;
	public final int y;
	public final int width;
	public final int height;
	protected final List<GuiButton> buttonList = Lists.newArrayList();
	protected final List<GuiLabel> labelList = Lists.newArrayList();
	protected GuiButton selectedButton;
	public GuiComponentBase(Minecraft mc, int x, int y, int width, int height) {
		this.mc = mc;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	public void keyTyped(char typedChar, int keyCode) {

	}

	public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		for (GuiButton guibutton : buttonList) {
			if (guibutton.mousePressed(mc, mouseX, mouseY)) {
				selectedButton = guibutton;
				guibutton.playPressSound(mc.getSoundHandler());
				actionPerformed(guibutton, mouseButton);
			}
		}
	}

	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		for (GuiButton guiButton : buttonList) {
			guiButton.drawButton(mc, mouseX, mouseY, partialTicks);
		}
		for (GuiLabel guiLabel : labelList) {
			guiLabel.drawLabel(mc, mouseX, mouseY);
		}
	}

	protected void actionPerformed(GuiButton button, int mouseButton) {

	}

	protected <T extends GuiButton> T addButton(T buttonIn) {
		buttonList.add(buttonIn);
		return buttonIn;
	}

}
