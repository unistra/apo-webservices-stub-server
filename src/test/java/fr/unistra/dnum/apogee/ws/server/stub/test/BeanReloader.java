package fr.unistra.dnum.apogee.ws.server.stub.test;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.TestContext;
import org.springframework.util.function.ThrowingConsumer;

import java.util.Map;

class BeanReloader {
    private final ConfigurableListableBeanFactory beanFactory;

    BeanReloader(ConfigurableListableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    static BeanReloader from(TestContext testContext) {
        if (testContext.getApplicationContext() instanceof ConfigurableApplicationContext applicationContext)
            return new BeanReloader(applicationContext.getBeanFactory());
        else throw new IllegalStateException("No configurable application context");
    }

    <T> void doWithBean(Class<T> type, ThrowingConsumer<? super T> action) throws Exception {
        for (Map.Entry<String, T> entry : beanFactory.getBeansOfType(type).entrySet()) {
            action.acceptWithException(entry.getValue());
            reloadDependentBeans(entry.getKey());
        }
    }

    void reloadDependentBeans(String beanName) throws Exception {
        reloadDependentBeans(beanName, () -> {});
    }

    void reloadDependentBeans(String beanName, InitializingBean bean) throws Exception {
        bean.afterPropertiesSet();
        for (String dependentBeanName : beanFactory.getDependentBeans(beanName))
            if (beanFactory.getBean(dependentBeanName) instanceof InitializingBean dependentBeanBean)
                reloadDependentBeans(dependentBeanName, dependentBeanBean);
    }
}
