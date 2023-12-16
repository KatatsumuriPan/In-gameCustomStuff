package kpan.ig_custom_stuff.gui.blockmodel;

import kpan.ig_custom_stuff.ModReference;
import kpan.ig_custom_stuff.block.TextureUV;
import kpan.ig_custom_stuff.block.model.BlockModelEntryBase;
import kpan.ig_custom_stuff.block.model.BlockModelEntryBase.ModelType;
import kpan.ig_custom_stuff.block.model.BlockModelEntryCage;
import kpan.ig_custom_stuff.block.model.BlockModelEntryNormal;
import kpan.ig_custom_stuff.block.model.BlockModelTextureEntry;
import kpan.ig_custom_stuff.gui.IMyGuiScreen;
import kpan.ig_custom_stuff.gui.texture.GuiSelectTexture;
import kpan.ig_custom_stuff.network.MyPacketHandler;
import kpan.ig_custom_stuff.network.client.MessageRegisterBlockModelsToServer;
import kpan.ig_custom_stuff.network.client.MessageReplaceBlockModelsToServer;
import kpan.ig_custom_stuff.resource.DynamicResourceLoader.TemporaryBlockModelLoader;
import kpan.ig_custom_stuff.resource.DynamicResourceManager;
import kpan.ig_custom_stuff.resource.DynamicResourceManager.ClientCache;
import kpan.ig_custom_stuff.resource.ids.BlockModelId;
import kpan.ig_custom_stuff.util.RenderUtil;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@SideOnly(Side.CLIENT)
public class GuiAddEditBlockModelNormal extends GuiScreen implements IMyGuiScreen {
	private static final int textureSize = 32;
	private static final int buttonTop = 100;
	private static final int viewLeft = 270;

	public static GuiAddEditBlockModelNormal add(IMyGuiScreen okScreen, IMyGuiScreen cancelScreen) {
		ResourceLocation dirt = new ResourceLocation("blocks/dirt");
		BlockModelTextureEntry[] faces = new BlockModelTextureEntry[6];
		BlockModelTextureEntry dirt_texture = new BlockModelTextureEntry(dirt, TextureUV.FULL, 0);
		Arrays.fill(faces, dirt_texture);
		return new GuiAddEditBlockModelNormal(okScreen, cancelScreen, true, new BlockModelEntryNormal(faces, dirt_texture));
	}
	public static GuiAddEditBlockModelNormal edit(IMyGuiScreen parent, BlockModelId modelId, BlockModelEntryBase blockModelEntry) {
		GuiAddEditBlockModelNormal gui = new GuiAddEditBlockModelNormal(parent, parent, false, blockModelEntry);
		gui.initModelId = modelId.namespace + ":" + modelId.path.substring("normal/".length());
		return gui;
	}

	private final IMyGuiScreen okScreen;
	private final IMyGuiScreen cancelScreen;
	private final boolean isAdd;
	private GuiButton createButton;
	private GuiButton isCageBtn;
	private String initModelId = "";
	private GuiTextField modelIdField;
	private @Nullable String modelIdError = null;
	private BlockModelEntryBase blockModelEntry;
	private IBakedModel modelCache;
	private int lastMouseX = -1;
	private int lastMouseY = -1;
	private float rotateYaw = 10 + 180;
	private float rotatePitch = 30;

	public GuiAddEditBlockModelNormal(IMyGuiScreen okScreen, IMyGuiScreen cancelScreen, boolean isAdd, BlockModelEntryBase blockModelEntry) {
		this.okScreen = okScreen;
		this.cancelScreen = cancelScreen;
		this.isAdd = isAdd;
		this.blockModelEntry = blockModelEntry;
		updateCache(blockModelEntry);
	}

