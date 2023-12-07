package kpan.ig_custom_stuff.asm.tf;

import kpan.ig_custom_stuff.asm.core.AsmTypes;
import kpan.ig_custom_stuff.asm.core.AsmUtil;
import kpan.ig_custom_stuff.asm.core.adapters.InjectInstructionsAdapter;
import kpan.ig_custom_stuff.asm.core.adapters.Instructions;
import kpan.ig_custom_stuff.asm.core.adapters.Instructions.OpcodeMethod;
import kpan.ig_custom_stuff.asm.core.adapters.MyClassVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class TF_FMLHandshakeServerState {

	private static final String TARGET = "net.minecraftforge.fml.common.network.handshake.FMLHandshakeServerState$3";
	private static final String HOOK = AsmTypes.HOOK + "HK_" + "FMLHandshakeServerState";

	public static ClassVisitor appendVisitor(ClassVisitor cv, String className) {
		if (!TARGET.equals(className))
			return cv;
		ClassVisitor newcv = new MyClassVisitor(cv, className) {
			@Override
			public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
				MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
				if (name.equals("accept")) {
					mv = InjectInstructionsAdapter.after(mv, name
							, Instructions.create()
									.methodRep(OpcodeMethod.SPECIAL, "net/minecraftforge/fml/common/network/handshake/FMLHandshakeMessage$RegistryData", "<init>")
							, Instructions.create()
									.insn(Opcodes.DUP)
									.invokeStatic(HOOK, "onMessageConstruct", AsmUtil.composeRuntimeMethodDesc(AsmTypes.VOID, "net/minecraftforge/fml/common/network/handshake/FMLHandshakeMessage$RegistryData"))
					);
					success();
				}
				return mv;
			}
		};
		return newcv;
	}
}
