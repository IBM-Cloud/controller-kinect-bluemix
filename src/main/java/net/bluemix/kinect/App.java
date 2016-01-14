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

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.openkinect.freenect.Context;
import org.openkinect.freenect.DepthFormat;
import org.openkinect.freenect.DepthHandler;
import org.openkinect.freenect.Device;
import org.openkinect.freenect.FrameMode;
import org.openkinect.freenect.Freenect;
import org.openkinect.freenect.VideoFormat;
import org.openkinect.freenect.VideoHandler;
import com.sun.jna.NativeLibrary;
import boofcv.gui.image.ImagePanel;
import boofcv.gui.image.ShowImages;
import boofcv.gui.image.VisualizeImageData;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.openkinect.UtilOpenKinect;
import boofcv.struct.image.ImageUInt16;
import boofcv.struct.image.ImageUInt8;
import boofcv.struct.image.MultiSpectral;

public class App {

	{
		// be sure to set OpenKinectExampleParam.PATH_TO_SHARED_LIBRARY to the
		// location of your shared library!
		NativeLibrary.addSearchPath("freenect", "/Users/nheidloff/git/libfreenect/build/lib");
	}
	
	int duration = 100000; // 100 seconds
	int angle = 10;

	MultiSpectral<ImageUInt8> rgb = new MultiSpectral<ImageUInt8>(ImageUInt8.class, 1, 1, 3);
	ImageUInt16 depth = new ImageUInt16(1, 1);

	BufferedImage outRgb;
	ImagePanel guiRgb;

	BufferedImage outDepth;
	ImagePanel guiDepth;

	Button buttonMove;
	Button buttonStop;
	Button buttonLeft;
	Button buttonRight;

	public void process() {
		buttonMove = new Button();
		buttonMove.setLabel("Move");
		buttonMove.setPositionX(120);
		buttonMove.setPositionY(90);
		buttonMove.setMqttAction(MQTTUtilities.COMMAND_MOVE);
		buttonStop = new Button();
		buttonStop.setLabel("Stop");
		buttonStop.setPositionX(530);
		buttonStop.setPositionY(90);
		buttonStop.setMqttAction(MQTTUtilities.COMMAND_STOP);
		buttonLeft = new Button();
		buttonLeft.setLabel("Left");
		buttonLeft.setPositionX(120);
		buttonLeft.setPositionY(400);
		buttonLeft.setMqttAction(MQTTUtilities.COMMAND_LEFT);
		buttonRight = new Button();
		buttonRight.setLabel("Right");
		buttonRight.setPositionX(530);
		buttonRight.setPositionY(400);
		buttonRight.setMqttAction(MQTTUtilities.COMMAND_RIGHT);

		Context kinect = Freenect.createContext();

		if (kinect.numDevices() < 0)
			throw new RuntimeException("No kinect found!");

		Device device = kinect.openDevice(0);
		
		device.setTiltAngle(angle);
		device.refreshTiltState();

		device.setDepthFormat(DepthFormat.REGISTERED);
		device.setVideoFormat(VideoFormat.RGB);

		device.startDepth(new DepthHandler() {
			@Override
			public void onFrameReceived(FrameMode mode, ByteBuffer frame, int timestamp) {
				processDepth(mode, frame, timestamp);
			}
		});
		device.startVideo(new VideoHandler() {
			@Override
			public void onFrameReceived(FrameMode mode, ByteBuffer frame, int timestamp) {
				processRgb(mode, frame, timestamp);
			}
		});

		long starTime = System.currentTimeMillis();
		while (starTime + duration > System.currentTimeMillis()) {
		}
		System.out.println("Automatically stopped");

		device.stopDepth();
		device.stopVideo();
		device.close();
	}

	protected void processDepth(FrameMode mode, ByteBuffer frame, int timestamp) {
	
		if (outDepth == null) {
			depth.reshape(mode.getWidth(), mode.getHeight());
			// in my case: width 640, height 480
			outDepth = new BufferedImage(depth.width, depth.height, BufferedImage.TYPE_INT_RGB);
			guiDepth = ShowImages.showWindow(outDepth, "Depth Image");
		}

		App.bufferDepthToU16(frame, depth);

		processButtonStatePhaseOne(buttonMove);
		processButtonStatePhaseOne(buttonStop);
		processButtonStatePhaseOne(buttonLeft);
		processButtonStatePhaseOne(buttonRight);

		VisualizeImageData.grayUnsigned(depth, outDepth, UtilOpenKinect.FREENECT_DEPTH_MM_MAX_VALUE);

		drawButton(buttonMove, outDepth);
		drawButton(buttonStop, outDepth);
		drawButton(buttonLeft, outDepth);
		drawButton(buttonRight, outDepth);

		processButtonStatePhaseTwo(buttonMove, outDepth);
		processButtonStatePhaseTwo(buttonStop, outDepth);
		processButtonStatePhaseTwo(buttonLeft, outDepth);
		processButtonStatePhaseTwo(buttonRight, outDepth);

		guiDepth.repaint();
	}

