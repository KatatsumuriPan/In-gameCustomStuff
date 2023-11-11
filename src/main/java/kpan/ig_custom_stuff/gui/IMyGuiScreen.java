package kpan.ig_custom_stuff.gui;

public interface IMyGuiScreen {

	default void redisplay() { throw unsupported(); }

	static RuntimeException unsupported() {
		return new UnsupportedOperationException("使いまわさないでください");
	}

}
