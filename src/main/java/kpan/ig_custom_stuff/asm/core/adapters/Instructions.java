package kpan.ig_custom_stuff.asm.core.adapters;

import com.google.common.collect.Lists;
import kpan.ig_custom_stuff.asm.core.AsmUtil;
import kpan.ig_custom_stuff.asm.core.MyAsmNameRemapper;
import kpan.ig_custom_stuff.asm.core.MyAsmNameRemapper.FieldRemap;
import kpan.ig_custom_stuff.asm.core.MyAsmNameRemapper.MethodRemap;
import kpan.ig_custom_stuff.asm.core.adapters.Instructions.Instr;
import org.apache.commons.lang3.StringUtils;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

@SuppressWarnings("unused")
public class Instructions implements List<Instr> {
	private final List<Instr> instructions;

	public Instructions() { this(Lists.newArrayList()); }
	public Instructions(List<Instr> instructions) { this.instructions = instructions; }

	public Instructions addInstr(Instr instr) {
		add(instr);
		return this;
	}
	public Instructions fieldInsn(OpcodeField opcode, String runtimeOwner, String runtimeName, String runtimeDesc) {
		return addInstr(Instr.fieldInsn(opcode, runtimeOwner, runtimeName, runtimeDesc));
	}
	public Instructions fieldInsn(OpcodeField opcode, FieldRemap field) {
		return addInstr(Instr.fieldInsn(opcode, field));
	}
	public Instructions getField(FieldRemap field) {
		return fieldInsn(OpcodeField.GET, field);
	}
	public Instructions getField(String runtimeOwner, String runtimeName, String runtimeDesc) {
		return fieldInsn(OpcodeField.GET, runtimeOwner, runtimeName, runtimeDesc);
	}
	public Instructions putField(FieldRemap field) {
		return fieldInsn(OpcodeField.PUT, field);
	}
	public Instructions putField(String runtimeOwner, String runtimeName, String runtimeDesc) {
		return fieldInsn(OpcodeField.PUT, runtimeOwner, runtimeName, runtimeDesc);
	}
	public Instructions getStatic(FieldRemap field) {
		return fieldInsn(OpcodeField.GETSTATIC, field);
	}
	public Instructions getStatic(String runtimeOwner, String runtimeName, String runtimeDesc) {
		return fieldInsn(OpcodeField.GETSTATIC, runtimeOwner, runtimeName, runtimeDesc);
	}
	public Instructions putStatic(FieldRemap field) {
		return fieldInsn(OpcodeField.PUTSTATIC, field);
	}
	public Instructions putStatic(String runtimeOwner, String runtimeName, String runtimeDesc) {
		return fieldInsn(OpcodeField.PUTSTATIC, runtimeOwner, runtimeName, runtimeDesc);
	}
	public Instructions iincInsn(int var, int increment) { return addInstr(Instr.iincInsn(var, increment)); }
	public Instructions intInsn(OpcodeInt opcode, int operand) { return addInstr(Instr.intInsn(opcode, operand)); }
	public Instructions insn(int opcode) { return addInstr(Instr.insn(opcode)); }
	public Instructions jumpInsn(OpcodeJump opcode, Label label) { return addInstr(Instr.jumpInsn(opcode, label)); }
	public Instructions jumpInsn(OpcodeJump opcode, int labelIndex) { return addInstr(Instr.jumpInsn(opcode, labelIndex)); }
	public Instructions jumpRep() { return addInstr(Instr.jumpRep()); }
	public Instructions label(Label label) { return addInstr(Instr.label(label)); }
	public Instructions label(int labelIndex) { return addInstr(Instr.label(labelIndex)); }
	public Instructions labelRep() { return addInstr(Instr.labelRep()); }
	public Instructions ldcInsn(Object cst) { return addInstr(Instr.ldcInsn(cst)); }
	public Instructions ldcRep() { return addInstr(Instr.ldcRep()); }
	public Instructions typeInsn(int opcode, String type) { return addInstr(Instr.typeInsn(opcode, type)); }
	public Instructions methodInsn(OpcodeMethod opcode, MethodRemap method) {
		return addInstr(Instr.methodInsn(opcode, method));
	}
	public Instructions methodInsn(OpcodeMethod opcode, String runtimeOwner, String runtimeName, String runtimeMethodDesc) {
		return addInstr(Instr.methodInsn(opcode, runtimeOwner, runtimeName, runtimeMethodDesc));
	}
	public Instructions invokeVirtual(MethodRemap method) {
		return methodInsn(OpcodeMethod.VIRTUAL, method);
	}
	public Instructions invokeVirtual(String runtimeOwner, String runtimeName, String runtimeMethodDesc) {
		return methodInsn(OpcodeMethod.VIRTUAL, runtimeOwner, runtimeName, runtimeMethodDesc);
	}
	public Instructions invokeStatic(MethodRemap method) {
		return methodInsn(OpcodeMethod.STATIC, method);
	}
	public Instructions invokeStatic(String runtimeOwner, String runtimeName, String runtimeMethodDesc) {
		return methodInsn(OpcodeMethod.STATIC, runtimeOwner, runtimeName, runtimeMethodDesc);
	}
	public Instructions invokeInterface(MethodRemap method) {
		return methodInsn(OpcodeMethod.INTERFACE, method);
	}
	public Instructions invokeInterface(String runtimeOwner, String runtimeName, String runtimeMethodDesc) {
		return methodInsn(OpcodeMethod.INTERFACE, runtimeOwner, runtimeName, runtimeMethodDesc);
	}
	public Instructions invokespecial(MethodRemap method) {
		return methodInsn(OpcodeMethod.SPECIAL, method);
	}
	public Instructions invokespecial(String runtimeOwner, String runtimeName, String runtimeMethodDesc) {
		return methodInsn(OpcodeMethod.SPECIAL, runtimeOwner, runtimeName, runtimeMethodDesc);
	}
	public Instructions methodRep(OpcodeMethod opcode, String runtimeOwner, String runtimeName) {
		return addInstr(new Instr.InvokeRep(opcode, runtimeOwner, runtimeName));
	}
	public Instructions varInsn(OpcodeVar opcode, int varIndex) {
		return addInstr(Instr.varInsn(opcode, varIndex));
	}
	public Instructions iload(int varIndex) { return varInsn(OpcodeVar.ILOAD, varIndex); }
	public Instructions lload(int varIndex) { return varInsn(OpcodeVar.LLOAD, varIndex); }
	public Instructions fload(int varIndex) { return varInsn(OpcodeVar.FLOAD, varIndex); }
	public Instructions dload(int varIndex) { return varInsn(OpcodeVar.DLOAD, varIndex); }
	public Instructions aload(int varIndex) { return varInsn(OpcodeVar.ALOAD, varIndex); }
	public Instructions istore(int varIndex) { return varInsn(OpcodeVar.ISTORE, varIndex); }
	public Instructions lstore(int varIndex) { return varInsn(OpcodeVar.LSTORE, varIndex); }
	public Instructions fstore(int varIndex) { return varInsn(OpcodeVar.FSTORE, varIndex); }
	public Instructions dstore(int varIndex) { return varInsn(OpcodeVar.DSTORE, varIndex); }
	public Instructions astore(int varIndex) { return varInsn(OpcodeVar.ASTORE, varIndex); }


