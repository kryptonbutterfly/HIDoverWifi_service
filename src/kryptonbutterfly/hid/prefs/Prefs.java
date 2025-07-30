package kryptonbutterfly.hid.prefs;

import com.google.gson.annotations.Expose;

import kryptonbutterfly.hid.Constants;
import kryptonbutterfly.hid.misc.Utils;

public final class Prefs
{
	@Expose
	public int port = 4620;
	
	@Expose
	public String binding = null;
	
	@Expose
	public boolean performAction = true;
	
	@Expose
	public boolean printAction = false;
	
	@Expose
	public int scrollSlowdown = 5;
	
	@Expose
	public float mouseAcceleration = 1.3F;
	
	@Expose
	public int connectionIdleTimeout = Constants.DEFAULT_IDLE_TIMEOUT;
	
	@Expose
	public String serverPassword = Utils.generate(32);
	
	@Expose
	public CryptoData cryptoData = new CryptoData();
}
