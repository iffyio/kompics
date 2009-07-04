package se.sics.kompics.wan.config;

import java.io.IOException;
import java.util.concurrent.Semaphore;

import org.apache.commons.cli.Option;
import org.apache.commons.configuration.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import se.sics.kompics.wan.util.HostsParserException;

public class PlanetLabConfiguration extends MasterConfiguration {

	private static ApplicationContext ctx; 
	public static final String PLANETLAB_APP_CONTEXT = "applicationContext-planetLabCache.xml";
	
	public static final String PROP_LOCAL_XML_RPC_PORT = "XmlRpcPort";
	
	public static final String PROP_HTTP_PROXY_HOST = "HttpProxyHost";
	public static final String PROP_HTTP_PROXY_PORT = "HttpProxyPort";
	public static final String PROP_HTTP_PROXY_USERNAME = "HttpProxyUsername";
	public static final String PROP_HTTP_PROXY_PASSWORD = "HttpProxyPassword";
	public static final String PROP_PLC_API_ADDRESS = "PlcApiAddress";
	
	
	public static final int DEFAULT_LOCAL_XML_RPC_PORT = 8088;
	public static final String DEFAULT_PLC_API_ADDRESS = "https://www.planet-lab.org/PLCAPI/";
	public static final String DEFAULT_HTTP_PROXY_HOST = "";
	public static final int DEFAULT_HTTP_PROXY_PORT = -1;
	public static final String DEFAULT_HTTP_PROXY_USERNAME = "";
	public static final String DEFAULT_HTTP_PROXY_PASSWORD = "";
	
	
	public static final String SPECIAL_COMMAND_UPLOAD_DIR = "#upload";
	public static final String SPECIAL_COMMAND_DOWNLOAD_DIR = "#download";
	
	/********************************************************/
	/********* Helper fields ********************************/
	/********************************************************/
	
	protected Option localXmlRpcPortOption;
	protected Option plcApiOption;
	protected Option httpProxyHostOption;
	protected Option httpProxyPortOption;
	protected Option httpProxyUsernameOption;
	protected Option httpProxyPasswordOption;
	
	protected static boolean plInitialized = false;
	
	// creates the queue used to limit the number of concurrent network intensive
	// threads. The queue is fair meaning that it operates in a FIFO manner
	private static Semaphore networkIntensiveTicket = new Semaphore(5, true);

	private static long startTime = System.currentTimeMillis();
	
	/**
	 * 
	 * @param args
	 * @throws IOException
	 * @throws HostsParserException 
	 */
	public PlanetLabConfiguration(String[] args) throws ConfigurationException, HostsParserException, IOException {
		super(args);
		
		plInitialized = true;
		ctx = new ClassPathXmlApplicationContext(PLANETLAB_APP_CONTEXT);
	}

	@Override
	protected void parseAdditionalOptions(String[] args) throws IOException {
		super.parseAdditionalOptions(args);
		localXmlRpcPortOption = new Option("localXmlRpcPort", true, "Local XML RPC port");
		localXmlRpcPortOption.setArgName("number");
		options.addOption(localXmlRpcPortOption);
		
		plcApiOption = new Option("plcApiAddress", true, "PLC API Address");
		plcApiOption.setArgName("address");
		options.addOption(plcApiOption);
		
		httpProxyHostOption = new Option("httpProxy", true, "Hostname of http proxy server.");
		httpProxyHostOption.setArgName("host");
		options.addOption(httpProxyHostOption);
		
		httpProxyPortOption = new Option("httpProxyPort", true, "Port number of http proxy server.");
		httpProxyPortOption.setArgName("number");
		options.addOption(httpProxyPortOption);
		
		httpProxyUsernameOption = new Option("httpProxyUsername", true, "Username for authentication with the http proxy server.");
		httpProxyUsernameOption.setArgName("username");
		options.addOption(httpProxyUsernameOption);
		
		httpProxyPasswordOption = new Option("httpProxyPassword", true, "Password for authentication with the http proxy server.");
		httpProxyPasswordOption.setArgName("password");
		options.addOption(httpProxyPasswordOption);
	}

