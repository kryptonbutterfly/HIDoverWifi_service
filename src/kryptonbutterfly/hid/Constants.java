package kryptonbutterfly.hid;

import java.io.File;

public interface Constants
{
	static final String	CERTIFICATE_ALIAS				= "HIDcert";
	static final String	GENERATE_PRIVATE_CERT_COMMAND	= "x509";
	
	static final long PROTOCOL_ID = -8596033659527291744L;
	
	static final File CONFIG_FILE = new File("./settings.json");
	
	static final String LINUX_COMMAND = "$ sudo hidoverwifi";
	
	static final int DEFAULT_IDLE_TIMEOUT = 60_000;
}
