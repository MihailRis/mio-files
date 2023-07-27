package mihailris.mio.tests;

import mihailris.mio.Disk;
import mihailris.mio.IOPath;
import mihailris.mio.IOUtil;

import java.io.File;

public class Test {
    public static void main(String[] args) throws Exception {
        Disk.createAbsDevice();

        IOPath root = Disk.absolute("/home");
        System.out.println(root);
    }
}
