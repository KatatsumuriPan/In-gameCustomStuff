package kpan.ig_custom_stuff.resource;

import kpan.ig_custom_stuff.ModTagsGenerated;
import kpan.ig_custom_stuff.util.MyReflectionHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.FallbackResourceManager;
import net.minecraft.client.resources.FolderResourcePack;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.io.FileUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SideOnly(Side.CLIENT)
public class ModResourcePack extends FolderResourcePack {

	public static final ModResourcePack CLIENT_CACHE;

	static {
		CLIENT_CACHE = new ModResourcePack(DynamicResourceManager.ClientCache.resourcePackPath);
	}

	private final Set<String> loadedNamespaces = new HashSet<>();

	public ModResourcePack(Path resourcePackDir) {
		super(resourcePackDir.toFile());
		Path assetsPath = resourcePackDir.resolve("assets");
		try {
			if (Files.exists(assetsPath)) {
				if (Files.isRegularFile(assetsPath))
					throw new RuntimeException("Remove the file \"" + assetsPath + "\"");
				FileUtils.deleteDirectory(assetsPath.toFile());
			}
			Files.createDirectories(assetsPath);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		List<IResourcePack> resourcePackList = MyReflectionHelper.getPrivateField(FMLClientHandler.instance(), "resourcePackList");
		resourcePackList.add(this);
	}

	@Override
	public String getPackName() {
		return "ics_client_cache";
	}

	@Override
	protected InputStream getInputStreamByName(String resourceName) throws IOException {
		if ("pack.mcmeta".equals(resourceName)) {
			return new ByteArrayInputStream((
					"{\n" +
							" \"pack\": {\n" +
							"   \"description\": \"" + ModTagsGenerated.MODNAME + "'s dynamic internal resourcepack\",\n" +
							"   \"pack_format\": 3\n" +
							"}\n" +
							"}").getBytes(StandardCharsets.UTF_8));
		} else {
			return super.getInputStreamByName(resourceName);
		}
	}

	public File getResourcePackDir() { return resourcePackFile; }

	public void addNamespace(String namespace) {
		if (loadedNamespaces.add(namespace)) {
			SimpleReloadableResourceManager resourceManager = (SimpleReloadableResourceManager) Minecraft.getMinecraft().getResourceManager();

			resourceManager.getResourceDomains().add(namespace);
			FallbackResourceManager fallbackresourcemanager = resourceManager.domainResourceManagers.get(namespace);

			if (fallbackresourcemanager == null) {
				fallbackresourcemanager = new FallbackResourceManager(resourceManager.rmMetadataSerializer);
				resourceManager.domainResourceManagers.put(namespace, fallbackresourcemanager);
			}

			fallbackresourcemanager.addResourcePack(this);
		}
	}

}
