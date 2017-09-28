package cn.edu.pku.sei.sc.nlp.task;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

/**
 * Created by Icing on 2017/9/27.
 */
@Getter
@Setter
public class DictEntity {

    private String name;

    private boolean possible;

    private Set<String> wordSet;

}
