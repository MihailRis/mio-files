package mihailris.mio.archives;

import mihailris.mio.Disk;
import mihailris.mio.IOPath;
import mihailris.mio.MemoryDevice;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZIPFile {
    /**
     * Unzip all content from ZIP file to destination directory.
     * Tries to delete unpacked files on exception
     * @param source target ZIP file
     * @param dest destination directory
     * @throws IOException on I/O errors
     */
    public static void unpack(IOPath source, IOPath dest) throws IOException {
        List<IOPath> created = new ArrayList<>();
        try (InputStream input = Disk.read(source)) {
            ZipInputStream zis = new ZipInputStream(input);
            ZipEntry zipEntry = zis.getNextEntry();
            long available = Disk.getUsableSpace(dest);
            while (zipEntry != null) {
                IOPath element = dest.child(zipEntry.getName());
                if (zipEntry.isDirectory()) {
                    if (!element.isDirectory()) {
                        element.mkdirs();
                        created.add(element);
                    }
                } else {
                    try {
                        if (zipEntry.getSize() >= available)
                            throw new IOException("no enough space to unpack " + zipEntry.getName() +
                                    " (" + zipEntry.getSize() + " B)");
                        long size = zipEntry.getSize();
                        byte[] bytes = new byte[(int) size];
                        int offset = 0;
                        while (offset < size) {
                            offset += zis.read(bytes, offset, (int) (size - offset));
                        }
                        element.write(bytes);
                        created.add(element);
                    } catch (IOException e) {
                        // Revert creations
                        for (IOPath path : created) {
                            try {
                                path.delete();
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        }
                        zis.closeEntry();
                        zis.close();
                        throw e;
                    }
                }
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
            zis.close();
        }
    }

    public static MemoryDevice createDevice(IOPath source) throws IOException {
        MemoryDevice device = new MemoryDevice();
        try (Disk.TempLabelHandle handle = Disk.addDeviceTemporary(device)) {
            unpack(source, IOPath.get(handle.label+":"));
        }
        return device;
    }
}
