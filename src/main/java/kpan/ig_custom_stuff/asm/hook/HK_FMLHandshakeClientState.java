package kpan.ig_custom_stuff.asm.hook;

import kpan.ig_custom_stuff.asm.acc.ACC_RegistryData;
import kpan.ig_custom_stuff.registry.MCRegistryUtil;
import net.minecraftforge.fml.common.network.handshake.FMLHandshakeMessage;
import net.minecraftforge.fml.common.network.handshake.FMLHandshakeMessage.RegistryData;

public class HK_FMLHandshakeClientState {

	public static void onWAITINGSERVERCOMPLETE(FMLHandshakeMessage msg) {
		RegistryData pkt = (RegistryData) msg;
		ACC_RegistryData acc = (ACC_RegistryData) pkt;
		MCRegistryUtil.syncClientItemRegistryDedicated(acc.get_itemEntries(), acc.get_removedItems());
		MCRegistryUtil.syncClientBlockRegistryDedicated(acc.get_blockEntries(), acc.get_removedBlocks());
	}
}
