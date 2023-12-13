package kpan.ig_custom_stuff.resource;

import kpan.ig_custom_stuff.block.BlockModelEntry;
import net.minecraft.util.ResourceLocation;

import java.util.Map;
import java.util.TreeMap;

public class BlockModels {
	//blockModelId->BlockModelEntry
	public final Map<ResourceLocation, BlockModelEntry> normalModels = new TreeMap<>();
	public final Map<ResourceLocation, BlockModelEntry> slabModels = new TreeMap<>();
}
