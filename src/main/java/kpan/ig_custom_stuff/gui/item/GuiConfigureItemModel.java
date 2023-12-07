package kpan.ig_custom_stuff.gui.item;

import kpan.ig_custom_stuff.gui.IMyGuiScreen;
import kpan.ig_custom_stuff.item.model.ItemModelEntry;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.function.Consumer;

@SideOnly(Side.CLIENT)
public class GuiConfigureItemModel extends GuiScreen implements IMyGuiScreen {

	private final IMyGuiScreen parent;
	private GuiButton doneButton;
	private GuiSelectItemModel guiSelectItemModel = null;
	private final @Nullable ItemModelEntry itemModelEntry;
	private final Consumer<ItemModelEntry> onCompleted;

	public GuiConfigureItemModel(IMyGuiScreen parent, @Nullable ItemModelEntry itemModelEntry, Consumer<ItemModelEntry> onCompleted) {
		this.parent = parent;
		this.itemModelEntry = itemModelEntry;
		this.onCompleted = onCompleted;
	}

	@Override
	public void initGui() {
		Keyboard.enableRepeatEvents(true);
		doneButton = addButton(new GuiButton(0, width / 2 - 155, height - 28, 150, 20, I18n.format("gui.done")));
		addButton(new GuiButton(1, width / 2 + 5, height - 28, 150, 20, I18n.format("gui.cancel")));
		if (guiSelectItemModel != null)
			guiSelectItemModel = new GuiSelectItemModel(mc, 0, 40, width, height - 60, fontRenderer, this, guiSelectItemModel);
		else
			guiSelectItemModel = new GuiSelectItemModel(mc, 0, 40, width, height - 60, fontRenderer, this, itemModelEntry);

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
				onCompleted.accept(guiSelectItemModel.getModelEntry());
				parent.redisplay();
			}
			case 1 -> parent.redisplay();
		}
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) {
		guiSelectItemModel.keyTyped(typedChar, keyCode);
		checkValid();
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		if (guiSelectItemModel.isExtended()) {
			guiSelectItemModel.mouseClicked(mouseX, mouseY, mouseButton);
			return;
		}
		guiSelectItemModel.mouseClicked(mouseX, mouseY, mouseButton);
		super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		drawCenteredString(fontRenderer, I18n.format("gui.ingame_custom_stuff.configure_item_model.title"), width / 2, 20, 0xffffffff);

		super.drawScreen(mouseX, mouseY, partialTicks);
		guiSelectItemModel.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	public void redisplay() {
		mc.displayGuiScreen(this);
	}


	private void checkValid() {
		doneButton.enabled = guiSelectItemModel.isValid();
	}
}