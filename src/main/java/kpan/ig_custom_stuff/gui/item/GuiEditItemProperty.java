package kpan.ig_custom_stuff.gui.item;

import kpan.ig_custom_stuff.gui.IMyGuiScreen;
import kpan.ig_custom_stuff.gui.MyGuiList;
import kpan.ig_custom_stuff.item.ItemPropertyEntry;
import kpan.ig_custom_stuff.item.ItemPropertyEntryBuilder;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.function.Consumer;

@SideOnly(Side.CLIENT)
public class GuiEditItemProperty extends GuiScreen implements IMyGuiScreen {

	private final IMyGuiScreen parent;
	private final ItemPropertyEntryBuilder builder;
	private final Consumer<ItemPropertyEntry> onCompleted;
	private GuiButton doneButton;
	private MyGuiList guiList;

	public GuiEditItemProperty(IMyGuiScreen parent, ItemPropertyEntry itemPropertyEntry, Consumer<ItemPropertyEntry> onCompleted) {
		this.parent = parent;
		builder = new ItemPropertyEntryBuilder(itemPropertyEntry);
		this.onCompleted = onCompleted;
	}

	@Override
	public void initGui() {
		Keyboard.enableRepeatEvents(true);
		doneButton = addButton(new GuiButton(0, width / 2 - 155, height - 28, 150, 20, I18n.format("gui.done")));
		addButton(new GuiButton(1, width / 2 + 5, height - 28, 150, 20, I18n.format("gui.cancel")));


		guiList = new MyGuiList(this, mc, width, height, 30, height - 30);
		guiList.addValuesButton("gui.ingame_custom_stuff.edit_item_property.creativeTab", builder.creativeTab, CreativeTabs::getTranslationKey, ItemPropertyEntry.ALL_CREATIVE_TABS.values(), builder::setCreativeTab);
		guiList.initGui();

		checkValid();
	}

	@Override
	public void onGuiClosed() {
		Keyboard.enableRepeatEvents(false);
	}

	public ItemPropertyEntry getPropertyEntry() {
		return builder.build();
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		switch (button.id) {
			case 0 -> {
				onCompleted.accept(getPropertyEntry());
				parent.redisplay();
			}
			case 1 -> parent.redisplay();
		}
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		super.keyTyped(typedChar, keyCode);
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
		drawCenteredString(fontRenderer, I18n.format("Block Property"), width / 2, 20, 0xffffffff);
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

}