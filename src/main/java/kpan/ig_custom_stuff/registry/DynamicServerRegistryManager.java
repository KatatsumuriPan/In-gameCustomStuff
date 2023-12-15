package kpan.ig_custom_stuff.registry;

import kpan.ig_custom_stuff.ModMain;
import kpan.ig_custom_stuff.ModTagsGenerated;
import kpan.ig_custom_stuff.block.BlockEntry;
import kpan.ig_custom_stuff.block.BlockEntry.BlockEntryJson;
import kpan.ig_custom_stuff.block.BlockLangEntry;
import kpan.ig_custom_stuff.block.BlockStateEntry;
import kpan.ig_custom_stuff.item.ItemEntry;
import kpan.ig_custom_stuff.item.ItemEntry.ItemEntryJson;
import kpan.ig_custom_stuff.item.ItemLangEntry;
import kpan.ig_custom_stuff.item.model.ItemModelEntry;
import kpan.ig_custom_stuff.resource.DynamicResourceManager.Server;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class DynamicServerRegistryManager {

	@Nullable
	private static Path registryPath = null;
	private static final Map<ResourceLocation, ItemEntry> items = new HashMap<>();
	private static final Map<ResourceLocation, BlockEntry> blocks = new HashMap<>();

	@Nullable
	public static ItemEntry getItem(ResourceLocation itemId) {
		return items.get(itemId);
	}

	public static void loadItem(MinecraftServer server) {
		items.clear();
		registryPath = server.getActiveAnvilConverter().getFile(server.getFolderName(), ModTagsGenerated.MODID).toPath().resolve("registry");
		if (!Files.isDirectory(registryPath.resolve("item"))) {
			try {
				Files.createDirectories(registryPath.resolve("item"));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		try (Stream<Path> stream = Files.list(registryPath.resolve("item"))) {
			stream.filter(Files::isDirectory).forEach(namespace_dir -> {
				String namespace = namespace_dir.getFileName().toString();
				try (Stream<Path> stream1 = Files.list(namespace_dir)) {
					stream1.filter(Files::isRegularFile).forEach(p -> {
						String path = p.getFileName().toString().replace('\\', '/');
						try {
							String json = new String(Files.readAllBytes(p), StandardCharsets.UTF_8);
							ItemEntryJson itemEntryJson = ItemEntryJson.fromJson(json);
							ResourceLocation itemId = new ResourceLocation(namespace, path.substring(0, path.lastIndexOf('.')));
							items.put(itemId, new ItemEntry(itemId, itemEntryJson.propertyEntry));
							if (Server.INSTANCE.getItemModel(itemId) == null) {
								ModMain.LOGGER.info("An item model of {} is not found.", itemId);
								ModMain.LOGGER.info("Try add default item model.");
								Server.INSTANCE.addModel(itemId, ItemModelEntry.defaultModel());
							}
							if (!Server.INSTANCE.hasItemNameLang("en_us", itemId)) {
								ModMain.LOGGER.info("A lang of {} is not found.", itemId);
								ModMain.LOGGER.info("Try add default lang.");
								ItemLangEntry.defaultLang(itemId.getPath()).register(itemId, false);
							}
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					});
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			});
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		if (server.isDedicatedServer()) {
			for (ItemEntry itemEntry : items.values()) {
				MCRegistryUtil.register(itemEntry, false);
			}
		} else {
			MCRegistryUtil.syncClientItemRegistryIntegrated(items.values());
		}
	}
	public static void register(ItemEntry itemEntry) throws IOException {
		saveFile(itemEntry);
		items.put(itemEntry.itemId, itemEntry);
		MCRegistryUtil.register(itemEntry, false);
	}
	public static void update(ItemEntry itemEntry) throws IOException {
		saveFile(itemEntry);
		items.put(itemEntry.itemId, itemEntry);
		MCRegistryUtil.update(itemEntry, false);
	}
	public static void unregisterItem(ResourceLocation itemId) throws IOException {
		deleteItemFile(itemId);
		items.remove(itemId);
		MCRegistryUtil.removeItem(itemId, false);
	}
	private static void saveFile(ItemEntry itemEntry) throws IOException {
		if (registryPath == null)
			throw new IllegalStateException("registryPath is null!");
		Path path = registryPath.resolve("item").resolve(itemEntry.itemId.getNamespace()).resolve(itemEntry.itemId.getPath() + ".json");
		Files.createDirectories(path.getParent());
		Files.write(path, itemEntry.toJson().getBytes(StandardCharsets.UTF_8));
	}
	private static void deleteItemFile(ResourceLocation itemId) throws IOException {
		if (registryPath == null)
			throw new IllegalStateException("registryPath is null!");
		Path path = registryPath.resolve("item").resolve(itemId.getNamespace()).resolve(itemId.getPath() + ".json");
		Files.delete(path);
	}
	public static Collection<ItemEntry> getItemEntries() {
		return items.values();
	}

	//block

	@Nullable
	public static BlockEntry getBlock(ResourceLocation blockId) {
		return blocks.get(blockId);
	}

	public static void loadBlock(MinecraftServer server) {
		blocks.clear();
		registryPath = server.getActiveAnvilConverter().getFile(server.getFolderName(), ModTagsGenerated.MODID).toPath().resolve("registry");
		if (!Files.isDirectory(registryPath.resolve("block"))) {
			try {
				Files.createDirectories(registryPath.resolve("block"));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		try (Stream<Path> stream = Files.list(registryPath.resolve("block"))) {
			stream.filter(Files::isDirectory).forEach(namespace_dir -> {
				String namespace = namespace_dir.getFileName().toString();
				try (Stream<Path> stream1 = Files.list(namespace_dir)) {
					stream1.filter(Files::isRegularFile).forEach(p -> {
						String path = p.getFileName().toString().replace('\\', '/');
						try {
							String json = new String(Files.readAllBytes(p), StandardCharsets.UTF_8);
							BlockEntryJson blockEntryJson = BlockEntryJson.fromJson(json);
							ResourceLocation blockId = new ResourceLocation(namespace, path.substring(0, path.lastIndexOf('.')));
							blocks.put(blockId, new BlockEntry(blockId, blockEntryJson.blockStateType, blockEntryJson.propertyEntry));

							if (Server.INSTANCE.getBlockState(blockId) == null) {
								ModMain.LOGGER.info("A block state of {} is not found.", blockId);
								ModMain.LOGGER.info("Try add default block state.");
								Server.INSTANCE.addBlockstate(blockId, BlockStateEntry.defaultBlockState());
							}
							if (!Server.INSTANCE.hasBlockNameLang("en_us", blockId)) {
								ModMain.LOGGER.info("A lang of {} is not found.", blockId);
								ModMain.LOGGER.info("Try add default lang.");
								BlockLangEntry.defaultLang(blockId.getPath()).register(blockId, false);
							}
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					});
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			});
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		if (server.isDedicatedServer()) {
			for (BlockEntry blockEntry : blocks.values()) {
				MCRegistryUtil.register(blockEntry, false);
			}
		} else {
			MCRegistryUtil.syncClientBlockRegistryIntegrated(blocks.values());
		}
	}
	public static void register(BlockEntry blockEntry) throws IOException {
		saveFile(blockEntry);
		blocks.put(blockEntry.blockId, blockEntry);
		MCRegistryUtil.register(blockEntry, false);
	}
	public static void update(BlockEntry blockEntry) throws IOException {
		saveFile(blockEntry);
		blocks.put(blockEntry.blockId, blockEntry);
		MCRegistryUtil.update(blockEntry, false);
	}
	public static void unregisterBlock(ResourceLocation blockId) throws IOException {
		deleteBlockFile(blockId);
		blocks.remove(blockId);
		MCRegistryUtil.removeBlock(blockId, false);
	}
	private static void saveFile(BlockEntry blockEntry) throws IOException {
		if (registryPath == null)
			throw new IllegalStateException("registryPath is null!");
		Path path = registryPath.resolve("block").resolve(blockEntry.blockId.getNamespace()).resolve(blockEntry.blockId.getPath() + ".json");
		Files.createDirectories(path.getParent());
		Files.write(path, blockEntry.toJson().getBytes(StandardCharsets.UTF_8));
	}
	private static void deleteBlockFile(ResourceLocation blockId) throws IOException {
		if (registryPath == null)
			throw new IllegalStateException("registryPath is null!");
		Path path = registryPath.resolve("block").resolve(blockId.getNamespace()).resolve(blockId.getPath() + ".json");
		Files.delete(path);
	}
	public static Collection<BlockEntry> getBlockEntries() {
		return blocks.values();
	}
}
