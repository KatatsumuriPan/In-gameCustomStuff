package kpan.ig_custom_stuff.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

public class GuiUtil {
	public static void RenderTexture(Minecraft mc, ResourceLocation textureId, int x, int y, int w, int h) {
		mc.getTextureManager().bindTexture(new ResourceLocation(textureId.getNamespace(), "textures/" + textureId.getPath() + ".png"));
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
		bufferbuilder.pos(x, y + h, 0.0D).tex(0, 1).endVertex();
		bufferbuilder.pos(x + w, y + h, 0.0D).tex(1, 1).endVertex();
		bufferbuilder.pos(x + w, y, 0.0D).tex(1, 0).endVertex();
		bufferbuilder.pos(x, y, 0.0D).tex(0, 0).endVertex();
		tessellator.draw();
	}
}
