package kpan.ig_custom_stuff.gui.texture;

import kpan.ig_custom_stuff.ModTagsGenerated;
import kpan.ig_custom_stuff.gui.IMyGuiScreen;
import kpan.ig_custom_stuff.gui.texture.GuiTextureList.EnumSelectedTextureList;
import kpan.ig_custom_stuff.resource.DynamicResourceLoader;
import kpan.ig_custom_stuff.resource.DynamicResourceManager.ClientCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@SideOnly(Side.CLIENT)
public class GuiSelectTexture extends GuiScreen implements IMyGuiScreen {

	private final Consumer<@Nullable ResourceLocation> textureIdConsumer;
	private GuiButton okBtn;
	private GuiButton vanillaBlock;
	private GuiButton vanillaItem;
	private GuiButton modBlock;
	private GuiButton modItem;
	private GuiButton backBtn;
	private EnumSelectedTextureList selectedTextureList;
	private GuiTextureList textureList;
	private GuiTextField searchField;
	private int infoLeft;
	private int infoWidth;

	public GuiSelectTexture(Consumer<@Nullable ResourceLocation> textureIdConsumer) {
		this.textureIdConsumer = textureIdConsumer;
	}

	@Override
	public void initGui() {
		selectedTextureList = EnumSelectedTextureList.NOT_SELECTED;
		infoLeft = (int) (width * 0.8);
		infoWidth = width - infoLeft;
		okBtn = addButton(new GuiButton(0, width / 2 - 4 - 150, height - 28, 150, 20, I18n.format("gui.done")));
		addButton(new GuiButton(1, width / 2 + 4, height - 28, 150, 20, I18n.format("gui.cancel")));
		backBtn = addButton(new GuiButton(2, 10, 0, 80, 20, I18n.format("gui.back")));
		searchField = new GuiTextField(0, mc.fontRenderer, 200, 2, infoLeft - 200 - 4, 16);

		vanillaBlock = addButton(new GuiButton(10, width / 2 - 150 / 2, 30, 150, 20, I18n.format("gui.ingame_custom_stuff.select_texture.button_label.minecraft_blocks")));
		vanillaItem = addButton(new GuiButton(11, width / 2 - 150 / 2, 60, 150, 20, I18n.format("gui.ingame_custom_stuff.select_texture.button_label.minecraft_items")));
		modBlock = addButton(new GuiButton(12, width / 2 - 150 / 2, 90, 150, 20, I18n.format("gui.ingame_custom_stuff.select_texture.button_label.custom_blocks")));
		modItem = addButton(new GuiButton(13, width / 2 - 150 / 2, 120, 150, 20, I18n.format("gui.ingame_custom_stuff.select_texture.button_label.custom_items")));

		textureList = new GuiTextureList(mc, infoLeft, height, 20, height - 30, this, this::getTextures);

		okBtn.enabled = false;
		backBtn.visible = false;
		textureList.visible = false;
		searchField.setEnabled(false);
		textureList.applyVisiblePredicate(null);
	}


