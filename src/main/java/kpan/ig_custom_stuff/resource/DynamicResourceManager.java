package kpan.ig_custom_stuff.resource;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.gson.JsonSyntaxException;
import kpan.ig_custom_stuff.ModTagsGenerated;
import kpan.ig_custom_stuff.block.BlockModelEntry;
import kpan.ig_custom_stuff.block.BlockStateEntry;
import kpan.ig_custom_stuff.item.model.ItemModelEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class DynamicResourceManager {
	public static final int ID_MAX_LENGTH = 100;//以下
	private static final Splitter SPLITTER = Splitter.on('=').limit(2);
	private static final Pattern PATTERN = Pattern.compile("%(\\d+\\$)?[\\d\\.]*[df]");
	public final Path assetsDir;
	public final Consumer<String> namespaceLoader;
	public final Map<String, Map<String, Map<String, String>>> lang = new TreeMap<>();//namespace->langCode->translationKey->name
	public final Map<ResourceLocation, BlockStateEntry> blockStates = new TreeMap<>();//blockStateId->
	public final Map<ResourceLocation, ItemModelEntry> itemModelIds = new TreeMap<>();//itemModelId->
	public final Map<ResourceLocation, BlockModelEntry> blockModelIds = new TreeMap<>();//blockModelId->
	public final Map<ResourceLocation, @Nullable TextureAnimationEntry> blockTextureIds = new TreeMap<>();
	public final Map<ResourceLocation, @Nullable TextureAnimationEntry> itemTextureIds = new TreeMap<>();

	public DynamicResourceManager(Path resourcepackDir, Consumer<String> namespaceLoader) {
		assetsDir = Paths.get(resourcepackDir.toString(), "assets");
		this.namespaceLoader = namespaceLoader;
		lang.put("en_us", new HashMap<>());
		try {
			Files.createDirectories(assetsDir);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	//lang
	public void addLang(String namespace, String langKey, String translateKey, String text) throws IOException {
		Map<String, String> translateMap = lang.computeIfAbsent(namespace, k -> new HashMap<>()).computeIfAbsent(langKey, k -> new HashMap<>());
		translateMap.put(translateKey, text);
		saveLang(namespace, langKey, translateMap);
	}
	public void removeLang(String namespace, String langKey, String translateKey) throws IOException {
		Map<String, Map<String, String>> namespacedLang = lang.get(namespace);
		if (namespacedLang == null)
			throw new IllegalStateException("Lang " + langKey + " doesn't exist!(namespace=" + namespace + ")");
		Map<String, String> translateMap = namespacedLang.get(langKey);
		if (translateMap == null)
			throw new IllegalStateException("Lang " + langKey + " doesn't exist!(namespace=" + namespace + ")");
		if (translateMap.remove(translateKey) == null)
			throw new IllegalStateException("Translation key " + translateKey + " of Lang " + langKey + " doesn't exist!");
		saveLang(namespace, langKey, translateMap);
	}
	@Nullable
	public String getLang(String namespace, String langKey, String translateKey) {
		Map<String, Map<String, String>> namespacedLang = lang.get(namespace);
		if (namespacedLang == null)
			return null;
		Map<String, String> translateMap = namespacedLang.get(langKey);
		if (translateMap == null)
			return null;
		return translateMap.get(translateKey);
	}
	private void saveLang(String namespace, String langKey, Map<String, String> translateMap) throws IOException {
		List<String> lines = new ArrayList<>();
		for (Entry<String, String> entry : translateMap.entrySet()) {
			lines.add(entry.getKey() + "=" + entry.getValue());
		}
		Path filePath = assetsDir.resolve(namespace).resolve("lang").resolve(langKey + ".lang");
		Files.createDirectories(filePath.getParent());
		Files.write(filePath, lines, StandardCharsets.UTF_8);
		namespaceLoader.accept(namespace);
	}
	public String getItemNameLang(String langKey, ResourceLocation itemId) {
		String translationKey = toTranslationKeyItem(itemId);
		String lang = getLang(itemId.getNamespace(), langKey, translationKey);
		return lang != null ? lang : translationKey;
	}
	public boolean hasItemNameLang(String langKey, ResourceLocation itemId) {
		String translationKey = toTranslationKeyItem(itemId);
		return getLang(itemId.getNamespace(), langKey, translationKey) != null;
	}
	public void removeItemNameLang(String langKey, ResourceLocation itemId) throws IOException {
		removeLang(itemId.getNamespace(), langKey, toTranslationKeyItem(itemId));
	}
	public String getBlockNameLang(String langKey, ResourceLocation blockId) {
		return getLang(blockId.getNamespace(), langKey, toTranslationKeyBlock(blockId));
	}
	public boolean hasBlockNameLang(String langKey, ResourceLocation blockId) {
		String translationKey = toTranslationKeyBlock(blockId);
		return getLang(blockId.getNamespace(), langKey, translationKey) != null;
	}
	public void removeBlockNameLang(String langKey, ResourceLocation blockId) throws IOException {
		removeLang(blockId.getNamespace(), langKey, toTranslationKeyBlock(blockId));
	}

	//model
	//itemModel
	public boolean isItemModelAdded(ResourceLocation itemModelId) {
		return itemModelIds.containsKey(itemModelId);
	}
	public void addModel(ResourceLocation itemId, ItemModelEntry itemModelEntry) throws IOException {
		ResourceLocation modelId = IdConverter.itemId2ItemModelId(itemId);
		Path filePath = assetsDir.resolve(modelId.getNamespace()).resolve("models").resolve(modelId.getPath());
		Files.createDirectories(filePath.getParent());
		Files.write(Paths.get(filePath + ".json"), itemModelEntry.toJson().getBytes(StandardCharsets.UTF_8));
		namespaceLoader.accept(modelId.getNamespace());
		itemModelIds.put(modelId, itemModelEntry);
	}
	@Nullable
	public ItemModelEntry getItemModel(ResourceLocation itemId) {
		return itemModelIds.get(IdConverter.itemId2ItemModelId(itemId));
	}
	public void removeItemModel(ResourceLocation itemId) throws IOException {
		ResourceLocation modelId = IdConverter.itemId2ItemModelId(itemId);
		Path filePath = assetsDir.resolve(modelId.getNamespace()).resolve("models").resolve(modelId.getPath());
		Files.delete(Paths.get(filePath + ".json"));
		namespaceLoader.accept(modelId.getNamespace());
		itemModelIds.remove(modelId);
	}
	//blockModel
	public boolean isBlockModelAdded(ResourceLocation blockModelId) {
		return blockModelIds.containsKey(blockModelId);
	}
	public boolean addBlockModel(ResourceLocation modelId, BlockModelEntry blockModelEntry) throws IOException {
		if (blockModelIds.containsKey(modelId))
			return false;
		Path blockModelFilePath = assetsDir.resolve(modelId.getNamespace()).resolve("models").resolve(modelId.getPath());
		Files.createDirectories(blockModelFilePath.getParent());
		Files.write(Paths.get(blockModelFilePath + ".json"), blockModelEntry.toJson().getBytes(StandardCharsets.UTF_8));
		namespaceLoader.accept(modelId.getNamespace());
		blockModelIds.put(modelId, blockModelEntry);
		return true;
	}
	public boolean replaceBlockModel(ResourceLocation modelId, BlockModelEntry blockModelEntry) throws IOException {
		if (!blockModelIds.containsKey(modelId))
			return false;
		Path blockModelFilePath = assetsDir.resolve(modelId.getNamespace()).resolve("models").resolve(modelId.getPath());
		Files.createDirectories(blockModelFilePath.getParent());
		Files.write(Paths.get(blockModelFilePath + ".json"), blockModelEntry.toJson().getBytes(StandardCharsets.UTF_8));
		namespaceLoader.accept(modelId.getNamespace());
		blockModelIds.put(modelId, blockModelEntry);
		return true;
	}
	@Nullable
	public BlockModelEntry getBlockModel(ResourceLocation modelId) {
		return blockModelIds.get(modelId);
	}
	public boolean removeBlockModel(ResourceLocation modelId) throws IOException {
		if (!blockModelIds.containsKey(modelId))
			return false;
		Path blockModelFilePath = assetsDir.resolve(modelId.getNamespace()).resolve("models").resolve(modelId.getPath());
		Files.delete(Paths.get(blockModelFilePath + ".json"));
		namespaceLoader.accept(modelId.getNamespace());
		blockModelIds.remove(modelId);
		return true;
	}

	public void setItemBlockModel(ResourceLocation itemModelId, ResourceLocation parentBlockModelId) throws IOException {
		String json = "{\n" +
				"    \"ics_itemblock_model_type\": \"block\",\n" +
				"    \"parent\": \"" + parentBlockModelId + "\"\n" +
				"}\n";
		Path filePath = assetsDir.resolve(itemModelId.getNamespace()).resolve("models").resolve(itemModelId.getPath());
		Files.createDirectories(filePath.getParent());
		Files.write(Paths.get(filePath + ".json"), json.getBytes(StandardCharsets.UTF_8));
		namespaceLoader.accept(itemModelId.getNamespace());
	}
	public void removeItemBlockModel(ResourceLocation itemModelId) throws IOException {
		Path filePath = assetsDir.resolve(itemModelId.getNamespace()).resolve("models").resolve(itemModelId.getPath());
		Files.delete(Paths.get(filePath + ".json"));
		namespaceLoader.accept(itemModelId.getNamespace());
	}


	//blockstate
	public void addBlockstate(ResourceLocation blockId, BlockStateEntry blockStateEntry) throws IOException {
		Path filePath = assetsDir.resolve(blockId.getNamespace()).resolve("blockstates").resolve(blockId.getPath());
		Files.createDirectories(filePath.getParent());
		Files.write(Paths.get(filePath + ".json"), blockStateEntry.toJson().getBytes(StandardCharsets.UTF_8));
		namespaceLoader.accept(blockId.getNamespace());
		blockStates.put(blockId, blockStateEntry);
	}
	public void replaceBlockstate(ResourceLocation blockId, BlockStateEntry blockStateEntry) throws IOException {
		addBlockstate(blockId, blockStateEntry);
	}

	@Nullable
	public BlockStateEntry getBlockState(ResourceLocation blockId) {
		return blockStates.get(blockId);
	}
	public void removeBlockState(ResourceLocation blockId) throws IOException {
		Path filePath = assetsDir.resolve(blockId.getNamespace()).resolve("blockstates").resolve(blockId.getPath());
		Files.delete(Paths.get(filePath + ".json"));
		namespaceLoader.accept(blockId.getNamespace());
		blockStates.remove(blockId);
	}

	//texture
	public boolean addTexture(ResourceLocation textureId, byte[] data, @Nullable TextureAnimationEntry animationEntry) throws IOException {
		Map<ResourceLocation, TextureAnimationEntry> map;
		if (IdConverter.isItemTextureId(textureId)) {
			if (!itemTextureIds.containsKey(textureId)) {
				map = itemTextureIds;
			} else {
				return false;
			}
		} else if (IdConverter.isBlockTextureId(textureId)) {
			if (!blockTextureIds.containsKey(textureId)) {
				map = blockTextureIds;
			} else {
				return false;
			}
		} else {
			return false;
		}
		Path filePath = assetsDir.resolve(textureId.getNamespace()).resolve("textures").resolve(textureId.getPath());
		Files.createDirectories(filePath.getParent());
		Files.write(Paths.get(filePath + ".png"), data);
		if (animationEntry != null)
			FileUtils.write(Paths.get(filePath + ".png.mcmeta").toFile(), animationEntry.toJson(), StandardCharsets.UTF_8);
		namespaceLoader.accept(textureId.getNamespace());
		map.put(textureId, animationEntry);
		return true;
	}
	public boolean replaceTexture(ResourceLocation textureId, byte[] data, @Nullable TextureAnimationEntry animationEntry) throws IOException {
		Map<ResourceLocation, TextureAnimationEntry> map;
		if (itemTextureIds.containsKey(textureId)) {
			map = itemTextureIds;
		} else if (blockTextureIds.containsKey(textureId)) {
			map = blockTextureIds;
		} else {
			return false;
		}
		Path filePath = assetsDir.resolve(textureId.getNamespace()).resolve("textures").resolve(textureId.getPath());
		Files.createDirectories(filePath.getParent());
		Files.write(Paths.get(filePath + ".png"), data);
		if (animationEntry != null)
			FileUtils.write(Paths.get(filePath + ".png.mcmeta").toFile(), animationEntry.toJson(), StandardCharsets.UTF_8);
		else if (Files.exists(Paths.get(filePath + ".png.mcmeta")))
			Files.delete(Paths.get(filePath + ".png.mcmeta"));
		map.put(textureId, animationEntry);
		return true;
	}
	public boolean removeTexture(ResourceLocation textureId) throws IOException {
		Map<ResourceLocation, TextureAnimationEntry> map;
		if (itemTextureIds.containsKey(textureId)) {
			map = itemTextureIds;
		} else if (blockTextureIds.containsKey(textureId)) {
			map = blockTextureIds;
		} else {
			return false;
		}
		Path filePath = assetsDir.resolve(textureId.getNamespace()).resolve("textures").resolve(textureId.getPath());
		Files.delete(Paths.get(filePath + ".png"));
		if (Files.exists(Paths.get(filePath + ".png.mcmeta")))
			Files.delete(Paths.get(filePath + ".png.mcmeta"));
		map.remove(textureId);
		return true;
	}


	public void unload() {
		lang.clear();
		blockStates.clear();
		itemModelIds.clear();
		blockModelIds.clear();
		blockTextureIds.clear();
		itemTextureIds.clear();
	}


	public static boolean isValidFilePath(String path) {
		if (path.isEmpty())
			return false;
		try {
			Paths.get(path);
			return true;
		} catch (InvalidPathException | NullPointerException ex) {
			return false;
		}
	}

	@Nullable
	public static String getBlockModelIdErrorMessage(ResourceLocation modelId, boolean allowAllNamespace) {
		if (!allowAllNamespace && modelId.toString().length() > ID_MAX_LENGTH)
			return I18n.format("gui.ingame_custom_stuff.error.id.too_long", ID_MAX_LENGTH);
		String namespace = modelId.getNamespace();
		if (namespace.isEmpty())
			return "Namespace is empty";
		String path = modelId.getPath();
		if (path.isEmpty())
			return "Path is empty";
		if (!allowAllNamespace) {
			if (namespace.equals("minecraft"))
				return "Namespace \"minecraft\" is not allowed";
			if (namespace.equals("forge"))
				return "Namespace \"forge\" is not allowed";
		}
		if (!namespace.matches("[a-z0-9_]+") || !path.matches("[a-z0-9_]+(/[a-z0-9_]+)*"))
			return "Only lower half-width alphanumeric and \"_\" are allowed";
		return null;
	}

	@NotNull
	public static String toTranslationKeyItem(ResourceLocation itemId) {
		return "item." + itemId.getNamespace() + "." + itemId.getPath() + ".name";
	}

	@NotNull
	public static String toTranslationKeyBlock(ResourceLocation blockId) {
		return "tile." + blockId.getNamespace() + "." + blockId.getPath() + ".name";
	}

	@Nullable
	public static String getResourceIdErrorMessage(String resourceId, boolean allowAllNamespace) {
		if (!allowAllNamespace && resourceId.length() > ID_MAX_LENGTH)
			return I18n.format("gui.ingame_custom_stuff.error.id.too_long", ID_MAX_LENGTH);
		if (!resourceId.contains(":"))
			return "gui.ingame_custom_stuff.error.namespace.empty";
		String[] split = resourceId.split(":", 2);
		String namespace = split[0];
		String path = split[1];
		if (namespace.isEmpty())
			return "gui.ingame_custom_stuff.error.namespace.empty";
		if (path.isEmpty())
			return "gui.ingame_custom_stuff.error.path.empty";
		if (!allowAllNamespace) {
			if (namespace.equals("minecraft") || namespace.equals("forge") || namespace.equals(ModTagsGenerated.MODID))
				return I18n.format("gui.ingame_custom_stuff.error.namespace.not_allowed", namespace);
		}
		if (!namespace.matches("[a-z0-9_]+") || !path.matches("[a-z0-9_]+(/[a-z0-9_]+)*"))
			return "gui.ingame_custom_stuff.error.allowed_id_characters";
		return null;
	}


	private void readFolder() {
		lang.clear();
		blockStates.clear();
		itemModelIds.clear();
		blockModelIds.clear();
		blockTextureIds.clear();
		itemTextureIds.clear();
		try {
			Files.createDirectories(assetsDir);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		//lang
		{
			try (var stream1 = Files.newDirectoryStream(assetsDir)) {
				for (Path namespace_dir : stream1) {
					String namespace = namespace_dir.getFileName().toString();
					Path dir = namespace_dir.resolve("lang");
					if (Files.exists(dir)) {
						try (Stream<Path> stream = Files.list(dir)) {
							stream.filter(Files::isRegularFile).forEach(p -> {
								String fileName = p.getFileName().toString();
								String langKey = fileName.substring(0, fileName.lastIndexOf('.'));
								Map<String, String> translateMap = lang.computeIfAbsent(namespace, k -> new HashMap<>()).computeIfAbsent(langKey, k -> new HashMap<>());
								try {
									List<String> lines = Files.readAllLines(p, StandardCharsets.UTF_8);
									for (String line : lines) {
										if (!line.isEmpty() && line.charAt(0) != '#') {
											String[] astring = Iterables.toArray(SPLITTER.split(line), String.class);
											if (astring != null && astring.length == 2) {
												String s1 = astring[0];
												String s2 = PATTERN.matcher(astring[1]).replaceAll("%$1s");
												translateMap.put(s1, s2);
											}
										}
									}
								} catch (IOException e) {
									throw new RuntimeException(e);
								}
							});
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
						namespaceLoader.accept(namespace);
					}
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		//blockStates
		{
			try (var stream1 = Files.newDirectoryStream(assetsDir)) {
				for (Path namespace_dir : stream1) {
					String namespace = namespace_dir.getFileName().toString();
					Path dir = namespace_dir.resolve("blockstates");
					if (Files.exists(dir)) {
						try (Stream<Path> stream = Files.list(dir)) {
							stream.filter(Files::isRegularFile).forEach(p -> {
								String path = dir.relativize(p).toString().replace('\\', '/');
								try {
									BlockStateEntry blockStateEntry = BlockStateEntry.fromJson(FileUtils.readFileToString(p.toFile(), StandardCharsets.UTF_8));
									if (blockStateEntry != null) {
										blockStates.put(new ResourceLocation(namespace, path.substring(0, path.lastIndexOf('.'))), blockStateEntry);
									}
								} catch (IOException e) {
									throw new RuntimeException(e);
								} catch (JsonSyntaxException e) {
									throw new RuntimeException("Invalid json file:" + p, e);
								}
							});
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
						namespaceLoader.accept(namespace);
					}
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		//blockModels/itemModels
		{
			try (var stream1 = Files.newDirectoryStream(assetsDir)) {
				for (Path namespace_dir : stream1) {
					String namespace = namespace_dir.getFileName().toString();
					Path dir = namespace_dir.resolve("models");
					Path dirItem = dir.resolve("item");
					if (Files.exists(dirItem)) {
						try (Stream<Path> stream = Files.find(dirItem, Integer.MAX_VALUE, (filePath, fileAttr) -> fileAttr.isRegularFile())) {
							stream.forEach(p -> {
								String path = dir.relativize(p).toString().replace('\\', '/');
								try {
									ItemModelEntry itemModelEntry = ItemModelEntry.fromJson(FileUtils.readFileToString(p.toFile(), StandardCharsets.UTF_8));
									if (itemModelEntry != null)
										itemModelIds.put(new ResourceLocation(namespace, path.substring(0, path.lastIndexOf('.'))), itemModelEntry);
								} catch (IOException e) {
									throw new RuntimeException(e);
								} catch (JsonSyntaxException e) {
									throw new RuntimeException("Invalid json file:" + p, e);
								}
							});
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					}
					Path dirBlock = dir.resolve("block");
					if (Files.exists(dirBlock)) {
						try (Stream<Path> stream = Files.find(dirBlock, Integer.MAX_VALUE, (filePath, fileAttr) -> fileAttr.isRegularFile())) {
							stream.forEach(p -> {
								String path = dir.relativize(p).toString().replace('\\', '/');
								try {
									blockModelIds.put(new ResourceLocation(namespace, path.substring(0, path.lastIndexOf('.'))), BlockModelEntry.fromJson(FileUtils.readFileToString(p.toFile(), StandardCharsets.UTF_8)));
								} catch (IOException e) {
									throw new RuntimeException(e);
								} catch (JsonSyntaxException e) {
									throw new RuntimeException("Invalid json file:" + p, e);
								}
							});
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					}
					namespaceLoader.accept(namespace);
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		//blockTexture/itemTexture
		{
			try (var stream1 = Files.newDirectoryStream(assetsDir)) {
				for (Path namespace_dir : stream1) {
					String namespace = namespace_dir.getFileName().toString();
					Path dir = namespace_dir.resolve("textures");
					Path dirItems = dir.resolve("items");
					if (Files.exists(dirItems)) {
						try (Stream<Path> stream = Files.find(dirItems, Integer.MAX_VALUE, (filePath, fileAttr) -> fileAttr.isRegularFile())) {
							stream.forEach(p -> {
								String path = dir.relativize(p).toString().replace('\\', '/');
								if (path.endsWith(".mcmeta"))
									return;
								ResourceLocation textureId = new ResourceLocation(namespace, path.substring(0, path.lastIndexOf('.')));
								@Nullable TextureAnimationEntry animationEntry = null;
								Path animation_path = Paths.get(p + ".mcmeta");
								if (Files.exists(animation_path)) {
									try {
										String json = FileUtils.readFileToString(animation_path.toFile(), StandardCharsets.UTF_8);
										animationEntry = TextureAnimationEntry.fromJson(json);
									} catch (IOException | JsonSyntaxException e) {
										throw new RuntimeException(e);
									}
								}
								itemTextureIds.put(textureId, animationEntry);
							});
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					}
					Path dirBlocks = dir.resolve("blocks");
					if (Files.exists(dirBlocks)) {
						try (Stream<Path> stream = Files.find(dirBlocks, Integer.MAX_VALUE, (filePath, fileAttr) -> fileAttr.isRegularFile())) {
							stream.forEach(p -> {
								String path = dir.relativize(p).toString().replace('\\', '/');
								if (path.endsWith(".mcmeta"))
									return;
								ResourceLocation textureId = new ResourceLocation(namespace, path.substring(0, path.lastIndexOf('.')));
								@Nullable TextureAnimationEntry animationEntry = null;
								Path animation_path = Paths.get(p + ".mcmeta");
								if (Files.exists(animation_path)) {
									try {
										String json = FileUtils.readFileToString(animation_path.toFile(), StandardCharsets.UTF_8);
										animationEntry = TextureAnimationEntry.fromJson(json);
									} catch (IOException | JsonSyntaxException e) {
										throw new RuntimeException(e);
									}
								}
								blockTextureIds.put(textureId, animationEntry);
							});
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					}
					namespaceLoader.accept(namespace);
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public static class Server {
		public static Path resourcePackDir = null;
		public static DynamicResourceManager INSTANCE = null;
		public static void init(MinecraftServer server) {
			resourcePackDir = server.getActiveAnvilConverter().getFile(server.getFolderName(), ModTagsGenerated.MODID).toPath().resolve("resourcepack");
			INSTANCE = new DynamicResourceManager(resourcePackDir, n -> {
			});
			INSTANCE.readFolder();
		}
	}

	public static class ClientCache {
		public static final Path resourcePackPath = Paths.get(Minecraft.getMinecraft().gameDir.getPath(), "server-resource-packs", ModTagsGenerated.MODID);
		@SuppressWarnings("Convert2MethodRef")//循環参照になる
		public static final DynamicResourceManager INSTANCE = new DynamicResourceManager(resourcePackPath, n -> ModResourcePack.CLIENT_CACHE.addNamespace(n));
		public static void readFromFolder() {
			INSTANCE.readFolder();
		}

	}

}
