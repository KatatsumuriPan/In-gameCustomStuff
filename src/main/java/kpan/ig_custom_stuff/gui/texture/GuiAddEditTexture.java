package kpan.ig_custom_stuff.gui.texture;

import com.google.gson.JsonSyntaxException;
import kpan.ig_custom_stuff.ModReference;
import kpan.ig_custom_stuff.gui.GuiSimpleOk;
import kpan.ig_custom_stuff.gui.IMyGuiScreen;
import kpan.ig_custom_stuff.network.MyPacketHandler;
import kpan.ig_custom_stuff.network.client.MessageRegisterTexturesToServer;
import kpan.ig_custom_stuff.network.client.MessageReplaceTexturesToServer;
import kpan.ig_custom_stuff.resource.DynamicResourceManager;
import kpan.ig_custom_stuff.resource.DynamicResourceManager.ClientCache;
import kpan.ig_custom_stuff.resource.TextureAnimationEntry;
import kpan.ig_custom_stuff.util.PngInfo;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.input.Keyboard;
import scala.Tuple2;

import java.awt.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

@SideOnly(Side.CLIENT)
public class GuiAddEditTexture extends GuiScreen implements IMyGuiScreen {

	public static GuiAddEditTexture add(IMyGuiScreen parent, boolean isItem) {
		return new GuiAddEditTexture(parent, true, isItem, "");
	}
	public static GuiAddEditTexture edit(IMyGuiScreen parent, String textureId, boolean isItem) {
		return new GuiAddEditTexture(parent, false, isItem, textureId);
	}

	private final IMyGuiScreen parent;
	private final boolean isAdd;
	private final boolean isItem;
	private String initTextureId;
	private GuiButton createButton;
	private GuiTextField textureIdField = null;
	private GuiTextField filePathField = null;
	private @Nullable String textureIdError = null;
	private @Nullable String filePathError = null;

	private GuiAddEditTexture(IMyGuiScreen parent, boolean isAdd, boolean isItem, String initTextureId) {
		this.parent = parent;
		this.isAdd = isAdd;
		this.isItem = isItem;
		this.initTextureId = initTextureId;
	}

