package kpan.ig_custom_stuff.gui.item;

import kpan.ig_custom_stuff.ModReference;
import kpan.ig_custom_stuff.gui.GuiDropDownButton;
import kpan.ig_custom_stuff.gui.IMyGuiScreen;
import kpan.ig_custom_stuff.item.ItemEntry;
import kpan.ig_custom_stuff.item.ItemLangEntry;
import kpan.ig_custom_stuff.item.ItemPropertyEntry;
import kpan.ig_custom_stuff.item.model.ItemModelEntry;
import kpan.ig_custom_stuff.network.MyPacketHandler;
import kpan.ig_custom_stuff.network.client.MessageRegisterItemEntryToServer;
import kpan.ig_custom_stuff.network.client.MessageReplaceItemEntryToServer;
import kpan.ig_custom_stuff.registry.MCRegistryUtil;
import kpan.ig_custom_stuff.resource.ids.ItemId;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

@SideOnly(Side.CLIENT)
public class GuiAddEditItem extends GuiScreen implements IMyGuiScreen {

	public static GuiAddEditItem add(IMyGuiScreen parent) {
		return new GuiAddEditItem(parent, true, null, null);
	}
	public static GuiAddEditItem edit(IMyGuiScreen parent, ItemEntry itemEntry, ItemModelEntry itemModelEntry, ItemLangEntry itemLangEntry) {
		GuiAddEditItem gui = new GuiAddEditItem(parent, false, itemEntry.propertyEntry, itemModelEntry);
		gui.initItemId = itemEntry.itemId.toString();
		gui.initItemName = itemLangEntry.usName;
		return gui;
	}

	private final IMyGuiScreen parent;
	private final boolean isAdd;
	private GuiButton createButton;
	private String initItemId = "";
	private String initItemName = "";
	private GuiTextField itemIdField;
	private @Nullable String itemIdError = null;
	private GuiTextField itemNameField;
	private GuiButton itemModelBtn;
	private ItemPropertyEntry itemPropertyEntry;
	private @Nullable ItemModelEntry itemModelEntry;

	private GuiAddEditItem(IMyGuiScreen parent, boolean isAdd, @Nullable ItemPropertyEntry itemPropertyEntry, @Nullable ItemModelEntry itemModelEntry) {
		this.parent = parent;
		this.isAdd = isAdd;
		this.itemPropertyEntry = itemPropertyEntry != null ? itemPropertyEntry : ItemPropertyEntry.defaultOption();
		this.itemModelEntry = itemModelEntry;
	}

