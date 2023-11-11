package kpan.ig_custom_stuff.asm.tf;

import kpan.ig_custom_stuff.asm.core.AsmTypes;
import kpan.ig_custom_stuff.asm.core.AsmUtil;
import kpan.ig_custom_stuff.asm.core.adapters.InjectInstructionsAdapter;
import kpan.ig_custom_stuff.asm.core.adapters.Instructions;
import kpan.ig_custom_stuff.asm.core.adapters.MyClassVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

public class TF_WeightedRandomModel {

	private static final String TARGET = "net.minecraftforge.client.model.ModelLoader$WeightedRandomModel";
	private static final String HOOK = AsmTypes.HOOK + "HK_" + "WeightedRandomModel";

	public static ClassVisitor appendVisitor(ClassVisitor cv, String className) {
		if (!TARGET.equals(className))
			return cv;
		ClassVisitor newcv = new MyClassVisitor(cv, className) {
			@Override
			public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
				MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
				if (name.equals("<init>") && desc.contains(AsmUtil.runtimeDesc(AsmUtil.toDesc("net.minecraft.util.ResourceLocation")))) {
					mv = InjectInstructionsAdapter.injectBeforeReturns(mv, name
							, Instructions.create()
									.aload(1)
									.aload(2)
									.invokeStatic(HOOK, "onWeightedRandomModelConstructed", AsmUtil.composeRuntimeMethodDesc(AsmTypes.VOID, "net.minecraft.util.ResourceLocation", "net.minecraft.client.renderer.block.model.VariantList"))
					);
					success();
				}
				return mv;
			}
		};
		return newcv;
	}
}
