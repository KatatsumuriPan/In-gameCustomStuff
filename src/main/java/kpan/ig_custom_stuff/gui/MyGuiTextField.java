package kpan.ig_custom_stuff.gui;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiPageButtonList;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

@SideOnly(Side.CLIENT)
public class MyGuiTextField extends GuiTextField {
	protected final int id;
	protected final FontRenderer fontRenderer;
	protected String text = "";
	protected int maxStringLength = 32;
	protected int cursorCounter;
	protected boolean enableBackgroundDrawing;
	protected boolean canLoseFocus = true;
	protected boolean isFocused;
	protected boolean isEnabled = true;
	protected int lineScrollOffset;
	protected int cursorPosition;
	protected int selectionEnd;
	protected int enabledColor = 0xE0E0E0;
	protected int disabledColor = 0x707070;
	protected GuiPageButtonList.GuiResponder guiResponder;
	protected Predicate<String> validator = Predicates.<String>alwaysTrue();

	public MyGuiTextField(int componentId, FontRenderer fontRenderer, int x, int y, int width, int height) {
		super(componentId, fontRenderer, x, y, width, height);
		id = componentId;
		this.fontRenderer = fontRenderer;
		setEnableBackgroundDrawing(true);
	}

	@Override
	public void setGuiResponder(GuiPageButtonList.GuiResponder guiResponderIn) { guiResponder = guiResponderIn; }

	@Override
	public void updateCursorCounter() {
		++cursorCounter;
	}

	protected boolean isValid(String s) { return validator.apply(s); }

	@Override
	public void setText(String textIn) {
		if (isValid(textIn)) {
			if (textIn.length() > maxStringLength) {
				text = textIn.substring(0, maxStringLength);
			} else {
				text = textIn;
			}

			setCursorPositionEnd();
		}
	}

	@Override
	public String getText() { return text; }

	@Override
	public String getSelectedText() {
		int i = cursorPosition < selectionEnd ? cursorPosition : selectionEnd;
		int j = cursorPosition < selectionEnd ? selectionEnd : cursorPosition;
		return text.substring(i, j);
	}

	@Override
	public void setValidator(Predicate<String> theValidator) { validator = theValidator; }

	@Override
	public void writeText(String textToWrite) {
		StringBuilder sb = new StringBuilder();
		String input = ChatAllowedCharacters.filterAllowedCharacters(textToWrite);
		int left = cursorPosition < selectionEnd ? cursorPosition : selectionEnd;
		int right = cursorPosition < selectionEnd ? selectionEnd : cursorPosition;
		int room = maxStringLength - text.length() - (left - right);

		if (!text.isEmpty()) {
			sb.append(text.substring(0, left));
		}

		int actual_input_len;

		if (room < input.length()) {
			sb.append(input.substring(0, room));
			actual_input_len = room;
		} else {
			sb.append(input);
			actual_input_len = input.length();
		}

		if (!text.isEmpty() && right < text.length()) {
			sb.append(text.substring(right));
		}

		String new_text = sb.toString();
		if (isValid(new_text)) {
			text = new_text;
			moveCursorBy(left - selectionEnd + actual_input_len);
			setResponderEntryValue(id, text);
		}
	}

	@Override
	public void setResponderEntryValue(int idIn, String textIn) {
		if (guiResponder != null) {
			guiResponder.setEntryValue(idIn, textIn);
		}
	}

	@Override
	public void deleteWords(int num) {
		if (!text.isEmpty()) {
			if (selectionEnd != cursorPosition) {
				writeText("");
			} else {
				deleteFromCursor(getNthWordFromCursor(num) - cursorPosition);
			}
		}
	}

	@Override
	public void deleteFromCursor(int num) {
		if (!text.isEmpty()) {
			if (selectionEnd != cursorPosition) {
				writeText("");
			} else {
				boolean flag = num < 0;
				int i = flag ? cursorPosition + num : cursorPosition;
				int j = flag ? cursorPosition : cursorPosition + num;
				String s = "";

				if (i >= 0) {
					s = text.substring(0, i);
				}

				if (j < text.length()) {
					s = s + text.substring(j);
				}

				if (isValid(s)) {
					text = s;

					if (flag) {
						moveCursorBy(num);
					}

					setResponderEntryValue(id, text);
				}
			}
		}
	}

	@Override
	public int getId() { return id; }

	@Override
	public int getNthWordFromCursor(int numWords) {
		return getNthWordFromPos(numWords, getCursorPosition());
	}

	@Override
	public int getNthWordFromPos(int n, int pos) {
		return getNthWordFromPosWS(n, pos, true);
	}

	@Override
	public int getNthWordFromPosWS(int n, int pos, boolean skipWs) {
		String text = this.text;
		int i = pos;
		boolean flag = n < 0;
		int j = Math.abs(n);

		for (int k = 0; k < j; ++k) {
			if (!flag) {
				int l = text.length();
				i = text.indexOf(32, i);

				if (i == -1) {
					i = l;
				} else {
					while (skipWs && i < l && text.charAt(i) == ' ') {
						++i;
					}
				}
			} else {
				while (skipWs && i > 0 && text.charAt(i - 1) == ' ') {
					--i;
				}

				while (i > 0 && text.charAt(i - 1) != ' ') {
					--i;
				}
			}
		}

		return i;
	}

