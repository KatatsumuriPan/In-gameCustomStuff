package kpan.ig_custom_stuff.gui;

import kpan.ig_custom_stuff.gui.block.GuiBlockMenu;
import kpan.ig_custom_stuff.gui.blockmodel.GuiBlockModelMenu;
import kpan.ig_custom_stuff.gui.item.GuiItemMenu;
import kpan.ig_custom_stuff.gui.texture.GuiTextureMenu;
import kpan.ig_custom_stuff.network.MyPacketHandler;
import kpan.ig_custom_stuff.network.client.MessageUpdateChunkLightToServer;
import kpan.ig_custom_stuff.util.handlers.ClientEventHandler;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

@SideOnly(Side.CLIENT)
public class GuiRegistryMenu extends GuiScreen implements IMyGuiScreen {

	private GuiButton updateChunkLightBtn;
	private long lastUpdatedTick = 0;

	public GuiRegistryMenu() {
	}

	@Override
	public void initGui() {
		addButton(new GuiButton(1, width / 2 - 100, 40, 200, 20, I18n.format("gui.ingame_custom_stuff.registry_menu.button_label.block")));
		addButton(new GuiButton(2, width / 2 - 100, 80, 200, 20, I18n.format("gui.ingame_custom_stuff.registry_menu.button_label.item")));
		addButton(new GuiButton(3, width / 2 - 100, 120, 200, 20, I18n.format("gui.ingame_custom_stuff.registry_menu.button_label.block_model")));
		addButton(new GuiButton(4, width / 2 - 100, 160, 200, 20, I18n.format("gui.ingame_custom_stuff.registry_menu.button_label.texture")));
		addButton(new GuiButton(0, width / 2 - 100, height - 28, 200, 20, I18n.format("gui.done")));

		updateChunkLightBtn = addButton(new GuiButton(5, width - 140, 0, 140, 20, I18n.format("gui.ingame_custom_stuff.registry_menu.button_label.update_chunk_light")));
		updateChunkLightBtn.enabled = false;
	}

	@Override
	public void redisplay() {
		mc.displayGuiScreen(this);
	}


	@Override
	protected void keyTyped(char typedChar, int keyCode) {
		if (keyCode == Keyboard.KEY_ESCAPE) {
			mc.displayGuiScreen(null);
		}
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		if (button.enabled) {
			switch (button.id) {
				case 0 -> mc.displayGuiScreen(null);
				case 1 -> mc.displayGuiScreen(new GuiBlockMenu(this));
				case 2 -> mc.displayGuiScreen(new GuiItemMenu(this));
				case 3 -> mc.displayGuiScreen(new GuiBlockModelMenu(this));
				case 4 -> mc.displayGuiScreen(new GuiTextureMenu(this));
				case 5 -> {
					EntityPlayerSP player = mc.player;
					MyPacketHandler.sendToServer(new MessageUpdateChunkLightToServer(player.chunkCoordX, player.chunkCoordZ));
					lastUpdatedTick = ClientEventHandler.tick;
					updateChunkLightBtn.enabled = false;
				}
				default -> {
				}
			}
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		super.drawScreen(mouseX, mouseY, partialTicks);
		drawCenteredString(fontRenderer, I18n.format("gui.ingame_custom_stuff.registry_menu.title"), width / 2, 8 - 4, 16777215);
	}


	@Override
	public void updateScreen() {
		super.updateScreen();
		if (ClientEventHandler.tick > lastUpdatedTick + 80)
			updateChunkLightBtn.enabled = true;
	}
}