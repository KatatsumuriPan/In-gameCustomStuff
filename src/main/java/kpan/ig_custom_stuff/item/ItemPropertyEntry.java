package kpan.ig_custom_stuff.item;

import com.google.common.collect.BiMap;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import io.netty.buffer.ByteBuf;
import kpan.ig_custom_stuff.block.BlockPropertyEntry;
import kpan.ig_custom_stuff.util.MyByteBufUtil;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.JsonUtils;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

public class ItemPropertyEntry {
	public static final CreativeTabs DEFAULT_CREATIVE_TAB = CreativeTabs.MISC;
	public static final BiMap<String, CreativeTabs> ALL_CREATIVE_TABS = BlockPropertyEntry.ALL_CREATIVE_TABS;

	static {


	}


	public static ItemPropertyEntry fromByteBuf(ByteBuf buf) {
		CreativeTabs creativeTab = ALL_CREATIVE_TABS.get(MyByteBufUtil.readString(buf));
		return new ItemPropertyEntry(creativeTab);
	}

	public static ItemPropertyEntry defaultOption() { return new ItemPropertyEntry(); }

	public final CreativeTabs creativeTab;

	private ItemPropertyEntry() {
		this(DEFAULT_CREATIVE_TAB);
	}

	public ItemPropertyEntry(CreativeTabs creativeTab) {
		this.creativeTab = creativeTab;
	}

	public void writeTo(ByteBuf buf) {
		MyByteBufUtil.writeString(buf, ALL_CREATIVE_TABS.inverse().get(creativeTab));
	}

	public static ItemPropertyEntry deserialize(@Nullable JsonObject jsonObject, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		if (jsonObject == null)
			return defaultOption();
		CreativeTabs creativeTab = ALL_CREATIVE_TABS.getOrDefault(JsonUtils.getString(jsonObject, "creativeTab", ""), DEFAULT_CREATIVE_TAB);

		return new ItemPropertyEntry(creativeTab);
	}

	public JsonElement serialize(Type type, JsonSerializationContext context) {
		JsonObject basic_property = new JsonObject();
		basic_property.addProperty("creativeTab", ALL_CREATIVE_TABS.inverse().get(creativeTab));
		return basic_property;
	}

}
