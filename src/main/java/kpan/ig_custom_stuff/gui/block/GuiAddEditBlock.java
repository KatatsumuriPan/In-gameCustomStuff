package kpan.ig_custom_stuff.gui.block;

import kpan.ig_custom_stuff.ModReference;
import kpan.ig_custom_stuff.block.BlockEntry;
import kpan.ig_custom_stuff.block.BlockLangEntry;
import kpan.ig_custom_stuff.block.BlockPropertyEntry;
import kpan.ig_custom_stuff.block.BlockStateEntry;
import kpan.ig_custom_stuff.gui.GuiDropDownButton;
import kpan.ig_custom_stuff.gui.IMyGuiScreen;
import kpan.ig_custom_stuff.network.MyPacketHandler;
import kpan.ig_custom_stuff.network.client.MessageRegisterBlockEntryToServer;
import kpan.ig_custom_stuff.network.client.MessageReplaceBlockEntryToServer;
import kpan.ig_custom_stuff.registry.MCRegistryUtil;
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
public class GuiAddEditBlock extends GuiScreen implements IMyGuiScreen {

	public static GuiAddEditBlock add(IMyGuiScreen parent) {
		return new GuiAddEditBlock(parent, true, null, null);
	}
	public static GuiAddEditBlock edit(IMyGuiScreen parent, BlockEntry blockEntry, BlockStateEntry blockStateEntry, BlockLangEntry blockLangEntry) {
		GuiAddEditBlock gui = new GuiAddEditBlock(parent, false, blockEntry.basicProperty, blockStateEntry);
		gui.initBlockId = blockEntry.blockId.toString();
		gui.initBlockName = blockLangEntry.usName;
		return gui;
	}

	private final IMyGuiScreen parent;
	private final boolean isAdd;
	private GuiButton createButton;
	private String initBlockId = "";
	private String initBlockName = "";
	private GuiTextField blockIdField;
	private @Nullable String blockIdError = null;
	private GuiTextField blockNameField;
	private GuiButton blockModelBtn;
	private BlockPropertyEntry blockPropertyEntry;
	private @Nullable BlockStateEntry blockStateEntry;

	private GuiAddEditBlock(IMyGuiScreen parent, boolean isAdd, @Nullable BlockPropertyEntry blockPropertyEntry, @Nullable BlockStateEntry blockStateEntry) {
		this.parent = parent;
		this.isAdd = isAdd;
		this.blockPropertyEntry = blockPropertyEntry != null ? blockPropertyEntry : BlockPropertyEntry.defaultOption();
		this.blockStateEntry = blockStateEntry;
	}

