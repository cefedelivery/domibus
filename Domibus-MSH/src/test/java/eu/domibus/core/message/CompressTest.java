package eu.domibus.core.message;

import mockit.integration.junit4.JMockit;
import org.apache.commons.compress.compressors.brotli.BrotliCompressorInputStream;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.meteogroup.jbrotli.Brotli;
import org.meteogroup.jbrotli.io.BrotliOutputStream;
import org.meteogroup.jbrotli.libloader.BrotliLibraryLoader;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
@RunWith(JMockit.class)
public class CompressTest {

    @Test
    public void splitFile() throws IOException {
        RandomAccessFile raf = new RandomAccessFile("c:/DEV/_work/compression/compressed", "r");
//        RandomAccessFile raf = new RandomAccessFile("c:/DEV/_work/compression/test-e38f79d9-e3c9-4639-a08a-b8f782c99d44", "r");
        long numSplits = 3; //from user input, extract it from args
        long sourceSize = raf.length();
        long bytesPerSplit = sourceSize / numSplits;
        long remainingBytes = sourceSize % numSplits;

        int maxReadBufferSize = 8 * 1024; //8KB
        for (int destIx = 1; destIx <= numSplits; destIx++) {
            BufferedOutputStream bw = new BufferedOutputStream(new FileOutputStream("c:/DEV/_work/compression/split." + destIx));
            if (bytesPerSplit > maxReadBufferSize) {
                long numReads = bytesPerSplit / maxReadBufferSize;
                long numRemainingRead = bytesPerSplit % maxReadBufferSize;
                for (int i = 0; i < numReads; i++) {
                    readWrite(raf, bw, maxReadBufferSize);
                }
                if (numRemainingRead > 0) {
                    readWrite(raf, bw, numRemainingRead);
                }
            } else {
                readWrite(raf, bw, bytesPerSplit);
            }
            bw.close();
        }
        if (remainingBytes > 0) {
            BufferedOutputStream bw = new BufferedOutputStream(new FileOutputStream("c:/DEV/_work/compression/split." + (numSplits + 1)));
            readWrite(raf, bw, remainingBytes);
            bw.close();
        }
        raf.close();
    }

    public static void mergeFiles(List<File> files, OutputStream mergingStream)
            throws IOException {

            for (File f : files) {
                Files.copy(f.toPath(), mergingStream);
            }

    }

    @Test
    public void joinFiles() throws IOException {
        File output = new File("c:/DEV/_work/compression/joined");

        try (OutputStream bw = new BufferedOutputStream(Files.newOutputStream(output.toPath()))) {
            findAndAppend(new File("c:/DEV/_work/compression"), bw);
        } catch (IOException exp) {
            exp.printStackTrace();
        }
    }

    public static void findAndAppend(File parent, OutputStream bw) throws IOException {

        // Find any matching files...
        File files[] = parent.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().contains("split");
            }
        });

        mergeFiles(Arrays.asList(files), bw);
    }


    protected void readWrite(RandomAccessFile raf, BufferedOutputStream bw, long numBytes) throws IOException {
        byte[] buf = new byte[(int) numBytes];
        int val = raf.read(buf);
        if (val != -1) {
            bw.write(buf);
        }
    }

    //working
    @Test
    public void testCompression3() throws IOException {
        BrotliLibraryLoader.loadBrotli();

        final Path toCompress = Paths.get("c:/DEV/_work/compression/test-e38f79d9-e3c9-4639-a08a-b8f782c99d44");
//        final Path toCompress = Paths.get("c:/DEV/_work/test-da97014e-6515-4bcf-bd07-35df52431edb");
        final InputStream inputStream = new BufferedInputStream(Files.newInputStream(toCompress));

        final Path compressed = Paths.get("c:/DEV/_work/compression/compressed");
        final OutputStream outputStream = Files.newOutputStream(compressed);
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
        Brotli.Parameter parameter = Brotli.DEFAULT_PARAMETER.setQuality(4);
        BrotliOutputStream brotliOutputStream = new BrotliOutputStream(bufferedOutputStream, parameter);

        byte[] chunk = new byte[8192];
        int chunkLen = 0;
        int offset = 0;
        while ((chunkLen = inputStream.read(chunk)) != -1) {
            for (int i : chunk) {
                brotliOutputStream.write(i);
            }
        }

        brotliOutputStream.flush();
        brotliOutputStream.close();

    }

    @Test
    public void testCommonDecompress() throws IOException {
//        InputStream fin = Files.newInputStream(Paths.get("c:/DEV/_work/compression/compressed"));
        InputStream fin = Files.newInputStream(Paths.get("c:/DEV/_work/compression/joined"));
        BufferedInputStream in = new BufferedInputStream(fin);
        OutputStream out = Files.newOutputStream(Paths.get("c:/DEV/_work/compression/decompressed"));
        BrotliCompressorInputStream brIn = new BrotliCompressorInputStream(in);
        final byte[] buffer = new byte[1024];
        int n = 0;
        while (-1 != (n = brIn.read(buffer))) {
            out.write(buffer, 0, n);
        }
        out.close();
        brIn.close();
    }

