package kpan.ig_custom_stuff.gui.block;

import kpan.ig_custom_stuff.ModReference;
import kpan.ig_custom_stuff.block.BlockStateEntry;
import kpan.ig_custom_stuff.block.BlockStateEntry.BlockstateType;
import kpan.ig_custom_stuff.gui.GuiDropDownButton;
import kpan.ig_custom_stuff.gui.IMyGuiScreen;
import kpan.ig_custom_stuff.resource.DynamicResourceManager;
import net.minecraft.client.gui.Gui;
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
import java.util.function.Consumer;

@SideOnly(Side.CLIENT)
public class GuiConfigureBlockState extends GuiScreen implements IMyGuiScreen {

	private final IMyGuiScreen parent;
	private GuiButton doneButton;
	private final @Nullable BlockStateEntry blockStateEntry;
	private final Consumer<BlockStateEntry> onCompleted;

	private GuiDropDownButton modelTypeBtn;
	private String initModelId = "";
	private GuiTextField modelIdFld;
	private @Nullable String modelIdError = null;

	public GuiConfigureBlockState(IMyGuiScreen parent, @Nullable BlockStateEntry blockStateEntry, Consumer<BlockStateEntry> onCompleted) {
		this.parent = parent;
		this.blockStateEntry = blockStateEntry;
		this.onCompleted = onCompleted;
		if (blockStateEntry != null) {
			initModelId = blockStateEntry.blockModelId.toString();
		} else {
			initModelId = "block/";
		}
	}

	@Override
	public void initGui() {
		Keyboard.enableRepeatEvents(true);
		doneButton = addButton(new GuiButton(0, width / 2 - 155, height - 28, 150, 20, I18n.format("gui.done")));
		addButton(new GuiButton(1, width / 2 + 5, height - 28, 150, 20, I18n.format("gui.cancel")));

		modelTypeBtn = addButton(new GuiDropDownButton(2, 100, 30, 200, 20, ""));
		modelIdFld = new GuiTextField(10, fontRenderer, 100, 60, 200, 20);
		modelIdFld.setMaxStringLength(32767);
		modelIdFld.setText(initModelId);
		BlockstateType type;
		if (blockStateEntry != null) {
			type = blockStateEntry.blockstateType;
		} else {
			type = BlockstateType.SIMPLE;
		}
		addButton(new GuiButton(3, 100 + 210, 60, 100, 20, I18n.format("gui.select")));

		BlockstateType[] values = BlockstateType.values();
		for (int i = 0; i < values.length; i++) {
			BlockstateType value = values[i];
			modelTypeBtn.add(100, 20, getString(value));
			if (value == type)
				modelTypeBtn.setSelectedButtonIndex(i);
		}
		checkValid();
	}

	@Override
	public void onGuiClosed() {
		Keyboard.enableRepeatEvents(false);
	}

	public BlockStateEntry getModelEntry() {
		String modelId = modelIdFld.getText();
		if (!modelId.contains(":"))
			modelId = ModReference.DEFAULT_NAMESPACE + ":" + modelId;
		return new BlockStateEntry(BlockstateType.values()[modelTypeBtn.getSelectedButtonIndex()], new ResourceLocation(modelId));
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		switch (button.id) {
			case 0 -> {
				onCompleted.accept(getModelEntry());
				parent.redisplay();
			}
			case 1 -> parent.redisplay();
			case 2 -> {
				modelTypeBtn.onPressed();
			}
			case 3 -> {
				mc.displayGuiScreen(new GuiSelectBlockModel(modelId -> {
					if (modelId != null)
						initModelId = modelId.toString();
					redisplay();
				}));
			}
		}
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (modelTypeBtn.isExtended()) {
			modelTypeBtn.keyTyped(typedChar, keyCode);
			return;
		}
		super.keyTyped(typedChar, keyCode);
		modelIdFld.textboxKeyTyped(typedChar, keyCode);
		checkValid();
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		if (modelTypeBtn.isExtended()) {
			modelTypeBtn.postMouseClicked(mc, mouseX, mouseY);
			return;
		}
		super.mouseClicked(mouseX, mouseY, mouseButton);
		modelTypeBtn.mousePressed(mc, mouseX, mouseY);
		modelIdFld.mouseClicked(mouseX, mouseY, mouseButton);
		modelTypeBtn.postMouseClicked(mc, mouseX, mouseY);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		drawCenteredString(fontRenderer, I18n.format("gui.ingame_custom_stuff.configure_block_state.title"), width / 2, 20, 0xffffffff);

		super.drawScreen(mouseX, mouseY, partialTicks);
		drawString(fontRenderer, I18n.format("gui.ingame_custom_stuff.configure_block_state.label.model_type"), 20, 30 + 7, 0xFFA0A0A0);
		drawString(fontRenderer, I18n.format("gui.ingame_custom_stuff.configure_block_state.label.model_id"), 20, 60 + 7, 0xFFA0A0A0);
		modelIdFld.drawTextBox();
		if (modelIdError != null)
			drawString(fontRenderer, I18n.format(modelIdError), 100 + 4, 80 + 4, 0xFFFF2222);
		else if (!modelIdFld.getText().contains(":"))
			drawString(fontRenderer, I18n.format("gui.ingame_custom_stuff.info.default_namespace_message", ModReference.DEFAULT_NAMESPACE), 100 + 4, 80 + 4, 0xFF22FF22);
		if (modelTypeBtn.isExtended())
			Gui.drawRect(0, 0, width, height, 0x80000000);
		modelTypeBtn.drawButton(mc, mouseX, mouseY, partialTicks);
	}

	@Override
	public void redisplay() {
		mc.displayGuiScreen(this);
	}


	private void checkValid() {
		String modelId = modelIdFld.getText();
		if (!modelId.contains(":"))
			modelId = ModReference.DEFAULT_NAMESPACE + ":" + modelId;
		modelIdError = DynamicResourceManager.getResourceIdErrorMessage(modelId, true);
		doneButton.enabled = modelIdError == null;
	}

	private static String getString(BlockstateType blockstateType) {
		return blockstateType.getString();
	}
}