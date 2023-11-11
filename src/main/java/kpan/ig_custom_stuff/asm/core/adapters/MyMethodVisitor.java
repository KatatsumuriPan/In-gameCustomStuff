package kpan.ig_custom_stuff.asm.core.adapters;

import com.google.common.collect.HashBiMap;
import kpan.ig_custom_stuff.asm.core.AsmUtil;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.TypePath;

import javax.annotation.Nullable;

public class MyMethodVisitor extends MethodVisitor {

	private final String nameForDebug;
	private int success = 0;
	private int successExpectedMinInclusive;
	private int successExpectedMaxInclusive;

	protected final HashBiMap<Label, Integer> labels = HashBiMap.create();

	public MyMethodVisitor(MethodVisitor mv, String nameForDebug) { this(mv, nameForDebug, 1); }
	public MyMethodVisitor(MethodVisitor mv, String nameForDebug, int successExpected) { this(mv, nameForDebug, successExpected, successExpected); }
	public MyMethodVisitor(MethodVisitor mv, String nameForDebug, int successExpectedMinInclusive, int successExpectedMaxInclusive) {
		super(AsmUtil.ASM_VER, mv);
		this.nameForDebug = nameForDebug;
		this.successExpectedMinInclusive = Math.max(successExpectedMinInclusive, 0);
		this.successExpectedMaxInclusive = Math.max(successExpectedMaxInclusive, 0);
	}
	public MyMethodVisitor setSuccessExpected(int successExpected) {
		setSuccessExpectedMin(successExpected);
		setSuccessExpectedMax(successExpected);
		return this;
	}
	public MyMethodVisitor setSuccessExpectedMin(int minInclusive) {
		successExpectedMinInclusive = Math.max(minInclusive, 0);
		return this;
	}
	public MyMethodVisitor setSuccessExpectedMax(int maxInclusive) {
		successExpectedMaxInclusive = Math.max(maxInclusive, 0);
		return this;
	}
	protected void success() {
		success++;
	}
	public int getSuccess() {
		return success;
	}
	@Nullable
	public Label tryGetLabel(int index) { return labels.inverse().get(index); }
	public Label getLabel(int index) {
		Label label = tryGetLabel(index);
		if (label == null)
			throw new RuntimeException("Label：L" + index + " is not found.");
		return label;
	}

	@Override
	public void visitJumpInsn(int opcode, Label label) {
		updateLabels(label);
		super.visitJumpInsn(opcode, label);
	}
	@Override
	public void visitLabel(Label label) {
		updateLabels(label);
		super.visitLabel(label);
	}
	@Override
	public void visitLineNumber(int line, Label start) {
		updateLabels(start);
		super.visitLineNumber(line, start);
	}
	@Override
	public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
		updateLabels(start);
		updateLabels(end);
		super.visitLocalVariable(name, desc, signature, start, end, index);
	}
	@Override
	public AnnotationVisitor visitLocalVariableAnnotation(int typeRef, TypePath typePath, Label[] start, Label[] end, int[] index, String desc, boolean visible) {
		for (int i = 0; i < start.length; i++) {
			updateLabels(start[i]);
			updateLabels(end[i]);
		}
		return super.visitLocalVariableAnnotation(typeRef, typePath, start, end, index, desc, visible);
	}
	@Override
	public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
		for (Label label : labels) {
			updateLabels(label);
		}
		updateLabels(dflt);
		super.visitLookupSwitchInsn(dflt, keys, labels);
	}
	@Override
	public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
		for (Label label : labels) {
			updateLabels(label);
		}
		updateLabels(dflt);
		super.visitTableSwitchInsn(min, max, dflt, labels);
	}
	@Override
	public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
		updateLabels(start);
		updateLabels(end);
		updateLabels(handler);
		super.visitTryCatchBlock(start, end, handler, type);
	}

	@Override
	public void visitEnd() {
		super.visitEnd();
		if (success < successExpectedMinInclusive || success > successExpectedMaxInclusive) {
			if (successExpectedMinInclusive == successExpectedMaxInclusive)
				throw new RuntimeException("transform failed:" + nameForDebug + "\nexpected:" + successExpectedMinInclusive + "\nactual:" + success);
			else if (successExpectedMaxInclusive == Integer.MAX_VALUE)
				throw new RuntimeException("transform failed:" + nameForDebug + "\nexpected: " + successExpectedMinInclusive + "~\nactual:" + success);
			else
				throw new RuntimeException("transform failed:" + nameForDebug + "\nexpected: " + successExpectedMinInclusive + "~" + successExpectedMaxInclusive + "\nactual:" + success);
		}
	}

	protected void updateLabels(Label label) {
		if (!labels.containsKey(label)) {
			labels.put(label, labels.size());
		}
	}
}