	@Override
	public void initGui() {
		Keyboard.enableRepeatEvents(true);
		createButton = addButton(new GuiButton(0, width / 2 - 155, height - 28, 150, 20, I18n.format("gui.done")));
		addButton(new GuiButton(1, width / 2 + 5, height - 28, 150, 20, I18n.format("gui.cancel")));

		isCageBtn = addButton(new GuiButton(2, 100, 75, 80, 20, ""));
		modelIdField = new GuiTextField(100, fontRenderer, 100, 40, 200, 20);
		modelIdField.setMaxStringLength(32767);
		modelIdField.setText(initModelId);

		//up
		addButton(new GuiButton(10, 100 + textureSize, buttonTop, textureSize, textureSize, ""));
		//north
		addButton(new GuiButton(11, 100 - textureSize, buttonTop + textureSize, textureSize, textureSize, ""));
		//west
		addButton(new GuiButton(12, 100, buttonTop + textureSize, textureSize, textureSize, ""));
		//south
		addButton(new GuiButton(13, 100 + textureSize, buttonTop + textureSize, textureSize, textureSize, ""));
		//east
		addButton(new GuiButton(14, 100 + textureSize * 2, buttonTop + textureSize, textureSize, textureSize, ""));
		//down
		addButton(new GuiButton(15, 100 + textureSize, buttonTop + textureSize * 2, textureSize, textureSize, ""));
		//particle
		addButton(new GuiButton(16, (int) (100 + textureSize * 3.5), buttonTop, textureSize, textureSize, ""));

		//all
		addButton(new GuiButton(17, (int) (100 + textureSize * 1.5) + 20, buttonTop + textureSize * 2 + 6, 80, 20, I18n.format("gui.ingame_custom_stuff.addedit_block_model.button_label.all")));

		//side all
		addButton(new GuiButton(18, 40, buttonTop + textureSize * 2 + 6, 80, 20, I18n.format("gui.ingame_custom_stuff.addedit_block_model.button_label.all_sides")));

		modelIdField.setFocused(true);
		modelIdField.setEnabled(isAdd);
		checkValid();
		updateButtonText();
	}

	@Override
	public void onGuiClosed() {
		Keyboard.enableRepeatEvents(false);
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		switch (button.id) {
			case 0 -> {
				Map<BlockModelId, BlockModelEntryBase> map = new HashMap<>();
				map.put(getModelId(), blockModelEntry);
				if (isAdd)
					MyPacketHandler.sendToServer(new MessageRegisterBlockModelsToServer(map));
				else
					MyPacketHandler.sendToServer(new MessageReplaceBlockModelsToServer(map));
				okScreen.redisplay();
			}
			case 1 -> cancelScreen.redisplay();
			case 2 -> {
				ModelType nextModelType;
				switch (blockModelEntry.modelType) {
					case NORMAL -> {
						nextModelType = ModelType.CAGE;
						blockModelEntry = new BlockModelEntryCage(blockModelEntry.getTextures());
					}
					case CAGE -> {
						nextModelType = ModelType.NORMAL;
						blockModelEntry = new BlockModelEntryNormal(blockModelEntry.getTextures());
					}
					default -> throw new AssertionError();
				}
				updateCache(blockModelEntry);
				updateButtonText();
			}
			case 10, 11, 12, 13, 14, 15, 16, 17, 18 -> {
				mc.displayGuiScreen(new GuiSelectTexture(resourceLocation -> {
					if (resourceLocation != null) {
						switch (button.id) {
							case 10 -> blockModelEntry.replaceTextureId(EnumFacing.UP.getName2(), resourceLocation);
							case 11 -> blockModelEntry.replaceTextureId(EnumFacing.NORTH.getName2(), resourceLocation);
							case 12 -> blockModelEntry.replaceTextureId(EnumFacing.WEST.getName2(), resourceLocation);
							case 13 -> blockModelEntry.replaceTextureId(EnumFacing.SOUTH.getName2(), resourceLocation);
							case 14 -> blockModelEntry.replaceTextureId(EnumFacing.EAST.getName2(), resourceLocation);
							case 15 -> blockModelEntry.replaceTextureId(EnumFacing.DOWN.getName2(), resourceLocation);
							case 16 -> blockModelEntry.replaceTextureId("particle", resourceLocation);
							case 17 -> {
								for (String textureTag : blockModelEntry.getTextures().keySet()) {
									blockModelEntry.replaceTextureId(textureTag, resourceLocation);
								}
							}
							case 18 -> {
								for (EnumFacing facing : EnumFacing.HORIZONTALS) {
									blockModelEntry.replaceTextureId(facing.getName2(), resourceLocation);
								}
							}
						}
						updateCache(blockModelEntry);
					}
					redisplay();
				}));
			}
		}
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) {
		modelIdField.textboxKeyTyped(typedChar, keyCode);

		checkValid();
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		if (mouseButton == 1) {
			for (GuiButton guibutton : buttonList) {
				if (guibutton.mousePressed(mc, mouseX, mouseY)) {
					switch (guibutton.id) {
						case 10, 11, 12, 13, 14, 15, 17, 18 -> {
							BlockModelTextureEntry textureEntry;
							switch (guibutton.id) {
								case 10 -> textureEntry = blockModelEntry.getTexture(EnumFacing.UP.getName2());
								case 11 -> textureEntry = blockModelEntry.getTexture(EnumFacing.NORTH.getName2());
								case 12 -> textureEntry = blockModelEntry.getTexture(EnumFacing.WEST.getName2());
								case 13 -> textureEntry = blockModelEntry.getTexture(EnumFacing.SOUTH.getName2());
								case 14 -> textureEntry = blockModelEntry.getTexture(EnumFacing.EAST.getName2());
								case 15 -> textureEntry = blockModelEntry.getTexture(EnumFacing.DOWN.getName2());
								default -> textureEntry = null;
							}
							if (textureEntry != null) {
								mc.displayGuiScreen(new GuiEditBlockModelFace(this, textureEntry, blockModelFaceEntry -> {
									switch (guibutton.id) {
										case 10 -> blockModelEntry.setTexture(EnumFacing.UP.getName2(), blockModelFaceEntry);
										case 11 -> blockModelEntry.setTexture(EnumFacing.NORTH.getName2(), blockModelFaceEntry);
										case 12 -> blockModelEntry.setTexture(EnumFacing.WEST.getName2(), blockModelFaceEntry);
										case 13 -> blockModelEntry.setTexture(EnumFacing.SOUTH.getName2(), blockModelFaceEntry);
										case 14 -> blockModelEntry.setTexture(EnumFacing.EAST.getName2(), blockModelFaceEntry);
										case 15 -> blockModelEntry.setTexture(EnumFacing.DOWN.getName2(), blockModelFaceEntry);
									}
									updateCache(blockModelEntry);
								}));
							} else if (guibutton.id == 17) {
								mc.displayGuiScreen(new GuiEditBlockModelFace(this, blockModelEntry.getTexture(EnumFacing.UP.getName2()), blockModelFaceEntry -> {
									if (blockModelFaceEntry != null) {
										for (EnumFacing facing : EnumFacing.VALUES) {
											blockModelEntry.setTexture(facing.getName2(), with(blockModelEntry.getTexture(facing.getName2()), blockModelFaceEntry.uv, blockModelFaceEntry.rotation));
										}
										updateCache(blockModelEntry);
									}
								}));
							} else if (guibutton.id == 18) {
								mc.displayGuiScreen(new GuiEditBlockModelFace(this, blockModelEntry.getTexture(EnumFacing.NORTH.getName2()), blockModelFaceEntry -> {
									if (blockModelFaceEntry != null) {
										for (EnumFacing facing : EnumFacing.HORIZONTALS) {
											blockModelEntry.setTexture(facing.getName2(), with(blockModelEntry.getTexture(facing.getName2()), blockModelFaceEntry.uv, blockModelFaceEntry.rotation));
										}
										updateCache(blockModelEntry);
									}
								}));
							}
						}
					}
				}
			}
		}
		modelIdField.mouseClicked(mouseX, mouseY, mouseButton);
		if (mouseX < viewLeft || mouseX > width - 10 || mouseY < 80 || mouseY > height - 30)
			return;
		lastMouseX = mouseX;
		lastMouseY = mouseY;
	}

