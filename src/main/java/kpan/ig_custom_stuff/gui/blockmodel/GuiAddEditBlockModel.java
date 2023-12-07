package kpan.ig_custom_stuff.gui.blockmodel;

import kpan.ig_custom_stuff.ModReference;
import kpan.ig_custom_stuff.block.BlockModelEntry;
import kpan.ig_custom_stuff.block.BlockModelEntry.ModelType;
import kpan.ig_custom_stuff.block.BlockModelFaceEntry;
import kpan.ig_custom_stuff.block.TextureUV;
import kpan.ig_custom_stuff.gui.IMyGuiScreen;
import kpan.ig_custom_stuff.gui.texture.GuiSelectTexture;
import kpan.ig_custom_stuff.network.MyPacketHandler;
import kpan.ig_custom_stuff.network.client.MessageRegisterBlockModelsToServer;
import kpan.ig_custom_stuff.network.client.MessageReplaceBlockModelsToServer;
import kpan.ig_custom_stuff.resource.DynamicResourceLoader.TemporaryBlockModelLoader;
import kpan.ig_custom_stuff.resource.DynamicResourceManager;
import kpan.ig_custom_stuff.resource.DynamicResourceManager.ClientCache;
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
public class GuiAddEditBlockModel extends GuiScreen implements IMyGuiScreen {
	private static final int textureSize = 32;
	private static final int buttonTop = 100;
	private static final int viewLeft = 270;

	public static GuiAddEditBlockModel add(IMyGuiScreen parent) {
		BlockModelFaceEntry[] faces = new BlockModelFaceEntry[6];
		for (int i = 0; i < faces.length; i++) {
			faces[i] = new BlockModelFaceEntry(EnumFacing.VALUES[i].getName2(), TextureUV.DEFAULT, 0, EnumFacing.VALUES[i]);
		}
		ResourceLocation dirt = new ResourceLocation("blocks/dirt");
		HashMap<String, ResourceLocation> textureIds = new HashMap<>();
		textureIds.put("down", dirt);
		textureIds.put("up", dirt);
		textureIds.put("north", dirt);
		textureIds.put("east", dirt);
		textureIds.put("south", dirt);
		textureIds.put("west", dirt);
		textureIds.put("particle", dirt);
		return new GuiAddEditBlockModel(parent, true, new BlockModelEntry(ModelType.NORMAL, faces, textureIds));
	}
	public static GuiAddEditBlockModel edit(IMyGuiScreen parent, ResourceLocation modelId, BlockModelEntry blockModelEntry) {
		GuiAddEditBlockModel gui = new GuiAddEditBlockModel(parent, false, blockModelEntry);
		gui.initModelId = modelId.toString();
		return gui;
	}

	private final IMyGuiScreen parent;
	private final boolean isAdd;
	private GuiButton createButton;
	private GuiButton modelTypeBtn;
	private String initModelId = "";
	private GuiTextField modelIdField;
	private @Nullable String modelIdError = null;
	private BlockModelEntry blockModelEntry;
	private IBakedModel modelCache;
	private int lastMouseX = -1;
	private int lastMouseY = -1;
	private float rotateYaw = 10;
	private float rotatePitch = 30;

	public GuiAddEditBlockModel(IMyGuiScreen parent, boolean isAdd, BlockModelEntry blockModelEntry) {
		this.parent = parent;
		this.isAdd = isAdd;
		this.blockModelEntry = blockModelEntry;
		modelCache = TemporaryBlockModelLoader.loadModel(blockModelEntry.toJson());
	}

