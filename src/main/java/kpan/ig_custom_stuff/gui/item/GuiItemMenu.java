package kpan.ig_custom_stuff.gui.item;

import kpan.ig_custom_stuff.gui.IMyGuiScreen;
import kpan.ig_custom_stuff.item.ItemEntry;
import kpan.ig_custom_stuff.item.ItemLangEntry;
import kpan.ig_custom_stuff.item.model.ItemModelEntry;
import kpan.ig_custom_stuff.network.MyPacketHandler;
import kpan.ig_custom_stuff.network.client.MessageDeleteItemEntryToServer;
import kpan.ig_custom_stuff.registry.MCRegistryUtil;
import kpan.ig_custom_stuff.resource.DynamicResourceManager.ClientCache;
import kpan.ig_custom_stuff.resource.ids.ItemId;
import kpan.ig_custom_stuff.util.RenderUtil;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

@SideOnly(Side.CLIENT)
public class GuiItemMenu extends GuiScreen implements IMyGuiScreen {

	private final IMyGuiScreen parent;
	private GuiItemList itemList;
	private GuiTextField searchField;
	private GuiButton deleteItemButton;
	private GuiButton editItemButton;
	private int infoLeft;
	private int infoWidth;

	public GuiItemMenu(IMyGuiScreen parent) {
		this.parent = parent;
	}

	@Override
	public void initGui() {
		infoLeft = width / 2 + 200 / 2;
		infoWidth = width - infoLeft;
		addButton(new GuiButton(0, width / 2 - 200 / 2, height - 28, 200, 20, I18n.format("gui.done")));
		addButton(new GuiButton(1, 20, height - 28, 90, 20, I18n.format("gui.add")));
		searchField = new GuiTextField(0, mc.fontRenderer, 200, 2, infoLeft - 200 - 4, 16);

		deleteItemButton = addButton(new GuiButton(10, infoLeft, infoWidth + 40, infoWidth, 20, I18n.format("gui.delete")));
		editItemButton = addButton(new GuiButton(11, infoLeft, infoWidth + 80, infoWidth, 20, I18n.format("gui.edit")));

		itemList = new GuiItemList(mc, infoLeft, height, 20, height - 30);

		deleteItemButton.enabled = false;
		editItemButton.enabled = false;
		itemList.applyVisiblePredicate(null);
	}

	public void refreshList() {
		itemList.refreshList();
	}

	@Override
	public void redisplay() {
		mc.displayGuiScreen(this);
	}


	@Override
	protected void keyTyped(char typedChar, int keyCode) {
		if (keyCode == Keyboard.KEY_ESCAPE) {
			parent.redisplay();
		}
		searchField.textboxKeyTyped(typedChar, keyCode);
		itemList.applyVisiblePredicate(e -> StringUtils.containsIgnoreCase(e.itemId.toString(), searchField.getText()));
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		if (button.enabled) {
			switch (button.id) {
				case 0 -> parent.redisplay();
				case 1 -> mc.displayGuiScreen(GuiAddEditItem.add(this));
				case 10 -> {
					mc.displayGuiScreen(new GuiYesNo((result, id) -> {
						if (result)
							MyPacketHandler.sendToServer(new MessageDeleteItemEntryToServer(itemList.getSelectedItemId()));
						redisplay();
					}, I18n.format("gui.ingame_custom_stuff.item_menu.delete_item", itemList.getSelectedItemId()), I18n.format("gui.ingame_custom_stuff.warn.deleting_message"), 0));
				}
				case 11 -> {
					ItemId itemId = itemList.getSelectedItemId();
					ItemEntry itemEntry = MCRegistryUtil.getItem(itemId);
					if (itemEntry == null)
						throw new IllegalStateException();
					ItemModelEntry itemModelEntry = ClientCache.INSTANCE.getItemModel(itemId);
					if (itemModelEntry == null)
						itemModelEntry = ItemModelEntry.defaultModel();
					String lang = ClientCache.INSTANCE.getItemNameLang("en_us", itemId);
					mc.displayGuiScreen(GuiAddEditItem.edit(this, itemEntry, itemModelEntry, new ItemLangEntry(lang)));
				}
			}
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		itemList.drawScreen(mouseX, mouseY, partialTicks);
		drawCenteredString(fontRenderer, I18n.format("gui.ingame_custom_stuff.item_menu.title"), 145, 8 - 4, 16777215);
		int w = Math.min(height / 3, (int) (infoWidth * 0.8));
		int l = infoLeft + ((infoWidth - w) / 2);
		Gui.drawRect(infoLeft, 0, width, height, 0xFF000000);
		Gui.drawRect(l, 0, l + w, w, -1);
		ItemId itemId = itemList.getSelectedItemId();
		if (itemId != null) {
			Item item = Item.REGISTRY.getObject(itemId.toResourceLocation());
			if (item != null)
				RenderUtil.renderItemIntoGUI(new ItemStack(item, 1), l, 0, w / 16f);
			String usName = ClientCache.INSTANCE.getItemNameLang("en_us", itemId);
			drawString(mc.fontRenderer, usName, infoLeft + 4, w + 4, -1);
		}
		super.drawScreen(mouseX, mouseY, partialTicks);
		searchField.drawTextBox();
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		itemList.mouseClicked(mouseX, mouseY, mouseButton);
		searchField.mouseClicked(mouseX, mouseY, mouseButton);
		deleteItemButton.enabled = itemList.getSelectedItemId() != null;
		editItemButton.enabled = itemList.getSelectedItemId() != null;
	}

	@Override
	protected void mouseReleased(int mouseX, int mouseY, int state) {
		super.mouseReleased(mouseX, mouseY, state);
		itemList.mouseReleased(mouseX, mouseY, state);
	}

	@Override
	public void handleMouseInput() throws IOException {
		itemList.handleMouseInput();
		super.handleMouseInput();
	}

	@Override
	public void updateScreen() {
		super.updateScreen();
		itemList.updateScreen();
		searchField.updateCursorCounter();
	}

}