	@Override
	public void initGui() {
		Keyboard.enableRepeatEvents(true);
		createButton = addButton(new GuiButton(0, width / 2 - 155, height - 28, 150, 20, I18n.format("gui.done")));
		addButton(new GuiButton(1, width / 2 + 5, height - 28, 150, 20, I18n.format("gui.cancel")));
		itemIdField = new GuiTextField(100, fontRenderer, 100, 40, 200, 20);
		itemIdField.setMaxStringLength(32767);
		itemIdField.setText(initItemId);

		itemNameField = new GuiTextField(101, fontRenderer, 100, 80, 200, 20);
		itemNameField.setMaxStringLength(32767);
		itemNameField.setText(initItemName);

		itemModelBtn = addButton(new GuiDropDownButton(2, 100, 150, 200, 20, ""));


		addButton(new GuiButton(3, 100, 120, 200, 20, I18n.format("gui.ingame_custom_stuff.addedit_item.edit_item_property")));


		itemIdField.setFocused(true);
		itemIdField.setEnabled(isAdd);
		updateModelButton();
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
				String itemId = itemIdField.getText();
				if (!itemId.contains(":"))
					itemId = ModReference.DEFAULT_NAMESPACE + ":" + itemId;
				ItemEntry itemEntry = new ItemEntry(new ItemId(new ResourceLocation(itemId)), itemPropertyEntry);
				ItemLangEntry itemLangEntry = new ItemLangEntry(itemNameField.getText());
				if (isAdd)
					MyPacketHandler.sendToServer(new MessageRegisterItemEntryToServer(itemEntry, itemModelEntry, itemLangEntry));
				else
					MyPacketHandler.sendToServer(new MessageReplaceItemEntryToServer(itemEntry, itemModelEntry, itemLangEntry));
				parent.redisplay();
			}
			case 1 -> parent.redisplay();
			case 2 -> {
				mc.displayGuiScreen(new GuiConfigureItemModel(this, itemModelEntry, blockStateEntry -> itemModelEntry = blockStateEntry));
			}
			case 3 -> {
				mc.displayGuiScreen(new GuiEditItemProperty(this, itemPropertyEntry, blockPropertyEntry -> itemPropertyEntry = blockPropertyEntry));
			}
		}
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) {
		itemIdField.textboxKeyTyped(typedChar, keyCode);
		itemNameField.textboxKeyTyped(typedChar, keyCode);

		checkValid();
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		itemIdField.mouseClicked(mouseX, mouseY, mouseButton);
		itemNameField.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		if (isAdd)
			drawCenteredString(fontRenderer, I18n.format("gui.ingame_custom_stuff.addedit_item.title.add"), width / 2, 20, -1);
		else
			drawCenteredString(fontRenderer, I18n.format("gui.ingame_custom_stuff.addedit_item.title.edit"), width / 2, 20, -1);

		drawString(fontRenderer, I18n.format("gui.ingame_custom_stuff.addedit_item.label.item_id"), 20, 40 + 7, 0xFFA0A0A0);
		itemIdField.drawTextBox();
		if (itemIdError != null)
			drawString(fontRenderer, I18n.format(itemIdError), 100 + 4, 60 + 4, 0xFFFF2222);
		else if (!itemIdField.getText().contains(":"))
			drawString(fontRenderer, I18n.format("gui.ingame_custom_stuff.info.default_namespace_message", ModReference.DEFAULT_NAMESPACE), 100 + 4, 60 + 4, 0xFF22FF22);

		drawString(fontRenderer, I18n.format("gui.ingame_custom_stuff.addedit_item.label.name"), 20, 80 + 7, 0xFFA0A0A0);
		itemNameField.drawTextBox();
		if (itemNameField.getText().isEmpty())
			drawString(fontRenderer, I18n.format("gui.ingame_custom_stuff.error.name.empty"), 100 + 4, 100 + 4, 0xFFFF2222);

		drawString(fontRenderer, I18n.format("gui.ingame_custom_stuff.addedit_item.label.property"), 20, 120 + 7, 0xFFA0A0A0);

		drawString(fontRenderer, I18n.format("gui.ingame_custom_stuff.addedit_item.label.model"), 20, 150 + 7, 0xFFA0A0A0);
		if (itemModelEntry == null)
			drawString(fontRenderer, I18n.format("gui.ingame_custom_stuff.error.model.not_configured"), 100 + 4, 170 + 4, 0xFFFF2222);


		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	public void redisplay() {
		initItemId = itemIdField.getText();
		initItemName = itemNameField.getText();
		mc.displayGuiScreen(this);
	}


	private void checkValid() {
		if (isAdd) {
			String itemId = itemIdField.getText();
			if (!itemId.contains(":"))
				itemId = ModReference.DEFAULT_NAMESPACE + ":" + itemId;
			itemIdError = MCRegistryUtil.getItemIdErrorMessage(itemId);
		}
		createButton.enabled =
				itemIdError == null
						&& itemModelEntry != null
						&& !itemNameField.getText().isEmpty();
	}

	private void updateModelButton() {
		if (itemModelEntry == null) {
			itemModelBtn.displayString = I18n.format("ingame_custom_stuff.item_model.none");
		} else {
			itemModelBtn.displayString = mc.fontRenderer.trimStringToWidth(itemModelEntry.getString(), itemModelBtn.width - 4);
		}
	}
}