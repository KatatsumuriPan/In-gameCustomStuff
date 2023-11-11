package kpan.ig_custom_stuff.asm.core.adapters;

import kpan.ig_custom_stuff.asm.core.AsmUtil;
import kpan.ig_custom_stuff.asm.core.MyAsmNameRemapper;
import kpan.ig_custom_stuff.asm.core.MyAsmNameRemapper.MethodRemap;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Objects;

public abstract class ReplaceMethodAdapter extends MyClassVisitor {

	protected final String runtimeName;
	protected final String runtimeDesc;
	protected int access;
	@Nullable
	protected String runtimeGenerics;
	@Nullable
	protected String[] runtimeExceptions;//RuntimeExceptionではなくExceptionのRuntime名
	protected boolean useAccess = false;
	protected boolean useGenerics = false;
	protected boolean useExceptions = false;

	private boolean found = false;

	public ReplaceMethodAdapter(ClassVisitor cv, MethodRemap method) {
		super(cv, method.mcpMethodName + " " + method.deobfMethodDesc);
		runtimeName = MyAsmNameRemapper.runtimeMethod(method);
		runtimeDesc = AsmUtil.runtimeDesc(method.deobfMethodDesc);
	}
	public ReplaceMethodAdapter(ClassVisitor cv, String runtimeMethodName, String runtimeDesc) {
		super(cv, runtimeMethodName + " " + runtimeDesc);
		runtimeName = runtimeMethodName;
		this.runtimeDesc = runtimeDesc;
	}
	@SuppressWarnings("unused")
	public void setAccess(int access) {
		this.access = access;
		useAccess = true;
	}
	@SuppressWarnings("unused")
	public void setGenerics(String deobfGenerics) {
		runtimeGenerics = AsmUtil.runtimeMethodGenerics(deobfGenerics);
		useGenerics = true;
	}
	@SuppressWarnings("unused")
	public void setExceptions(String[] deobfExceptions) {
		runtimeExceptions = AsmUtil.runtimeExceptions(deobfExceptions);
		useExceptions = true;
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		if (isTarget(access, name, desc, signature, exceptions)) {
			found = true;
			if (!useAccess)
				this.access = access;
			if (!useGenerics)
				runtimeGenerics = signature;
			if (!useExceptions)
				runtimeExceptions = exceptions;
			return null;//既存のを削除、visitEndで追加
		} else
			return super.visitMethod(access, name, desc, signature, exceptions);
	}

	@Override
	public void visitEnd() {
		if (found) {
			MethodVisitor mv = super.visitMethod(access, runtimeName, runtimeDesc, runtimeGenerics, runtimeExceptions);
			if (mv != null) {
				mv.visitCode();
				methodBody(mv);
				mv.visitMaxs(0, 0);//引数は無視され、再計算される(Write時に再計算されるのでtrace時点では0,0のまま)
				mv.visitEnd();
			}
			success();
		}
		super.visitEnd();
	}

	protected abstract void methodBody(MethodVisitor mv);

	private boolean isTarget(int access, String name, String desc, String signature, String[] exceptions) {
		if (!name.equals(runtimeName))
			return false;

		if (runtimeDesc != null && !desc.equals(runtimeDesc))
			return false;

		if (useAccess) {
			if (access != this.access)
				return false;
		}
		if (useGenerics) {
			if (!equals(signature, runtimeGenerics))
				return false;
		}
		if (useExceptions) {
			if (!Arrays.equals(exceptions, runtimeExceptions))
				return false;
		}
		return true;
	}
	//Java7との互換性のために、Objects.equalsを使用してない
	private static boolean equals(Object a, Object b) {
		return Objects.equals(a, b);
	}

}
