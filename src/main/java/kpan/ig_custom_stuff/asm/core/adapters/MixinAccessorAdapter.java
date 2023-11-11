package kpan.ig_custom_stuff.asm.core.adapters;

import kpan.ig_custom_stuff.ModTagsGenerated;
import kpan.ig_custom_stuff.asm.core.AccessTransformerForMixin;
import kpan.ig_custom_stuff.asm.core.AsmTypes;
import kpan.ig_custom_stuff.asm.core.AsmTypes.MethodDesc;
import kpan.ig_custom_stuff.asm.core.AsmUtil;
import kpan.ig_custom_stuff.asm.core.MyAsmNameRemapper;
import kpan.ig_custom_stuff.asm.core.MyAsmNameRemapper.FieldRemap;
import kpan.ig_custom_stuff.asm.core.MyAsmNameRemapper.MethodRemap;
import org.apache.commons.lang3.ArrayUtils;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.TypePath;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;

@SuppressWarnings("unused")
public class MixinAccessorAdapter extends MyClassVisitor {

	private final String deobfTargetClassName;
	private final Class<?> accessor;
	private final HashMap<String, RuntimeInfo> fieldInfoMap = new HashMap<>();
	private final HashMap<String, MethodInfo> methodInfoMap = new HashMap<>();
	public MixinAccessorAdapter(ClassVisitor cv, String deobfTargetClassName, String accessorClassName) {
		super(cv, deobfTargetClassName);
		this.deobfTargetClassName = deobfTargetClassName;
		mixinTarget = deobfTargetClassName;
		try {
			accessor = Class.forName(accessorClassName.replace('/', '.'));
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
		mixinTarget = null;
	}
	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		super.visit(version, access, name, signature, superName, ArrayUtils.add(interfaces, accessor.getName().replace('.', '/')));
	}

