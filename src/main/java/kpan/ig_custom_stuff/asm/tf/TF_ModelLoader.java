package kpan.ig_custom_stuff.asm.tf;

import kpan.ig_custom_stuff.asm.core.AsmTypes;
import kpan.ig_custom_stuff.asm.core.AsmUtil;
import kpan.ig_custom_stuff.asm.core.MyAsmNameRemapper.MethodRemap;
import kpan.ig_custom_stuff.asm.core.adapters.InjectInstructionsAdapter;
import kpan.ig_custom_stuff.asm.core.adapters.Instructions;
import kpan.ig_custom_stuff.asm.core.adapters.MyClassVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Collection;

public class TF_ModelLoader {

	private static final String TARGET = "net.minecraftforge.client.model.ModelLoader";
	private static final String HOOK = AsmTypes.HOOK + "HK_" + "ModelLoader";

	private static final MethodRemap setupModelRegistry = new MethodRemap(TARGET, "setupModelRegistry", AsmUtil.toMethodDesc("net.minecraft.util.registry.IRegistry"), "func_177570_a");
	private static final MethodRemap loadVariantItemModels = new MethodRemap(TARGET, "loadVariantItemModels", AsmTypes.METHOD_VOID, "func_177577_b");

	public static ClassVisitor appendVisitor(ClassVisitor cv, String className) {
		if (!TARGET.equals(className))
			return cv;
		ClassVisitor newcv = new MyClassVisitor(cv, className) {
			@Override
			public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
				MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
				if (setupModelRegistry.isTarget(name, desc)) {
					mv = InjectInstructionsAdapter.after(mv, name
							, Instructions.create()
									.invokeVirtual(loadVariantItemModels)
							, Instructions.create()
									.aload(0)
									.invokeStatic(HOOK, "postLoadVariantItemModels", AsmUtil.composeRuntimeMethodDesc(AsmTypes.VOID, AsmTypes.OBJECT))
					);
					mv = InjectInstructionsAdapter.after(mv, name
							, Instructions.create()
									.invokeInterface(AsmTypes.SET, "addAll", AsmUtil.composeRuntimeMethodDesc(AsmTypes.BOOL, Collection.class.getName()))
									.insn(Opcodes.POP)
							, Instructions.create()
									.aload(1)
									.invokeStatic(HOOK, "addDefaultTextures", AsmUtil.composeRuntimeMethodDesc(AsmTypes.VOID, AsmTypes.SET))
					);
					success();
				}
				return mv;
			}
		};
		return newcv;
	}
}
