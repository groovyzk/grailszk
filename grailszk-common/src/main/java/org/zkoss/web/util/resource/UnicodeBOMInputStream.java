package org.zkoss.web.util.resource;

import java.io.IOException;
import java.io.PushbackInputStream;
import java.io.InputStream;

public class UnicodeBOMInputStream extends InputStream {
    private final PushbackInputStream in;
    private final BOM bom;
    private boolean skipped;
    
    public UnicodeBOMInputStream(final InputStream inputStream) throws NullPointerException, IOException {
        this.skipped = false;
        if (inputStream == null) {
            throw new NullPointerException("invalid input stream: null is not allowed");
        }
        this.in = new PushbackInputStream(inputStream, 4);
        final byte[] bom = new byte[4];
        final int read = this.in.read(bom);
        Label_0241: {
            switch (read) {
                case 4: {
                    if (bom[0] == -1 && bom[1] == -2 && bom[2] == 0 && bom[3] == 0) {
                        this.bom = BOM.UTF_32_LE;
                        break Label_0241;
                    }
                    if (bom[0] == 0 && bom[1] == 0 && bom[2] == -2 && bom[3] == -1) {
                        this.bom = BOM.UTF_32_BE;
                        break Label_0241;
                    }
                }
                case 3: {
                    if (bom[0] == -17 && bom[1] == -69 && bom[2] == -65) {
                        this.bom = BOM.UTF_8;
                        break Label_0241;
                    }
                }
                case 2: {
                    if (bom[0] == -1 && bom[1] == -2) {
                        this.bom = BOM.UTF_16_LE;
                        break Label_0241;
                    }
                    if (bom[0] == -2 && bom[1] == -1) {
                        this.bom = BOM.UTF_16_BE;
                        break Label_0241;
                    }
                    break;
                }
            }
            this.bom = BOM.NONE;
        }
        if (read > 0) {
            this.in.unread(bom, 0, read);
        }
    }
    
    public final BOM getBOM() {
        return this.bom;
    }
    
    public final synchronized UnicodeBOMInputStream skipBOM() throws IOException {
        if (!this.skipped) {
            this.in.skip(this.bom.bytes.length);
            this.skipped = true;
        }
        return this;
    }
    
    @Override
    public int read() throws IOException {
        return this.in.read();
    }
    
    @Override
    public int read(final byte[] b) throws IOException, NullPointerException {
        return this.in.read(b, 0, b.length);
    }
    
    @Override
    public int read(final byte[] b, final int off, final int len) throws IOException, NullPointerException {
        return this.in.read(b, off, len);
    }
    
    @Override
    public long skip(final long n) throws IOException {
        return this.in.skip(n);
    }
    
    @Override
    public int available() throws IOException {
        return this.in.available();
    }
    
    @Override
    public void close() throws IOException {
        this.in.close();
    }
    
    @Override
    public synchronized void mark(final int readlimit) {
        this.in.mark(readlimit);
    }
    
    @Override
    public synchronized void reset() throws IOException {
        this.in.reset();
    }
    
    @Override
    public boolean markSupported() {
        return this.in.markSupported();
    }
    
    public static final class BOM {
        public static final BOM NONE;
        public static final BOM UTF_8;
        public static final BOM UTF_16_LE;
        public static final BOM UTF_16_BE;
        public static final BOM UTF_32_LE;
        public static final BOM UTF_32_BE;
        final byte[] bytes;
        private final String description;
        
        @Override
        public final String toString() {
            return this.description;
        }
        
        public final byte[] getBytes() {
            final int length = this.bytes.length;
            final byte[] result = new byte[length];
            System.arraycopy(this.bytes, 0, result, 0, length);
            return result;
        }
        
        private BOM(final byte[] bom, final String description) {
            assert bom != null : "invalid BOM: null is not allowed";
            assert description != null : "invalid description: null is not allowed";
            assert description.length() != 0 : "invalid description: empty string is not allowed";
            this.bytes = bom;
            this.description = description;
        }
        
        static {
            NONE = new BOM(new byte[0], "NONE");
            UTF_8 = new BOM(new byte[] { -17, -69, -65 }, "UTF-8");
            UTF_16_LE = new BOM(new byte[] { -1, -2 }, "UTF-16 little-endian");
            UTF_16_BE = new BOM(new byte[] { -2, -1 }, "UTF-16 big-endian");
            UTF_32_LE = new BOM(new byte[] { -1, -2, 0, 0 }, "UTF-32 little-endian");
            UTF_32_BE = new BOM(new byte[] { 0, 0, -2, -1 }, "UTF-32 big-endian");
        }
    }
}
