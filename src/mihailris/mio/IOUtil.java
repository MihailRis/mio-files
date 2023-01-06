package mihailris.mio;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class IOUtil {
    public static void transfer(InputStream input, OutputStream output, long length) throws IOException {
        long totalRead = 0;
        byte[] buffer = new byte[1024];
        while (totalRead < length) {
            int readBytes = input.read(buffer, 0, (int) Math.min(buffer.length, length - totalRead));
            output.write(buffer, 0, readBytes);
            totalRead += readBytes;
        }
    }

    public static void readFully(InputStream input, byte[] bytes) throws IOException {
        int read;
        for (int pos = 0; pos < bytes.length; pos += read) {
            read = input.read(bytes, pos, bytes.length - pos);
            if (read < 0) {
                throw new EOFException();
            }
        }
    }

    /**
     * Unzip all content from ZIP file to destination directory.
     * Tries to delete unpacked files on exception
     * @param source target ZIP file
     * @param dest destination directory
     * @throws IOException on I/O errors
     */
    public static void unpack(Disk disk, IOPath source, IOPath dest) throws IOException {

        List<IOPath> created = new ArrayList<>();
        try (InputStream input = disk.read(source)) {
            ZipInputStream zis = new ZipInputStream(input);
            ZipEntry zipEntry = zis.getNextEntry();
            long available = disk.getUsableSpace(dest);
            while (zipEntry != null) {
                IOPath element = dest.child(zipEntry.getName());
                if (zipEntry.isDirectory()) {
                    if (!disk.isDirectory(element)) {
                        disk.mkdirs(element);
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
                        disk.write(element, bytes);
                        created.add(element);
                    } catch (IOException e) {
                        // Revert creations
                        for (IOPath path : created) {
                            try {
                                disk.delete(path);
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

    public static String tree(Disk disk, IOPath root) {
        StringBuilder builder = new StringBuilder();

        if (root.getDepth() == 0) {
            builder.append("<").append(root.getPrefix()).append(" root>");
        } else {
            builder.append(root);
        }
        if (disk.isDirectory(root)) {
            builder.append(":");
            IOPath[] files = disk.list(root);
            if (files.length == 0) {
                builder.append(" empty");
                return builder.toString();
            }
            for (IOPath file : files) {
                builder.append("\n");
                tree(disk, file, builder, 1);
            }
        }
        return builder.toString();
    }

    private static void tree(Disk disk, IOPath root, StringBuilder builder, int indent) {
        for (int i = 0; i < indent; i++) {
            builder.append("\t");
        }
        builder.append(root.name());
        if (disk.isDirectory(root)) {
            builder.append(":");
            IOPath[] files = disk.list(root);
            if (files.length == 0) {
                builder.append(" empty");
                return;
            }
            for (IOPath file : files) {
                builder.append("\n");
                tree(disk, file, builder, indent+1);
            }
        }
    }
}
