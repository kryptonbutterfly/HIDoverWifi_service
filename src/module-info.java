module kryptonbutterfly.HIDoverWifi
{
	exports kryptonbutterfly.hid to kryptonbutterfly.ArgsManager;
	exports kryptonbutterfly.hid.prefs to com.google.gson;
	
	requires com.google.gson;
	requires kryptonbutterfly.ArgsManager;
	requires transitive kryptonbutterfly.CLI_Utils;
	requires kryptonbutterfly.mathUtils;
	requires java.desktop;
}
