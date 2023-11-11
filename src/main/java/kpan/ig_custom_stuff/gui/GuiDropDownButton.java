package kpan.ig_custom_stuff.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;

public class GuiDropDownButton extends GuiButton {

	public final List<GuiButton> buttonList = new ArrayList<>();
	private final String defaultText;
	private int nextButtonY = y;
	private int extendState = 0;
	private int selectedButtonIndex = -1;
	private int maxWidth = 0;

	public GuiDropDownButton(int buttonId, int x, int y, String buttonText) {
		super(buttonId, x, y, buttonText);
		defaultText = buttonText;
	}
	public GuiDropDownButton(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText) {
		super(buttonId, x, y, widthIn, heightIn, buttonText);
		defaultText = buttonText;
	}

	public void keyTyped(char typedChar, int keyCode) {
		if (isExtended() && keyCode == Keyboard.KEY_ESCAPE)
			extendState = 0;
	}

	public void postMouseClicked(Minecraft mc, int mouseX, int mouseY) {
		if (extendState == 0)
			return;
		if (extendState == 1) {
			extendState = 2;
			return;
		}
		extendState = 0;

		for (int i = 0; i < buttonList.size(); i++) {
			GuiButton button = buttonList.get(i);
			if (button.mousePressed(mc, mouseX, mouseY)) {
				selectedButtonIndex = i;
				button.playPressSound(mc.getSoundHandler());
				displayString = button.displayString;
			}
		}
	}

	public void onPressed() {
		if (extendState == 0) {
			extendState = 1;
		}
	}

	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
		if (extendState == 0) {
			super.drawButton(mc, mouseX, mouseY, partialTicks);
			return;
		}
		for (GuiButton button : buttonList) {
			button.drawButton(mc, mouseX, mouseY, partialTicks);
		}
	}

	//

	public GuiDropDownButton add(int width, int height, String text) {
		return add(new GuiButton(0, 0, 0, width, height, text));
	}

	private GuiDropDownButton add(GuiButton button) {
		button.x = x;
		button.y = nextButtonY;
		nextButtonY += button.height;
		maxWidth = Math.max(maxWidth, button.width);
		button.width = maxWidth;
		buttonList.add(button);
		return this;
	}

	public int getSelectedButtonIndex() {
		return selectedButtonIndex;
	}
	public void setSelectedButtonIndex(int index) {
		if (index >= 0 && index < buttonList.size()) {
			selectedButtonIndex = index;
			displayString = buttonList.get(index).displayString;
		} else {
			selectedButtonIndex = -1;
			displayString = defaultText;
		}
	}

	public boolean isExtended() {
		return extendState != 0;
	}

}
