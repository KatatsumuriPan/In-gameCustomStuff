package kpan.ig_custom_stuff.block;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

public class DynamicBlockStateContainer extends BlockStateContainer {
	public static final PropertyInteger META = PropertyInteger.create("meta", 0, 15);
	public static final PropertyDirection FACING = PropertyDirection.create("facing");
	public static final PropertyDirection HORIZONTAL = BlockHorizontal.FACING;
	public static final PropertyEnum<Axis> XYZ_AXIS = PropertyEnum.create("axis", EnumFacing.Axis.class);

	public DynamicBlockStateContainer(DynamicBlockBase blockIn) {
		super(blockIn, META);
		List<DynamicBlockState> list1 = new ArrayList<>(16);
		for (int i = 0; i < 16; i++) {
			list1.add(new DynamicBlockState(this, blockIn, i));
		}
		validStates = ImmutableList.copyOf(list1);
	}


	public static int getMetaFromXYZ(Axis axis) {
		switch (axis) {
			case X -> {
				return 4;
			}
			case Y -> {
				return 0;
			}
			case Z -> {
				return 8;
			}
			default -> {
				return 0;
			}
		}
	}
	public static Axis getAxisFromMeta(int meta) {
		if (meta == 0)
			return Axis.Y;
		else if (meta == 4)
			return Axis.X;
		else if (meta == 8)
			return Axis.Z;
		else
			return Axis.Y;
	}

	@SuppressWarnings("deprecation")
	public static class DynamicBlockState implements IBlockState {

		private static final Joiner COMMA_JOINER = Joiner.on(',');
		@SuppressWarnings("Guava")
		private static final Function<Entry<IProperty<?>, Comparable<?>>, String> MAP_ENTRY_TO_STRING = new Function<>() {
			@Override
			public String apply(@Nullable Entry<IProperty<?>, Comparable<?>> p_apply_1_) {
				if (p_apply_1_ == null) {
					return "<NULL>";
				} else {
					IProperty<?> iproperty = p_apply_1_.getKey();
					return iproperty.getName() + "=" + getPropertyName(iproperty, p_apply_1_.getValue());
				}
			}
			@SuppressWarnings("unchecked")
			private <T extends Comparable<T>> String getPropertyName(IProperty<T> property, Comparable<?> entry) {
				return property.getName((T) entry);
			}
		};

		private final DynamicBlockStateContainer owner;
		private final DynamicBlockBase block;
		private final int meta;
		public DynamicBlockState(DynamicBlockStateContainer owner, DynamicBlockBase block, int meta) {
			this.owner = owner;
			this.block = block;
			this.meta = meta;
		}

