package cn.edu.pku.sei.sc.nlp.task.dict;

import cn.edu.pku.sei.sc.nlp.task.DictEntity;
import cn.edu.pku.sei.sc.nlp.task.DictWord;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.Reader;
import java.util.*;

/**
 * Created by Icing on 2017/9/27.
 */
public class Dictionary {

    private static Dictionary global;

    private static ThreadLocal<Dictionary> local = new ThreadLocal<>();

    public static Dictionary getLocal() {
        if (local.get() == null)
            local.set(new Dictionary(getGlobal()));
        return local.get();
    }

    public static Dictionary getGlobal() {
        if (global == null) {
            synchronized (Dictionary.class) {
                if (global == null)
                    global = new Dictionary();
            }
        }
        return global;
    }

    private List<DictEntity> entities; //字典里的所有实体

    private List<DictWord> words; //字典里的所有词语

    private int nLeft;

    private static final int eliminateTarget = 50; //剩余实体数小于此值后开始完整查询

    private static final String dictPath = "dict/entity_dict.csv";

    public Dictionary() {
        entities = new ArrayList<>();
        words = new ArrayList<>();
        Map<String, DictWord> wordMap = new HashMap<>();

        try (Reader reader = new FileReader(dictPath)) {
            Iterable<CSVRecord> records = CSVFormat.DEFAULT.parse(reader);

            for (CSVRecord record : records) {
                DictEntity dictEntity = new DictEntity();
                dictEntity.setPossible(true);
                dictEntity.setName(record.get(0));
                dictEntity.setWordSet(new HashSet<>());

                String[] entityWords = record.get(1).split(" ");
                for (String entityWord : entityWords) {
                    dictEntity.getWordSet().add(entityWord);
                    DictWord dictWord = wordMap.get(entityWord);
                    if (dictWord == null) {
                        dictWord = new DictWord();
                        dictWord.setName(entityWord);
                        dictWord.setSupports(new ArrayList<>());
                        words.add(dictWord);
                        wordMap.put(entityWord, dictWord);
                    }
                    dictWord.getSupports().add(dictEntity);
                }

                entities.add(dictEntity);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        words.sort(Comparator.comparingInt(w -> - w.getSupports().size()));
        nLeft = entities.size();
    }

    public Dictionary(Dictionary src) {
        entities = new ArrayList<>();
        words = new ArrayList<>();
        Map<String, DictEntity> entityMap = new HashMap<>();

        for (DictEntity entity : src.entities) {
                DictEntity dictEntity = new DictEntity();
                dictEntity.setName(entity.getName());
                dictEntity.setWordSet(entity.getWordSet());
                dictEntity.setPossible(entity.isPossible());
                entityMap.put(entity.getName(), dictEntity);
                entities.add(dictEntity);
            }

            for (DictWord word : src.words) {
                DictWord dictWord = new DictWord();
                dictWord.setName(word.getName());
                dictWord.setSupports(new ArrayList<>());
                for (DictEntity entity : word.getSupports())
                dictWord.getSupports().add(entityMap.get(entity.getName()));
            words.add(dictWord);
        }
        nLeft = entities.size();
    }

    public Set<String> lookUp(Set<String> wordSet) {
        for (DictWord word : words) {
            if (eliminateTarget >= nLeft) //达到排除目标
                break;

            if (!wordSet.contains(word.getName())) { //不包含该单词
                for (DictEntity dictEntity : word.getSupports()) {
                    if (dictEntity.isPossible()) {
                        dictEntity.setPossible(false);
                        nLeft--;
                    }
                }
            }
        }

        Set<String> res = new HashSet<>();
        for (DictEntity entity : entities) {
            if (entity.isPossible() && wordSet.containsAll(entity.getWordSet()))
                res.add(entity.getName());
            else
                entity.setPossible(true);
        }
        nLeft = entities.size();
        return res;
    }

}
