package cn.edu.pku.sei.sc.nlp.task;

/**
 * Created by Icing on 2017/9/27.
 */
@FunctionalInterface
public interface Loader<T, R> {

    R load(T t) throws Exception;

}
