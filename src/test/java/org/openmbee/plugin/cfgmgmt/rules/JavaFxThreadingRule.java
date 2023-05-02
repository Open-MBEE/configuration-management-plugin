package org.openmbee.plugin.cfgmgmt.rules;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import javax.swing.*;
import java.util.concurrent.CountDownLatch;

public class JavaFxThreadingRule implements TestRule {
    private static boolean jfxThreadReady;

    @Override
    public Statement apply(Statement statement, Description description) {
        return new JfxThreadStatement(statement);
    }

    private static class JfxThreadStatement extends Statement {
        private final Statement statement;
        private Throwable caughtException;

        public JfxThreadStatement(Statement statement) {
            this.statement = statement;
        }

        @Override
        public void evaluate() throws Throwable {
            if (!jfxThreadReady) {
                setupJfx();
                jfxThreadReady = true;
            }

            CountDownLatch latch = new CountDownLatch(1);

            Platform.runLater(() -> {
                try {
                    statement.evaluate();
                } catch (Throwable e) {
                    caughtException = e;
                }
                latch.countDown();
            });

            latch.await();

            if (caughtException != null) {
                // allows evaluate to run but throws to properly fail tests
                throw caughtException;
            }
        }

        protected void setupJfx() throws InterruptedException {
            CountDownLatch latch = new CountDownLatch(1);

            // yes this is required, platform isn't initialized yet
            SwingUtilities.invokeLater(() -> {
                new JFXPanel();
                latch.countDown();
            });

            latch.await();
        }
    }
}
