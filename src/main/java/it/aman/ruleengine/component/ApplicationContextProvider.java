package it.aman.ruleengine.component;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class ApplicationContextProvider implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public ApplicationContext getContext() {
        return applicationContext;
    }

    /**
     * Returns the Spring managed bean instance of the given class type if it exists.
     * Returns null otherwise.
     * @param beanClass
     * @return
     */
    public <T extends Object> T getBean(Class<T> beanClass) {
        return applicationContext.getBean(beanClass);
    }
}