	protected void processRgb(FrameMode mode, ByteBuffer frame, int timestamp) {
		if (mode.getVideoFormat() != VideoFormat.RGB) {
			System.out.println("Bad rgb format!");
		}

		if (outRgb == null) {
			rgb.reshape(mode.getWidth(), mode.getHeight());
			outRgb = new BufferedImage(rgb.width, rgb.height, BufferedImage.TYPE_INT_RGB);
			guiRgb = ShowImages.showWindow(outRgb, "RGB Image");
		}

		App.bufferRgbToMsU8(frame, rgb);
		ConvertBufferedImage.convertTo_U8(rgb, outRgb, true);

		drawButton(buttonMove, outRgb);
		drawButton(buttonStop, outRgb);
		drawButton(buttonLeft, outRgb);
		drawButton(buttonRight, outRgb);

		processButtonStatePhaseTwo(buttonMove, outRgb);
		processButtonStatePhaseTwo(buttonStop, outRgb);
		processButtonStatePhaseTwo(buttonLeft, outRgb);
		processButtonStatePhaseTwo(buttonRight, outRgb);

		guiRgb.repaint();
	}

	private void drawButton(Button button, BufferedImage image) {
		Graphics2D g2;
		Shape s;

		g2 = image.createGraphics();
		g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, 30));
		g2.setColor(Color.gray);
		s = new Ellipse2D.Double(button.getPositionX() - 50, button.getPositionY() - 50, 100, 100);
		g2.draw(s);
		g2.fill(s);
		if (button.getState() == Button.STATE_PRESSED) {
			g2.setColor(Color.red);
		} else {
			if (button.getState() == Button.STATE_NOT_PRESSED) {
				g2.setColor(Color.white);
			} else {
				g2.setColor(Color.red);
			}
		}
		g2.drawString(button.getLabel(), button.getPositionX() - 40, button.getPositionY() + 10);
	}

	private void processButtonStatePhaseTwo(Button button, BufferedImage image) {
		Graphics2D g2;
		if (button.getState() == Button.STATE_ACTIVATED) {
			if (System.currentTimeMillis() < button.getTimeStampActivated() + 3000) {
				g2 = image.createGraphics();
				g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, 30));
				g2.setColor(Color.red);
				g2.drawString("Button Pressed: " + button.getLabel(), 120, 240);

				try {
					if (button.isMqttCommandSent() == false) {
						MQTTUtilities.sendMQTTMessage(button.getMqttAction());
					}
					button.setMqttCommandSent(true);
				} catch (MqttException e) {
					e.printStackTrace();
				}

			} else {
				button.setState(Button.STATE_NOT_PRESSED);
				button.setMqttCommandSent(false);
			}
		}
	}

	private void processButtonStatePhaseOne(Button button) {
		int depthPixel = depth.get(button.getPositionX(), button.getPositionY());

		if (button.getState() == Button.STATE_NOT_PRESSED) {
			if (depthPixel > 520) {
				if (depthPixel < 1000) {
					button.setState(Button.STATE_PRESSED);
					button.setTimeStampPressed();
				}
			}
		} else {
			if (button.getState() == Button.STATE_PRESSED) {
				if (System.currentTimeMillis() < button.getTimeStampPressed() + 1500) {
					if (depthPixel < 520) {
						button.setState(Button.STATE_NOT_PRESSED);
					} else {
						if (depthPixel > 1000) {
							button.setState(Button.STATE_NOT_PRESSED);
						}
					}
				}
				if (System.currentTimeMillis() > button.getTimeStampPressed() + 1500) {
					if (depthPixel > 520) {
						if (depthPixel < 1000) {
							button.setState(Button.STATE_ACTIVATED);
							button.setTimeStampActivated();
						}
					}
				}
			}
		}
	}

	public static void main(String args[]) {
		App app = new App();
		app.process();
	}

	public static void bufferDepthToU16(ByteBuffer input, ImageUInt16 output) {
		int indexIn = 0;
		for (int y = 0; y < output.height; y++) {
			int indexOut = output.startIndex + (y + 1) * output.stride - 1;
			for (int x = 0; x < output.width; x++, indexOut--) {
				output.data[indexOut] = (short) ((input.get(indexIn++) & 0xFF) | ((input.get(indexIn++) & 0xFF) << 8));
				if (((indexOut + output.stride) % output.stride) == 0) {
					indexOut = output.startIndex + (y + 2) * output.stride - 1;
				}
			}
		}
	}

	public static void bufferRgbToMsU8(ByteBuffer input, MultiSpectral<ImageUInt8> output) {
		ImageUInt8 band0 = output.getBand(0);
		ImageUInt8 band1 = output.getBand(1);
		ImageUInt8 band2 = output.getBand(2);

		int indexIn = 0;
		for (int y = 0; y < output.height; y++) {
			int indexOut = output.startIndex + (y + 1) * output.stride - 1;
			for (int x = 0; x < output.width; x++, indexOut--) {
				band0.data[indexOut] = input.get(indexIn++);
				band1.data[indexOut] = input.get(indexIn++);
				band2.data[indexOut] = input.get(indexIn++);
				if (((indexOut + output.stride) % output.stride) == 0) {
					indexOut = output.startIndex + (y + 2) * output.stride - 1;
				}
			}
		}
	}
}
