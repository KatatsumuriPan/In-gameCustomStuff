package kpan.ig_custom_stuff.asm.core.adapters;

import kpan.ig_custom_stuff.asm.core.adapters.Instructions.Instr;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

@SuppressWarnings("unused")
public class InjectInstructionsAdapter extends ReplaceInstructionsAdapter {

	private final int injectIndex;

	public InjectInstructionsAdapter(MethodVisitor mv, String name, Instructions targets, Instructions instructions, int injectIndex) {
		super(mv, name, targets, instructions);
		if (injectIndex < 0)
			injectIndex = targets.size() + injectIndex + 1;
		this.injectIndex = injectIndex;
	}

	@Override
	protected void visitAllInstructions() {
		for (int i = 0; i < holds.size(); i++) {
			if (i == injectIndex)
				super.visitAllInstructions();
			holds.get(i).visit(mv, this);
		}
		if (injectIndex >= holds.size())
			super.visitAllInstructions();
	}

	public static InjectInstructionsAdapter before(MethodVisitor mv, String name, Instructions targets, Instructions instructions) {
		return new InjectInstructionsAdapter(mv, name, targets, instructions, 0);
	}

	public static InjectInstructionsAdapter after(MethodVisitor mv, String name, Instructions targets, Instructions instructions) {
		return new InjectInstructionsAdapter(mv, name, targets, instructions, -1);
	}

	public static ReplaceInstructionsAdapter beforeAfter(MethodVisitor mv, String name, Instructions targets, Instructions before, Instructions after) {
		return new InjectInstructionsAdapter(mv, name, targets, before, 0) {
			@Override
			protected void visitAllInstructions() {
				super.visitAllInstructions();
				for (Instr instruction : after) {
					instruction.visit(mv, this);
				}

			}
		};
	}
	public static MethodVisitor injectFirst(MethodVisitor mv, String nameForDebug, final Instructions instructions) {
		return new MyMethodVisitor(mv, nameForDebug) {
			@Override
			public void visitCode() {
				super.visitCode();
				for (Instr instruction : instructions) {
					instruction.visit(mv, this);
				}
				success();
			}
		};
	}
	public static MethodVisitor injectBeforeReturns(MethodVisitor mv, String nameForDebug, final Instructions instructions) {
		return new MyMethodVisitor(mv, nameForDebug) {
			@Override
			public void visitInsn(int opcode) {
				if (opcode == Opcodes.IRETURN || opcode == Opcodes.LRETURN || opcode == Opcodes.FRETURN || opcode == Opcodes.DRETURN || opcode == Opcodes.ARETURN || opcode == Opcodes.RETURN) {
					for (Instr instruction : instructions) {
						instruction.visit(mv, this);
					}
					success();
				}
				super.visitInsn(opcode);
			}

			@Override
			public void visitEnd() {
				//RETURN���������Ă��ǂ��̂ŃV���[�g�J�b�g
				if (mv != null) {
					mv.visitEnd();
				}
			}
		};
	}
}
