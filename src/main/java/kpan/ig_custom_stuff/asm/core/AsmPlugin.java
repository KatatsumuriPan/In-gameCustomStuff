package kpan.ig_custom_stuff.asm.core;

import kpan.ig_custom_stuff.ModTagsGenerated;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.MCVersion;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.Name;
import org.apache.logging.log4j.LogManager;

import javax.annotation.Nullable;
import java.util.Map;

@IFMLLoadingPlugin.TransformerExclusions({ModTagsGenerated.MODGROUP + ".asm.core.", ModTagsGenerated.MODGROUP + ".asm.tf.", ModTagsGenerated.MODGROUP + ".util.MyReflectionHelper"})
@Name("AsmPlugin")
@MCVersion("1.12.2")
public class AsmPlugin implements IFMLLoadingPlugin {

	public AsmPlugin() {
		LogManager.getLogger().debug("This is " + (AsmUtil.isDeobfEnvironment() ? "deobf" : "obf") + " environment");
	}

	@Override
	public String[] getASMTransformerClass() { return new String[]{ASMTransformer.class.getName()}; }

	@Override
	public String getModContainerClass() { return null; }

	@Nullable
	@Override
	public String getSetupClass() { return null; }

	@Override
	public void injectData(Map<String, Object> data) { }

	@Override
	public String getAccessTransformerClass() { return AccessTransformerForMixin.class.getName(); }

}
