package kpan.ig_custom_stuff.gui;

import kpan.ig_custom_stuff.util.BooleanConsumer;
import kpan.ig_custom_stuff.util.FloatConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.config.GuiButtonExt;
import net.minecraftforge.fml.client.config.GuiSlider;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.client.config.HoverChecker;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.Function;
import java.util.function.IntConsumer;

public class MyGuiList extends GuiListExtended {

	public final GuiScreen owningScreen;
	public final Minecraft mc;
	public List<IEntry> listEntries;
	public int maxLabelTextWidth = 0;
	public int maxEntryRightBound = 0;
	public int labelX;
	public int controlX;
	public int controlWidth;
	public int resetX;
	public int scrollBarX;
	private boolean isOverlaying = false;

	public MyGuiList(GuiScreen parent, Minecraft mc, int width, int height, int top, int bottom) {
		super(mc, width, height, top, bottom, 20);
		owningScreen = parent;
		setShowSelectionBox(false);
		this.mc = mc;
		listEntries = new ArrayList<>();
		controlWidth = width;
	}

	public void addBooleanButton(String translationKey, boolean beforeValue, BooleanConsumer onChanged) {
		listEntries.add(new BooleanEntry(this, translationKey, beforeValue, onChanged));
	}
	public void addIntegerButton(String translationKey, int beforeValue, int minValue, int maxValue, IntConsumer onChanged) {
		listEntries.add(new IntegerEntry(this, translationKey, beforeValue, minValue, maxValue, onChanged));
	}
	public void addFloatButton(String translationKey, float beforeValue, float minValue, float maxValue, FloatConsumer onChanged) {
		listEntries.add(new FloatEntry(this, translationKey, beforeValue, minValue, maxValue, onChanged));
	}
	public void addDoubleButton(String translationKey, double beforeValue, double minValue, double maxValue, DoubleConsumer onChanged) {
		listEntries.add(new DoubleEntry(this, translationKey, beforeValue, minValue, maxValue, onChanged));
	}
	public <T> void addValuesButton(String translationKey, T beforeValue, Function<T, String> toTranslationKey, Collection<T> values, Consumer<T> onChanged) {
		listEntries.add(new SelectValueEntry<>(this, translationKey, beforeValue, toTranslationKey, values, onChanged));
	}

	public void initGui() {

		maxLabelTextWidth = 0;
		for (IEntry entry : listEntries) {
			if (entry.getLabelWidth() > maxLabelTextWidth)
				maxLabelTextWidth = entry.getLabelWidth();
		}

		left = 0;
		right = width;
		int viewWidth = maxLabelTextWidth + 8 + (width / 2);
		labelX = (width / 2) - (viewWidth / 2);
		controlX = labelX + maxLabelTextWidth + 8;
		resetX = (width / 2) + (viewWidth / 2) - 45;

		maxEntryRightBound = 0;
		for (IEntry entry : listEntries) {
			if (entry.getEntryRightBound() > maxEntryRightBound)
				maxEntryRightBound = entry.getEntryRightBound();
		}

		scrollBarX = maxEntryRightBound + 5;
		controlWidth = maxEntryRightBound - controlX - 45;
	}

	public void keyTyped(char eventChar, int eventKey) {
		for (IEntry entry : listEntries) {
			entry.keyTyped(eventChar, eventKey);
		}
	}

	public void updateScreen() {
		for (IEntry entry : listEntries) {
			entry.updateCursorCounter();
		}
	}

	@Override
	public boolean mouseClicked(int mouseX, int mouseY, int mouseEvent) {
		for (IEntry entry : listEntries) {
			if (entry.mouseClicked(mouseX, mouseY, mouseEvent))
				return true;
		}
		return super.mouseClicked(mouseX, mouseY, mouseEvent);
	}

	@Override
	public void handleMouseInput() {
		super.handleMouseInput();
		for (IEntry entry : listEntries) {
			entry.handleMouseInput();
		}
	}
	public void drawScreenPost(int mouseX, int mouseY, float partialTicks) {
		if (!isOverlaying) {
			for (IEntry entry : listEntries) {
				entry.drawToolTip(mouseX, mouseY);
			}
		}
		for (IEntry entry : listEntries) {
			entry.drawPost(mouseX, mouseY, partialTicks);
		}
	}

	public boolean isOverlaying() {
		return isOverlaying;
	}

	@Override
	public int getSize() {
		return listEntries.size();
	}

	@Override
	public IEntry getListEntry(int index) {
		return listEntries.get(index);
	}

