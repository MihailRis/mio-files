package mihailris.mio;

import java.io.IOException;

public interface DiskListener {
    public enum DiskEvent {
        READ,
        WRITE,
        DELETE,
        LIST,
        MKDIRS,
        UNZIP,
    }

    void onFail(DiskEvent event, IOPath path, String comment, IOException e);
    void onEvent(DiskEvent event, IOPath path, String comment);
}
