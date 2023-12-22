package kpan.ig_custom_stuff.block.item;

import kpan.ig_custom_stuff.block.DynamicBlockBase;
import kpan.ig_custom_stuff.block.DynamicBlockStateContainer;
import kpan.ig_custom_stuff.block.EnumSlabType;
import kpan.ig_custom_stuff.util.MyReflectionHelper;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemDynamicBlockBase extends ItemBlock {
	public ItemDynamicBlockBase(DynamicBlockBase block) {
		super(block);
		MyReflectionHelper.setPrivateField(Impl.class, this, "registryName", block.getRegistryName());
	}

	@Override
	public DynamicBlockBase getBlock() {
		return (DynamicBlockBase) super.getBlock();
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		switch (getBlock().getBlockStateType()) {
			case SIMPLE, FACE6, HORIZONTAL4, XYZ, STAIR -> {
				return super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
			}
			case SLAB -> {
				ItemStack itemstack = player.getHeldItem(hand);
				if (!itemstack.isEmpty() && player.canPlayerEdit(pos.offset(facing), facing, itemstack)) {
					IBlockState iblockstate = worldIn.getBlockState(pos);

					if (iblockstate.getBlock() == getBlock() && iblockstate.getValue(DynamicBlockStateContainer.SLAB) != EnumSlabType.DOUBLE) {
						EnumSlabType slabType = iblockstate.getValue(DynamicBlockStateContainer.SLAB);

						if (facing == EnumFacing.UP && slabType == EnumSlabType.BOTTOM || facing == EnumFacing.DOWN && slabType == EnumSlabType.TOP) {
							IBlockState newState = iblockstate.withProperty(DynamicBlockStateContainer.SLAB, EnumSlabType.DOUBLE);
							AxisAlignedBB axisalignedbb = newState.getCollisionBoundingBox(worldIn, pos);

							if (axisalignedbb != Block.NULL_AABB && worldIn.checkNoEntityCollision(axisalignedbb.offset(pos)) && worldIn.setBlockState(pos, newState, 11)) {
								SoundType soundtype = getBlock().getSoundType(newState, worldIn, pos, player);
								worldIn.playSound(player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
								itemstack.shrink(1);

								if (player instanceof EntityPlayerMP) {
									CriteriaTriggers.PLACED_BLOCK.trigger((EntityPlayerMP) player, pos, itemstack);
								}
							}

							return EnumActionResult.SUCCESS;
						}
					}

					return tryPlace(player, itemstack, worldIn, pos.offset(facing)) ? EnumActionResult.SUCCESS : super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
				} else {
					return EnumActionResult.FAIL;
				}
			}
			default -> throw new AssertionError();
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean canPlaceBlockOnSide(World worldIn, BlockPos pos, EnumFacing side, EntityPlayer player, ItemStack stack) {
		BlockPos blockpos = pos;
		IBlockState iblockstate = worldIn.getBlockState(pos);

		if (iblockstate.getBlock() == getBlock() && iblockstate.getValue(DynamicBlockStateContainer.SLAB) != EnumSlabType.DOUBLE) {
			boolean isTop = iblockstate.getValue(DynamicBlockStateContainer.SLAB) == EnumSlabType.TOP;

			if (side == EnumFacing.UP && !isTop || side == EnumFacing.DOWN && isTop) {
				return true;
			}
		}

		pos = pos.offset(side);
		IBlockState iblockstate1 = worldIn.getBlockState(pos);
		if (iblockstate1.getBlock() == getBlock() && iblockstate1.getValue(DynamicBlockStateContainer.SLAB) != EnumSlabType.DOUBLE)
			return true;
		return super.canPlaceBlockOnSide(worldIn, blockpos, side, player, stack);
	}

	private boolean tryPlace(EntityPlayer player, ItemStack stack, World worldIn, BlockPos pos) {
		IBlockState iblockstate = worldIn.getBlockState(pos);

		if (iblockstate.getBlock() == getBlock() && iblockstate.getValue(DynamicBlockStateContainer.SLAB) != EnumSlabType.DOUBLE) {

			IBlockState newState = iblockstate.withProperty(DynamicBlockStateContainer.SLAB, EnumSlabType.DOUBLE);
			AxisAlignedBB axisalignedbb = newState.getCollisionBoundingBox(worldIn, pos);

			if (axisalignedbb != Block.NULL_AABB && worldIn.checkNoEntityCollision(axisalignedbb.offset(pos)) && worldIn.setBlockState(pos, newState, 11)) {
				SoundType soundtype = getBlock().getSoundType(newState, worldIn, pos, player);
				worldIn.playSound(player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
				stack.shrink(1);
			}

			return true;
		}

		return false;
	}


}
