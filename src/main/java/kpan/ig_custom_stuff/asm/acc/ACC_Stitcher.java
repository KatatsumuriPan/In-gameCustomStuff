package kpan.ig_custom_stuff.asm.acc;

import kpan.ig_custom_stuff.asm.core.adapters.MixinAccessorAdapter.NewField;

public interface ACC_Stitcher {

	//新しいインスタンスフィールドの追加&getter作成
	//getterとsetterの両方を作成する必要はないが、初期化する方法が無いので両方作るのが基本
	//@NewFieldはgetterとsetterの両方に必要
	@NewField
	int get_usedWidth();
	@NewField
	void set_usedWidth(int value);

	@NewField
	int get_usedHeight();
	@NewField
	void set_usedHeight(int value);

}
