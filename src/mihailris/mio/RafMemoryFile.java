package mihailris.mio;

import java.io.*;

public class RafMemoryFile implements IMemoryFile {
    private final File file;
    private final long offset;
    private final long length;

    public RafMemoryFile(File file, long offset, long length) {
        this.file = file;
        this.offset = offset;
        this.length = length;
    }

    @Override
    public InputStream read() throws IOException {
        RandomAccessFile raf = new RandomAccessFile(file, "r");
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

            @Override
            public void close() throws IOException {
                raf.close();
            }
        };
    }

    @Override
    public long length() {
        return length;
    }

    @Override
    public OutputStream write(MemoryDevice device, boolean append) throws IOException {
        return null;
    }

    @Override
    public void close() {

    }
}