//    @Test
//    public void testCompression() throws IOException {
//        BrotliLibraryLoader.loadBrotli();
//
//        final Path toCompress = Paths.get("c:/DEV/_work/compression/test-e38f79d9-e3c9-4639-a08a-b8f782c99d44");
////        final Path toCompress = Paths.get("c:/DEV/_work/test-da97014e-6515-4bcf-bd07-35df52431edb");
//        final InputStream inputStream = new BufferedInputStream(Files.newInputStream(toCompress));
//
//        final Path compressed = Paths.get("c:/DEV/_work/compression/compressed");
//        final OutputStream outputStream = Files.newOutputStream(compressed);
//        Brotli.Parameter parameter = Brotli.DEFAULT_PARAMETER.setQuality(4);
//        BrotliOutputStream brotliOutputStream = new BrotliOutputStream(outputStream, parameter);
//
//        byte[] chunk = new byte[1024];
//        int chunkLen = 0;
//        int offset = 0;
//        while ((chunkLen = inputStream.read(chunk)) != -1) {
////            brotliOutputStream.write(chunk, offset, chunkLen);
//            brotliOutputStream.write(chunk);
//            chunk = new byte[1024];
//        }
//
//      /*  BrotliStreamCompressor brotliStreamCompressor = new BrotliStreamCompressor(parameter);
//        final byte[] bytes = brotliStreamCompressor.compressArray(new byte[0], 0, 0, true);
//        outputStream.write(bytes);*/
//
////        brotliOutputStream.finish();
//        brotliOutputStream.close();
//    }

/*
    @Test
    public void testCompression1() throws IOException {
        BrotliLibraryLoader.loadBrotli();

        BrotliCompressor brotliCompressor = new BrotliCompressor();
        Brotli.Parameter parameter = Brotli.DEFAULT_PARAMETER.setQuality(4);

        final Path toDecompress = Paths.get("c:/DEV/_work/compression/test-e38f79d9-e3c9-4639-a08a-b8f782c99d44");
        final InputStream bufferedInputStreaminputStream = new BufferedInputStream(Files.newInputStream(toDecompress));
        final byte[] in = IOUtils.toByteArray(bufferedInputStreaminputStream);
        final byte[] out = new byte[in.length];


        brotliCompressor.compress(parameter, in, out);


        BrotliDeCompressor brotliDeCompressor = new BrotliDeCompressor();
        final byte[] outFinal = new byte[out.length];
        brotliDeCompressor.deCompress(out, outFinal);
    }

    @Test
    public void testDecompression() throws IOException {
        BrotliLibraryLoader.loadBrotli();

        final Path toDecompress = Paths.get("c:/DEV/_work/compression/compressed");
        final InputStream bufferedInputStreaminputStream = new BufferedInputStream(Files.newInputStream(toDecompress));
        final BrotliInputStream inputStream = new BrotliInputStream(bufferedInputStreaminputStream);

        final Path deCompressed = Paths.get("c:/DEV/_work/compression/decompressed");
        final OutputStream outputStream = Files.newOutputStream(deCompressed);

        byte[] chunk = new byte[1024];
        int chunkLen = 0;
        int offset = 0;
        while ((chunkLen = inputStream.read(chunk)) != -1) {
            outputStream.write(chunk, 0, chunkLen);
            chunk = new byte[1024];
        }


        outputStream.flush();
        outputStream.close();
    }

    @Test
    public void testDecompression1() throws IOException {
        BrotliLibraryLoader.loadBrotli();

        final Path toDecompress = Paths.get("c:/DEV/_work/compression/compressed");
        final InputStream bufferedInputStreaminputStream = new BufferedInputStream(Files.newInputStream(toDecompress));
        BrotliDeCompressor brotliDeCompressor = new BrotliDeCompressor();
        final byte[] in = IOUtils.toByteArray(bufferedInputStreaminputStream);
        final byte[] out = new byte[in.length];
        brotliDeCompressor.deCompress(in, out);
    }
*/

}
