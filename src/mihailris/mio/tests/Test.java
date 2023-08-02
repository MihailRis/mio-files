package mihailris.mio.tests;

import mihailris.mio.Disk;
import mihailris.mio.IOPath;

import java.util.Arrays;

public class Test {
    public static void main(String[] args) throws Exception {
        Disk.createAbsDevice();

        IOPath root = Disk.absolute("");
        System.out.println(Arrays.toString(root.list()));
    }
}
