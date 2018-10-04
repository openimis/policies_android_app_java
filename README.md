# openIMIS - IMIS Android Application

The openIMIS IMIS Android Application is the mobile client used by
the Enrollment Officers to easily enter and register, renew and manage 
insurees from anywhere online or offline.

## Getting Started

These instructions will get you a copy of the project up and
running on your local machine for development and testing purposes.
See deployment for notes on how to deploy the project on a live 
system.

### Prerequisites

In order to use and develop the openIMIS IMIS Android Application
on your local machine, you first need to install:

* [Android Studio with Android SDK](https://developer.android.com/studio)
* [openIMIS Web Services](https://github.com/openimis/web_service_vb)


### Installation

To make a copy of this project on your local machine, please clone
the repository.

```
git clone https://github.com/openimis/openimis_android_app_java
```

### Configuration

In order to run the openIMIS IMIS Android Application, you need to
know the openIMIS Web Services domain (DNS or IP address) and to 
configure it in the tz.co.exact.imis.AppInformation java file.

```
private static String _Domain = "http://demo.openimis.org/";
```

For demo purposes, the default Web Services domain is set to the openIMIS
demo server: demo.openimis.org.  

### Running the app

After configuring the application, you can then compile and execute
on Android operated mobile devices.

## Deployment

To deploy on a live environment, the previous steps have to be followed
and to build the APK. Manual deployment is required on each mobile device.

## Versioning

We use [SemVer](http://semver.org/) for versioning. For the versions available, see the [tags on this repository](https://github.com/openimis/openimis_android_app_java/tags).

## License

Copyright (c) Swiss Agency for Development and Cooperation (SDC)

This project is licensed under the GNU AGPL v3 License - see the
[LICENSE.md](LICENSE.md) file for details.

