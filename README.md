# MIO-Files
File subsystem created for [Zendes2.5](https://mihailris.itch.io/zendes25).

IODevice - base device interface.

Implementations:
- ResDevice (internal files) - uses java resources (readonly)
- DirDevice (directory) - creates IODevice with selected directory as paths root
- AbsDevice (external files) - used only for work with external files ('abs:' is not allowed by IOPath.get, use Disk.absolute instead)
- MemoryDevice (in-memory files) - used as virtual device with data stored in memory

Example of initialization:
```java
Disk disk = new Disk(Main.class); // use any class instead of Main in the same .jar as the application
// right here Disk has no any pre-defined IODevice added
// you need to configure it yourself

// creates ResDevice with label 'res' at root of java resources
disk.createResDevice("res", "/");
// creates DirDevice from given directory
disk.createDirDevice("user", new File(gameDir));
```

Reading string:
```java
String text = disk.readString(IOPath.get("res:texts/names.txt"));
// or
String text = disk.readString(IOPath.get("res:texts/names.txt"), charset);
```

Reading bytes array:
```java
byte[] bytes = disk.readBytes(IOPath.get("res:colomaps/lights.cm"));
```

Reading properties:
```java
Properties properties = new Properties();
disk.read(IOPath.get("res:engine_settings.properties"), properties);
```

Write string:
```java
// 'user' device is added with Disk.createDirDevice("user", new File(...));
disk.write(IOPath.get("user:error_log.txt"), getErrorLog());
```

Write bytes array:
```java
IOPath.get(IOPath.get("user:save.bin")).writeBytes(bytes);
```

Work with external files:
```java
// initialization
disk.createAbsDevice();
// create absolute IOPath
IOPath file = disk.absolute("/home/user/segfault.wav");
// -> abs:home/user/segfault.wav
```
Add custom IODevice:
```java
disk.addDevice("name", new ...);
```
Remove IODevice from Disk (does not affect real files):
```java
disk.removeDevice("user");
```

Creating millions devices is only affecting memory and performance
