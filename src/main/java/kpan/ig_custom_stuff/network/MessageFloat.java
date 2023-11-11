package kpan.ig_custom_stuff.network;

import io.netty.buffer.ByteBuf;

public abstract class MessageFloat extends MessageBase {
	//デフォルトコンストラクタは必須
	public MessageFloat() { }

	public float data;

	public MessageFloat(float data) {
		this.data = data;
	}
	public MessageFloat setData(float data) {
		this.data = data;
		return this;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		data = buf.readFloat();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeFloat(data);
	}

}
