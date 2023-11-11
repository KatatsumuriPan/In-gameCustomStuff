package kpan.ig_custom_stuff.resource;

import com.google.common.base.Joiner;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import kpan.ig_custom_stuff.ModMain;
import kpan.ig_custom_stuff.ModTagsGenerated;
import kpan.ig_custom_stuff.asm.core.MyAsmNameRemapper;
import kpan.ig_custom_stuff.block.DynamicBlockBase;
import kpan.ig_custom_stuff.registry.MCRegistryUtil;
import kpan.ig_custom_stuff.resource.DynamicResourceManager.ClientCache;
import kpan.ig_custom_stuff.resource.StitchManager.StitchFullSpaceException;
import kpan.ig_custom_stuff.util.MyReflectionHelper;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.client.renderer.block.model.ModelManager;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.BlockStateMapper;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.animation.ModelBlockAnimation;
import net.minecraftforge.client.resource.VanillaResourceType;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLLog;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DynamicResourceLoader {

	public static final Map<ResourceLocation, TextureAtlasSprite> unregisteredTextureCache = new HashMap<>();
	public static final Multimap<ResourceLocation, ResourceLocation> itemModelTextureDependencies = HashMultimap.create();//textureId2itemModelId
	public static final Multimap<ResourceLocation, ResourceLocation> blockModelTextureDependencies = HashMultimap.create();//textureId2blockModelId
	public static final Multimap<ResourceLocation, ResourceLocation> blockModelDependencies = HashMultimap.create();//modelId2blockId
	public static final ICustomModelLoader vanillaLoaderInstance;
	private static final Function<ResourceLocation, TextureAtlasSprite> DefaultTextureGetter_INSTANCE;
	public static final List<ResourceLocation> VANILLA_BLOCK_TEXTURES = new ArrayList<>();//textureId
	public static final List<ResourceLocation> VANILLA_ITEM_TEXTURES = new ArrayList<>();//textureId
	public static final String REMOVED_ITEM_NAME = "REMOVED";
	public static final String REMOVED_BLOCK_NAME = "REMOVED";

	static {
		vanillaLoaderInstance = MyReflectionHelper.getPrivateStaticField("net.minecraftforge.client.model.ModelLoader$VanillaLoader", "INSTANCE");
		DefaultTextureGetter_INSTANCE = MyReflectionHelper.getPrivateStaticField("net.minecraftforge.client.model.ModelLoader$DefaultTextureGetter", "INSTANCE");
		//noinspection DataFlowIssue
		new BufferedReader(new InputStreamReader(MyAsmNameRemapper.class.getResourceAsStream("/minecraft_block_textures.txt"))).lines().forEach(line -> {
					VANILLA_BLOCK_TEXTURES.add(new ResourceLocation(line));
				}
		);
		//noinspection DataFlowIssue
		new BufferedReader(new InputStreamReader(MyAsmNameRemapper.class.getResourceAsStream("/minecraft_item_textures.txt"))).lines().forEach(line -> {
					VANILLA_ITEM_TEXTURES.add(new ResourceLocation(line));
				}
		);
	}

	public static void loadItemModels(DynamicResourceManager instance) {
		loadItemModels(instance.itemModelIds.keySet().stream().map(IdConverter::itemModelId2itemModelName).collect(Collectors.toList()));
	}
	public static void loadItemModels(Iterable<ResourceLocation> itemModelNames) {
		loadVariantModels(stateModels ->
		{
			ModelLoader modelLoader = MyReflectionHelper.invokePrivateMethod(MyReflectionHelper.getPublicStaticField("net.minecraftforge.client.model.ModelLoader$VanillaLoader", "INSTANCE"), "getLoader");
			for (ResourceLocation itemModelName : itemModelNames) {
				ResourceLocation file = modelLoader.getItemLocation(itemModelName.toString());//modelId
				ModelResourceLocation memory = ModelLoader.getInventoryVariant(itemModelName.toString());
				IModel model = ModelLoaderRegistry.getMissingModel();
				Exception exception = null;
				removeModelCache(memory);
				Map<ResourceLocation, ResourceLocation> aliases = MyReflectionHelper.getPrivateStaticField(ModelLoaderRegistry.class, "aliases");
				aliases.remove(memory);
				try {
					model = ModelLoaderRegistry.getModel(memory);
				} catch (Exception blockstateException) {
					try {
						removeModelCache(file);
						model = ModelLoaderRegistry.getModel(file);
						MyReflectionHelper.invokePrivateStaticMethod(ModelLoaderRegistry.class, "addAlias", new Class<?>[]{ResourceLocation.class, ResourceLocation.class}, new Object[]{memory, file});
					} catch (Exception normalException) {
						exception = new RuntimeException("Could not load item model either from the normal location " + file + " or from the blockstate");
					}
				}
				if (exception != null) {
					FMLLog.log.error(exception);
					model = MyReflectionHelper.invokePrivateStaticMethod(ModelLoaderRegistry.class, "getMissingModel", new Class<?>[]{ResourceLocation.class, Throwable.class}, new Object[]{memory, exception});
				}
				stateModels.put(memory, model);
			}
		});
	}
	public static void loadBlockModel(ResourceLocation blockId) {
		Block block = Block.REGISTRY.getObject(blockId);
		if (block instanceof DynamicBlockBase)
			loadBlockModel((DynamicBlockBase) block);
	}
	public static void loadBlockModel(DynamicBlockBase block) {
		ModelLoader modelLoader = MyReflectionHelper.invokePrivateMethod(MyReflectionHelper.getPublicStaticField("net.minecraftforge.client.model.ModelLoader$VanillaLoader", "INSTANCE"), "getLoader");
		BlockStateMapper mapper = modelLoader.blockModelShapes.getBlockStateMapper();

		loadVariantModels(stateModels -> {
			Map<ModelResourceLocation, IModel> original_stateModels = MyReflectionHelper.getPrivateField(modelLoader, "stateModels");
			MyReflectionHelper.setPrivateField(modelLoader, "stateModels", stateModels);
			for (ResourceLocation blockStateId : mapper.getBlockstateLocations(block)) {
				ResourceLocation blockStateLocation = new ResourceLocation(blockStateId.getNamespace(), "blockstates/" + blockStateId.getPath() + ".json");
				//キャッシュ削除
				modelLoader.blockDefinitions.remove(blockStateLocation);
				Map<IBlockState, ModelResourceLocation> variants = mapper.getVariants(block);
				for (ModelResourceLocation modelName : variants.values()) {
					removeModelCache(modelName);
				}
				//ロード
				//ModelBaker.loadBlock
				MyReflectionHelper.invokePrivateMethod(ModelBakery.class, modelLoader, "loadBlock", new Class[]{BlockStateMapper.class, Block.class, ResourceLocation.class}, new Object[]{mapper, block, blockStateId});
			}
			MyReflectionHelper.setPrivateField(modelLoader, "stateModels", original_stateModels);
		});

		ModelManager modelManager = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getModelManager();
		Map<IBlockState, IBakedModel> bakedModelStore = modelLoader.blockModelShapes.bakedModelStore;
		for (Entry<IBlockState, ModelResourceLocation> entry : mapper.getVariants(block).entrySet()) {
			bakedModelStore.put(entry.getKey(), modelManager.getModel(entry.getValue()));
		}
	}
	public static void addBlockModelDependency(ResourceLocation blockId, Collection<ResourceLocation> modelIds) {
		for (ResourceLocation modelId : modelIds) {
			if (modelId.getNamespace().equals("minecraft"))
				continue;
			blockModelDependencies.put(modelId, blockId);
		}
	}
	public static void reloadBlockModelDependants(ResourceLocation modelId) {
		Collection<ResourceLocation> blockIds = blockModelDependencies.get(modelId);
		if (!blockIds.isEmpty()) {
			for (ResourceLocation blockId : blockIds) {
				loadBlockResources(blockId);
			}
			DynamicResourceLoader.loadItemModels(blockIds);
			for (ResourceLocation blockId : blockIds) {
				reloadItemModelMesh(blockId);
			}
		}
		SingleBlockModelLoader.loadBlockModel(modelId);
	}

	public static void reloadItemModelMesh(ResourceLocation itemOrBlockId) {
		ItemModelMesher itemModelMesher = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();
		Item item = Item.REGISTRY.getObject(itemOrBlockId);
		ModelResourceLocation modelResourceLocation = new ModelResourceLocation(itemOrBlockId, "inventory");
		itemModelMesher.register(item, 0, modelResourceLocation);
	}

	public static void loadBlockResources(ResourceLocation blockId) {
		DynamicResourceLoader.loadBlockModel(blockId);
		DynamicResourceLoader.loadItemModels(Collections.singletonList(blockId));
		DynamicResourceLoader.reloadItemModelMesh(blockId);
	}

	@SuppressWarnings("unchecked")
	private static void loadVariantModels(Consumer<Map<ModelResourceLocation, IModel>> loader) {
		ModelLoader modelLoader = MyReflectionHelper.invokePrivateMethod(MyReflectionHelper.getPublicStaticField("net.minecraftforge.client.model.ModelLoader$VanillaLoader", "INSTANCE"), "getLoader");

		//ModelManager.setupModelRegistry()
		//load
		{
			Map<ModelResourceLocation, IModel> stateModels = new HashMap<>();
			loader.accept(stateModels);
			if (stateModels.isEmpty())
				return;
			((Map<ModelResourceLocation, IModel>) MyReflectionHelper.getPrivateField(modelLoader, "stateModels")).putAll(stateModels);

			bakeModels(stateModels);
		}
	}
	private static void removeModelCache(ResourceLocation resourceLocation) {
		Map<ResourceLocation, IModel> cache = MyReflectionHelper.getPrivateStaticField(ModelLoaderRegistry.class, "cache");
		Map<ResourceLocation, ResourceLocation> aliases = MyReflectionHelper.getPrivateStaticField(ModelLoaderRegistry.class, "aliases");
		IModel removed = cache.remove(resourceLocation);
		if (removed != null && removed.getClass().getSimpleName().equals("WeightedRandomModel")) {
			List<ResourceLocation> locations = MyReflectionHelper.getPrivateField(removed, "locations");
			for (ResourceLocation location : locations) {
				removeModelCache(location);
			}
		}
		ResourceLocation aliase = aliases.get(resourceLocation);
		if (aliase != null) {
			IModel removed1 = cache.remove(aliase);
			if (removed1 != null && removed1.getClass().getSimpleName().equals("WeightedRandomModel")) {
				List<ResourceLocation> locations = MyReflectionHelper.getPrivateField(removed1, "locations");
				for (ResourceLocation location : locations) {
					removeModelCache(location);
				}
			}
		}
	}
	private static void bakeModels(Map<ModelResourceLocation, IModel> stateModels) {
		ModelLoader modelLoader = MyReflectionHelper.invokePrivateMethod(MyReflectionHelper.getPublicStaticField("net.minecraftforge.client.model.ModelLoader$VanillaLoader", "INSTANCE"), "getLoader");
		IModel missingModel = MyReflectionHelper.getPrivateField(modelLoader, "missingModel");
		IBakedModel missingBaked = missingModel.bake(missingModel.getDefaultState(), DefaultVertexFormats.ITEM, DefaultTextureGetter_INSTANCE);

		HashMultimap<IModel, ModelResourceLocation> models = HashMultimap.create();
		Multimaps.invertFrom(Multimaps.forMap(stateModels), models);

		Map<IModel, IBakedModel> bakedModels = Maps.newHashMap();
		for (IModel model : models.keySet()) {
			if (model == MyReflectionHelper.invokePrivateMethod(modelLoader, "getMissingModel")) {
				bakedModels.put(model, missingBaked);
			} else {
				removeBakedCache(model, modelLoader);
				try {
					bakedModels.put(model, bakeModel(model));
				} catch (Exception e) {
					String modelLocations = "[" + Joiner.on(", ").join(models.get(model)) + "]";
					FMLLog.log.error("Exception baking model for location(s) {}:", modelLocations, e);
					bakedModels.put(model, missingBaked);
				}
			}
		}

		ModelManager modelManager = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getModelManager();
		for (Entry<ModelResourceLocation, IModel> e : stateModels.entrySet()) {
			modelManager.modelRegistry.putObject(e.getKey(), bakedModels.get(e.getValue()));
		}
	}
	private static IBakedModel bakeModel(IModel model) {
		return model.bake(model.getDefaultState(), DefaultVertexFormats.ITEM, DefaultTextureGetter_INSTANCE);
	}
	private static void removeBakedCache(IModel model, ModelLoader modelLoader) {
		try {
			Class<?> vanillaModelWrapperClass = Class.forName("net.minecraftforge.client.model.ModelLoader$VanillaModelWrapper");
			if (((Object) model).getClass() != vanillaModelWrapperClass)
				return;
			Class<?> bakedModelCacheKeyClass = Class.forName("net.minecraftforge.client.model.ModelLoader$BakedModelCacheKey");
			Constructor<?>[] constructors = bakedModelCacheKeyClass.getConstructors();
			Object cacheKey = constructors[0].newInstance(modelLoader, model, model.getDefaultState(), DefaultVertexFormats.ITEM, DefaultTextureGetter_INSTANCE);
			LoadingCache<?, ?> modelCache = MyReflectionHelper.getPrivateField(vanillaLoaderInstance, "modelCache");
			modelCache.invalidate(cacheKey);
		} catch (ClassNotFoundException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}


	//texture

	public static void loadTexturesDynamic(Iterable<ResourceLocation> textureIds) {
		try {
			StitchManager.loadTexturesDynamic(textureIds);
			for (ResourceLocation textureId : textureIds) {
				reloadTextureDependantsModel(textureId);
			}
		} catch (StitchFullSpaceException e) {
			ModMain.LOGGER.info("Reload resources!");
			FMLClientHandler.instance().refreshResources(VanillaResourceType.MODELS);
		}
	}
	public static void unregisterTextures(Iterable<ResourceLocation> textureIds) {
		TextureMap textureMap = Minecraft.getMinecraft().getTextureMapBlocks();
		for (ResourceLocation textureId : textureIds) {
			TextureAtlasSprite textureAtlasSprite = textureMap.mapRegisteredSprites.remove(textureId.toString());
			textureMap.mapUploadedSprites.remove(textureId.toString());
			if (textureAtlasSprite != null)
				unregisteredTextureCache.put(textureId, textureAtlasSprite);
			reloadTextureDependantsModel(textureId);
		}
	}
	public static void addTextureDependencies(ResourceLocation modelId, Collection<ResourceLocation> textureIds) {
		if (ClientCache.INSTANCE.isItemModelAdded(modelId)) {
			for (ResourceLocation textureId : textureIds) {
				if (textureId.getNamespace().equals("minecraft"))
					continue;
				itemModelTextureDependencies.put(textureId, modelId);
			}
		} else if (ClientCache.INSTANCE.isBlockModelAdded(modelId)) {
			for (ResourceLocation textureId : textureIds) {
				if (textureId.getNamespace().equals("minecraft"))
					continue;
				blockModelTextureDependencies.put(textureId, modelId);
			}
		}
	}
	public static void reloadTextureDependantsModel(ResourceLocation textureId) {
		Collection<ResourceLocation> itemModelIds = itemModelTextureDependencies.get(textureId);
		if (!itemModelIds.isEmpty()) {
			loadItemModels(itemModelIds.stream().map(IdConverter::itemModelId2itemModelName).collect(Collectors.toList()));
			for (ResourceLocation modelId : itemModelIds) {
				reloadItemModelMesh(IdConverter.itemModelId2ItemId(modelId));
			}
		}
		Collection<ResourceLocation> blockModelIds = blockModelTextureDependencies.get(textureId);
		if (!blockModelIds.isEmpty()) {
			for (ResourceLocation blockModelId : blockModelIds) {
				reloadBlockModelDependants(blockModelId);
			}
		}
	}
	public static Collection<ResourceLocation> getDefaultTextureIds() {
		return Arrays.asList(new ResourceLocation(ModTagsGenerated.MODID, "items/removed"));
	}

	//lang
	@SuppressWarnings("deprecation")
	public static void putLang(String translateKey, String text) {
		net.minecraft.client.resources.I18n.i18nLocale.properties.put(translateKey, text);
		net.minecraft.util.text.translation.I18n.localizedName.languageList.put(translateKey, text);
	}

	//other
	public static void reloadAllChunks() {
		for (RenderChunk renderChunk : Minecraft.getMinecraft().renderGlobal.viewFrustum.renderChunks) {
			renderChunk.setNeedsUpdate(false);
		}
	}


	public static class SingleBlockModelLoader {
		private static final Map<ResourceLocation, IBakedModel> modelId2bakedModel = new HashMap<>();

		public static void loadBlockModels(Iterable<ResourceLocation> modelIds) {
			for (ResourceLocation modelId : modelIds) {
				loadBlockModel(modelId);
			}
		}
		public static void loadBlockModel(ResourceLocation modelId) {
			removeModelCache(modelId);
			IModel model = ModelLoaderRegistry.getModelOrMissing(modelId);
			modelId2bakedModel.put(modelId, bakeModel(model));
		}

		public static IBakedModel getModel(ResourceLocation modelId) {
			return modelId2bakedModel.get(modelId);
		}
		public static Collection<ResourceLocation> getModels() {
			return modelId2bakedModel.keySet();
		}
		public static void remove(ResourceLocation modelId) {
			modelId2bakedModel.remove(modelId);
		}
		public static void unloadAll() {
			modelId2bakedModel.clear();
		}
	}

	public static class TemporaryBlockModelLoader {
		private static final ResourceLocation temporaryRL = new ResourceLocation(ModTagsGenerated.MODID, "temporary");
		private static final ModelBlockAnimation defaultModelBlockAnimation;

		static {
			defaultModelBlockAnimation = MyReflectionHelper.getPrivateStaticField(ModelBlockAnimation.class, "defaultModelBlockAnimation");
		}

		public static IBakedModel loadModel(String json) {
			ModelLoader modelLoader = MyReflectionHelper.invokePrivateMethod(MyReflectionHelper.getPublicStaticField("net.minecraftforge.client.model.ModelLoader$VanillaLoader", "INSTANCE"), "getLoader");
			ModelBlock modelBlock = ModelBlock.deserialize(new StringReader(json));
			try {
				Class<?> vanillaModelWrapperClass = Class.forName("net.minecraftforge.client.model.ModelLoader$VanillaModelWrapper");
				Constructor<?> constructor = vanillaModelWrapperClass.getConstructors()[0];
				constructor.setAccessible(true);
				IModel model = (IModel) constructor.newInstance(modelLoader, temporaryRL, modelBlock, false, defaultModelBlockAnimation);
				model.getTextures();
				return bakeModel(model);
			} catch (ClassNotFoundException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
				throw new RuntimeException(e);
			}
		}

	}

	public static abstract class ReloadResourceListener implements IResourceManagerReloadListener {

		public static final ReloadResourceListener PRE = new ReloadResourceListener() {
			@Override
			public void onResourceManagerReload(IResourceManager resourceManager) {
				itemModelTextureDependencies.clear();
				blockModelTextureDependencies.clear();
				blockModelDependencies.clear();
			}
		};
		public static final ReloadResourceListener POST = new ReloadResourceListener() {
			@Override
			public void onResourceManagerReload(IResourceManager resourceManager) {
				SingleBlockModelLoader.loadBlockModels(ClientCache.INSTANCE.blockModelIds.keySet());
				for (ResourceLocation itemId : MCRegistryUtil.getRemovedItemIds()) {
					putLang(DynamicResourceManager.toTranslationKeyItem(itemId), REMOVED_ITEM_NAME);
				}
				for (ResourceLocation blockId : MCRegistryUtil.getRemovedBlockIds()) {
					putLang(DynamicResourceManager.toTranslationKeyBlock(blockId), REMOVED_BLOCK_NAME);
				}
			}
		};

	}
}
