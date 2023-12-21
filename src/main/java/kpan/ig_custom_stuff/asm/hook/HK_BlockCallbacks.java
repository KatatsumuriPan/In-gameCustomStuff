package kpan.ig_custom_stuff.asm.hook;

import kpan.ig_custom_stuff.block.DynamicBlockBase;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ObjectIntIdentityMap;

public class HK_BlockCallbacks {
	public static void onOnAdd(int id, Block block, ObjectIntIdentityMap<IBlockState> blockstateMap) {
		if (block instanceof DynamicBlockBase) {
			for (int meta = 0; meta < 16; meta++) {
				blockstateMap.put(block.getStateFromMeta(meta), id << 4 | meta);
			}
		}
	}
}
