package kpan.ig_custom_stuff.asm.core;

import kpan.ig_custom_stuff.asm.core.adapters.MixinAccessorAdapter;
import kpan.ig_custom_stuff.asm.tf.TF_CPacketCustomPayload;
import kpan.ig_custom_stuff.asm.tf.TF_FMLHandshakeClientState;
import kpan.ig_custom_stuff.asm.tf.TF_FMLHandshakeServerState;
import kpan.ig_custom_stuff.asm.tf.TF_GameData;
import kpan.ig_custom_stuff.asm.tf.TF_Minecraft;
import kpan.ig_custom_stuff.asm.tf.TF_ModelLoader;
import kpan.ig_custom_stuff.asm.tf.TF_ModelLoaderRegistry;
import kpan.ig_custom_stuff.asm.tf.TF_RegistryData;
import kpan.ig_custom_stuff.asm.tf.TF_Stitcher;
import kpan.ig_custom_stuff.asm.tf.TF_TextureMap;
import kpan.ig_custom_stuff.asm.tf.TF_WeightedRandomModel;
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
			cv = TF_CPacketCustomPayload.appendVisitor(cv, transformedName);
			cv = TF_FMLHandshakeClientState.appendVisitor(cv, transformedName);
			cv = TF_FMLHandshakeServerState.appendVisitor(cv, transformedName);
			cv = TF_GameData.appendVisitor(cv, transformedName);
			cv = TF_Minecraft.appendVisitor(cv, transformedName);
			cv = TF_ModelLoader.appendVisitor(cv, transformedName);
			cv = TF_ModelLoaderRegistry.appendVisitor(cv, transformedName);
			cv = TF_RegistryData.appendVisitor(cv, transformedName);
			cv = TF_Stitcher.appendVisitor(cv, transformedName);
			cv = TF_TextureMap.appendVisitor(cv, transformedName);
			cv = TF_WeightedRandomModel.appendVisitor(cv, transformedName);

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