	public Instructions rep() { return addInstr(Instr.REP); }

	public static Instructions create(Instr... instructions) { return new Instructions(Lists.newArrayList(instructions)); }

	public static class Instr {
		public static final Instr REP = new Instr(0, null) {
			@Override
			public void visit(MethodVisitor mv, MyMethodVisitor adapter) { }
			@Override
			protected boolean isRep() { return true; }
			@Override
			protected boolean repEquals(Instr other) { return true; }
		};
		private static final Instr LDC_REP = new Instr(0, VisitType.LDC) {
			@Override
			public void visit(MethodVisitor mv, MyMethodVisitor adapter) { }
			@Override
			protected boolean isRep() { return true; }
			@Override
			protected boolean repEquals(Instr other) { return other.type == VisitType.LDC; }
		};
		private static final Instr LABEL_REP = new Instr(0, VisitType.LABEL) {
			@Override
			public void visit(MethodVisitor mv, MyMethodVisitor adapter) { }
			@Override
			protected boolean isRep() { return true; }
			@Override
			protected boolean repEquals(Instr other) { return other.type == VisitType.LABEL; }
		};
		private static final Instr JUMP_REP = new Instr(0, VisitType.JUMP) {
			@Override
			public void visit(MethodVisitor mv, MyMethodVisitor adapter) { }
			@Override
			protected boolean isRep() { return true; }
			@Override
			protected boolean repEquals(Instr other) { return other.type == VisitType.JUMP; }
		};

