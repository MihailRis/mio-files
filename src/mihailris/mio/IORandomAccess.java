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

    default short readShort() throws IOException {
        int ch1 = read();
        int ch2 = read();
        if ((ch1 | ch2) < 0)
            throw new EOFException();
        return (short)((ch1 << 8) + (ch2));
    }

    default int readInt() throws IOException {
        int ch1 = read();
        int ch2 = read();
        int ch3 = read();
        int ch4 = read();
        if ((ch1 | ch2 | ch3 | ch4) < 0)
            throw new EOFException();
        return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4));
    }

    default void writeShort(int value) throws IOException {
        write((value >> 8) & 0xFF);
        write((value) & 0xFF);
    }

    default void writeInt(int value) throws IOException {
        write((value >> 24) & 0xFF);
        write((value >> 16) & 0xFF);
        write((value >> 8) & 0xFF);
        write((value) & 0xFF);
    }
}
