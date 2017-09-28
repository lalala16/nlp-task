package cn.edu.pku.sei.sc.nlp.task;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Created by Icing on 2017/9/27.
 */
@Getter
@Setter
public class DictWord {

    private String name;

    private List<DictEntity> supports;

}
