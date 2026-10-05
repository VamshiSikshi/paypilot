package com.paypilot.backend.domain.action;

import com.paypilot.backend.domain.merchant.Merchant;
import com.paypilot.backend.domain.user.User;
import com.paypilot.backend.domain.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ActionExecutionTest {

    private Merchant merchant;
    private User user;

    @BeforeEach
    void setUp() {
        merchant = new Merchant(
                "acc_test_123",
                "Test Business",
                "IN",
                "INR",
                "Asia/Kolkata"
        );

        user = new User(
                merchant,
                "owner@test.com",
                "Test Owner",
                UserRole.OWNER
        );
    }

    @Test
    void shouldStartInConfirmationRequiredState() {
        ActionExecution execution = createExecution(true);

        assertThat(execution.getStatus())
                .isEqualTo(ActionExecutionStatus.CONFIRMATION_REQUIRED);
        assertThat(execution.getConfirmedAt()).isNull();
        assertThat(execution.getStartedAt()).isNull();
        assertThat(execution.getCompletedAt()).isNull();
    }

    @Test
    void shouldStartInPendingStateWhenConfirmationIsNotRequired() {
        ActionExecution execution = createExecution(false);

        assertThat(execution.getStatus())
                .isEqualTo(ActionExecutionStatus.PENDING);
        assertThat(execution.getConfirmedAt()).isNull();
        assertThat(execution.getStartedAt()).isNull();
        assertThat(execution.getCompletedAt()).isNull();
    }

    @Test
    void shouldConfirmExecution() {
        ActionExecution execution = createExecution(true);

        execution.confirm();

        assertThat(execution.getStatus())
                .isEqualTo(ActionExecutionStatus.CONFIRMED);
        assertThat(execution.getConfirmedAt()).isNotNull();
    }

    @Test
    void shouldStartPendingExecution() {
        ActionExecution execution = createExecution(false);

        execution.start();

        assertThat(execution.getStatus())
                .isEqualTo(ActionExecutionStatus.EXECUTING);
        assertThat(execution.getStartedAt()).isNotNull();
    }

    @Test
    void shouldStartConfirmedExecution() {
        ActionExecution execution = createExecution(true);

        execution.confirm();
        execution.start();

        assertThat(execution.getStatus())
                .isEqualTo(ActionExecutionStatus.EXECUTING);
        assertThat(execution.getStartedAt()).isNotNull();
    }

    @Test
    void shouldCompleteExecutingExecution() {
        ActionExecution execution = createExecution(false);

        execution.start();
        execution.complete("{\"success\":true}");

        assertThat(execution.getStatus())
                .isEqualTo(ActionExecutionStatus.COMPLETED);
        assertThat(execution.getResultData())
                .isEqualTo("{\"success\":true}");
        assertThat(execution.getCompletedAt()).isNotNull();
        assertThat(execution.getErrorMessage()).isNull();
    }

    @Test
    void shouldFailExecutingExecution() {
        ActionExecution execution = createExecution(false);

        execution.start();
        execution.fail("Refund failed");

        assertThat(execution.getStatus())
                .isEqualTo(ActionExecutionStatus.FAILED);
        assertThat(execution.getErrorMessage())
                .isEqualTo("Refund failed");
        assertThat(execution.getCompletedAt()).isNotNull();
    }

    @Test
    void shouldCancelConfirmationRequiredExecution() {
        ActionExecution execution = createExecution(true);

        execution.cancel();

        assertThat(execution.getStatus())
                .isEqualTo(ActionExecutionStatus.CANCELLED);
    }

    @Test
    void shouldCancelPendingExecution() {
        ActionExecution execution = createExecution(false);

        execution.cancel();

        assertThat(execution.getStatus())
                .isEqualTo(ActionExecutionStatus.CANCELLED);
    }

    @Test
    void shouldCancelConfirmedExecution() {
        ActionExecution execution = createExecution(true);

        execution.confirm();
        execution.cancel();

        assertThat(execution.getStatus())
                .isEqualTo(ActionExecutionStatus.CANCELLED);
    }

    @Test
    void shouldRejectConfirmWhenExecutionIsNotAwaitingConfirmation() {
        ActionExecution execution = createExecution(false);

        assertThatThrownBy(execution::confirm)
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldRejectStartWhenExecutionRequiresConfirmation() {
        ActionExecution execution = createExecution(true);

        assertThatThrownBy(execution::start)
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldRejectCompleteWhenExecutionIsNotExecuting() {
        ActionExecution execution = createExecution(false);

        assertThatThrownBy(() ->
                execution.complete("{\"success\":true}")
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldRejectFailWhenExecutionIsNotExecuting() {
        ActionExecution execution = createExecution(false);

        assertThatThrownBy(() ->
                execution.fail("Refund failed")
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldRejectCancelWhenExecutionIsExecuting() {
        ActionExecution execution = createExecution(false);

        execution.start();

        assertThatThrownBy(execution::cancel)
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldRejectStartAfterCompletion() {
        ActionExecution execution = createExecution(false);

        execution.start();
        execution.complete("{\"success\":true}");

        assertThatThrownBy(execution::start)
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldRejectStartAfterFailure() {
        ActionExecution execution = createExecution(false);

        execution.start();
        execution.fail("Refund failed");

        assertThatThrownBy(execution::start)
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldRejectStartAfterCancellation() {
        ActionExecution execution = createExecution(false);

        execution.cancel();

        assertThatThrownBy(execution::start)
                .isInstanceOf(IllegalStateException.class);
    }

    private ActionExecution createExecution(boolean confirmationRequired) {
        return new ActionExecution(
                merchant,
                user,
                null,
                null,
                ActionType.REFUND,
                ActionTargetType.PAYMENT,
                100L,
                confirmationRequired,
                "{\"amount\":2500}",
                "refund-payment-100"
        );
    }
}