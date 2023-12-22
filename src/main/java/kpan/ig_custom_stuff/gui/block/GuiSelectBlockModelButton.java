package kpan.ig_custom_stuff.gui.block;

import kpan.ig_custom_stuff.block.BlockStateEntry;
import kpan.ig_custom_stuff.gui.IMyGuiScreen;
import kpan.ig_custom_stuff.resource.DynamicResourceLoader.SingleBlockModelLoader;
import kpan.ig_custom_stuff.resource.ids.BlockModelGroupId;
import kpan.ig_custom_stuff.resource.ids.BlockModelGroupId.BlockModelGroupType;
import kpan.ig_custom_stuff.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.resources.I18n;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class GuiSelectBlockModelButton extends GuiButton {

	public final IMyGuiScreen parent;
	private BlockModelGroupType blockModelGroupType;
	public @Nullable BlockModelGroupId blockModelGroupId;
	public int rotationX;
	public int rotationY;
	public boolean uvlock;

	public GuiSelectBlockModelButton(IMyGuiScreen parent, int buttonId, int x, int y, int widthIn, int heightIn, @Nullable BlockStateEntry blockStateEntry) {
		this(parent, buttonId, x, y, widthIn, heightIn, blockStateEntry != null ? blockStateEntry.blockModelGroupId : null, blockStateEntry != null ? blockStateEntry.rotationX : 0, blockStateEntry != null ? blockStateEntry.rotationY : 0, blockStateEntry != null ? blockStateEntry.uvlock : false);
	}
	public GuiSelectBlockModelButton(IMyGuiScreen parent, int buttonId, int x, int y, int widthIn, int heightIn, GuiSelectBlockModelButton before) {
		this(parent, buttonId, x, y, widthIn, heightIn, before.blockModelGroupId, before.rotationX, before.rotationY, before.uvlock);
	}
	public GuiSelectBlockModelButton(IMyGuiScreen parent, int buttonId, int x, int y, int widthIn, int heightIn, @Nullable BlockModelGroupId blockModelGroupId, int rotationX, int rotationY, boolean uvlock) {
		super(buttonId, x, y, widthIn, heightIn, "");
		this.parent = parent;
		this.blockModelGroupId = blockModelGroupId;
		this.rotationX = rotationX;
		this.rotationY = rotationY;
		blockModelGroupType = blockModelGroupId != null ? blockModelGroupId.blockModelGroupType : BlockModelGroupType.NORMAL;
		this.uvlock = uvlock;
	}

	public void setBlockModelGroupType(BlockModelGroupType blockModelGroupType) {
		this.blockModelGroupType = blockModelGroupType;
		if (blockModelGroupId != null && blockModelGroupId.blockModelGroupType != blockModelGroupType) {
			blockModelGroupId = null;
			rotationX = 0;
			rotationY = 0;
			uvlock = blockModelGroupType == BlockModelGroupType.STAIR;
		}
	}

	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
		if (visible) {
			hovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
			Gui.drawRect(x, y, x + width, y + height, -1);
			if (blockModelGroupId != null) {
				IBakedModel model = SingleBlockModelLoader.getModel(blockModelGroupId.getRenderModelId());
				if (model != null)
					RenderUtil.renderModel(x, y, Math.min(width, height) / 16f, 30 - rotationY, 20, rotationX, model);
			} else {
				String text = I18n.format("gui.ingame_custom_stuff.configure_block_state.label.not_configured_model");
				FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
				fontRenderer.drawSplitString(text, x + width / 2 - Math.min(width, fontRenderer.getStringWidth(text)) / 2, y + height / 2, width, 0x80000000);
			}
			GlStateManager.disableDepth();
		}
	}

	public void postDraw(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
		if (mousePressed(mc, mouseX, mouseY)) {
			if (blockModelGroupType == BlockModelGroupType.NORMAL)
				((GuiScreen) parent).drawHoveringText(Arrays.asList(I18n.format("gui.ingame_custom_stuff.configure_block_state.edit_rotation_hovering_text1"),
						I18n.format("gui.ingame_custom_stuff.configure_block_state.edit_rotation_hovering_text2")), mouseX, mouseY);
			else
				((GuiScreen) parent).drawHoveringText(Arrays.asList(I18n.format("gui.ingame_custom_stuff.configure_block_state.edit_rotation_hovering_text1")), mouseX, mouseY);
		}
	}

	public void onPressed(int mouse) {
		if (mouse == 0) {
			Minecraft.getMinecraft().displayGuiScreen(new GuiSelectBlockModel(blockModelGroupType, modelGroupId -> {
				if (modelGroupId != null) {
					blockModelGroupId = modelGroupId;
				}
				parent.redisplay();
			}));
		} else if (mouse == 1) {
			if (blockModelGroupId != null && blockModelGroupType == BlockModelGroupType.NORMAL) {
				Minecraft.getMinecraft().displayGuiScreen(new GuiEditBlockModelRotation(parent, blockModelGroupId.getRenderModelId(), rotationX, rotationY, (rotationX, rotationY) -> {
					this.rotationX = rotationX;
					this.rotationY = rotationY;
					parent.redisplay();
				}));
			}
		}
	}
}
