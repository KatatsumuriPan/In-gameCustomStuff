package kpan.ig_custom_stuff.asm.core;

import org.objectweb.asm.util.Textifier;

import javax.annotation.Nullable;

public class MyTextifier extends Textifier {
	private final @Nullable String methodName;

	public MyTextifier(@Nullable String methodName) {
		super(AsmUtil.ASM_VER);//引数アリじゃないとException投げる
		this.methodName = methodName;
	}
	@Override
	public void visitFieldInsn(int opcode, String owner, String name, String desc) {
		buf.setLength(0);
		buf.append(tab2).append(OPCODES[opcode]).append(' ');
		appendDescriptor(0, owner);
		buf.append('.').append(name).append(" : ");
		appendDescriptor(1, desc);
		if (!AsmUtil.isDeobfEnvironment()) {
			buf.append('\t');
			buf.append("//");
			buf.append(MyAsmNameRemapper.getClassDeobfName(owner));
			buf.append('.');
			buf.append(MyAsmNameRemapper.obf2SrgFieldName(owner, name));
			buf.append(' ');
			buf.append(AsmUtil.deobfDesc(desc));
		}
		buf.append('\n');
		text.add(buf.toString());
	}
	@Override
	public void visitMaxs(int maxStack, int maxLocals) {
		super.visitMaxs(maxStack, maxLocals);
	}
	@Override
	public void visitMethodEnd() {
		if (methodName != null)
			System.out.println("\n" + methodName + "\n" + text.toString());
		else
			System.out.println("\n" + text.toString());
	}

}
