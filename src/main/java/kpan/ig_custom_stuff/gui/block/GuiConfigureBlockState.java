package kpan.ig_custom_stuff.gui.block;

import com.google.common.collect.ImmutableMap;
import kpan.ig_custom_stuff.block.BlockStateEntry;
import kpan.ig_custom_stuff.block.BlockStateEntry.BlockStateType;
import kpan.ig_custom_stuff.gui.GuiDropDownButton;
import kpan.ig_custom_stuff.gui.IMyGuiScreen;
import net.minecraft.client.gui.Gui;
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
public class GuiConfigureBlockState extends GuiScreen implements IMyGuiScreen {

	private final IMyGuiScreen parent;
	private GuiButton doneButton;
	private final @Nullable BlockStateEntry blockStateEntry;
	private final Consumer<BlockStateEntry> onCompleted;

	private GuiSelectBlockModelButton selectBlockModelButton;
	private GuiDropDownButton modelTypeBtn;

	public GuiConfigureBlockState(IMyGuiScreen parent, @Nullable BlockStateEntry blockStateEntry, Consumer<BlockStateEntry> onCompleted) {
		this.parent = parent;
		this.blockStateEntry = blockStateEntry;
		this.onCompleted = onCompleted;
	}

	@Override
	public void initGui() {
		Keyboard.enableRepeatEvents(true);
		doneButton = addButton(new GuiButton(0, width / 2 - 155, height - 28, 150, 20, I18n.format("gui.done")));
		addButton(new GuiButton(1, width / 2 + 5, height - 28, 150, 20, I18n.format("gui.cancel")));

		BlockStateType type;
		if (modelTypeBtn != null) {
			type = BlockStateType.values()[modelTypeBtn.getSelectedButtonIndex()];
		} else if (blockStateEntry != null) {
			type = blockStateEntry.blockstateType;
		} else {
			type = BlockStateType.SIMPLE;
		}
		modelTypeBtn = addButton(new GuiDropDownButton(2, 140, 30, 160, 20, ""));
		if (selectBlockModelButton != null)
			selectBlockModelButton = addButton(new GuiSelectBlockModelButton(this, 3, 140, 60, 80, 80, selectBlockModelButton));
		else
			selectBlockModelButton = addButton(new GuiSelectBlockModelButton(this, 3, 140, 60, 80, 80, blockStateEntry));

		BlockStateType[] values = BlockStateType.values();
		for (int i = 0; i < values.length; i++) {
			BlockStateType value = values[i];
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
		return new BlockStateEntry(getBlockStateType(), selectBlockModelButton.blockModelGroupId, selectBlockModelButton.rotationX, selectBlockModelButton.rotationY, selectBlockModelButton.uvlock, ImmutableMap.of());
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
				selectBlockModelButton.onPressed(0);
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
		checkValid();
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		if (modelTypeBtn.isExtended()) {
			modelTypeBtn.postMouseClicked(mc, mouseX, mouseY);
			selectBlockModelButton.setBlockModelGroupType(getBlockStateType().getModelGroupType());
			checkValid();
			return;
		}
		super.mouseClicked(mouseX, mouseY, mouseButton);
		if (selectBlockModelButton.mousePressed(mc, mouseX, mouseY) && mouseButton == 1)
			selectBlockModelButton.onPressed(1);
		modelTypeBtn.mousePressed(mc, mouseX, mouseY);
		modelTypeBtn.postMouseClicked(mc, mouseX, mouseY);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		drawCenteredString(fontRenderer, I18n.format("gui.ingame_custom_stuff.configure_block_state.title"), width / 2, 20, 0xffffffff);

		super.drawScreen(mouseX, mouseY, partialTicks);
		drawString(fontRenderer, I18n.format("gui.ingame_custom_stuff.configure_block_state.label.blockstate_type"), 20, 30 + 7, 0xFFA0A0A0);
		drawString(fontRenderer, I18n.format("gui.ingame_custom_stuff.configure_block_state.label.model_id"), 20, 60 + 7, 0xFFA0A0A0);
		if (modelTypeBtn.isExtended())
			Gui.drawRect(0, 0, width, height, 0x80000000);
		modelTypeBtn.drawButton(mc, mouseX, mouseY, partialTicks);
		selectBlockModelButton.postDraw(mc, mouseX, mouseY, partialTicks);
	}

	@Override
	public void redisplay() {
		mc.displayGuiScreen(this);
	}


	private void checkValid() {
		doneButton.enabled = selectBlockModelButton.blockModelGroupId != null;
	}

	private BlockStateType getBlockStateType() {
		return BlockStateType.values()[modelTypeBtn.getSelectedButtonIndex()];
	}

	private static String getString(BlockStateType blockstateType) {
		return blockstateType.getString();
	}
}