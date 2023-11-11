package kpan.ig_custom_stuff.asm.tf;

import kpan.ig_custom_stuff.asm.core.AsmTypes;
import kpan.ig_custom_stuff.asm.core.AsmUtil;
import kpan.ig_custom_stuff.asm.core.MyAsmNameRemapper;
import kpan.ig_custom_stuff.asm.core.MyAsmNameRemapper.MethodRemap;
import kpan.ig_custom_stuff.asm.core.adapters.InjectInstructionsAdapter;
import kpan.ig_custom_stuff.asm.core.adapters.Instructions;
import kpan.ig_custom_stuff.asm.core.adapters.Instructions.OpcodeMethod;
import kpan.ig_custom_stuff.asm.core.adapters.MixinAccessorAdapter;
import kpan.ig_custom_stuff.asm.core.adapters.MyClassVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class TF_TextureMap {

	private static final String TARGET = "net.minecraft.client.renderer.texture.TextureMap";
	private static final String HOOK = AsmTypes.HOOK + "HK_" + "TextureMap";
	private static final String ACC = AsmTypes.ACC + "ACC_" + "TextureMap";

	private static final String STITCHER = "net.minecraft.client.renderer.texture.Stitcher";

	private static final MethodRemap loadSprites = new MethodRemap(TARGET, "loadSprites", AsmUtil.toMethodDesc(AsmTypes.VOID, "net.minecraft.client.resources.IResourceManager", "net.minecraft.client.renderer.texture.ITextureMapPopulator"), "func_174943_a");
	private static final MethodRemap loadTextureAtlas = new MethodRemap(TARGET, "loadTextureAtlas", AsmUtil.toMethodDesc(AsmTypes.VOID, "net.minecraft.client.resources.IResourceManager"), "func_110571_b");

	public static ClassVisitor appendVisitor(ClassVisitor cv, String className) {
		if (!TARGET.equals(className))
			return cv;
		ClassVisitor newcv = new MyClassVisitor(cv, className) {
			@Override
			public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
				MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
				if (loadTextureAtlas.isTarget(name, desc)) {
					mv = InjectInstructionsAdapter.after(mv, name
							, Instructions.create()
									.methodRep(OpcodeMethod.SPECIAL, MyAsmNameRemapper.runtimeClass(STITCHER), "<init>")
							, Instructions.create()
									.insn(Opcodes.DUP)
									.aload(0)
									.invokeStatic(HOOK, "storeStitcher", AsmUtil.composeRuntimeMethodDesc(AsmTypes.VOID, STITCHER, TARGET))
					);
					success();
				}
				if (loadSprites.isTarget(name, desc)) {
					mv = InjectInstructionsAdapter.injectFirst(mv, name
							, Instructions.create()
									.aload(0)
									.invokeStatic(HOOK, "onLoadSprites", AsmUtil.composeRuntimeMethodDesc(AsmTypes.VOID, TARGET))
					);
					success();
				}
				return mv;
			}
		}.setSuccessExpected(2);
		newcv = new MixinAccessorAdapter(newcv, className, ACC);
		return newcv;
	}
}
