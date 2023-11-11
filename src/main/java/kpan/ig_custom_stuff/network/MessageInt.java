package kpan.ig_custom_stuff.network;

import io.netty.buffer.ByteBuf;

public abstract class MessageInt extends MessageBase {
	//デフォルトコンストラクタは必須
	public MessageInt() { }

	public int data;

	public MessageInt(int data) {
		this.data = data;
	}
	public MessageInt setData(int data) {
		this.data = data;
		return this;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		data = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(data);
	}

}