		public static class InvokeRep extends Instr {
			private final OpcodeMethod opcode;
			private final String runtimeOwner;
			private final String runtimeMethodName;

			public InvokeRep(OpcodeMethod opcode, String runtimeOwner, String runtimeMethodName) {
				super(opcode.opcode, VisitType.METHOD);
				this.opcode = opcode;
				this.runtimeOwner = runtimeOwner.replace('.', '/');
				this.runtimeMethodName = runtimeMethodName;
			}

			@Override
			public void visit(MethodVisitor mv, MyMethodVisitor adapter) { }
			@Override
			protected boolean isRep() { return true; }
			@Override
			protected boolean repEquals(Instr other) {
				if (other.type != VisitType.METHOD)
					return false;
				if (opcode.opcode != other.opcode)
					return false;
				if (!runtimeOwner.equals(other.params[0]))
					return false;
				return runtimeMethodName.equals(other.params[1]);
			}
			@Override
			public boolean equals(Object o) {
				if (this == o)
					return true;
				if (o == null || getClass() != o.getClass())
					return false;
				InvokeRep other = (InvokeRep) o;
				return opcode == other.opcode && runtimeOwner.equals(other.runtimeOwner) && runtimeMethodName.equals(other.runtimeMethodName);
			}
			@Override
			public int hashCode() {
				return Objects.hash(opcode, runtimeOwner, runtimeMethodName);
			}
		}

