# Uppidy Android Demo

This repository contains the source code for the Uppidy Android Demo app.


Please see the [issues](https://github.com/uppidy/uppidy-android-demo/issues) section to
report any bugs or feature requests and to see the list of known issues.

## License

* [Apache Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html)

## Building

The build requires [Maven](http://maven.apache.org/download.html)
v3.0.4+ and the [Android SDK](http://developer.android.com/sdk/index.html)
to be installed in your development environment. In addition you'll need to set
the `ANDROID_HOME` environment variable to the location of your SDK:

    export ANDROID_HOME=/opt/tools/android-sdk

After satisfying those requirements, the build is pretty simple:

* Run `mvn clean package` from the `uppidy-android-demo-app` directory to build the APK only
* Run `mvn clean install` from the root directory to build the app and also run
  the integration tests, this requires a connected Android device or running
  emulator

See [here](https://github.com/uppidy/uppidy-android-demo/wiki/Building-From-Eclipse) for
instructions on building from [Eclipse](http://eclipse.org).

## Acknowledgements

This project uses the [Uppidy Android SDK](https://github.com/uppidy/uppidy-android-sdk)
built on top of [Uppidy API 12.07](http://develop.uppidy.com/).

It also uses many other open source libraries such as:

* [spring-android](https://github.com/SpringSource/spring-android)
* [spring-social](https://github.com/SpringSource/spring-social)
* [android-maven-plugin](https://github.com/jayway/maven-android-plugin)

These are just a few of the major dependencies, the entire list of dependencies
is listed in the [app's POM file](https://github.com/uppidy/uppidy-android-demo/blob/master/uppidy-android-demo-app/pom.xml).

## Contributing

Please fork this repository and contribute back using
[pull requests](https://github.com/uppidy/uppidy-android-demo/pulls).

Any contributions, large or small, major features, bug fixes, additional
language translations, unit/integration tests are welcomed and appreciated
but will be thoroughly reviewed and discussed.
