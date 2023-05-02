package org.openmbee.plugin.cfgmgmt.ui.impl;

import org.openmbee.plugin.cfgmgmt.constants.ExceptionConstants;
import org.openmbee.plugin.cfgmgmt.exception.ConfigurationManagementRuntimeException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

public abstract class AbstractBaseControllerFactory {
    private List<Object> expectedParameters;

    public List<Object> getExpectedParameters() {
        return expectedParameters;
    }

    protected void setExpectedParameters(List<Object> expectedParameters) {
        this.expectedParameters = expectedParameters;
    }

    protected Object handleCall(Class<?> classType) {
        try {
            for(Constructor<?> constructor : classType.getConstructors()) {
                if(constructor.getParameterCount() > 0 && hasValidParameterAmountAndTypes(constructor.getParameterTypes())) {
                    return createObjectWithExpectedArgs(classType, constructor.getParameterTypes());
                }
            }

            return classType.getConstructor().newInstance(); // default
        } catch(Exception e) {
            throw new ConfigurationManagementRuntimeException(ExceptionConstants.ERROR_DURING_FXML_CONSTRUCTOR_INSTANTIATION + e.getMessage());
        }
    }

    protected boolean hasValidParameterAmountAndTypes(Class<?>[] constructorParameters) {
        if(constructorParameters.length == getExpectedParameters().size()) {
            for (Object expectedParameter : getExpectedParameters()) {
                boolean hasCurrentExpectedParameter = false;
                for (Class<?> constructorParameterType : constructorParameters) {
                    if (sameClassOrInterface(expectedParameter, constructorParameterType)) {
                        hasCurrentExpectedParameter = true;
                        break;
                    }
                }

                if (!hasCurrentExpectedParameter) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    protected boolean sameClassOrInterface(Object expectedParameter, Class<?> constructorParameterType) {
        return expectedParameter.getClass() == constructorParameterType ||
                Arrays.stream(expectedParameter.getClass().getInterfaces()).anyMatch(i -> i == constructorParameterType);
    }

    protected Object createObjectWithExpectedArgs(Class<?> classType, Class<?>[] constructorParameterTypes)
            throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        return classType.getDeclaredConstructor(constructorParameterTypes).newInstance(getParameterArray());
    }

    protected Object[] getParameterArray() {
        return getExpectedParameters().toArray(Object[]::new);
    }
}
