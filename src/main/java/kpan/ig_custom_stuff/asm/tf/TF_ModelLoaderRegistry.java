package kpan.ig_custom_stuff.asm.tf;

import kpan.ig_custom_stuff.asm.core.AsmTypes;
import kpan.ig_custom_stuff.asm.core.AsmUtil;
import kpan.ig_custom_stuff.asm.core.adapters.InjectInstructionsAdapter;
import kpan.ig_custom_stuff.asm.core.adapters.Instructions;
import kpan.ig_custom_stuff.asm.core.adapters.Instructions.OpcodeMethod;
import kpan.ig_custom_stuff.asm.core.adapters.MyClassVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

public class TF_ModelLoaderRegistry {

	private static final String TARGET = "net.minecraftforge.client.model.ModelLoaderRegistry";
	private static final String HOOK = AsmTypes.HOOK + "HK_" + "ModelLoaderRegistry";

	public static ClassVisitor appendVisitor(ClassVisitor cv, String className) {
		if (!TARGET.equals(className))
			return cv;
		ClassVisitor newcv = new MyClassVisitor(cv, className) {
			@Override
			public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
				MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
				if (name.equals("getModel")) {
					mv = InjectInstructionsAdapter.before(mv, name
							, Instructions.create()
									.methodRep(OpcodeMethod.INTERFACE, "net.minecraftforge.client.model.IModel", "getTextures")
							, Instructions.create()
									.aload(0)
									.aload(1)
									.invokeStatic(HOOK, "onModelLoaded", AsmUtil.composeRuntimeMethodDesc(AsmTypes.VOID, "net.minecraft.util.ResourceLocation", "net.minecraftforge.client.model.IModel"))
					);
					success();
				}
				return mv;
			}
		};
		return newcv;
	}
}
