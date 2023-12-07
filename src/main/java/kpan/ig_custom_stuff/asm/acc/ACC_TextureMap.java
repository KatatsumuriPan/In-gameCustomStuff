package kpan.ig_custom_stuff.asm.acc;

import kpan.ig_custom_stuff.asm.core.adapters.MixinAccessorAdapter.NewField;
import net.minecraft.client.renderer.texture.Stitcher;

public interface ACC_TextureMap {

	//新しいインスタンスフィールドの追加&getter作成
	//getterとsetterの両方を作成する必要はないが、初期化する方法が無いので両方作るのが基本
	//@NewFieldはgetterとsetterの両方に必要
	@NewField
	Stitcher get_stitcher();
	@NewField
	void set_stitcher(Stitcher value);

}
