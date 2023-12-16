package kpan.ig_custom_stuff.asm.hook;

import kpan.ig_custom_stuff.ModMain;
import kpan.ig_custom_stuff.block.BlockEntry;
import kpan.ig_custom_stuff.item.ItemEntry;
import kpan.ig_custom_stuff.registry.MCRegistryUtil;
import kpan.ig_custom_stuff.resource.ids.BlockId;
import kpan.ig_custom_stuff.resource.ids.ItemId;
import kpan.ig_custom_stuff.util.MyReflectionHelper;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.GameData;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.RegistryManager;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Map;

public class HK_GameData {


	public static List<Pair<ItemEntry, Integer>> itemEntries = null;
	public static List<Pair<BlockEntry, Integer>> blockEntries = null;

	public static void onLoadFrozenDataToStagingRegistry(ResourceLocation name, ForgeRegistry<?> newRegistry, Map<ResourceLocation, Integer> _new) {
		if (ModMain.server != null) {
			//localWorldの読み込みである
			if (name.equals(GameData.ITEMS)) {
				ForgeRegistry<Item> ITEM_REGISTRY = RegistryManager.ACTIVE.getRegistry(GameData.ITEMS);
				for (ItemId itemId : MCRegistryUtil.getItemIds()) {
					if (!newRegistry.containsKey(itemId.toResourceLocation())) {
						MyReflectionHelper.invokePrivateMethod(newRegistry, "add", new Class[]{int.class, IForgeRegistryEntry.class}, new Object[]{ITEM_REGISTRY.getID(itemId.toResourceLocation()), ITEM_REGISTRY.getValue(itemId.toResourceLocation())});
					}
				}
				for (BlockId blockId : MCRegistryUtil.getBlockIds()) {
					if (!newRegistry.containsKey(blockId.toResourceLocation())) {
						MyReflectionHelper.invokePrivateMethod(newRegistry, "add", new Class[]{int.class, IForgeRegistryEntry.class}, new Object[]{ITEM_REGISTRY.getID(blockId.toResourceLocation()), ITEM_REGISTRY.getValue(blockId.toResourceLocation())});
					}
				}
			}
			if (name.equals(GameData.BLOCKS)) {
				ForgeRegistry<Block> BLOCK_REGISTRY = RegistryManager.ACTIVE.getRegistry(GameData.BLOCKS);
				for (BlockId blockId : MCRegistryUtil.getBlockIds()) {
					if (!newRegistry.containsKey(blockId.toResourceLocation())) {
						MyReflectionHelper.invokePrivateMethod(newRegistry, "add", new Class[]{int.class, IForgeRegistryEntry.class}, new Object[]{BLOCK_REGISTRY.getID(blockId.toResourceLocation()), BLOCK_REGISTRY.getValue(blockId.toResourceLocation())});
					}
				}
			}
		}
	}

	public static void onLoadPersistentDataToStagingRegistry(ResourceLocation name, ForgeRegistry<?> _new) {
		//何もしない
	}
}
