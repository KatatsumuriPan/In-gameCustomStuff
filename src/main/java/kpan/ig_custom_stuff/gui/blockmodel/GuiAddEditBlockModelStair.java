package kpan.ig_custom_stuff.gui.blockmodel;

import kpan.ig_custom_stuff.ModReference;
import kpan.ig_custom_stuff.block.TextureUV;
import kpan.ig_custom_stuff.block.model.BlockModelEntryBase;
import kpan.ig_custom_stuff.block.model.BlockModelEntryStair;
import kpan.ig_custom_stuff.block.model.BlockModelEntryStair.StairModelType;
import kpan.ig_custom_stuff.block.model.BlockModelTextureEntry;
import kpan.ig_custom_stuff.gui.IMyGuiScreen;
import kpan.ig_custom_stuff.gui.texture.GuiSelectTexture;
import kpan.ig_custom_stuff.network.MyPacketHandler;
import kpan.ig_custom_stuff.network.client.MessageRegisterBlockModelsToServer;
import kpan.ig_custom_stuff.network.client.MessageReplaceBlockModelsToServer;
import kpan.ig_custom_stuff.resource.DynamicResourceLoader.TemporaryBlockModelLoader;
import kpan.ig_custom_stuff.resource.DynamicResourceManager;
import kpan.ig_custom_stuff.resource.DynamicResourceManager.ClientCache;
import kpan.ig_custom_stuff.resource.ids.BlockModelGroupId;
import kpan.ig_custom_stuff.resource.ids.BlockModelGroupId.BlockModelGroupType;
import kpan.ig_custom_stuff.resource.ids.BlockModelId;
import kpan.ig_custom_stuff.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiButtonImage;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
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
import java.util.TreeMap;

@SideOnly(Side.CLIENT)
public class GuiAddEditBlockModelStair extends GuiScreen implements IMyGuiScreen {
	private static final ResourceLocation RESOURCE_PACKS_TEXTURE = new ResourceLocation("textures/gui/resource_packs.png");
	private static final int textureSize = 32;
	private static final int buttonTop = 100;
	private static final int viewLeft = 270;

	public static GuiAddEditBlockModelStair add(IMyGuiScreen okScreen, IMyGuiScreen cancelScreen) {
		ResourceLocation dirt = new ResourceLocation("blocks/dirt");
		BlockModelTextureEntry full = new BlockModelTextureEntry(dirt, TextureUV.FULL, 0);
		Map<String, BlockModelTextureEntry> textures = new TreeMap<>();
		textures.put("front", full);
		textures.put("top", full);
		textures.put("bottom", full);
		textures.put("side-left", full);
		textures.put("side-right", full);
		textures.put("particle", full);
		BlockModelEntryStair entryStraight = new BlockModelEntryStair(StairModelType.STRAIGHT, textures);
		entryStraight.setTexture("back", full);
		BlockModelEntryStair entryInner = new BlockModelEntryStair(StairModelType.INNER, textures);
		entryInner.setTexture("back", full);
		BlockModelEntryStair entryOuter = new BlockModelEntryStair(StairModelType.OUTER, textures);
		return new GuiAddEditBlockModelStair(okScreen, cancelScreen, true, entryStraight, entryInner, entryOuter);
	}
	public static GuiAddEditBlockModelStair edit(IMyGuiScreen parent, BlockModelGroupId modelGroupId) {
		Map<String, BlockModelId> map = modelGroupId.getBlockModelIds();
		BlockModelEntryStair entryStraight = (BlockModelEntryStair) ClientCache.INSTANCE.getBlockModel(map.get("straight"));
		BlockModelEntryStair entryInner = (BlockModelEntryStair) ClientCache.INSTANCE.getBlockModel(map.get("inner"));
		BlockModelEntryStair entryOuter = (BlockModelEntryStair) ClientCache.INSTANCE.getBlockModel(map.get("outer"));
		GuiAddEditBlockModelStair gui = new GuiAddEditBlockModelStair(parent, parent, false, entryStraight, entryInner, entryOuter);
		gui.initModelGroupId = modelGroupId.namespace + ":" + modelGroupId.path.substring("stair/".length());
		return gui;
	}

