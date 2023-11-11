package kpan.ig_custom_stuff.gui.texture;

import kpan.ig_custom_stuff.ModTagsGenerated;
import kpan.ig_custom_stuff.gui.IMyGuiScreen;
import kpan.ig_custom_stuff.gui.texture.GuiTextureList.EnumSelectedTextureList;
import kpan.ig_custom_stuff.network.MyPacketHandler;
import kpan.ig_custom_stuff.network.client.MessageDeleteTexturesToServer;
import kpan.ig_custom_stuff.resource.DynamicResourceLoader;
import kpan.ig_custom_stuff.resource.DynamicResourceManager.ClientCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

@SideOnly(Side.CLIENT)
public class GuiTextureMenu extends GuiScreen implements IMyGuiScreen {

	private final IMyGuiScreen parent;
	private GuiButton addTexture;
	private GuiButton modBlock;
	private GuiButton modItem;
	private GuiButton backBtn;
	private EnumSelectedTextureList selectedTextureList;
	private GuiTextureList textureList;
	private GuiTextField searchField;
	private GuiButton deleteTextureButton;
	private GuiButton replaceTextureButton;
	private int infoLeft;
	private int infoWidth;

	public GuiTextureMenu(IMyGuiScreen parent) {
		this.parent = parent;
		selectedTextureList = EnumSelectedTextureList.NOT_SELECTED;
	}

	@Override
	public void initGui() {
		infoLeft = (int) (width * 0.8);
		infoWidth = width - infoLeft;
		addButton(new GuiButton(0, width / 2 - 200 / 2, height - 28, 200, 20, I18n.format("gui.done")));
		addTexture = addButton(new GuiButton(1, 20, height - 28, 90, 20, I18n.format("gui.add")));
		searchField = new GuiTextField(0, mc.fontRenderer, 200, 2, infoLeft - 200 - 4, 16);

		deleteTextureButton = addButton(new GuiButton(10, infoLeft, infoWidth + 40, infoWidth, 20, I18n.format("gui.delete")));
		replaceTextureButton = addButton(new GuiButton(11, infoLeft, infoWidth + 80, infoWidth, 20, I18n.format("gui.replace")));
		textureList = new GuiTextureList(mc, infoLeft, height, 20, height - 30, this, this::getTextures);

		backBtn = addButton(new GuiButton(20, 10, 0, 80, 20, I18n.format("gui.back")));
		modBlock = addButton(new GuiButton(21, width / 2 - 150 / 2, 30, 150, 20, I18n.format("gui.ingame_custom_stuff.texture_menu.button_label.custom_blocks")));
		modItem = addButton(new GuiButton(22, width / 2 - 150 / 2, 60, 150, 20, I18n.format("gui.ingame_custom_stuff.texture_menu.button_label.custom_items")));

		deleteTextureButton.enabled = false;
		replaceTextureButton.enabled = false;
		updateState();
	}

	public void refreshList() {
		textureList.refreshList();
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
		if (selectedTextureList != EnumSelectedTextureList.NOT_SELECTED)
			textureList.applyVisiblePredicate(e -> StringUtils.containsIgnoreCase(e.textureId.toString(), searchField.getText()));
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		if (button.enabled) {
			switch (button.id) {
				case 0 -> parent.redisplay();
				case 1 -> mc.displayGuiScreen(GuiAddEditTexture.add(this, selectedTextureList == EnumSelectedTextureList.MOD_ITEM));
				case 10 -> {
					mc.displayGuiScreen(new GuiYesNo((result, id) -> {
						if (result)
							MyPacketHandler.sendToServer(new MessageDeleteTexturesToServer(Collections.singletonList(textureList.getSelectedTextureId())));
						redisplay();
					}, I18n.format("gui.ingame_custom_stuff.texture_menu.delete_texture", textureList.getSelectedTextureId()), I18n.format("gui.ingame_custom_stuff.warn.deleting_message"), 0));
				}
				case 11 ->
						mc.displayGuiScreen(GuiAddEditTexture.edit(this, textureList.getSelectedTextureId().toString(), selectedTextureList == EnumSelectedTextureList.MOD_ITEM));
				case 20 -> {
					selectedTextureList = EnumSelectedTextureList.NOT_SELECTED;
					updateState();
				}
				case 21 -> {
					selectedTextureList = EnumSelectedTextureList.MOD_BLOCK;
					updateState();
				}
				case 22 -> {
					selectedTextureList = EnumSelectedTextureList.MOD_ITEM;
					updateState();
				}
			}
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		textureList.drawScreen(mouseX, mouseY, partialTicks);
		drawCenteredString(fontRenderer, I18n.format("gui.ingame_custom_stuff.texture_menu.title"), 145, 8 - 4, 0xffffff);
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
			int y = w + 1;
			fontRenderer.drawString(I18n.format("gui.ingame_custom_stuff.texture_menu.texture_size", sprite.getIconWidth(), sprite.getIconHeight()), l + 1, y, -1);
			y += 10;
			if (sprite.getFrameCount() != 1) {
				fontRenderer.drawString(I18n.format("gui.ingame_custom_stuff.texture_menu.texture_animation_frames", sprite.getFrameCount()), l + 1, y, -1);
				y += 10;
			}
		}
		super.drawScreen(mouseX, mouseY, partialTicks);
		searchField.drawTextBox();
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		textureList.mouseClicked(mouseX, mouseY, mouseButton);
		searchField.mouseClicked(mouseX, mouseY, mouseButton);
		deleteTextureButton.enabled = textureList.getSelectedTextureId() != null;
		replaceTextureButton.enabled = textureList.getSelectedTextureId() != null;
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
				return ClientCache.INSTANCE.blockTextureIds.keySet();
			}
			case MOD_ITEM -> {
				return ClientCache.INSTANCE.itemTextureIds.keySet();
			}
			default -> throw new AssertionError();
		}
	}

	private void updateState() {
		switch (selectedTextureList) {
			case NOT_SELECTED -> {
				backBtn.visible = false;
				modBlock.visible = true;
				modItem.visible = true;
				textureList.visible = false;
				textureList.refreshList();
				searchField.setEnabled(false);
				textureList.applyVisiblePredicate(null);
				addTexture.enabled = false;
			}
			case MOD_BLOCK -> {
				backBtn.visible = true;
				modBlock.visible = false;
				modItem.visible = false;
				textureList.visible = true;
				textureList.refreshList();
				searchField.setEnabled(true);
				textureList.applyVisiblePredicate(e -> e.textureId.toString().contains(searchField.getText()));
				addTexture.enabled = true;
			}
			case MOD_ITEM -> {
				backBtn.visible = true;
				modBlock.visible = false;
				modItem.visible = false;
				textureList.visible = true;
				textureList.refreshList();
				searchField.setEnabled(true);
				textureList.applyVisiblePredicate(e -> e.textureId.toString().contains(searchField.getText()));
				addTexture.enabled = true;
			}
		}
	}
}