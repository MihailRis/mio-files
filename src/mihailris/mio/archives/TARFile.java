package mihailris.mio.archives;

import mihailris.mio.MemoryDevice;
import mihailris.mio.JavaMemoryFile;

import java.io.*;

/**
 * Simple TAR-file reader
 */
public class TARFile implements Closeable {
    /**
    struct posix_header
    {
        char name[100];
        char mode[8];
        char uid[8];
        char gid[8];
        char size[12];
        char mtime[12];
        char chksum[8];
        char typeflag;
        char linkname[100];
        char magic[6];
        char version[2];
        char uname[32];
        char gname[32];
        char devmajor[8];
        char devminor[8];
        char prefix[155];
    };
    #define TVERSION "00"
    #define TVERSLEN 2
    **/

    public static final int MAGIC_LEN = 6;
    public static final String MAGIC = "ustar";

    private final RandomAccessFile raf;
    private final File file;
    public TARFile(File file) throws FileNotFoundException {
        this.raf = new RandomAccessFile(file, "r");
        this.file = file;
    }

    public static MemoryDevice createDevice(File file) throws IOException {
        try (TARFile reader = new TARFile(file)) {
            return reader.createDevice();
        }
    }

    public static class TAREntry {
        public String name;
        public int mode;
        public int uid;
        public int gid;
        public int size;
        public long chksum;
        public char type;
        public long mtime;
        public long offset;
        public String linkName;
        public String uname;
        public String gname;
        public String deviceMajor;
        public String deviceMinor;

        @Override
        public String toString() {
            return "TAREntry("+name+", "+size+"B)";
        }
    }

    public TAREntry readNext() throws IOException {
        TAREntry entry = new TAREntry();
        entry.name = readCStr(100);
        entry.mode = Integer.parseInt(readCStr(8), 8);
        entry.uid = Integer.parseInt(readCStr(8), 8);
        entry.gid = Integer.parseInt(readCStr(8), 8);
        entry.size = Integer.parseInt(readCStr(12), 8);
        entry.mtime = Long.parseLong(readCStr(12), 8);
        entry.chksum = Long.parseLong(readCStr(8), 8);
        entry.type = (char) raf.read();
        entry.linkName = readCStr(100);

        String magic = readCStr(MAGIC_LEN).trim();
        if (!magic.equals(MAGIC)) {
            throw new IOException("magic "+magic+" != "+MAGIC+"; format is not supported");
        }
        raf.read(); // vMajor
        raf.read(); // vMinor
        entry.uname = readCStr(32);
        entry.gname = readCStr(32);
        entry.deviceMajor = readCStr(8);
        entry.deviceMinor = readCStr(8);
        raf.skipBytes(155);
        raf.skipBytes(12);
        entry.offset = raf.getFilePointer();
        raf.skipBytes(entry.size);
        skipZeros();
        return entry;
    }

    public byte[] readContentBytes(TAREntry entry) throws IOException {
        return readContentBytes(entry.offset, entry.size);
    }

    private byte[] readContentBytes(long offset, int size) throws IOException {
        long lastPos = raf.getFilePointer();
        raf.seek(offset);
        try {
            byte[] data = new byte[size];
            raf.readFully(data);
            return data;
        } finally {
            raf.seek(lastPos);
        }
    }

    public InputStream readContent(TAREntry entry) throws IOException {
        return readContent(entry.offset);
    }

    public InputStream readContent(long offset) throws IOException {
        raf.seek(offset);
        return new InputStream() {
            @Override
            public int read() throws IOException {
                return raf.read();
            }

            @Override
            public int read(byte[] bytes, int i, int i1) throws IOException {
                return raf.read(bytes, i, i1);
            }
        };
    }

    public boolean hasNext() throws IOException {
        return (raf.getFilePointer() != raf.length());
    }

    private void skipZeros() throws IOException {
        int read;
        //noinspection StatementWithEmptyBody
        while ((read = raf.read()) == 0) {
        }
        if (read != -1) {
            raf.seek(raf.getFilePointer() - 1);
        }
    }

    private String readCStr(int length) throws IOException {
        byte[] stringBytes = new byte[length];
        raf.readFully(stringBytes);
        String string = new String(stringBytes);
        int index = string.indexOf('\0');
        if (index == -1)
            return string;
        return string.substring(0, index);
    }

    @Override
    public void close() throws IOException {
        raf.close();
    }

    public void readInto(MemoryDevice device) throws IOException {
        while (hasNext()) {
            TARFile.TAREntry entry = readNext();
            String path = entry.name;
            if (entry.type != '0' && entry.type != '\0')
                continue;
            if (path.contains("/")) {
                device.mkdirs(path.substring(0, path.lastIndexOf('/')));
            }
            device.setLastModified(path, entry.mtime);
            device.set(path, new JavaMemoryFile(file, entry.offset, entry.size));
        }
    }

    public MemoryDevice createDevice() throws IOException {
        MemoryDevice device = new MemoryDevice();
        readInto(device);
        return device;
    }
}
