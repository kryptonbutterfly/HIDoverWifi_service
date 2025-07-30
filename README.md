<img width="82" align="left" src="https://raw.githubusercontent.com/kryptonbutterfly/HIDoverWifi_android/master/md/icon.svg"/>

# HID over Wifi service
</br>
A service that enables the use of your Android device as a mouse & keyboard.

## Requirements

#### Phone
* Android 13+

#### Computer
* A graphical dedian based system
  (requires dpkg & systemd, if missing installs xdotool & jre21)

## Setup
* install the [HIDoverWifi.deb](https://github.com/kryptonbutterfly/HIDoverWifi_service/releases/download/v1.0.0/HIDoverWifi.deb) package
* in order to configure the service run
  ```console
  sudo hidoverwifi
  ```
* type `help` to display all available commands.
* type `x509` to generate a certificate for the service to use.
  If no password is provided a strong password will be generated.
* type `public` to generate the matching certificate to be used by the android app.
* when everything is configured type `exit` to persist the changes
* now you can either restart the system or run:
  ```console
  sudo systemctl restart HIDoverWifi.service
  ```
* the generated certificate can be found at: `/etc/HIDoverWifi/`</br>
  By default it's named `public.p12` and the default password is `public`

All settings are stored at:
`
/etc/HIDoverWifi/settings.json
`

[setup Android App](https://github.com/kryptonbutterfly/HIDoverWifi_android#app-setup)

## Manual setup

* save the [HIDoverWifi.jar](https://github.com/kryptonbutterfly/HIDoverWifi_service/releases/download/v1.0.0/HIDoverWifi.jar) to a folder of your choosing
* ensure you have xdotool and a jre21+ installed.
  [see](https://github.com/kryptonbutterfly/HIDoverWifi_service#ensure-dependencies-are-installed)
* to configure run
  ```console
  java -jar HIDoverWifi.jar
  ```
* [see Setup](https://github.com/kryptonbutterfly/HIDoverWifi_service#Setup) on how to configure
* restarting your system shouldn't be necessary
* to manually start the service run
  ```console
  java -jar HIDoverWifi.jar -s
  ```

All files will be stored in the directory from which the command was executed.

[setup Android App](https://github.com/kryptonbutterfly/HIDoverWifi_android#app-setup)

### Ensure dependencies are installed

ensure jre21+ is installed
```console
java --version
```

expected output:</br>
`openjdk 21.0.7 …`

If you don't have java installed or the version is older than 21.0.0 install it.
```console
sudo apt install openjdk-21-jre
```


ensure xdotool is installed
```console
xdotool
```

If it's not installed run
```console
sudo apt install xdotool
```