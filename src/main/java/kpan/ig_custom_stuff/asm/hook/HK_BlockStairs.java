package kpan.ig_custom_stuff.asm.hook;

import kpan.ig_custom_stuff.block.BlockStateEntry.BlockStateType;
import kpan.ig_custom_stuff.block.DynamicBlockBase;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.state.IBlockState;

public class HK_BlockStairs {

	public static boolean isBlockStairs(IBlockState state) {
		if (state.getBlock() instanceof BlockStairs)
			return true;
		if (state.getBlock() instanceof DynamicBlockBase dynamic && dynamic.getBlockStateType() == BlockStateType.STAIR)
			return true;
		return false;
	}

}
