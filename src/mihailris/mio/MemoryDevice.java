package mihailris.mio;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class MemoryDevice extends IODeviceAdapter {
    protected boolean readonly;
    protected DirNode root;
    private long usableSpace = 1024*1024*256;

    @Override
    public boolean isReadonly() {
        return readonly;
    }

    @Override
    public long getUsableSpace(String path) {
        return usableSpace;
    }

    @Override
    public OutputStream write(String path, boolean append) throws IOException {
        Node node = getWriteableNode(path, !append);
        if (!(node instanceof FileNode))
            throw new FileNotFoundException(path);
        FileNode file = (FileNode) node;
        return new ByteArrayOutputStream() {
            @Override
            public void close() throws IOException {
                super.close();
                byte[] bytes = toByteArray();
                if (!append) {
                    file.content = bytes;
                    return;
                }
                byte[] newContent = new byte[file.content.length + bytes.length];
                System.arraycopy(file.content, 0, newContent, 0, file.content.length);
                System.arraycopy(bytes, 0, newContent, file.content.length, bytes.length);
                file.content = newContent;
            }
        };
    }

    @Override
    public boolean setLastModified(String path, long lastModified) {
        Node node = getNode(path);
        if (node == null)
            return false;
        node.lastModified = lastModified;
        return true;
    }

    @Override
    public boolean mkdirs(String path) {
        String[] elements = path.split("/");
        Node node = root;
        for (String element : elements) {
            while (node instanceof LinkNode) {
                LinkNode link = (LinkNode) node;
                node = link.target;
            }
            if (node instanceof DirNode) {
                DirNode dir = (DirNode) node;
                node = dir.nodes.get(element);
                if (node == null) {
                    node = new DirNode(element, dir, new HashMap<>());
                    dir.nodes.put(element, node);
                }
            } else if (node instanceof FileNode) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean delete(String path) {
        Node node = getNode(path);
        if (node == null)
            return false;
        if (node.parent == null)
            return false;
        DirNode dir = node.parent;
        dir.nodes.put(node.name, null);
        return true;
    }

    @Override
    public InputStream read(String path) throws IOException {
        Node node = getNode(path);
        while (node instanceof LinkNode) {
            node = ((LinkNode)node).target;
        }
        if (!(node instanceof FileNode))
            throw new IOException(path+" is a directory");

        FileNode file = (FileNode) node;
        return new ByteArrayInputStream(file.content);
    }

    @Override
    public IOPath[] listDir(IOPath path) {
        Node node = getNode(path.getPath());
        while (node instanceof LinkNode) {
            node = ((LinkNode)node).target;
        }
        if (!(node instanceof DirNode))
            return null;
        DirNode dir = (DirNode) node;
        IOPath[] paths = new IOPath[dir.nodes.size()];
        int index = 0;
        for (String key : dir.nodes.keySet()){
            paths[index++] = path.child(key);
        }
        return paths;
    }

    @Override
    public long length(String path) {
        Node node = getNode(path);
        while (node instanceof LinkNode) {
            node = ((LinkNode)node).target;
        }
        if (!(node instanceof FileNode))
            return -1;

        return ((FileNode)node).content.length;
    }

    @Override
    public long lastModified(String path) {
        Node node = getNode(path);
        if (node == null)
            return -1;
        return node.lastModified;
    }

    @Override
    public boolean exists(String path) {
        return getNode(path) != null;
    }

    @Override
    public boolean isFile(String path) {
        return getNode(path) instanceof FileNode;
    }

    @Override
    public boolean isDirectory(String path) {
        return getNode(path) instanceof DirNode;
    }

    @Override
    public boolean isLink(String path) {
        return getNode(path) instanceof LinkNode;
    }

    public void setUsableSpace(long usableSpace) {
        this.usableSpace = usableSpace;
    }

    static class Node {
        DirNode parent;
        long lastModified;
        String name;
        Node(String name, DirNode parent){
            this.name = name;
            this.parent = parent;
        }
    }

    private Node getNode(String path){
        String[] elements = path.split("/");
        Node node = root;
        for (String element : elements) {
            while (node instanceof LinkNode) {
                LinkNode link = (LinkNode) node;
                node = link.target;
            }
            if (node instanceof DirNode) {
                DirNode dir = (DirNode) node;
                node = dir.nodes.get(element);
                if (node == null)
                    return null;
            }
            else if (node instanceof FileNode) {
                return null;
            }
        }
        return node;
    }

    private Node getWriteableNode(String path, boolean create){
        String[] elements = path.split("/");
        Node node = root;
        for (int i = 0; i < elements.length; i++) {
            String element = elements[i];
            while (node instanceof LinkNode) {
                LinkNode link = (LinkNode) node;
                node = link.target;
            }
            if (node instanceof DirNode) {
                DirNode dir = (DirNode) node;
                node = dir.nodes.get(element);
                if (node == null) {
                    if (i == elements.length-1 && create){
                        FileNode file = new FileNode(element, dir, new byte[0]);
                        dir.nodes.put(element, file);
                        return file;
                    }
                    return null;
                }
            }
            else if (node instanceof FileNode) {
                return null;
            }
        }
        while (node instanceof LinkNode) {
            node = ((LinkNode)node).target;
        }
        if (node instanceof FileNode) {
            return node;
        }
        return null;
    }

    static class DirNode extends Node {
        final Map<String, Node> nodes;

        public DirNode(String name, DirNode parent, Map<String, Node> nodes) {
            super(name, parent);
            this.nodes = nodes;
        }
    }

    static class LinkNode extends Node {
        final Node target;

        public LinkNode(String name, DirNode parent, Node target) {
            super(name, parent);
            this.target = target;
        }
    }

    static class FileNode extends Node {
        byte[] content;

        FileNode(String name, DirNode parent, byte[] content) {
            super(name, parent);
            this.content = content;
        }
    }
}
