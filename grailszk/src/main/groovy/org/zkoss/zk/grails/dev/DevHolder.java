package org.zkoss.zk.grails.dev;

import java.util.HashSet;
import java.io.File;
import java.util.Objects;
import java.util.Set;

public class DevHolder {

    private final Set<DevEntry> set = new HashSet<>();

    private DevEntry contains(String s) {
        for(DevEntry de: set) {
            if(de.path.equals(s)) {
                return de;
            }
        }
        return null;
    }

    public File check(String s) {
        DevEntry e = contains(s);
        if(e != null) {
            set.remove(e);
            return e.file;
        }
        return null;
    }

    public void add(String s, File f) {
        set.clear();
        set.add(new DevEntry(s, f));
    }

    static class DevEntry {

        final String path;
        final File file;

        DevEntry(String path, File file) {
            this.path = path;
            this.file = file;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof DevEntry)) return false;
            DevEntry devEntry = (DevEntry) o;
            return Objects.equals(path, devEntry.path);
        }

        @Override
        public int hashCode() {
            return path != null ? path.hashCode() : 0;
        }
    }

}
