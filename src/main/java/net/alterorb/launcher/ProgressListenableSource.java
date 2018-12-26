package net.alterorb.launcher;

import okio.Buffer;
import okio.ForwardingSource;
import okio.Source;

import java.io.IOException;

public class ProgressListenableSource extends ForwardingSource {

    private final ProgressListener progressListener;
    private final long contentLength;
    private long totalBytesRead;

    public ProgressListenableSource(Source delegate, long contentLength, ProgressListener progressListener) {
        super(delegate);
        this.progressListener = progressListener;
        this.contentLength = contentLength;
    }

    @Override
    public long read(Buffer sink, long byteCount) throws IOException {
        long bytesRead = super.read(sink, byteCount);
        totalBytesRead += bytesRead != -1 ? bytesRead : 0;

        if (progressListener != null) {
            progressListener.update(totalBytesRead, contentLength, bytesRead == -1);
        }
        return bytesRead;
    }

    public interface ProgressListener {

        void update(long bytesRead, long contentLength, boolean done);
    }
}
