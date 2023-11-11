package kpan.ig_custom_stuff.asm.core.adapters;

import com.google.common.collect.Lists;
import kpan.ig_custom_stuff.asm.core.adapters.Instructions.Instr;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import javax.annotation.Nonnull;
import java.util.ArrayList;

public class ReplaceInstructionsAdapter extends MyMethodVisitor {

	protected final Instructions targets;
	protected final Instructions instructions;
	protected final ArrayList<Instr> holds = Lists.newArrayList();//Java7との互換性のために、ダイヤモンド演算子を使用してない

	private int maxMatched = 0;

	public ReplaceInstructionsAdapter(@Nonnull MethodVisitor mv, String name, Instructions targets, Instructions instructions) {
		super(mv, name);
		this.targets = targets;
		this.instructions = instructions;
	}

	protected final boolean check(Instr instr) {
		Instr instr2 = targets.get(holds.size());
		instr2.solveLabel(this);
		if (instr.equals(instr2)) {
			holds.add(instr);
			if (holds.size() == targets.size()) {
				visitAllInstructions();
				holds.clear();
				success();
			}
			return true;
		} else {
			for (int start = 1; start < holds.size(); start++) {
				if (extracted(start)) {
					for (int i = 0; i < start; i++) {
						holds.get(i).visit(mv, this);
					}
					holds.subList(0, start).clear();
					return true;
				}
			}
			flushVisits();
			return false;
		}
	}

	private boolean extracted(int start) {
		for (int i = 0; i < holds.size() - start; i++) {
			Instr instr = holds.get(start + i);
			if (!instr.equals(targets.get(i)))
				return false;
		}
		return true;
	}

	protected void visitAllInstructions() {
		for (Instr instruction : instructions) {
			instruction.visit(mv, this);
		}
	}

	protected final void flushVisits() {
		maxMatched = Math.max(maxMatched, holds.size());
		for (Instr element : holds) {
			element.visit(mv, this);
		}
		holds.clear();
	}

	@Override
	public void visitEnd() {
		flushVisits();
		try {
			super.visitEnd();
		} catch (Exception e) {
			System.out.println("maxMatch:" + maxMatched);
			throw e;
		}
	}
	@Override
	public void visitFieldInsn(int opcode, String owner, String name, String desc) {
		if (!check(Instr.fieldInsn(opcode, owner, name, desc)))
			super.visitFieldInsn(opcode, owner, name, desc);
	}
	@Override
	public void visitIincInsn(int var, int increment) {
		if (!check(Instr.iincInsn(var, increment)))
			super.visitIincInsn(var, increment);
	}
	@Override
	public void visitInsn(int opcode) {
		if (!check(Instr.insn(opcode)))
			super.visitInsn(opcode);
	}
	@Override
	public void visitIntInsn(int opcode, int operand) {
		if (!check(Instr.intInsn(opcode, operand)))
			super.visitIntInsn(opcode, operand);
	}
	@Override
	public void visitInvokeDynamicInsn(String name, String desc, Handle bsm, Object... bsmArgs) {
		if (!check(Instr.dynamicInsn(name, desc, bsm, bsmArgs)))
			super.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
	}
	@Override
	public void visitJumpInsn(int opcode, Label label) {
		updateLabels(label);
		if (!check(Instr.jumpInsn(opcode, label))) {
			if (mv != null)
				mv.visitJumpInsn(opcode, label);
		}
	}
	@Override
	public void visitLabel(Label label) {
		updateLabels(label);
		if (!check(Instr.label(label))) {
			if (mv != null)
				super.visitLabel(label);
		}
	}
	@Override
	public void visitLdcInsn(Object cst) {
		if (!check(Instr.ldcInsn(cst)))
			super.visitLdcInsn(cst);
	}
	@Override
	public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
		//これは最後のLOCALVARIABLEで呼ばれる
		flushVisits();
		super.visitLocalVariable(name, desc, signature, start, end, index);
	}
	@Override
	public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
		flushVisits();
		super.visitLookupSwitchInsn(dflt, keys, labels);
	}
	@Override
	public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
		if (api < Opcodes.ASM5 || !check(Instr.methodInsn(opcode, owner, name, desc, itf)))
			super.visitMethodInsn(opcode, owner, name, desc, itf);
	}
	@SuppressWarnings("deprecation")
	@Override
	public void visitMethodInsn(int opcode, String owner, String name, String desc) {
		if (api >= Opcodes.ASM5 || !check(Instr.methodInsn(opcode, owner, name, desc, opcode == Opcodes.INVOKEINTERFACE)))
			super.visitMethodInsn(opcode, owner, name, desc);
	}
	@Override
	public void visitMultiANewArrayInsn(String desc, int dims) {
		flushVisits();
		super.visitMultiANewArrayInsn(desc, dims);
	}
	@Override
	public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
		flushVisits();
		super.visitTableSwitchInsn(min, max, dflt, labels);
	}
	@Override
	public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
		flushVisits();
		super.visitTryCatchBlock(start, end, handler, type);
	}
	@Override
	public void visitTypeInsn(int opcode, String type) {
		if (!check(Instr.typeInsn(opcode, type)))
			super.visitTypeInsn(opcode, type);
	}
	@Override
	public void visitVarInsn(int opcode, int var) {
		if (!check(Instr.varInsn(opcode, var)))
			super.visitVarInsn(opcode, var);
	}

}
