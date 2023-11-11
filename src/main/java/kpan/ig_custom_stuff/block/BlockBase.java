package kpan.ig_custom_stuff.block;

import kpan.ig_custom_stuff.ModMain;
import kpan.ig_custom_stuff.block.item.ItemBlockBase;
import kpan.ig_custom_stuff.block.item.ItemBlockVariants;
import kpan.ig_custom_stuff.item.ItemInit;
import kpan.ig_custom_stuff.util.PropertyUtil;
import kpan.ig_custom_stuff.util.interfaces.IHasModel;
import kpan.ig_custom_stuff.util.interfaces.IMetaName;
import kpan.ig_custom_stuff.util.interfaces.block.IHasMultiModels;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Random;

public class BlockBase extends Block implements IHasModel {

	private static final SoundType DEFAULT_SOUND = SoundType.STONE;
	private static final float DEFAULT_HARDNESS = 0.5f;
	private static final ArrayList<BlockBase> FOR_REGISTER = new ArrayList<>();
	private static boolean preparing = false;

	public static void prepareRegistering() {
		//noinspection ResultOfMethodCallIgnored
		BlockInit.BLOCKS.isEmpty();//BlockInitを先に読み込む
		preparing = true;
		for (BlockBase b : FOR_REGISTER) {
			b.registerAsBlock();
			b.registerAsItem();
		}
		preparing = false;
	}

	protected int fortuneBonus = 0;

	@SuppressWarnings("unused")
	public BlockBase(String name, Material material) { this(name, material, material.getMaterialMapColor(), DEFAULT_SOUND, DEFAULT_HARDNESS); }
	@SuppressWarnings("unused")
	public BlockBase(String name, Material material, MapColor mapColor) { this(name, material, mapColor, DEFAULT_SOUND, DEFAULT_HARDNESS); }
	@SuppressWarnings("unused")
	public BlockBase(String name, Material material, SoundType soundtype) { this(name, material, material.getMaterialMapColor(), soundtype, DEFAULT_HARDNESS); }
	@SuppressWarnings("unused")
	public BlockBase(String name, Material material, MapColor mapColor, SoundType soundtype) { this(name, material, mapColor, soundtype, DEFAULT_HARDNESS); }
	@SuppressWarnings("unused")
	public BlockBase(String name, Material material, float hardness) { this(name, material, material.getMaterialMapColor(), DEFAULT_SOUND, hardness); }
	@SuppressWarnings("unused")
	public BlockBase(String name, Material material, MapColor mapColor, float hardness) { this(name, material, mapColor, DEFAULT_SOUND, hardness); }
	@SuppressWarnings("unused")
	public BlockBase(String name, Material material, SoundType soundtype, float hardness) { this(name, material, material.getMaterialMapColor(), soundtype, hardness); }
	public BlockBase(String name, Material material, MapColor mapColor, SoundType soundtype, float hardness) {
		super(material, mapColor);

		setTranslationKey(name);
		setRegistryName(name);
		setSoundType(soundtype);
		setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
		setHardness(hardness);
		if (!preparing)
			FOR_REGISTER.add(this);
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
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) { return baseBoundingBox(state, source, pos); }
	@SuppressWarnings("deprecation")
	protected AxisAlignedBB baseBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) { return super.getBoundingBox(state, source, pos); }

	//register
	public void registerAsBlock() {
		BlockInit.BLOCKS.add(this);
	}

	@SuppressWarnings("DataFlowIssue")
	public void registerAsItem() {
		if (this instanceof IMetaName)
			ItemInit.ITEMS.add(new ItemBlockVariants(this).setRegistryName(getRegistryName()));
		else
			ItemInit.ITEMS.add(new ItemBlockBase(this).setRegistryName(getRegistryName()));
	}

	@Override
	public void registerItemModels() {
		if (this instanceof IHasMultiModels)
			IHasMultiModels.registerMultiItemModels(this);
		else
			ModMain.proxy.registerSingleModel(Item.getItemFromBlock(this), 0, getInventoryItemStateName(0));
	}
	public String getInventoryItemStateName(int itemMeta) {
		IBlockState state = getStateFromMeta(itemMeta);
		return PropertyUtil.getPropertyString(state);
	}

	@Override
	public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
		if (this instanceof IHasMultiModels)
			for (int i = 0; i <= ((IHasMultiModels) this).metaMax(); i++) {
				items.add(new ItemStack(this, 1, i));
			}
		else
			super.getSubBlocks(itemIn, items);
	}

	//その他

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
