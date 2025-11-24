package fr.unistra.dnum.apogee.ws.test;

import org.assertj.core.api.InstanceOfAssertFactory;
import org.assertj.core.api.ListAssert;

import java.util.List;
import java.util.function.Function;

public class AssertUtil {
    private AssertUtil() { /*_*/ }

    @SuppressWarnings("unchecked")
    public static <T,E> InstanceOfAssertFactory<Object, ListAssert<E>> tableau(Function<T,List<E>> item) {
        return new InstanceOfAssertFactory<>(Object.class,
                tableau -> ListAssert.assertThatList(tableau != null ? item.apply((T) tableau) : null)
        );
    }

}
