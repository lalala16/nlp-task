package cn.edu.pku.sei.sc.nlp.task;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Icing on 2017/9/27.
 */
public class TaskApplication {

    public static String[] ignoreLines = new String[] {"==== Front", "Background", "Methods",
            "Results", "Conclusions", "==== Body",
            "Authors' contributions", "Click here for file",
            "Acknowledgements"};

    public static String[] stopLines = new String[] {"Figures and Tables", "==== Refs"};

    private static final String outputPath = "result/";

    private static final String outputPrefix = "jack";

    private final static String NEW_LINE_SEPARATOR="\n";

    private static final int poolSize = 40;

    private static final ExecutorService executorService = Executors.newFixedThreadPool(poolSize);

    public static BlockingQueue<DocResult> results = new LinkedBlockingQueue<>();

    public static AtomicInteger nUnfinished = new AtomicInteger();

    public static int nTotal = 0;

    public static long startTime = 0;

    private static int outputCount = 0;

    private static final int singleFileLength = 10000;

    private static void output() {
        List<DocResult> buffer = new ArrayList<>();

        while(nUnfinished.get() != 0) {
            try {
                DocResult temp = results.poll(2000, TimeUnit.MILLISECONDS);
                if (temp == null)
                    continue;
                buffer.add(temp);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if (buffer.size() >= singleFileLength) {
                outputToFile(buffer);
                buffer.clear();
            }
        }

        if (!buffer.isEmpty())
            outputToFile(buffer);
        System.out.println("Completed!");
    }

    private static void outputToFile(List<DocResult> buffer) {
        outputCount ++;

        List<String[]> data = new ArrayList<>();
        for (DocResult docResult : buffer) {
            String docName = docResult.getName();
            StringBuilder stringBuilder = new StringBuilder();
            int wordCount = 0;
            int wordKinds = docResult.getWordCounts().size();

            for (Map.Entry<String, Integer> entry : docResult.getWordCounts().entrySet()) {
                wordCount += entry.getValue();
                stringBuilder.append(entry.getKey()).append(":").append(entry.getValue()).append(" ");
            }

            String[] line = new String[] {docName, String.valueOf(wordKinds), String.valueOf(wordCount), stringBuilder.toString()};
            data.add(line);
        }

        try {
            String fileName = outputPath + outputPrefix + "_" + outputCount + ".csv";
            FileWriter fileWriter = new FileWriter(fileName);
            CSVFormat csvFormat = CSVFormat.DEFAULT.withRecordSeparator(NEW_LINE_SEPARATOR);
            CSVPrinter printer=new CSVPrinter(fileWriter, csvFormat);

            for (String[] datum : data)
                printer.printRecord((Object[]) datum);

            printer.flush();
            printer.close();

            System.out.println("输出部分结果到：" + fileName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }



    public static void main(String[] args) throws IOException, InterruptedException {

        File docs = new File("docs");
        List<File> texts = Utils.scanFileRecursively(docs, Utils.TXT_FILE_FILTER);
        nUnfinished.set(texts.size());
        nTotal = texts.size();
        startTime = System.currentTimeMillis();
        executorService.submit(TaskApplication::output);
        for (File text : texts) {
            executorService.submit(new TaskRunner(text));
        }
        executorService.shutdown();
        while(!executorService.isTerminated()) {
            executorService.awaitTermination(1000, TimeUnit.MILLISECONDS);
        }

        System.out.println("Total time: " + (System.currentTimeMillis() - startTime) + "ms.");
    }

}
