# Tide Calculator

A RESTFul interface for calculating tide times for any date from 1970 onwards.  Based on harmonics from 2004, the calculated times and hights seems to be within 10 minutes of the main websites.

The times and heights produced by this software should not be used for navigation.

## Build and Run

You will need to unzip the xml.zip file in /src/main/resources/xml.

To build and run use mvn spring-boot:run

## Endpoint examples

http://localhost:8080/tides/Leith
http://localhost:8080/tides/Leith?date=20150728
http://localhost:8080/tides/hourly/Leith
http://localhost:8080/tides/hourly/Leith?date=20150728
http://localhost:8080/tides/stations

Output is currently application/json

Enjoy.

