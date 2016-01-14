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

public class Button {

	static final int STATE_NOT_PRESSED = 0;
	static final int STATE_PRESSED = 1;
	static final int STATE_ACTIVATED = 2;
		
	private int state = STATE_NOT_PRESSED;
	
	private long timeStampPressed;
	private long timeStampActivated;
	
	private int positionX;
	private int positionY;
	
	private String label;
	
	private String mqttAction;
	private boolean mqttCommandSent = false;

	public boolean isMqttCommandSent() {
		return mqttCommandSent;
	}

	public void setMqttCommandSent(boolean mqttCommandSent) {
		this.mqttCommandSent = mqttCommandSent;
	}

	public String getMqttAction() {
		return mqttAction;
	}

	public void setMqttAction(String mqttAction) {
		this.mqttAction = mqttAction;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public int getPositionX() {
		return positionX;
	}

	public void setPositionX(int positionX) {
		this.positionX = positionX;
	}

	public int getPositionY() {
		return positionY;
	}

	public void setPositionY(int positionY) {
		this.positionY = positionY;
	}

	public long getTimeStampPressed() {
		return timeStampPressed;
	}

	public void setTimeStampPressed() {
		this.timeStampPressed = System.currentTimeMillis();
	}

	public long getTimeStampActivated() {
		return timeStampActivated;
	}

	public void setTimeStampActivated() {
		this.timeStampActivated = System.currentTimeMillis();
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}
}