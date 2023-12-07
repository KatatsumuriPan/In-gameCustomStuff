package kpan.ig_custom_stuff.util;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class MyByteBufUtil {

	public static void writeVarInt(ByteBuf buf, int value) {
		ByteBufUtils.writeVarInt(buf, value, 5);
	}

	public static void writeVarIntAsShort(ByteBuf buf, int value) {
		//15bit目が延長フラグ
		while (true) {
			if ((value & ~0x7fff) == 0) {
				buf.writeShort(value);
				break;
			} else {
				buf.writeShort(value & 0x7fff | 0x8000);
				value >>>= 15;
			}
		}
	}

	public static void writeBlockPos(ByteBuf buf, BlockPos pos) {
		buf.writeLong(pos.toLong());
	}

	public static void writeBlockState(ByteBuf buf, IBlockState state, boolean onlyMeta) {
		Block block = state.getBlock();
		ResourceLocation resourcelocation = Block.REGISTRY.getNameForObject(block);
		writeString(buf, resourcelocation.toString());
		buf.writeBoolean(onlyMeta);
		if (onlyMeta) {
			buf.writeByte(block.getMetaFromState(state));
		} else {
			writeString(buf, PropertyUtil.getPropertyString(state));
		}
	}

	public static void writeVec3d(ByteBuf buf, Vec3d vec) {
		buf.writeDouble(vec.x);
		buf.writeDouble(vec.y);
		buf.writeDouble(vec.z);
	}

	public static void writeTextComponent(ByteBuf buf, ITextComponent component) {
		writeString(buf, ITextComponent.Serializer.componentToJson(component));
	}

	public static void writeString(ByteBuf buf, String string) {
		if (string.length() >= 0x800) {
			buf.writeBoolean(true);
			try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
				var gzip = new GZIPOutputStream(stream);
				gzip.write(string.getBytes(StandardCharsets.UTF_8));
				gzip.close();//ByteArrayOutputStreamは閉じても影響なし
				byte[] compressed = stream.toByteArray();
				writeVarInt(buf, compressed.length);
				buf.writeBytes(compressed);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} else {
			buf.writeBoolean(false);
			ByteBufUtils.writeUTF8String(buf, string);
		}
	}

	public static <E extends Enum<E>> void writeEnum(ByteBuf buf, Enum<E> enum1) {
		writeVarInt(buf, enum1.ordinal());
	}

	public static int readVarInt(ByteBuf buf) {
		return ByteBufUtils.readVarInt(buf, 5);
	}

	public static int readVarIntAsShort(ByteBuf buf) {
		int value = 0;
		for (int exp = 0; ; exp++) {
			short v = buf.readShort();
			value |= (v & 0x7fff) << exp * 15;
			if ((v & 0x8000) == 0)
				break;
		}
		return value;
	}

	public static BlockPos readBlockPos(ByteBuf buf) {
		return BlockPos.fromLong(buf.readLong());
	}

	//	@SuppressWarnings("deprecation")
	//	public static IBlockState readBlockState(ByteBuf buf) {
	//		Block block = Block.getBlockFromName(ByteBufUtils.readUTF8String(buf));
	//		if (buf.readBoolean()) {
	//			int meta = buf.readByte();
	//			return block.getStateFromMeta(meta);
	//		} else {
	//			String properties = ByteBufUtils.readUTF8String(buf);
	//			return PropertyUtil.withStringPropeties(block.getDefaultState(), properties);
	//		}
	//	}

	public static Vec3d readVec3d(ByteBuf buf) {
		return new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
	}

	public static ITextComponent readTextComponent(ByteBuf buf) {
		return ITextComponent.Serializer.jsonToComponent(readString(buf));
	}

	public static String readString(ByteBuf buf) {
		if (buf.readBoolean()) {
			int length = readVarInt(buf);
			byte[] compressed = new byte[length];
			buf.readBytes(compressed);
			try (GZIPInputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(compressed))) {
				byte[] decompressed = IOUtils.toByteArray(gzipInputStream);
				return new String(decompressed, StandardCharsets.UTF_8);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} else {
			return ByteBufUtils.readUTF8String(buf);
		}
	}

	public static <E extends Enum<E>> E readEnum(ByteBuf buf, Class<E> clazz) {
		return clazz.getEnumConstants()[readVarInt(buf)];
	}

}
