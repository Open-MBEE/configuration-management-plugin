package org.openmbee.plugin.cfgmgmt.application;

import org.openmbee.plugin.cfgmgmt.constants.ExceptionConstants;
import org.openmbee.plugin.cfgmgmt.rules.JavaFxThreadingRule;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;

import java.io.IOException;

import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class TestBaseModuleStop {
    private BaseModuleStop baseModuleStop;
    private BaseModule module;
    private Logger logger;

    @Rule
    public JavaFxThreadingRule javaFxThreadingRule = new JavaFxThreadingRule();

    @Before
    public void setup() {
        module = mock(BaseModule.class);
        baseModuleStop = spy(new BaseModuleStop(module));
        logger = mock(Logger.class);

        doReturn(logger).when(baseModuleStop).getLogger();
        Platform.setImplicitExit(false);
    }

    @After
    public void teardown() {
        Platform.setImplicitExit(true);
    }

    @Test
    public void run_noIssues() throws IOException {
        Stage stage = mock(Stage.class);

        when(module.getPrimaryStage()).thenReturn(stage);
        doNothing().when(module).stop(true, stage);

        baseModuleStop.run();

        verify(logger, never()).error(anyString(), anyString());
    }

    @Test
    public void run_getStageException() {
        String exceptionMessage = "Runtime Exception";
        Exception exception = spy(new RuntimeException(exceptionMessage));
        try {
            doThrow(exception).when(module).getPrimaryStage();

            baseModuleStop.run();

            verify(logger).error(ExceptionConstants.IO_EXCEPTION_ERROR_MESSAGE, exceptionMessage);
        } catch(Exception e) {
            fail("Unexpected Exception");
        }
    }
}
