package kpan.ig_custom_stuff.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

import java.util.List;

public class RenderUtil {


	public static void renderItemIntoGUI(ItemStack stack, int x, int y, float scale) {
		renderItemModelIntoGUI(stack, x, y, scale, Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(stack, null, null));
	}

	public static void renderItemModelIntoGUI(ItemStack stack, int x, int y, float scale, IBakedModel bakedModel) {
		RenderHelper.enableGUIStandardItemLighting();
		TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();
		GlStateManager.pushMatrix();
		textureManager.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		textureManager.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
		GlStateManager.enableRescaleNormal();
		GlStateManager.enableAlpha();
		GlStateManager.alphaFunc(516, 0.1F);
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		setupGuiTransform(x, y, scale, bakedModel.isGui3d());
		bakedModel = net.minecraftforge.client.ForgeHooksClient.handleCameraTransforms(bakedModel, ItemCameraTransforms.TransformType.GUI, false);
		Minecraft.getMinecraft().getRenderItem().renderItem(stack, bakedModel);
		GlStateManager.disableAlpha();
		GlStateManager.disableRescaleNormal();
		GlStateManager.disableLighting();
		GlStateManager.popMatrix();
		textureManager.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		textureManager.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();
	}

	private static void setupGuiTransform(int xPosition, int yPosition, float scale, boolean isGui3d) {
		GlStateManager.translate((float) xPosition, (float) yPosition, 100.0F + Minecraft.getMinecraft().getRenderItem().zLevel);
		GlStateManager.scale(scale, scale, scale);
		GlStateManager.translate(8.0F, 8.0F, 0.0F);
		GlStateManager.scale(1.0F, -1.0F, 1.0F);
		GlStateManager.scale(16.0F, 16.0F, 16.0F);

		if (isGui3d) {
			GlStateManager.enableLighting();
		} else {
			GlStateManager.disableLighting();
		}
	}

	public static void renderModel(int x, int y, float scale, IBakedModel bakedModel) {
		renderModel(x, y, scale, 225, 30, bakedModel);
	}
	public static void renderModel(int x, int y, float scale, float yaw, float pitch, IBakedModel bakedModel) {
		RenderHelper.enableGUIStandardItemLighting();
		TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();
		GlStateManager.pushMatrix();
		textureManager.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		textureManager.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
		GlStateManager.enableRescaleNormal();
		GlStateManager.enableAlpha();
		GlStateManager.alphaFunc(516, 0.1F);
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		setupGuiTransform(x, y, scale, bakedModel.isGui3d());
		bakedModel = net.minecraftforge.client.ForgeHooksClient.handleCameraTransforms(bakedModel, TransformType.FIXED, false);
		GlStateManager.scale(1.252f, 1.25f, 1.25f);
		if (pitch != 0)
			GlStateManager.rotate(pitch, 1, 0, 0);
		if (yaw != 0)
			GlStateManager.rotate(yaw, 0, 1, 0);
		GlStateManager.translate(-0.5F, -0.5F, -0.5F);
		renderModel(bakedModel);
		GlStateManager.disableAlpha();
		GlStateManager.disableRescaleNormal();
		GlStateManager.disableLighting();
		GlStateManager.popMatrix();
		textureManager.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		textureManager.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();
	}

	public static void renderModel(IBakedModel bakedModel) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		bufferbuilder.begin(7, DefaultVertexFormats.ITEM);

		for (EnumFacing enumfacing : EnumFacing.values()) {
			renderQuads(bufferbuilder, bakedModel.getQuads(null, enumfacing, 0L));
		}

		renderQuads(bufferbuilder, bakedModel.getQuads(null, null, 0L));
		tessellator.draw();
	}
	public static void renderQuads(BufferBuilder renderer, List<BakedQuad> quads) {
		int color = -1;
		for (BakedQuad quad : quads) {
			net.minecraftforge.client.model.pipeline.LightUtil.renderQuadColor(renderer, quad, color);
		}
	}

	//uvは0-16
	public static void drawTexturedModalRect(int xCoord, int yCoord, float zLevel, TextureAtlasSprite sprite, float minU, float minV, float maxU, float maxV, int rotation, int widthIn, int heightIn) {
		minU = sprite.getInterpolatedU(minU);
		minV = sprite.getInterpolatedV(minV);
		maxU = sprite.getInterpolatedU(maxU);
		maxV = sprite.getInterpolatedV(maxV);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
		switch (rotation) {
			case 0 -> {
				bufferbuilder.pos(xCoord, yCoord + heightIn, zLevel).tex(minU, maxV).endVertex();
				bufferbuilder.pos(xCoord + widthIn, yCoord + heightIn, zLevel).tex(maxU, maxV).endVertex();
				bufferbuilder.pos(xCoord + widthIn, yCoord, zLevel).tex(maxU, minV).endVertex();
				bufferbuilder.pos(xCoord, yCoord, zLevel).tex(minU, minV).endVertex();
			}
			case 90 -> {
				bufferbuilder.pos(xCoord, yCoord + heightIn, zLevel).tex(maxU, maxV).endVertex();
				bufferbuilder.pos(xCoord + widthIn, yCoord + heightIn, zLevel).tex(maxU, minV).endVertex();
				bufferbuilder.pos(xCoord + widthIn, yCoord, zLevel).tex(minU, minV).endVertex();
				bufferbuilder.pos(xCoord, yCoord, zLevel).tex(minU, maxV).endVertex();
			}
			case 180 -> {
				bufferbuilder.pos(xCoord, yCoord + heightIn, zLevel).tex(maxU, minV).endVertex();
				bufferbuilder.pos(xCoord + widthIn, yCoord + heightIn, zLevel).tex(minU, minV).endVertex();
				bufferbuilder.pos(xCoord + widthIn, yCoord, zLevel).tex(minU, maxV).endVertex();
				bufferbuilder.pos(xCoord, yCoord, zLevel).tex(maxU, maxV).endVertex();
			}
			case 270 -> {
				bufferbuilder.pos(xCoord, yCoord + heightIn, zLevel).tex(minU, minV).endVertex();
				bufferbuilder.pos(xCoord + widthIn, yCoord + heightIn, zLevel).tex(minU, maxV).endVertex();
				bufferbuilder.pos(xCoord + widthIn, yCoord, zLevel).tex(maxU, maxV).endVertex();
				bufferbuilder.pos(xCoord, yCoord, zLevel).tex(maxU, minV).endVertex();
			}
		}
		tessellator.draw();
	}


}
