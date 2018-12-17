package eu.domibus.core.message;

import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.*;
import java.nio.file.Files;
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

    @Test
    public void testSplit() {
        long sourceSize = 419430400;
        long mbInBytes = 1048576;
        long fragmentSizeInMB = 500;
        long fragmentSizeInBytes = fragmentSizeInMB * mbInBytes;
        System.out.println("fragmentSizeInBytes=" + fragmentSizeInBytes);

        long numberOfFragments = sourceSize / fragmentSizeInBytes;
        System.out.println("numberOfFragments=" + numberOfFragments);
        long remainingFragment = sourceSize % fragmentSizeInBytes;
        System.out.println("remainingFragment=" + remainingFragment);
        long totalNumberOfFragments = numberOfFragments;
        if(remainingFragment > 0) {
            totalNumberOfFragments = numberOfFragments + 1;
        }


        System.out.println("totalNumberOfFragments=" + totalNumberOfFragments);
    }
}