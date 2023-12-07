package kpan.ig_custom_stuff.gui;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.math.MathHelper;

public class GuiNumericField extends MyGuiTextField {

	private double pow = 1;//0の時は無制限
	private float value = 0;
	private float min;
	private float max;

	public GuiNumericField(int componentId, FontRenderer fontrendererObj, int x, int y, int width, int height) {
		super(componentId, fontrendererObj, x, y, width, height);
		min = applyDecimalPoint(-Float.MAX_VALUE);
		max = applyDecimalPoint(Float.MAX_VALUE);
	}
	public float getValue() { return value; }
	public void setValue(float value) {
		value = applyDecimalPoint(value);
		this.value = MathHelper.clamp(value, min, max);
		if (pow != 0 && pow <= 1)
			setText(Integer.toString((int) this.value));
		else
			setText(Float.toString(this.value));
	}
	public void setMin(float min) {
		this.min = applyDecimalPoint(min);
		setValue(value);
	}
	public void setMax(float max) {
		this.max = applyDecimalPoint(max);
		setValue(value);
	}
	public void setDecimalpoint(int decimalpoint) {
		if (decimalpoint == Integer.MIN_VALUE)
			pow = 0;
		else
			pow = Math.pow(10, -decimalpoint);
		setValue(value);
	}

	@Override
	protected boolean isValid(String s) {
		if (s.isEmpty()) {
			if (min <= 0 && 0 <= max)
				return true;
			else
				return false;
		}
		if (!super.isValid(s))
			return false;
		float val;
		try {
			val = Float.parseFloat(s);
		} catch (NumberFormatException e) {
			return false;
		}
		return min <= val && val <= max;
	}

	@Override
	public void setResponderEntryValue(int idIn, String textIn) {
		if (textIn.isEmpty()) {
			value = MathHelper.clamp(0, min, max);//isValidで大丈夫なはずだが一応
		} else {
			value = MathHelper.clamp(applyDecimalPoint(Float.parseFloat(textIn)), min, max);
		}
		if (guiResponder != null) {
			guiResponder.setEntryValue(idIn, textIn);
			guiResponder.setEntryValue(idIn, value);
		}
	}

	@Override
	protected boolean isAllowedCharacter(char character) {
		return character == '-' || character >= '0' && character <= '9' || character == '.';
	}

	private float applyDecimalPoint(float value) {
		if (pow == 0)
			return value;
		else
			return (float) (Math.floor(value * pow) / pow);
	}
}