		public static Instr dynamicInsn(String runtimeName, String runtimeDesc, Handle bsm, Object... bsmArgs) {
			return new Instr(Opcodes.INVOKEDYNAMIC, VisitType.DYNAMIC, runtimeName, runtimeDesc.replace('.', '/'), bsm, bsmArgs);
		}
		public static Instr fieldInsn(OpcodeField opcode, FieldRemap field) {
			return fieldInsn(opcode.opcode, field);
		}
		public static Instr fieldInsn(OpcodeField opcode, String runtimeOwner, String runtimeName, String runtimeDesc) {
			return fieldInsn(opcode.opcode, runtimeOwner, runtimeName, runtimeDesc);
		}
		public static Instr fieldInsn(int opcode, FieldRemap field) {
			return new Instr(opcode, VisitType.FIELD, field.toRuntime());
		}
		public static Instr fieldInsn(int opcode, String runtimeOwner, String runtimeName, String runtimeDesc) {
			return new Instr(opcode, VisitType.FIELD, runtimeOwner.replace('.', '/'), runtimeName, AsmUtil.toDesc(runtimeDesc));
		}
		public static Instr iincInsn(int var, int increment) {
			return new Instr(Opcodes.IINC, VisitType.IINC, var, increment);
		}
		public static Instr intInsn(OpcodeInt opcode, int operand) {
			return intInsn(opcode.opcode, operand);
		}
		public static Instr intInsn(int opcode, int operand) {
			return new Instr(opcode, VisitType.INT, operand);
		}
		public static Instr insn(int opcode) {
			return new Instr(opcode, VisitType.INSN);
		}
		public static Instr jumpInsn(OpcodeJump opcode, Label label) {
			return jumpInsn(opcode.opcode, label);
		}
		public static Instr jumpInsn(OpcodeJump opcode, int labelIndex) {
			return jumpInsn(opcode.opcode, labelIndex);
		}
		public static Instr jumpInsn(int opcode, Label label) {
			return new Instr(opcode, VisitType.JUMP, label);
		}
		public static Instr jumpInsn(int opcode, int labelIndex) {
			return new Instr(opcode, VisitType.JUMP, labelIndex);
		}
		public static Instr jumpRep() {
			return JUMP_REP;
		}
		public static Instr label(Label label) {
			return new Instr(0, VisitType.LABEL, label);
		}
		public static Instr label(int labelIndex) {
			return new Instr(0, VisitType.LABEL, labelIndex);
		}
		public static Instr labelRep() {
			return LABEL_REP;
		}
		public static Instr ldcInsn(Object cst) {
			return new Instr(0, VisitType.LDC, cst);
		}
		public static Instr ldcRep() {
			return LDC_REP;
		}
		public static Instr typeInsn(int opcode, String type) {
			return new Instr(opcode, VisitType.TYPE, MyAsmNameRemapper.runtimeClass(type));
		}
		public static Instr methodInsn(OpcodeMethod opcode, MethodRemap method) {
			if (opcode == OpcodeMethod.INTERFACE)
				return methodInsn(opcode.opcode, method, true);
			else
				return methodInsn(opcode.opcode, method);
		}
		public static Instr methodInsn(OpcodeMethod opcode, String runtimeOwner, String runtimeName, String runtimeMethodDesc) {
			if (opcode == OpcodeMethod.INTERFACE)
				return methodInsn(opcode.opcode, runtimeOwner, runtimeName, runtimeMethodDesc, true);
			else
				return methodInsn(opcode.opcode, runtimeOwner, runtimeName, runtimeMethodDesc);
		}
		public static Instr methodInsn(int opcode, MethodRemap method) {
			return methodInsn(opcode, method, false);
		}
		public static Instr methodInsn(int opcode, String runtimeOwner, String runtimeName, String runtimeMethodDesc) {
			return methodInsn(opcode, runtimeOwner, runtimeName, runtimeMethodDesc, false);
		}
		public static Instr methodInsn(int opcode, MethodRemap method, boolean interfaceCall) {
			Object[] array = new Object[4];
			System.arraycopy(method.toRuntime(), 0, array, 0, 3);
			array[3] = interfaceCall;
			return new Instr(opcode, VisitType.METHOD, array);
		}
		public static Instr methodInsn(int opcode, String runtimeOwner, String runtimeName, String runtimeMethodDesc, boolean interfaceCall) {
			return new Instr(opcode, VisitType.METHOD, runtimeOwner.replace('.', '/'), runtimeName, runtimeMethodDesc.replace('.', '/'), interfaceCall);
		}
		public static Instr varInsn(OpcodeVar opcode, int varIndex) {
			return varInsn(opcode.opcode, varIndex);
		}
		public static Instr varInsn(int opcode, int varIndex) {
			return new Instr(opcode, VisitType.VAR, varIndex);
		}

		private final int opcode;
		private final Object[] params;
		private final VisitType type;

		Instr(int opcode, VisitType type, Object... params) {
			this.opcode = opcode;
			this.type = type;
			this.params = params;
		}

