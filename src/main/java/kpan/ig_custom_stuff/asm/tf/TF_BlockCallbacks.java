package kpan.ig_custom_stuff.asm.tf;

import kpan.ig_custom_stuff.asm.core.AsmTypes;
import kpan.ig_custom_stuff.asm.core.AsmUtil;
import kpan.ig_custom_stuff.asm.core.adapters.InjectInstructionsAdapter;
import kpan.ig_custom_stuff.asm.core.adapters.Instructions;
import kpan.ig_custom_stuff.asm.core.adapters.MyClassVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

public class TF_BlockCallbacks {

	private static final String TARGET = "net.minecraftforge.registries.GameData$BlockCallbacks";
	private static final String HOOK = AsmTypes.HOOK + "HK_" + "BlockCallbacks";

	public static ClassVisitor appendVisitor(ClassVisitor cv, String className) {
		if (!TARGET.equals(className))
			return cv;
		ClassVisitor newcv = new MyClassVisitor(cv, className) {
			@Override
			public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
				MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
				if (name.equals("onAdd") && !desc.endsWith("IForgeRegistryEntry;)V")) {
					mv = InjectInstructionsAdapter.injectBeforeReturns(mv, name
							, Instructions.create()
									.iload(3)
									.aload(4)
									.aload(6)
									.invokeStatic(HOOK, "onOnAdd", AsmUtil.composeRuntimeMethodDesc(AsmTypes.VOID, AsmTypes.INT, AsmTypes.BLOCK, "net.minecraft.util.ObjectIntIdentityMap"))
					);
					success();
				}
				return mv;
			}
		};
		return newcv;
	}
}
