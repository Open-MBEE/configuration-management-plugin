package org.openmbee.plugin.cfgmgmt.application;

import org.openmbee.plugin.cfgmgmt.constants.ExceptionConstants;
import org.openmbee.plugin.cfgmgmt.rules.JavaFxThreadingRule;
import javafx.application.Platform;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;

import java.io.IOException;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

public class TestBaseModuleStart {
    private BaseModuleStart baseModuleStart;
    private BaseModule module;
    private Logger logger;

    @Rule
    public JavaFxThreadingRule javaFxThreadingRule = new JavaFxThreadingRule();

    @Before
    public void setup() {
        module = mock(BaseModule.class);
        baseModuleStart = spy(new BaseModuleStart(module));
        logger = mock(Logger.class);

        doReturn(logger).when(baseModuleStart).getLogger();
        Platform.setImplicitExit(false);
    }

    @After
    public void teardown() {
        Platform.setImplicitExit(true);
    }

    @Test
    public void run_noIssues() throws IOException {
        doNothing().when(module).moduleStartRoutine();

        baseModuleStart.run();

        verify(logger, never()).error(anyString(), anyString());
    }

    @Test
    public void run_getStageException() {
        String exceptionMessage = "Runtime Exception";
        Exception exception = spy(new RuntimeException(exceptionMessage));
        try {
            doThrow(exception).when(module).moduleStartRoutine();

            baseModuleStart.run();

            verify(logger).error(ExceptionConstants.IO_EXCEPTION_ERROR_MESSAGE, exceptionMessage);
        } catch(Exception e) {
            fail("Unexpected Exception");
        }
    }
}