	@Override
	protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
		super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
		if (lastMouseX == -1)
			return;
		rotateYaw += (mouseX - lastMouseX);
		rotatePitch += (mouseY - lastMouseY);
		rotatePitch = MathHelper.clamp(rotatePitch, -90, 90);
		lastMouseX = mouseX;
		lastMouseY = mouseY;
	}

	@Override
	protected void mouseReleased(int mouseX, int mouseY, int state) {
		super.mouseReleased(mouseX, mouseY, state);
		lastMouseX = -1;
		lastMouseY = -1;
	}
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (isAdd)
			drawCenteredString(fontRenderer, I18n.format("gui.ingame_custom_stuff.addedit_block_model.title.add"), width / 2, 20, -1);
		else
			drawCenteredString(fontRenderer, I18n.format("gui.ingame_custom_stuff.addedit_block_model.title.edit"), width / 2, 20, -1);

		drawString(fontRenderer, I18n.format("gui.ingame_custom_stuff.addedit_block_model.label.block_model_id"), 20, 40 + 7, 0xFFA0A0A0);
		modelIdField.drawTextBox();
		if (modelIdError != null)
			drawString(fontRenderer, I18n.format(modelIdError), 100 + 4, 60 + 4, 0xFFFF2222);
		else if (isAdd)
			drawString(fontRenderer, I18n.format("gui.ingame_custom_stuff.addedit_block_model.info.real_model_id", getModelId().toString()), 100 + 4, 60 + 4, 0xFF22FF22);

		drawString(fontRenderer, I18n.format("gui.ingame_custom_stuff.addedit_block_model.label.is_cage"), 20, 75 + 7, 0xFFA0A0A0);

		drawString(fontRenderer, I18n.format("gui.ingame_custom_stuff.addedit_block_model.label.textures"), 20, 110, 0xFFA0A0A0);
		drawCenteredString(fontRenderer, I18n.format("gui.ingame_custom_stuff.addedit_block_model.label.particle"), (int) (100 + textureSize * 4), buttonTop - 10, 0xFFA0A0A0);


		for (int i = 0; i < EnumFacing.VALUES.length; i++) {

			int x;
			int y = buttonTop;
			switch (EnumFacing.VALUES[i]) {
				case UP -> {
					x = 100 + textureSize;
				}
				case DOWN -> {
					x = 100 + textureSize;
					y += textureSize * 2;
				}
				case NORTH -> {
					x = 100 - textureSize;
					y += textureSize;
				}
				case SOUTH -> {
					x = 100 + textureSize;
					y += textureSize;
				}
				case WEST -> {
					x = 100;
					y += textureSize;
				}
				case EAST -> {
					x = 100 + textureSize * 2;
					y += textureSize;
				}
				default -> throw new AssertionError();
			}

			BlockModelTextureEntry face = blockModelEntry.getTexture(EnumFacing.VALUES[i].getName2());
			TextureAtlasSprite sprite = mc.getTextureMapBlocks().getAtlasSprite(face.textureId.toString());
			drawTexture(x, y, sprite, face.uv, face.rotation);
		}
		{
			//particle
			int x = (int) (100 + textureSize * 3.5);
			int y = buttonTop;
			TextureAtlasSprite sprite = mc.getTextureMapBlocks().getAtlasSprite(blockModelEntry.getTexture("particle").textureId.toString());
			drawTexture(x, y, sprite, TextureUV.FULL, 0);
		}
		Gui.drawRect(viewLeft, 80, width - 10, height - 30, -1);
		int w = Math.min(width - 10 - 250, height - 30 - 80);
		RenderUtil.renderModel(viewLeft + (width - 10 - viewLeft) / 2 - w / 2, 80, w / 16f, rotateYaw, rotatePitch, modelCache);
		drawString(fontRenderer, "N", 100 - textureSize + 1, buttonTop + textureSize + 1, 0xFFFF2222);

		for (GuiButton guibutton : buttonList) {
			if (guibutton.mousePressed(mc, mouseX, mouseY)) {
				switch (guibutton.id) {
					case 10, 11, 12, 13, 14, 15, 17, 18 -> {
						drawHoveringText(Arrays.asList(I18n.format("gui.ingame_custom_stuff.addedit_block_model.edit_face_hovering_text1"), I18n.format("gui.ingame_custom_stuff.addedit_block_model.edit_face_hovering_text2")), mouseX, mouseY, fontRenderer);
					}
				}
			}
		}
	}

	@Override
	public void redisplay() {
		initModelId = modelIdField.getText();
		mc.displayGuiScreen(this);
	}


	private void checkValid() {
		if (isAdd) {
			if (modelIdField.getText().isEmpty()) {
				modelIdError = "gui.ingame_custom_stuff.error.path.empty";
			} else {
				BlockModelId modelId = getModelId();
				modelIdError = DynamicResourceManager.getBlockModelIdErrorMessage(modelId, false);
				if (ClientCache.INSTANCE.blockModelIds.containsKey(modelId))
					modelIdError = "gui.ingame_custom_stuff.error.id_already_exists";
			}
		}
		createButton.enabled =
				modelIdError == null
						&& blockModelEntry != null;
	}

	private void updateButtonText() {
		isCageBtn.displayString = blockModelEntry instanceof BlockModelEntryCage ? "true" : "false";
	}

	private void updateCache(BlockModelEntryBase blockModelEntry) {
		modelCache = TemporaryBlockModelLoader.loadModel(blockModelEntry.toJson());
	}

	private BlockModelId getModelId() {
		String modelName = modelIdField.getText();
		int index = modelName.indexOf(':');
		String namespace;
		String path;
		if (index >= 0) {
			namespace = modelName.substring(0, index);
			path = modelName.substring(index + 1);
		} else {
			namespace = ModReference.DEFAULT_NAMESPACE;
			path = modelName;
		}
		path = "normal/" + path;
		return new BlockModelId(namespace, path);
	}


	private void drawTexture(int x, int y, TextureAtlasSprite sprite, TextureUV uv, int rotation) {
		Gui.drawRect(x, y, x + textureSize, y + textureSize, -1);
		GlStateManager.bindTexture(mc.getTextureMapBlocks().getGlTextureId());
		RenderUtil.drawTexturedModalRect(x, y, zLevel, sprite, uv.minU, uv.minV, uv.maxU, uv.maxV, rotation, textureSize, textureSize);
	}

	private BlockModelTextureEntry with(BlockModelTextureEntry base, TextureUV uv, int rotation) {
		return new BlockModelTextureEntry(base.textureId, uv, rotation);
	}
}