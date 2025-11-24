package fr.unistra.dnum.apogee.ws.server.stub.domaine;

import org.springframework.util.StringUtils;

import java.text.Normalizer;
import java.util.*;
import java.util.function.Function;
import java.util.function.IntFunction;

import static java.lang.reflect.Array.newInstance;

public class DomainUtils {
    private DomainUtils() { /*_*/ }

    @SafeVarargs
    public static <T> T[] lasts(Function<T,String> keyFn, T... array) {
        String lastKey = "";
        LinkedList<T> lastValues = new LinkedList<>();
        for(T t : array) {
            if (t == null) continue;
            String key = keyFn.apply(t);
            if(key == null || key.compareTo(lastKey) > 0) {
                lastKey = key;
                lastValues.clear();
                lastValues.add(t);
            } else if(key.compareTo(lastKey) == 0) {
                lastValues.add(t);
            }
        }
        return lastValues.toArray(arrayGenerator(array));
    }

    @SuppressWarnings("unchecked")
    private static <T> IntFunction<T[]> arrayGenerator(T[] otherArray) {
        return size -> (T[]) newInstance(otherArray.getClass().getComponentType(), size);
    }

    @SafeVarargs
    public static <T> Optional<T> last(Function<T,String> keyFn, T... array) {
        return first(lasts(keyFn, array));
    }

    @SafeVarargs
    private static <T> Optional<T> first(T... lasts) {
        return lasts.length > 0 ? Optional.of(lasts[0]) : Optional.empty();
    }

    public static String unaccent(String value) {
        return value == null ? null : Normalizer
                .normalize(value, Normalizer.Form.NFKD)
                .replaceAll("\\p{M}", "");
    }

    public static boolean isEmpty(String... list) {
        return isEmpty(Arrays.asList(list));
    }

    public static boolean isEmpty(List<String> list) {
        return list == null
                || list.stream().noneMatch(StringUtils::hasText);
    }

    public static List<String> toList(String... values) {
        return Arrays.stream(values)
                .filter(StringUtils::hasText)
                .toList();
    }
}
