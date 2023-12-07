package kpan.ig_custom_stuff.asm.tf;

import kpan.ig_custom_stuff.asm.core.AsmTypes;
import kpan.ig_custom_stuff.asm.core.AsmUtil;
import kpan.ig_custom_stuff.asm.core.adapters.InjectInstructionsAdapter;
import kpan.ig_custom_stuff.asm.core.adapters.Instructions;
import kpan.ig_custom_stuff.asm.core.adapters.MyClassVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

public class TF_GameData {

	private static final String TARGET = "net.minecraftforge.registries.GameData";
	private static final String HOOK = AsmTypes.HOOK + "HK_" + "GameData";

	public static ClassVisitor appendVisitor(ClassVisitor cv, String className) {
		if (!TARGET.equals(className))
			return cv;
		ClassVisitor newcv = new MyClassVisitor(cv, className) {
			@Override
			public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
				MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
				if (name.equals("loadFrozenDataToStagingRegistry")) {
					mv = InjectInstructionsAdapter.after(mv, name
							, Instructions.create()
									.invokeInterface("java.util.stream.Stream", "forEach", AsmUtil.composeRuntimeMethodDesc(AsmTypes.VOID, "java.util.function.Consumer"))
							, Instructions.create()
									.aload(1)
									.aload(5)
									.aload(6)
									.invokeStatic(HOOK, "onLoadFrozenDataToStagingRegistry", AsmUtil.composeRuntimeMethodDesc(AsmTypes.VOID, "net.minecraft.util.ResourceLocation", "net.minecraftforge.registries.ForgeRegistry", "java.util.Map"))
					);
					success();
				}
				if (name.equals("loadPersistentDataToStagingRegistry")) {
					mv = InjectInstructionsAdapter.before(mv, name
							, Instructions.create()
									.aload(8)
									.aload(5)
							, Instructions.create()
									.aload(4)
									.aload(8)
									.invokeStatic(HOOK, "onLoadPersistentDataToStagingRegistry", AsmUtil.composeRuntimeMethodDesc(AsmTypes.VOID, "net.minecraft.util.ResourceLocation", "net.minecraftforge.registries.ForgeRegistry"))
					);
					success();
				}
				return mv;
			}
		}.setSuccessExpected(2);
		return newcv;
	}
}
