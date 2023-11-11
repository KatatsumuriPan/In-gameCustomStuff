package kpan.ig_custom_stuff.block;

import io.netty.buffer.ByteBuf;
import kpan.ig_custom_stuff.resource.DynamicResourceLoader;
import kpan.ig_custom_stuff.resource.DynamicResourceManager;
import kpan.ig_custom_stuff.resource.DynamicResourceManager.ClientCache;
import kpan.ig_custom_stuff.resource.DynamicResourceManager.Server;
import kpan.ig_custom_stuff.util.MyByteBufUtil;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;

public class BlockLangEntry {

	public static BlockLangEntry fromByteBuf(ByteBuf buf) {
		var usName = MyByteBufUtil.readString(buf);
		return new BlockLangEntry(usName);
	}

	public static BlockLangEntry defaultLang(String name) {
		return new BlockLangEntry(name);
	}

	public static BlockLangEntry removedItem() { return new BlockLangEntry("REMOVED"); }
	public static BlockLangEntry removedBlock() { return new BlockLangEntry("REMOVED"); }

	public final String usName;

	public BlockLangEntry(String usName) {
		this.usName = usName;
	}

	public void writeTo(ByteBuf buf) {
		MyByteBufUtil.writeString(buf, usName);
	}

	public void register(ResourceLocation blockId, boolean isRemote) throws IOException {
		String translateKey = DynamicResourceManager.toTranslationKeyBlock(blockId);
		if (isRemote) {
			ClientCache.INSTANCE.addLang(blockId.getNamespace(), "en_us", translateKey, usName);
			DynamicResourceLoader.putLang(translateKey, usName);
		} else {
			Server.INSTANCE.addLang(blockId.getNamespace(), "en_us", translateKey, usName);
		}
	}

	public void update(ResourceLocation blockId, boolean isRemote) throws IOException {
		String translateKey = DynamicResourceManager.toTranslationKeyBlock(blockId);
		if (isRemote) {
			ClientCache.INSTANCE.addLang(blockId.getNamespace(), "en_us", translateKey, usName);
			DynamicResourceLoader.putLang(translateKey, usName);
		} else {
			Server.INSTANCE.addLang(blockId.getNamespace(), "en_us", translateKey, usName);
		}
	}

}