		public void visit(@Nullable MethodVisitor mv, @Nullable MyMethodVisitor adapter) {
			if (mv == null)
				return;
			switch (type) {
				case DYNAMIC:
					mv.visitInvokeDynamicInsn((String) params[0], (String) params[1], (Handle) params[2], (Object[]) params[3]);
					break;
				case FIELD:
					mv.visitFieldInsn(opcode, (String) params[0], (String) params[1], (String) params[2]);
					break;
				case IINC:
					mv.visitIincInsn((Integer) params[0], (Integer) params[1]);//なんかのエラーを防ぐためにintではなくInteger
					break;
				case INT:
					mv.visitIntInsn(opcode, (Integer) params[0]);
					break;
				case INSN:
					mv.visitInsn(opcode);
					break;
				case JUMP:
					if (params[0] instanceof Label) {
						mv.visitJumpInsn(opcode, (Label) params[0]);
					} else {
						if (adapter == null)
							throw new IllegalArgumentException("the adapter must not be null to solve the label");
						mv.visitJumpInsn(opcode, adapter.getLabel((Integer) params[0]));
					}
					break;
				case LABEL:
					if (params[0] instanceof Label) {
						mv.visitLabel((Label) params[0]);
					} else {
						if (adapter == null)
							throw new IllegalArgumentException("the adapter must not be null to solve the label");
						mv.visitLabel(adapter.getLabel((Integer) params[0]));
					}
					break;
				case LDC:
					mv.visitLdcInsn(params[0]);
					break;
				case METHOD:
					mv.visitMethodInsn(opcode, (String) params[0], (String) params[1], (String) params[2], (Boolean) params[3]);
					break;
				case TYPE:
					mv.visitTypeInsn(opcode, (String) params[0]);
					break;
				case VAR:
					mv.visitVarInsn(opcode, (Integer) params[0]);
					break;
				default:
					throw new RuntimeException("Invalid Type:" + type);
			}
		}

		public void solveLabel(MyMethodVisitor adapter) {
			if (isRep())
				return;
			if (type == VisitType.JUMP) {
				if (params[0] instanceof Integer) {
					Label label = adapter.tryGetLabel((Integer) params[0]);
					if (label != null)
						params[0] = label;
				}
			}
			if (type == VisitType.LABEL) {
				if (params[0] instanceof Integer) {
					Label label = adapter.tryGetLabel((Integer) params[0]);
					if (label != null)
						params[0] = label;
				}
			}
		}

