package mihailris.mio.tests;

import mihailris.mio.Disk;
import mihailris.mio.IOPath;
import mihailris.mio.IOUtil;

import java.io.File;

public class Test {
    public static void main(String[] args) throws Exception {
        Disk.createDirDevice("local", new File("./"));
        Disk.createMemoryDevice("memory");

        Disk.copy(IOPath.get("local:src"), IOPath.get("memory:"));
        System.out.println(IOUtil.tree(IOPath.get("memory:")));
    }
}
