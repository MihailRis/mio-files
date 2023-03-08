package mihailris.mio.tests;

import mihailris.mio.Disk;
import mihailris.mio.IOPath;
import mihailris.mio.IOUtil;
import mihailris.mio.MemoryDevice;
import mihailris.mio.archives.TARFile;

import java.io.File;
import java.io.IOException;

public class Test {
    public static void main(String[] args) throws IOException {
        Disk.createAbsDevice();

        File file = new File("test.tar");

        MemoryDevice device = TARFile.createDevice(file);
        Disk.addDevice("mem", device);

        System.out.println(IOUtil.tree(IOPath.get("mem:")));
        System.out.println(IOPath.get("mem:src/mihailris/mio/IODevice.java").readString());
        Disk.close();
    }
}
