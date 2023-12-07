package kpan.ig_custom_stuff.block;

import kpan.ig_custom_stuff.util.MyReflectionHelper;
import kpan.ig_custom_stuff.util.interfaces.block.IHasMultiModels;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DynamicBlockBase extends Block {

	protected int fortuneBonus = 0;

	private boolean isFullOpaqueCube;
	private boolean isRemoved = false;
	private FaceCullingType faceCullingType;

	public DynamicBlockBase(ResourceLocation blockId, BlockPropertyEntry blockPropertyEntry) {
		super(Material.ROCK, Material.ROCK.getMaterialMapColor());
		setTranslationKey(blockId.getNamespace() + "." + blockId.getPath());
		MyReflectionHelper.setPrivateField(Impl.class, this, "registryName", blockId);

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


	//アイテムドロップ等

	@Override
	public int damageDropped(IBlockState state) { return getMetaFromState(state); }

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

	//タイルエンティティ

	//ブロックステート
	protected ArrayList<IProperty<?>> getProperties() {
		return new ArrayList<>();
		/*	Overrideサンプル
		ArrayList<IProperty<?>> properties = super.getProperties();
		properties.add(AGE);
		return properties;
		 */
	}

	@Override
	public int getMetaFromState(IBlockState state) { return 0; }

	@SuppressWarnings("deprecation")
	@Override
	public IBlockState getStateFromMeta(int meta) { return getDefaultState(); }

	@Override
	protected final BlockStateContainer createBlockState() {
		ArrayList<IProperty<?>> properties = getProperties();
		return new BlockStateContainer(this, properties.toArray(new IProperty[0]));
	}

	//描画、モデル系
	@SuppressWarnings("deprecation")
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) { return super.getBoundingBox(state, source, pos); }
	@SuppressWarnings("deprecation")
	@Override
	public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState) {
		if (!isRemoved)
			super.addCollisionBoxToList(state, worldIn, pos, entityBox, collidingBoxes, entityIn, isActualState);
	}
	@Override
	public BlockRenderLayer getRenderLayer() {
		return isFullOpaqueCube ? BlockRenderLayer.SOLID : BlockRenderLayer.CUTOUT;
	}
	@SuppressWarnings("deprecation")
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return isFullOpaqueCube;
	}
	@SuppressWarnings("deprecation")
	@Override
	public boolean isFullCube(IBlockState state) {
		return isFullOpaqueCube;
	}
	@SuppressWarnings("deprecation")
	@Override
	public boolean isFullBlock(IBlockState state) {
		return isFullOpaqueCube;
	}
	@SuppressWarnings("deprecation")
	@Override
	public boolean getUseNeighborBrightness(IBlockState state) {
		return !isOpaqueCube(state);
	}
	@Override
	public int getLightOpacity(IBlockState state, IBlockAccess world, BlockPos pos) {
		return isOpaqueCube(state) ? 255 : 0;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
		switch (faceCullingType) {
			case NORMAL -> {
				return super.shouldSideBeRendered(blockState, blockAccess, pos, side);
			}
			case GLASS -> {
				IBlockState iblockstate = blockAccess.getBlockState(pos.offset(side));
				Block block = iblockstate.getBlock();
				if (blockState != iblockstate) {
					return true;
				}
				if (block == this) {
					return false;
				}
				return super.shouldSideBeRendered(blockState, blockAccess, pos, side);
			}
			default -> throw new AssertionError();
		}
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
