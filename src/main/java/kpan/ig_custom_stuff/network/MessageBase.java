package kpan.ig_custom_stuff.network;

import io.netty.buffer.ByteBuf;
import kpan.ig_custom_stuff.util.MyByteBufUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public abstract class MessageBase implements IMessage {
	//デフォルトコンストラクタは必須
	public MessageBase() { }

	//ClientOnlyを呼び出すとエラー出るので注意
	//ClientOnlyクラスのフィールドなどの呼び出しでもエラーになる
	//GuiScreen screen = Minecraft.getMinecraft().currentScreen;はなぜか大丈夫
	//少なくともMinecraft.getMinecraft().playerはアウトで.player.worldはセーフ
	//Minecraft.getMinecraft().worldはセーフ
	//Minecraft.getMinecraft().currentScreenはセーフ
	//Minecraft.getMinecraft().player.sendChatMessageはセーフ？
	public abstract void doAction(MessageContext ctx);

	protected static void writeVarInt(ByteBuf buf, int value) {
		MyByteBufUtil.writeVarInt(buf, value);
	}

	protected static void writeBlockPos(ByteBuf buf, BlockPos pos) {
		MyByteBufUtil.writeBlockPos(buf, pos);
	}
	protected static void writeVec3d(ByteBuf buf, Vec3d vec) {
		MyByteBufUtil.writeVec3d(buf, vec);
	}
	protected static void writeString(ByteBuf buf, String string) {
		MyByteBufUtil.writeString(buf, string);
	}

	protected static <E extends Enum<E>> void writeEnum(ByteBuf buf, Enum<E> enum1) {
		MyByteBufUtil.writeEnum(buf, enum1);
	}

	protected static void writeTextComponent(ByteBuf buf, ITextComponent component) {
		MyByteBufUtil.writeTextComponent(buf, component);
	}

	protected static int readVarInt(ByteBuf buf) {
		return MyByteBufUtil.readVarInt(buf);
	}

	protected static BlockPos readBlockPos(ByteBuf buf) {
		return MyByteBufUtil.readBlockPos(buf);
	}

	protected static Vec3d readVec3d(ByteBuf buf) {
		return MyByteBufUtil.readVec3d(buf);
	}

	protected static String readString(ByteBuf buf) {
		return MyByteBufUtil.readString(buf);
	}

	protected static <E extends Enum<E>> E readEnum(ByteBuf buf, Class<E> clazz) {
		return MyByteBufUtil.readEnum(buf, clazz);
	}

	public ITextComponent readTextComponent(ByteBuf buf) {
		return ITextComponent.Serializer.jsonToComponent(ByteBufUtils.readUTF8String(buf));
	}

}
