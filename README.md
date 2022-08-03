# MIO-Files
File subsystem created for [Zendes2.5](https://mihailris.itch.io/zendes25).

IODevice - base device interface.

Implementations:
- ResDevice (internal files) - uses jarDir (inside of .jar) if program run from jar-file else uses localDir
- DirDevice (directory) - creates IODevice with selected directory as filesystem root
- AbsDevice (external files) - used only for work with external files ('abs:' is not allowed by IOPath.get, use Disk.absolute instead)

Example of initialization:
```java
Disk.initialize();
// right here Disk has no any pre-defined IODevice added
// you need to configure it yourself
Disk.createResDevice("res", "res"); // creates ResDevice
Disk.createDirDevice("user", new File(gameDir)); // creates DirDevice from given directory
```

Reading string:
```java
String text = Disk.readString(IOPath.get("res:texts/names.txt"));
// or
String text = Disk.readString(IOPath.get("res:texts/names.txt"), charset);
```

Reading bytes array:
```java
byte[] bytes = Disk.readBytes(IOPath.get("res:colomaps/lights.cm"));
```

Reading properties:
```java
Properties properties = new Properties();
Disk.read(properties, IOPath.get("res:engine_settings.properties"));
```

Write string:
```java
// 'user' device is added with Disk.createDirDevice("user", new File(...));
Disk.writeString(IOPath.get("user:error_log.txt"), getErrorLog());
```

Work with external files:
```java
// initialization
Disk.createAbsDevice();
// create absolute IOPath
IOPath file = Disk.absolute("/home/user/segfault.wav");
// -> abs:home/user/segfault.wav
```
Add custom IODevice:
```java
Disk.addDevice("name", new ...);
```
Remove IODevice from Disk (does not affect real files):
```java
Disk.removeDevice("user");
```

Creating millions devices is only affecting memory and performance
