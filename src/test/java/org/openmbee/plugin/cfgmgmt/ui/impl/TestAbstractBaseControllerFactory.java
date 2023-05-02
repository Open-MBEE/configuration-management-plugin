package org.openmbee.plugin.cfgmgmt.ui.impl;

import org.openmbee.plugin.cfgmgmt.constants.ExceptionConstants;
import org.openmbee.plugin.cfgmgmt.controller.CmControllerFactory;
import org.openmbee.plugin.cfgmgmt.model.CmControllerSettings;
import org.openmbee.plugin.cfgmgmt.rules.JavaFxThreadingRule;
import org.openmbee.plugin.cfgmgmt.ui.IUIFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class TestAbstractBaseControllerFactory {
    private AbstractBaseControllerFactory abstractBaseControllerFactory;
    private List<Object> expectedParameters;

    @Rule
    public JavaFxThreadingRule rule = new JavaFxThreadingRule();

    @Before
    public void setUp() {
        expectedParameters = new ArrayList<>();
        expectedParameters.add(mock(CmControllerSettings.class));

        abstractBaseControllerFactory = Mockito.spy(new CmControllerFactory());

        doReturn(expectedParameters).when(abstractBaseControllerFactory).getExpectedParameters();
    }

    protected static class ClassWithoutDefaultConstructor {
        public ClassWithoutDefaultConstructor(Integer arg1, Integer arg2) {}
    }
    protected static class ClassWithZeroArgConstructor {
        public ClassWithZeroArgConstructor() {}
    }
    protected static class ClassWithCorrectNumberOfArgsButWrongTypes {
        public ClassWithCorrectNumberOfArgsButWrongTypes() {}
        public ClassWithCorrectNumberOfArgsButWrongTypes(Integer arg1) {}
    }
    protected static class ClassWithCorrectNumberAndTypesOfArgs {
        public ClassWithCorrectNumberAndTypesOfArgs() {}
        public ClassWithCorrectNumberAndTypesOfArgs(CmControllerSettings arg1) {}
    }

    @Test
    public void call_noDefaultConstructorAndInvalidParameters() throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
        try {
            abstractBaseControllerFactory.handleCall(ClassWithoutDefaultConstructor.class);

            fail("Expected exception did not occur");
        } catch(RuntimeException e) {
            assertTrue(e.getMessage().contains(ExceptionConstants.ERROR_DURING_FXML_CONSTRUCTOR_INSTANTIATION));
            verify(abstractBaseControllerFactory).hasValidParameterAmountAndTypes(any());
            verify(abstractBaseControllerFactory, never()).createObjectWithExpectedArgs(any(), any());
        }
    }

    @Test
    public void call_hasDefaultConstructorAndInvalidParameters() throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
        assertTrue(abstractBaseControllerFactory.handleCall(ClassWithZeroArgConstructor.class) instanceof ClassWithZeroArgConstructor);
        verify(abstractBaseControllerFactory, never()).hasValidParameterAmountAndTypes(any());
        verify(abstractBaseControllerFactory, never()).createObjectWithExpectedArgs(any(), any());
    }

    @Test
    public void handleCall_hasConstructorWithValidParameters() throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
        doReturn(true).when(abstractBaseControllerFactory).hasValidParameterAmountAndTypes(any());

        assertTrue(abstractBaseControllerFactory.handleCall(ClassWithCorrectNumberAndTypesOfArgs.class) instanceof ClassWithCorrectNumberAndTypesOfArgs);
        verify(abstractBaseControllerFactory).hasValidParameterAmountAndTypes(any());
        verify(abstractBaseControllerFactory).createObjectWithExpectedArgs(any(), any());
    }

    @Test
    public void hasValidParameterAmountAndTypes_wrongAmount() {
        Constructor<?>[] constructors = ClassWithZeroArgConstructor.class.getConstructors();

        assertFalse(abstractBaseControllerFactory.hasValidParameterAmountAndTypes(constructors[0].getParameterTypes()));
    }

    @Test
    public void hasValidParameterAmountAndTypes_correctAmountWrongTypes() {
        Constructor<?>[] constructors = ClassWithCorrectNumberOfArgsButWrongTypes.class.getConstructors();
        Class<?>[] parameters = constructors[1].getParameterTypes();

        for(int i = 0; i < expectedParameters.size(); i++) {
            doReturn(false).when(abstractBaseControllerFactory).sameClassOrInterface(expectedParameters.get(i), parameters[i]);
        }

        assertFalse(abstractBaseControllerFactory.hasValidParameterAmountAndTypes(parameters));
    }

    @Test
    public void hasValidParameterAmountAndTypes_correctAmountAndTypes() {
        Constructor<?>[] constructors = ClassWithCorrectNumberAndTypesOfArgs.class.getConstructors();
        Class<?>[] parameters = constructors[1].getParameterTypes();

        for(int i = 0; i < expectedParameters.size(); i++) {
            doReturn(true).when(abstractBaseControllerFactory).sameClassOrInterface(expectedParameters.get(i), parameters[i]);
        }

        assertTrue(abstractBaseControllerFactory.hasValidParameterAmountAndTypes(constructors[1].getParameterTypes()));
    }

    @Test
    public void sameClassOrInterface_noMatches() {
        Object object = "object";

        assertFalse(abstractBaseControllerFactory.sameClassOrInterface(object, Integer.class));
    }

    @Test
    public void sameClassOrInterface_matchesInterface() {
        Object object = Mockito.spy(IUIFactory.class);

        assertTrue(abstractBaseControllerFactory.sameClassOrInterface(object, IUIFactory.class));
    }

    @Test
    public void sameClassOrInterface_matchesDirectly() {
        Object object = spy(new UIFactory());

        assertTrue(abstractBaseControllerFactory.sameClassOrInterface(object, UIFactory.class));
    }
}