	@Override
	public int getScrollBarX() {
		return scrollBarX;
	}

	@Override
	public int getListWidth() {
		return width;
	}

	@Override
	protected boolean isSelected(int slotIndex) {
		return selectedElement == slotIndex;
	}

	public static class BooleanEntry extends ButtonEntry {
		protected final boolean beforeValue;
		protected boolean currentValue;
		protected final BooleanConsumer onChanged;

		private BooleanEntry(MyGuiList myGuiList, String tranlationKey, boolean beforeValue, BooleanConsumer onChanged) {
			super(myGuiList, tranlationKey);
			this.beforeValue = beforeValue;
			currentValue = beforeValue;
			this.onChanged = onChanged;
			btnValue.enabled = true;
			updateValueButtonText();
		}

		@Override
		public void updateValueButtonText() {
			btnValue.displayString = I18n.format(String.valueOf(currentValue));
			btnValue.packedFGColour = currentValue ? GuiUtils.getColorCode('2', true) : GuiUtils.getColorCode('4', true);
		}

		@Override
		public void valueButtonPressed(int slotIndex) {
			currentValue = !currentValue;
			onChanged.accept(currentValue);
		}

	}

	public static class SelectValueEntry<T> extends ButtonEntry {
		protected final Function<T, String> toTranslationKey;
		private final T[] values;
		private final List<GuiButton> buttonList = new ArrayList<>();
		private final Consumer<T> onChanged;
		protected T currentValue;
		private boolean showing = false;
		private int scroll = 0;
		private final int scrollMax;

		public SelectValueEntry(MyGuiList myGuiList, String tranlationKey, T currentValue, Function<T, String> toTranslationKey, Collection<T> values, Consumer<T> onChanged) {
			super(myGuiList, tranlationKey);
			this.currentValue = currentValue;
			this.toTranslationKey = toTranslationKey;
			this.values = (T[]) values.toArray(new Object[0]);
			this.onChanged = onChanged;
			updateValueButtonText();
			int y = 0;
			for (T value : values) {
				buttonList.add(new GuiButton(0, myGuiList.controlX, y, myGuiList.controlWidth, 20, I18n.format(this.toTranslationKey.apply(value))));
				y += 20;
			}
			scrollMax = Math.max(0, y - myGuiList.height + myGuiList.top);
		}

		@Override
		public void updateValueButtonText() {
			btnValue.displayString = I18n.format(toTranslationKey.apply(currentValue));
		}

		@Override
		public void valueButtonPressed(int slotIndex) {
			showing = true;
			myGuiList.isOverlaying = true;
		}

		@Override
		public boolean mouseClicked(int x, int y, int mouseEvent) {
			if (!showing)
				return super.mouseClicked(x, y, mouseEvent);
			showing = false;
			myGuiList.isOverlaying = false;
			for (int i = 0; i < buttonList.size(); i++) {
				GuiButton button = buttonList.get(i);
				if (button.mousePressed(mc, myGuiList.mouseX, myGuiList.mouseY)) {
					currentValue = values[i];
					onChanged.accept(currentValue);
					button.playPressSound(mc.getSoundHandler());
					updateValueButtonText();
				}
			}
			return true;
		}

		@Override
		public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partial) {
			super.drawEntry(slotIndex, x, y, listWidth, slotHeight, mouseX, mouseY, isSelected, partial);
		}

