package kpan.ig_custom_stuff.gui.block;

import kpan.ig_custom_stuff.block.BlockStateEntry.BlockStateModelEntry;
import kpan.ig_custom_stuff.gui.IMyGuiScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import org.jetbrains.annotations.Nullable;

public class GuiSelectBlockModelButton extends GuiButton {

	public final IMyGuiScreen parent;
	public @Nullable BlockStateModelEntry modelEntry;

	public GuiSelectBlockModelButton(IMyGuiScreen parent, int buttonId, int x, int y, int widthIn, int heightIn, @Nullable BlockStateModelEntry modelEntry) {
		super(buttonId, x, y, widthIn, heightIn, "");
		this.parent = parent;
		this.modelEntry = modelEntry;
	}

	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
		if (visible) {
			hovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
			Gui.drawRect(x, y, x + width, y + height, -1);
			if (modelEntry != null)
				modelEntry.render(x, y, Math.min(width, height) / 16f, 30, 20);
			else {
				String text = I18n.format("gui.ingame_custom_stuff.configure_block_state.label.not_configured_model");
				FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
				fontRenderer.drawSplitString(text, x + width / 2 - Math.min(width, fontRenderer.getStringWidth(text)) / 2, y + height / 2, width, 0x80000000);
			}
			GlStateManager.disableDepth();
		}
	}

	public void onPressed(int mouse) {
		if (mouse == 0) {
			Minecraft.getMinecraft().displayGuiScreen(new GuiSelectBlockModel(modelId -> {
				if (modelId != null) {
					if (modelEntry == null)
						modelEntry = new BlockStateModelEntry(modelId);
					else
						modelEntry = modelEntry.with(modelId);
				}
				parent.redisplay();
			}));
		} else if (mouse == 1) {
			if (modelEntry != null) {
				Minecraft.getMinecraft().displayGuiScreen(new GuiEditBlockModelRotation(parent, modelEntry, modelEntry -> {
					this.modelEntry = modelEntry;
					parent.redisplay();
				}));
			}
		}
	}
}
