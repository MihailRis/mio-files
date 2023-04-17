package mihailris.mio.misc;

import mihailris.mio.IODeviceAdapter;
import mihailris.mio.IOPath;
import mihailris.mio.IORandomAccess;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class RAFS extends IODeviceAdapter {
    private final boolean readonly;
    private final IORandomAccess raf;
    public static final int MAX_ENTRIES = 0xFFFF;
    private int clusterSize = 4096;
    private final int capacity;
    private final int clusters;
    private final int header;
    private final boolean[] clusterMap;

    private static final int ENTRY_DIR = 1;
    private static final int ENTRY_FILE = 2;

    private void formatFS() throws IOException {
        raf.setLength(header + clusterSize);
        raf.position(header);
        raf.write(ENTRY_DIR);
        raf.writeShort(0); // size
        useCluster(0);

        System.out.println(header + capacity);
        raf.setLength(header + capacity);
    }

    public RAFS(IORandomAccess raf, boolean readonly, int clusterSize, int clusters) throws IOException {
        this.readonly = readonly;
        this.raf = raf;
        if (raf.position() != 0)
            throw new IllegalArgumentException("IORandomAccess.position() returned value > 0");
        this.clusterSize = clusterSize;
        this.clusters = clusters;
        this.capacity = clusterSize * clusters;
        header = clusters;
        clusterMap = new boolean[clusters];
        if (raf.available() == 0) {
            formatFS();
        } else {
            for (int i = 0; i < clusters; i++) {
                clusterMap[i] = raf.read() != 0;
            }
        }
        raf.position(0);
    }

    @Override
    public OutputStream write(String path, boolean append) throws IOException {
        synchronized (raf) {
            byte[] nameBuffer = new byte[256];
            int parentOffset = getParentOffset(path);
            if (parentOffset < 0)
                throw new IOException("no directory found '" + path.substring(0, path.lastIndexOf('/')) + "'");
            String name = path;
            if (name.contains("/")) {
                name = name.substring(name.lastIndexOf('/') + 1);
            }
            raf.position(header + parentOffset);
            byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
            int type = raf.read();
            assert (type == ENTRY_DIR);
            int size = raf.readShort() & 0xFFFF;
            System.out.println(size);
            for (int i = 0; i < size; i++) {
                int fileSize = raf.readInt();
                int cluster = raf.readInt();
                int nameLength = raf.read();
                raf.readFully(nameBuffer, 0, nameLength);
                if (Arrays.equals(nameBytes, nameBuffer)) {
                    int position = toPosition(cluster);
                    if (append) {
                        position += fileSize;
                    }
                    seek(position);
                    return null;
                }
            }
            int end = (int) raf.position();
            if (append)
                throw new IOException("file '" + path + "' does not exists");

            int cluster = findFreeCluster();
            if (cluster < 0)
                throw new IOException("no enough space to create a file");
            useCluster(cluster);

            raf.position(parentOffset);
            raf.writeShort(size + 1);
            raf.position(end);

            int entryPosition = (int) raf.position();
            raf.writeInt(0);
            raf.writeInt(cluster);

            raf.write(nameBytes.length);
            raf.write(nameBytes);

            int position = toPosition(cluster);
            return new OutputStream() {
                @Override
                public void write(int b) throws IOException {
                    RAFS.this.write(entryPosition, b);
                }

                @Override
                public void write(byte[] b, int off, int len) throws IOException {

                }
            };
        }
    }

    private void write(int entryPosition, int b) throws IOException {
        synchronized (raf) {
            seek(entryPosition);
            int size = raf.readInt();
            raf.position(entryPosition);
            raf.writeInt(size+1);
            int cluster = raf.readInt();
            while (size > clusterSize -4) {
                seek(toPosition(cluster)+(clusterSize -4));
                cluster = raf.readInt();
                size -= clusterSize -4;
                if (cluster == -1) {

                }
            }
        }
    }

    private void useCluster(int cluster) throws IOException {
        if (clusterMap[cluster]) {
            throw new IllegalStateException("cluster is already in use "+cluster);
        }
        clusterMap[cluster] = true;
        raf.position(cluster);
        raf.write(1);
        raf.position(toPosition(cluster) + clusterSize - 4);
        raf.writeInt(-1);
    }

    private int findFreeCluster() {
        for (int i = 0; i < clusters; i++) {
            if (!clusterMap[i]) {
                return i;
            }
        }
        return -1;
    }

    private int toPosition(int cluster) {
        return header + cluster * clusterSize;
    }

    private void seek(int position) throws IOException {
        synchronized (raf) {
            if (position > raf.position() + raf.available()) {
                raf.setLength(position);
            }
            raf.position(position);
        }
    }

    private int getFileOffset(String path) throws IOException {
        synchronized (raf) {
            String[] parts = path.split("/");
            if (parts.length == 0)
                return 0;
            System.out.println(Arrays.toString(parts));
        }
        return -1;
    }

    private int getParentOffset(String path) throws IOException {
        int index = path.lastIndexOf('/');
        if (index == -1)
            return 0;
        return getFileOffset(path.substring(0, index));
    }

    @Override
    public boolean isReadonly() {
        return readonly;
    }

    @Override
    public InputStream read(String path) throws IOException {
        return null;
    }

    @Override
    public IOPath[] listDir(IOPath path) {
        return new IOPath[0];
    }

    @Override
    public long length(String path) {
        return 0;
    }

    @Override
    public long lastModified(String path) {
        return 0;
    }

    @Override
    public boolean exists(String path) {
        try {
            return getFileOffset(path) >= 0;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public boolean isFile(String path) {
        try {
            int offset = getFileOffset(path);
            if (offset >= 0)
                return true;
        } catch (IOException ignored) {
        }
        return false;
    }

    @Override
    public boolean isDirectory(String path) {
        return false;
    }

    @Override
    public boolean isLink(String path) {
        return false;
    }
}
