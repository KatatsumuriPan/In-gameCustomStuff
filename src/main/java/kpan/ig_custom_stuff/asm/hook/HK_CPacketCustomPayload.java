package kpan.ig_custom_stuff.asm.hook;

import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;

public class HK_CPacketCustomPayload {
	public static int getMaxPacketSize() {
		return FMLProxyPacket.MAX_LENGTH;
	}
}
