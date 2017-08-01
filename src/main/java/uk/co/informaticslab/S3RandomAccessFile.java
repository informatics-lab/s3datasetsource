package uk.co.informaticslab;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3URI;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import ucar.unidata.io.RandomAccessFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Provides random access to files in S3 via byte ranged requests.
 *
 * originally written by @jamesmcclain
 */
public class S3RandomAccessFile extends RandomAccessFile {

    public static final int DEFAULT_S3_BUFFER_SIZE = 1<<19;
    public static final int DEFAULT_MAX_CACHE_SIZE = 1<<25;

    private final AmazonS3URI uri;
    private final AmazonS3 client;
    private final String bucket;
    private final String key;
    private final ObjectMetadata metadata;

    private int cacheBlockSize = -1;
    private int maxCacheBlocks = -1;

    private Map<Long, byte[]> cache = new HashMap<Long, byte[]>();
    private LinkedList<Long> index = new LinkedList<Long>();

    public S3RandomAccessFile(AmazonS3 client, String url) throws IOException {
        this(client, url, DEFAULT_S3_BUFFER_SIZE);
    }

    public S3RandomAccessFile(AmazonS3 client, String url, int bufferSize) throws IOException {
        this(client, url, bufferSize, DEFAULT_MAX_CACHE_SIZE);
    }

    public S3RandomAccessFile(AmazonS3 client, String url, int bufferSize, int maxCacheSize) throws IOException {
        super(bufferSize);
        this.file = null;
        this.location = url;

        // Only enable cache if given size is at least twice the buffer size
        if (maxCacheSize >= 2*bufferSize) {
            this.cacheBlockSize = 2*bufferSize;
            this.maxCacheBlocks = maxCacheSize / this.cacheBlockSize;
        }
        else {
            this.cacheBlockSize = this.maxCacheBlocks = -1;
        }

        this.client = client;
        this.uri = new AmazonS3URI(url);
        this.bucket = uri.getBucket();
        this.key = uri.getKey();
        this.metadata = client.getObjectMetadata(bucket, key);
    }

    public void close() throws IOException {
        cache.clear();
        index.clear();
    }

    /**
     * After execution of this function, the given block is guranteed to
     * be in the cache.
     */
    private void ensure(Long key) throws IOException {
        if (!cache.containsKey(key)) {
            long position = key.longValue() * cacheBlockSize;
            int toEOF = (int)(length() - position);
            int bytes = toEOF < cacheBlockSize ? toEOF : cacheBlockSize;
            byte[] buffer = new byte[bytes];

            read__(position, buffer, 0, cacheBlockSize);
            cache.put(key, buffer);
            index.add(key);
            assert(cache.size() == index.size());
            while(cache.size() > maxCacheBlocks) {
                long id = index.removeFirst();
                cache.remove(id);
            }

            return;
        }
    }

    /**
     * Read directly from S3 [1], without going through the buffer.
     * All reading goes through here or readToByteChannel;
     *
     * 1. https://docs.aws.amazon.com/AmazonS3/latest/dev/RetrievingObjectUsingJava.html
     *
     * @param pos    start here in the file
     * @param buff   put data into this buffer
     * @param offset buffer offset
     * @param len    this number of bytes
     * @return actual number of bytes read
     * @throws IOException on io error
     */
    @Override
    protected int read_(long pos, byte[] buff, int offset, int len) throws IOException {
        if (!(cacheBlockSize > 0) || !(maxCacheBlocks > 0)) {
            return read__(pos, buff, offset, len);
        }

        long start = pos / cacheBlockSize;
        long end = (pos+len-1) / cacheBlockSize;

        if (pos >= length()) { // Do not read past end of the file
            return 0;
        }
        else if (end - start > 1) { // If the request touches more than two cache blocks, punt (should never happen)
            return read__(pos, buff, offset, len);
        }
        else if (end - start == 1) { // If the request touches two cache blocks, split it
            int length1 = (int)((end*cacheBlockSize) - pos);
            int length2 = (int)((pos+len) - (end*cacheBlockSize));
            return read_(pos, buff, offset, length1) + read_(pos+length1, buff, offset+length1, length2);
        }

        // Service a request that touches only one cache block
        Long key = new Long(start);
        ensure(key);

        byte[] src = (byte[])cache.get(key);
        int srcPos = (int)(pos - (key.longValue() * cacheBlockSize));
        int toEOB = src.length - srcPos;
        int length = toEOB < len ? toEOB : len;
        System.arraycopy(src, srcPos, buff, offset, length);

        return len;
    }

    private int read__(long pos, byte[] buff, int offset, int len) throws IOException {
        GetObjectRequest rangeObjectRequest = new GetObjectRequest(bucket, key);
        rangeObjectRequest.setRange(pos, pos+len-1);

        S3Object objectPortion = client.getObject(rangeObjectRequest);
        InputStream objectData = objectPortion.getObjectContent();
        int bytes = 0;
        int totalBytes = 0;

        bytes = objectData.read(buff, offset + totalBytes, len - totalBytes);
        while ((bytes > 0) && ((len - totalBytes) > 0)) {
            totalBytes += bytes;
            bytes = objectData.read(buff, offset + totalBytes, len - totalBytes);
        }

        objectData.close();
        objectPortion.close();

        return totalBytes;
    }

    @Override
    public long readToByteChannel(WritableByteChannel dest, long offset, long nbytes) throws IOException {
        int n = (int) nbytes;
        byte[] buff = new byte[n];
        int done = read_(offset, buff, 0, n);
        dest.write(ByteBuffer.wrap(buff));
        return done;
    }

    @Override
    public long length() throws IOException {
        return metadata.getContentLength();
    }

    @Override
    public long getLastModified() {
        return metadata.getLastModified().getTime();
    }

    public Map<Long, byte[]> getCache() {
        return cache;
    }

    public LinkedList<Long> getIndex() {
        return index;
    }

    public ObjectMetadata getMetadata() {
        return metadata;
    }
}
