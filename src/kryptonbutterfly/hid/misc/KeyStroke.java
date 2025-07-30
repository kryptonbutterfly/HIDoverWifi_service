package kryptonbutterfly.hid.misc;

import static java.awt.event.KeyEvent.*;

public enum KeyStroke
{
	TAB(VK_TAB),
	CTRL(VK_CONTROL),
	SHIFT(VK_SHIFT),
	ALT(VK_ALT),
	SUPER(VK_WINDOWS),
	ALTGR(VK_ALT_GRAPH),
	PAGE_DOWN(VK_PAGE_DOWN),
	PAGE_UP(VK_PAGE_UP),
	DOWN(VK_DOWN),
	UP(VK_UP),
	LEFT(VK_LEFT),
	RIGHT(VK_RIGHT),
	POS1(VK_HOME),
	END(VK_END),
	INSERT(VK_INSERT),
	DEL(VK_DELETE),
	
	ESC(VK_ESCAPE),
	F1(VK_F1),
	F2(VK_F2),
	F3(VK_F3),
	F4(VK_F4),
	F5(VK_F5),
	F6(VK_F6),
	F7(VK_F7),
	F8(VK_F8),
	F9(VK_F9),
	F10(VK_F10),
	F11(VK_F11),
	F12(VK_F12),
	
	CIRCUMFLEX(VK_BACK_QUOTE),
	DIG0(VK_0),
	DIG1(VK_1),
	DIG2(VK_2),
	DIG3(VK_3),
	DIG4(VK_4),
	DIG5(VK_5),
	DIG6(VK_6),
	DIG7(VK_7),
	DIG8(VK_8),
	DIG9(VK_9),
	SHARP_S(VK_BACK_SLASH),
	ACUTE_ACCENT(VK_DEAD_GRAVE),
	
	Q(VK_Q),
	W(VK_W),
	E(VK_E),
	R(VK_R),
	T(VK_T),
	Y(VK_Y),
	U(VK_U),
	I(VK_I),
	O(VK_O),
	P(VK_P),
	Ü(VK_DEAD_DIAERESIS),
	PLUS(VK_PLUS),
	HASH(VK_DEAD_BREVE),
	
	A(VK_A),
	S(VK_S),
	D(VK_D),
	F(VK_F),
	G(VK_G),
	H(VK_H),
	J(VK_J),
	K(VK_K),
	L(VK_L),
	Ö(VK_DEAD_DOUBLEACUTE),
	Ä(VK_DEAD_CARON),
	
	LESS(VK_LESS),
	Z(VK_Z),
	X(VK_X),
	C(VK_C),
	V(VK_V),
	B(VK_B),
	N(VK_N),
	M(VK_M),
	COMMA(VK_COMMA),
	DOT(VK_PERIOD),
	MINUS(VK_MINUS),
	
	SPACE(VK_SPACE),
	ENTER(VK_ENTER),
	BACK_SPACE(VK_BACK_SPACE);
	
	public final int key;
	
	KeyStroke(int key)
	{
		this.key = key;
	}
	
	public static KeyStroke getKey(String key)
	{
		try
		{
			return KeyStroke.valueOf(key);
		}
		catch (IllegalArgumentException e)
		{
			System.err.printf("Unexpected Key: '%s'\n", key);
			return null;
		}
	}
}
