package kpan.ig_custom_stuff.asm.tf;

import kpan.ig_custom_stuff.asm.core.AsmTypes;
import kpan.ig_custom_stuff.asm.core.AsmUtil;
import kpan.ig_custom_stuff.asm.core.MyAsmNameRemapper.MethodRemap;
import kpan.ig_custom_stuff.asm.core.adapters.Instructions;
import kpan.ig_custom_stuff.asm.core.adapters.Instructions.OpcodeInt;
import kpan.ig_custom_stuff.asm.core.adapters.MyClassVisitor;
import kpan.ig_custom_stuff.asm.core.adapters.ReplaceInstructionsAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

public class TF_CPacketCustomPayload {

	private static final String TARGET = "net.minecraft.network.play.client.CPacketCustomPayload";
	private static final String HOOK = AsmTypes.HOOK + "HK_" + "CPacketCustomPayload";

	private static final MethodRemap readPacketData = new MethodRemap(TARGET, "readPacketData", AsmUtil.toMethodDesc(AsmTypes.VOID, "net.minecraft.network.PacketBuffer"), "func_148837_a");

	public static ClassVisitor appendVisitor(ClassVisitor cv, String className) {
		if (!TARGET.equals(className))
			return cv;
		ClassVisitor newcv = new MyClassVisitor(cv, className) {
			@Override
			public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
				MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
				if (name.equals("<init>") && !desc.equals(AsmTypes.METHOD_VOID)) {
					mv = new ReplaceInstructionsAdapter(mv, name
							, Instructions.create()
							.intInsn(OpcodeInt.SIPUSH, 32767)
							, Instructions.create()
							.invokeStatic(HOOK, "getMaxPacketSize", AsmUtil.composeRuntimeMethodDesc(AsmTypes.INT))
					);
					success();
				}
				if (readPacketData.isTarget(name, desc)) {
					mv = new ReplaceInstructionsAdapter(mv, name
							, Instructions.create()
							.intInsn(OpcodeInt.SIPUSH, 32767)
							, Instructions.create()
							.invokeStatic(HOOK, "getMaxPacketSize", AsmUtil.composeRuntimeMethodDesc(AsmTypes.INT))
					);
					success();
				}
				return mv;
			}
		}.setSuccessExpectedMin(1).setSuccessExpectedMax(2);
		return newcv;
	}
}
