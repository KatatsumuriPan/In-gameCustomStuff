package kpan.ig_custom_stuff.asm.hook;

import kpan.ig_custom_stuff.asm.acc.ACC_Stitcher;
import kpan.ig_custom_stuff.resource.StitchManager;
import net.minecraft.client.renderer.texture.Stitcher;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;

public class HK_Stitcher {

	public static void storeUsedWH(Stitcher stitcher) {
		((ACC_Stitcher) stitcher).set_usedWidth(stitcher.currentWidth);
		((ACC_Stitcher) stitcher).set_usedHeight(stitcher.currentHeight);
		stitcher.currentWidth += Math.min(16, 256 * 512 / stitcher.currentHeight);
	}

	public static void onAddSprite(Stitcher self, TextureAtlasSprite textureAtlasSprite) {
		ResourceLocation textureId = new ResourceLocation(textureAtlasSprite.getIconName());
		String namespace = textureId.getNamespace();
		if (namespace.equals("minecraft"))
			return;
		StitchManager.addLoadedTextureHash(textureId);
	}
}
