package mihailris.mio;

import java.io.EOFException;
import java.io.IOException;

public interface IORandomAccess extends AutoCloseable {
    int read() throws IOException;
    int read(byte[] bytes, int offset, int length) throws IOException;
    void position(long pos) throws IOException;
    long position() throws IOException;
    long available() throws IOException;
    void write(int b) throws IOException;
    void write(byte[] bytes, int offset, int length) throws IOException;

    default void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    default void readFully(byte[] b) throws IOException {
        readFully(b, 0, b.length);
    }

    default void readFully(byte[] b, int off, int len) throws IOException {
        int n = 0;
        do {
            int count = this.read(b, off + n, len - n);
            if (count < 0)
                throw new EOFException();
            n += count;
        } while (n < len);
    }

    void setLength(int size) throws IOException;
}
