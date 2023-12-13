package kpan.ig_custom_stuff.gui.block;

import kpan.ig_custom_stuff.block.BlockLangEntry;
import kpan.ig_custom_stuff.block.BlockStateEntry;
import kpan.ig_custom_stuff.gui.IMyGuiScreen;
import kpan.ig_custom_stuff.network.MyPacketHandler;
import kpan.ig_custom_stuff.network.client.MessageDeleteBlockEntryToServer;
import kpan.ig_custom_stuff.registry.MCRegistryUtil;
import kpan.ig_custom_stuff.resource.DynamicResourceManager.ClientCache;
import kpan.ig_custom_stuff.resource.ids.BlockId;
import kpan.ig_custom_stuff.util.RenderUtil;
import net.minecraft.block.Block;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

@SideOnly(Side.CLIENT)
public class GuiBlockMenu extends GuiScreen implements IMyGuiScreen {

	private final IMyGuiScreen parent;
	private GuiBlockList blockList;
	private GuiTextField searchField;
	private GuiButton deleteBlockButton;
	private GuiButton editBlockButton;
	private int infoLeft;
	private int infoWidth;

	public GuiBlockMenu(IMyGuiScreen parent) {
		this.parent = parent;
	}

	@Override
	public void initGui() {
		infoLeft = width / 2 + 200 / 2;
		infoWidth = width - infoLeft;
		addButton(new GuiButton(0, width / 2 - 200 / 2, height - 28, 200, 20, I18n.format("gui.done")));
		addButton(new GuiButton(1, 20, height - 28, 90, 20, I18n.format("gui.add")));
		searchField = new GuiTextField(0, mc.fontRenderer, 200, 2, infoLeft - 200 - 4, 16);

		deleteBlockButton = addButton(new GuiButton(10, infoLeft, infoWidth + 40, infoWidth, 20, I18n.format("gui.delete")));
		editBlockButton = addButton(new GuiButton(11, infoLeft, infoWidth + 80, infoWidth, 20, I18n.format("gui.edit")));

		blockList = new GuiBlockList(mc, infoLeft, height, 20, height - 30, this);

		deleteBlockButton.enabled = false;
		editBlockButton.enabled = false;
		blockList.applyVisiblePredicate(null);
	}

	public void refreshList() {
		blockList.refreshList();
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
		blockList.applyVisiblePredicate(e -> StringUtils.containsIgnoreCase(e.blockId.toString(), searchField.getText()));
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		if (button.enabled) {
			switch (button.id) {
				case 0 -> parent.redisplay();
				case 1 -> mc.displayGuiScreen(GuiAddEditBlock.add(this));
				case 10 -> {
					mc.displayGuiScreen(new GuiYesNo((result, id) -> {
						if (result)
							MyPacketHandler.sendToServer(new MessageDeleteBlockEntryToServer(blockList.getSelectedBlockId()));
						redisplay();
					}, I18n.format("gui.ingame_custom_stuff.addedit_block.delete_block", blockList.getSelectedBlockId()), I18n.format("gui.ingame_custom_stuff.warn.deleting_message"), 0));
				}
				case 11 -> {
					BlockId blockId = blockList.getSelectedBlockId();
					BlockStateEntry blockStateEntry = ClientCache.INSTANCE.getBlockState(blockId.toBlockStateId());
					if (blockStateEntry == null)
						throw new IllegalStateException();
					String lang = ClientCache.INSTANCE.getBlockNameLang("en_us", blockId);
					mc.displayGuiScreen(GuiAddEditBlock.edit(this, MCRegistryUtil.getBlock(blockId), blockStateEntry, new BlockLangEntry(lang)));
				}
			}
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		blockList.drawScreen(mouseX, mouseY, partialTicks);
		drawCenteredString(fontRenderer, I18n.format("gui.ingame_custom_stuff.block_menu.title"), 145, 8 - 4, 16777215);
		int w = Math.min(height / 3, (int) (infoWidth * 0.8));
		int l = infoLeft + ((infoWidth - w) / 2);
		Gui.drawRect(infoLeft, 0, width, height, 0xFF000000);
		Gui.drawRect(l, 0, l + w, w, -1);
		BlockId blockId = blockList.getSelectedBlockId();
		if (blockId != null) {
			Block block = Block.REGISTRY.getObject(blockId.toResourceLocation());
			RenderUtil.renderItemIntoGUI(new ItemStack(block, 1), l, 0, w / 16f);
			String usName = ClientCache.INSTANCE.getBlockNameLang("en_us", blockId);
			drawString(mc.fontRenderer, usName, infoLeft + 4, w + 4, -1);
		}
		super.drawScreen(mouseX, mouseY, partialTicks);
		searchField.drawTextBox();
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		blockList.mouseClicked(mouseX, mouseY, mouseButton);
		searchField.mouseClicked(mouseX, mouseY, mouseButton);
		deleteBlockButton.enabled = blockList.getSelectedBlockId() != null;
		editBlockButton.enabled = blockList.getSelectedBlockId() != null;
	}

	@Override
	protected void mouseReleased(int mouseX, int mouseY, int state) {
		super.mouseReleased(mouseX, mouseY, state);
		blockList.mouseReleased(mouseX, mouseY, state);
	}

	@Override
	public void handleMouseInput() throws IOException {
		blockList.handleMouseInput();
		super.handleMouseInput();
	}

	@Override
	public void updateScreen() {
		super.updateScreen();
		blockList.updateScreen();
		searchField.updateCursorCounter();
	}

}