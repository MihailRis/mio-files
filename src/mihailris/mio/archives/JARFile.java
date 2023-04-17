package mihailris.mio.archives;

import mihailris.mio.Disk;
import mihailris.mio.IOPath;
import mihailris.mio.MemoryDevice;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

public class JARFile {
    /**
     * Unzip all content from JAR file to destination directory.
     * Tries to delete unpacked files on exception
     * @param source target JAR file
     * @param dest destination directory
     * @throws IOException on I/O errors
     */
    public static void unpack(IOPath source, IOPath dest) throws IOException {
        try (InputStream input = Disk.read(source)) {
            try (JarInputStream zis = new JarInputStream(input)) {
                unpack(source, dest, zis);
            }
        }
    }

    public static void unpack(IOPath source, IOPath dest, JarInputStream zis) throws IOException {
        List<IOPath> created = new ArrayList<>();
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
                        byte[] buffer = new byte[1024];
                        while (true) {
                            int red = zis.read(buffer, 0, buffer.length);
                            if (red == -1)
                                break;
                            baos.write(buffer, 0, red);
                        }
                        element.parent().mkdirs();
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
    }

    public static MemoryDevice createDevice(IOPath source) throws IOException {
        MemoryDevice device = new MemoryDevice();
        try (Disk.TempLabelHandle handle = Disk.addDeviceTemporary(device)) {
            unpack(source, IOPath.get(handle.label+":"));
        }
        return device;
    }

    private static void pack(IOPath base, IOPath source, JarOutputStream zip) throws IOException {
        if (source.isFile()) {
            String name = source.getPath().substring(base.getPath().length());
            if (name.startsWith("/")) {
                name = name.substring(1);
            }
            JarEntry entry = new JarEntry(name);
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
        JarOutputStream zip = new JarOutputStream(output);
        if (source.isFile()) {
            JarEntry entry = new JarEntry(source.name());
            zip.putNextEntry(entry);
            byte[] data = source.readBytes();
            zip.write(data, 0, data.length);
            zip.closeEntry();
        } else {
            pack(source, source, zip);
        }
        zip.close();
    }
}
