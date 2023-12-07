package kpan.ig_custom_stuff.item;

import kpan.ig_custom_stuff.gui.GuiRegistryMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemRegistryBook extends ItemBase {
	public ItemRegistryBook(String name) {
		super(name, CreativeTabs.MISC);
	}


	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
		if (worldIn.isRemote)
			Client.onUse(playerIn, worldIn, handIn);
		return new ActionResult<>(EnumActionResult.SUCCESS, playerIn.getHeldItem(handIn));
	}
	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (worldIn.isRemote)
			Client.onUse(player, worldIn, hand);
		return EnumActionResult.SUCCESS;
	}

	private static class Client {

		public static void onUse(EntityPlayer player, World world, EnumHand hand) {
			Minecraft.getMinecraft().displayGuiScreen(new GuiRegistryMenu());
		}
	}
}
