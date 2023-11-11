package kpan.ig_custom_stuff.gui.block;

import kpan.ig_custom_stuff.block.BlockPropertyEntry;
import kpan.ig_custom_stuff.block.BlockPropertyEntryBuilder;
import kpan.ig_custom_stuff.gui.IMyGuiScreen;
import kpan.ig_custom_stuff.gui.MyGuiList;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.Arrays;
import java.util.function.Consumer;

@SideOnly(Side.CLIENT)
public class GuiEditBlockProperty extends GuiScreen implements IMyGuiScreen {

	private final IMyGuiScreen parent;
	private final BlockPropertyEntryBuilder builder;
	private final Consumer<BlockPropertyEntry> onCompleted;
	private GuiButton doneButton;
	private MyGuiList guiList;

	public GuiEditBlockProperty(IMyGuiScreen parent, BlockPropertyEntry blockPropertyEntry, Consumer<BlockPropertyEntry> onCompleted) {
		this.parent = parent;
		builder = new BlockPropertyEntryBuilder(blockPropertyEntry);
		this.onCompleted = onCompleted;
	}

	@Override
	public void initGui() {
		Keyboard.enableRepeatEvents(true);
		doneButton = addButton(new GuiButton(0, width / 2 - 155, height - 28, 150, 20, I18n.format("gui.done")));
		addButton(new GuiButton(1, width / 2 + 5, height - 28, 150, 20, I18n.format("gui.cancel")));


		guiList = new MyGuiList(this, mc, width, height - 70, 30);
		guiList.addFloatButton("gui.ingame_custom_stuff.edit_block_property.hardness", builder.hardness, 0, Float.POSITIVE_INFINITY, builder::setHardness);
		guiList.addFloatButton("gui.ingame_custom_stuff.edit_block_property.resistance", builder.resistance, 0, Float.POSITIVE_INFINITY, builder::setResistance);
		guiList.addValuesButton("gui.ingame_custom_stuff.edit_block_property.soundType", builder.soundType, BlockPropertyEntry::getTranslationKey, BlockPropertyEntry.VANILLA_SOUND_TYPE_LIST, builder::setSoundType);
		guiList.addValuesButton("gui.ingame_custom_stuff.edit_block_property.creativeTab", builder.creativeTab, BlockPropertyEntry::getTranslationKey, BlockPropertyEntry.ALL_CREATIVE_TAB_LIST, builder::setCreativeTab);
		guiList.addValuesButton("gui.ingame_custom_stuff.edit_block_property.material", builder.material, BlockPropertyEntry::getTranslationKey, BlockPropertyEntry.VANILLA_MATERIAL_LIST, builder::setMaterial);
		guiList.addValuesButton("gui.ingame_custom_stuff.edit_block_property.renderingType", builder.isFullOpaqueCube, b -> b ? "gui.ingame_custom_stuff.edit_block_property.renderingType.dirt" : "gui.ingame_custom_stuff.edit_block_property.renderingType.glass", Arrays.asList(true, false), builder::setIsFullOpaqueCube);
		guiList.initGui();

		checkValid();
	}

	@Override
	public void onGuiClosed() {
		Keyboard.enableRepeatEvents(false);
	}

	public BlockPropertyEntry getPropertyEntry() {
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
		drawCenteredString(fontRenderer, I18n.format("gui.ingame_custom_stuff.edit_block_property.title"), width / 2, 20, 0xffffffff);
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