	private final IMyGuiScreen okScreen;
	private final IMyGuiScreen cancelScreen;
	private final boolean isAdd;
	private GuiButton createButton;
	private String initModelGroupId = "";
	private GuiTextField modelIdField;
	private @Nullable String modelIdError = null;
	private BlockModelEntryStair entryStraight;
	private BlockModelEntryStair entryInner;
	private BlockModelEntryStair entryOuter;
	private IBakedModel modelCache;
	private int lastMouseX = -1;
	private int lastMouseY = -1;
	private float rotateYaw = 10;
	private float rotatePitch = 30;
	private StairModelType renderingStairType = StairModelType.STRAIGHT;

	public GuiAddEditBlockModelStair(IMyGuiScreen okScreen, IMyGuiScreen cancelScreen, boolean isAdd, BlockModelEntryStair entryStraight, BlockModelEntryStair entryInner, BlockModelEntryStair entryOuter) {
		this.okScreen = okScreen;
		this.cancelScreen = cancelScreen;
		this.isAdd = isAdd;
		this.entryStraight = entryStraight;
		this.entryInner = entryInner;
		this.entryOuter = entryOuter;
		updateCache();
	}

	@Override
	public void initGui() {
		Keyboard.enableRepeatEvents(true);
		createButton = addButton(new GuiButton(0, width / 2 - 155, height - 28, 150, 20, I18n.format("gui.done")));
		addButton(new GuiButton(1, width / 2 + 5, height - 28, 150, 20, I18n.format("gui.cancel")));

		modelIdField = new GuiTextField(100, fontRenderer, 100, 40, 200, 20);
		modelIdField.setMaxStringLength(32767);
		modelIdField.setText(initModelGroupId);

		addButton(new GuiButtonImage(3, viewLeft, height - 30 - 22, 14, 22, 34, 5, 32, RESOURCE_PACKS_TEXTURE) {
			@Override
			public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
				GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
				super.drawButton(mc, mouseX, mouseY, partialTicks);
			}
		});
		addButton(new GuiButtonImage(4, width - 10 - 14, height - 30 - 22, 14, 22, 10, 5, 32, RESOURCE_PACKS_TEXTURE) {
			@Override
			public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
				GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
				super.drawButton(mc, mouseX, mouseY, partialTicks);
			}
		});

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
				BlockModelGroupId modelGroupId = getBlockModelGroupId();
				map.put(modelGroupId.getVariantId("straight"), entryStraight);
				map.put(modelGroupId.getVariantId("inner"), entryInner);
				map.put(modelGroupId.getVariantId("outer"), entryOuter);
				if (isAdd)
					MyPacketHandler.sendToServer(new MessageRegisterBlockModelsToServer(map));
				else
					MyPacketHandler.sendToServer(new MessageReplaceBlockModelsToServer(map));
				okScreen.redisplay();
			}
			case 1 -> cancelScreen.redisplay();
			case 3 -> {
				renderingStairType = StairModelType.values()[(renderingStairType.ordinal() + 2) % 3];
				updateCache();
			}
			case 4 -> {
				renderingStairType = StairModelType.values()[(renderingStairType.ordinal() + 1) % 3];
				updateCache();
			}
			case 10, 11, 12, 13, 14, 15, 16, 17, 18 -> {
				mc.displayGuiScreen(new GuiSelectTexture(resourceLocation -> {
					if (resourceLocation != null) {
						switch (button.id) {
							case 10 -> {
								entryStraight.replaceTextureId("top", resourceLocation);
								entryInner.replaceTextureId("top", resourceLocation);
								entryOuter.replaceTextureId("top", resourceLocation);
							}
							case 11 -> {
								entryStraight.replaceTextureId("back", resourceLocation);
								entryInner.replaceTextureId("back", resourceLocation);
							}
							case 12 -> {
								entryStraight.replaceTextureId("side-left", resourceLocation);
								entryInner.replaceTextureId("side-left", resourceLocation);
								entryOuter.replaceTextureId("side-left", resourceLocation);
							}
							case 13 -> {
								entryStraight.replaceTextureId("front", resourceLocation);
								entryInner.replaceTextureId("front", resourceLocation);
								entryOuter.replaceTextureId("front", resourceLocation);
							}
							case 14 -> {
								entryStraight.replaceTextureId("side-right", resourceLocation);
								entryInner.replaceTextureId("side-right", resourceLocation);
								entryOuter.replaceTextureId("side-right", resourceLocation);
							}
							case 15 -> {
								entryStraight.replaceTextureId("bottom", resourceLocation);
								entryInner.replaceTextureId("bottom", resourceLocation);
								entryOuter.replaceTextureId("bottom", resourceLocation);
							}
							case 16 -> {
								entryStraight.replaceTextureId("particle", resourceLocation);
								entryInner.replaceTextureId("particle", resourceLocation);
								entryOuter.replaceTextureId("particle", resourceLocation);
							}
							case 17 -> {
								for (String textureTag : entryStraight.getTextures().keySet()) {
									entryStraight.replaceTextureId(textureTag, resourceLocation);
									entryInner.replaceTextureId(textureTag, resourceLocation);
									if (!textureTag.equals("back"))
										entryOuter.replaceTextureId(textureTag, resourceLocation);
								}
							}
							case 18 -> {
								for (String textureTag : new String[]{"front", "back", "side-left", "side-right"}) {
									entryStraight.replaceTextureId(textureTag, resourceLocation);
									entryInner.replaceTextureId(textureTag, resourceLocation);
									if (!textureTag.equals("back"))
										entryOuter.replaceTextureId(textureTag, resourceLocation);
								}
							}
						}
						updateCache();
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
								case 10 -> textureEntry = entryStraight.getTexture("top");
								case 11 -> textureEntry = entryStraight.getTexture("back");
								case 12 -> textureEntry = entryStraight.getTexture("side-left");
								case 13 -> textureEntry = entryStraight.getTexture("front");
								case 14 -> textureEntry = entryStraight.getTexture("side-right");
								case 15 -> textureEntry = entryStraight.getTexture("bottom");
								default -> textureEntry = null;
							}
							if (textureEntry != null) {
								mc.displayGuiScreen(new GuiEditBlockModelFace(this, textureEntry, blockModelFaceEntry -> {
									if (blockModelFaceEntry != null) {
										switch (guibutton.id) {
											case 10 -> {
												entryStraight.setTexture("top", blockModelFaceEntry);
												entryInner.setTexture("top", blockModelFaceEntry);
												entryOuter.setTexture("top", blockModelFaceEntry);
											}
											case 11 -> {
												entryStraight.setTexture("back", blockModelFaceEntry);
												entryInner.setTexture("back", blockModelFaceEntry);
											}
											case 12 -> {
												entryStraight.setTexture("side-left", blockModelFaceEntry);
												entryInner.setTexture("side-left", blockModelFaceEntry);
												entryOuter.setTexture("side-left", blockModelFaceEntry);
											}
											case 13 -> {
												entryStraight.setTexture("front", blockModelFaceEntry);
												entryInner.setTexture("front", blockModelFaceEntry);
												entryOuter.setTexture("front", blockModelFaceEntry);
											}
											case 14 -> {
												entryStraight.setTexture("side-right", blockModelFaceEntry);
												entryInner.setTexture("side-right", blockModelFaceEntry);
												entryOuter.setTexture("side-right", blockModelFaceEntry);
											}
											case 15 -> {
												entryStraight.setTexture("bottom", blockModelFaceEntry);
												entryInner.setTexture("bottom", blockModelFaceEntry);
												entryOuter.setTexture("bottom", blockModelFaceEntry);
											}
										}
										updateCache();
									}
								}));
							} else if (guibutton.id == 17) {
								mc.displayGuiScreen(new GuiEditBlockModelFace(this, entryStraight.getTexture("top"), blockModelFaceEntry -> {
									if (blockModelFaceEntry != null) {
										for (String textureTag : entryStraight.getTextures().keySet()) {
											if (textureTag.equals("particle"))
												continue;
											entryStraight.setTexture(textureTag, with(entryStraight.getTexture(textureTag), blockModelFaceEntry.uv, blockModelFaceEntry.rotation));
											entryInner.setTexture(textureTag, with(entryInner.getTexture(textureTag), blockModelFaceEntry.uv, blockModelFaceEntry.rotation));
											if (!textureTag.equals("back"))
												entryOuter.setTexture(textureTag, with(entryOuter.getTexture(textureTag), blockModelFaceEntry.uv, blockModelFaceEntry.rotation));
										}
										updateCache();
									}
								}));
							} else if (guibutton.id == 18) {
								mc.displayGuiScreen(new GuiEditBlockModelFace(this, entryStraight.getTexture("front"), blockModelFaceEntry -> {
									if (blockModelFaceEntry != null) {
										for (String textureTag : new String[]{"front", "back", "side-left", "side-right"}) {
											entryStraight.setTexture(textureTag, with(entryStraight.getTexture(textureTag), blockModelFaceEntry.uv, blockModelFaceEntry.rotation));
											entryInner.setTexture(textureTag, with(entryInner.getTexture(textureTag), blockModelFaceEntry.uv, blockModelFaceEntry.rotation));
											if (!textureTag.equals("back"))
												entryOuter.setTexture(textureTag, with(entryOuter.getTexture(textureTag), blockModelFaceEntry.uv, blockModelFaceEntry.rotation));
										}
										updateCache();
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
		Gui.drawRect(viewLeft, 80, width - 10, height - 30, -1);
		int w = Math.min(width - 10 - 250, height - 30 - 80);
		RenderUtil.renderModel(viewLeft + (width - 10 - viewLeft) / 2 - w / 2, 80, w / 16f, rotateYaw, rotatePitch, modelCache);
		drawString(fontRenderer, "N", 100 - textureSize + 1, buttonTop + textureSize + 1, 0xFFFF2222);

		RenderHelper.disableStandardItemLighting();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (isAdd)
			drawCenteredString(fontRenderer, I18n.format("gui.ingame_custom_stuff.addedit_block_model.stair.title.add"), width / 2, 20, -1);
		else
			drawCenteredString(fontRenderer, I18n.format("gui.ingame_custom_stuff.addedit_block_model.stair.title.edit"), width / 2, 20, -1);

		drawString(fontRenderer, I18n.format("gui.ingame_custom_stuff.addedit_block_model.label.block_model_id"), 20, 40 + 7, 0xFFA0A0A0);
		modelIdField.drawTextBox();
		if (modelIdError != null)
			drawString(fontRenderer, I18n.format(modelIdError), 100 + 4, 60 + 4, 0xFFFF2222);
		else if (isAdd)
			drawString(fontRenderer, I18n.format("gui.ingame_custom_stuff.addedit_block_model.info.real_model_id", getBlockModelGroupId().toResourceLocation().toString()), 100 + 4, 60 + 4, 0xFF22FF22);

		drawString(fontRenderer, I18n.format("gui.ingame_custom_stuff.addedit_block_model.label.textures"), 20, 110, 0xFFA0A0A0);
		drawCenteredString(fontRenderer, I18n.format("gui.ingame_custom_stuff.addedit_block_model.label.particle"), (int) (100 + textureSize * 4), buttonTop - 10, 0xFFA0A0A0);

		{
			//top
			{
				int x = 100 + textureSize;
				int y = buttonTop;
				BlockModelTextureEntry face = entryStraight.getTexture("top");
				TextureAtlasSprite sprite = mc.getTextureMapBlocks().getAtlasSprite(face.textureId.toString());
				drawTexture(x, y, sprite, face.uv, face.rotation);
			}
			//bottom
			{
				int x = 100 + textureSize;
				int y = buttonTop + textureSize * 2;
				BlockModelTextureEntry face = entryStraight.getTexture("bottom");
				TextureAtlasSprite sprite = mc.getTextureMapBlocks().getAtlasSprite(face.textureId.toString());
				drawTexture(x, y, sprite, face.uv, face.rotation);
			}
			//back
			{
				int x = 100 - textureSize;
				int y = buttonTop + textureSize;
				BlockModelTextureEntry face = entryStraight.getTexture("back");
				TextureAtlasSprite sprite = mc.getTextureMapBlocks().getAtlasSprite(face.textureId.toString());
				drawTexture(x, y, sprite, face.uv, face.rotation);
			}
			//side-left
			{
				int x = 100;
				int y = buttonTop + textureSize;
				BlockModelTextureEntry face = entryStraight.getTexture("side-left");
				TextureAtlasSprite sprite = mc.getTextureMapBlocks().getAtlasSprite(face.textureId.toString());
				drawTexture(x, y, sprite, face.uv, face.rotation);
				Gui.drawRect(x + textureSize / 2, y, x + textureSize, y + textureSize / 2, 0x8000_0000);
			}
			//front
			{
				int x = 100 + textureSize;
				int y = buttonTop + textureSize;
				BlockModelTextureEntry face = entryStraight.getTexture("front");
				TextureAtlasSprite sprite = mc.getTextureMapBlocks().getAtlasSprite(face.textureId.toString());
				drawTexture(x, y, sprite, face.uv, face.rotation);
			}
			//side-right
			{
				int x = 100 + textureSize * 2;
				int y = buttonTop + textureSize;
				BlockModelTextureEntry face = entryStraight.getTexture("side-right");
				TextureAtlasSprite sprite = mc.getTextureMapBlocks().getAtlasSprite(face.textureId.toString());
				drawTexture(x, y, sprite, face.uv, face.rotation);
				Gui.drawRect(x, y, x + textureSize / 2, y + textureSize / 2, 0x8000_0000);
			}

			//particle
			{
				int x = (int) (100 + textureSize * 3.5);
				int y = buttonTop;
				BlockModelTextureEntry face = entryStraight.getTexture("particle");
				TextureAtlasSprite sprite = mc.getTextureMapBlocks().getAtlasSprite(face.textureId.toString());
				drawTexture(x, y, sprite, TextureUV.FULL, 0);
			}
		}

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
		initModelGroupId = modelIdField.getText();
		mc.displayGuiScreen(this);
	}


	private void checkValid() {
		if (isAdd) {
			if (modelIdField.getText().isEmpty()) {
				modelIdError = "gui.ingame_custom_stuff.error.path.empty";
			} else {
				BlockModelGroupId modelGroupId = getBlockModelGroupId();
				modelIdError = DynamicResourceManager.getBlockModelIdErrorMessage(modelGroupId, false);
				if (ClientCache.INSTANCE.blockModelIds.containsKey(modelGroupId.getRenderModelId()))
					modelIdError = "gui.ingame_custom_stuff.error.id_already_exists";
			}
		}
		createButton.enabled = modelIdError == null;
	}


	private BlockModelGroupId getBlockModelGroupId() {
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
		path = "stair/" + path;
		return new BlockModelGroupId(BlockModelGroupType.STAIR, new BlockModelId(namespace, path));
	}

	private void updateCache() {
		switch (renderingStairType) {
			case STRAIGHT -> {
				modelCache = TemporaryBlockModelLoader.loadModel(entryStraight.toJson());
			}
			case INNER -> {
				modelCache = TemporaryBlockModelLoader.loadModel(entryInner.toJson());
			}
			case OUTER -> {
				modelCache = TemporaryBlockModelLoader.loadModel(entryOuter.toJson());
			}
		}
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