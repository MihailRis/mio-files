package mihailris.mio.archives;

import mihailris.mio.Disk;
import mihailris.mio.IOPath;
import mihailris.mio.MemoryDevice;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

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
                        if (size == -1) {
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            int offset = 0;
                            byte[] buffer = new byte[1024];
                            while (true) {
                                int red = zis.read(buffer, offset, (int) (size - offset));
                                if (red == -1)
                                    break;
                                baos.write(buffer, offset, red);
                                offset += red;
                            }
                            element.write(baos.toByteArray());
                        } else {
                            byte[] bytes = new byte[(int) size];
                            int offset = 0;
                            while (offset < size) {
                                int red = zis.read(bytes, offset, (int) (size - offset));
                                if (red == -1)
                                    break;
                                offset += red;
                            }
                            element.write(bytes);
                        }
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

    private static void pack(IOPath base, IOPath source, ZipOutputStream zip) throws IOException {
        if (source.isFile()) {
            String name = source.getPath().substring(base.getPath().length());
            ZipEntry entry = new ZipEntry(name);
            zip.putNextEntry(entry);
            byte[] data = source.readBytes();
            zip.write(data, 0, data.length);
            zip.closeEntry();
        } else {
            IOPath[] files = source.list();
            if (files == null)
                return;
            for (IOPath file : files) {
                pack(base, file, zip);
            }
        }
    }

    public static void pack(IOPath source, IOPath dest) throws IOException {
        OutputStream output;
        File jfile = Disk.getFile(dest);
        if (jfile == null) {
            output = new ByteArrayOutputStream() {
                @Override
                public void close() throws IOException {
                    super.close();
                    dest.write(toByteArray());
                }
            };
        } else {
            output = new FileOutputStream(jfile);
        }
        ZipOutputStream zip = new ZipOutputStream(output);
        if (source.isFile()) {
            ZipEntry entry = new ZipEntry(source.name());
            zip.putNextEntry(entry);
            byte[] data = source.readBytes();
            zip.write(data, 0, data.length);
            zip.closeEntry();
        } else {
            pack(source, source, zip);
        }
    }
}
