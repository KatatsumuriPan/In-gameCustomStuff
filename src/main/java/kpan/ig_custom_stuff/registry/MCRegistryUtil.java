package kpan.ig_custom_stuff.registry;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import kpan.ig_custom_stuff.ModMain;
import kpan.ig_custom_stuff.ModTagsGenerated;
import kpan.ig_custom_stuff.block.BlockEntry;
import kpan.ig_custom_stuff.block.BlockLangEntry;
import kpan.ig_custom_stuff.block.BlockPropertyEntry;
import kpan.ig_custom_stuff.block.BlockStateEntry.BlockStateType;
import kpan.ig_custom_stuff.block.DynamicBlockBase;
import kpan.ig_custom_stuff.block.item.ItemDynamicBlockBase;
import kpan.ig_custom_stuff.item.DynamicItemBase;
import kpan.ig_custom_stuff.item.ItemEntry;
import kpan.ig_custom_stuff.item.ItemPropertyEntry;
import kpan.ig_custom_stuff.resource.DynamicResourceLoader;
import kpan.ig_custom_stuff.resource.DynamicResourceManager;
import kpan.ig_custom_stuff.resource.RemovedResourcesResourcePack;
import kpan.ig_custom_stuff.util.MyReflectionHelper;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.GameData;
import net.minecraftforge.registries.IRegistryDelegate;
import net.minecraftforge.registries.RegistryManager;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class MCRegistryUtil {

	public static final ForgeRegistry<Item> ITEM_REGISTRY = RegistryManager.ACTIVE.getRegistry(GameData.ITEMS);
	public static final ForgeRegistry<Block> BLOCK_REGISTRY = RegistryManager.ACTIVE.getRegistry(GameData.BLOCKS);
	private static final BiMap<ResourceLocation, ItemEntry> ITEMS = HashBiMap.create();
	private static final BiMap<ResourceLocation, BlockEntry> BLOCKS = HashBiMap.create();
	private static final Set<ResourceLocation> removedItems = new HashSet<>();
	private static final Set<ResourceLocation> removedBlocks = new HashSet<>();

	public static void register(ItemEntry itemEntry, boolean isRemote) {
		//レジストリ登録が必ず先になる必要あり
		//シングルでは、サーバーとクライアントのスレッドの実行順は不定なので全てクライアント側で処理する
		if (!isRemote && ModMain.proxy.hasClientSide())
			return;
		if (ITEMS.containsKey(itemEntry.itemId))
			return;
		DynamicItemBase item;
		if (removedItems.contains(itemEntry.itemId)) {
			item = (DynamicItemBase) ITEM_REGISTRY.getValue(itemEntry.itemId);
			if (item == null)
				throw new IllegalStateException(itemEntry.itemId + " is not registered!?");
			removedItems.remove(itemEntry.itemId);
		} else {
			item = new DynamicItemBase(itemEntry.itemId, itemEntry.propertyEntry);
			ITEM_REGISTRY.unfreeze();
			ITEM_REGISTRY.register(item);
			ITEM_REGISTRY.freeze();
		}
		if (isRemote) {
			registerItemModel(itemEntry.itemId, item);
		}
		ITEMS.put(itemEntry.itemId, itemEntry);
	}
	private static void registerItemModel(ResourceLocation itemId, Item item) {
		//register
		ModelResourceLocation modelResourceLocation = new ModelResourceLocation(itemId, "inventory");
		ModelLoader.setCustomModelResourceLocation(item, 0, modelResourceLocation);

		//以降手動登録後の処理の再現
		{
			ModelLoader modelLoader = MyReflectionHelper.invokePrivateMethod(MyReflectionHelper.getPublicStaticField("net.minecraftforge.client.model.ModelLoader$VanillaLoader", "INSTANCE"), "getLoader");
			Map<IRegistryDelegate<Item>, Set<String>> customVariantNames = MyReflectionHelper.getPrivateStaticField(ModelBakery.class, "customVariantNames");
			List<String> modelNames = new ArrayList<>(customVariantNames.get(item.delegate));
			modelLoader.variantNames.put(item, modelNames);
			DynamicResourceLoader.loadItemModels(modelNames.stream().map(ResourceLocation::new).collect(Collectors.toList()));
		}

		//RenderItem
		//register
		ItemModelMesher itemModelMesher = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();
		itemModelMesher.register(item, 0, modelResourceLocation);
	}

	public static void update(ItemEntry itemEntry, boolean isRemote) {
		//シングルでは、サーバーとクライアントのスレッドの実行順は不定なので全てクライアント側で処理する
		if (!isRemote && ModMain.proxy.hasClientSide())
			return;
		if (!ITEMS.containsKey(itemEntry.itemId))
			throw new IllegalStateException(itemEntry.itemId + " is not registered!");
		ITEMS.put(itemEntry.itemId, itemEntry);
		DynamicItemBase item = (DynamicItemBase) ITEM_REGISTRY.getValue(itemEntry.itemId);
		item.setProperty(itemEntry.propertyEntry);
	}

	public static boolean isItemRegistered(ResourceLocation itemId) {
		return ITEMS.containsKey(itemId);
	}

	@Nullable
	public static ItemEntry getItem(ResourceLocation itemId) {
		return ITEMS.get(itemId);
	}

	public static void removeItem(ResourceLocation itemId, boolean isRemote) {
		//シングルでは、サーバーとクライアントのスレッドの実行順は不定なので全てクライアント側で処理する
		if (!isRemote && ModMain.proxy.hasClientSide())
			return;
		if (ITEMS.remove(itemId) == null)
			return;
		removedItems.add(itemId);
		DynamicResourceLoader.loadItemModels(Collections.singletonList(itemId));
	}

	public static void syncClientItemRegistryIntegrated(Collection<ItemEntry> serverRegistry) {
		clearItems();
		for (ItemEntry itemEntry : serverRegistry) {
			register(itemEntry, true);
		}
	}
	public static void syncClientItemRegistryDedicated(Collection<ItemEntry> serverRegistry, Collection<ResourceLocation> removedItemIds) {
		clearItems();
		for (ItemEntry itemEntry : serverRegistry) {
			register(itemEntry, true);
		}
		for (ResourceLocation removedItemId : removedItemIds) {
			Item item = new DynamicItemBase(removedItemId, ItemPropertyEntry.defaultOption());
			ITEM_REGISTRY.unfreeze();
			ITEM_REGISTRY.register(item);
			ITEM_REGISTRY.freeze();
			RemovedResourcesResourcePack.INSTANCE.addRemovedItem(removedItemId);
		}
		removedItems.addAll(removedItemIds);
		DynamicResourceLoader.loadItemModels(removedItemIds);
		for (ResourceLocation removedItemId : removedItemIds) {
			try {
				BlockLangEntry.removedItem().register(removedItemId, true);
			} catch (IOException e) {
				Client.sendMessage(new TextComponentString(e.getMessage()));
			}
		}
	}

	public static void reloadItemModelMeshes() {
		//RenderItem
		//register
		ItemModelMesher itemModelMesher = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();
		for (ResourceLocation itemId : ITEMS.keySet()) {
			Item item = ITEM_REGISTRY.getValue(itemId);
			ModelResourceLocation modelResourceLocation = new ModelResourceLocation(itemId, "inventory");
			itemModelMesher.register(item, 0, modelResourceLocation);
		}
		for (ResourceLocation blockId : BLOCKS.keySet()) {
			Item item = ITEM_REGISTRY.getValue(blockId);
			ModelResourceLocation modelResourceLocation = new ModelResourceLocation(blockId, "inventory");
			itemModelMesher.register(item, 0, modelResourceLocation);
		}
	}

	public static Collection<ResourceLocation> getItemIds() {
		return ITEMS.keySet();
	}
	public static Collection<ResourceLocation> getRemovedItemIds() {
		return removedItems;
	}

	@Nullable
	public static String getItemIdErrorMessage(ResourceLocation itemId) {
		if (itemId.toString().length() > DynamicResourceManager.ID_MAX_LENGTH)
			return I18n.format("gui.ingame_custom_stuff.error.id.too_long", DynamicResourceManager.ID_MAX_LENGTH);
		String namespace = itemId.getNamespace();
		if (namespace.isEmpty())
			return "gui.ingame_custom_stuff.error.namespace.empty";
		if (itemId.getPath().isEmpty())
			return "gui.ingame_custom_stuff.error.path.empty";
		if (namespace.equals("minecraft") || namespace.equals("forge") || namespace.equals(ModTagsGenerated.MODID))
			return I18n.format("gui.ingame_custom_stuff.error.namespace.not_allowed", namespace);
		if (!itemId.getPath().matches("[a-z0-9_]+"))
			return "gui.ingame_custom_stuff.error.allowed_id_characters";
		if (MCRegistryUtil.isItemRegistered(itemId))
			return "gui.ingame_custom_stuff.error.id_already_exists";
		return null;
	}

	@Nullable
	public static String getItemIdErrorMessage(String itemId) {
		if (itemId.length() > DynamicResourceManager.ID_MAX_LENGTH)
			return I18n.format("gui.ingame_custom_stuff.error.id.too_long", DynamicResourceManager.ID_MAX_LENGTH);
		if (!itemId.contains(":"))
			return "gui.ingame_custom_stuff.error.namespace.empty";
		String namespace = itemId.split(":", 2)[0];
		String path = itemId.split(":", 2)[1];
		if (namespace.isEmpty())
			return "gui.ingame_custom_stuff.error.namespace.empty";
		if (path.isEmpty())
			return "gui.ingame_custom_stuff.error.path.empty";
		if (namespace.equals("minecraft") || namespace.equals("forge") || namespace.equals(ModTagsGenerated.MODID))
			return I18n.format("gui.ingame_custom_stuff.error.namespace.not_allowed", namespace);
		if (!namespace.matches("[a-z0-9_]+") || !path.matches("[a-z0-9_]+"))
			return "gui.ingame_custom_stuff.error.allowed_id_characters";
		if (MCRegistryUtil.isItemRegistered(new ResourceLocation(itemId)))
			return "gui.ingame_custom_stuff.error.id_already_exists";
		return null;
	}

	private static void clearItems() {
		RemovedResourcesResourcePack.INSTANCE.clearAll();//直後にclearBlocksも呼ばれるはず
		BiMap<Integer, Item> ids = MyReflectionHelper.getPrivateField(ITEM_REGISTRY, "ids");
		BiMap<ResourceLocation, Item> names = MyReflectionHelper.getPrivateField(ITEM_REGISTRY, "names");
		for (ResourceLocation itemId : ITEMS.keySet()) {
			if (!names.containsKey(itemId))
				continue;//integrated server
			Item value = names.remove(itemId);
			if (value == null)
				throw new IllegalStateException("Removed a entry that did not have an associated id: " + itemId + " This should never happen unless hackery!");
			Integer id = ids.inverse().remove(value);
			if (id == null)
				throw new IllegalStateException("Removed a entry that did not have an associated id: " + itemId + " " + value.toString() + " This should never happen unless hackery!");
		}
		ITEMS.clear();
		removedItems.clear();
	}


	//block

	public static void register(BlockEntry blockEntry, boolean isRemote) {
		//レジストリ登録が必ず先になる必要あり
		//シングルでは、サーバーとクライアントのスレッドの実行順は不定なので全てクライアント側で処理する
		if (!isRemote && ModMain.proxy.hasClientSide())
			return;
		ResourceLocation blockId = blockEntry.blockId;
		if (BLOCKS.containsKey(blockId))
			return;
		DynamicBlockBase block;
		if (removedBlocks.contains(blockId)) {
			block = (DynamicBlockBase) BLOCK_REGISTRY.getValue(blockId);
			if (block == null)
				throw new IllegalStateException(blockId + " is not registered!?");
			block.setProperty(blockEntry.basicProperty);
			block.setBlockStateType(blockEntry.blockStateType);
			block.setRemoved(false);
			removedBlocks.remove(blockId);
		} else {
			block = new DynamicBlockBase(blockId, blockEntry.blockStateType, blockEntry.basicProperty);
			BLOCK_REGISTRY.unfreeze();
			BLOCK_REGISTRY.register(block);
			BLOCK_REGISTRY.freeze();
		}
		if (isRemote) {
			DynamicResourceLoader.registerBlockStateMapper(block);
			DynamicResourceLoader.loadBlockModel(block);
		}
		BLOCKS.put(blockId, blockEntry);

		//item
		ItemDynamicBlockBase item;
		if (removedItems.contains(blockId)) {
			item = (ItemDynamicBlockBase) ITEM_REGISTRY.getValue(blockId);
			if (item == null)
				throw new IllegalStateException(blockId + " is not registered!?");
			removedItems.remove(blockId);
		} else {
			item = new ItemDynamicBlockBase(block);
			ITEM_REGISTRY.unfreeze();
			ITEM_REGISTRY.register(item);
			ITEM_REGISTRY.freeze();
		}
		if (isRemote) {
			registerItemModel(blockId, item);
		}
	}

	public static void update(BlockEntry blockEntry, boolean isRemote) {
		//シングルでは、サーバーとクライアントのスレッドの実行順は不定なので全てクライアント側で処理する
		if (!isRemote && ModMain.proxy.hasClientSide())
			return;
		ResourceLocation blockId = blockEntry.blockId;
		if (!BLOCKS.containsKey(blockId))
			throw new IllegalStateException(blockId + " is not registered!");
		BLOCKS.put(blockId, blockEntry);
		DynamicBlockBase block = (DynamicBlockBase) BLOCK_REGISTRY.getValue(blockId);
		block.setProperty(blockEntry.basicProperty);
		block.setBlockStateType(blockEntry.blockStateType);
	}

	public static boolean isBlockRegistered(ResourceLocation blockId) {
		return BLOCKS.containsKey(blockId);
	}

	@Nullable
	public static BlockEntry getBlock(ResourceLocation blockId) {
		return BLOCKS.get(blockId);
	}

	public static void removeBlock(ResourceLocation blockId, boolean isRemote) {
		//シングルでは、サーバーとクライアントのスレッドの実行順は不定なので全てクライアント側で処理する
		if (!isRemote && ModMain.proxy.hasClientSide())
			return;
		if (BLOCKS.remove(blockId) == null)
			return;
		removedBlocks.add(blockId);
		((DynamicBlockBase) BLOCK_REGISTRY.getValue(blockId)).setProperty(BlockPropertyEntry.forRemovedBlock());
		((DynamicBlockBase) BLOCK_REGISTRY.getValue(blockId)).setBlockStateType(BlockStateType.SIMPLE);
		((DynamicBlockBase) BLOCK_REGISTRY.getValue(blockId)).setRemoved(true);
	}

	public static boolean isRemovedBlock(ResourceLocation blockId) {
		return removedBlocks.contains(blockId);
	}

	public static void syncClientBlockRegistryIntegrated(Collection<BlockEntry> serverRegistry) {
		clearBlocks();
		for (BlockEntry entry : serverRegistry) {
			register(entry, true);
		}
	}
	public static void syncClientBlockRegistryDedicated(Collection<BlockEntry> serverRegistry, Collection<ResourceLocation> removedBlockIds) {
		clearBlocks();
		for (BlockEntry entry : serverRegistry) {
			register(entry, true);
		}
		for (ResourceLocation removedBlockId : removedBlockIds) {
			DynamicBlockBase block = new DynamicBlockBase(removedBlockId, BlockStateType.SIMPLE, BlockPropertyEntry.forRemovedBlock());
			block.setRemoved(true);
			BLOCK_REGISTRY.unfreeze();
			BLOCK_REGISTRY.register(block);
			BLOCK_REGISTRY.freeze();
			Item item = new ItemDynamicBlockBase(block);
			ITEM_REGISTRY.unfreeze();
			ITEM_REGISTRY.register(item);
			ITEM_REGISTRY.freeze();
			RemovedResourcesResourcePack.INSTANCE.addRemovedBlock(removedBlockId);
			DynamicResourceLoader.loadBlockResources(removedBlockId);
		}
		removedBlocks.addAll(removedBlockIds);
		for (ResourceLocation removedBlockId : removedBlockIds) {
			try {
				BlockLangEntry.removedBlock().register(removedBlockId, true);
			} catch (IOException e) {
				Client.sendMessage(new TextComponentString(e.getMessage()));
			}
		}
	}

	public static Collection<ResourceLocation> getBlockIds() {
		return BLOCKS.keySet();
	}
	public static Collection<ResourceLocation> getRemovedBlockIds() {
		return removedBlocks;
	}

	@Nullable
	public static String getBlockIdErrorMessage(ResourceLocation blockId) {
		if (blockId.toString().length() > DynamicResourceManager.ID_MAX_LENGTH)
			return I18n.format("gui.ingame_custom_stuff.error.id.too_long", DynamicResourceManager.ID_MAX_LENGTH);
		String namespace = blockId.getNamespace();
		if (namespace.isEmpty())
			return "gui.ingame_custom_stuff.error.namespace.empty";
		if (blockId.getPath().isEmpty())
			return "gui.ingame_custom_stuff.error.path.empty";
		if (namespace.equals("minecraft") || namespace.equals("forge") || namespace.equals(ModTagsGenerated.MODID))
			return I18n.format("gui.ingame_custom_stuff.error.namespace.not_allowed", namespace);
		if (!blockId.getPath().matches("[a-z0-9_]+"))
			return "gui.ingame_custom_stuff.error.allowed_id_characters";
		if (MCRegistryUtil.isBlockRegistered(blockId))
			return "gui.ingame_custom_stuff.error.id_already_exists";
		if (MCRegistryUtil.isItemRegistered(blockId))
			return "gui.ingame_custom_stuff.error.same_item_id_already_exists";
		return null;
	}
	@Nullable
	public static String getBlockIdErrorMessage(String blockId) {
		if (blockId.length() > DynamicResourceManager.ID_MAX_LENGTH)
			return I18n.format("gui.ingame_custom_stuff.error.id.too_long", DynamicResourceManager.ID_MAX_LENGTH);
		if (!blockId.contains(":"))
			return "gui.ingame_custom_stuff.error.namespace.empty";
		String namespace = blockId.split(":", 2)[0];
		String path = blockId.split(":", 2)[1];
		if (namespace.isEmpty())
			return "gui.ingame_custom_stuff.error.namespace.empty";
		if (path.isEmpty())
			return "gui.ingame_custom_stuff.error.path.empty";
		if (namespace.equals("minecraft") || namespace.equals("forge") || namespace.equals(ModTagsGenerated.MODID))
			return I18n.format("gui.ingame_custom_stuff.error.namespace.not_allowed", namespace);
		if (!namespace.matches("[a-z0-9_]+") || !path.matches("[a-z0-9_]+"))
			return "gui.ingame_custom_stuff.error.allowed_id_characters";
		if (MCRegistryUtil.isBlockRegistered(new ResourceLocation(blockId)))
			return "gui.ingame_custom_stuff.error.id_already_exists";
		if (MCRegistryUtil.isItemRegistered(new ResourceLocation(blockId)))
			return "gui.ingame_custom_stuff.error.same_item_id_already_exists";
		return null;
	}

	private static void clearBlocks() {
		//RemovedResourcesResourcePack.INSTANCE.clearAll();//直前にclearItemsが呼ばれてるはず
		BiMap<Integer, Block> ids = MyReflectionHelper.getPrivateField(BLOCK_REGISTRY, "ids");
		BiMap<ResourceLocation, Block> names = MyReflectionHelper.getPrivateField(BLOCK_REGISTRY, "names");
		for (ResourceLocation blockId : BLOCKS.keySet()) {
			if (!names.containsKey(blockId))
				continue;//integrated server
			Block value = names.remove(blockId);
			if (value == null)
				throw new IllegalStateException("Removed a entry that did not have an associated id: " + blockId + " This should never happen unless hackery!");
			Integer id = ids.inverse().remove(value);
			if (id == null)
				throw new IllegalStateException("Removed a entry that did not have an associated id: " + blockId + " " + value + " This should never happen unless hackery!");
		}
		BLOCKS.clear();
		removedBlocks.clear();
	}


	private class Client {
		public static void sendMessage(ITextComponent textComponent) {
			Minecraft.getMinecraft().player.sendMessage(textComponent);
		}
	}
}
