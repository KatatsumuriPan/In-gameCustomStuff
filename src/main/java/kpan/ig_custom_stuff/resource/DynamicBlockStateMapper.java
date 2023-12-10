package kpan.ig_custom_stuff.resource;

import com.google.common.collect.Maps;
import kpan.ig_custom_stuff.util.PropertyUtil;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.IStateMapper;

import java.util.Map;

public class DynamicBlockStateMapper implements IStateMapper {
	protected Map<IBlockState, ModelResourceLocation> mapStateModelLocations = Maps.<IBlockState, ModelResourceLocation>newLinkedHashMap();

	@Override
	public Map<IBlockState, ModelResourceLocation> putStateModelLocations(Block blockIn) {
		mapStateModelLocations.clear();
		for (IBlockState iblockstate : blockIn.getBlockState().getValidStates()) {
			mapStateModelLocations.put(iblockstate, getModelResourceLocation(iblockstate));
		}
		return mapStateModelLocations;
	}

	protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
		return new ModelResourceLocation(Block.REGISTRY.getNameForObject(state.getBlock()), PropertyUtil.getPropertyString(state));
	}
}
