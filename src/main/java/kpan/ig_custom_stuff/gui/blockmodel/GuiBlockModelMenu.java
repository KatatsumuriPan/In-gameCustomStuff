package kpan.ig_custom_stuff.gui.blockmodel;

import kpan.ig_custom_stuff.block.BlockModelEntry;
import kpan.ig_custom_stuff.gui.IMyGuiScreen;
import kpan.ig_custom_stuff.network.MyPacketHandler;
import kpan.ig_custom_stuff.network.client.MessageDeleteBlockModelsToServer;
import kpan.ig_custom_stuff.resource.DynamicResourceLoader.SingleBlockModelLoader;
import kpan.ig_custom_stuff.resource.DynamicResourceManager.ClientCache;
import kpan.ig_custom_stuff.util.RenderUtil;
import kpan.ig_custom_stuff.util.handlers.ClientEventHandler;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.Collections;

@SideOnly(Side.CLIENT)
public class GuiBlockModelMenu extends GuiScreen implements IMyGuiScreen {

	private final IMyGuiScreen parent;
	private GuiBlockModelList blockModelList;
	private GuiTextField searchField;
	private GuiButton deleteItemButton;
	private GuiButton editItemButton;
	private int infoLeft;
	private int infoWidth;

	public GuiBlockModelMenu(IMyGuiScreen parent) {
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

		blockModelList = new GuiBlockModelList(mc, infoLeft, height, 20, height - 30, SingleBlockModelLoader::getModels);

		deleteItemButton.enabled = false;
		editItemButton.enabled = false;
		blockModelList.applyVisiblePredicate(null);
	}

	public void refreshList() {
		blockModelList.refreshList();
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
		blockModelList.applyVisiblePredicate(e -> StringUtils.containsIgnoreCase(e.modelId.toString(), searchField.getText()));
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		if (button.enabled) {
			switch (button.id) {
				case 0 -> parent.redisplay();
				case 1 -> mc.displayGuiScreen(GuiAddEditBlockModel.add(this));
				case 10 -> {
					mc.displayGuiScreen(new GuiYesNo((result, id) -> {
						if (result)
							MyPacketHandler.sendToServer(new MessageDeleteBlockModelsToServer(Collections.singletonList(blockModelList.getSelectedModelId())));
						redisplay();
					}, I18n.format("gui.ingame_custom_stuff.block_model_menu.delete_block_model", blockModelList.getSelectedModelId()), I18n.format("gui.ingame_custom_stuff.warn.deleting_message"), 0));
				}
				case 11 -> {
					ResourceLocation modelId = blockModelList.getSelectedModelId();
					BlockModelEntry model = ClientCache.INSTANCE.getBlockModel(modelId);
					if (model == null)
						throw new IllegalStateException();
					mc.displayGuiScreen(GuiAddEditBlockModel.edit(this, modelId, model));
				}
			}
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		blockModelList.drawScreen(mouseX, mouseY, partialTicks);
		drawCenteredString(fontRenderer, I18n.format("gui.ingame_custom_stuff.block_model_menu.title"), 145, 8 - 4, 16777215);
		int w = Math.min(height / 3, (int) (infoWidth * 0.8));
		int l = infoLeft + ((infoWidth - w) / 2);
		Gui.drawRect(infoLeft, 0, width, height, 0xFF000000);
		Gui.drawRect(l, 0, l + w, w, -1);
		ResourceLocation modelId = blockModelList.getSelectedModelId();
		if (modelId != null) {
			IBakedModel model = SingleBlockModelLoader.getModel(modelId);
			if (model != null)
				RenderUtil.renderModel(l, 0, w / 16f, ClientEventHandler.tick * 2, 30, model);
		}
		super.drawScreen(mouseX, mouseY, partialTicks);
		searchField.drawTextBox();
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		blockModelList.mouseClicked(mouseX, mouseY, mouseButton);
		searchField.mouseClicked(mouseX, mouseY, mouseButton);
		deleteItemButton.enabled = blockModelList.getSelectedModelId() != null;
		editItemButton.enabled = blockModelList.getSelectedModelId() != null;
	}

	@Override
	protected void mouseReleased(int mouseX, int mouseY, int state) {
		super.mouseReleased(mouseX, mouseY, state);
		blockModelList.mouseReleased(mouseX, mouseY, state);
	}

	@Override
	public void handleMouseInput() throws IOException {
		blockModelList.handleMouseInput();
		super.handleMouseInput();
	}

	@Override
	public void updateScreen() {
		super.updateScreen();
		blockModelList.updateScreen();
		searchField.updateCursorCounter();
	}

}