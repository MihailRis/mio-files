package mihailris.mio;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Comparator;

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

    public static String tree(IOPath root) {
        StringBuilder builder = new StringBuilder();

        if (root.getDepth() == 0) {
            builder.append("<").append(root.getPrefix()).append(" root>");
        } else {
            builder.append(root);
        }
        if (root.isDirectory()) {
            builder.append(":");
            IOPath[] files = root.list();
            if (files.length == 0) {
                builder.append(" empty");
                return builder.toString();
            }
            for (IOPath file : files) {
                builder.append("\n");
                tree(file, builder, 1);
            }
        }
        return builder.toString();
    }

    private static void tree(IOPath root, StringBuilder builder, int indent) {
        for (int i = 0; i < indent; i++) {
            builder.append("  ");
        }
        builder.append(root.name());
        if (root.isDirectory()) {
            builder.append("/");
            IOPath[] files = root.list();
            if (files.length == 0) {
                builder.append(" empty");
                return;
            }
            for (IOPath file : files) {
                builder.append("\n");
                tree(file, builder, indent+1);
            }
        }
    }

    public static final Comparator<IOPath> modifyDateComparator = Comparator.comparingLong(IOPath::lastModified).reversed();
    public static void sortByModificationDate(IOPath[] files) {
        Arrays.sort(files, modifyDateComparator);
    }
}
