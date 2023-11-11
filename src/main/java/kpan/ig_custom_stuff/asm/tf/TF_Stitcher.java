package kpan.ig_custom_stuff.asm.tf;

import kpan.ig_custom_stuff.asm.core.AsmTypes;
import kpan.ig_custom_stuff.asm.core.AsmUtil;
import kpan.ig_custom_stuff.asm.core.MyAsmNameRemapper.FieldRemap;
import kpan.ig_custom_stuff.asm.core.MyAsmNameRemapper.MethodRemap;
import kpan.ig_custom_stuff.asm.core.adapters.InjectInstructionsAdapter;
import kpan.ig_custom_stuff.asm.core.adapters.Instructions;
import kpan.ig_custom_stuff.asm.core.adapters.MixinAccessorAdapter;
import kpan.ig_custom_stuff.asm.core.adapters.MyClassVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

public class TF_Stitcher {

	private static final String TARGET = "net.minecraft.client.renderer.texture.Stitcher";
	private static final String HOOK = AsmTypes.HOOK + "HK_" + "Stitcher";
	private static final String ACC = AsmTypes.ACC + "ACC_" + "Stitcher";

	private static final String textureAtlasSprite = "net.minecraft.client.renderer.texture.TextureAtlasSprite";

	private static final FieldRemap currentWidth = new FieldRemap(TARGET, "currentWidth", AsmTypes.INT, "field_94318_c");
	private static final MethodRemap doStitch = new MethodRemap(TARGET, "doStitch", AsmTypes.METHOD_VOID, "func_94305_f");
	private static final MethodRemap addSprite = new MethodRemap(TARGET, "addSprite", AsmUtil.toMethodDesc(AsmTypes.VOID, textureAtlasSprite), "func_110934_a");

	public static ClassVisitor appendVisitor(ClassVisitor cv, String className) {
		if (!TARGET.equals(className))
			return cv;
		ClassVisitor newcv = new MyClassVisitor(cv, className) {
			@Override
			public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
				MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
				if (doStitch.isTarget(name, desc)) {
					mv = InjectInstructionsAdapter.before(mv, name
							, Instructions.create()
									.getField(currentWidth)
							, Instructions.create()
									.aload(0)
									.invokeStatic(HOOK, "storeUsedWH", AsmUtil.composeRuntimeMethodDesc(AsmTypes.VOID, TARGET))
					);
					success();
				}
				if (addSprite.isTarget(name, desc)) {
					mv = InjectInstructionsAdapter.injectFirst(mv, name
							, Instructions.create()
									.aload(0)
									.aload(1)
									.invokeStatic(HOOK, "onAddSprite", AsmUtil.composeRuntimeMethodDesc(AsmTypes.VOID, TARGET, textureAtlasSprite))
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