	@Override
	public void initGui() {
		Keyboard.enableRepeatEvents(true);
		createButton = addButton(new GuiButton(0, width / 2 - 155, height - 28, 150, 20, I18n.format("gui.done")));
		addButton(new GuiButton(1, width / 2 + 5, height - 28, 150, 20, I18n.format("gui.cancel")));
		textureIdField = new GuiTextField(2, fontRenderer, width / 2 - 100, 60, 200, 20);
		textureIdField.setMaxStringLength(32767);
		textureIdField.setText(initTextureId);
		String filePathText = filePathField != null ? filePathField.getText() : "";
		filePathField = new GuiTextField(3, fontRenderer, width / 2 - 100, 120, 200, 20);
		filePathField.setMaxStringLength(32767);
		filePathField.setText(filePathText);

		addButton(new GuiButton(4, width / 2 + 110, 120, 80, 20, I18n.format("gui.browse...")));

		textureIdField.setFocused(true);
		textureIdField.setEnabled(isAdd);

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
				Path path = Paths.get(filePathField.getText());
				byte[] data;
				try {
					data = Files.readAllBytes(path);
				} catch (IOException e) {
					mc.displayGuiScreen(new GuiSimpleOk(this::redisplay, "gui.ingame_custom_stuff.add_texture.error.io.title", I18n.format("gui.ingame_custom_stuff.add_texture.error.io.message", path).replace("\\n", "\n")));
					return;
				}
				PngInfo pngInfo;
				try {
					pngInfo = PngInfo.from(data);
				} catch (IOException e) {
					mc.displayGuiScreen(new GuiSimpleOk(this::redisplay, "gui.ingame_custom_stuff.add_texture.error.invalid_png.title", I18n.format("gui.ingame_custom_stuff.add_texture.error.invalid_png.message", path).replace("\\n", "\n")));
					return;
				}
				@Nullable TextureAnimationEntry animationEntry = null;
				if (Files.exists(Paths.get(path + ".mcmeta"))) {
					try {
						String json = FileUtils.readFileToString(Paths.get(path + ".mcmeta").toFile(), StandardCharsets.UTF_8);
						animationEntry = TextureAnimationEntry.fromJson(json);
					} catch (IOException | JsonSyntaxException e) {
						mc.displayGuiScreen(new GuiSimpleOk(this::redisplay, "gui.ingame_custom_stuff.add_texture.error.invalid_animation.title", I18n.format("gui.ingame_custom_stuff.add_texture.error.invalid_animation.message", path).replace("\\n", "\n")));
						return;
					}
					if (animationEntry.getFrameCount() >= 1000) {
						mc.displayGuiScreen(new GuiSimpleOk(this::redisplay, "gui.ingame_custom_stuff.add_texture.error.too_many_animation_frames.title", I18n.format("gui.ingame_custom_stuff.add_texture.error.too_many_animation_frames.message", path).replace("\\n", "\n")));
						return;
					}
				}

				if (animationEntry != null) {
					if (pngInfo.pngHeight % pngInfo.pngWidth != 0) {
						mc.displayGuiScreen(new GuiSimpleOk(this::redisplay, "gui.ingame_custom_stuff.add_texture.error.invalid_animation_aspect.title", I18n.format("gui.ingame_custom_stuff.add_texture.error.invalid_animation_aspect.message", path).replace("\\n", "\n")));
						return;
					}
				} else {
					if (pngInfo.pngWidth != pngInfo.pngHeight) {
						mc.displayGuiScreen(new GuiSimpleOk(this::redisplay, "gui.ingame_custom_stuff.add_texture.error.not16x.title", I18n.format("gui.ingame_custom_stuff.add_texture.error.not16x.message", path).replace("\\n", "\n")));
//					mc.displayGuiScreen(new GuiSimpleOk(this::redisplay, "gui.ingame_custom_stuff.add_texture.error.png_is_not_square.title", I18n.format("gui.ingame_custom_stuff.add_texture.error.png_is_not_square.message", path).replace("\\n", "\n")));
						return;
					}
				}
				if (pngInfo.pngWidth > 512) {
					mc.displayGuiScreen(new GuiSimpleOk(this::redisplay, "gui.ingame_custom_stuff.add_texture.error.not16x.title", I18n.format("gui.ingame_custom_stuff.add_texture.error.not16x.message", path).replace("\\n", "\n")));
//					mc.displayGuiScreen(new GuiSimpleOk(this::redisplay, "gui.ingame_custom_stuff.add_texture.error.size_too_large.title", I18n.format("gui.ingame_custom_stuff.add_texture.error.size_too_large.message", path).replace("\\n", "\n")));
					return;
				}
				if (pngInfo.pngWidth % 16 != 0) {
					mc.displayGuiScreen(new GuiSimpleOk(this::redisplay, "gui.ingame_custom_stuff.add_texture.error.not16x.title", I18n.format("gui.ingame_custom_stuff.add_texture.error.not16x.message", path).replace("\\n", "\n")));
					return;
				}
				if (data.length > 1024 * 1024) {
					mc.displayGuiScreen(new GuiSimpleOk(this::redisplay, "gui.ingame_custom_stuff.add_texture.error.file_too_large.title", I18n.format("gui.ingame_custom_stuff.add_texture.error.file_too_large.message", path).replace("\\n", "\n")));
					return;
				}

				String textureIdStr = textureIdField.getText();
				if (!textureIdStr.contains(":"))
					textureIdStr = ModReference.DEFAULT_NAMESPACE + ":" + textureIdStr;
				ResourceLocation textureId = new ResourceLocation(textureIdStr);
				if (isItem) {
					if (!textureId.getPath().contains("items/"))
						textureId = new ResourceLocation(textureId.getNamespace(), "items/" + textureId.getPath());
				} else {
					if (!textureId.getPath().contains("blocks/"))
						textureId = new ResourceLocation(textureId.getNamespace(), "blocks/" + textureId.getPath());
				}
				if (isAdd)
					MyPacketHandler.sendToServer(new MessageRegisterTexturesToServer(Collections.singletonMap(textureId, Tuple2.apply(data, animationEntry))));
				else
					MyPacketHandler.sendToServer(new MessageReplaceTexturesToServer(Collections.singletonMap(textureId, Tuple2.apply(data, animationEntry))));
				parent.redisplay();
			}
			case 1 -> parent.redisplay();
			case 4 -> {
				FileDialog dialog = new FileDialog((Frame) null, I18n.format("gui.ingame_custom_stuff.add_texture.select_file.title"), FileDialog.LOAD);
				dialog.setVisible(true);
				String file = dialog.getFile();
				if (file != null) {
					filePathField.setText(dialog.getDirectory() + file);
					checkValid();
				}
			}
		}
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) {
		textureIdField.textboxKeyTyped(typedChar, keyCode);
		filePathField.textboxKeyTyped(typedChar, keyCode);

		if (keyCode == Keyboard.KEY_TAB) {
			if (textureIdField.isFocused()) {
				filePathField.setFocused(true);
				textureIdField.setFocused(false);
			} else if (filePathField.isFocused()) {
				filePathField.setFocused(false);
				textureIdField.setFocused(true);
			}
		}
		checkValid();
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		//setFocusを正しく実行させるためにactionPerformedが後に実行されるような順
		textureIdField.mouseClicked(mouseX, mouseY, mouseButton);
		filePathField.mouseClicked(mouseX, mouseY, mouseButton);
		super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		if (isAdd) {
			if (isItem)
				drawCenteredString(fontRenderer, I18n.format("gui.ingame_custom_stuff.add_texture.title.add.item"), width / 2, 20, 0xffffffff);
			else
				drawCenteredString(fontRenderer, I18n.format("gui.ingame_custom_stuff.add_texture.title.add.block"), width / 2, 20, 0xffffffff);
		} else {
			if (isItem)
				drawCenteredString(fontRenderer, I18n.format("gui.ingame_custom_stuff.add_texture.title.edit.item"), width / 2, 20, 0xffffffff);
			else
				drawCenteredString(fontRenderer, I18n.format("gui.ingame_custom_stuff.add_texture.title.edit.block"), width / 2, 20, 0xffffffff);
		}

		drawString(fontRenderer, I18n.format("gui.ingame_custom_stuff.add_texture.label.texture_id"), width / 2 - 100, 40, 0xffa0a0a0);
		textureIdField.drawTextBox();
		if (textureIdError != null)
			drawString(fontRenderer, I18n.format(textureIdError), width / 2 - 100 + 8, 80 + 4, 0xFFFF2222);
		else if (!textureIdField.getText().contains(":"))
			drawString(fontRenderer, I18n.format("gui.ingame_custom_stuff.info.default_namespace_message", ModReference.DEFAULT_NAMESPACE), width / 2 - 100 + 8, 80 + 4, 0xFF22FF22);

		drawString(fontRenderer, I18n.format("gui.ingame_custom_stuff.add_texture.label.file_path"), width / 2 - 100, 100 + 7, 0xffa0a0a0);
		filePathField.drawTextBox();
		if (filePathError != null)
			drawString(fontRenderer, I18n.format(filePathError), width / 2 - 100 + 8, 140 + 4, 0xFFFF2222);

		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	public void redisplay() {
		initTextureId = textureIdField.getText();
		mc.displayGuiScreen(this);
	}


	private void checkValid() {
		if (isAdd) {
			if (textureIdField.getText().isEmpty()) {
				textureIdError = "gui.ingame_custom_stuff.error.path.empty";
			} else {
				String textureId = textureIdField.getText();
				if (!textureId.contains(":"))
					textureId = ModReference.DEFAULT_NAMESPACE + ":" + textureId;
				textureIdError = DynamicResourceManager.getResourceIdErrorMessage(textureId, false);
				if (isItem) {
					if (ClientCache.INSTANCE.itemTextureIds.containsKey(new ResourceLocation(textureId)))
						textureIdError = "gui.ingame_custom_stuff.error.id_already_exists";
				} else {
					if (ClientCache.INSTANCE.blockTextureIds.containsKey(new ResourceLocation(textureId)))
						textureIdError = "gui.ingame_custom_stuff.error.id_already_exists";
				}
			}
		}
		filePathError = DynamicResourceManager.isValidFilePath(filePathField.getText()) ? null : "gui.ingame_custom_stuff.error.path.invalid";
		if (filePathError == null)
			filePathError = filePathField.getText().endsWith(".png") ? null : "gui.ingame_custom_stuff.error.png_only";

		createButton.enabled = textureIdError == null && filePathError == null;

	}
}
