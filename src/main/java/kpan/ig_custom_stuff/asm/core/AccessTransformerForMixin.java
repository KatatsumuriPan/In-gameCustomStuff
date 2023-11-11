package kpan.ig_custom_stuff.asm.core;

import com.google.common.io.CharSource;
import net.minecraftforge.fml.common.asm.transformers.AccessTransformer;

import java.io.IOException;

public class AccessTransformerForMixin extends AccessTransformer {

	private static AccessTransformerForMixin INSTANCE;
	private static boolean callSuper = false;


	public static void toPublic(String deobfOwner, String srgMethodName, String deobfMethodDesc) {
		String rule = "public " + deobfOwner + " " + srgMethodName + deobfMethodDesc;
		callSuper = true;
		INSTANCE.processATFile(CharSource.wrap(rule));
		callSuper = false;
	}

	public AccessTransformerForMixin() throws IOException {
		super();
		INSTANCE = this;
	}

	@Override
	protected void processATFile(CharSource rulesResource) {
		if (!callSuper)
			return;
		try {
			super.processATFile(rulesResource);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
