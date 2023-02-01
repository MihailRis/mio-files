package mihailris.mio;

import java.io.*;

public class BytesMemoryFile implements IMemoryFile {
    byte[] content;

    public BytesMemoryFile(byte[] bytes) {
        this.content = bytes;
    }

    @Override
    public InputStream read() throws IOException {
        return new ByteArrayInputStream(content);
    }

    @Override
    public long length() {
        return content.length;
    }

    @Override
    public OutputStream write(MemoryDevice device, boolean append) throws IOException {
        return new ByteArrayOutputStream() {
            @Override
            public void close() throws IOException {
                super.close();
                byte[] bytes = toByteArray();
                if (!append) {
                    device.usableSpace -= (bytes.length - content.length);
                    content = bytes;
                    return;
                }
                byte[] newContent = new byte[content.length + bytes.length];
                System.arraycopy(content, 0, newContent, 0, content.length);
                System.arraycopy(bytes, 0, newContent, content.length, bytes.length);
                device.usableSpace -= bytes.length;
                content = newContent;
            }
        };
    }

    @Override
    public void close() {
        content = null;
    }
}
