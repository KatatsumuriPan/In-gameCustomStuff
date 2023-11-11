package kpan.ig_custom_stuff.asm.core;

import com.google.common.base.Strings;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import kpan.ig_custom_stuff.util.MyReflectionHelper;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import net.minecraftforge.fml.common.patcher.ClassPatchManager;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.objectweb.asm.ClassReader;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class MyAsmNameRemapper {

	private static LaunchClassLoader classLoader;
	private static final Map<String, BiMap<String, String>> fieldObfSrgMap = Maps.newHashMap();
	private static final Map<String, BiMap<NameDescPair, NameDescPair>> methodObfSrgMap = Maps.newHashMap();
	private static final Set<String> srgMcpLoadedSet = Sets.newHashSet();

	//FMLDeobfuscatingRemapper
	private static Map<String, Map<String, String>> rawFieldMaps;
	private static Map<String, Map<String, String>> rawMethodMaps;

	public static void init() {
		if (classLoader == null) {
			classLoader = MyReflectionHelper.getPrivateField(FMLDeobfuscatingRemapper.INSTANCE, "classLoader");
			loadDeobfMap();
			LogManager.getLogger().info("Obf Rename Mapping Loaded Completely");
		}
	}
	private static void loadDeobfMap() {
		rawFieldMaps = MyReflectionHelper.getPrivateField(FMLDeobfuscatingRemapper.INSTANCE, "rawFieldMaps");
		rawMethodMaps = MyReflectionHelper.getPrivateField(FMLDeobfuscatingRemapper.INSTANCE, "rawMethodMaps");
	}

	public static String runtimeClass(String deobfName) {
		if (AsmUtil.isDeobfEnvironment())
			return deobfName.replace('.', '/');
		else
			return getClassObfName(deobfName);
	}
	public static String runtimeField(FieldRemap fieldRemap) {
		if (AsmUtil.isDeobfEnvironment())
			return fieldRemap.mcpFieldName;

		String obf_owner = getClassObfName(fieldRemap.deobfOwner);
		String srg_name = fieldRemap.srgFieldName;
		return srg2ObfFieldName(obf_owner, srg_name);
	}
	public static String runtimeMethod(MethodRemap methodRemap) {
		if (AsmUtil.isDeobfEnvironment())
			return methodRemap.mcpMethodName;

		String obf_owner = getClassObfName(methodRemap.deobfOwner);
		String srg_name = methodRemap.srgMethodName;
		return srg2ObfMethodName(obf_owner, srg_name, AsmUtil.obfDesc(methodRemap.deobfMethodDesc));
	}

	public static String getClassDeobfName(String obfName) {
		obfName = obfName.replace('.', '/');
		return FMLDeobfuscatingRemapper.INSTANCE.map(obfName);
	}
	public static String getClassObfName(String deobfName) {
		deobfName = deobfName.replace('.', '/');
		return FMLDeobfuscatingRemapper.INSTANCE.unmap(deobfName);
	}

	public static String obf2SrgMethodName(String obfOwner, String obfName, String obfDesc) {
		BiMap<NameDescPair, NameDescPair> map = getMethodObf2SrgMap(obfOwner);
		if (map == null)
			return obfName;
		NameDescPair mcp_pair = map.get(new NameDescPair(obfName, obfDesc));
		if (mcp_pair == null)
			return obfName;
		return mcp_pair.name;
	}
	public static String srg2ObfMethodName(String obfOwner, String srgName, String obfDesc) {
		BiMap<NameDescPair, NameDescPair> map = getMethodObf2SrgMap(obfOwner);
		if (map == null)
			return srgName;
		NameDescPair obf_pair = map.inverse().get(new NameDescPair(srgName, obfDesc));
		if (obf_pair == null)
			return srgName;
		return obf_pair.name;
	}
	public static String obf2SrgFieldName(String obfOwner, String obfName) {
		BiMap<String, String> map = getFieldObf2SrgMap(obfOwner);
		if (map == null)
			return obfName;
		String srg_name = map.get(obfName);
		if (srg_name == null)
			return obfName;
		return srg_name;
	}
	public static String srg2ObfFieldName(String obfOwner, String srgName) {
		BiMap<String, String> map = getFieldObf2SrgMap(obfOwner);
		if (map == null)
			return srgName;
		String obf_name = map.inverse().get(srgName);
		if (obf_name == null)
			return srgName;
		return obf_name;
	}

	private static @Nullable BiMap<NameDescPair, NameDescPair> getMethodObf2SrgMap(String obfOwner) {
		if (!srgMcpLoadedSet.contains(obfOwner)) {
			findAndMergeSuperMaps(obfOwner);
		}
		return methodObfSrgMap.get(obfOwner);
	}
	private static @Nullable BiMap<String, String> getFieldObf2SrgMap(String obfOwner) {
		if (!srgMcpLoadedSet.contains(obfOwner)) {
			findAndMergeSuperMaps(obfOwner);
		}
		return fieldObfSrgMap.get(obfOwner);
	}
	private static void findAndMergeSuperMaps(String obfName) {
		try {
			String superName = null;
			String[] interfaces = new String[0];
			byte[] classBytes = ClassPatchManager.INSTANCE.getPatchedResource(obfName, getClassDeobfName(obfName), classLoader);
			if (classBytes != null) {
				ClassReader cr = new ClassReader(classBytes);
				superName = cr.getSuperName();
				interfaces = cr.getInterfaces();
			}
			mergeSuperMaps(obfName, superName, interfaces);
			srgMcpLoadedSet.add(obfName);
		} catch (IOException e) {
			FMLLog.log.error("Error getting patched resource:", e);//for java8
			//			FMLLog.getLogger().error("Error getting patched resource:", e);//for java7
		}
	}
	private static void mergeSuperMaps(String obfName, @Nullable String superName, String[] interfaces) {
		if (Strings.isNullOrEmpty(superName))
			return;

		String deobf = getClassDeobfName(obfName);
		List<String> allParents = ImmutableList.<String>builder().add(superName).addAll(Arrays.asList(interfaces)).build();
		// generate maps for all parent objects
		for (String parentThing : allParents) {
			if (!fieldObfSrgMap.containsKey(parentThing)) {
				findAndMergeSuperMaps(parentThing);
			}
		}
		BiMap<NameDescPair, NameDescPair> method_obfsrg_map = HashBiMap.create();
		BiMap<String, String> field_obfsrg_map = HashBiMap.create();
		for (String parentThing : allParents) {
			String deobf_parent = getClassDeobfName(parentThing);
			if (methodObfSrgMap.containsKey(parentThing)) {
				method_obfsrg_map.putAll(methodObfSrgMap.get(parentThing));
			}
			if (fieldObfSrgMap.containsKey(parentThing)) {
				field_obfsrg_map.putAll(fieldObfSrgMap.get(parentThing));
			}

		}
		if (rawMethodMaps.containsKey(obfName)) {
			for (Entry<String, String> entry : rawMethodMaps.get(obfName).entrySet()) {
				String[] split_obf = entry.getKey().split("\\(");
				String name_obf = split_obf[0];
				String desc_obf = "(" + split_obf[1];
				String name_srg = entry.getValue();
				method_obfsrg_map.forcePut(new NameDescPair(name_obf, desc_obf), new NameDescPair(name_srg, desc_obf));
			}
		}
		if (rawFieldMaps.containsKey(obfName)) {
			for (Entry<String, String> entry : rawFieldMaps.get(obfName).entrySet()) {
				String name_obf = entry.getKey().split(":")[0];
				String name_srg = entry.getValue().split(":")[0];
				field_obfsrg_map.forcePut(name_obf, name_srg);
			}
		}

		methodObfSrgMap.put(obfName, ImmutableBiMap.copyOf(method_obfsrg_map));
		fieldObfSrgMap.put(obfName, ImmutableBiMap.copyOf(field_obfsrg_map));

		LogManager.getLogger().debug("map : " + deobf + "  count : " + method_obfsrg_map.size() + "," + field_obfsrg_map.size());
	}

	public static class NameDescPair {
		public final String name;
		public final String desc;

		public NameDescPair(String name, String desc) {
			this.name = name;
			this.desc = desc;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof NameDescPair) {
				NameDescPair other = (NameDescPair) obj;
				return name.equals(other.name) && desc.equals(other.desc);
			}
			return false;
		}

		@Override
		public int hashCode() {
			return name.hashCode() | desc.hashCode();
		}

		@Override
		public String toString() {
			return name + " " + desc;
		}
	}

	public static class FieldRemap {
		public final String deobfOwner;
		public final String mcpFieldName;
		public final String deobfDesc;
		public final String srgFieldName;

		public FieldRemap(String deobfOwner, String mcpFieldName, String deobfDesc, @Nullable String srgFieldName) {
			this.deobfOwner = deobfOwner.replace('.', '/');
			this.mcpFieldName = mcpFieldName;
			this.deobfDesc = AsmUtil.toDesc(deobfDesc);
			this.srgFieldName = StringUtils.isEmpty(srgFieldName) ? mcpFieldName : srgFieldName;
		}

		public Object[] toRuntime() {
			if (AsmUtil.isDeobfEnvironment())
				return new Object[]{deobfOwner, mcpFieldName, deobfDesc};
			String obf_owner = getClassObfName(deobfOwner);
			String obf_name = srg2ObfFieldName(obf_owner, srgFieldName);
			return new Object[]{obf_owner, obf_name, AsmUtil.obfDesc(deobfDesc)};
		}

	}

	public static class MethodRemap {
		public final String deobfOwner;
		public final String mcpMethodName;
		public final String deobfMethodDesc;
		public final String srgMethodName;

		public MethodRemap(String deobfOwner, String mcpMethodName, String deobfMethodDesc, @Nullable String srgMethodName) {
			this.deobfOwner = deobfOwner.replace('.', '/');
			this.mcpMethodName = mcpMethodName;
			this.deobfMethodDesc = deobfMethodDesc;
			this.srgMethodName = StringUtils.isEmpty(srgMethodName) ? mcpMethodName : srgMethodName;
		}

		public boolean isTarget(String runtimeName, String runtimeDesc) {
			if (AsmUtil.isDeobfEnvironment()) {
				return runtimeName.equals(mcpMethodName) && runtimeDesc.equals(deobfMethodDesc);
			} else {
				String obf_owner = getClassObfName(deobfOwner);
				String obf_methoddesc = AsmUtil.obfDesc(deobfMethodDesc);
				String obf_name = srg2ObfMethodName(obf_owner, srgMethodName, obf_methoddesc);
				return runtimeName.equals(obf_name) && runtimeDesc.equals(obf_methoddesc);
			}
		}

		public Object[] toRuntime() {
			if (AsmUtil.isDeobfEnvironment()) {
				return new Object[]{deobfOwner, mcpMethodName, deobfMethodDesc};
			} else {
				String obf_owner = getClassObfName(deobfOwner);
				String obf_methoddesc = AsmUtil.obfDesc(deobfMethodDesc);
				String obf_name = srg2ObfMethodName(obf_owner, srgMethodName, obf_methoddesc);
				return new Object[]{obf_owner, obf_name, obf_methoddesc};
			}
		}

	}

}