	@Override
	public void initGui() {
		Keyboard.enableRepeatEvents(true);
		createButton = addButton(new GuiButton(0, width / 2 - 155, height - 28, 150, 20, I18n.format("gui.done")));
		addButton(new GuiButton(1, width / 2 + 5, height - 28, 150, 20, I18n.format("gui.cancel")));
		blockIdField = new GuiTextField(100, fontRenderer, 100, 40, 200, 20);
		blockIdField.setMaxStringLength(32767);
		blockIdField.setText(initBlockId);

		blockNameField = new GuiTextField(101, fontRenderer, 100, 80, 200, 20);
		blockNameField.setMaxStringLength(32767);
		blockNameField.setText(initBlockName);

		blockModelBtn = addButton(new GuiDropDownButton(2, 100, 150, 200, 20, ""));


		addButton(new GuiButton(3, 100, 120, 200, 20, I18n.format("gui.ingame_custom_stuff.addedit_block.edit_block_property")));


		blockIdField.setFocused(true);
		blockIdField.setEnabled(isAdd);
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
				String blockId = blockIdField.getText();
				if (!blockId.contains(":"))
					blockId = ModReference.DEFAULT_NAMESPACE + ":" + blockId;
				BlockEntry blockEntry = new BlockEntry(new ResourceLocation(blockId), blockPropertyEntry);
				BlockLangEntry blockLangEntry = new BlockLangEntry(blockNameField.getText());
				if (isAdd)
					MyPacketHandler.sendToServer(new MessageRegisterBlockEntryToServer(blockEntry, blockStateEntry, blockLangEntry));
				else
					MyPacketHandler.sendToServer(new MessageReplaceBlockEntryToServer(blockEntry, blockStateEntry, blockLangEntry));
				parent.redisplay();
			}
			case 1 -> parent.redisplay();
			case 2 -> {
				mc.displayGuiScreen(new GuiConfigureBlockState(this, blockStateEntry, blockStateEntry -> this.blockStateEntry = blockStateEntry));
			}
			case 3 -> {
				mc.displayGuiScreen(new GuiEditBlockProperty(this, blockPropertyEntry, blockPropertyEntry -> this.blockPropertyEntry = blockPropertyEntry));
			}
		}
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) {
		blockIdField.textboxKeyTyped(typedChar, keyCode);
		blockNameField.textboxKeyTyped(typedChar, keyCode);

		checkValid();
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		blockIdField.mouseClicked(mouseX, mouseY, mouseButton);
		blockNameField.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		if (isAdd)
			drawCenteredString(fontRenderer, I18n.format("gui.ingame_custom_stuff.addedit_block.title.add"), width / 2, 20, -1);
		else
			drawCenteredString(fontRenderer, I18n.format("gui.ingame_custom_stuff.addedit_block.title.edit"), width / 2, 20, -1);

		drawString(fontRenderer, I18n.format("gui.ingame_custom_stuff.addedit_block.label.block_id"), 20, 40 + 7, 0xFFA0A0A0);
		blockIdField.drawTextBox();
		if (blockIdError != null)
			drawString(fontRenderer, I18n.format(blockIdError), 100 + 4, 60 + 4, 0xFFFF2222);
		else if (!blockIdField.getText().contains(":"))
			drawString(fontRenderer, I18n.format("gui.ingame_custom_stuff.info.default_namespace_message", ModReference.DEFAULT_NAMESPACE), 100 + 4, 60 + 4, 0xFF22FF22);

		drawString(fontRenderer, I18n.format("gui.ingame_custom_stuff.addedit_block.label.name"), 20, 80 + 7, 0xFFA0A0A0);
		blockNameField.drawTextBox();
		if (blockNameField.getText().isEmpty())
			drawString(fontRenderer, I18n.format("gui.ingame_custom_stuff.error.name.empty"), 100 + 4, 100 + 4, 0xFFFF2222);

		drawString(fontRenderer, I18n.format("gui.ingame_custom_stuff.addedit_block.label.property"), 20, 120 + 7, 0xFFA0A0A0);

		drawString(fontRenderer, I18n.format("gui.ingame_custom_stuff.addedit_block.label.model"), 20, 150 + 7, 0xFFA0A0A0);
		if (blockStateEntry == null)
			drawString(fontRenderer, I18n.format("gui.ingame_custom_stuff.error.model.not_configured"), 100 + 4, 170 + 4, 0xFFFF2222);


		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	public void redisplay() {
		initBlockId = blockIdField.getText();
		initBlockName = blockNameField.getText();
		mc.displayGuiScreen(this);
	}


	private void checkValid() {
		if (isAdd) {
			String blockId = blockIdField.getText();
			if (!blockId.contains(":"))
				blockId = ModReference.DEFAULT_NAMESPACE + ":" + blockId;
			blockIdError = MCRegistryUtil.getBlockIdErrorMessage(blockId);
		}
		createButton.enabled =
				blockIdError == null
						&& blockStateEntry != null
						&& !blockNameField.getText().isEmpty();
	}

	private void updateModelButton() {
		if (blockStateEntry == null) {
			blockModelBtn.displayString = I18n.format("ingame_custom_stuff.block_model.none");
		} else {
			blockModelBtn.displayString = mc.fontRenderer.trimStringToWidth(blockStateEntry.getString(), blockModelBtn.width - 4);
		}
	}
}