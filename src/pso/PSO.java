package pso;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.Calendar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class PSO {
    static final Integer numThreads = 10;

    public static void main(String[] args) {
        Thread[] pool = new Thread[numThreads];
        List<String> results = Collections.synchronizedList(new ArrayList<String>());

        for (int i = 0; i < pool.length; i++ ) {
            pool[i] = new Thread(new AlphaSwarm(results));
            pool[i].start();
        }

        boolean stillAlive = true;

        while(stillAlive) {
            stillAlive = false;
            for (int j = 0; j < pool.length; j++) {
                if (pool[j].isAlive()) {
                    stillAlive = true;
                    break;
                }
            }

            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        Path file = Paths.get(getDate().toString());
        BufferedWriter writer;

        try {
            writer = Files.newBufferedWriter(file, Charset.forName("UTF-8"));
            synchronized(results) {
                Iterator<String> i = results.iterator();
                while(i.hasNext()) {
                    String res = i.next();
                    writer.write(res, 0, res.length());
                    writer.newLine();
                }
            }
            writer.close();
        } catch (IOException e) {
             System.err.format("IOException: %s%n", e);
        }
    }

    public static Date getDate() {
        Calendar cal = Calendar.getInstance();
        return cal.getTime();
    }
}
