package kpan.ig_custom_stuff.block.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemBlockBase extends ItemBlock {
	public ItemBlockBase(Block block) {
		super(block);
	}

	@Override
	public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, IBlockState newState) {
		if (!world.setBlockState(pos, newState, 11))
			return false;

		IBlockState state = world.getBlockState(pos);
		if (state.getBlock() == block) {
			setTileEntityNBT(world, player, pos, stack);
			block.onBlockPlacedBy(world, pos, state, player, stack);

			if (player instanceof EntityPlayerMP)
				CriteriaTriggers.PLACED_BLOCK.trigger((EntityPlayerMP) player, pos, stack);
		}

		return true;
	}
	@Override
	public String getTranslationKey() { return block.getTranslationKey(); }
	@Override
	public String getTranslationKey(ItemStack stack) {
		return getTranslationKey();
	}

}
