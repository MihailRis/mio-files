package mihailris.mio;

import java.io.*;

public class BytesMemoryFile implements IMemoryFile {
    byte[] content;
    int length;

    public BytesMemoryFile(byte[] bytes) {
        this.content = bytes;
    }

    @Override
    public InputStream readStream() throws IOException {
        return new ByteArrayInputStream(content, 0, length);
    }

    @Override
    public long length() {
        return length;
    }

    @Override
    public OutputStream write(MemoryDevice device, boolean append) throws IOException {
        return new ByteArrayOutputStream() {
            @Override
            public void close() throws IOException {
                super.close();
                byte[] bytes = toByteArray();
                if (!append) {
                    device.usableSpace -= (length - content.length);
                    content = bytes;
                    return;
                }
                grow(length + bytes.length);
                System.arraycopy(bytes, 0, content, length, bytes.length);
                device.usableSpace -= bytes.length;
                length += bytes.length;
            }
        };
    }

    public void write(byte[] bytes, int offset, int dstOffset, int length) throws IOException {
        grow(dstOffset + length);
        System.arraycopy(bytes, offset, content, dstOffset, length);
        this.length = Math.max(this.length, dstOffset + length);
    }

    void grow(int minimum) {
        if (content.length >= minimum) {
            return;
        }
        byte[] newContent = new byte[minimum];
        System.arraycopy(content, 0, newContent, 0, length);
        content = newContent;
    }

    @Override
    public void close() {
        content = null;
    }

    public void append(int b) {
        grow(length+1);
        content[length++] = (byte) b;
    }

    @Override
    public IORandomAccess openRandomAccess(boolean writeable) throws IOException {
        return new BytesMemoryFileRaf(this);
    }
}
