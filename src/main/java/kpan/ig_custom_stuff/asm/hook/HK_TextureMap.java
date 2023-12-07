package kpan.ig_custom_stuff.asm.hook;

import kpan.ig_custom_stuff.asm.acc.ACC_TextureMap;
import kpan.ig_custom_stuff.resource.StitchManager;
import net.minecraft.client.renderer.texture.Stitcher;
import net.minecraft.client.renderer.texture.TextureMap;

public class HK_TextureMap {

	public static void storeStitcher(Stitcher stitcher, TextureMap self) {
		//引数の順が逆なことに注意
		((ACC_TextureMap) self).set_stitcher(stitcher);
	}

	public static void onLoadSprites(TextureMap self) {
		StitchManager.onLoadSprites();
	}

}
