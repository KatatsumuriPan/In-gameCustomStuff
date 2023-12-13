package kpan.ig_custom_stuff.resource;

import kpan.ig_custom_stuff.ModTagsGenerated;
import kpan.ig_custom_stuff.resource.ids.BlockId;
import kpan.ig_custom_stuff.resource.ids.ItemId;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.AbstractResourcePack;
import net.minecraft.client.resources.FallbackResourceManager;
import net.minecraft.client.resources.SimpleReloadableResourceManager;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

public class RemovedResourcesResourcePack extends AbstractResourcePack {

	public static final RemovedResourcesResourcePack INSTANCE = new RemovedResourcesResourcePack();

	private final Set<String> loadedNamespaces = new HashSet<>();

	private final Set<String> removedBlockStates = new HashSet<>();//"assets/namespace/blockstates/name.json"
	private final Set<String> removedItemBlockModel = new HashSet<>();//"assets/namespace/models/item/path.json"
	private final Set<String> removedItemModel = new HashSet<>();//"assets/namespace/models/item/path.json"

	private RemovedResourcesResourcePack() {
		super(new File("DUMMY"));
	}

	public void addRemovedBlock(BlockId blockId) {
		removedBlockStates.add("assets/" + blockId.namespace + "/blockstates/" + blockId.name + ".json");
		removedItemBlockModel.add("assets/" + blockId.namespace + "/models/item/" + blockId.name + ".json");
		addNamespace(blockId.namespace);
	}

	public void removeRemovedBlock(BlockId blockId) {
		removedBlockStates.remove("assets/" + blockId.namespace + "/blockstates/" + blockId.name + ".json");
		removedItemBlockModel.remove("assets/" + blockId.namespace + "/models/item/" + blockId.name + ".json");
	}

	public void addRemovedItem(ItemId itemId) {
		removedItemModel.add("assets/" + itemId.namespace + "/models/item/" + itemId.name + ".json");
		addNamespace(itemId.namespace);
	}

	public void removeRemovedItem(ItemId itemId) {
		removedItemModel.remove("assets/" + itemId.namespace + "/models/item/" + itemId.name + ".json");
	}

	public void clearAll() {
		loadedNamespaces.clear();
		removedBlockStates.clear();
		removedItemBlockModel.clear();
		removedItemModel.clear();
	}

	@Override
	public String getPackName() {
		return "ics_internal_removed_resource_pack";
	}

	@Override
	protected InputStream getInputStreamByName(String name) throws IOException {
		String json;
		if (removedBlockStates.contains(name))
			json = "{\n" +
					"  \"variants\": {\n" +
					"    \"normal\": {\n" +
					"      \"model\": \"" + ModTagsGenerated.MODID + ":removed\"\n" +
					"    },\n" +
					"    \"inventory\": {\n" +
					"      \"model\": \"" + ModTagsGenerated.MODID + ":removed\"\n" +
					"    }\n" +
					"  }\n" +
					"}\n";
		else if (removedItemBlockModel.contains(name))
			json = "{\n" +
					"    \"parent\": \"" + ModTagsGenerated.MODID + ":block/removed\"\n" +
					"}\n";
		else
			json = "{\n" +
					"  \"parent\": \"item/generated\",\n" +
					"  \"textures\": {\n" +
					"    \"layer0\": \"" + ModTagsGenerated.MODID + ":items/removed\"\n" +
					"  }\n" +
					"}\n";
		return new ByteArrayInputStream((json).getBytes(StandardCharsets.UTF_8));
	}
	@Override
	protected boolean hasResourceName(String name) {
		return removedBlockStates.contains(name) || removedItemBlockModel.contains(name) || removedItemModel.contains(name);
	}
	@Override
	public Set<String> getResourceDomains() {
		return loadedNamespaces;
	}

	private void addNamespace(String namespace) {
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