	@Override
	protected void keyTyped(char typedChar, int keyCode) {
		if (keyCode == Keyboard.KEY_ESCAPE) {
			textureIdConsumer.accept(null);
		}
		searchField.textboxKeyTyped(typedChar, keyCode);
		if (selectedTextureList != EnumSelectedTextureList.NOT_SELECTED)
			textureList.applyVisiblePredicate(e -> StringUtils.containsIgnoreCase(e.textureId.toString(), searchField.getText()));
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		if (button.enabled) {
			switch (button.id) {
				case 0 -> {
					textureIdConsumer.accept(textureList.getSelectedTextureId());
				}
				case 1 -> textureIdConsumer.accept(null);
				case 2 -> {
					backBtn.visible = false;
					vanillaBlock.visible = true;
					vanillaItem.visible = true;
					modBlock.visible = true;
					modItem.visible = true;
					textureList.visible = false;
					selectedTextureList = EnumSelectedTextureList.NOT_SELECTED;
					textureList.refreshList();
					searchField.setEnabled(false);
					textureList.applyVisiblePredicate(null);
				}
				case 10 -> {
					backBtn.visible = true;
					vanillaBlock.visible = false;
					vanillaItem.visible = false;
					modBlock.visible = false;
					modItem.visible = false;
					textureList.visible = true;
					selectedTextureList = EnumSelectedTextureList.VANILLA_BLOCK;
					textureList.refreshList();
					searchField.setEnabled(true);
					textureList.applyVisiblePredicate(e -> e.textureId.toString().contains(searchField.getText()));
				}
				case 11 -> {
					backBtn.visible = true;
					vanillaBlock.visible = false;
					vanillaItem.visible = false;
					modBlock.visible = false;
					modItem.visible = false;
					textureList.visible = true;
					selectedTextureList = EnumSelectedTextureList.VANILLA_ITEM;
					textureList.refreshList();
					searchField.setEnabled(true);
					textureList.applyVisiblePredicate(e -> e.textureId.toString().contains(searchField.getText()));
				}
				case 12 -> {
					backBtn.visible = true;
					vanillaBlock.visible = false;
					vanillaItem.visible = false;
					modBlock.visible = false;
					modItem.visible = false;
					textureList.visible = true;
					selectedTextureList = EnumSelectedTextureList.MOD_BLOCK;
					textureList.refreshList();
					searchField.setEnabled(true);
					textureList.applyVisiblePredicate(e -> e.textureId.toString().contains(searchField.getText()));
				}
				case 13 -> {
					backBtn.visible = true;
					vanillaBlock.visible = false;
					vanillaItem.visible = false;
					modBlock.visible = false;
					modItem.visible = false;
					textureList.visible = true;
					selectedTextureList = EnumSelectedTextureList.MOD_ITEM;
					textureList.refreshList();
					searchField.setEnabled(true);
					textureList.applyVisiblePredicate(e -> e.textureId.toString().contains(searchField.getText()));
				}
			}
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		textureList.drawScreen(mouseX, mouseY, partialTicks);
		drawCenteredString(fontRenderer, I18n.format("gui.ingame_custom_stuff.select_texture.title"), 145, 8 - 4, 0xffffff);
		int w = infoWidth;
		int l = infoLeft;
		Gui.drawRect(l, 0, width, height, 0xFF000000);
		GlStateManager.color(1, 1, 1, 1);
		GlStateManager.enableBlend();
		Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation(ModTagsGenerated.MODID, "textures/gui/bg.png"));
		drawModalRectWithCustomSizedTexture(l, 0, 0, 0, w, w, 16, 16);
		ResourceLocation textureId = textureList.getSelectedTextureId();
		if (textureId != null) {
			TextureAtlasSprite sprite = mc.getTextureMapBlocks().getAtlasSprite(textureId.toString());
			GlStateManager.bindTexture(mc.getTextureMapBlocks().getGlTextureId());
			drawTexturedModalRect(infoLeft, 0, sprite, w, w);
		}
		super.drawScreen(mouseX, mouseY, partialTicks);
		searchField.drawTextBox();
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		textureList.mouseClicked(mouseX, mouseY, mouseButton);
		searchField.mouseClicked(mouseX, mouseY, mouseButton);
		okBtn.enabled = textureList.getSelectedTextureId() != null;
	}

	@Override
	protected void mouseReleased(int mouseX, int mouseY, int state) {
		super.mouseReleased(mouseX, mouseY, state);
		textureList.mouseReleased(mouseX, mouseY, state);
	}

	@Override
	public void handleMouseInput() throws IOException {
		textureList.handleMouseInput();
		super.handleMouseInput();
	}

	@Override
	public void updateScreen() {
		super.updateScreen();
		textureList.updateScreen();
		searchField.updateCursorCounter();
	}

	private Collection<ResourceLocation> getTextures() {
		switch (selectedTextureList) {
			case NOT_SELECTED -> {
				return Collections.emptyList();
			}
			case VANILLA_BLOCK -> {
				return DynamicResourceLoader.VANILLA_BLOCK_TEXTURES;
			}
			case VANILLA_ITEM -> {
				return DynamicResourceLoader.VANILLA_ITEM_TEXTURES;
			}
			case MOD_BLOCK -> {
				return ClientCache.INSTANCE.blockTextureIds.keySet().stream().sorted().collect(Collectors.toList());
			}
			case MOD_ITEM -> {
				return ClientCache.INSTANCE.itemTextureIds.keySet().stream().sorted().collect(Collectors.toList());
			}
			default -> throw new AssertionError();
		}
	}

}