# MIO-Files
File subsystem originally created for [Zendes2.5](https://mihailris.itch.io/zendes25).

IODevice - base device interface.

Implementations:
- ResDevice (internal files) - uses java resources (readonly)
- DirDevice (directory) - creates IODevice with selected directory as paths root
- AbsDevice (external files) - used only for work with external files ('abs:' is not allowed by IOPath.get, use Disk.absolute instead)
- MemoryDevice (in-memory files) - used as virtual device with data stored in memory

Example of initialization:
```java
// creates ResDevice with label 'res' at root of java resources
Disk.createResDevice("res", "/");
// creates DirDevice from given directory
Disk.createDirDevice("user", new File(gameDir));
```

## Reading
Reading string:
```java
String text = IOPath.get("res:texts/names.txt").readString();
// or
String text = IOPath.get("res:texts/names.txt").readString(charset);
```

Reading bytes array:
```java
byte[] bytes = IOPath.get("res:colomaps/lights.cm").readBytes();
```

Reading properties:
```java
Properties properties = new Properties();
IOPath.get("res:engine_settings.properties").read(properties);
```


## Writing
Write string:
```java
// 'user' device is added with Disk.createDirDevice("user", new File(...));
IOPath.get("user:error_log.txt").write(getErrorLog());
```

Write bytes array:
```java
IOPath.get("user:save.bin").write(bytes);
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

Add IODevice temporary to use with Disk:
```java
try (Disk.TempLabelHandle handle = Disk.addDeviceTemporary(device)) {
    IOPath.get(handle.label+":test.txt").write("Some text");    
}
```

## Archives

### ZIP:

Create memory device based on ZIP-file content:
```java
MemoryDevice device = ZIPFile.createDevice(IOPath.get("user:archive.zip"));
```


Unpack ZIP-file:
```java
ZIPFile.unpack(IOPath.get("user:archive.zip"), IOPath.get("user:destination/path"));
```

### TAR:

Create memory device based on TAR-file content:
```java
MemoryDevice device = TARFile.createDevice(new File("some_archive.tar"));
```

Unpack TAR-file into an existing MemoryDevice:
```java
MemoryDevice device = ...;
TARFile.readInto(new File("some_archive.tar"), device);
// it only store offsets to files content, not actual content
```

Unpack TAR-file:
```java
TARFile.unpack(new File("some_archive.tar"), IOPath.get("user:destination/path"));
```

Creating millions devices is only affecting memory and performance

