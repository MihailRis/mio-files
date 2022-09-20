package mihailris.mio.tests;

import mihailris.mio.Disk;
import mihailris.mio.IOPath;
import mihailris.mio.MemoryDevice;

import java.util.Arrays;

public class Test {
    public static void main(String[] args) {
        Disk.initialize(Test.class);
        MemoryDevice device = new MemoryDevice();
        Disk.addDevice("mem", device);
        System.out.println("Test.main "+ Arrays.toString(IOPath.get("mem:").listDir()));
        Disk.close();
    }
}
