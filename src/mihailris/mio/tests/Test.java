package mihailris.mio.tests;

import mihailris.mio.Disk;
import mihailris.mio.IORandomAccess;
import mihailris.mio.MemoryDevice;

public class Test {
    public static void main(String[] args) throws Exception {
        Disk.createAbsDevice();

        MemoryDevice device = new MemoryDevice();
        try (IORandomAccess raf = device.openRandomAccess("test.fs", true)) {
            raf.write('A');
            raf.position(0);
            System.out.println((char)raf.read());
            System.out.println(raf.available());
        }
        Disk.close();
    }
}
