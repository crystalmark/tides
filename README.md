# Tide Calculator

A simple Java API for calculating tide times for any date from 1970 onwards.  
Based on harmonics from 2004, the calculated times and heights seems to be within 10 minutes of the main websites.
The times and heights produced by this software should not be used for navigation.

## Build

You will need maven and Java 8 (min), the project has no other external dependencies.
```bash
mvn clean package -DskipTests
```

## Install on to your maven repo
```bash
mvn install:install-file -Dfile=target/jtide-0.0.1.jar -DgroupId=com.github.guikeller -DartifactId=jtide -Dversion=0.0.1 -Dpackaging=jar
```

## Contributing

You might send through a PR, if well explained I will merge in.
Otherwise, feel free to change the package name and make it your own.

## Examples

See the "DemoUsage" class on the test folder.

## In all fairness

This is fork of "https://github.com/crystalmark/tides/" so all the credit should go to him.
I just made it into a lib that others can use, tidied up a bit, and removed external deps.

## License
MIT - Enjoy!

