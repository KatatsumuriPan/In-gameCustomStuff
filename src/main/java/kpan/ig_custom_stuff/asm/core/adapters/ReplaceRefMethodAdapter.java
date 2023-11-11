package kpan.ig_custom_stuff.asm.core.adapters;

import kpan.ig_custom_stuff.asm.core.AsmTypes.MethodDesc;
import kpan.ig_custom_stuff.asm.core.AsmUtil;
import kpan.ig_custom_stuff.asm.core.MyAsmNameRemapper;
import kpan.ig_custom_stuff.asm.core.MyAsmNameRemapper.MethodRemap;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class ReplaceRefMethodAdapter extends ReplaceMethodAdapter {

	private final String runtimeClassForRefMethodParam;
	private final String originalName;
	private final String refClass;
	private final String runtimeReturnType;
	private final String[] runtimeParams;

	public ReplaceRefMethodAdapter(ClassVisitor cv, String refClass, MethodRemap method) {
		super(cv, method);
		originalName = method.mcpMethodName;
		runtimeClassForRefMethodParam = MyAsmNameRemapper.runtimeClass(method.deobfOwner);
		this.refClass = refClass;
		MethodDesc md = MethodDesc.fromMethodDesc(AsmUtil.runtimeDesc(method.deobfMethodDesc));
		runtimeReturnType = md.returnDesc;
		runtimeParams = md.paramsDesc;
	}
	public ReplaceRefMethodAdapter(ClassVisitor cv, String refClass, String runtimeClassForRefMethodParam, String runtimeMethodName, String runtimeDesc) {
		super(cv, runtimeMethodName, runtimeDesc);
		originalName = runtimeMethodName;
		this.runtimeClassForRefMethodParam = runtimeClassForRefMethodParam;
		this.refClass = refClass;
		MethodDesc md = MethodDesc.fromMethodDesc(runtimeDesc);
		runtimeReturnType = md.returnDesc;
		runtimeParams = md.paramsDesc;
	}

	@Override
	protected void methodBody(MethodVisitor mv) {
		boolean is_static = (access & Opcodes.ACC_STATIC) != 0;
		int offset = 0;

		//this
		if (!is_static) {
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			offset = 1;
		}

		//params
		AsmUtil.loadLocals(mv, runtimeParams, offset);

		//invoke
		if (is_static)
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, refClass, originalName, runtimeDesc, false);
		else
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, refClass, originalName, AsmUtil.toMethodDesc(runtimeReturnType, runtimeClassForRefMethodParam, runtimeParams), false);

		//return
		mv.visitInsn(AsmUtil.toReturnOpcode(runtimeReturnType));
	}

}
