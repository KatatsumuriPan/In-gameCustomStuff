package kpan.ig_custom_stuff.asm.core;

import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

import javax.annotation.Nullable;
import java.lang.reflect.Method;

public class AsmUtil {
	public static final int ASM_VER = Opcodes.ASM5;

	public static boolean isDeobfEnvironment() { return FMLLaunchHandler.isDeobfuscatedEnvironment(); }

	public static boolean isOptifineLoaded() {
		try {
			Class.forName("optifine.Patcher");
		} catch (ClassNotFoundException e) {
			return false;
		}
		return true;
	}

	//MethodDescにも使用可能
	public static String obfDesc(String deobfDesc) {
		StringBuilder sb = new StringBuilder(deobfDesc.length());
		boolean object = false;
		StringBuilder sb_object = new StringBuilder();
		for (int i = 0; i < deobfDesc.length(); i++) {
			char c = deobfDesc.charAt(i);
			if (object) {
				if (c == ';') {
					String name = MyAsmNameRemapper.getClassObfName(sb_object.toString());
					sb.append('L');
					sb.append(name);
					sb.append(';');
					object = false;
					sb_object.setLength(0);
				} else {
					sb_object.append(c);
				}
			} else {
				if (c == 'L') {
					object = true;
				} else {
					sb.append(c);
				}
			}
		}
		return sb.toString();
	}

	//MethodDescにも使用可能
	public static String deobfDesc(String obfDesc) {
		StringBuilder sb = new StringBuilder(obfDesc.length());
		boolean object = false;
		StringBuilder sb_object = new StringBuilder();
		for (int i = 0; i < obfDesc.length(); i++) {
			char c = obfDesc.charAt(i);
			if (object) {
				if (c == ';') {
					String name = MyAsmNameRemapper.getClassDeobfName(sb_object.toString());
					sb.append('L');
					sb.append(name);
					sb.append(';');
					object = false;
					sb_object.setLength(0);
				} else {
					sb_object.append(c);
				}
			} else {
				if (c == 'L') {
					object = true;
				} else {
					sb.append(c);
				}
			}
		}
		return sb.toString();
	}

	public static String toMethodDesc(Object returnType, Object... rawDesc) {
		StringBuilder sb = new StringBuilder("(");
		for (Object o : rawDesc) {
			sb.append(toDesc(o));
		}
		sb.append(')');
		sb.append(toDesc(returnType));
		return sb.toString();
	}

	public static String toDesc(Object raw) {
		if (raw instanceof Class) {
			Class<?> clazz = (Class<?>) raw;
			return Type.getDescriptor(clazz);
		} else if (raw instanceof String) {
			String desc = (String) raw;
			int arr_dim = 0;
			while (arr_dim < desc.length() - 1) {
				if (desc.charAt(arr_dim) != '[')
					break;
				arr_dim++;
			}
			String arr_str = arr_dim > 0 ? StringUtils.repeat('[', arr_dim) : "";
			desc = desc.substring(arr_dim);
			if (desc.equals(AsmTypes.VOID) || desc.equals(AsmTypes.BOOL) || desc.equals(AsmTypes.CHAR) || desc.equals(AsmTypes.BYTE) || desc.equals(AsmTypes.SHORT) || desc.equals(AsmTypes.INT)
					|| desc.equals(AsmTypes.LONG) || desc.equals(AsmTypes.FLOAT) || desc.equals(AsmTypes.DOUBLE))
				return arr_str + desc;
			desc = desc.replace('.', '/');
			desc = desc.matches("L.+;") ? desc : "L" + desc + ";";//全体とマッチ
			return arr_str + desc;
		} else if (raw instanceof Object[]) {
			StringBuilder sb = new StringBuilder();
			for (Object o : (Object[]) raw) {
				sb.append(toDesc(o));
			}
			return sb.toString();
		} else if (raw instanceof Method) {
			return Type.getMethodDescriptor((Method) raw);
		} else {
			throw new IllegalArgumentException();
		}
	}

	@SuppressWarnings("unused")
	public static MethodVisitor traceMethod(MethodVisitor mv, @Nullable String methodName) {
		Textifier p = new MyTextifier(methodName);
		return new TraceMethodVisitor(mv, p);
	}

	//MethodDescにも使用可能
	public static String runtimeDesc(String deobfDesc) {
		if (isDeobfEnvironment())
			return deobfDesc;
		else
			return obfDesc(deobfDesc);
	}

	public static String composeRuntimeMethodDesc(Object deobfReturnType, Object... deobfParams) {
		return runtimeDesc(toMethodDesc(deobfReturnType, deobfParams));
	}

	public static String runtimeMethodGenerics(String deobfGenerics) {
		throw new NotImplementedException("TODO");//TODO
	}

	public static String[] runtimeExceptions(String[] deobfExceptions) {
		throw new NotImplementedException("TODO");//TODO
	}

	public static int toLoadOpcode(String desc) {
		switch (desc) {
			case AsmTypes.BOOL:
			case AsmTypes.CHAR:
			case AsmTypes.BYTE:
			case AsmTypes.SHORT:
			case AsmTypes.INT:
				return Opcodes.ILOAD;
			case AsmTypes.LONG:
				return Opcodes.LLOAD;
			case AsmTypes.FLOAT:
				return Opcodes.FLOAD;
			case AsmTypes.DOUBLE:
				return Opcodes.DLOAD;
			default:
				return Opcodes.ALOAD;
		}
	}
	public static int loadLocals(MethodVisitor mv, String[] descs, int offset) {
		for (String desc : descs) {
			int opcode = AsmUtil.toLoadOpcode(desc);
			mv.visitVarInsn(opcode, offset);
			if (opcode == Opcodes.LLOAD || opcode == Opcodes.DOUBLE)
				offset += 2;
			else
				offset += 1;
		}
		return offset;
	}

	public static int toReturnOpcode(String type) {
		switch (type) {
			case AsmTypes.VOID:
				return Opcodes.RETURN;
			case AsmTypes.BOOL:
			case AsmTypes.CHAR:
			case AsmTypes.BYTE:
			case AsmTypes.SHORT:
			case AsmTypes.INT:
				return Opcodes.IRETURN;
			case AsmTypes.LONG:
				return Opcodes.LRETURN;
			case AsmTypes.FLOAT:
				return Opcodes.FRETURN;
			case AsmTypes.DOUBLE:
				return Opcodes.DRETURN;
			default:
				return Opcodes.ARETURN;
		}
	}

}
