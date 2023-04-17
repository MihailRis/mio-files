package mihailris.mio.tests;

import mihailris.mio.Disk;
import mihailris.mio.IOPath;
import mihailris.mio.IORandomAccess;
import mihailris.mio.MemoryDevice;
import mihailris.mio.archives.ZIPFile;

import java.io.File;
import java.util.zip.ZipEntry;

public class Test {
    public static void main(String[] args) throws Exception {
        Disk.createDirDevice("local", new File("./"));
        ZIPFile.unpack(IOPath.get("local:dread.jar"), IOPath.get("local:t"));
    }
}
