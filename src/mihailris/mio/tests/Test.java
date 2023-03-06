package mihailris.mio.tests;

import mihailris.mio.Disk;
import mihailris.mio.IORandomAccess;
import mihailris.mio.MemoryDevice;

import java.io.DataOutputStream;
import java.io.OutputStream;

public class Test {
    public static void main(String[] args) throws Exception {
        Disk.createAbsDevice();

        MemoryDevice device = new MemoryDevice();
        try (IORandomAccess raf = device.openRandomAccess("test.fs", true)) {
            raf.write('A');
            new DataOutputStream((OutputStream) raf).writeUTF("Some example Text");
            raf.position(2);
            System.out.println((char)raf.read());
            System.out.println(raf.available());
        }
        Disk.close();
    }
}