	@Override
	protected void processAdditionalOptions() throws IOException {
		super.processAdditionalOptions();
		if (line.hasOption(localXmlRpcPortOption.getOpt()))
		{
			int scf = new Integer(line.getOptionValue(localXmlRpcPortOption.getOpt()));
			compositeConfig.setProperty(PROP_LOCAL_XML_RPC_PORT, scf);
		}
		
		if (line.hasOption(plcApiOption.getOpt())) {
			String plc = new String(line.getOptionValue(plcApiOption.getOpt()));
			compositeConfig.setProperty(PROP_PLC_API_ADDRESS, plc);
		}
		
		if (line.hasOption(httpProxyHostOption.getOpt())) {
			String host = new String(line.getOptionValue(httpProxyHostOption.getOpt()));
			compositeConfig.setProperty(PROP_HTTP_PROXY_HOST, host);
		}
		
		if (line.hasOption(httpProxyPortOption.getOpt())) {
			int port = new Integer(line.getOptionValue(httpProxyPortOption.getOpt()));
			compositeConfig.setProperty(PROP_HTTP_PROXY_PORT, port);
		}
		
		if (line.hasOption(httpProxyUsernameOption.getOpt())) {
			String user = new String(line.getOptionValue(httpProxyUsernameOption.getOpt()));
			compositeConfig.setProperty(PROP_HTTP_PROXY_USERNAME, user);
		}
		
		if (line.hasOption(httpProxyPasswordOption.getOpt())) {
			String pass = new String(line.getOptionValue(httpProxyPasswordOption.getOpt()));
			compositeConfig.setProperty(PROP_HTTP_PROXY_PASSWORD, pass);
		}
	}

	static public int getLocalXmlRpcPort()
	{
		return configuration.compositeConfig.getInt(PROP_LOCAL_XML_RPC_PORT, DEFAULT_LOCAL_XML_RPC_PORT);
	}

	static public String getPlcApiAddress()
	{
		return configuration.compositeConfig.getString(PROP_PLC_API_ADDRESS, DEFAULT_PLC_API_ADDRESS);
	}
	
	static public String getHttpProxyHost()
	{
		return configuration.compositeConfig.getString(PROP_HTTP_PROXY_HOST, DEFAULT_HTTP_PROXY_HOST);
	}
	static public int getHttpProxyPort()
	{
		return configuration.compositeConfig.getInt(PROP_HTTP_PROXY_PORT, DEFAULT_HTTP_PROXY_PORT);
	}

	static public String getHttpProxyUsername()
	{
		return configuration.compositeConfig.getString(PROP_HTTP_PROXY_USERNAME, DEFAULT_HTTP_PROXY_USERNAME);
	}
	static public String getHttpProxyPassword()
	{
		return configuration.compositeConfig.getString(PROP_HTTP_PROXY_PASSWORD, DEFAULT_HTTP_PROXY_PASSWORD);
	}

	
	static protected void planetLabInitialized() {
		baseInitialized();
		if (plInitialized == false)
		{
			throw new IllegalStateException("MasterServerConfiguration not initialized  before use.");
		}
	}
	
	public static int getNetworkIntensiveTicket() throws InterruptedException {
		networkIntensiveTicket.acquire();
		return networkIntensiveTicket.availablePermits();
	}
	
	public static int releaseNetworkIntensiveTicket() {
		networkIntensiveTicket.release();
		return networkIntensiveTicket.availablePermits();
	}
	
	/**
	 * increase the number of concurrent network intensive threads that are
	 * allowed to run. Note that this only can be increased
	 * 
	 * @param newLimit
	 *            the new limit
	 */
	public static void increaseConcurrentCopyLimit(int newLimit) {
		int currentLimit = networkIntensiveTicket.getQueueLength()
				+ networkIntensiveTicket.availablePermits();
		int diff = newLimit - currentLimit;
		if (currentLimit < newLimit) {
			networkIntensiveTicket.release(diff);
		} else {
			// only increase is allowed
		}
		return;
	}

	
	/**
	 * Time since the master started. 
	 * 
	 * @return number of milliseconds since the master started.
	 */

	public static long getTime() {
		long currentTime = System.currentTimeMillis();
		return (currentTime - startTime) ;
	}
	
	public static ApplicationContext getCtx() {
		return ctx;
	}

}