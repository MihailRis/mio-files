package mihailris.mio;

import java.io.*;

public class ReadonlyMemoryFile implements IMemoryFile {
    private final File file;
    private final long offset;
    private final long length;

    public ReadonlyMemoryFile(File file, long offset, long length) {
        this.file = file;
        this.offset = offset;
        this.length = length;
    }

    public ReadonlyMemoryFile(File file, long offset) {
        this(file, offset, file.length()-offset);
    }

    public ReadonlyMemoryFile(File file) {
        this(file, 0, file.length());
    }

    @Override
    public InputStream read() throws IOException {
        if (offset == 0)
            return new FileInputStream(file);
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
        throw new IOException(getClass().getSimpleName()+" is read-only");
    }

    @Override
    public void close() {

    }
}