	@Override
	public void initGui() {
		Keyboard.enableRepeatEvents(true);
		createButton = addButton(new GuiButton(0, width / 2 - 155, height - 28, 150, 20, I18n.format("gui.done")));
		addButton(new GuiButton(1, width / 2 + 5, height - 28, 150, 20, I18n.format("gui.cancel")));
		modelTypeBtn = addButton(new GuiButton(2, width - 150, 20, 150, 20, ""));
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
				Map<ResourceLocation, BlockModelEntry> map = new HashMap<>();
				map.put(getModelId(), blockModelEntry);
				if (isAdd)
					MyPacketHandler.sendToServer(new MessageRegisterBlockModelsToServer(map));
				else
					MyPacketHandler.sendToServer(new MessageReplaceBlockModelsToServer(map));
				parent.redisplay();
			}
			case 1 -> parent.redisplay();
			case 2 -> {
				ModelType nextModelType;
				switch (blockModelEntry.modelType) {
					case NORMAL -> nextModelType = ModelType.CAGE;
					case CAGE -> nextModelType = ModelType.NORMAL;
					default -> throw new AssertionError();
				}
				blockModelEntry = new BlockModelEntry(nextModelType, blockModelEntry.faces, blockModelEntry.textureIds);
				modelCache = TemporaryBlockModelLoader.loadModel(blockModelEntry.toJson());
				updateButtonText();
			}
			case 10, 11, 12, 13, 14, 15, 16, 17, 18 -> {
				mc.displayGuiScreen(new GuiSelectTexture(resourceLocation -> {
					if (resourceLocation != null) {
						switch (button.id) {
							case 10 -> blockModelEntry.textureIds.put("up", resourceLocation);
							case 11 -> blockModelEntry.textureIds.put("north", resourceLocation);
							case 12 -> blockModelEntry.textureIds.put("west", resourceLocation);
							case 13 -> blockModelEntry.textureIds.put("south", resourceLocation);
							case 14 -> blockModelEntry.textureIds.put("east", resourceLocation);
							case 15 -> blockModelEntry.textureIds.put("down", resourceLocation);
							case 16 -> blockModelEntry.textureIds.put("particle", resourceLocation);
							case 17 -> {
								blockModelEntry.textureIds.put("up", resourceLocation);
								blockModelEntry.textureIds.put("north", resourceLocation);
								blockModelEntry.textureIds.put("west", resourceLocation);
								blockModelEntry.textureIds.put("south", resourceLocation);
								blockModelEntry.textureIds.put("east", resourceLocation);
								blockModelEntry.textureIds.put("down", resourceLocation);
								blockModelEntry.textureIds.put("particle", resourceLocation);
							}
							case 18 -> {
								blockModelEntry.textureIds.put("north", resourceLocation);
								blockModelEntry.textureIds.put("west", resourceLocation);
								blockModelEntry.textureIds.put("south", resourceLocation);
								blockModelEntry.textureIds.put("east", resourceLocation);
							}
						}
						modelCache = TemporaryBlockModelLoader.loadModel(blockModelEntry.toJson());
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
							BlockModelFaceEntry faceEntry;
							switch (guibutton.id) {
								case 10 -> faceEntry = blockModelEntry.faces[EnumFacing.UP.getIndex()];
								case 11 -> faceEntry = blockModelEntry.faces[EnumFacing.NORTH.getIndex()];
								case 12 -> faceEntry = blockModelEntry.faces[EnumFacing.WEST.getIndex()];
								case 13 -> faceEntry = blockModelEntry.faces[EnumFacing.SOUTH.getIndex()];
								case 14 -> faceEntry = blockModelEntry.faces[EnumFacing.EAST.getIndex()];
								case 15 -> faceEntry = blockModelEntry.faces[EnumFacing.DOWN.getIndex()];
								default -> faceEntry = null;
							}
							if (faceEntry != null) {
								mc.displayGuiScreen(new GuiEditBlockModelFace(this, blockModelEntry.textureIds, faceEntry, blockModelFaceEntry -> {
									if (blockModelFaceEntry != null) {
										switch (guibutton.id) {
											case 10 -> blockModelEntry.faces[EnumFacing.UP.getIndex()] = blockModelFaceEntry;
											case 11 -> blockModelEntry.faces[EnumFacing.NORTH.getIndex()] = blockModelFaceEntry;
											case 12 -> blockModelEntry.faces[EnumFacing.WEST.getIndex()] = blockModelFaceEntry;
											case 13 -> blockModelEntry.faces[EnumFacing.SOUTH.getIndex()] = blockModelFaceEntry;
											case 14 -> blockModelEntry.faces[EnumFacing.EAST.getIndex()] = blockModelFaceEntry;
											case 15 -> blockModelEntry.faces[EnumFacing.DOWN.getIndex()] = blockModelFaceEntry;
										}
										modelCache = TemporaryBlockModelLoader.loadModel(blockModelEntry.toJson());
									}
								}));
							} else if (guibutton.id == 17) {
								mc.displayGuiScreen(new GuiEditBlockModelFace(this, blockModelEntry.textureIds, blockModelEntry.faces[EnumFacing.UP.getIndex()], blockModelFaceEntry -> {
									if (blockModelFaceEntry != null) {
										blockModelEntry.faces[EnumFacing.UP.getIndex()] = blockModelFaceEntry;
										blockModelEntry.faces[EnumFacing.NORTH.getIndex()] = with(blockModelEntry.faces[EnumFacing.NORTH.getIndex()], blockModelFaceEntry.uv, blockModelFaceEntry.rotation);
										blockModelEntry.faces[EnumFacing.WEST.getIndex()] = with(blockModelEntry.faces[EnumFacing.WEST.getIndex()], blockModelFaceEntry.uv, blockModelFaceEntry.rotation);
										blockModelEntry.faces[EnumFacing.SOUTH.getIndex()] = with(blockModelEntry.faces[EnumFacing.SOUTH.getIndex()], blockModelFaceEntry.uv, blockModelFaceEntry.rotation);
										blockModelEntry.faces[EnumFacing.EAST.getIndex()] = with(blockModelEntry.faces[EnumFacing.EAST.getIndex()], blockModelFaceEntry.uv, blockModelFaceEntry.rotation);
										blockModelEntry.faces[EnumFacing.DOWN.getIndex()] = with(blockModelEntry.faces[EnumFacing.DOWN.getIndex()], blockModelFaceEntry.uv, blockModelFaceEntry.rotation);
										modelCache = TemporaryBlockModelLoader.loadModel(blockModelEntry.toJson());
									}
								}));
							} else if (guibutton.id == 18) {
								mc.displayGuiScreen(new GuiEditBlockModelFace(this, blockModelEntry.textureIds, blockModelEntry.faces[EnumFacing.NORTH.getIndex()], blockModelFaceEntry -> {
									if (blockModelFaceEntry != null) {
										blockModelEntry.faces[EnumFacing.NORTH.getIndex()] = blockModelFaceEntry;
										blockModelEntry.faces[EnumFacing.WEST.getIndex()] = with(blockModelEntry.faces[EnumFacing.WEST.getIndex()], blockModelFaceEntry.uv, blockModelFaceEntry.rotation);
										blockModelEntry.faces[EnumFacing.SOUTH.getIndex()] = with(blockModelEntry.faces[EnumFacing.SOUTH.getIndex()], blockModelFaceEntry.uv, blockModelFaceEntry.rotation);
										blockModelEntry.faces[EnumFacing.EAST.getIndex()] = with(blockModelEntry.faces[EnumFacing.EAST.getIndex()], blockModelFaceEntry.uv, blockModelFaceEntry.rotation);
										modelCache = TemporaryBlockModelLoader.loadModel(blockModelEntry.toJson());
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
		else if (!modelIdField.getText().contains(":"))
			drawString(fontRenderer, I18n.format("gui.ingame_custom_stuff.info.default_namespace_message", ModReference.DEFAULT_NAMESPACE), 100 + 4, 60 + 4, 0xFF22FF22);


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

			BlockModelFaceEntry face = blockModelEntry.faces[EnumFacing.VALUES[i].getIndex()];
			TextureAtlasSprite sprite = mc.getTextureMapBlocks().getAtlasSprite(blockModelEntry.textureIds.get(face.textureTag).toString());
			drawTexture(x, y, sprite, face.uv, face.rotation);
		}
		{
			//particle
			int x = (int) (100 + textureSize * 3.5);
			int y = buttonTop;
			TextureAtlasSprite sprite = mc.getTextureMapBlocks().getAtlasSprite(blockModelEntry.textureIds.get("particle").toString());
			drawTexture(x, y, sprite, TextureUV.DEFAULT, 0);
		}
		Gui.drawRect(viewLeft, 80, width - 10, height - 30, -1);
		RenderUtil.renderModel(viewLeft, 80, Math.min(width - 10 - 250, height - 30 - 80) / 16f, rotateYaw, rotatePitch, modelCache);
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
				ResourceLocation modelId = getModelId();
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
		modelTypeBtn.displayString = blockModelEntry.modelType.getString();
	}

	private ResourceLocation getModelId() {
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
		if (!path.startsWith("block/"))
			path = "block/" + path;
		return new ResourceLocation(namespace, path);
	}


	private void drawTexture(int x, int y, TextureAtlasSprite sprite, TextureUV uv, int rotation) {
		Gui.drawRect(x, y, x + textureSize, y + textureSize, -1);
		GlStateManager.bindTexture(mc.getTextureMapBlocks().getGlTextureId());
		RenderUtil.drawTexturedModalRect(x, y, zLevel, sprite, uv.minU, uv.minV, uv.maxU, uv.maxV, rotation, textureSize, textureSize);
	}

	private BlockModelFaceEntry with(BlockModelFaceEntry base, TextureUV uv, int rotation) {
		return new BlockModelFaceEntry(base.textureTag, uv, rotation, base.cullface);
	}
}