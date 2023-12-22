package kpan.ig_custom_stuff.asm.tf;

import kpan.ig_custom_stuff.asm.core.AsmTypes;
import kpan.ig_custom_stuff.asm.core.AsmUtil;
import kpan.ig_custom_stuff.asm.core.MyAsmNameRemapper.MethodRemap;
import kpan.ig_custom_stuff.asm.core.adapters.ReplaceRefMethodAdapter;
import org.objectweb.asm.ClassVisitor;

public class TF_BlockStairs {

	private static final String TARGET = "net.minecraft.block.BlockStairs";
	private static final String HOOK = AsmTypes.HOOK + "HK_" + "BlockStairs";

	private static final MethodRemap isBlockStairs = new MethodRemap(TARGET, "isBlockStairs", AsmUtil.toMethodDesc(AsmTypes.BOOL, AsmTypes.IBLOCKSTATE), "func_185709_i");

	public static ClassVisitor appendVisitor(ClassVisitor cv, String className) {
		if (!TARGET.equals(className))
			return cv;
		ClassVisitor newcv = new ReplaceRefMethodAdapter(cv, HOOK, isBlockStairs);
		return newcv;
	}
}
