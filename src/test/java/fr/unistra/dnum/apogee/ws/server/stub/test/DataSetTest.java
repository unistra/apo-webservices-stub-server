package fr.unistra.dnum.apogee.ws.server.stub.test;

import org.springframework.test.context.TestExecutionListeners;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@Documented
@TestExecutionListeners(listeners = TestDataSetTestExecutionListener.class, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
public @interface DataSetTest {
}
