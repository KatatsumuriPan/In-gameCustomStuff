package kpan.ig_custom_stuff.item;

import io.netty.buffer.ByteBuf;
import kpan.ig_custom_stuff.resource.DynamicResourceLoader;
import kpan.ig_custom_stuff.resource.DynamicResourceManager;
import kpan.ig_custom_stuff.resource.DynamicResourceManager.ClientCache;
import kpan.ig_custom_stuff.resource.DynamicResourceManager.Server;
import kpan.ig_custom_stuff.util.MyByteBufUtil;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;

public class ItemLangEntry {

	public static ItemLangEntry fromByteBuf(ByteBuf buf) {
		var usName = MyByteBufUtil.readString(buf);
		return new ItemLangEntry(usName);
	}

	public static ItemLangEntry defaultLang(String name) {
		return new ItemLangEntry(name);
	}

	public final String usName;

	public ItemLangEntry(String usName) {
		this.usName = usName;
	}

	public void writeTo(ByteBuf buf) {
		MyByteBufUtil.writeString(buf, usName);
	}

	public void register(ResourceLocation itemId, boolean isRemote) throws IOException {
		String translateKey = DynamicResourceManager.toTranslationKeyItem(itemId);
		if (isRemote) {
			ClientCache.INSTANCE.addLang(itemId.getNamespace(), "en_us", translateKey, usName);
			DynamicResourceLoader.putLang(translateKey, usName);
		} else {
			Server.INSTANCE.addLang(itemId.getNamespace(), "en_us", translateKey, usName);
		}
	}

	public void update(ResourceLocation itemId, boolean isRemote) throws IOException {
		String translateKey = DynamicResourceManager.toTranslationKeyItem(itemId);
		if (isRemote) {
			ClientCache.INSTANCE.addLang(itemId.getNamespace(), "en_us", translateKey, usName);
			DynamicResourceLoader.putLang(translateKey, usName);
		} else {
			Server.INSTANCE.addLang(itemId.getNamespace(), "en_us", translateKey, usName);
		}
	}

}
