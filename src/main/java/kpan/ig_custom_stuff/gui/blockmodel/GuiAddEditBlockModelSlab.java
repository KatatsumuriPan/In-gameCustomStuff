package kpan.ig_custom_stuff.gui.blockmodel;

import kpan.ig_custom_stuff.ModReference;
import kpan.ig_custom_stuff.block.BlockModelEntry;
import kpan.ig_custom_stuff.block.BlockModelEntry.ModelType;
import kpan.ig_custom_stuff.block.BlockModelFaceEntry;
import kpan.ig_custom_stuff.block.EnumSlabType;
import kpan.ig_custom_stuff.block.TextureUV;
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
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
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
public class GuiAddEditBlockModelSlab extends GuiScreen implements IMyGuiScreen {
	private static final ResourceLocation RESOURCE_PACKS_TEXTURE = new ResourceLocation("textures/gui/resource_packs.png");
	private static final int textureSize = 32;
	private static final int buttonTop = 100;
	private static final int viewLeft = 270;

	public static GuiAddEditBlockModelSlab add(IMyGuiScreen okScreen, IMyGuiScreen cancelScreen) {
		BlockModelFaceEntry[] faces_double = new BlockModelFaceEntry[6];
		BlockModelFaceEntry[] faces_top = new BlockModelFaceEntry[6];
		BlockModelFaceEntry[] faces_bottom = new BlockModelFaceEntry[6];
		for (int i = 0; i < faces_double.length; i++) {
			EnumFacing facing = EnumFacing.VALUES[i];
			faces_double[i] = new BlockModelFaceEntry(facing.getName2(), TextureUV.FULL, 0, facing);
			faces_top[i] = new BlockModelFaceEntry(facing.getName2(), facing.getAxis() == Axis.Y ? TextureUV.FULL : TextureUV.TOP, 0, facing == EnumFacing.DOWN ? null : facing);
			faces_bottom[i] = new BlockModelFaceEntry(facing.getName2(), facing.getAxis() == Axis.Y ? TextureUV.FULL : TextureUV.BOTTOM, 0, facing == EnumFacing.UP ? null : facing);
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
		return new GuiAddEditBlockModelSlab(okScreen, cancelScreen, true, new BlockModelEntry(ModelType.SLAB_TOP, faces_top, textureIds), new BlockModelEntry(ModelType.SLAB_BOTTOM, faces_bottom, new HashMap<>(textureIds)), new BlockModelEntry(ModelType.SLAB_DOUBLE, faces_double, new HashMap<>(textureIds)));
	}
	public static GuiAddEditBlockModelSlab edit(IMyGuiScreen parent, BlockModelGroupId modelGroupId) {
		Map<String, BlockModelId> map = modelGroupId.getBlockModelIds();
		BlockModelEntry entryTop = ClientCache.INSTANCE.getBlockModel(map.get("top"));
		BlockModelEntry entryBottom = ClientCache.INSTANCE.getBlockModel(map.get("bottom"));
		BlockModelEntry entryDouble = ClientCache.INSTANCE.getBlockModel(map.get("double"));
		GuiAddEditBlockModelSlab gui = new GuiAddEditBlockModelSlab(parent, parent, false, entryTop, entryBottom, entryDouble);
		gui.initModelGroupId = modelGroupId.namespace + ":" + modelGroupId.path.substring("slab/".length());
		return gui;
	}

	private final IMyGuiScreen okScreen;
	private final IMyGuiScreen cancelScreen;
	private final boolean isAdd;
	private GuiButton createButton;
	private String initModelGroupId = "";
	private GuiTextField modelIdField;
	private @Nullable String modelIdError = null;
	private BlockModelEntry entryTop;
	private BlockModelEntry entryBottom;
	private BlockModelEntry entryDouble;
	private IBakedModel modelCache;
	private int lastMouseX = -1;
	private int lastMouseY = -1;
	private float rotateYaw = 10;
	private float rotatePitch = 30;
	private EnumSlabType renderingSlabType = EnumSlabType.DOUBLE;

	public GuiAddEditBlockModelSlab(IMyGuiScreen okScreen, IMyGuiScreen cancelScreen, boolean isAdd, BlockModelEntry entryTop, BlockModelEntry entryBottom, BlockModelEntry entryDouble) {
		this.okScreen = okScreen;
		this.cancelScreen = cancelScreen;
		this.isAdd = isAdd;
		this.entryTop = entryTop;
		this.entryBottom = entryBottom;
		this.entryDouble = entryDouble;
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
				Map<BlockModelId, BlockModelEntry> map = new HashMap<>();
				BlockModelGroupId modelGroupId = getBlockModelGroupId();
				map.put(modelGroupId.getVariantId("top"), entryTop);
				map.put(modelGroupId.getVariantId("bottom"), entryBottom);
				map.put(modelGroupId.getVariantId("double"), entryDouble);
				if (isAdd)
					MyPacketHandler.sendToServer(new MessageRegisterBlockModelsToServer(map));
				else
					MyPacketHandler.sendToServer(new MessageReplaceBlockModelsToServer(map));
				okScreen.redisplay();
			}
			case 1 -> cancelScreen.redisplay();
			case 3 -> {
				renderingSlabType = EnumSlabType.values()[(renderingSlabType.ordinal() + 2) % 3];
				updateCache();
			}
			case 4 -> {
				renderingSlabType = EnumSlabType.values()[(renderingSlabType.ordinal() + 1) % 3];
				updateCache();
			}
			case 10, 11, 12, 13, 14, 15, 16, 17, 18 -> {
				mc.displayGuiScreen(new GuiSelectTexture(resourceLocation -> {
					if (resourceLocation != null) {
						switch (button.id) {
							case 10 -> {
								entryTop.textureIds.put("up", resourceLocation);
								entryBottom.textureIds.put("up", resourceLocation);
								entryDouble.textureIds.put("up", resourceLocation);
							}
							case 11 -> {
								entryTop.textureIds.put("north", resourceLocation);
								entryBottom.textureIds.put("north", resourceLocation);
								entryDouble.textureIds.put("north", resourceLocation);
							}
							case 12 -> {
								entryTop.textureIds.put("west", resourceLocation);
								entryBottom.textureIds.put("west", resourceLocation);
								entryDouble.textureIds.put("west", resourceLocation);
							}
							case 13 -> {
								entryTop.textureIds.put("south", resourceLocation);
								entryBottom.textureIds.put("south", resourceLocation);
								entryDouble.textureIds.put("south", resourceLocation);
							}
							case 14 -> {
								entryTop.textureIds.put("east", resourceLocation);
								entryBottom.textureIds.put("east", resourceLocation);
								entryDouble.textureIds.put("east", resourceLocation);
							}
							case 15 -> {
								entryTop.textureIds.put("down", resourceLocation);
								entryBottom.textureIds.put("down", resourceLocation);
								entryDouble.textureIds.put("down", resourceLocation);
							}
							case 16 -> {
								entryTop.textureIds.put("particle", resourceLocation);
								entryBottom.textureIds.put("particle", resourceLocation);
								entryDouble.textureIds.put("particle", resourceLocation);
							}
							case 17 -> {
								for (EnumFacing facing : EnumFacing.VALUES) {
									entryTop.textureIds.put(facing.getName2(), resourceLocation);
									entryBottom.textureIds.put(facing.getName2(), resourceLocation);
									entryDouble.textureIds.put(facing.getName2(), resourceLocation);
								}
								entryTop.textureIds.put("particle", resourceLocation);
								entryBottom.textureIds.put("particle", resourceLocation);
								entryDouble.textureIds.put("particle", resourceLocation);
							}
							case 18 -> {
								for (EnumFacing facing : EnumFacing.HORIZONTALS) {
									entryTop.textureIds.put(facing.getName2(), resourceLocation);
									entryBottom.textureIds.put(facing.getName2(), resourceLocation);
									entryDouble.textureIds.put(facing.getName2(), resourceLocation);
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
							BlockModelFaceEntry faceEntry;
							switch (guibutton.id) {
								case 10 -> faceEntry = entryDouble.faces[EnumFacing.UP.getIndex()];
								case 11 -> faceEntry = entryDouble.faces[EnumFacing.NORTH.getIndex()];
								case 12 -> faceEntry = entryDouble.faces[EnumFacing.WEST.getIndex()];
								case 13 -> faceEntry = entryDouble.faces[EnumFacing.SOUTH.getIndex()];
								case 14 -> faceEntry = entryDouble.faces[EnumFacing.EAST.getIndex()];
								case 15 -> faceEntry = entryDouble.faces[EnumFacing.DOWN.getIndex()];
								default -> faceEntry = null;
							}
							if (faceEntry != null) {
								mc.displayGuiScreen(new GuiEditBlockModelFace(this, entryDouble.textureIds, faceEntry, blockModelFaceEntry -> {
									if (blockModelFaceEntry != null) {
										switch (guibutton.id) {
											case 10 -> {
												entryTop.faces[EnumFacing.UP.getIndex()] = blockModelFaceEntry;
												entryBottom.faces[EnumFacing.UP.getIndex()] = blockModelFaceEntry;
												entryDouble.faces[EnumFacing.UP.getIndex()] = blockModelFaceEntry;
											}
											case 11 -> {
												entryTop.faces[EnumFacing.NORTH.getIndex()] = blockModelFaceEntry.subFace(0, 0, 1, 0.5f);
												entryBottom.faces[EnumFacing.NORTH.getIndex()] = blockModelFaceEntry.subFace(0, 0.5f, 1, 1);
												entryDouble.faces[EnumFacing.NORTH.getIndex()] = blockModelFaceEntry;
											}
											case 12 -> {
												entryTop.faces[EnumFacing.WEST.getIndex()] = blockModelFaceEntry.subFace(0, 0, 1, 0.5f);
												entryBottom.faces[EnumFacing.WEST.getIndex()] = blockModelFaceEntry.subFace(0, 0.5f, 1, 1);
												entryDouble.faces[EnumFacing.WEST.getIndex()] = blockModelFaceEntry;
											}
											case 13 -> {
												entryTop.faces[EnumFacing.SOUTH.getIndex()] = blockModelFaceEntry.subFace(0, 0, 1, 0.5f);
												entryBottom.faces[EnumFacing.SOUTH.getIndex()] = blockModelFaceEntry.subFace(0, 0.5f, 1, 1);
												entryDouble.faces[EnumFacing.SOUTH.getIndex()] = blockModelFaceEntry;
											}
											case 14 -> {
												entryTop.faces[EnumFacing.EAST.getIndex()] = blockModelFaceEntry.subFace(0, 0, 1, 0.5f);
												entryBottom.faces[EnumFacing.EAST.getIndex()] = blockModelFaceEntry.subFace(0, 0.5f, 1, 1);
												entryDouble.faces[EnumFacing.EAST.getIndex()] = blockModelFaceEntry;
											}
											case 15 -> {
												entryTop.faces[EnumFacing.DOWN.getIndex()] = blockModelFaceEntry;
												entryBottom.faces[EnumFacing.DOWN.getIndex()] = blockModelFaceEntry;
												entryDouble.faces[EnumFacing.DOWN.getIndex()] = blockModelFaceEntry;
											}
										}
										updateCache();
									}
								}));
							} else if (guibutton.id == 17) {
								mc.displayGuiScreen(new GuiEditBlockModelFace(this, entryDouble.textureIds, entryDouble.faces[EnumFacing.UP.getIndex()], blockModelFaceEntry -> {
									if (blockModelFaceEntry != null) {
										entryTop.faces[EnumFacing.UP.getIndex()] = blockModelFaceEntry;
										entryBottom.faces[EnumFacing.UP.getIndex()] = blockModelFaceEntry;
										entryDouble.faces[EnumFacing.UP.getIndex()] = blockModelFaceEntry;
										for (EnumFacing facing : EnumFacing.VALUES) {
											if (facing == EnumFacing.UP)
												continue;
											if (facing != EnumFacing.DOWN) {
												entryTop.faces[facing.getIndex()] = with(entryTop.faces[facing.getIndex()], blockModelFaceEntry.uv, blockModelFaceEntry.rotation).subFace(0, 0, 1, 0.5f);
												entryBottom.faces[facing.getIndex()] = with(entryBottom.faces[facing.getIndex()], blockModelFaceEntry.uv, blockModelFaceEntry.rotation).subFace(0, 0.5f, 1, 1);
											} else {
												entryTop.faces[facing.getIndex()] = with(entryTop.faces[facing.getIndex()], blockModelFaceEntry.uv, blockModelFaceEntry.rotation);
												entryBottom.faces[facing.getIndex()] = with(entryBottom.faces[facing.getIndex()], blockModelFaceEntry.uv, blockModelFaceEntry.rotation);
											}
											entryDouble.faces[facing.getIndex()] = with(entryDouble.faces[facing.getIndex()], blockModelFaceEntry.uv, blockModelFaceEntry.rotation);
										}
										updateCache();
									}
								}));
							} else if (guibutton.id == 18) {
								mc.displayGuiScreen(new GuiEditBlockModelFace(this, entryDouble.textureIds, entryDouble.faces[EnumFacing.NORTH.getIndex()], blockModelFaceEntry -> {
									if (blockModelFaceEntry != null) {
										entryTop.faces[EnumFacing.NORTH.getIndex()] = blockModelFaceEntry.subFace(0, 0, 1, 0.5f);
										entryBottom.faces[EnumFacing.NORTH.getIndex()] = blockModelFaceEntry.subFace(0, 0.5f, 1, 1);
										entryDouble.faces[EnumFacing.NORTH.getIndex()] = blockModelFaceEntry;
										for (EnumFacing facing : EnumFacing.HORIZONTALS) {
											if (facing == EnumFacing.NORTH)
												continue;
											entryTop.faces[facing.getIndex()] = with(entryTop.faces[facing.getIndex()], blockModelFaceEntry.uv, blockModelFaceEntry.rotation).subFace(0, 0, 1, 0.5f);
											entryBottom.faces[facing.getIndex()] = with(entryBottom.faces[facing.getIndex()], blockModelFaceEntry.uv, blockModelFaceEntry.rotation).subFace(0, 0.5f, 1, 1);
											entryDouble.faces[facing.getIndex()] = with(entryDouble.faces[facing.getIndex()], blockModelFaceEntry.uv, blockModelFaceEntry.rotation);
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
			drawCenteredString(fontRenderer, I18n.format("gui.ingame_custom_stuff.addedit_block_model.slab.title.add"), width / 2, 20, -1);
		else
			drawCenteredString(fontRenderer, I18n.format("gui.ingame_custom_stuff.addedit_block_model.slab.title.edit"), width / 2, 20, -1);

		drawString(fontRenderer, I18n.format("gui.ingame_custom_stuff.addedit_block_model.label.block_model_id"), 20, 40 + 7, 0xFFA0A0A0);
		modelIdField.drawTextBox();
		if (modelIdError != null)
			drawString(fontRenderer, I18n.format(modelIdError), 100 + 4, 60 + 4, 0xFFFF2222);
		else if (isAdd)
			drawString(fontRenderer, I18n.format("gui.ingame_custom_stuff.addedit_block_model.info.real_model_id", getBlockModelGroupId().toResourceLocation().toString()), 100 + 4, 60 + 4, 0xFF22FF22);

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

			BlockModelFaceEntry face = entryDouble.faces[EnumFacing.VALUES[i].getIndex()];
			TextureAtlasSprite sprite = mc.getTextureMapBlocks().getAtlasSprite(entryDouble.textureIds.get(face.textureTag).toString());
			drawTexture(x, y, sprite, face.uv, face.rotation);
		}
		{
			//particle
			int x = (int) (100 + textureSize * 3.5);
			int y = buttonTop;
			TextureAtlasSprite sprite = mc.getTextureMapBlocks().getAtlasSprite(entryDouble.textureIds.get("particle").toString());
			drawTexture(x, y, sprite, TextureUV.FULL, 0);
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
		path = "slab/" + path;
		return new BlockModelGroupId(BlockModelGroupType.SLAB, new BlockModelId(namespace, path));
	}

	private void updateCache() {
		switch (renderingSlabType) {
			case TOP -> {
				modelCache = TemporaryBlockModelLoader.loadModel(entryTop.toJsonNormal());
			}
			case BOTTOM -> {
				modelCache = TemporaryBlockModelLoader.loadModel(entryBottom.toJsonNormal());
			}
			case DOUBLE -> {
				modelCache = TemporaryBlockModelLoader.loadModel(entryDouble.toJsonNormal());
			}
		}
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