	@Override
	public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
		fieldInfoMap.put(name, new RuntimeInfo(desc, (access & Opcodes.ACC_STATIC) != 0));
		return super.visitField(access, name, desc, signature, value);
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		methodInfoMap.put(name, new MethodInfo(desc, (access & Opcodes.ACC_STATIC) != 0, (access & Opcodes.ACC_PRIVATE) != 0));
		return super.visitMethod(access, name, desc, signature, exceptions);
	}

	@Override
	public void visitEnd() {
		for (Method method : accessor.getMethods()) {
			String methodName = method.getName();
			if (methodName.startsWith("get_")) {
				//getter
				String deobf_name = methodName.substring("get_".length());
				Class<?> type = method.getReturnType();
				boolean is_static = Modifier.isStatic(method.getModifiers());
				if (type == void.class)
					throw new IllegalStateException("return type of getter is void!:" + methodName);
				if (method.getParameterCount() != 0)
					throw new IllegalStateException("parameters of getter are empty!:" + methodName);

				String srg_name = method.getAnnotation(SrgName.class) != null ? method.getAnnotation(SrgName.class).value() : deobf_name;
				String runtime_name = MyAsmNameRemapper.runtimeField(new FieldRemap(deobfTargetClassName, deobf_name, type.getName(), srg_name));
				String runtime_desc = AsmUtil.runtimeDesc(AsmUtil.toDesc(type));
				if (method.getAnnotation(NewField.class) != null) {
					if ((srg_name != null && !deobf_name.equals(srg_name)) || !deobf_name.equals(runtime_name))
						throw new IllegalStateException("Field duplicated!:" + methodName);
					if (!fieldInfoMap.containsKey(runtime_name)) {
						String runtimeGenerics = null;//TODO
						FieldVisitor fv = visitField(Opcodes.ACC_PUBLIC | (is_static ? Opcodes.ACC_STATIC : 0), runtime_name, runtime_desc, runtimeGenerics, null);
						if (fv != null)
							fv.visitEnd();
					}
				}
				RuntimeInfo fieldInfo = fieldInfoMap.get(runtime_name);
				if (fieldInfo == null)
					throw new IllegalStateException("Unknown field:" + runtime_name + "(" + methodName + ")");
				if (!fieldInfo.runtimeDesc.equals(runtime_desc))
					throw new IllegalStateException("Unmatched field type:" + runtime_name + "(" + methodName + ")");
				if (fieldInfo.isStatic != is_static)
					throw new IllegalStateException("Unmatched field access(static):" + runtime_name + "(" + methodName + ")");

				MethodVisitor mv = visitMethod(Opcodes.ACC_PUBLIC | (is_static ? Opcodes.ACC_STATIC : 0), methodName, AsmUtil.toMethodDesc(runtime_desc), null, null);
				if (mv != null) {
					mv.visitCode();
					if (is_static) {
						mv.visitFieldInsn(Opcodes.GETSTATIC, MyAsmNameRemapper.runtimeClass(deobfTargetClassName), runtime_name, runtime_desc);
					} else {
						mv.visitVarInsn(Opcodes.ALOAD, 0);
						mv.visitFieldInsn(Opcodes.GETFIELD, MyAsmNameRemapper.runtimeClass(deobfTargetClassName), runtime_name, runtime_desc);
					}
					mv.visitInsn(AsmUtil.toReturnOpcode(runtime_desc));
					mv.visitMaxs(0, 0);//引数は無視され、再計算される(Write時に再計算されるのでtrace時点では0,0のまま)
					mv.visitEnd();
				}

			} else if (methodName.startsWith("set_")) {
				//setter
				String deobf_name = methodName.substring("set_".length());
				Class<?> type = method.getParameterTypes()[0];
				boolean is_static = Modifier.isStatic(method.getModifiers());
				if (method.getReturnType() != void.class)
					throw new IllegalStateException("return type of getter is not void!:" + methodName);
				if (method.getParameterCount() != 1)
					throw new IllegalStateException("parameters num of getter is not 1!:" + methodName);

				String srg_name = method.getAnnotation(SrgName.class) != null ? method.getAnnotation(SrgName.class).value() : null;
				String runtime_name = MyAsmNameRemapper.runtimeField(new FieldRemap(deobfTargetClassName, deobf_name, type.getName(), srg_name));
				String runtime_desc = AsmUtil.runtimeDesc(AsmUtil.toDesc(type));
				if (method.getAnnotation(NewField.class) != null) {
					if ((srg_name != null && !deobf_name.equals(srg_name)) || !deobf_name.equals(runtime_name))
						throw new IllegalStateException("Field duplicated!:" + methodName);
					if (!fieldInfoMap.containsKey(runtime_name)) {
						String runtimeGenerics = null;//TODO
						FieldVisitor fv = visitField(Opcodes.ACC_PUBLIC | (is_static ? Opcodes.ACC_STATIC : 0), runtime_name, runtime_desc, runtimeGenerics, null);
						if (fv != null)
							fv.visitEnd();
					}
				}
				RuntimeInfo fieldInfo = fieldInfoMap.get(runtime_name);
				if (fieldInfo == null)
					throw new IllegalStateException("Unknown field:" + runtime_name + "(" + methodName + ")");
				if (!fieldInfo.runtimeDesc.equals(runtime_desc))
					throw new IllegalStateException("Unmatched field type:" + runtime_name + "(" + methodName + ")");
				if (fieldInfo.isStatic != is_static)
					throw new IllegalStateException("Unmatched field access(static):" + runtime_name + "(" + methodName + ")");

				MethodVisitor mv = visitMethod(Opcodes.ACC_PUBLIC | (is_static ? Opcodes.ACC_STATIC : 0), methodName, AsmUtil.toMethodDesc(AsmTypes.VOID, runtime_desc), null, null);
				if (mv != null) {
					mv.visitCode();
					if (is_static) {
						mv.visitVarInsn(AsmUtil.toLoadOpcode(runtime_desc), 0);
						mv.visitFieldInsn(Opcodes.PUTSTATIC, MyAsmNameRemapper.runtimeClass(deobfTargetClassName), runtime_name, runtime_desc);
					} else {
						mv.visitVarInsn(Opcodes.ALOAD, 0);
						mv.visitVarInsn(AsmUtil.toLoadOpcode(runtime_desc), 1);
						mv.visitFieldInsn(Opcodes.PUTFIELD, MyAsmNameRemapper.runtimeClass(deobfTargetClassName), runtime_name, runtime_desc);
					}
					mv.visitInsn(Opcodes.RETURN);
					mv.visitMaxs(0, 0);//引数は無視され、再計算される(Write時に再計算されるのでtrace時点では0,0のまま)
					mv.visitEnd();
				}
			} else if (method.getAnnotation(NewMethod.class) != null) {
				//staticじゃないならinterfaceがメソッドを持ってるので問題ない
				//staticなものは無効とする
			} else {
				//bridge
				String method_desc = AsmUtil.toDesc(method);
				boolean is_static = Modifier.isStatic(method.getModifiers());

				String runtime_name;
				if (method.getAnnotation(SrgName.class) != null) {
					String srg_name = method.getAnnotation(SrgName.class).value();
					runtime_name = MyAsmNameRemapper.runtimeMethod(new MethodRemap(deobfTargetClassName, methodName, method_desc, srg_name));
				} else {
					runtime_name = methodName;
				}

				if (methodName.equals(runtime_name)) {
					AccessTransformerForMixin.toPublic(deobfTargetClassName, methodName, method_desc);
				} else {
					String runtime_desc = AsmUtil.runtimeDesc(method_desc);
					MethodInfo methodInfo = methodInfoMap.get(runtime_name);
					if (methodInfo == null)
						throw new IllegalStateException("Unknown method:" + runtime_name + "(" + methodName + ")");
					if (!methodInfo.runtimeDesc.equals(runtime_desc))
						throw new IllegalStateException("Unknown method desc:" + runtime_name + "(" + methodName + ")");
					if (methodInfo.isStatic != is_static)
						throw new IllegalStateException("Unknown method access(static):" + runtime_name + "(" + methodName + ")");
					if (methodInfoMap.containsKey(methodName))
						throw new IllegalStateException("Duplicated method:" + runtime_name + "(" + methodName + ")");
					boolean is_private = methodInfo.isPrivate;

					MethodVisitor mv = visitMethod(Opcodes.ACC_PUBLIC | (is_static ? Opcodes.ACC_STATIC : 0), methodName, runtime_desc, null, null);
					if (mv != null) {
						mv.visitCode();
						int offset = 0;
						//this
						if (!is_static) {
							mv.visitVarInsn(Opcodes.ALOAD, 0);
							offset = 1;
						}
						//params
						for (int i = 0; i < method.getParameterCount(); i++) {
							int opcode = AsmUtil.toLoadOpcode(AsmUtil.toDesc(method.getParameterTypes()[i]));
							mv.visitVarInsn(opcode, offset);
							if (opcode == Opcodes.LLOAD || opcode == Opcodes.DOUBLE)
								offset += 2;
							else
								offset += 1;
						}
						//invoke
						if (is_static)
							mv.visitMethodInsn(Opcodes.INVOKESTATIC, MyAsmNameRemapper.runtimeClass(deobfTargetClassName), runtime_name, runtime_desc, false);
						else if (is_private)
							mv.visitMethodInsn(Opcodes.INVOKESPECIAL, MyAsmNameRemapper.runtimeClass(deobfTargetClassName), runtime_name, runtime_desc, false);
						else
							mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, MyAsmNameRemapper.runtimeClass(deobfTargetClassName), runtime_name, runtime_desc, false);
						//return
						mv.visitInsn(AsmUtil.toReturnOpcode(AsmUtil.toDesc(method.getReturnType())));

						mv.visitMaxs(0, 0);//引数は無視され、再計算される(Write時に再計算されるのでtrace時点では0,0のまま)
						mv.visitEnd();
					}
				}
			}
		}
		success();
		super.visitEnd();
	}

	private static String mixinTarget = null;

	public static ClassVisitor transformAccessor(ClassVisitor cv, String transformedName) {
		if (mixinTarget == null || !transformedName.startsWith(ModTagsGenerated.MODGROUP + ".asm.acc."))
			return cv;

		cv = new MyClassVisitor(cv, transformedName, 0) {
			@Override
			public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
				MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
				if ((access & Opcodes.ACC_STATIC) != 0) {
					mv = new MethodVisitor(AsmUtil.ASM_VER, mv) {
						@Override
						public void visitFrame(int type, int nLocal, Object[] local, int nStack, Object[] stack) {
						}
						@Override
						public void visitInsn(int opcode) {
						}
						@Override
						public void visitIntInsn(int opcode, int operand) {
						}
						@Override
						public void visitVarInsn(int opcode, int var) {
						}
						@Override
						public void visitTypeInsn(int opcode, String type) {
						}
						@Override
						public void visitFieldInsn(int opcode, String owner, String name, String desc) {
						}
						@Override
						public void visitMethodInsn(int opcode, String owner, String name, String desc) {
						}
						@Override
						public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
						}
						@Override
						public void visitInvokeDynamicInsn(String name, String desc, Handle bsm, Object... bsmArgs) {
						}
						@Override
						public void visitJumpInsn(int opcode, Label label) {
						}
						@Override
						public void visitLabel(Label label) {
						}
						@Override
						public void visitLdcInsn(Object cst) {
						}
						@Override
						public void visitIincInsn(int var, int increment) {
						}
						@Override
						public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
						}
						@Override
						public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
						}
						@Override
						public void visitMultiANewArrayInsn(String desc, int dims) {
						}
						@Override
						public AnnotationVisitor visitInsnAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
							return null;
						}
						@Override
						public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
						}
						@Override
						public AnnotationVisitor visitTryCatchAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
							return null;
						}
						@Override
						public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
						}
						@Override
						public AnnotationVisitor visitLocalVariableAnnotation(int typeRef, TypePath typePath, Label[] start, Label[] end, int[] index, String desc, boolean visible) {
							return null;
						}
						@Override
						public void visitLineNumber(int line, Label start) {
						}
						@Override
						public void visitMaxs(int maxStack, int maxLocals) {
						}
						@Override
						public void visitEnd() {
							if (name.startsWith("get_")) {
								//invoke
								mv.visitMethodInsn(Opcodes.INVOKESTATIC, mixinTarget.replace('.', '/'), name, desc, false);
								//return
								mv.visitInsn(AsmUtil.toReturnOpcode(desc.substring("()".length())));
							} else if (name.startsWith("set_")) {
								//params
								mv.visitVarInsn(AsmUtil.toLoadOpcode(desc.substring(1, desc.length() - 2)), 0);
								//invoke
								mv.visitMethodInsn(Opcodes.INVOKESTATIC, mixinTarget.replace('.', '/'), name, desc, false);
								//return
								mv.visitInsn(Opcodes.RETURN);
							} else {
								MethodDesc md = MethodDesc.fromMethodDesc(desc);
								//params
								AsmUtil.loadLocals(mv, md.paramsDesc, 0);
								//invoke
								mv.visitMethodInsn(Opcodes.INVOKESTATIC, mixinTarget.replace('.', '/'), name, desc, false);
								//return
								mv.visitInsn(AsmUtil.toReturnOpcode(md.returnDesc));
							}
							mv.visitMaxs(0, 0);//引数は無視され、再計算される(Write時に再計算されるのでtrace時点では0,0のまま)
							super.visitEnd();
						}
					};
				}
				return mv;
			}
		};

		return cv;
	}

	/**
	 * ターゲットクラスに新しいフィールドとして追加する。
	 * getterとsetterの両方で重複して付ける必要がある
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface NewField {
	}

	/**
	 * ターゲットクラスに新しいメソッドとして追加する。
	 * staticメソッドに対しては使用できない。
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface NewMethod {
	}

	/**
	 * 難読化されているバニラのコードなどで、フィールドやメソッドのsrg名を指定するために使う。
	 * メソッドの名前を元からget_field_71432_Pのようにしても良いが、それだと分かりづらいので、
	 * メソッドの名前をget_instanceにしたまま実行時にはget_field_71432_Pに変換するためにこのアノテーションを用いる。
	 * ただ、バニラのコードに対してはForgeが用意しているAccessTransformerを使った方が良い。
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface SrgName {
		String value() default "";
	}

	private static class RuntimeInfo {
		public final String runtimeDesc;
		public final boolean isStatic;

		private RuntimeInfo(String runtimeDesc, boolean isStatic) {
			this.runtimeDesc = runtimeDesc;
			this.isStatic = isStatic;
		}
	}

	private static class MethodInfo {
		public final String runtimeDesc;
		public final boolean isStatic;
		public final boolean isPrivate;

		private MethodInfo(String runtimeDesc, boolean isStatic, boolean isPrivate) {
			this.runtimeDesc = runtimeDesc;
			this.isStatic = isStatic;
			this.isPrivate = isPrivate;
		}
	}
}
