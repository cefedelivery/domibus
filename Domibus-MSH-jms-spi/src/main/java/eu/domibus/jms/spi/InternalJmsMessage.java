package eu.domibus.jms.spi;

import java.util.*;

/**
 * @author Cosmin Baciu
 * @since 3.2
 */
public class InternalJmsMessage {
	protected String id;
	protected String type;
	protected String content;
	protected Date timestamp;

	protected Map<String, Object> properties = new HashMap<>();

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

	public Map<String, Object> getProperties() {
		return properties;
	}

	//TODO separate between headers and properties
	public Map<String, Object> getJMSProperties() {
		Map<String, Object> jmsProperties = new HashMap<>();
		for (String key : properties.keySet()) {
			if (key.startsWith("JMS")) {
				jmsProperties.put(key, properties.get(key));
			}
		}
		return jmsProperties;
	}

	//TODO separate between headers and properties
	public Map<String, Object> getCustomProperties() {
		Map<String, Object> customProperties = new HashMap<>();
		for (String key : properties.keySet()) {
			if (!key.startsWith("JMS")) {
				customProperties.put(key, properties.get(key));
			}
		}
		return customProperties;
	}

	public void setProperties(Map<String, Object> properties) {
		this.properties = properties;
	}

	@Override
	public String toString() {
		return new org.apache.commons.lang.builder.ToStringBuilder(this)
				.append("id", id)
				.append("type", type)
				.append("content", content)
				.append("timestamp", timestamp)
				.append("properties", properties)
				.toString();
	}
}
