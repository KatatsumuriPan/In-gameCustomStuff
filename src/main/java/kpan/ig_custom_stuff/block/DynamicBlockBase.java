package kpan.ig_custom_stuff.block;

import kpan.ig_custom_stuff.block.BlockStateEntry.BlockStateType;
import kpan.ig_custom_stuff.resource.ids.BlockId;
import kpan.ig_custom_stuff.util.MyReflectionHelper;
import kpan.ig_custom_stuff.util.interfaces.block.IHasMultiModels;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

public class DynamicBlockBase extends Block {
	public static final AxisAlignedBB AABB_BOTTOM_HALF = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.5D, 1.0D);
	public static final AxisAlignedBB AABB_TOP_HALF = new AxisAlignedBB(0.0D, 0.5D, 0.0D, 1.0D, 1.0D, 1.0D);

	protected int fortuneBonus = 0;

	private boolean isFullOpaqueCube;
	private boolean isRemoved = false;
	private FaceCullingType faceCullingType;
	private BlockStateType blockStateType;

	public DynamicBlockBase(BlockId blockId, BlockStateType blockStateType, BlockPropertyEntry blockPropertyEntry) {
		super(Material.ROCK, Material.ROCK.getMaterialMapColor());
		this.blockStateType = blockStateType;
		setTranslationKey(blockId.namespace + "." + blockId.name);
		MyReflectionHelper.setPrivateField(Impl.class, this, "registryName", blockId.toResourceLocation());

		setProperty(blockPropertyEntry);
	}

	public void setProperty(BlockPropertyEntry blockPropertyEntry) {
		setHardness(blockPropertyEntry.hardness);
		setResistance(blockPropertyEntry.resistance);
		setSoundType(blockPropertyEntry.soundType);
		setCreativeTab(blockPropertyEntry.creativeTab);
		setMaterial(blockPropertyEntry.material);
		setBlockMapColor(blockPropertyEntry.material.getMaterialMapColor());
		isFullOpaqueCube = blockPropertyEntry.isFullOpaqueCube;
		faceCullingType = blockPropertyEntry.faceCullingType;
	}

	public void setBlockStateType(BlockStateType blockStateType) {
		this.blockStateType = blockStateType;
	}
	public void setRemoved(boolean removed) {
		isRemoved = removed;
	}

	@Override
	public Block setHardness(float hardness) {
		blockHardness = hardness;
		return this;
	}

	public void setMaterial(Material material) {
		this.material = material;
	}
	public void setBlockMapColor(MapColor blockMapColor) {
		this.blockMapColor = blockMapColor;
	}

	//ブロックステート

	public BlockStateType getBlockStateType() {
		return blockStateType;
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		switch (blockStateType) {
			case SIMPLE -> {
				return 0;
			}
			case FACE6 -> {
				return state.getValue(DynamicBlockStateContainer.FACING).getIndex();
			}
			case HORIZONTAL4 -> {
				return state.getValue(DynamicBlockStateContainer.HORIZONTAL).getHorizontalIndex();
			}
			case XYZ -> {
				return DynamicBlockStateContainer.getMetaFromXYZ(state.getValue(DynamicBlockStateContainer.XYZ_AXIS));
			}
			case SLAB -> {
				return DynamicBlockStateContainer.getMetaFromSlab(state.getValue(DynamicBlockStateContainer.SLAB));
			}
			default -> throw new AssertionError();
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public IBlockState getStateFromMeta(int meta) {
		switch (blockStateType) {
			case SIMPLE -> {
				return getDefaultState();
			}
			case FACE6 -> {
				return getDefaultState().withProperty(DynamicBlockStateContainer.FACING, EnumFacing.byIndex(meta));
			}
			case HORIZONTAL4 -> {
				return getDefaultState().withProperty(DynamicBlockStateContainer.HORIZONTAL, EnumFacing.byHorizontalIndex(meta));
			}
			case XYZ -> {
				return getDefaultState().withProperty(DynamicBlockStateContainer.XYZ_AXIS, DynamicBlockStateContainer.getAxisFromMeta(meta));
			}
			case SLAB -> {
				return getDefaultState().withProperty(DynamicBlockStateContainer.SLAB, DynamicBlockStateContainer.getSlabFromMeta(meta));
			}
			default -> throw new AssertionError();
		}
	}

	@Override
	protected final BlockStateContainer createBlockState() {
		return new DynamicBlockStateContainer(this);
	}


	@Override
	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		switch (blockStateType) {
			case SIMPLE -> {
				return getDefaultState();
			}
			case FACE6 -> {
				return getDefaultState().withProperty(DynamicBlockStateContainer.FACING, EnumFacing.getDirectionFromEntityLiving(pos, placer));
			}
			case HORIZONTAL4 -> {
				return getDefaultState().withProperty(DynamicBlockStateContainer.HORIZONTAL, placer.getHorizontalFacing().getOpposite());
			}
			case XYZ -> {
				return getDefaultState().withProperty(DynamicBlockStateContainer.XYZ_AXIS, facing.getAxis());
			}
			case SLAB -> {
				IBlockState iblockstate = getDefaultState().withProperty(DynamicBlockStateContainer.SLAB, EnumSlabType.BOTTOM);
				return facing != EnumFacing.DOWN && (facing == EnumFacing.UP || (double) hitY <= 0.5D) ? iblockstate : iblockstate.withProperty(DynamicBlockStateContainer.SLAB, EnumSlabType.TOP);
			}
			default -> throw new AssertionError();
		}
	}

	//アイテムドロップ等

	@Override
	public int damageDropped(IBlockState state) { return 0; }

	public Item getItem(IBlockState state) {
		return Item.getItemFromBlock(this);
	}
	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return getItem(state);
	}
	@SuppressWarnings("deprecation")
	@Override
	public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state) {
		return new ItemStack(getItem(state), 1, damageDropped(state));
	}

	@Override
	public int quantityDropped(Random random) {
		return isRemoved ? 0 : 1;
	}

	@Override
	public int quantityDroppedWithBonus(int fortune, Random random) {
		if (fortuneBonus > 0 && fortune > 0) {
			int i = random.nextInt(fortune + 2) - 1;
			if (i < 0)
				i = 0;

			return quantityDropped(random) + fortuneBonus * (i + 1);
		} else
			return quantityDropped(random);
	}

	@Override
	public int quantityDropped(IBlockState state, int fortune, Random random) {
		switch (blockStateType) {
			case SLAB -> {
				return state.getValue(DynamicBlockStateContainer.SLAB) == EnumSlabType.DOUBLE ? 2 : 1;
			}
			default -> {
				return super.quantityDropped(state, fortune, random);
			}
		}
	}
	@Override
	public void dropBlockAsItemWithChance(World worldIn, BlockPos pos, IBlockState state, float chance, int fortune) {
		if (!worldIn.isRemote && !worldIn.restoringBlockSnapshots) // do not drop items while restoring blockstates, prevents item dupe
		{
			NonNullList<ItemStack> drops = NonNullList.create();
			getDrops(drops, worldIn, pos, state, fortune);

			for (ItemStack drop : drops) {
				if (worldIn.rand.nextFloat() <= chance)
					spawnAsEntity(worldIn, pos, drop);
			}
		}
	}
	@Override
	public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, TileEntity te, ItemStack stack) {
		player.addStat(StatList.getBlockStats(this));//null許容
		player.addExhaustion(0.005F);

		if (canSilkHarvest(worldIn, pos, state, player) && EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, stack) > 0) {
			spawnAsEntity(worldIn, pos, getSilkTouchDrop(state));
		} else {
			harvesters.set(player);
			int i = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, stack);
			dropHarvestedItem(worldIn, pos, state, player, stack, i);
			harvesters.set(null);
		}
	}
	protected void dropHarvestedItem(World world, BlockPos pos, IBlockState state, EntityPlayer player, ItemStack stack, int fortune) {
		dropBlockAsItem(world, pos, state, fortune);
	}

	@Override
	protected ItemStack getSilkTouchDrop(IBlockState state) {
		if (isRemoved)
			return ItemStack.EMPTY;
		Item item = getItem(state);
		int i = 0;
		if (item.getHasSubtypes())
			i = damageDropped(state);
		return new ItemStack(item, 1, i);
	}

	@Override
	protected boolean canSilkHarvest() {
		if (blockStateType == BlockStateType.SLAB)
			return false;
		return true;
	}

	//タイルエンティティ

	//モデル系

	@SuppressWarnings("deprecation")
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		switch (blockStateType) {
			case SLAB -> {
				switch (state.getValue(DynamicBlockStateContainer.SLAB)) {
					case TOP -> {
						return AABB_TOP_HALF;
					}
					case BOTTOM -> {
						return AABB_BOTTOM_HALF;
					}
					case DOUBLE -> {
						return FULL_BLOCK_AABB;
					}
					default -> throw new AssertionError();
				}
			}
			default -> {
				return super.getBoundingBox(state, source, pos);
			}
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState) {
		if (!isRemoved)
			super.addCollisionBoxToList(state, worldIn, pos, entityBox, collidingBoxes, entityIn, isActualState);
	}

	@Override
	public boolean isTopSolid(IBlockState state) {
		switch (blockStateType) {
			case SLAB -> {
				return state.getValue(DynamicBlockStateContainer.SLAB) != EnumSlabType.BOTTOM;
			}
			default -> {
				return super.isTopSolid(state);
			}
		}
	}

	@Override
	public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
		switch (blockStateType) {
			case SLAB -> {
				switch (state.getValue(DynamicBlockStateContainer.SLAB)) {
					case TOP -> {
						if (face == EnumFacing.UP)
							return BlockFaceShape.SOLID;
					}
					case BOTTOM -> {
						if (face == EnumFacing.DOWN)
							return BlockFaceShape.SOLID;
					}
					case DOUBLE -> {
						return BlockFaceShape.SOLID;
					}
				}
				return BlockFaceShape.UNDEFINED;
			}
			default -> {
				return super.getBlockFaceShape(worldIn, state, pos, face);
			}
		}
	}

	//描画
	@Override
	public BlockRenderLayer getRenderLayer() {
		return isFullOpaqueCube ? BlockRenderLayer.SOLID : BlockRenderLayer.CUTOUT;
	}

	//レンダリングで使用
	@SuppressWarnings("deprecation")
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		if (blockStateType == null)//constructorで呼ばれる
			return true;
		switch (blockStateType) {
			case SLAB -> {
				if (state.getValue(DynamicBlockStateContainer.SLAB) != EnumSlabType.DOUBLE)
					return false;
			}
		}
		return isFullOpaqueCube;
	}
	//形状・一部レンダリングで使用
	//窒息判定やBlock.isNormalCube、BlockModelRenderer.fillQuadBoundsなど
	@SuppressWarnings("deprecation")
	@Override
	public boolean isFullCube(IBlockState state) {
		switch (blockStateType) {
			case SLAB -> {
				if (state.getValue(DynamicBlockStateContainer.SLAB) != EnumSlabType.DOUBLE)
					return false;
			}
		}
		return isFullOpaqueCube;
	}
	//一部形状と一部レンダリングで使用
	//爆発時の炎設置判定やモブ歩行AI、液体のレンダリングやBlock.isSideSolidなど
	@SuppressWarnings("deprecation")
	@Override
	public boolean isFullBlock(IBlockState state) {
		switch (blockStateType) {
			case SLAB -> {
				if (state.getValue(DynamicBlockStateContainer.SLAB) != EnumSlabType.DOUBLE)
					return false;
			}
		}
		return isFullOpaqueCube;
	}
	@SuppressWarnings("deprecation")
	@Override
	public boolean getUseNeighborBrightness(IBlockState state) {
		return !isOpaqueCube(state);
	}
	@Override
	public int getLightOpacity(IBlockState state, IBlockAccess world, BlockPos pos) {
		return isFullOpaqueCube ? 255 : 0;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
		switch (faceCullingType) {
			case NORMAL -> {
				return super.shouldSideBeRendered(blockState, blockAccess, pos, side);
			}
			case GLASS -> {
				IBlockState blockStateOther = blockAccess.getBlockState(pos.offset(side));
				Block blockOther = blockStateOther.getBlock();
				if (blockOther == this && blockStateType == BlockStateType.SLAB) {
					EnumSlabType slabType = blockState.getValue(DynamicBlockStateContainer.SLAB);
					EnumSlabType slabTypeOther = blockStateOther.getValue(DynamicBlockStateContainer.SLAB);
					if (slabType == EnumSlabType.TOP && side == EnumFacing.DOWN || slabType == EnumSlabType.BOTTOM && side == EnumFacing.UP)
						return true;
					if (slabTypeOther == EnumSlabType.TOP && side == EnumFacing.UP || slabTypeOther == EnumSlabType.BOTTOM && side == EnumFacing.DOWN)
						return true;
					if (slabType == EnumSlabType.DOUBLE && slabTypeOther != EnumSlabType.DOUBLE && side.getAxis() != Axis.Y)
						return true;
					return false;
				}
				if (blockState != blockStateOther) {
					return true;
				}
				if (blockOther == this) {
					return false;
				}
				return super.shouldSideBeRendered(blockState, blockAccess, pos, side);
			}
			default -> throw new AssertionError();
		}
	}

	@Override
	public boolean doesSideBlockRendering(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing face) {
		switch (blockStateType) {
			case SLAB -> {
				if (net.minecraftforge.common.ForgeModContainer.disableStairSlabCulling)
					return super.doesSideBlockRendering(state, world, pos, face);

				if (state.isOpaqueCube())
					return true;

				EnumSlabType slabType = state.getValue(DynamicBlockStateContainer.SLAB);
				return (slabType == EnumSlabType.TOP && face == EnumFacing.UP) || (slabType == EnumSlabType.BOTTOM && face == EnumFacing.DOWN) && isFullOpaqueCube;
			}
			default -> {
				return super.doesSideBlockRendering(state, world, pos, face);
			}
		}
	}

	@Override
	public boolean isSideSolid(IBlockState base_state, IBlockAccess world, BlockPos pos, EnumFacing side) {
		switch (blockStateType) {
			case SLAB -> {
				return (base_state.getValue(DynamicBlockStateContainer.SLAB) == EnumSlabType.TOP && side == EnumFacing.UP)
						|| (base_state.getValue(DynamicBlockStateContainer.SLAB) == EnumSlabType.BOTTOM && side == EnumFacing.DOWN);
			}
		}
		return super.isSideSolid(base_state, world, pos, side);
		/*
		if (base_state.isTopSolid() && side == EnumFacing.UP) // Short circuit to vanilla function if its true
			return true;

		if (this instanceof BlockSlab)
		{
			IBlockState state = this.getActualState(base_state, world, pos);
			return base_state.isFullBlock()
					|| (state.getValue(BlockSlab.HALF) == BlockSlab.EnumBlockHalf.TOP    && side == EnumFacing.UP  )
					|| (state.getValue(BlockSlab.HALF) == BlockSlab.EnumBlockHalf.BOTTOM && side == EnumFacing.DOWN);
		}
		else if (this instanceof BlockFarmland)
		{
			return (side != EnumFacing.DOWN && side != EnumFacing.UP);
		}
		else if (this instanceof BlockStairs)
		{
			IBlockState state = this.getActualState(base_state, world, pos);
			boolean flipped = state.getValue(BlockStairs.HALF) == BlockStairs.EnumHalf.TOP;
			BlockStairs.EnumShape shape = (BlockStairs.EnumShape)state.getValue(BlockStairs.SHAPE);
			EnumFacing facing = (EnumFacing)state.getValue(BlockStairs.FACING);
			if (side == EnumFacing.UP) return flipped;
			if (side == EnumFacing.DOWN) return !flipped;
			if (facing == side) return true;
			if (flipped)
			{
				if (shape == BlockStairs.EnumShape.INNER_LEFT ) return side == facing.rotateYCCW();
				if (shape == BlockStairs.EnumShape.INNER_RIGHT) return side == facing.rotateY();
			}
			else
			{
				if (shape == BlockStairs.EnumShape.INNER_LEFT ) return side == facing.rotateY();
				if (shape == BlockStairs.EnumShape.INNER_RIGHT) return side == facing.rotateYCCW();
			}
			return false;
		}
		else if (this instanceof BlockSnow)
		{
			IBlockState state = this.getActualState(base_state, world, pos);
			return ((Integer)state.getValue(BlockSnow.LAYERS)) >= 8;
		}
		else if (this instanceof BlockHopper && side == EnumFacing.UP)
		{
			return true;
		}
		else if (this instanceof BlockCompressedPowered)
		{
			return true;
		}
		return isNormalCube(base_state, world, pos);
		 */
	}


	//その他

	@Override
	public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
		if (this instanceof IHasMultiModels)
			for (int i = 0; i <= ((IHasMultiModels) this).metaMax(); i++) {
				items.add(new ItemStack(this, 1, i));
			}
		else
			super.getSubBlocks(itemIn, items);
	}

	//アクセッサー
	@SuppressWarnings("DataFlowIssue")
	public String getItemRegistryName() {
		return getRegistryName().getPath();
	}

	@SuppressWarnings("unused")
	public Material getRegisteredMaterial() {
		return material;
	}

	@SuppressWarnings("unused")
	public float getRegisteredHardness() {
		return blockHardness;
	}

}
