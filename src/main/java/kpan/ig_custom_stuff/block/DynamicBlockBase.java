package kpan.ig_custom_stuff.block;

import com.google.common.collect.Lists;
import kpan.ig_custom_stuff.block.BlockStateEntry.BlockStateType;
import kpan.ig_custom_stuff.resource.ids.BlockId;
import kpan.ig_custom_stuff.util.MyReflectionHelper;
import kpan.ig_custom_stuff.util.interfaces.block.IHasMultiModels;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.BlockStairs.EnumHalf;
import net.minecraft.block.BlockStairs.EnumShape;
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
import net.minecraft.util.Mirror;
import net.minecraft.util.NonNullList;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

public class DynamicBlockBase extends Block {
	public static final AxisAlignedBB AABB_HALF_TOP = new AxisAlignedBB(0.0D, 0.5D, 0.0D, 1.0D, 1.0D, 1.0D);
	public static final AxisAlignedBB AABB_HALF_BOTTOM = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.5D, 1.0D);
	/**
	 * B: .. T: x.
	 * B: .. T: x.
	 */
	protected static final AxisAlignedBB AABB_QTR_TOP_WEST = new AxisAlignedBB(0.0D, 0.5D, 0.0D, 0.5D, 1.0D, 1.0D);
	/**
	 * B: .. T: .x
	 * B: .. T: .x
	 */
	protected static final AxisAlignedBB AABB_QTR_TOP_EAST = new AxisAlignedBB(0.5D, 0.5D, 0.0D, 1.0D, 1.0D, 1.0D);
	/**
	 * B: .. T: xx
	 * B: .. T: ..
	 */
	protected static final AxisAlignedBB AABB_QTR_TOP_NORTH = new AxisAlignedBB(0.0D, 0.5D, 0.0D, 1.0D, 1.0D, 0.5D);
	/**
	 * B: .. T: ..
	 * B: .. T: xx
	 */
	protected static final AxisAlignedBB AABB_QTR_TOP_SOUTH = new AxisAlignedBB(0.0D, 0.5D, 0.5D, 1.0D, 1.0D, 1.0D);
	/**
	 * B: .. T: x.
	 * B: .. T: ..
	 */
	protected static final AxisAlignedBB AABB_OCT_TOP_NW = new AxisAlignedBB(0.0D, 0.5D, 0.0D, 0.5D, 1.0D, 0.5D);
	/**
	 * B: .. T: .x
	 * B: .. T: ..
	 */
	protected static final AxisAlignedBB AABB_OCT_TOP_NE = new AxisAlignedBB(0.5D, 0.5D, 0.0D, 1.0D, 1.0D, 0.5D);
	/**
	 * B: .. T: ..
	 * B: .. T: x.
	 */
	protected static final AxisAlignedBB AABB_OCT_TOP_SW = new AxisAlignedBB(0.0D, 0.5D, 0.5D, 0.5D, 1.0D, 1.0D);
	/**
	 * B: .. T: ..
	 * B: .. T: .x
	 */
	protected static final AxisAlignedBB AABB_OCT_TOP_SE = new AxisAlignedBB(0.5D, 0.5D, 0.5D, 1.0D, 1.0D, 1.0D);
	/**
	 * B: x. T: ..
	 * B: x. T: ..
	 */
	protected static final AxisAlignedBB AABB_QTR_BOT_WEST = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.5D, 0.5D, 1.0D);
	/**
	 * B: .x T: ..
	 * B: .x T: ..
	 */
	protected static final AxisAlignedBB AABB_QTR_BOT_EAST = new AxisAlignedBB(0.5D, 0.0D, 0.0D, 1.0D, 0.5D, 1.0D);
	/**
	 * B: xx T: ..
	 * B: .. T: ..
	 */
	protected static final AxisAlignedBB AABB_QTR_BOT_NORTH = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.5D, 0.5D);
	/**
	 * B: .. T: ..
	 * B: xx T: ..
	 */
	protected static final AxisAlignedBB AABB_QTR_BOT_SOUTH = new AxisAlignedBB(0.0D, 0.0D, 0.5D, 1.0D, 0.5D, 1.0D);
	/**
	 * B: x. T: ..
	 * B: .. T: ..
	 */
	protected static final AxisAlignedBB AABB_OCT_BOT_NW = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.5D, 0.5D, 0.5D);
	/**
	 * B: .x T: ..
	 * B: .. T: ..
	 */
	protected static final AxisAlignedBB AABB_OCT_BOT_NE = new AxisAlignedBB(0.5D, 0.0D, 0.0D, 1.0D, 0.5D, 0.5D);
	/**
	 * B: .. T: ..
	 * B: x. T: ..
	 */
	protected static final AxisAlignedBB AABB_OCT_BOT_SW = new AxisAlignedBB(0.0D, 0.0D, 0.5D, 0.5D, 0.5D, 1.0D);
	/**
	 * B: .. T: ..
	 * B: .x T: ..
	 */
	protected static final AxisAlignedBB AABB_OCT_BOT_SE = new AxisAlignedBB(0.5D, 0.0D, 0.5D, 1.0D, 0.5D, 1.0D);

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
			case STAIR -> {
				int i = DynamicBlockStateContainer.getMetaFromStairHalf(state.getValue(DynamicBlockStateContainer.STAIR_HALF));
				i |= DynamicBlockStateContainer.getMetaFromStairFacing(state.getValue(DynamicBlockStateContainer.HORIZONTAL));
				return i;
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
			case STAIR -> {
				IBlockState iblockstate = getDefaultState().withProperty(DynamicBlockStateContainer.STAIR_HALF, DynamicBlockStateContainer.getStairHalfFromMeta(meta));
				iblockstate = iblockstate.withProperty(DynamicBlockStateContainer.HORIZONTAL, DynamicBlockStateContainer.getStairFacingFromMeta(meta));
				return iblockstate;
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
			case STAIR -> {
				IBlockState iblockstate = getDefaultState();
				iblockstate = iblockstate.withProperty(DynamicBlockStateContainer.HORIZONTAL, placer.getHorizontalFacing()).withProperty(DynamicBlockStateContainer.STAIR_SHAPE, BlockStairs.EnumShape.STRAIGHT);
				return iblockstate.withProperty(DynamicBlockStateContainer.STAIR_HALF, facing != EnumFacing.DOWN && (facing == EnumFacing.UP || (double) hitY <= 0.5D) ? BlockStairs.EnumHalf.BOTTOM : EnumHalf.TOP);
			}
			default -> throw new AssertionError();
		}
	}

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
		switch (blockStateType) {
			case STAIR -> {
				return state.withProperty(DynamicBlockStateContainer.STAIR_SHAPE, getStairsShape(state, worldIn, pos));
			}
			default -> {
				return state;
			}
		}
	}

	/**
	 * Returns the blockstate with the given rotation from the passed blockstate. If inapplicable, returns the passed
	 * blockstate.
	 *
	 * @deprecated call via {@link IBlockState#withRotation(Rotation)} whenever possible. Implementing/overriding is
	 * fine.
	 */
	@Override
	public IBlockState withRotation(IBlockState state, Rotation rot) {
		switch (blockStateType) {
			case STAIR -> {
				return state.withProperty(DynamicBlockStateContainer.HORIZONTAL, rot.rotate(state.getValue(DynamicBlockStateContainer.HORIZONTAL)));
			}
			default -> {
				return state;
			}
		}
	}

	/**
	 * Returns the blockstate with the given mirror of the passed blockstate. If inapplicable, returns the passed
	 * blockstate.
	 *
	 * @deprecated call via {@link IBlockState#withMirror(Mirror)} whenever possible. Implementing/overriding is fine.
	 */
	@Override
	@SuppressWarnings("incomplete-switch")
	public IBlockState withMirror(IBlockState state, Mirror mirrorIn) {
		switch (blockStateType) {
			case STAIR -> {
				EnumFacing enumfacing = (EnumFacing) state.getValue(DynamicBlockStateContainer.HORIZONTAL);
				BlockStairs.EnumShape blockstairs$enumshape = (BlockStairs.EnumShape) state.getValue(DynamicBlockStateContainer.STAIR_SHAPE);

				switch (mirrorIn) {
					case LEFT_RIGHT -> {
						if (enumfacing.getAxis() == Axis.Z) {
							return switch (blockstairs$enumshape) {
								case OUTER_LEFT ->
										state.withRotation(Rotation.CLOCKWISE_180).withProperty(DynamicBlockStateContainer.STAIR_SHAPE, BlockStairs.EnumShape.OUTER_RIGHT);
								case OUTER_RIGHT ->
										state.withRotation(Rotation.CLOCKWISE_180).withProperty(DynamicBlockStateContainer.STAIR_SHAPE, BlockStairs.EnumShape.OUTER_LEFT);
								case INNER_RIGHT ->
										state.withRotation(Rotation.CLOCKWISE_180).withProperty(DynamicBlockStateContainer.STAIR_SHAPE, BlockStairs.EnumShape.INNER_LEFT);
								case INNER_LEFT ->
										state.withRotation(Rotation.CLOCKWISE_180).withProperty(DynamicBlockStateContainer.STAIR_SHAPE, BlockStairs.EnumShape.INNER_RIGHT);
								default -> state.withRotation(Rotation.CLOCKWISE_180);
							};
						}
					}
					case FRONT_BACK -> {
						if (enumfacing.getAxis() == Axis.X) {
							return switch (blockstairs$enumshape) {
								case OUTER_LEFT ->
										state.withRotation(Rotation.CLOCKWISE_180).withProperty(DynamicBlockStateContainer.STAIR_SHAPE, BlockStairs.EnumShape.OUTER_RIGHT);
								case OUTER_RIGHT ->
										state.withRotation(Rotation.CLOCKWISE_180).withProperty(DynamicBlockStateContainer.STAIR_SHAPE, BlockStairs.EnumShape.OUTER_LEFT);
								case INNER_RIGHT ->
										state.withRotation(Rotation.CLOCKWISE_180).withProperty(DynamicBlockStateContainer.STAIR_SHAPE, BlockStairs.EnumShape.INNER_RIGHT);
								case INNER_LEFT ->
										state.withRotation(Rotation.CLOCKWISE_180).withProperty(DynamicBlockStateContainer.STAIR_SHAPE, BlockStairs.EnumShape.INNER_LEFT);
								case STRAIGHT -> state.withRotation(Rotation.CLOCKWISE_180);
							};
						}
					}
				}

				return state;
			}
			default -> {
				return state;
			}
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
						return AABB_HALF_TOP;
					}
					case BOTTOM -> {
						return AABB_HALF_BOTTOM;
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
		if (isRemoved)
			return;
		switch (blockStateType) {
			case STAIR -> {
				if (!isActualState)
					state = getActualState(state, worldIn, pos);

				for (AxisAlignedBB aabb : getStairCollisionBoxList(state)) {
					addCollisionBoxToList(pos, entityBox, collidingBoxes, aabb);
				}
			}
			default -> super.addCollisionBoxToList(state, worldIn, pos, entityBox, collidingBoxes, entityIn, isActualState);
		}
	}


	@Override
	public RayTraceResult collisionRayTrace(IBlockState blockState, World worldIn, BlockPos pos, Vec3d start, Vec3d end) {
		switch (blockStateType) {
			case STAIR -> {
				List<RayTraceResult> list = Lists.newArrayList();

				for (AxisAlignedBB axisalignedbb : getStairCollisionBoxList(getActualState(blockState, worldIn, pos))) {
					list.add(rayTrace(pos, start, end, axisalignedbb));
				}

				RayTraceResult raytraceresult1 = null;
				double d1 = 0.0D;

				for (RayTraceResult raytraceresult : list) {
					if (raytraceresult != null) {
						double d0 = raytraceresult.hitVec.squareDistanceTo(end);

						if (d0 > d1) {
							raytraceresult1 = raytraceresult;
							d1 = d0;
						}
					}
				}

				return raytraceresult1;
			}
			default -> {
				return super.collisionRayTrace(blockState, worldIn, pos, start, end);
			}
		}
	}


	@Override
	public boolean isTopSolid(IBlockState state) {
		switch (blockStateType) {
			case SLAB -> {
				return state.getValue(DynamicBlockStateContainer.SLAB) != EnumSlabType.BOTTOM;
			}
			case STAIR -> {
				return state.getValue(DynamicBlockStateContainer.STAIR_HALF) == BlockStairs.EnumHalf.TOP;
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
			case STAIR -> {
				state = getActualState(state, worldIn, pos);
				if (face.getAxis() == EnumFacing.Axis.Y) {
					return face == EnumFacing.UP == (state.getValue(DynamicBlockStateContainer.STAIR_HALF) == BlockStairs.EnumHalf.TOP) ? BlockFaceShape.SOLID : BlockFaceShape.UNDEFINED;
				} else {
					BlockStairs.EnumShape blockstairs$enumshape = state.getValue(DynamicBlockStateContainer.STAIR_SHAPE);

					if (blockstairs$enumshape != BlockStairs.EnumShape.OUTER_LEFT && blockstairs$enumshape != BlockStairs.EnumShape.OUTER_RIGHT) {
						EnumFacing enumfacing = state.getValue(DynamicBlockStateContainer.HORIZONTAL);

						return switch (blockstairs$enumshape) {
							case INNER_RIGHT -> enumfacing != face && enumfacing != face.rotateYCCW() ? BlockFaceShape.UNDEFINED : BlockFaceShape.SOLID;
							case INNER_LEFT -> enumfacing != face && enumfacing != face.rotateY() ? BlockFaceShape.UNDEFINED : BlockFaceShape.SOLID;
							case STRAIGHT -> enumfacing == face ? BlockFaceShape.SOLID : BlockFaceShape.UNDEFINED;
							default -> BlockFaceShape.UNDEFINED;
						};
					} else {
						return BlockFaceShape.UNDEFINED;
					}
				}
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
			case STAIR -> {
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
			case STAIR -> {
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
			case STAIR -> {
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
			case STAIR -> {
				if (net.minecraftforge.common.ForgeModContainer.disableStairSlabCulling)
					return super.doesSideBlockRendering(state, world, pos, face);

				if (state.isOpaqueCube())
					return true;

				state = getActualState(state, world, pos);

				EnumHalf half = state.getValue(DynamicBlockStateContainer.STAIR_HALF);
				EnumFacing side = state.getValue(DynamicBlockStateContainer.HORIZONTAL);
				EnumShape shape = state.getValue(DynamicBlockStateContainer.STAIR_SHAPE);
				if (face == EnumFacing.UP)
					return half == EnumHalf.TOP;
				if (face == EnumFacing.DOWN)
					return half == EnumHalf.BOTTOM;
				if (shape == EnumShape.OUTER_LEFT || shape == EnumShape.OUTER_RIGHT)
					return false;
				if (face == side)
					return true;
				if (shape == EnumShape.INNER_LEFT && face.rotateY() == side)
					return true;
				if (shape == EnumShape.INNER_RIGHT && face.rotateYCCW() == side)
					return true;
				return false;
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
			case STAIR -> {
				IBlockState state = getActualState(base_state, world, pos);
				boolean flipped = state.getValue(DynamicBlockStateContainer.STAIR_HALF) == BlockStairs.EnumHalf.TOP;
				BlockStairs.EnumShape shape = (BlockStairs.EnumShape) state.getValue(DynamicBlockStateContainer.STAIR_SHAPE);
				EnumFacing facing = (EnumFacing) state.getValue(DynamicBlockStateContainer.HORIZONTAL);
				if (side == EnumFacing.UP)
					return flipped;
				if (side == EnumFacing.DOWN)
					return !flipped;
				if (facing == side)
					return true;
				if (flipped) {
					if (shape == BlockStairs.EnumShape.INNER_LEFT)
						return side == facing.rotateYCCW();
					if (shape == BlockStairs.EnumShape.INNER_RIGHT)
						return side == facing.rotateY();
				} else {
					if (shape == BlockStairs.EnumShape.INNER_LEFT)
						return side == facing.rotateY();
					if (shape == BlockStairs.EnumShape.INNER_RIGHT)
						return side == facing.rotateYCCW();
				}
				return false;
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


	private static List<AxisAlignedBB> getStairCollisionBoxList(IBlockState bstate) {
		List<AxisAlignedBB> list = Lists.newArrayList();
		boolean flag = bstate.getValue(DynamicBlockStateContainer.STAIR_HALF) == BlockStairs.EnumHalf.TOP;
		list.add(flag ? AABB_HALF_TOP : AABB_HALF_BOTTOM);
		BlockStairs.EnumShape shape = bstate.getValue(DynamicBlockStateContainer.STAIR_SHAPE);

		if (shape == BlockStairs.EnumShape.STRAIGHT || shape == BlockStairs.EnumShape.INNER_LEFT || shape == BlockStairs.EnumShape.INNER_RIGHT) {
			list.add(getCollQuarterBlock(bstate));
		}

		if (shape != BlockStairs.EnumShape.STRAIGHT) {
			list.add(getCollEighthBlock(bstate));
		}

		return list;
	}

	/**
	 * Returns a bounding box representing a quarter of a block (two eight-size cubes back to back).
	 * Used in all stair shapes except OUTER.
	 */
	private static AxisAlignedBB getCollQuarterBlock(IBlockState bstate) {
		boolean flag = bstate.getValue(DynamicBlockStateContainer.STAIR_HALF) == BlockStairs.EnumHalf.TOP;

		return switch (bstate.getValue(DynamicBlockStateContainer.HORIZONTAL)) {
			default -> flag ? AABB_QTR_BOT_NORTH : AABB_QTR_TOP_NORTH;
			case SOUTH -> flag ? AABB_QTR_BOT_SOUTH : AABB_QTR_TOP_SOUTH;
			case WEST -> flag ? AABB_QTR_BOT_WEST : AABB_QTR_TOP_WEST;
			case EAST -> flag ? AABB_QTR_BOT_EAST : AABB_QTR_TOP_EAST;
		};
	}

	/**
	 * Returns a bounding box representing an eighth of a block (a block whose three dimensions are halved).
	 * Used in all stair shapes except STRAIGHT (gets added alone in the case of OUTER; alone with a quarter block in
	 * case of INSIDE).
	 */
	private static AxisAlignedBB getCollEighthBlock(IBlockState bstate) {
		EnumFacing enumfacing = bstate.getValue(DynamicBlockStateContainer.HORIZONTAL);
		EnumFacing enumfacing1 = switch (bstate.getValue(DynamicBlockStateContainer.STAIR_SHAPE)) {
			default -> enumfacing;
			case OUTER_RIGHT -> enumfacing.rotateY();
			case INNER_RIGHT -> enumfacing.getOpposite();
			case INNER_LEFT -> enumfacing.rotateYCCW();
		};

		boolean flag = bstate.getValue(DynamicBlockStateContainer.STAIR_HALF) == BlockStairs.EnumHalf.TOP;

		return switch (enumfacing1) {
			default -> flag ? AABB_OCT_BOT_NW : AABB_OCT_TOP_NW;
			case SOUTH -> flag ? AABB_OCT_BOT_SE : AABB_OCT_TOP_SE;
			case WEST -> flag ? AABB_OCT_BOT_SW : AABB_OCT_TOP_SW;
			case EAST -> flag ? AABB_OCT_BOT_NE : AABB_OCT_TOP_NE;
		};
	}


	private static BlockStairs.EnumShape getStairsShape(IBlockState p_185706_0_, IBlockAccess p_185706_1_, BlockPos p_185706_2_) {
		EnumFacing enumfacing = p_185706_0_.getValue(DynamicBlockStateContainer.HORIZONTAL);
		IBlockState iblockstate = p_185706_1_.getBlockState(p_185706_2_.offset(enumfacing));

		if (BlockStairs.isBlockStairs(iblockstate) && p_185706_0_.getValue(DynamicBlockStateContainer.STAIR_HALF) == iblockstate.getValue(DynamicBlockStateContainer.STAIR_HALF)) {
			EnumFacing enumfacing1 = iblockstate.getValue(DynamicBlockStateContainer.HORIZONTAL);

			if (enumfacing1.getAxis() != p_185706_0_.getValue(DynamicBlockStateContainer.HORIZONTAL).getAxis() && isDifferentStairs(p_185706_0_, p_185706_1_, p_185706_2_, enumfacing1.getOpposite())) {
				if (enumfacing1 == enumfacing.rotateYCCW()) {
					return BlockStairs.EnumShape.OUTER_LEFT;
				}

				return BlockStairs.EnumShape.OUTER_RIGHT;
			}
		}

		IBlockState iblockstate1 = p_185706_1_.getBlockState(p_185706_2_.offset(enumfacing.getOpposite()));

		if (BlockStairs.isBlockStairs(iblockstate1) && p_185706_0_.getValue(DynamicBlockStateContainer.STAIR_HALF) == iblockstate1.getValue(DynamicBlockStateContainer.STAIR_HALF)) {
			EnumFacing enumfacing2 = iblockstate1.getValue(DynamicBlockStateContainer.HORIZONTAL);

			if (enumfacing2.getAxis() != p_185706_0_.getValue(DynamicBlockStateContainer.HORIZONTAL).getAxis() && isDifferentStairs(p_185706_0_, p_185706_1_, p_185706_2_, enumfacing2)) {
				if (enumfacing2 == enumfacing.rotateYCCW()) {
					return BlockStairs.EnumShape.INNER_LEFT;
				}

				return BlockStairs.EnumShape.INNER_RIGHT;
			}
		}

		return BlockStairs.EnumShape.STRAIGHT;
	}

	private static boolean isDifferentStairs(IBlockState p_185704_0_, IBlockAccess p_185704_1_, BlockPos p_185704_2_, EnumFacing p_185704_3_) {
		IBlockState iblockstate = p_185704_1_.getBlockState(p_185704_2_.offset(p_185704_3_));
		return !BlockStairs.isBlockStairs(iblockstate) || iblockstate.getValue(DynamicBlockStateContainer.HORIZONTAL) != p_185704_0_.getValue(DynamicBlockStateContainer.HORIZONTAL) || iblockstate.getValue(DynamicBlockStateContainer.STAIR_HALF) != p_185704_0_.getValue(DynamicBlockStateContainer.STAIR_HALF);
	}

}
