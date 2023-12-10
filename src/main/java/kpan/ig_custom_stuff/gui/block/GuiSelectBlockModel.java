package kpan.ig_custom_stuff.gui.block;

import kpan.ig_custom_stuff.gui.IMyGuiScreen;
import kpan.ig_custom_stuff.gui.blockmodel.GuiBlockModelList;
import kpan.ig_custom_stuff.resource.DynamicResourceLoader.SingleBlockModelLoader;
import kpan.ig_custom_stuff.resource.DynamicResourceManager.ClientCache;
import kpan.ig_custom_stuff.util.RenderUtil;
import kpan.ig_custom_stuff.util.handlers.ClientEventHandler;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@SideOnly(Side.CLIENT)
public class GuiSelectBlockModel extends GuiScreen implements IMyGuiScreen {

	private final Consumer<@Nullable ResourceLocation> modelIdConsumer;
	private GuiButton okBtn;
	private GuiBlockModelList blockModelList;
	private GuiTextField searchField;
	private int infoLeft;
	private int infoWidth;

	public GuiSelectBlockModel(Consumer<@Nullable ResourceLocation> modelIdConsumer) {
		this.modelIdConsumer = modelIdConsumer;
	}

	@Override
	public void initGui() {
		infoLeft = (int) (width * 0.8);
		infoWidth = width - infoLeft;
		okBtn = addButton(new GuiButton(0, width / 2 - 4 - 150, height - 28, 150, 20, I18n.format("gui.done")));
		addButton(new GuiButton(1, width / 2 + 4, height - 28, 150, 20, I18n.format("gui.cancel")));
		searchField = new GuiTextField(0, mc.fontRenderer, 200, 2, infoLeft - 200 - 4, 16);

		blockModelList = new GuiBlockModelList(mc, infoLeft, height, 20, height - 30, this::getModels);

		okBtn.enabled = false;
	}


	@Override
	protected void keyTyped(char typedChar, int keyCode) {
		if (keyCode == Keyboard.KEY_ESCAPE) {
			modelIdConsumer.accept(null);
		}
		searchField.textboxKeyTyped(typedChar, keyCode);
		blockModelList.applyVisiblePredicate(e -> StringUtils.containsIgnoreCase(e.modelId.toString(), searchField.getText()));
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		if (button.enabled) {
			switch (button.id) {
				case 0 -> {
					modelIdConsumer.accept(blockModelList.getSelectedModelId());
				}
				case 1 -> modelIdConsumer.accept(null);
			}
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		//blockModelListの後に描画するとCAGEモデルの向きがバグる
		//理由は不明
		{
			int w = infoWidth;
			int l = infoLeft;
			Gui.drawRect(l, 0, width, height, 0xFF000000);
			Gui.drawRect(l, 0, l + w, w, -1);
			ResourceLocation modelId = blockModelList.getSelectedModelId();
			if (modelId != null) {
				IBakedModel model = SingleBlockModelLoader.getModel(modelId);
				if (model != null)
					RenderUtil.renderModel(l, 0, w / 16f, ClientEventHandler.tick * 2, 30, model);
			}
		}
		blockModelList.drawScreen(mouseX, mouseY, partialTicks);
		drawCenteredString(fontRenderer, I18n.format("gui.ingame_custom_stuff.select_block_model.title"), 145, 8 - 4, 0xffffff);
		super.drawScreen(mouseX, mouseY, partialTicks);
		searchField.drawTextBox();
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		blockModelList.mouseClicked(mouseX, mouseY, mouseButton);
		searchField.mouseClicked(mouseX, mouseY, mouseButton);
		okBtn.enabled = blockModelList.getSelectedModelId() != null;
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

	private Collection<ResourceLocation> getModels() {
		return ClientCache.INSTANCE.blockModelIds.keySet().stream().sorted().collect(Collectors.toList());
	}

}