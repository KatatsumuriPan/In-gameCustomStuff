package kpan.ig_custom_stuff.asm.core;

import kpan.ig_custom_stuff.asm.core.adapters.MixinAccessorAdapter;
import kpan.ig_custom_stuff.asm.tf.TF_FontRenderer;
import kpan.ig_custom_stuff.asm.tf.TF_TileEntityFurnace;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

public class ASMTransformer implements IClassTransformer {

	/**
	 * クラスが最初に読み込まれた時に呼ばれる。
	 *
	 * @param name            クラスの難読化名(区切りは'.')
	 * @param transformedName クラスの易読化名
	 * @param bytes           オリジナルのクラス
	 */
	@Override
	public byte[] transform(String name, String transformedName, byte[] bytes) {
		try {
			MyAsmNameRemapper.init();
			if (bytes == null)
				return null;
			//byte配列を読み込み、利用しやすい形にする。
			ClassReader cr = new ClassReader(bytes);
			//これのvisitを呼ぶことによって情報が溜まっていく。
			ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);//maxStack,maxLocal,frameの全てを計算
			//Adapterを通して書き換え出来るようにする。
			ClassVisitor cv = cw;
			cv = MixinAccessorAdapter.transformAccessor(cv, transformedName);
			cv = TF_FontRenderer.appendVisitor(cv, transformedName);
			cv = TF_TileEntityFurnace.appendVisitor(cv, transformedName);

			if (cv == cw)
				return bytes;

			//元のクラスと同様の順番でvisitメソッドを呼んでくれる
			cr.accept(cv, 0);

			byte[] new_bytes = cw.toByteArray();

			//Writer内の情報をbyte配列にして返す。
			return new_bytes;
		} catch (Exception e) {
			System.out.println("transformedName:" + transformedName);
			System.out.println(e.getMessage());
			e.printStackTrace();
			throw e;
		}
	}


}
