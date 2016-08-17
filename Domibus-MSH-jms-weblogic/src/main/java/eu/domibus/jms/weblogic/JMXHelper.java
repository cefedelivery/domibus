package eu.domibus.jms.weblogic;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Hashtable;
import java.util.Map;

public class JMXHelper {
	public static final String LOCAL_DOMAIN_RUNTIME_MBEANSERVER_JNDI = "java:comp/env/jmx/domainRuntime";
	public static final String REMOTE_DOMAIN_RUNTIME_MBEANSERVER_JNDI = "/jndi/weblogic.management.mbeanservers.domainruntime";
	public static final String DOMAIN_RUNTIME_SERVICE_OBJECTNAME = "com.bea:Name=DomainRuntimeService,Type=weblogic.management.mbeanservers.domainruntime.DomainRuntimeServiceMBean";

	private static final String ADMIN_URL_PROPERTY = "weblogic.management.server";

	private static final Log LOG = LogFactory.getLog(JMXHelper.class);

	private SecurityHelper securityHelper = new SecurityHelper();

	/**
	 * Locally lookup domain runtime bean server. This will only work if we are already on an admin server.
	 * 
	 * @return MBeanServer or <code>null</code> if not found
	 */
	public MBeanServer getDomainRuntimeMBeanServer() {
		MBeanServer mbeanServer = null;
		try {
			InitialContext ic = new InitialContext();
			mbeanServer = (MBeanServer) ic.lookup(LOCAL_DOMAIN_RUNTIME_MBEANSERVER_JNDI);
		} catch (NamingException e) {
			LOG.error("Failed to find domain runtime mbean server locally", e);
		}
		return mbeanServer;
	}

	/**
	 * Remotely connect to domain runtime mbean server. Try to find url, username and password using system properties.
	 * 
	 * @return MBeanServerConnection or <code>null</code> if connection failed.
	 */
	public MBeanServerConnection getDomainRuntimeMBeanServerConnection() {
		MBeanServerConnection mbsc = null;
		try {
			String adminUrl = System.getProperty(ADMIN_URL_PROPERTY);
			if (adminUrl == null) {
				// We must be on admin server...build remote url by introspecting admin server mbean
				adminUrl = "t3://";
				String adminHost = null;
				Integer adminPort = null;
				MBeanServer mbs = getDomainRuntimeMBeanServer();
				ObjectName drs = getDomainRuntimeService();
				ObjectName[] servers = (ObjectName[]) mbs.getAttribute(drs, "ServerRuntimes");
				for (ObjectName server : servers) {
					adminHost = (String) mbs.getAttribute(server, "AdminServerHost");
					adminPort = (Integer) mbs.getAttribute(server, "AdminServerListenPort");
					if (adminHost != null && adminPort != null) {
						break;
					}
				}
				adminUrl = adminUrl + adminHost + ":" + adminPort;
			}
			// Build JMX service url
			String protocol = adminUrl.substring(0, adminUrl.indexOf(":"));
			if (!"t3".equals(protocol)) {
				protocol = "t3"; // Enforce t3 to prevent connectivity issues
			}
			String host = adminUrl.substring(adminUrl.indexOf(":") + 3, adminUrl.lastIndexOf(":"));
			String port = adminUrl.substring(adminUrl.lastIndexOf(":") + 1);
			JMXServiceURL serviceURL = new JMXServiceURL(protocol, host, Integer.parseInt(port), REMOTE_DOMAIN_RUNTIME_MBEANSERVER_JNDI);
			// Build security context to connect with
			Map<String, String> bootIdentity = securityHelper.getBootIdentity();
			String username = bootIdentity.get("username");
			String password = bootIdentity.get("password");
			Hashtable<String, String> ctx = new Hashtable<String, String>();
			ctx.put(Context.SECURITY_PRINCIPAL, username);
			ctx.put(Context.SECURITY_CREDENTIALS, password);
			ctx.put(JMXConnectorFactory.PROTOCOL_PROVIDER_PACKAGES, "weblogic.management.remote");
			JMXConnector connector = JMXConnectorFactory.connect(serviceURL, ctx);
			mbsc = connector.getMBeanServerConnection();
		} catch (Exception e) {
			LOG.error("Failed to connect to domain runtime mbean server", e);
		}
		return mbsc;
	}

	public ObjectName getDomainRuntimeService() {
		ObjectName drs = null;
		try {
			drs = new ObjectName(DOMAIN_RUNTIME_SERVICE_OBJECTNAME);
		} catch (MalformedObjectNameException e) {
			LOG.error("Failed to get domain runtime mbean service object name", e);
		}
		return drs;
	}

}
