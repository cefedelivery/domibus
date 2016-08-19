package eu.domibus.api.jms;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Cosmin Baciu on 17-Aug-16.
 */
public class JmsMessage {
	protected String id;
	protected String type;
	protected String content;
	protected Date timestamp;

	protected Map<String, String> headers = new HashMap<>();
	protected Map<String, String> properties = new HashMap<>();

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	public Map<String, String> getJMSProperties() {
		Map<String, String> jmsProperties = new HashMap<String, String>();
		for (String key : properties.keySet()) {
			if (key.startsWith("JMS")) {
				jmsProperties.put(key, properties.get(key));
			}
		}
		return jmsProperties;
	}

	public Map<String, String> getCustomProperties() {
		Map<String, String> customProperties = new HashMap<String, String>();
		for (String key : properties.keySet()) {
			if (!key.startsWith("JMS")) {
				customProperties.put(key, properties.get(key));
			}
		}
		return customProperties;
	}

	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}

}
