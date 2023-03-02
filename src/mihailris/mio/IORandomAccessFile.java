package mihailris.mio;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class IORandomAccessFile extends RandomAccessFile implements IORandomAccess {
    public IORandomAccessFile(String name, String mode) throws FileNotFoundException {
        super(name, mode);
    }

    public IORandomAccessFile(File file, String mode) throws FileNotFoundException {
        super(file, mode);
    }

    @Override
    public void position(long pos) throws IOException {
        seek(pos);
    }

    @Override
    public long position() throws IOException {
        return getFilePointer();
    }

    @Override
    public long available() throws IOException {
        return super.length() - position();
    }
}