		@Override
		public Collection<IProperty<?>> getPropertyKeys() {
			switch (block.getBlockStateType()) {
				case SIMPLE -> {
					return Collections.emptyList();
				}
				case FACE6 -> {
					return Collections.singletonList(FACING);
				}
				case HORIZONTAL4 -> {
					return Collections.singletonList(HORIZONTAL);
				}
				case XYZ -> {
					return Collections.singletonList(XYZ_AXIS);
				}
				default -> throw new AssertionError();
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T extends Comparable<T>> T getValue(IProperty<T> property) {
			switch (block.getBlockStateType()) {
				case SIMPLE -> {
					//何もしない
				}
				case FACE6 -> {
					if (property == FACING)
						return (T) EnumFacing.byIndex(meta);
				}
				case HORIZONTAL4 -> {
					if (property == HORIZONTAL)
						return (T) EnumFacing.byHorizontalIndex(meta);
				}
				case XYZ -> {
					if (property == XYZ_AXIS)
						return (T) getAxisFromMeta(meta);
				}
				default -> throw new AssertionError();
			}
			return property.getAllowedValues().iterator().next();
		}

		@Override
		public <T extends Comparable<T>, V extends T> IBlockState withProperty(IProperty<T> property, V value) {
			switch (block.getBlockStateType()) {
				case SIMPLE -> {
					//何もしない
				}
				case FACE6 -> {
					if (property == FACING)
						return owner.validStates.get(((EnumFacing) value).getIndex());
				}
				case HORIZONTAL4 -> {
					if (property == HORIZONTAL)
						return owner.validStates.get(((EnumFacing) value).getHorizontalIndex());
				}
				case XYZ -> {
					if (property == XYZ_AXIS) {
						return owner.validStates.get(getMetaFromXYZ((EnumFacing.Axis) value));
					}
				}
				default -> throw new AssertionError();
			}
			return owner.validStates.get(0);
		}

		@Override
		public <T extends Comparable<T>> IBlockState cycleProperty(IProperty<T> property) {
			return withProperty(property, cyclePropertyValue(property.getAllowedValues(), getValue(property)));
		}

		@Override
		public ImmutableMap<IProperty<?>, Comparable<?>> getProperties() {
			switch (block.getBlockStateType()) {
				case SIMPLE -> {
					return ImmutableMap.copyOf(Collections.emptyMap());
				}
				case FACE6 -> {
					return ImmutableMap.of(FACING, EnumFacing.byIndex(meta));
				}
				case HORIZONTAL4 -> {
					return ImmutableMap.of(HORIZONTAL, EnumFacing.byHorizontalIndex(meta));
				}
				case XYZ -> {
					return ImmutableMap.of(XYZ_AXIS, getAxisFromMeta(meta));
				}
				default -> throw new AssertionError();
			}
		}

		@Override
		public String toString() {
			StringBuilder stringbuilder = new StringBuilder();
			stringbuilder.append(Block.REGISTRY.getNameForObject(getBlock()));
			if (!getProperties().isEmpty()) {
				stringbuilder.append("[");
				COMMA_JOINER.appendTo(stringbuilder, Iterables.transform(getProperties().entrySet(), MAP_ENTRY_TO_STRING));
				stringbuilder.append("]");
			}
			return stringbuilder.toString();
		}

		//blockへ委譲
		@Override
		public boolean onBlockEventReceived(World worldIn, BlockPos pos, int id, int param) {
			return block.eventReceived(this, worldIn, pos, id, param);
		}
		@Override
		public void neighborChanged(World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
			block.neighborChanged(this, worldIn, pos, blockIn, fromPos);
		}
		@Override
		public Material getMaterial() {
			return block.getMaterial(this);
		}
		@Override
		public boolean isFullBlock() {
			return block.isFullBlock(this);
		}
		@Override
		public boolean canEntitySpawn(Entity entityIn) {
			return block.canEntitySpawn(this, entityIn);
		}
		@Override
		public int getLightOpacity() {
			return block.getLightOpacity(this);
		}
		@Override
		public int getLightOpacity(IBlockAccess world, BlockPos pos) {
			return block.getLightOpacity(this, world, pos);
		}
		@Override
		public int getLightValue() {
			return block.getLightValue(this);
		}
		@Override
		public int getLightValue(IBlockAccess world, BlockPos pos) {
			return block.getLightValue(this, world, pos);
		}
		@Override
		@SideOnly(Side.CLIENT)
		public boolean isTranslucent() {
			return block.isTranslucent(this);
		}
		@Override
		public boolean useNeighborBrightness() {
			return block.getUseNeighborBrightness(this);
		}
		@Override
		public MapColor getMapColor(IBlockAccess p_185909_1_, BlockPos p_185909_2_) {
			return block.getMapColor(this, p_185909_1_, p_185909_2_);
		}
		@Override
		public IBlockState withRotation(Rotation rot) {
			return block.withRotation(this, rot);
		}
		@Override
		public IBlockState withMirror(Mirror mirrorIn) {
			return block.withMirror(this, mirrorIn);
		}
		@Override
		public boolean isFullCube() {
			return block.isFullCube(this);
		}
		@Override
		@SideOnly(Side.CLIENT)
		public boolean hasCustomBreakingProgress() {
			return block.hasCustomBreakingProgress(this);
		}
		@Override
		public EnumBlockRenderType getRenderType() {
			return block.getRenderType(this);
		}
		@Override
		@SideOnly(Side.CLIENT)
		public int getPackedLightmapCoords(IBlockAccess source, BlockPos pos) {
			return block.getPackedLightmapCoords(this, source, pos);
		}
		@Override
		@SideOnly(Side.CLIENT)
		public float getAmbientOcclusionLightValue() {
			return block.getAmbientOcclusionLightValue(this);
		}
		@Override
		public boolean isBlockNormalCube() {
			return block.isBlockNormalCube(this);
		}
		@Override
		public boolean isNormalCube() {
			return block.isNormalCube(this);
		}
		@Override
		public boolean canProvidePower() {
			return block.canProvidePower(this);
		}
		@Override
		public int getWeakPower(IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
			return block.getWeakPower(this, blockAccess, pos, side);
		}
		@Override
		public boolean hasComparatorInputOverride() {
			return block.hasComparatorInputOverride(this);
		}
		@Override
		public int getComparatorInputOverride(World worldIn, BlockPos pos) {
			return block.getComparatorInputOverride(this, worldIn, pos);
		}
		@Override
		public float getBlockHardness(World worldIn, BlockPos pos) {
			return block.getBlockHardness(this, worldIn, pos);
		}
		@Override
		public float getPlayerRelativeBlockHardness(EntityPlayer player, World worldIn, BlockPos pos) {
			return block.getPlayerRelativeBlockHardness(this, player, worldIn, pos);
		}
		@Override
		public int getStrongPower(IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
			return block.getStrongPower(this, blockAccess, pos, side);
		}
		@Override
		public EnumPushReaction getPushReaction() {
			return block.getPushReaction(this);
		}
		@Override
		public IBlockState getActualState(IBlockAccess blockAccess, BlockPos pos) {
			return block.getActualState(this, blockAccess, pos);
		}
		@Override
		@SideOnly(Side.CLIENT)
		public AxisAlignedBB getSelectedBoundingBox(World worldIn, BlockPos pos) {
			return block.getSelectedBoundingBox(this, worldIn, pos);
		}
		@Override
		@SideOnly(Side.CLIENT)
		public boolean shouldSideBeRendered(IBlockAccess blockAccess, BlockPos pos, EnumFacing facing) {
			return block.shouldSideBeRendered(this, blockAccess, pos, facing);
		}
		@Override
		public boolean isOpaqueCube() {
			return block.isOpaqueCube(this);
		}
		@Override
		@Nullable
		public AxisAlignedBB getCollisionBoundingBox(IBlockAccess worldIn, BlockPos pos) {
			return block.getCollisionBoundingBox(this, worldIn, pos);
		}
		@Override
		public void addCollisionBoxToList(World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean p_185908_6_) {
			block.addCollisionBoxToList(this, worldIn, pos, entityBox, collidingBoxes, entityIn, p_185908_6_);
		}
		@Override
		public AxisAlignedBB getBoundingBox(IBlockAccess blockAccess, BlockPos pos) {
			return block.getBoundingBox(this, blockAccess, pos);
		}
		@Override
		public RayTraceResult collisionRayTrace(World worldIn, BlockPos pos, Vec3d start, Vec3d end) {
			return block.collisionRayTrace(this, worldIn, pos, start, end);
		}
		@Override
		public boolean isTopSolid() {
			return block.isTopSolid(this);
		}
		@Override
		public boolean doesSideBlockRendering(IBlockAccess world, BlockPos pos, EnumFacing side) {
			return block.doesSideBlockRendering(this, world, pos, side);
		}
		@Override
		public boolean isSideSolid(IBlockAccess world, BlockPos pos, EnumFacing side) {
			return block.isSideSolid(this, world, pos, side);
		}
		@Override
		public boolean doesSideBlockChestOpening(IBlockAccess world, BlockPos pos, EnumFacing side) {
			return block.doesSideBlockChestOpening(this, world, pos, side);
		}
		@Override
		public Vec3d getOffset(IBlockAccess access, BlockPos pos) {
			return block.getOffset(this, access, pos);
		}
		@Override
		public boolean causesSuffocation() {
			return block.causesSuffocation(this);
		}
		@Override
		public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, BlockPos pos, EnumFacing facing) {
			return block.getBlockFaceShape(worldIn, this, pos, facing);
		}
		@Override
		public Block getBlock() {
			return block;
		}

		private static <T> T cyclePropertyValue(Collection<T> values, T currentValue) {
			Iterator<T> iterator = values.iterator();

			while (iterator.hasNext()) {
				if (iterator.next().equals(currentValue)) {
					if (iterator.hasNext()) {
						return iterator.next();
					}

					return values.iterator().next();
				}
			}

			return iterator.next();
		}
	}
}
