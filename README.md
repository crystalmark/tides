# JTides / A Tide Calculator

A simple Java API for calculating tide times for any date from 1970 onwards. 

Based on harmonics from 2004, the calculated times and heights seems to be within 10 minutes of the main websites.

The times and heights produced by this software should not be used for navigation.

## Build

You will need maven and Java 8 (min), the project has no other external dependencies.
```bash
mvn clean package -DskipTests
```

## Install on to your maven repo

You may get the binary from: https://github.com/guikeller/tides/releases
```bash
mvn install:install-file -Dfile=target/jtides-0.0.3.jar -DgroupId=com.github -DartifactId=jtides -Dversion=0.0.3 -Dpackaging=jar
```

## Use as a dependency pom.xml
```xml
<dependency>
    <groupId>com.github</groupId>
    <artifactId>jtides</artifactId>
    <version>0.0.3</version>
</dependency>    
```

## Contributing

You might send through a PR, if well explained I will merge in.

Otherwise, feel free to fork it and make it your own.

## Example

This is the "DemoUsage" which is also on the test folder.

```java
import TideApi;
import TimedValue;
import StationTreeNode;

import java.time.LocalDate;
import java.util.List;
import java.util.TreeMap;

/**
 * Run this class, see the output generated, compare with what it is out there (google)
 * times and tides might be slightly off, if so adjust on the 'consumer' / 'client'
 * LICENSE: MIT
 * @author Gui Keller
 */
public class DemoUsage {

    private boolean previousIsLeaf = false;
    private final TideApi api = new TideApi();

    public DemoUsage() {
        super();
    }

    public void spaceOutput(String demoName) {
        System.out.println(" ");
        System.out.println("- - -");
        System.out.println(demoName);
    }

    public void printAllStations() {
        this.spaceOutput("#printAllStations");
        List<String> stations = api.getStations();
        stations.forEach(station -> System.out.println(station));
    }

    public void getStationsTree() {
        this.spaceOutput("#getStationsTree");
        TreeMap<String, StationTreeNode> stationsTree = api.getStationsTree();
        printTreeMapRecursively(stationsTree);
    }

    protected void printTreeMapRecursively(TreeMap<String, StationTreeNode> stationsTree) {
        stationsTree.keySet().forEach(key -> {
            StationTreeNode treeNode = stationsTree.get(key);
            if (treeNode.getSubTree().isEmpty()) {
                System.out.println("leaf: " + treeNode);
                this.previousIsLeaf = true;
            } else {
                if (previousIsLeaf) {
                    System.out.println(" * * * ");
                    this.previousIsLeaf = false;
                }
                System.out.println("node: " + key);
                this.printTreeMapRecursively(treeNode.getSubTree());
            }
        });
    }

    // Google: "tide Aba, Nagasaki, Japan" (and compare the results) =]
    public void getHourlyTides() {
        this.spaceOutput("#getHourlyTides");
        String firstStationName = "Aba, Nagasaki, Japan";
        List<TimedValue> hourlyTides = api.getHourlyTides(firstStationName, LocalDate.now());
        hourlyTides.forEach(timedValue -> {
            System.out.println("value: " + timedValue.getValue() + " / type: " + timedValue.getType() + " / time: " + timedValue.getCalendar());
        });
    }

    // Google: "tide Aba, Nagasaki, Japan" (and compare the results) =]
    public void getTideHeightAtTimeAndPlace() {
        this.spaceOutput("#getTideHeightAtTimeAndPlace");

        String firstStationName = "Aba, Nagasaki, Japan";
        List<TimedValue> hourlyTides = api.getTideHeightAtTimeAndPlace(firstStationName, LocalDate.now());
        hourlyTides.forEach(timedValue -> {
            System.out.println("value: " + timedValue.getValue() + " / type: " + timedValue.getType() + " / time: " + timedValue.getCalendar());
        });
    }

    public void printEndMsg() {
        System.out.println();
        System.out.println("* * * # * * *");
        System.out.println("It works, so if you want something better, clone this repo and do it yourself.");
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Demo usage..");
        DemoUsage tests = new DemoUsage();
        tests.printAllStations();
        tests.getStationsTree();
        tests.getHourlyTides();
        tests.getTideHeightAtTimeAndPlace();
        tests.printEndMsg();
        System.exit(0);
    }

}
```

## Output
Tides for "Aba, Nagasaki, Japan" ( date: 13th of May 2021 )

```log
- - -
#getHourlyTides
value: 1.4160338406309354 / type: FALLING / time: 2021-05-13T00:00
value: 1.0593295805457326 / type: null / time: 2021-05-13T01:00
value: 0.9472243346611353 / type: null / time: 2021-05-13T02:00
value: 1.1234888524531055 / type: LW / time: 2021-05-13T03:00
value: 1.5484599856801293 / type: null / time: 2021-05-13T04:00
value: 2.098801990782672 / type: null / time: 2021-05-13T05:00
value: 2.6036313175538597 / type: null / time: 2021-05-13T06:00
value: 2.9042747353176233 / type: null / time: 2021-05-13T07:00
value: 2.9081374954183987 / type: null / time: 2021-05-13T08:00
value: 2.611319683424369 / type: HW / time: 2021-05-13T09:00
value: 2.0862407548668926 / type: null / time: 2021-05-13T10:00
value: 1.4511633240157014 / type: null / time: 2021-05-13T11:00
value: 0.8417258047292665 / type: null / time: 2021-05-13T12:00
value: 0.39086702195567785 / type: null / time: 2021-05-13T13:00
value: 0.2089387114058255 / type: null / time: 2021-05-13T14:00
value: 0.3559938122303696 / type: LW / time: 2021-05-13T15:00
value: 0.8130136042459453 / type: null / time: 2021-05-13T16:00
value: 1.4722754823329631 / type: null / time: 2021-05-13T17:00
value: 2.163127433547176 / type: null / time: 2021-05-13T18:00
value: 2.7081212558523506 / type: null / time: 2021-05-13T19:00
value: 2.9828427878643384 / type: null / time: 2021-05-13T20:00
value: 2.9496273057371596 / type: HW / time: 2021-05-13T21:00
value: 2.6536726117132727 / type: null / time: 2021-05-13T22:00
value: 2.194172657716818 / type: null / time: 2021-05-13T23:00
 
- - -
#getTideHeightAtTimeAndPlace
value: 0.9456539277660275 / type: LW / time: 2021-05-13T01:55
value: 2.9455814602000903 / type: HW / time: 2021-05-13T07:32
value: 0.2083019230732174 / type: LW / time: 2021-05-13T14:05
value: 3.0055881051929414 / type: HW / time: 2021-05-13T20:24
```

Google: "tide Aba, Nagasaki, Japan" (and compare the results).

PS: times and tides might be slightly off, if so adjust on the 'consumer' / 'client' side.

## Contributions

Gui Keller - https://github.com/guikeller/ 

## License

MIT - Enjoy!

