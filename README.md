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

The configuration of the application is done through product flavors and build variants. The configuration can be found in ```app\build.gradle``` file. The default configuration contains demoProd or demoRelease flavors. Here is an example of product flavor. 

```
demoProd {
	applicationId "org.openimis.imispolicies.demoProd"
	buildConfigField "String", "API_BASE_URL", '"http://demo.openimis.org/rest/"'
	buildConfigField "String", "RAR_PASSWORD", '")(#$1HsD"'
	buildConfigField "boolean", "SHOW_CONTROL_NUMBER_MENU", 'false'
	resValue "string", "app_name_policies", "Policies Demo"
	dimension = 'std'
}
```

Parameters: 

* ```applicationId``` allow to build the application with its own application id. This allows to run different version of the application on the same device.
* ```API_BASE_URL``` represents the openIMIS REST API URL (based on DNS or IP address) to connect to. 
* ```RAR_PASSWORD``` represents the password to be used for the offline extract. 
* ```SHOW_CONTROL_NUMBER_MENU``` allow to show or hide the Control Number menu item in case the implementation does not implement the ePayment module. 
* ```app_name_policies``` is a resource string allowing to change the name of the application.

Escape procedures can be configured and language resource files can be changed. Please follow the ```sourceSets``` record. Look in ```app\src\demo``` folder for an example. 

```
sourceSets {
	demoProd.java.srcDir 'src/demo/java'
	demoRelease.java.srcDir 'src/demo/java'
}
```

You can add your own product flavor. Make sure you choose the updated built variant or your product flavor when running or building the application.

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