		protected boolean isRep() { return false; }
		protected boolean repEquals(Instr other) { return false; }

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj instanceof Instr) {
				Instr other = (Instr) obj;
				return equals(this, other);
			}
			return false;
		}

		private static boolean equals(Instr a, Instr b) {
			if (a == REP || b == REP)
				return true;
			if (a.isRep())
				return a.repEquals(b);
			if (b.isRep())
				return b.repEquals(a);
			if (a.type != b.type)
				return false;
			if (a.opcode != b.opcode)
				return false;
			return Arrays.equals(a.params, b.params);
		}

		private enum VisitType {
			DYNAMIC,
			FIELD,
			IINC,
			INT,
			INSN,
			JUMP,
			LABEL,
			LDC,
			METHOD,
			TYPE,
			VAR,
		}

		@Override
		public String toString() {
			return type +
					"(" +
					StringUtils.join(params, ",") +
					")";
		}
	}

	public enum OpcodeVar {
		ILOAD(Opcodes.ILOAD),
		LLOAD(Opcodes.LLOAD),
		FLOAD(Opcodes.FLOAD),
		DLOAD(Opcodes.DLOAD),
		ALOAD(Opcodes.ALOAD),
		ISTORE(Opcodes.ISTORE),
		LSTORE(Opcodes.LSTORE),
		FSTORE(Opcodes.FSTORE),
		DSTORE(Opcodes.DSTORE),
		ASTORE(Opcodes.ASTORE),
		;

		public final int opcode;

		OpcodeVar(int opcode) { this.opcode = opcode; }
	}

	public enum OpcodeField {
		GET(Opcodes.GETFIELD),
		PUT(Opcodes.PUTFIELD),
		GETSTATIC(Opcodes.GETSTATIC),
		PUTSTATIC(Opcodes.PUTSTATIC),
		;

		public final int opcode;

		OpcodeField(int opcode) { this.opcode = opcode; }
	}

	public enum OpcodeMethod {
		VIRTUAL(Opcodes.INVOKEVIRTUAL),
		SPECIAL(Opcodes.INVOKESPECIAL),
		STATIC(Opcodes.INVOKESTATIC),
		INTERFACE(Opcodes.INVOKEINTERFACE),
		;

		public final int opcode;

		OpcodeMethod(int opcode) { this.opcode = opcode; }
	}

	public enum OpcodeInt {
		BIPUSH(Opcodes.BIPUSH),
		SIPUSH(Opcodes.SIPUSH),
		NEWARRAY(Opcodes.NEWARRAY),
		;

		public final int opcode;

		OpcodeInt(int opcode) { this.opcode = opcode; }
	}

	public enum OpcodeJump {
		IFEQ(Opcodes.IFEQ),
		IFNE(Opcodes.IFNE),
		IFLT(Opcodes.IFLT),
		IFGE(Opcodes.IFGE),
		IFGT(Opcodes.IFGT),
		IFLE(Opcodes.IFLE),
		IF_ICMPEQ(Opcodes.IF_ICMPEQ),
		IF_ICMPNE(Opcodes.IF_ICMPNE),
		IF_ICMPLT(Opcodes.IF_ICMPLT),
		IF_ICMPGE(Opcodes.IF_ICMPGE),
		IF_ICMPGT(Opcodes.IF_ICMPGT),
		IF_ICMPLE(Opcodes.IF_ICMPLE),
		IF_ACMPEQ(Opcodes.IF_ACMPEQ),
		IF_ACMPNE(Opcodes.IF_ACMPNE),
		GOTO(Opcodes.GOTO),
		JSR(Opcodes.JSR),
		IFNULL(Opcodes.IFNULL),
		IFNONNULL(Opcodes.IFNONNULL),
		;

		public final int opcode;

		OpcodeJump(int opcode) { this.opcode = opcode; }
	}

	//Listインターフェース

	@Override
	public int size() { return instructions.size(); }

	@Override
	public boolean isEmpty() { return instructions.isEmpty(); }

	@Override
	public boolean contains(Object o) {
		return instructions.contains(o);
	}

	@Override
	public Iterator<Instr> iterator() { return instructions.iterator(); }

	@Override
	public Object[] toArray() { return instructions.toArray(); }

	@Override
	public <T> T[] toArray(T[] a) { return instructions.toArray(a); }

	@Override
	public boolean add(Instr e) { return instructions.add(e); }

	@Override
	public boolean remove(Object o) { return instructions.remove(o); }

	@Override
	public boolean containsAll(Collection<?> c) { return instructions.containsAll(c); }

	@Override
	public boolean addAll(Collection<? extends Instr> c) { return instructions.addAll(c); }

	@Override
	public boolean addAll(int index, Collection<? extends Instr> c) { return instructions.addAll(index, c); }

	@Override
	public boolean removeAll(Collection<?> c) { return instructions.removeAll(c); }

	@Override
	public boolean retainAll(Collection<?> c) { return instructions.retainAll(c); }

	@Override
	public void clear() { instructions.clear(); }

	@Override
	public Instr get(int index) { return instructions.get(index); }

	@Override
	public Instr set(int index, Instr element) { return instructions.set(index, element); }

	@Override
	public void add(int index, Instr element) { instructions.add(index, element); }

	@Override
	public Instr remove(int index) { return instructions.remove(index); }

	@Override
	public int indexOf(Object o) { return instructions.indexOf(o); }

	@Override
	public int lastIndexOf(Object o) { return instructions.lastIndexOf(o); }

	@Override
	public ListIterator<Instr> listIterator() { return instructions.listIterator(); }

	@Override
	public ListIterator<Instr> listIterator(int index) { return instructions.listIterator(index); }

	@Override
	public List<Instr> subList(int fromIndex, int toIndex) { return instructions.subList(fromIndex, toIndex); }

}
