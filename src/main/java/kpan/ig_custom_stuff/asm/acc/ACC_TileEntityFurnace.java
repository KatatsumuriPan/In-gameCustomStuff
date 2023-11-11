package kpan.ig_custom_stuff.asm.acc;

import kpan.ig_custom_stuff.asm.core.adapters.MixinAccessorAdapter.NewField;
import kpan.ig_custom_stuff.asm.core.adapters.MixinAccessorAdapter.SrgName;

public interface ACC_TileEntityFurnace {

	//フィールドアクセスの使用例

	//インスタンスフィールドのgetter
	//remapが必要な場合はSrgNameでsrg名を指定する(バニラのコードならそもそもAccessTransformerを使うべき)
	@SrgName("field_174906_k")
	int get_cookTime();

	//インスタンスフィールドのsetter
	//説明はgetter同様
	@SrgName("field_145958_o")
	void set_furnaceCustomName(String value);

	//新しいインスタンスフィールドの追加&getter作成
	//getterとsetterの両方を作成する必要はないが、初期化する方法が無いので両方作るのが基本
	//@NewFieldはgetterとsetterの両方に必要
	@NewField
	int get_openCount();

	//新しいインスタンスフィールドの追加&setter作成
	//説明はgetter同様
	@NewField
	void set_openCount(int value);

	//staticフィールドのgetter
	//インスタンスフィールドの場合と同様
	//setterも同様にして作れる
	@SrgName("field_145959_l")
	static int[] get_SLOTS_BOTTOM() { throw new AssertionError(); }

	//新しいstaticフィールドの追加&getter作成
	//インスタンスフィールドの場合と同様
	@NewField
	static int get_openCountStatic() { throw new AssertionError(); }

	//新しいstaticフィールドの追加&setter作成
	//インスタンスフィールドの場合と同様
	@NewField
	static void set_openCountStatic(int value) { throw new AssertionError(); }

}
