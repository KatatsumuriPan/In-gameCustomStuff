package kpan.ig_custom_stuff.resource;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import io.netty.buffer.ByteBuf;
import kpan.ig_custom_stuff.util.MyByteBufUtil;
import net.minecraft.util.JsonUtils;
import org.apache.commons.lang3.Validate;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TextureAnimationEntry {

	public static TextureAnimationEntry fromByteBuf(ByteBuf buf) {
		int count = MyByteBufUtil.readVarInt(buf);
		List<TextureAnimationFrame> animationFrames = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			animationFrames.add(TextureAnimationFrame.fromByteBuf(buf));
		}
		int frameWidth = MyByteBufUtil.readVarInt(buf);
		int frameHeight = MyByteBufUtil.readVarInt(buf);
		int frameTime = MyByteBufUtil.readVarInt(buf);
		boolean interpolate = buf.readBoolean();
		return new TextureAnimationEntry(animationFrames, frameWidth, frameHeight, frameTime, interpolate);
	}
	public static TextureAnimationEntry fromJson(String json) throws JsonSyntaxException {
		return Serializer.GSON.fromJson(json, TextureAnimationEntry.class);
	}

	private final List<TextureAnimationFrame> animationFrames;
	private final int frameWidth;
	private final int frameHeight;
	private final int frameTime;
	private final boolean interpolate;

	public TextureAnimationEntry(List<TextureAnimationFrame> animationFramesIn, int frameWidthIn, int frameHeightIn, int frameTimeIn, boolean interpolateIn) {
		animationFrames = animationFramesIn;
		frameWidth = frameWidthIn;
		frameHeight = frameHeightIn;
		frameTime = frameTimeIn;
		interpolate = interpolateIn;
	}

	public int getFrameHeight() {
		return frameHeight;
	}

	public int getFrameWidth() {
		return frameWidth;
	}

	public int getFrameCount() {
		return animationFrames.size();
	}

	public int getFrameTime() {
		return frameTime;
	}

	public boolean isInterpolate() {
		return interpolate;
	}

	private TextureAnimationFrame getAnimationFrame(int frame) {
		return animationFrames.get(frame);
	}

	public int getFrameTimeSingle(int frame) {
		TextureAnimationFrame animationframe = getAnimationFrame(frame);
		return animationframe.hasNoTime() ? frameTime : animationframe.getFrameTime();
	}

	public boolean frameHasTime(int frame) {
		return !animationFrames.get(frame).hasNoTime();
	}

	public int getFrameIndex(int frame) {
		return animationFrames.get(frame).getFrameIndex();
	}

	public Set<Integer> getFrameIndexSet() {
		Set<Integer> set = Sets.newHashSet();

		for (TextureAnimationFrame animationframe : animationFrames) {
			set.add(animationframe.getFrameIndex());
		}

		return set;
	}

	public void writeTo(ByteBuf buf) {
		MyByteBufUtil.writeVarInt(buf, animationFrames.size());
		for (TextureAnimationFrame frame : animationFrames) {
			frame.writeTo(buf);
		}
		MyByteBufUtil.writeVarInt(buf, frameWidth);
		MyByteBufUtil.writeVarInt(buf, frameHeight);
		MyByteBufUtil.writeVarInt(buf, frameTime);
		buf.writeBoolean(interpolate);
	}

	public String toJson() {
		return Serializer.GSON.toJson(this);
	}


	public static class TextureAnimationFrame {

		public static TextureAnimationFrame fromByteBuf(ByteBuf buf) {
			int frameIndex = MyByteBufUtil.readVarInt(buf);
			int frameTime = MyByteBufUtil.readVarInt(buf);
			return new TextureAnimationFrame(frameIndex, frameTime);
		}

		private final int frameIndex;
		private final int frameTime;

		public TextureAnimationFrame(int frameIndexIn) {
			this(frameIndexIn, -1);
		}

		public TextureAnimationFrame(int frameIndexIn, int frameTimeIn) {
			frameIndex = frameIndexIn;
			frameTime = frameTimeIn;
		}

		public boolean hasNoTime() {
			return frameTime == -1;
		}

		public int getFrameTime() {
			return frameTime;
		}

		public int getFrameIndex() {
			return frameIndex;
		}

		public void writeTo(ByteBuf buf) {
			MyByteBufUtil.writeVarInt(buf, frameIndex);
			MyByteBufUtil.writeVarInt(buf, frameTime);
		}

	}

	public static class Serializer implements JsonDeserializer<TextureAnimationEntry>, JsonSerializer<TextureAnimationEntry> {

		private static final Gson GSON;

		static {
			GsonBuilder gsonbuilder = new GsonBuilder();
			gsonbuilder.registerTypeHierarchyAdapter(TextureAnimationEntry.class, new Serializer());
			GSON = gsonbuilder.create();
		}

		@Override
		public TextureAnimationEntry deserialize(JsonElement jsonElement, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			List<TextureAnimationFrame> list = Lists.newArrayList();
			JsonObject jsonObject = jsonElement.getAsJsonObject();
			JsonObject animation = JsonUtils.getJsonObject(jsonObject, "animation");
			int i = JsonUtils.getInt(animation, "frametime", 1);

			if (i != 1) {
				Validate.inclusiveBetween(1L, 2147483647L, i, "Invalid default frame time");
			}

			if (animation.has("frames")) {
				try {
					JsonArray jsonarray = JsonUtils.getJsonArray(animation, "frames");

					for (int j = 0; j < jsonarray.size(); ++j) {
						JsonElement jsonelement = jsonarray.get(j);
						TextureAnimationFrame animationframe = parseAnimationFrame(j, jsonelement);

						if (animationframe != null) {
							list.add(animationframe);
						}
					}
				} catch (ClassCastException classcastexception) {
					throw new JsonParseException("Invalid animation->frames: expected array, was " + animation.get("frames"), classcastexception);
				}
			}

			int k = JsonUtils.getInt(animation, "width", -1);
			int l = JsonUtils.getInt(animation, "height", -1);

			if (k != -1) {
				Validate.inclusiveBetween(1L, 2147483647L, k, "Invalid width");
			}

			if (l != -1) {
				Validate.inclusiveBetween(1L, 2147483647L, l, "Invalid height");
			}

			boolean flag = JsonUtils.getBoolean(animation, "interpolate", false);
			return new TextureAnimationEntry(list, k, l, i, flag);
		}

		private TextureAnimationFrame parseAnimationFrame(int frame, JsonElement element) {
			if (element.isJsonPrimitive()) {
				return new TextureAnimationFrame(JsonUtils.getInt(element, "frames[" + frame + "]"));
			} else if (element.isJsonObject()) {
				JsonObject jsonobject = JsonUtils.getJsonObject(element, "frames[" + frame + "]");
				int i = JsonUtils.getInt(jsonobject, "time", -1);

				if (jsonobject.has("time")) {
					Validate.inclusiveBetween(1L, 2147483647L, i, "Invalid frame time");
				}

				int j = JsonUtils.getInt(jsonobject, "index");
				Validate.inclusiveBetween(0L, 2147483647L, j, "Invalid frame index");
				return new TextureAnimationFrame(j, i);
			} else {
				return null;
			}
		}

		@Override
		public JsonElement serialize(TextureAnimationEntry object, Type typeOfT, JsonSerializationContext context) {
			JsonObject jsonobject = new JsonObject();
			JsonObject animation = new JsonObject();
			animation.addProperty("frametime", object.getFrameTime());

			if (object.getFrameWidth() != -1) {
				animation.addProperty("width", object.getFrameWidth());
			}
			if (object.getFrameHeight() != -1) {
				animation.addProperty("height", object.getFrameHeight());
			}
			if (object.getFrameCount() > 0) {
				JsonArray jsonarray = new JsonArray();

				for (int i = 0; i < object.getFrameCount(); ++i) {
					if (object.frameHasTime(i)) {
						JsonObject jsonobject1 = new JsonObject();
						jsonobject1.addProperty("index", object.getFrameIndex(i));
						jsonobject1.addProperty("time", object.getFrameTimeSingle(i));
						jsonarray.add(jsonobject1);
					} else {
						jsonarray.add(new JsonPrimitive(object.getFrameIndex(i)));
					}
				}

				animation.add("frames", jsonarray);
			}
			if (object.isInterpolate()) {
				animation.addProperty("interpolate", true);
			}

			jsonobject.add("animation", animation);
			return jsonobject;
		}

	}
}
