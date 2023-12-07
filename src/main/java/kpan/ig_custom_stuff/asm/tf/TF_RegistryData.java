package kpan.ig_custom_stuff.asm.tf;

import io.netty.buffer.ByteBuf;
import kpan.ig_custom_stuff.asm.core.AsmTypes;
import kpan.ig_custom_stuff.asm.core.AsmUtil;
import kpan.ig_custom_stuff.asm.core.adapters.InjectInstructionsAdapter;
import kpan.ig_custom_stuff.asm.core.adapters.Instructions;
import kpan.ig_custom_stuff.asm.core.adapters.MixinAccessorAdapter;
import kpan.ig_custom_stuff.asm.core.adapters.MyClassVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

public class TF_RegistryData {

	private static final String TARGET = "net.minecraftforge.fml.common.network.handshake.FMLHandshakeMessage$RegistryData";
	private static final String HOOK = AsmTypes.HOOK + "HK_" + "RegistryData";
	private static final String ACC = AsmTypes.ACC + "ACC_" + "RegistryData";

	public static ClassVisitor appendVisitor(ClassVisitor cv, String className) {
		if (!TARGET.equals(className))
			return cv;
		ClassVisitor newcv = new MyClassVisitor(cv, className) {
			@Override
			public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
				MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
				if (name.equals("fromBytes")) {
					mv = InjectInstructionsAdapter.injectBeforeReturns(mv, name
							, Instructions.create()
									.aload(0)
									.aload(1)
									.invokeStatic(HOOK, "fromBytes", AsmUtil.composeRuntimeMethodDesc(AsmTypes.VOID, TARGET, AsmUtil.toDesc(ByteBuf.class)))
					);
					success();
				}
				if (name.equals("toBytes")) {
					mv = InjectInstructionsAdapter.injectBeforeReturns(mv, name
							, Instructions.create()
									.aload(0)
									.aload(1)
									.invokeStatic(HOOK, "toBytes", AsmUtil.composeRuntimeMethodDesc(AsmTypes.VOID, TARGET, AsmUtil.toDesc(ByteBuf.class)))
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