	@Override
	public void moveCursorBy(int num) {
		setCursorPosition(selectionEnd + num);
	}

	@Override
	public void setCursorPosition(int pos) {
		cursorPosition = pos;
		int i = text.length();
		cursorPosition = MathHelper.clamp(cursorPosition, 0, i);
		setSelectionPos(cursorPosition);
	}

	@Override
	public void setCursorPositionZero() {
		setCursorPosition(0);
	}

	@Override
	public void setCursorPositionEnd() {
		setCursorPosition(text.length());
	}

	@Override
	public boolean textboxKeyTyped(char typedChar, int keyCode) {
		if (!isFocused) {
			return false;
		} else if (GuiScreen.isKeyComboCtrlA(keyCode)) {
			setCursorPositionEnd();
			setSelectionPos(0);
			return true;
		} else if (GuiScreen.isKeyComboCtrlC(keyCode)) {
			GuiScreen.setClipboardString(getSelectedText());
			return true;
		} else if (GuiScreen.isKeyComboCtrlV(keyCode)) {
			if (isEnabled) {
				writeText(GuiScreen.getClipboardString());
			}

			return true;
		} else if (GuiScreen.isKeyComboCtrlX(keyCode)) {
			GuiScreen.setClipboardString(getSelectedText());

			if (isEnabled) {
				writeText("");
			}

			return true;
		} else {
			switch (keyCode) {
				case Keyboard.KEY_BACK:

					if (GuiScreen.isCtrlKeyDown()) {
						if (isEnabled) {
							deleteWords(-1);
						}
					} else if (isEnabled) {
						deleteFromCursor(-1);
					}

					return true;
				case Keyboard.KEY_HOME:

					if (GuiScreen.isShiftKeyDown()) {
						setSelectionPos(0);
					} else {
						setCursorPositionZero();
					}

					return true;
				case Keyboard.KEY_LEFT:

					if (GuiScreen.isShiftKeyDown()) {
						if (GuiScreen.isCtrlKeyDown()) {
							setSelectionPos(getNthWordFromPos(-1, getSelectionEnd()));
						} else {
							setSelectionPos(getSelectionEnd() - 1);
						}
					} else if (GuiScreen.isCtrlKeyDown()) {
						setCursorPosition(getNthWordFromCursor(-1));
					} else {
						moveCursorBy(-1);
					}

					return true;
				case Keyboard.KEY_RIGHT:

					if (GuiScreen.isShiftKeyDown()) {
						if (GuiScreen.isCtrlKeyDown()) {
							setSelectionPos(getNthWordFromPos(1, getSelectionEnd()));
						} else {
							setSelectionPos(getSelectionEnd() + 1);
						}
					} else if (GuiScreen.isCtrlKeyDown()) {
						setCursorPosition(getNthWordFromCursor(1));
					} else {
						moveCursorBy(1);
					}

					return true;
				case Keyboard.KEY_END:

					if (GuiScreen.isShiftKeyDown()) {
						setSelectionPos(text.length());
					} else {
						setCursorPositionEnd();
					}

					return true;
				case Keyboard.KEY_DELETE:

					if (GuiScreen.isCtrlKeyDown()) {
						if (isEnabled) {
							deleteWords(1);
						}
					} else if (isEnabled) {
						deleteFromCursor(1);
					}

					return true;
				default:

					if (isAllowedCharacter(typedChar)) {
						if (isEnabled) {
							writeText(Character.toString(typedChar));
						}

						return true;
					} else {
						return false;
					}
			}
		}
	}

	protected boolean isAllowedCharacter(char typedChar) {
		return ChatAllowedCharacters.isAllowedCharacter(typedChar);
	}

	@Override
	public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
		boolean flag = mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;

		if (canLoseFocus) {
			setFocused(flag);
		}

