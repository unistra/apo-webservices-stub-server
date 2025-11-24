package fr.unistra.dnum.apogee.ws.server.stub.test;

import fr.unistra.dnum.apogee.ws.server.stub.infrastructure.DataSet;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

import java.nio.charset.StandardCharsets;

public class TestDataSetTestExecutionListener extends AbstractTestExecutionListener {
    @Override
    public void beforeTestClass(TestContext testContext) throws Exception {
        TestDataSet testData = testContext.getTestClass().getAnnotation(TestDataSet.class);
        if (testData != null)
            before(testContext, toResource(testData, testContext.getTestClass().getSimpleName()));
    }

    @Override
    public void afterTestClass(TestContext testContext) throws Exception {
        TestDataSet testData = testContext.getTestClass().getAnnotation(TestDataSet.class);
        if (testData != null)
            after(testContext);
    }

    @Override
    public void beforeTestMethod(TestContext testContext) throws Exception {
        TestDataSet testData = testContext.getTestMethod().getAnnotation(TestDataSet.class);
        if (testData != null)
            before(testContext, toResource(testData, testContext.getTestMethod().getName()));
    }

    @Override
    public void afterTestMethod(TestContext testContext) throws Exception {
        TestDataSet testData = testContext.getTestMethod().getAnnotation(TestDataSet.class);
        if (testData != null)
            after(testContext);
    }

    private ByteArrayResource toResource(TestDataSet testData, String description) {
        return new ByteArrayResource(testData.value().getBytes(StandardCharsets.UTF_8), description) {
            @Override
            public String getDescription() {
                return "@TestDataSet in " + description;
            }
        };
    }

    private void before(TestContext testContext, Resource resource) throws Exception {
        BeanReloader.from(testContext).doWithBean(DataSet.class,
                dataset -> dataset.load(resource));
    }

    private void after(TestContext testContext) throws Exception {
        BeanReloader.from(testContext).doWithBean(DataSet.class,
                DataSet::close);
    }

}
