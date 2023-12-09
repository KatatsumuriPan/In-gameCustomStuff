package kpan.ig_custom_stuff.gui;

import com.google.common.collect.Lists;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

@SideOnly(Side.CLIENT)
public class GuiSimpleOk extends GuiScreen {

	protected Runnable onPushed;
	protected String title;
	private final String message;
	private final List<String> messageLines = Lists.<String>newArrayList();
	protected String okButtonText;

	public GuiSimpleOk(Runnable onPushed, String title, String message) {
		this(onPushed, title, message, "gui.ok");
	}
	public GuiSimpleOk(Runnable onPushed, String title, String message, String okButtonText) {
		this.onPushed = onPushed;
		this.title = I18n.format(title);
		this.message = I18n.format(message).replace("\\n", "\n");
		this.okButtonText = I18n.format(okButtonText);
	}

	@Override
	public void initGui() {
		buttonList.add(new GuiButton(0, width / 2 - 100, height / 6 + 96, okButtonText));
		messageLines.clear();
		messageLines.addAll(fontRenderer.listFormattedStringToWidth(message, width - 50));
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		onPushed.run();
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		drawCenteredString(fontRenderer, title, width / 2, 70, 16777215);
		int y = 90;

		for (String s : messageLines) {
			drawCenteredString(fontRenderer, s, width / 2, y, 16777215);
			y += fontRenderer.FONT_HEIGHT;
		}

		super.drawScreen(mouseX, mouseY, partialTicks);
	}

}