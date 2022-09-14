package mihailris.mio.tests;

import mihailris.mio.Disk;

public class Test {
    public static void main(String[] args) {
        Disk.initialize(Test.class);
        Disk.close();
    }
}
