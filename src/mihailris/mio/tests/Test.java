package mihailris.mio.tests;

import mihailris.mio.Disk;
import mihailris.mio.IOPath;
import mihailris.mio.IOUtil;
import mihailris.mio.MemoryDevice;

import java.io.IOException;

public class Test {
    public static void main(String[] args) throws IOException {
        Disk disk = new Disk(Test.class);
        disk.createAbsDevice();
        MemoryDevice device = new MemoryDevice();
        disk.addDevice("mem", device);
        disk.write(IOPath.get("mem:test.edt"), "test");
        IOPath dest = IOPath.get("mem:skitala");
        disk.mkdirs(dest);
        IOUtil.unpack(disk, disk.absolute("/home/ubuntu/Skitala.zip"), dest);

        System.out.println(IOUtil.tree(disk, IOPath.get("mem:")));
        disk.close();
    }
}
