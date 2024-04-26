package base;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;

/**
 * {@linkplain #expendKeys(String[], String[], String[])} for testing multiple
 * keys/keycombinations at once
 * <p>
 * {@linkplain #consumeKey(String)} for using the key, until it is lifted and
 * used again
 * <p>
 * {@linkplain #useKeyForTick(String)} for using the key for one tick, making it
 * available again after the tick
 */
public class KeyHandler implements KeyListener {

	public static ArrayList<String> CURRENTLY_PRESSED = new ArrayList<>();
	public static ArrayList<String> CONSUMED = new ArrayList<>();
	public static ArrayList<String> USEDFORTICK = new ArrayList<>();

	/**
	 * Handle KeyEvents
	 */

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
		addInputString(keyToString(e));
	}

	public static void mousePressed(MouseEvent e) {
		int inputType = e.getButton();
		String inputString;
		switch (inputType) {
			case 1:
				inputString = "LEFTCLICK";
				break;
			case 2:
				inputString = "MIDDLECLICK";
				break;
			case 3:
				inputString = "RIGHTCLICK";
				break;
			default:
				inputString = "";
		}
		addInputString(inputString);
	}

	public static void mouseScrolled(MouseWheelEvent e) {
		String inputString;
		inputString = e.getPreciseWheelRotation() < 0 ? "SCROLLUP" : "SCROLLDOWN";
		addInputString(inputString);
	}

	@Override
	public void keyReleased(KeyEvent e) {
		removeInputString(keyToString(e));
	}

	public static void mouseReleased(MouseEvent e) {
		int inputType = e.getButton();
		String inputString;
		switch (inputType) {
			case 1:
				inputString = "LEFTCLICK";
				break;
			case 2:
				inputString = "MIDDLECLICK";
				break;
			case 3:
				inputString = "RIGHTCLICK";
				break;
			default:
				inputString = "";
		}
		removeInputString(inputString);
	}

	/**
	 * 
	 * 
	 * 
	 * 
	 */

	/**
	 * Public methods to use for checking and using inputs
	 */

	/**
	 * Used to remove "input" from CURRENTLY_PRESSED and add it to CONSUMED
	 * 
	 * @return Whether it was able to consume the input or not.
	 */
	public static boolean consumeKey(String input) {

		if (input == null)
			return false;

		boolean pressed = false;
		ArrayList<String> tempPressedList = new ArrayList<>(CURRENTLY_PRESSED);
		for (String s : tempPressedList) {
			if (s.equals(input)) {
				CURRENTLY_PRESSED.remove(s);
				pressed = true;
				if (!input.equals("SCROLLDOWN") && !input.equals("SCROLLUP"))
					CONSUMED.add(input);
			}
		}
		return pressed;
	}

	/**
	 * Powerful tool, to efficiently consume/use Inputs, and Input-Combinations (->
	 * "STRG" + "C").
	 * Uses mainKeys and controlKeys with same length and pairs entries via index.
	 * 
	 * @note Example:
	 * 
	 * 
	 *       expendKeys({"C", "V"},{"STRG", "STRG"}, {"C", "U"});
	 * 
	 *       -> Consumes "C" if "STRG + C" is pressed and uses "V" if "STRG + V" is
	 *       pressed.
	 * 
	 * @param mainKeys    Keys which are consumed/used if related controlKey is
	 *                    pressed.
	 * @param controlKeys Keys which have to be pressed to consume/use related
	 *                    mainKey.
	 * @param modes       String array containing "C" for consume and "U" for use
	 *                    for each key pair.
	 * @return int[] containing the indexes of the keys which were consumed/used.
	 */
	public static int[] expendKeys(String[] mainKeys, String[] controlKeys, String[] modes) {
		ArrayList<Integer> result = new ArrayList<>();

		if(modes == null || modes.length == 0)
			throw new IllegalArgumentException("Modes can't be null or empty");

		if(controlKeys == null){
			controlKeys = new String[mainKeys.length];
			for(int i = 0; i < mainKeys.length; i++){
				controlKeys[i] = "";
			}
		}

		if(modes.length != mainKeys.length){
			String[] newModes = new String[mainKeys.length];
			for(int i = 0; i < modes.length; i++){
				newModes[i] = modes[i];
			}
			for(int i = modes.length; i < newModes.length; i++){
				newModes[i] = modes[modes.length - 1];
			}
			modes = newModes;
		}

		for (int i = 0; i < mainKeys.length; i++) {
			if (modes[i].equals("C")) {
				if (keyCurrentlyPressed(controlKeys[i]) && consumeKey(mainKeys[i])) {
					result.add(i);
				}
			} else if (modes[i].equals("U")) {
				if (keyCurrentlyPressed(controlKeys[i]) && useKeyForTick(mainKeys[i])) {
					result.add(i);
				}
			}
		}
		// Convert ArrayList<Integer> to int[]
		return result.stream().mapToInt(i -> i).toArray();
	}

	/**
	 * Used to remove "input" from CURRENTLY_PRESSED and add it to USEDFORTICK.
	 * Every tick the USEDFORTICK inputs will be added to CURRENTL_PRESSED as long
	 * as they are still pressed.
	 * 
	 * @return Whether it was able to use the input or not.
	 */
	public static boolean useKeyForTick(String input) {
		boolean pressed = false;
		ArrayList<String> tempPressedList = new ArrayList<>(CURRENTLY_PRESSED);
		for (String s : tempPressedList) {
			if (s.equals(input)) {
				CURRENTLY_PRESSED.remove(s);
				pressed = true;
				USEDFORTICK.add(input);
			}
		}
		return pressed;
	}

	public static boolean keyCurrentlyPressed(String key) {
		if (key.equals(""))
			return true;
		boolean result = false;
		for (String s : CURRENTLY_PRESSED) {
			if (s.equals(key))
				result = true;
		}
		return result;
	}

	/** @return All Strings from keys which CURRENTLY_PRESSED contains. */
	public static String[] keysCurrentlyPressed(String[] keys) {
		ArrayList<String> result = new ArrayList<>();
		for (String s : CURRENTLY_PRESSED) {
			for (String k : keys) {
				if (s.equals(k))
					result.add(s);
			}
		}
		return result.toArray(new String[result.size()]);
	}

	public static boolean keyCurrentlyConsumed(String key) {
		boolean result = false;
		for (String s : CONSUMED) {
			if (s.equals(key))
				result = true;
		}
		return result;
	}

	/** @return All Strings from keys which CONSUMED contains. */
	public static String[] keysCurrentlyConsumed(String[] keys) {
		ArrayList<String> result = new ArrayList<>();
		for (String s : CONSUMED) {
			for (String k : keys) {
				if (s.equals(k))
					result.add(s);
			}
		}
		return result.toArray(new String[result.size()]);
	}

	public static boolean keyCurrentlyUsed(String key) {
		boolean result = false;
		for (String s : USEDFORTICK) {
			if (s.equals(key))
				result = true;
		}
		return result;
	}

	/** @return All Strings from keys which USEDFORTICK contains. */
	public static String[] keysCurrentlyUsed(String[] keys) {
		ArrayList<String> result = new ArrayList<>();
		for (String s : USEDFORTICK) {
			for (String k : keys) {
				if (s.equals(k))
					result.add(s);
			}
		}
		return result.toArray(new String[result.size()]);
	}

	/**
	 * 
	 * 
	 * 
	 * 
	 */

	/**
	 * Intern handling of KeyEvent Strings
	 */

	private static void addInputString(String input) {
		if (input.equals("") || input == null)
			return;
		Boolean alreadyPressed = false;
		for (String str : CURRENTLY_PRESSED) {
			if (str.equals(input))
				alreadyPressed = true;
		}
		for (String str : CONSUMED) {
			if (str.equals(input))
				alreadyPressed = true;
		}
		for (String str : USEDFORTICK) {
			if (str.equals(input))
				alreadyPressed = true;
		}
		if (!alreadyPressed) {
			CURRENTLY_PRESSED.add(input);
		}
		/* System.out.println("CP, C, U");
		System.out.println(CURRENTLY_PRESSED);

		System.out.println(CONSUMED);
		System.out.println(USEDFORTICK); */

	}

	/**
	 * Used when key isn't pressed anymore. Removes "input" from CONSUMED,
	 * USEDFORTICK and
	 * CURRENTLY_PRESSED.
	 */
	private static void removeInputString(String input) {
		if (input.equals(""))
			return;
		ArrayList<String> tempPressedList = new ArrayList<>(CURRENTLY_PRESSED);
		ArrayList<String> tempUsedList = new ArrayList<>(CONSUMED);
		ArrayList<String> tempUsedForTickList = new ArrayList<>(USEDFORTICK);

		for (String s : tempPressedList) {
			String str = s;
			if(str == null)
				continue;
			if (str.equals(input))
				CURRENTLY_PRESSED.remove(str);
		}
		for (String str : tempUsedList) {
			if (str.equals(input))
				CONSUMED.remove(str);
		}
		for (String str : tempUsedForTickList) {
			if (str.equals(input))
				USEDFORTICK.remove(str);
		}
		/*
		 * CURRENTLY_PRESSED = tempPressedList;
		 * CONSUMED = tempUsedList;
		 * USEDFORTICK = tempUsedForTickList;
		 */
	}

	private static String keyToString(KeyEvent e) {
		String inputString = KeyEvent.getKeyText(e.getExtendedKeyCode());
		if (inputString.length() > 1) {
			int keyCode = e.getKeyCode();
			inputString = switch (keyCode) {
				case 27 -> "ESCAPE";
				case 130 -> "CIRCUMFLEX";
				case 20 -> "CAPSLOCK";
				case 16 -> "SHIFT";
				case 17 -> "CONTROL";
				case 129 -> "AKUT";
				case 153 -> "LESSTHAN";
				case 18 -> "ALT";
				case 32 -> "SPACE";
				case 525 -> "MENU";
				case 8 -> "BACKSPACE";
				case 10 -> "ENTER";
				case 521 -> "+";
				case 520 -> "#";
				case 45 -> "-";
				case 46 -> ".";
				case 44 -> ",";
				case 155 -> "INSERT";
				case 127 -> "DELETE";
				case 36 -> "POS1";
				case 35 -> "END";
				case 33 -> "PAGEUP";
				case 34 -> "PAGEDOWN";
				case 144 -> "NUM";
				case 111 -> "/";
				case 106 -> "*";
				case 109 -> "NUM-";
				case 107 -> "NUM+";
				case 110 -> "NUM,";
				case 96 -> "NUM0";
				case 97 -> "NUM1";
				case 98 -> "NUM2";
				case 99 -> "NUM3";
				case 100 -> "NUM4";
				case 101 -> "NUM5";
				case 102 -> "NUM6";
				case 103 -> "NUM7";
				case 104 -> "NUM8";
				case 105 -> "NUM9";
				case 37 -> "LEFT";
				case 38 -> "UP";
				case 39 -> "RIGHT";
				case 40 -> "DOWN";
				case 112 -> "F1";
				case 113 -> "F2";
				case 114 -> "F3";
				case 115 -> "F4";
				case 116 -> "F5";
				case 117 -> "F6";
				case 118 -> "F7";
				case 119 -> "F8";
				case 120 -> "F9";
				case 121 -> "F10";
				case 122 -> "F11";
				case 123 -> "F12";

				default -> "";
			};

		}
		return inputString;
	}

	/**
	 * 
	 * 
	 * 
	 */

	/**
	 * Currently return all inputs from USEDFORTICK which are still pressed into
	 * CURRENTLY_PRESSED
	 */
	public static void tick() {
		ArrayList<String> tempUsedForTickList = new ArrayList<>(USEDFORTICK);
		USEDFORTICK = new ArrayList<>();
		for (String s : tempUsedForTickList)
			addInputString(s);

	}

	public class textInput {

		public String text;

		public textInput() {
			text = "";
		}

		public boolean handleInputs() {
			for (String s : CURRENTLY_PRESSED) {
				boolean keyHandeled = true;
				if (s.equals("BACKSPACE")) {
					if (text.length() > 0) {
						text = text.substring(0, text.length() - 1);
					}
				} else if (s.equals("ENTER")) {
					return true;
				} else {
					if (s.length() == 1)
						text += s;
					else
						keyHandeled = false;
				}
				if (keyHandeled)
					consumeKey(s);
			}
			return false;
		}
	}
}
