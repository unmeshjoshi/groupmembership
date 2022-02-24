package org.distrib.patterns.common;

import org.distrib.patterns.net.InetAddressAndPort;
import org.distrib.patterns.net.Networks;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;

import static org.junit.Assert.fail;

public class TestUtils {
    private static Random random = new Random();
    public static File tempDir(String prefix) {
        var ioDir = System.getProperty("java.io.tmpdir");
        var f = new File(ioDir, prefix + random.nextInt(1000000));
        f.mkdirs();
        f.deleteOnExit();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                Files.walkFileTree(f.toPath(), new SimpleFileVisitor< Path >() {
                    @Override
                    public FileVisitResult visitFileFailed(Path path, IOException exc) throws IOException {
                        // If the root path did not exist, ignore the error; otherwise throw it.
                        if (exc instanceof NoSuchFileException && path.toFile().equals(f))
                            return FileVisitResult.TERMINATE;
                        throw exc;
                    }

                    @Override
                    public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
                        Files.delete(path);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path path, IOException exc) throws IOException {
                        if (exc != null) {
                            throw exc;
                        }

                        ;
                        List filesToKeep = new ArrayList<>();
                        if (!filesToKeep.contains(path.toFile())) {
                            Files.delete(path);
                        }

                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));
        return f;
    }

    public static void waitUntilTrue(Callable<Boolean> predicate, String msg,
                                     Duration waitTime) {
        try {
            var startTime = System.nanoTime();
            while (true) {
                if (predicate.call())
                    return;

                if (System.nanoTime() > (startTime + waitTime.toNanos())) {
                    fail(msg);
                }

                Thread.sleep(100);
            }
        } catch (Exception e) {
            // should never hit here
            throw new RuntimeException(e);
        }
    }

    //This seems to the right way to get random port for tests.
    //But Kafka had an issue https://issues.apache.org/jira/browse/KAFKA-1501
    //TODO: Figure out if there is any issue with this.
    public static int getRandomPort() throws RuntimeException {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(0);
            return serverSocket.getLocalPort();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {

                }
            }
        }
    }

    public static InetAddressAndPort randomAddress() {
        InetAddress inetAddress = new Networks().ipv4Address();
        return InetAddressAndPort.create(inetAddress.getHostAddress(), TestUtils.getRandomPort());
    }
}
