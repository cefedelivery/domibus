package utility;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

public class PropertyFileRead {

	public String Read(String ele) throws IOException {

		FileReader reader = new FileReader(System.getProperty("user.dir") + "/src/main/java/data/data.properties");

		Properties properties = new Properties();
		properties.load(reader);

		return properties.getProperty(ele);
	}

	public void write(List<String> value) throws Exception {

		FileWriter writer = new FileWriter(System.getProperty("user.dir") + "/src/main/java/data/dataW.properties");
		Properties propertiesW = new Properties();

		for (int i = 0; i < value.size(); i++) {
			propertiesW.setProperty("MessageID" + i, value.get(i));
		}
		propertiesW.store(writer, "WriteFile");
	}

	public String ReadW(String ele) throws IOException {

		FileReader reader = new FileReader(System.getProperty("user.dir") + "/src/main/java/data/dataW.properties");

		Properties properties = new Properties();
		properties.load(reader);
		System.out.println("value of message id is " + properties.getProperty(ele));

		return properties.getProperty(ele);
	}

}