		@Override
		public void handleMouseInput() {
			super.handleMouseInput();
			if (showing) {
				scroll = MathHelper.clamp(scroll - Mouse.getEventDWheel() / 4, 0, scrollMax);
			}
		}
		@Override
		public void drawPost(int mouseX, int mouseY, float partialTicks) {
			if (showing) {
				Gui.drawRect(0, 0, myGuiList.owningScreen.width, myGuiList.owningScreen.height, 0x80000000);
				int buttonY = myGuiList.top;
				for (GuiButton button : buttonList) {
					button.width = myGuiList.controlWidth;
					button.x = myGuiList.controlX;
					button.y = buttonY - scroll;
					button.drawButton(mc, mouseX, mouseY, partialTicks);
					buttonY += 20;
				}
			}
		}
	}

	//TODO
	public static class ArrayEntry extends ButtonEntry {
		protected final Object[] beforeValues;
		protected Object[] currentValues;

		public ArrayEntry(MyGuiList myGuiList, String tranlationKey) {
			super(myGuiList, tranlationKey);
			beforeValues = new Object[0];
			currentValues = new Object[0];
			updateValueButtonText();
		}

		@Override
		public void updateValueButtonText() {
			btnValue.displayString = "";
			for (Object o : currentValues) {
				btnValue.displayString += ", [" + o + "]";
			}

			btnValue.displayString = btnValue.displayString.replaceFirst(", ", "");
		}

		@Override
		public void valueButtonPressed(int slotIndex) {
//			mc.displayGuiScreen(new GuiEditArray(owningScreen, configElement, slotIndex, currentValues, true));
		}

		public void setListFromChildScreen(Object[] newList) {
			if (!Arrays.deepEquals(currentValues, newList)) {
				currentValues = newList;
				updateValueButtonText();
			}
		}

	}

	public static class NumberSliderEntry extends ButtonEntry {
		protected final double beforeValue;
		protected final double minValue;
		protected final double maxValue;

		public NumberSliderEntry(MyGuiList myGuiList, String tranlationKey, double beforeValue, double minValue, double maxValue) {
			super(myGuiList, tranlationKey, new GuiSlider(0, myGuiList.controlX, 0, myGuiList.controlWidth, 18,
					"", "", minValue, maxValue, beforeValue, true, true));
			this.beforeValue = beforeValue;
			this.minValue = minValue;
			this.maxValue = maxValue;
		}

		@Override
		public void updateValueButtonText() {
			((GuiSlider) btnValue).updateSlider();
		}

		@Override
		public void valueButtonPressed(int slotIndex) { }

	}

	public static abstract class ButtonEntry extends ListEntryBase {
		protected final GuiButtonExt btnValue;

		public ButtonEntry(MyGuiList myGuiList, String tranlationKey) {
			this(myGuiList, tranlationKey, new GuiButtonExt(0, myGuiList.controlX, 0, myGuiList.controlWidth, 18,
					I18n.format(tranlationKey)));
		}

		public ButtonEntry(MyGuiList myGuiList, String tranlationKey, GuiButtonExt button) {
			super(myGuiList, tranlationKey);
			btnValue = button;
		}

		public abstract void updateValueButtonText();

		public abstract void valueButtonPressed(int slotIndex);

		@Override
		public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partial) {
			super.drawEntry(slotIndex, x, y, listWidth, slotHeight, mouseX, mouseY, isSelected, partial);
			btnValue.width = myGuiList.controlWidth;
			btnValue.x = myGuiList.controlX;
			btnValue.y = y;
			btnValue.drawButton(mc, mouseX, mouseY, partial);
		}

		@Override
		public boolean mousePressed(int index, int x, int y, int mouseEvent, int relativeX, int relativeY) {
			if (btnValue.mousePressed(mc, x, y)) {
				btnValue.playPressSound(mc.getSoundHandler());
				valueButtonPressed(index);
				updateValueButtonText();
				return true;
			} else
				return super.mousePressed(index, x, y, mouseEvent, relativeX, relativeY);
		}

		@Override
		public void mouseReleased(int index, int x, int y, int mouseEvent, int relativeX, int relativeY) {
			super.mouseReleased(index, x, y, mouseEvent, relativeX, relativeY);
			btnValue.mouseReleased(x, y);
		}

		@Override
		public void keyTyped(char eventChar, int eventKey) { }

		@Override
		public void updateCursorCounter() { }

	}

	public static class IntegerEntry extends StringEntry {
		protected final int beforeValue;
		protected final int minValue;
		protected final int maxValue;
		private final IntConsumer onChanged;

		public IntegerEntry(MyGuiList myGuiList, String tranlationKey, int beforeValue, int minValue, int maxValue, IntConsumer onChanged) {
			super(myGuiList, tranlationKey, String.valueOf(beforeValue));
			this.beforeValue = beforeValue;
			this.minValue = minValue;
			this.maxValue = maxValue;
			this.onChanged = onChanged;
		}

		@Override
		public void keyTyped(char eventChar, int eventKey) {
			String validChars = "0123456789";
			String before = textFieldValue.getText();
			if (validChars.contains(String.valueOf(eventChar))
					|| (!before.startsWith("-") && textFieldValue.getCursorPosition() == 0 && eventChar == '-')
					|| eventKey == Keyboard.KEY_BACK || eventKey == Keyboard.KEY_DELETE
					|| eventKey == Keyboard.KEY_LEFT || eventKey == Keyboard.KEY_RIGHT || eventKey == Keyboard.KEY_HOME || eventKey == Keyboard.KEY_END)
				textFieldValue.textboxKeyTyped((eventChar), eventKey);

			if (!textFieldValue.getText().trim().isEmpty() && !textFieldValue.getText().trim().equals("-")) {
				try {
					long value = Long.parseLong(textFieldValue.getText().trim());
					if (value < minValue || value > maxValue)
						isValidValue = false;
					else {
						isValidValue = true;
						onChanged.accept((int) value);
					}
				} catch (Throwable e) {
					isValidValue = false;
				}
			} else
				isValidValue = false;
		}

	}

	public static class FloatEntry extends StringEntry {
		protected final float beforeValue;
		protected final float minValue;
		protected final float maxValue;
		private final FloatConsumer onChanged;

		public FloatEntry(MyGuiList myGuiList, String tranlationKey, float beforeValue, float minValue, float maxValue, FloatConsumer onChanged) {
			super(myGuiList, tranlationKey, String.valueOf(beforeValue));
			this.beforeValue = beforeValue;
			this.minValue = minValue;
			this.maxValue = maxValue;
			this.onChanged = onChanged;
		}

		@Override
		public void keyTyped(char eventChar, int eventKey) {
			String validChars = "0123456789";
			String before = textFieldValue.getText();
			if (validChars.contains(String.valueOf(eventChar)) ||
					(!before.startsWith("-") && textFieldValue.getCursorPosition() == 0 && eventChar == '-')
					|| (!before.contains(".") && eventChar == '.')
					|| eventKey == Keyboard.KEY_BACK || eventKey == Keyboard.KEY_DELETE || eventKey == Keyboard.KEY_LEFT || eventKey == Keyboard.KEY_RIGHT
					|| eventKey == Keyboard.KEY_HOME || eventKey == Keyboard.KEY_END)
				textFieldValue.textboxKeyTyped((eventChar), eventKey);

			if (!textFieldValue.getText().trim().isEmpty() && !textFieldValue.getText().trim().equals("-")) {
				try {
					float value = Float.parseFloat(textFieldValue.getText().trim());
					if (value < minValue || value > maxValue)
						isValidValue = false;
					else {
						isValidValue = true;
						onChanged.accept(value);
					}
				} catch (Throwable e) {
					isValidValue = false;
				}
			} else
				isValidValue = false;
		}

	}

	public static class DoubleEntry extends StringEntry {
		protected final double beforeValue;
		protected final double minValue;
		protected final double maxValue;
		private final DoubleConsumer onChanged;

		public DoubleEntry(MyGuiList myGuiList, String tranlationKey, double beforeValue, double minValue, double maxValue, DoubleConsumer onChanged) {
			super(myGuiList, tranlationKey, String.valueOf(beforeValue));
			this.beforeValue = beforeValue;
			this.minValue = minValue;
			this.maxValue = maxValue;
			this.onChanged = onChanged;
		}

		@Override
		public void keyTyped(char eventChar, int eventKey) {
			String validChars = "0123456789";
			String before = textFieldValue.getText();
			if (validChars.contains(String.valueOf(eventChar)) ||
					(!before.startsWith("-") && textFieldValue.getCursorPosition() == 0 && eventChar == '-')
					|| (!before.contains(".") && eventChar == '.')
					|| eventKey == Keyboard.KEY_BACK || eventKey == Keyboard.KEY_DELETE || eventKey == Keyboard.KEY_LEFT || eventKey == Keyboard.KEY_RIGHT
					|| eventKey == Keyboard.KEY_HOME || eventKey == Keyboard.KEY_END)
				textFieldValue.textboxKeyTyped((eventChar), eventKey);

			if (!textFieldValue.getText().trim().isEmpty() && !textFieldValue.getText().trim().equals("-")) {
				try {
					double value = Double.parseDouble(textFieldValue.getText().trim());
					if (value < minValue || value > maxValue)
						isValidValue = false;
					else {
						isValidValue = true;
						onChanged.accept(value);
					}
				} catch (Throwable e) {
					isValidValue = false;
				}
			} else
				isValidValue = false;
		}

	}

	public static class StringEntry extends ListEntryBase {
		protected final GuiTextField textFieldValue;
		protected final String beforeValue;

		public StringEntry(MyGuiList myGuiList, String tranlationKey, String beforeValue) {
			super(myGuiList, tranlationKey);
			this.beforeValue = beforeValue;
			textFieldValue = new GuiTextField(10, mc.fontRenderer, myGuiList.controlX + 1, 0, myGuiList.controlWidth - 3, 16);
			textFieldValue.setMaxStringLength(10000);
			textFieldValue.setText(beforeValue);
		}

		@Override
		public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partial) {
			super.drawEntry(slotIndex, x, y, listWidth, slotHeight, mouseX, mouseY, isSelected, partial);
			textFieldValue.x = myGuiList.controlX + 2;
			textFieldValue.y = y + 1;
			textFieldValue.width = myGuiList.controlWidth - 4;
			textFieldValue.drawTextBox();
		}

		@Override
		public void keyTyped(char eventChar, int eventKey) {
			if (eventKey == Keyboard.KEY_LEFT || eventKey == Keyboard.KEY_RIGHT || eventKey == Keyboard.KEY_HOME || eventKey == Keyboard.KEY_END) {
				textFieldValue.textboxKeyTyped(eventChar, eventKey);

//				if (configElement.getValidationPattern() != null) {
//					if (configElement.getValidationPattern().matcher(textFieldValue.getText().trim()).matches())
//						isValidValue = true;
//					else
//						isValidValue = false;
//				}
			}
		}

		@Override
		public void updateCursorCounter() {
			textFieldValue.updateCursorCounter();
		}

		@Override
		public boolean mouseClicked(int x, int y, int mouseEvent) {
			textFieldValue.mouseClicked(x, y, mouseEvent);
			return false;
		}

	}

	public static abstract class ListEntryBase implements IEntry {
		protected final Minecraft mc;
		protected final MyGuiList myGuiList;
		protected final String name;
		protected List<String> toolTip;
		protected boolean isValidValue = true;
		protected HoverChecker tooltipHoverChecker;
		protected boolean drawLabel;

		public ListEntryBase(MyGuiList myGuiList, String translationKey) {
			this.myGuiList = myGuiList;
			mc = Minecraft.getMinecraft();
			name = I18n.format(translationKey);
			toolTip = new ArrayList<>();
			drawLabel = true;

			if (I18n.hasKey(translationKey + ".tooltip"))
				toolTip.add(I18n.format(translationKey + ".tooltip").replace("\\n", "\n"));
		}

		@Override
		public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partial) {

			if (drawLabel) {
				String label = (!isValidValue ? TextFormatting.RED.toString() : TextFormatting.WHITE.toString()) + name;
				mc.fontRenderer.drawString(
						label,
						myGuiList.labelX,
						y + slotHeight / 2 - mc.fontRenderer.FONT_HEIGHT / 2,
						16777215);
			}

			if (tooltipHoverChecker == null)
				tooltipHoverChecker = new HoverChecker(y, y + slotHeight, x, myGuiList.controlX - 8, 800);
			else
				tooltipHoverChecker.updateBounds(y, y + slotHeight, x, myGuiList.controlX - 8);
		}

		@Override
		public void drawToolTip(int mouseX, int mouseY) {
			boolean canHover = mouseY < myGuiList.bottom && mouseY > myGuiList.top;
			if (toolTip != null && tooltipHoverChecker != null) {
				if (tooltipHoverChecker.checkHover(mouseX, mouseY, canHover))
					GuiUtils.drawHoveringText(toolTip, mouseX, mouseY, myGuiList.width, myGuiList.height, 300, myGuiList.owningScreen.mc.fontRenderer);
			}
		}

		@Override
		public void drawPost(int mouseX, int mouseY, float partialTicks) { }
		@Override
		public boolean mousePressed(int index, int x, int y, int mouseEvent, int relativeX, int relativeY) {
			return false;
		}

		@Override
		public void mouseReleased(int index, int x, int y, int mouseEvent, int relativeX, int relativeY) {
		}

		@Override
		public void handleMouseInput() {

		}

		@Override
		public abstract void keyTyped(char eventChar, int eventKey);

		@Override
		public abstract void updateCursorCounter();

		@Override
		public boolean mouseClicked(int x, int y, int mouseEvent) {
			return false;
		}

		@Override
		public void updatePosition(int p_178011_1_, int p_178011_2_, int p_178011_3_, float partial) { }


		@Override
		public int getLabelWidth() {
			return mc.fontRenderer.getStringWidth(name);
		}

		@Override
		public int getEntryRightBound() {
			return myGuiList.resetX + 40;
		}

	}

	public interface IEntry extends GuiListExtended.IGuiListEntry {

		void keyTyped(char eventChar, int eventKey);

		void updateCursorCounter();

		boolean mouseClicked(int x, int y, int mouseEvent);

		void drawToolTip(int mouseX, int mouseY);

		void drawPost(int mouseX, int mouseY, float partialTicks);

		int getLabelWidth();

		int getEntryRightBound();

		void handleMouseInput();
	}
}
