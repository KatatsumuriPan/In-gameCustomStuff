package kpan.ig_custom_stuff.network;

import io.netty.buffer.ByteBuf;

public abstract class MessageSimple extends MessageBase {
	//デフォルトコンストラクタは必須
	public MessageSimple() { }

	@Override
	public void fromBytes(ByteBuf buf) { }

	@Override
	public void toBytes(ByteBuf buf) { }

}
