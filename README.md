An Android app to control model railroad signals via Bluetooth Low Energy (BLE) by writing Characteristics to an Arduino Nano BLE acting as a Gatt-server. The arduino code is in another project.

Currently the app only supports a simple "Hauptsignal" with the 4 aspects of Frei/Free, Langsamfart/Slow, Halt/Stop and Not/Emergency-Fail, but it can be expanded with other signals, controlling model building lightning and similar.

I priortized to have all the signals on one screen, hence there is not a list of devices which you connect to, and then press button.

Instead, all devices with the name "SignalController" are listed, and assumed to have the 4 aspects mentioned above. If an aspect is pressed, the App connects to the gatt-server, discovers the services and delivers the payload. 
There is no connect-first flow in the app.

Next Steps:
-----------
- Check if it is needed to discover services before writing. Currently the services available are anyway assumed based on the name of the device/gatt-server.

Issues:
-------
- Since it is BLE, the response time can be a bit slow, up to a second of delay.

Expansions
----------
In order to allow other signal-types, the App needs the following modifications:
- Show different buttons based on the name of the BLE device/Gatt server.
    - Currently, as long as the Arduino gatt-server has the name "SignalController", it will be displayed with the 4 aspects given above. Different types of signals would need to have their Gatt-server named differently.
- Recognize the different buttons, the name of the device/gatt server and updated the payload accordingly

Credits
-------
The app is highly based on Punchthroughs tutorial at https://punchthrough.com/android-ble-guide/ and their companion starter project obtainable from https://github.com/PunchThrough/ble-starter-android.
The companion project is licensed under Apache 2.0 (http://www.apache.org/licenses/LICENSE-2.0) - I made modifications to suit my need.
 

