package kpan.ig_custom_stuff.asm.hook;

import kpan.ig_custom_stuff.asm.acc.ACC_RegistryData;
import kpan.ig_custom_stuff.registry.DynamicServerRegistryManager;
import kpan.ig_custom_stuff.registry.MCRegistryUtil;
import net.minecraftforge.fml.common.network.handshake.FMLHandshakeMessage.RegistryData;

import java.util.ArrayList;

public class HK_FMLHandshakeServerState {

	public static void onMessageConstruct(RegistryData registryData) {
		((ACC_RegistryData) registryData).set_itemEntries(new ArrayList<>(DynamicServerRegistryManager.getItemEntries()));
		((ACC_RegistryData) registryData).set_blockEntries(new ArrayList<>(DynamicServerRegistryManager.getBlockEntries()));
		((ACC_RegistryData) registryData).set_removedItems(new ArrayList<>(MCRegistryUtil.getRemovedItemIds()));
		((ACC_RegistryData) registryData).set_removedBlocks(new ArrayList<>(MCRegistryUtil.getRemovedBlockIds()));
	}
}
