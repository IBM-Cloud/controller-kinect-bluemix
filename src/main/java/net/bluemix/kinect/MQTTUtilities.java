/*
 * Copyright IBM Corp. 2016
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.bluemix.kinect;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MQTTUtilities {
	
	static public String COMMAND_MOVE = "move";
	static public String COMMAND_STOP = "stop";
	static public String COMMAND_LEFT = "left";
	static public String COMMAND_RIGHT = "right";

	static public void sendMQTTMessage(String command) throws MqttException {

		String deviceId = "niklas";
		String apikey = "";
		String apitoken = "";
		String deviceType = "kinect";

		String org = null;
		String topic = "iot-2/type/" + deviceType + "/id/" + deviceId
				+ "/cmd/anki/fmt/json";
		int qos = 0;

		boolean configExists = true;
		if (apikey == null)
			configExists = false;
		else {
			if (apikey.equalsIgnoreCase(""))
				configExists = false;
		}
		if (apitoken == null)
			configExists = false;
		else {
			if (apitoken.equalsIgnoreCase(""))
				configExists = false;
		}
		String[] tokens = apikey.split("-", -1);
		if (tokens == null)
			configExists = false;
		else {
			if (tokens.length != 3)
				configExists = false;
			else {
				org = tokens[1];
			}
		}

		String broker = "tcp://" + org
				+ ".messaging.internetofthings.ibmcloud.com:1883";
		String clientId = "a:" + org + ":" + deviceId;

		String content = "";
		
		if (command.equalsIgnoreCase(COMMAND_MOVE)) {
			content = "{\"d\":{\"action\":\"move\"}}";
		} else if (command.equalsIgnoreCase(COMMAND_STOP)) {
			content = "{\"d\":{\"action\":\"stop\"}}";
		} else if (command.equalsIgnoreCase(COMMAND_LEFT)) {
			content = "{\"d\":{\"action\":\"left\"}}";
		} else if (command.equalsIgnoreCase(COMMAND_RIGHT)) {
			content = "{\"d\":{\"action\":\"right\"}}";
		} 

		if (configExists == false)
			throw new MqttException(0);

		MemoryPersistence persistence = new MemoryPersistence();
		MqttClient sampleClient = new MqttClient(broker, clientId, persistence);
		MqttConnectOptions connOpts = new MqttConnectOptions();
		connOpts.setPassword(apitoken.toCharArray());
		connOpts.setUserName(apikey);
		connOpts.setCleanSession(true);

		sampleClient.connect(connOpts);

		MqttMessage message = new MqttMessage(content.getBytes());

		message.setQos(qos);
		sampleClient.publish(topic, message);

		sampleClient.disconnect();
	}
}