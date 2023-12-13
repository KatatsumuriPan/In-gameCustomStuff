package kpan.ig_custom_stuff.resource;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.gson.JsonSyntaxException;
import kpan.ig_custom_stuff.ModTagsGenerated;
import kpan.ig_custom_stuff.block.BlockModelEntry;
import kpan.ig_custom_stuff.block.BlockStateEntry;
import kpan.ig_custom_stuff.item.model.ItemModelEntry;
import kpan.ig_custom_stuff.resource.ids.BlockId;
import kpan.ig_custom_stuff.resource.ids.BlockModelGroupId;
import kpan.ig_custom_stuff.resource.ids.BlockModelId;
import kpan.ig_custom_stuff.resource.ids.BlockStateId;
import kpan.ig_custom_stuff.resource.ids.BlockTextureId;
import kpan.ig_custom_stuff.resource.ids.ItemId;
import kpan.ig_custom_stuff.resource.ids.ItemModelId;
import kpan.ig_custom_stuff.resource.ids.ItemTextureId;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.server.MinecraftServer;
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
	public final Map<BlockStateId, BlockStateEntry> blockStates = new TreeMap<>();
	public final Map<ItemModelId, ItemModelEntry> itemModelIds = new TreeMap<>();
	public final Map<BlockModelId, BlockModelEntry> blockModelIds = new TreeMap<>();
	public final Map<BlockTextureId, @Nullable TextureAnimationEntry> blockTextureIds = new TreeMap<>();
	public final Map<ItemTextureId, @Nullable TextureAnimationEntry> itemTextureIds = new TreeMap<>();

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
	public String getItemNameLang(String langKey, ItemId itemId) {
		String translationKey = toTranslationKeyItem(itemId);
		String lang = getLang(itemId.namespace, langKey, translationKey);
		return lang != null ? lang : translationKey;
	}
	public boolean hasItemNameLang(String langKey, ItemId itemId) {
		String translationKey = toTranslationKeyItem(itemId);
		return getLang(itemId.namespace, langKey, translationKey) != null;
	}
	public void removeItemNameLang(String langKey, ItemId itemId) throws IOException {
		removeLang(itemId.namespace, langKey, toTranslationKeyItem(itemId));
	}
	public String getBlockNameLang(String langKey, BlockId blockId) {
		return getLang(blockId.namespace, langKey, toTranslationKeyBlock(blockId));
	}
	public boolean hasBlockNameLang(String langKey, BlockId blockId) {
		String translationKey = toTranslationKeyBlock(blockId);
		return getLang(blockId.namespace, langKey, translationKey) != null;
	}
	public void removeBlockNameLang(String langKey, BlockId blockId) throws IOException {
		removeLang(blockId.namespace, langKey, toTranslationKeyBlock(blockId));
	}

	//model
	//itemModel
	public boolean isItemModelAdded(ItemModelId itemModelId) {
		return itemModelIds.containsKey(itemModelId);
	}
	public void addModel(ItemId itemId, ItemModelEntry itemModelEntry) throws IOException {
		ItemModelId modelId = itemId.toModelId();
		Path filePath = assetsDir.resolve(modelId.namespace).resolve("models").resolve(modelId.toResourceLocation().getPath());
		Files.createDirectories(filePath.getParent());
		Files.write(Paths.get(filePath + ".json"), itemModelEntry.toJson().getBytes(StandardCharsets.UTF_8));
		namespaceLoader.accept(modelId.namespace);
		itemModelIds.put(modelId, itemModelEntry);
	}
	@Nullable
	public ItemModelEntry getItemModel(ItemId itemId) {
		return itemModelIds.get(itemId.toModelId());
	}
	public void removeItemModel(ItemId itemId) throws IOException {
		ItemModelId modelId = itemId.toModelId();
		Path filePath = assetsDir.resolve(modelId.namespace).resolve("models").resolve(modelId.toResourceLocation().getPath());
		Files.delete(Paths.get(filePath + ".json"));
		namespaceLoader.accept(modelId.namespace);
		itemModelIds.remove(modelId);
	}
	//blockModel
	public boolean isBlockModelAdded(BlockModelId blockModelId) {
		return blockModelIds.containsKey(blockModelId);
	}
	public boolean addBlockModel(BlockModelId modelId, BlockModelEntry blockModelEntry) throws IOException {
		if (blockModelIds.containsKey(modelId))
			return false;
		Path blockModelFilePath = assetsDir.resolve(modelId.namespace).resolve("models").resolve(modelId.toResourceLocation().getPath());
		Files.createDirectories(blockModelFilePath.getParent());
		blockModelEntry.saveToFiles(blockModelFilePath);
		namespaceLoader.accept(modelId.namespace);
		blockModelIds.put(modelId, blockModelEntry);
		return true;
	}
	public boolean replaceBlockModel(BlockModelId modelId, BlockModelEntry blockModelEntry) throws IOException {
		if (!blockModelIds.containsKey(modelId))
			return false;
		Path blockModelFilePath = assetsDir.resolve(modelId.namespace).resolve("models").resolve(modelId.toResourceLocation().getPath());
		Files.createDirectories(blockModelFilePath.getParent());
		blockModelIds.get(modelId).deleteFiles(blockModelFilePath);
		blockModelEntry.saveToFiles(blockModelFilePath);
		namespaceLoader.accept(modelId.namespace);
		blockModelIds.put(modelId, blockModelEntry);
		return true;
	}
	@Nullable
	public BlockModelEntry getBlockModel(BlockModelId modelId) {
		return blockModelIds.get(modelId);
	}
	public boolean removeBlockModel(BlockModelId modelId) throws IOException {
		if (!blockModelIds.containsKey(modelId))
			return false;
		Path blockModelFilePath = assetsDir.resolve(modelId.namespace).resolve("models").resolve(modelId.toResourceLocation().getPath());
		blockModelIds.remove(modelId).deleteFiles(blockModelFilePath);
		namespaceLoader.accept(modelId.namespace);
		return true;
	}

	public void setItemBlockModel(ItemModelId itemModelId, BlockStateEntry blockStateEntry) throws IOException {
		Path filePath = assetsDir.resolve(itemModelId.namespace).resolve("models").resolve(itemModelId.toResourceLocation().getPath());
		Files.createDirectories(filePath.getParent());
		Files.write(Paths.get(filePath + ".json"), blockStateEntry.getItemModelJson(this).getBytes(StandardCharsets.UTF_8));
		namespaceLoader.accept(itemModelId.namespace);
	}
	public void removeItemBlockModel(ItemModelId itemModelId) throws IOException {
		Path filePath = assetsDir.resolve(itemModelId.namespace).resolve("models").resolve(itemModelId.toResourceLocation().getPath());
		Files.delete(Paths.get(filePath + ".json"));
		namespaceLoader.accept(itemModelId.namespace);
	}


	//blockstate
	public void addBlockstate(BlockId blockId, BlockStateEntry blockStateEntry) throws IOException {
		Path filePath = assetsDir.resolve(blockId.namespace).resolve("blockstates").resolve(blockId.name);
		Files.createDirectories(filePath.getParent());
		Files.write(Paths.get(filePath + ".json"), blockStateEntry.toJson().getBytes(StandardCharsets.UTF_8));
		namespaceLoader.accept(blockId.namespace);
		blockStates.put(blockId.toBlockStateId(), blockStateEntry);
	}
	public void replaceBlockstate(BlockId blockId, BlockStateEntry blockStateEntry) throws IOException {
		addBlockstate(blockId, blockStateEntry);
	}

	@Nullable
	public BlockStateEntry getBlockState(BlockStateId blockStateId) {
		return blockStates.get(blockStateId);
	}
	public void removeBlockState(BlockStateId blockStateId) throws IOException {
		Path filePath = assetsDir.resolve(blockStateId.namespace).resolve("blockstates").resolve(blockStateId.name);
		Files.delete(Paths.get(filePath + ".json"));
		namespaceLoader.accept(blockStateId.namespace);
		blockStates.remove(blockStateId);
	}

	//texture
	public boolean addTexture(ItemTextureId textureId, byte[] data, @Nullable TextureAnimationEntry animationEntry) throws IOException {
		if (itemTextureIds.containsKey(textureId))
			return false;
		Path filePath = assetsDir.resolve(textureId.namespace).resolve("textures").resolve(textureId.toResourceLocation().getPath());
		Files.createDirectories(filePath.getParent());
		Files.write(Paths.get(filePath + ".png"), data);
		if (animationEntry != null)
			FileUtils.write(Paths.get(filePath + ".png.mcmeta").toFile(), animationEntry.toJson(), StandardCharsets.UTF_8);
		namespaceLoader.accept(textureId.namespace);
		itemTextureIds.put(textureId, animationEntry);
		return true;
	}
	public boolean addTexture(BlockTextureId textureId, byte[] data, @Nullable TextureAnimationEntry animationEntry) throws IOException {
		if (blockTextureIds.containsKey(textureId))
			return false;
		Path filePath = assetsDir.resolve(textureId.namespace).resolve("textures").resolve(textureId.toResourceLocation().getPath());
		Files.createDirectories(filePath.getParent());
		Files.write(Paths.get(filePath + ".png"), data);
		if (animationEntry != null)
			FileUtils.write(Paths.get(filePath + ".png.mcmeta").toFile(), animationEntry.toJson(), StandardCharsets.UTF_8);
		namespaceLoader.accept(textureId.namespace);
		blockTextureIds.put(textureId, animationEntry);
		return true;
	}

	public boolean replaceTexture(ItemTextureId textureId, byte[] data, @Nullable TextureAnimationEntry animationEntry) throws IOException {
		if (!itemTextureIds.containsKey(textureId))
			return false;
		Path filePath = assetsDir.resolve(textureId.namespace).resolve("textures").resolve(textureId.toResourceLocation().getPath());
		Files.createDirectories(filePath.getParent());
		Files.write(Paths.get(filePath + ".png"), data);
		if (animationEntry != null)
			FileUtils.write(Paths.get(filePath + ".png.mcmeta").toFile(), animationEntry.toJson(), StandardCharsets.UTF_8);
		else if (Files.exists(Paths.get(filePath + ".png.mcmeta")))
			Files.delete(Paths.get(filePath + ".png.mcmeta"));
		itemTextureIds.put(textureId, animationEntry);
		return true;
	}
	public boolean replaceTexture(BlockTextureId textureId, byte[] data, @Nullable TextureAnimationEntry animationEntry) throws IOException {
		if (!blockTextureIds.containsKey(textureId))
			return false;
		Path filePath = assetsDir.resolve(textureId.namespace).resolve("textures").resolve(textureId.toResourceLocation().getPath());
		Files.createDirectories(filePath.getParent());
		Files.write(Paths.get(filePath + ".png"), data);
		if (animationEntry != null)
			FileUtils.write(Paths.get(filePath + ".png.mcmeta").toFile(), animationEntry.toJson(), StandardCharsets.UTF_8);
		else if (Files.exists(Paths.get(filePath + ".png.mcmeta")))
			Files.delete(Paths.get(filePath + ".png.mcmeta"));
		blockTextureIds.put(textureId, animationEntry);
		return true;
	}
	public boolean removeTexture(ItemTextureId textureId) throws IOException {
		if (!itemTextureIds.containsKey(textureId))
			return false;
		Path filePath = assetsDir.resolve(textureId.namespace).resolve("textures").resolve(textureId.toResourceLocation().getPath());
		Files.delete(Paths.get(filePath + ".png"));
		if (Files.exists(Paths.get(filePath + ".png.mcmeta")))
			Files.delete(Paths.get(filePath + ".png.mcmeta"));
		itemTextureIds.remove(textureId);
		return true;
	}
	public boolean removeTexture(BlockTextureId textureId) throws IOException {
		if (!blockTextureIds.containsKey(textureId))
			return false;
		Path filePath = assetsDir.resolve(textureId.namespace).resolve("textures").resolve(textureId.toResourceLocation().getPath());
		Files.delete(Paths.get(filePath + ".png"));
		if (Files.exists(Paths.get(filePath + ".png.mcmeta")))
			Files.delete(Paths.get(filePath + ".png.mcmeta"));
		blockTextureIds.remove(textureId);
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
	public static String getBlockModelIdErrorMessage(BlockModelId modelId, boolean allowAllNamespace) {
		if (!allowAllNamespace && modelId.toString().length() > ID_MAX_LENGTH)
			return I18n.format("gui.ingame_custom_stuff.error.id.too_long", ID_MAX_LENGTH);
		String namespace = modelId.namespace;
		if (namespace.isEmpty())
			return "Namespace is empty";
		String path = modelId.path;
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

	@Nullable
	public static String getBlockModelIdErrorMessage(BlockModelGroupId modelGroupId, boolean allowAllNamespace) {
		if (!allowAllNamespace && modelGroupId.toString().length() > ID_MAX_LENGTH)
			return I18n.format("gui.ingame_custom_stuff.error.id.too_long", ID_MAX_LENGTH);
		String namespace = modelGroupId.namespace;
		if (namespace.isEmpty())
			return "Namespace is empty";
		String path = modelGroupId.path;
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
	public static String toTranslationKeyItem(ItemId itemId) {
		return "item." + itemId.namespace + "." + itemId.name + ".name";
	}

	@NotNull
	public static String toTranslationKeyBlock(BlockId blockId) {
		return "tile." + blockId.namespace + "." + blockId.name + ".name";
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
										blockStates.put(new BlockStateId(namespace, path.substring(0, path.lastIndexOf('.'))), blockStateEntry);
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
								String path = dirItem.relativize(p).toString().replace('\\', '/');
								try {
									ItemModelEntry itemModelEntry = ItemModelEntry.fromJson(FileUtils.readFileToString(p.toFile(), StandardCharsets.UTF_8));
									if (itemModelEntry != null)
										itemModelIds.put(new ItemModelId(namespace, path.substring(0, path.lastIndexOf('.'))), itemModelEntry);
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
					//normal
					{
						Path dirNormal = dirBlock.resolve("normal");
						if (Files.exists(dirNormal)) {
							try (Stream<Path> stream = Files.find(dirNormal, Integer.MAX_VALUE, (filePath, fileAttr) -> fileAttr.isRegularFile())) {
								stream.forEach(p -> {
									String path = dirBlock.relativize(p).toString().replace('\\', '/');
									try {
										blockModelIds.put(new BlockModelId(namespace, path.substring(0, path.lastIndexOf('.'))), BlockModelEntry.fromJson(FileUtils.readFileToString(p.toFile(), StandardCharsets.UTF_8)));
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
					}
					//slab
					{
						Path dirSlab = dirBlock.resolve("slab");
						if (Files.exists(dirSlab)) {
							try (Stream<Path> stream = Files.find(dirSlab, Integer.MAX_VALUE, (filePath, fileAttr) -> fileAttr.isRegularFile())) {
								stream.forEach(p -> {
									String path = dirBlock.relativize(p).toString().replace('\\', '/');
									path = path.substring(0, path.lastIndexOf('.'));
									try {
										blockModelIds.put(new BlockModelId(namespace, path), BlockModelEntry.fromJson(FileUtils.readFileToString(p.toFile(), StandardCharsets.UTF_8)));
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
								String path = dirItems.relativize(p).toString().replace('\\', '/');
								if (path.endsWith(".mcmeta"))
									return;
								ItemTextureId textureId = new ItemTextureId(namespace, path.substring(0, path.lastIndexOf('.')));
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
								String path = dirBlocks.relativize(p).toString().replace('\\', '/');
								if (path.endsWith(".mcmeta"))
									return;
								BlockTextureId textureId = new BlockTextureId(namespace, path.substring(0, path.lastIndexOf('.')));
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
			checkAndUpdateVersion();
			INSTANCE.readFolder();
		}

		private static void checkAndUpdateVersion() {
			try {
				Path version_path = resourcePackDir.getParent().resolve("resourcepack_version.txt");
				if (!Files.exists(version_path)) {
					//初期バージョン
					updateBlockModelDirectory();
				} else {
					String version = FileUtils.readFileToString(version_path.toFile(), StandardCharsets.UTF_8);
					switch (version) {
						case "1" -> {
							//最新
						}
						default -> throw new RuntimeException("Unknown resource pack version:" + version);
					}
				}
				Files.write(version_path, "1".getBytes(StandardCharsets.UTF_8));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}


		private static void updateBlockModelDirectory() {
			List<BlockModelId> updatedBlockModelIds = new ArrayList<>();
			//まずはblockmodelの移動
			{
				try (var stream1 = Files.newDirectoryStream(INSTANCE.assetsDir)) {
					for (Path namespace_dir : stream1) {
						String namespace = namespace_dir.getFileName().toString();
						Path dir = namespace_dir.resolve("models");
						Path dirBlock = dir.resolve("block");
						if (Files.exists(dirBlock)) {
							try (Stream<Path> stream = Files.find(dirBlock, Integer.MAX_VALUE, (filePath, fileAttr) -> fileAttr.isRegularFile())) {
								stream.forEach(p -> {
									String path = dirBlock.relativize(p).toString().replace('\\', '/');
									try {
										BlockModelId blockModelId = new BlockModelId(namespace, path.substring(0, path.lastIndexOf('.')));
										updatedBlockModelIds.add(blockModelId);
										Path new_path = dirBlock.resolve("normal").resolve(dirBlock.relativize(p));
										Files.createDirectories(new_path.getParent());
										Files.move(p, new_path);
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
					}
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
			if (updatedBlockModelIds.isEmpty())
				return;

			//blockStatesのアップデート
			{
				try (var stream1 = Files.newDirectoryStream(INSTANCE.assetsDir)) {
					for (Path namespace_dir : stream1) {
						Path dir = namespace_dir.resolve("blockstates");
						if (Files.exists(dir)) {
							try (Stream<Path> stream = Files.list(dir)) {
								stream.filter(Files::isRegularFile).forEach(p -> {
									try {
										String json = FileUtils.readFileToString(p.toFile(), StandardCharsets.UTF_8);
										for (BlockModelId updatedBlockModelId : updatedBlockModelIds) {
											json = json.replace(updatedBlockModelId.namespace + ":" + updatedBlockModelId.path, updatedBlockModelId.namespace + ":normal/" + updatedBlockModelId.path);
										}
										Files.write(p, json.getBytes(StandardCharsets.UTF_8));
									} catch (IOException e) {
										throw new RuntimeException(e);
									}
								});
							} catch (IOException e) {
								throw new RuntimeException(e);
							}
						}
					}
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
			//itemModelsのアップデート
			{
				try (var stream1 = Files.newDirectoryStream(INSTANCE.assetsDir)) {
					for (Path namespace_dir : stream1) {
						String namespace = namespace_dir.getFileName().toString();
						Path dir = namespace_dir.resolve("models");
						Path dirItem = dir.resolve("item");
						if (Files.exists(dirItem)) {
							try (Stream<Path> stream = Files.find(dirItem, Integer.MAX_VALUE, (filePath, fileAttr) -> fileAttr.isRegularFile())) {
								stream.forEach(p -> {
									try {
										String json = FileUtils.readFileToString(p.toFile(), StandardCharsets.UTF_8);
										for (BlockModelId updatedBlockModelId : updatedBlockModelIds) {
											json = json.replace(updatedBlockModelId.toString(), updatedBlockModelId.namespace + ":block/normal/" + updatedBlockModelId.path.substring("block/".length()));
										}
										Files.write(p, json.getBytes(StandardCharsets.UTF_8));
									} catch (IOException e) {
										throw new RuntimeException(e);
									}
								});
							} catch (IOException e) {
								throw new RuntimeException(e);
							}
						}
					}
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
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
