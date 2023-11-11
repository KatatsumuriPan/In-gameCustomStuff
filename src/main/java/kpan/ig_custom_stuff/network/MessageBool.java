package kpan.ig_custom_stuff.network;

import io.netty.buffer.ByteBuf;

public abstract class MessageBool extends MessageBase {
	//デフォルトコンストラクタは必須
	public MessageBool() { }

	public boolean data;

	public MessageBool(boolean data) {
		this.data = data;
	}
	public MessageBool setData(boolean data) {
		this.data = data;
		return this;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		data = buf.readBoolean();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeBoolean(data);
	}

}
