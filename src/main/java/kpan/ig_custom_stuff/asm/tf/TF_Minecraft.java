package kpan.ig_custom_stuff.asm.tf;

import kpan.ig_custom_stuff.asm.core.AsmTypes;
import kpan.ig_custom_stuff.asm.core.AsmUtil;
import kpan.ig_custom_stuff.asm.core.MyAsmNameRemapper.MethodRemap;
import kpan.ig_custom_stuff.asm.core.adapters.InjectInstructionsAdapter;
import kpan.ig_custom_stuff.asm.core.adapters.Instructions;
import kpan.ig_custom_stuff.asm.core.adapters.MyClassVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

public class TF_Minecraft {

	private static final String TARGET = "net.minecraft.client.Minecraft";
	private static final String HOOK = AsmTypes.HOOK + "HK_" + "Minecraft";

	private static final MethodRemap loadWorld = new MethodRemap(TARGET, "loadWorld", AsmUtil.toMethodDesc(AsmTypes.VOID, "net.minecraft.client.multiplayer.WorldClient", AsmTypes.STRING), "func_71353_a");

	public static ClassVisitor appendVisitor(ClassVisitor cv, String className) {
		if (!TARGET.equals(className))
			return cv;
		ClassVisitor newcv = new MyClassVisitor(cv, className) {
			@Override
			public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
				MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
				if (loadWorld.isTarget(name, desc)) {
					mv = InjectInstructionsAdapter.injectFirst(mv, name
							, Instructions.create()
									.aload(1)
									.invokeStatic(HOOK, "onUnload", AsmUtil.composeRuntimeMethodDesc(AsmTypes.VOID, AsmTypes.WORLD))
					);
					success();
				}
				return mv;
			}
		};
		return newcv;
	}
}
