package kpan.ig_custom_stuff.gui.item;

import kpan.ig_custom_stuff.ModReference;
import kpan.ig_custom_stuff.gui.GuiComponentBase;
import kpan.ig_custom_stuff.gui.GuiDropDownButton;
import kpan.ig_custom_stuff.gui.IMyGuiScreen;
import kpan.ig_custom_stuff.gui.texture.GuiSelectTexture;
import kpan.ig_custom_stuff.item.model.ItemModelEntry;
import kpan.ig_custom_stuff.item.model.ItemModelEntry.ModelType;
import kpan.ig_custom_stuff.resource.DynamicResourceManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class GuiSelectItemModel extends GuiComponentBase {

	public final GuiDropDownButton modelTypeBtn;
	public final GuiTextField textureIdField;
	private final IMyGuiScreen owner;
	private final FontRenderer fontRenderer;
	private @Nullable String textureIdError = null;

	public GuiSelectItemModel(Minecraft mc, int x, int y, int width, int height, FontRenderer fontRenderer, IMyGuiScreen owner, @Nullable GuiSelectItemModel old) {
		this(mc, x, y, width, height, fontRenderer, owner, old != null ? old.getModelEntry() : null);
	}
	public GuiSelectItemModel(Minecraft mc, int x, int y, int width, int height, FontRenderer fontRenderer, IMyGuiScreen owner, @Nullable ItemModelEntry itemModelEntry) {
		super(mc, x, y, width, height);
		this.owner = owner;
		this.fontRenderer = fontRenderer;
		modelTypeBtn = addButton(new GuiDropDownButton(0, x + 100, y + 0, 200, 20, ""));
		textureIdField = new GuiTextField(10, fontRenderer, x + 100, y + 30, 200, 20);
		textureIdField.setMaxStringLength(32767);
		ModelType modelType;
		if (itemModelEntry != null) {
			textureIdField.setText(itemModelEntry.textureIds.iterator().next().toString());
			modelType = itemModelEntry.modelType;
		} else {
			textureIdField.setText("items/");
			modelType = ModelType.SIMPLE;
		}
		addButton(new GuiButton(1, x + 100 + 210, y + 30, 100, 20, I18n.format("gui.select")));

		ModelType[] values = ModelType.values();
		for (int i = 0; i < values.length; i++) {
			ModelType value = values[i];
			if (value == ModelType.CUSTOM)
				continue;
			modelTypeBtn.add(100, 20, getString(value));
			if (value == modelType)
				modelTypeBtn.setSelectedButtonIndex(i);
		}
		checkValid();
	}


	public ItemModelEntry getModelEntry() {
		String textureId = textureIdField.getText();
		if (!textureId.contains(":"))
			textureId = ModReference.DEFAULT_NAMESPACE + ":" + textureId;
		return ItemModelEntry.normalType(ModelType.values()[modelTypeBtn.getSelectedButtonIndex()], new ResourceLocation(textureId));
	}

	public boolean isValid() {
		return textureIdError == null;
	}

	public boolean isExtended() {
		return modelTypeBtn.isExtended();
	}

	@Override
	public void keyTyped(char typedChar, int keyCode) {
		if (modelTypeBtn.isExtended()) {
			modelTypeBtn.keyTyped(typedChar, keyCode);
			return;
		}
		super.keyTyped(typedChar, keyCode);
		textureIdField.textboxKeyTyped(typedChar, keyCode);
		checkValid();
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		drawCenteredString(fontRenderer, I18n.format("gui.ingame_custom_stuff.select_item_model.title"), width / 2, 8 - 4, 16777215);
		drawString(fontRenderer, I18n.format("gui.ingame_custom_stuff.select_item_model.label.model_type"), x + 20, y + 0 + 7, 0xFFA0A0A0);
		drawString(fontRenderer, I18n.format("gui.ingame_custom_stuff.select_item_model.label.texture_id"), x + 20, y + 30 + 7, 0xFFA0A0A0);
		textureIdField.drawTextBox();
		if (textureIdError != null)
			drawString(fontRenderer, I18n.format(textureIdError), x + 100 + 4, y + 50 + 4, 0xFFFF2222);
		else if (!textureIdField.getText().contains(":"))
			drawString(fontRenderer, I18n.format("gui.ingame_custom_stuff.info.default_namespace_message", ModReference.DEFAULT_NAMESPACE), x + 100 + 4, y + 50 + 4, 0xFF22FF22);
		if (modelTypeBtn.isExtended())
			Gui.drawRect(0, 0, ((GuiScreen) owner).width, ((GuiScreen) owner).height, 0x80000000);
		modelTypeBtn.drawButton(mc, mouseX, mouseY, partialTicks);
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		if (modelTypeBtn.isExtended()) {
			modelTypeBtn.postMouseClicked(mc, mouseX, mouseY);
			return;
		}
		super.mouseClicked(mouseX, mouseY, mouseButton);
		modelTypeBtn.mousePressed(mc, mouseX, mouseY);
		textureIdField.mouseClicked(mouseX, mouseY, mouseButton);
		modelTypeBtn.postMouseClicked(mc, mouseX, mouseY);
	}
	@Override
	protected void actionPerformed(GuiButton button, int mouseButton) {
		switch (button.id) {
			case 0 -> {
				if (mouseButton == 0 || mouseButton == 1)
					modelTypeBtn.onPressed();
			}
			case 1 -> {
				if (mouseButton == 0)
					mc.displayGuiScreen(new GuiSelectTexture(textureId -> {
						if (textureId != null)
							textureIdField.setText(textureId.toString());
						owner.redisplay();
					}));
			}
		}
	}


	private void checkValid() {
		String textureId = textureIdField.getText();
		if (!textureId.contains(":"))
			textureId = ModReference.DEFAULT_NAMESPACE + ":" + textureId;
		textureIdError = DynamicResourceManager.getResourceIdErrorMessage(textureId, true);
	}

	private static String getString(ModelType modelType) {
		return modelType.getString();
	}
}
