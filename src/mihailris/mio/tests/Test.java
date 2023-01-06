package mihailris.mio.tests;

import mihailris.mio.Disk;
import mihailris.mio.IOPath;
import mihailris.mio.IOUtil;
import mihailris.mio.MemoryDevice;

import java.io.IOException;

public class Test {
    public static void main(String[] args) throws IOException {
        Disk.createAbsDevice();
        MemoryDevice device = new MemoryDevice();
        Disk.addDevice("mem", device);

        IOPath.get("mem:test.edt").write("test");
        IOPath dest = IOPath.get("mem:skitala");
        dest.mkdirs();
        IOUtil.unpack(Disk.absolute("/home/ubuntu/Skitala.zip"), dest);

        System.out.println(IOUtil.tree(IOPath.get("mem:")));
        Disk.close();
    }
}
