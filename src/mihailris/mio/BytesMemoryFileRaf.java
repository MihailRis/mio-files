package mihailris.mio;

import java.io.IOException;

public class BytesMemoryFileRaf implements IORandomAccess {
    private final BytesMemoryFile file;
    private int offset = 0;

    public BytesMemoryFileRaf(BytesMemoryFile file) {
        this.file = file;
    }

    @Override
    public int read() throws IOException {
        if (offset >= file.length()) {
            return -1;
        }
        return file.content[offset++];
    }

    @Override
    public int read(byte[] bytes, int offset, int length) throws IOException {
        if (offset >= file.length) {
            return -1;
        }
        int toread = (int) Math.min(length, available());
        System.arraycopy(file.content, this.offset, bytes, offset, toread);
        return toread;
    }

    @Override
    public void position(long pos) throws IOException {
        if (pos < 0 || pos > file.length()) {
            throw new IOException("invalid position "+pos+"/"+file.length());
        }
        offset = (int) pos;
    }

    @Override
    public long position() throws IOException {
        return offset;
    }

    @Override
    public long available() throws IOException {
        return file.length()-offset;
    }

    @Override
    public void write(int b) throws IOException {
        if (offset == file.length()) {
            file.append(b);
        } else {
            file.content[offset] = (byte) b;
        }
    }

    @Override
    public void write(byte[] bytes, int offset, int length) throws IOException {
        file.write(bytes, offset, this.offset, length);
    }

    @Override
    public void setLength(int size) {
        file.setLength(size);
    }

    @Override
    public void close() {

    }
}
