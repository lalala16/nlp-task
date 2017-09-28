package cn.edu.pku.sei.sc.nlp.task;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * Created by Icing on 2017/9/28.
 */
@Getter
@Setter
public class DocResult {

    private String name;

    private Map<String, Integer> wordCounts;

}
