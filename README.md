Controller to connect Kinect with Bluemix
================================================================================

The [controller-kinect-bluemix](https://github.com/IBM-Bluemix/controller-kinect-bluemix) project contains sample code that shows how to send MQTT commands to [IBM Bluemix](https://bluemix.net) when 'buttons' are pressed via Kinect.

Buttons are pressed when you move hands over them and wait for two seconds. The project is configured so that this only works when hands are between 50 cm and 1,00 m away from the Kinect meassured via the depth sensor.

The best way to explain the functionality are pictures. Check out the pictures in the [screenshots](https://github.com/IBM-Bluemix/controller-kinect-bluemix/tree/master/screenshots) directory. You can use this project for example to steer [Anki Overdrive](https://anki.com/en-us/overdrive/starter-kit) cars.

![alt text](https://raw.githubusercontent.com/IBM-Bluemix/controller-kinect-bluemix/master/screenshots/kinect-pressed-rgb.jpg "Kinect")

The project has been implemented via the [libfreenect](https://github.com/OpenKinect/libfreenect) and the [BoofCV](https://github.com/lessthanoptimal/BoofCV) libraries. MQTT messages are sent via [Paho](https://projects.eclipse.org/projects/technology.paho) to [IBM Bluemix](https://bluemix.net/) and the [Internet of Things](https://console.ng.bluemix.net/catalog/internet-of-things/) foundation. 

Via the Internet of Things foundation the commands can be sent to devices like the Anki Overdrive cars. The cars can be connected to the foundation via the separate project [Node.js Controller and MQTT API for Anki Overdrive](https://github.com/IBM-Bluemix/node-mqtt-for-anki-overdrive).

Author: Niklas Heidloff [@nheidloff](http://twitter.com/nheidloff)


Setup
================================================================================

I've used a [Kinect](https://en.wikipedia.org/wiki/Kinect) (model 1414) and a MacBook Pro.

The libfreenect library needs to be installed first. Follow the [instructions](https://github.com/OpenKinect/libfreenect#osx) on GitHub.

The controller is a Java application. Make sure the following tools are installed and on your path.

* [Java](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
* [Maven](https://maven.apache.org/install.html)
* [Git](https://git-scm.com/downloads)

Invoke the following command to download the project.

> git clone https://github.com/IBM-Bluemix/controller-kinect-bluemix.git

Before you can build the project you need to modify two Java files. In net.bluemix.kinect.App define the location of the libfreenect library. In net.bluemix.kinect.MQTTUtilities define the MQTT properties: deviceId, deviceType, apikey and apitoken.

You will need to create an instance of the [IBM IoT Foundation](https://console.ng.bluemix.net/?ace_base=true/#/store/cloudOEPaneId=store&serviceOfferingGuid=8e3a9040-7ce8-4022-a36b-47f836d2b83e&fromCatalog=true) service in Bluemix.  Within the IoT Foundation dashboard your need to register a device as well as an app.

1. Click the devices tab and click Add Device
2. Create a new device type called "kinect"
3. In the device ID field enter a unique ID 
4. Click on the API Keys tab
5. Click the New API Key link

In order to build the project invoke this command.

> mvn package

After this you can run the controller class net.bluemix.kinect.App via "java". Alternatively you can import the project in Eclipse as Maven project and run the app from there.

In order to steer the [Anki Overdrive](https://github.com/IBM-Bluemix/node-mqtt-for-anki-overdrive) cars you can import a [flow](https://github.com/IBM-Bluemix/node-mqtt-for-anki-overdrive/blob/master/node-red-speech-kinect.json) in Node-RED.