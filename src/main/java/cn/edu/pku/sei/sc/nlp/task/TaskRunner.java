package cn.edu.pku.sei.sc.nlp.task;

import cn.edu.pku.sei.sc.nlp.task.dict.Dictionary;
import opennlp.tools.sentdetect.SentenceDetector;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

import java.io.File;
import java.util.*;

/**
 * Created by Icing on 2017/9/27.
 */
public class TaskRunner implements Runnable {

    private File textFile;

    private ThreadLocal<SentenceDetector> sentenceDetector = ThreadLocal.withInitial(TaskRunner::initSentenceDetector);

    private ThreadLocal<Tokenizer> tokenizer = ThreadLocal.withInitial(TaskRunner::initTokenizer);

    private static SentenceDetector initSentenceDetector() {
        SentenceModel sentenceModel = Utils.loadModel(SentenceModel::new, "model/en-sent.bin");
        return new SentenceDetectorME(sentenceModel);
    }

    private static Tokenizer initTokenizer() {
        TokenizerModel tokenizerModel = Utils.loadModel(TokenizerModel::new, "model/en-token.bin");
        return new TokenizerME(tokenizerModel);
    }

    private static final List<String> ignoreLines = Arrays.asList(TaskApplication.ignoreLines);

    private static final List<String> stopLines = Arrays.asList(TaskApplication.stopLines);

    public TaskRunner(File textFile) {
        this.textFile = textFile;
    }

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();

        StringBuilder stringBuilder = new StringBuilder();
        try (Scanner scanner = new Scanner(textFile)) {
            while(scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (stopLines.contains(line))
                    break;
                if (!ignoreLines.contains(line))
                    stringBuilder.append(line);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        String text = stringBuilder.toString();

        DocResult result = new DocResult();
        result.setName(textFile.getName());
        result.setWordCounts(new HashMap<>());

        //分句
        String[] sentences = sentenceDetector.get().sentDetect(text);
        for (String sentence : sentences) {
            String[] tokens = tokenizer.get().tokenize(sentence);
            Set<String> wordSet = new HashSet<>();
            wordSet.addAll(Arrays.asList(tokens));

            Set<String> entitySet = Dictionary.getLocal().lookUp(wordSet);
            for (String entity : entitySet) {
                Integer count = result.getWordCounts().get(entity);
                if (count == null)
                    count = 0;
                result.getWordCounts().put(entity, count + 1);
            }
        }

        try {
            TaskApplication.results.put(result);
            int left = TaskApplication.nUnfinished.decrementAndGet();
            double percent = (1.0 - ((double)left/ (double)TaskApplication.nTotal)) * 100;
            String per = String.format("%.2f", percent);
            String status = Thread.currentThread().getName() + ">\t" + per + "% Processing..." + left + " document(s) left. ";
            String time = "total: " + (System.currentTimeMillis() - TaskApplication.startTime) / 1000.0 + "s. " + (System.currentTimeMillis() - startTime) + "ms used for " + result.getName();
            status += time;
            System.out.println(status);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