		if (isFocused && flag && mouseButton == 0) {
			int i = mouseX - x;

			if (enableBackgroundDrawing) {
				i -= 4;
			}

			String s = fontRenderer.trimStringToWidth(text.substring(lineScrollOffset), getWidth());
			setCursorPosition(fontRenderer.trimStringToWidth(s, i).length() + lineScrollOffset);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void drawTextBox() {
		if (getVisible()) {
			if (getEnableBackgroundDrawing()) {
				drawRect(x - 1, y - 1, x + width + 1, y + height + 1, 0xFFA0A0A0);
				drawRect(x, y, x + width, y + height, 0xFF000000);
			}

			int i = isEnabled ? enabledColor : disabledColor;
			int j = cursorPosition - lineScrollOffset;
			int k = selectionEnd - lineScrollOffset;
			String s = fontRenderer.trimStringToWidth(text.substring(lineScrollOffset), getWidth());
			boolean flag = j >= 0 && j <= s.length();
			boolean flag1 = isFocused && cursorCounter / 6 % 2 == 0 && flag;
			int l = enableBackgroundDrawing ? x + 4 : x;
			int i1 = enableBackgroundDrawing ? y + (height - 8) / 2 : y;
			int j1 = l;

			if (k > s.length()) {
				k = s.length();
			}

			if (!s.isEmpty()) {
				String s1 = flag ? s.substring(0, j) : s;
				j1 = fontRenderer.drawStringWithShadow(s1, l, i1, i);
			}

			boolean flag2 = cursorPosition < text.length() || text.length() >= getMaxStringLength();
			int k1 = j1;

			if (!flag) {
				k1 = j > 0 ? l + width : l;
			} else if (flag2) {
				k1 = j1 - 1;
				--j1;
			}

			if (!s.isEmpty() && flag && j < s.length()) {
				j1 = fontRenderer.drawStringWithShadow(s.substring(j), j1, i1, i);
			}

			if (flag1) {
				if (flag2) {
					Gui.drawRect(k1, i1 - 1, k1 + 1, i1 + 1 + fontRenderer.FONT_HEIGHT, 0xFFD0D0D0);
				} else {
					fontRenderer.drawStringWithShadow("_", k1, i1, i);
				}
			}

			if (k != j) {
				float l1 = l + fontRenderer.getStringWidth(s.substring(0, k));
				drawSelectionBox(k1, i1 - 1, l1 - 1, i1 + 1 + fontRenderer.FONT_HEIGHT);
			}
		}
	}

	protected void drawSelectionBox(float startX, int startY, float endX, int endY) {
		if (startX < endX) {
			float i = startX;
			startX = endX;
			endX = i;
		}

		if (startY < endY) {
			int j = startY;
			startY = endY;
			endY = j;
		}

		if (endX > x + width) {
			endX = x + width;
		}

		if (startX > x + width) {
			startX = x + width;
		}

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		GlStateManager.color(0.0F, 0.0F, 255.0F, 255.0F);
		GlStateManager.disableTexture2D();
		GlStateManager.enableColorLogic();
		GlStateManager.colorLogicOp(GlStateManager.LogicOp.OR_REVERSE);
		bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
		bufferbuilder.pos(startX, endY, 0.0D).endVertex();
		bufferbuilder.pos(endX, endY, 0.0D).endVertex();
		bufferbuilder.pos(endX, startY, 0.0D).endVertex();
		bufferbuilder.pos(startX, startY, 0.0D).endVertex();
		tessellator.draw();
		GlStateManager.disableColorLogic();
		GlStateManager.enableTexture2D();
	}

	@Override
	public void setMaxStringLength(int length) {
		maxStringLength = length;

		if (text.length() > length) {
			text = text.substring(0, length);
		}
	}

	@Override
	public int getMaxStringLength() { return maxStringLength; }

	@Override
	public int getCursorPosition() { return cursorPosition; }

	@Override
	public boolean getEnableBackgroundDrawing() { return enableBackgroundDrawing; }

	@Override
	public void setEnableBackgroundDrawing(boolean enableBackgroundDrawingIn) { enableBackgroundDrawing = enableBackgroundDrawingIn; }

	@Override
	public void setTextColor(int color) { enabledColor = color; }

	@Override
	public void setDisabledTextColour(int color) { disabledColor = color; }

	@Override
	public void setFocused(boolean isFocusedIn) {
		if (isFocusedIn && !isFocused) {
			cursorCounter = 0;
		}

		isFocused = isFocusedIn;

		if (Minecraft.getMinecraft().currentScreen != null) {
			Minecraft.getMinecraft().currentScreen.setFocused(isFocusedIn);
		}
	}

	@Override
	public boolean isFocused() { return isFocused; }

	@Override
	public void setEnabled(boolean enabled) { isEnabled = enabled; }

	@Override
	public int getSelectionEnd() { return selectionEnd; }

	@Override
	public int getWidth() { return getEnableBackgroundDrawing() ? width - 8 : width; }

	@Override
	public void setSelectionPos(int position) {
		int i = text.length();

		if (position > i) {
			position = i;
		}

		if (position < 0) {
			position = 0;
		}

		selectionEnd = position;

		if (fontRenderer != null) {
			if (lineScrollOffset > i) {
				lineScrollOffset = i;
			}

			int j = getWidth();
			String s = fontRenderer.trimStringToWidth(text.substring(lineScrollOffset), j);
			int k = s.length() + lineScrollOffset;

			if (position == lineScrollOffset) {
				lineScrollOffset -= fontRenderer.trimStringToWidth(text, j, true).length();
			}

			if (position > k) {
				lineScrollOffset += position - k;
			} else if (position <= lineScrollOffset) {
				lineScrollOffset -= lineScrollOffset - position;
			}

			lineScrollOffset = MathHelper.clamp(lineScrollOffset, 0, i);
		}
	}

	@Override
	public void setCanLoseFocus(boolean canLoseFocusIn) { canLoseFocus = canLoseFocusIn; }

}
