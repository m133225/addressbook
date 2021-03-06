package address.model;

import static address.model.ChangeObjectInModelCommand.State.*;
import static address.model.datatypes.person.ReadOnlyViewablePerson.ChangeInProgress.*;

import address.events.BaseEvent;
import address.events.CommandFinishedEvent;
import address.events.UpdatePersonOnRemoteRequestEvent;
import address.model.datatypes.person.Person;
import address.model.datatypes.person.ReadOnlyPerson;
import address.model.datatypes.person.ViewablePerson;
import commons.PlatformExecUtil;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Handles optimistic UI updating, cancellation/changing command, and remote consistency logic
 * for editing a person in the addressbook.
 */
public class EditPersonCommand extends ChangePersonInModelCommand {

    public static final String COMMAND_TYPE = "Edit";

    private final Consumer<BaseEvent> eventRaiser;
    private final ModelManager model;
    private final ViewablePerson target;
    private final String addressbookName;

    // Person state snapshots
    private ReadOnlyPerson personDataBeforeExecution;
    private ReadOnlyPerson personDataAfterExecution;

    /**
     * @param inputRetriever Will run on execution {@link #run()} thread. This should handle thread concurrency
     *                       logic (eg. {@link PlatformExecUtil#call(Callable)} within itself.
     *                       If the returned Optional is empty, the command will be cancelled.
     * @see super#ChangePersonInModelCommand(int, Supplier, int)
     */
    public EditPersonCommand(int commandId, ViewablePerson target, Supplier<Optional<ReadOnlyPerson>> inputRetriever,
                             int gracePeriodDurationInSeconds, Consumer<BaseEvent> eventRaiser,
                             ModelManager model, String addressbookName) {
        super(commandId, inputRetriever, gracePeriodDurationInSeconds);
        assert target != null;
        this.target = target;
        this.model = model;
        this.eventRaiser = eventRaiser;
        this.addressbookName = addressbookName;
    }

    protected ViewablePerson getViewable() {
        return target;
    }

    @Override
    public int getTargetPersonId() {
        return target.getId();
    }

    @Override
    protected void before() {
        if (model.personHasOngoingChange(target)) {
            try {
                model.getOngoingChangeForPerson(target).waitForCompletion();
            } catch (InterruptedException e) {
                e.printStackTrace();
                assert false;
            }
        }
        PlatformExecUtil.runAndWait(() -> target.setChangeInProgress(EDITING));
        model.assignOngoingChangeToPerson(target.getId(), this);
        target.stopSyncingWithBackingObject();
        personDataBeforeExecution = new Person(target);
    }

    @Override
    protected void after() {
        personDataAfterExecution = new Person(target);
        PlatformExecUtil.runAndWait(() -> {
            target.clearChangeInProgress();
            target.continueSyncingWithBackingObject();
            target.forceSyncFromBacking();
        });
        model.unassignOngoingChangeForPerson(target.getId());
        eventRaiser.accept(new CommandFinishedEvent(
                new SingleTargetCommandResult(getCommandId(), COMMAND_TYPE, getState().toResultStatus(), TARGET_TYPE,
                        target.idString(), personDataBeforeExecution.fullName(), personDataAfterExecution.fullName())
        ));
    }

    @Override
    protected void simulateResult() {
        assert input != null;
        PlatformExecUtil.runAndWait(() -> target.simulateUpdate(input));
    }

    @Override
    protected void handleChangeToSecondsLeftInGracePeriod(int secondsLeft) {
        PlatformExecUtil.runAndWait(() -> target.setSecondsLeftInPendingState(secondsLeft));
    }

    @Override
    protected void handleEditRequest(Supplier<Optional<ReadOnlyPerson>> editInputSupplier) {
        // take details and update viewable, then restart grace period
        final Optional<ReadOnlyPerson> editInput = editInputSupplier.get();
        if (editInput.isPresent()) { // edit request confirmed
            cancelCommand();
            model.execNewEditPersonCommand(target, () -> editInput);
        }
    }

    @Override
    protected void handleDeleteRequest() {
        cancelCommand();
        model.execNewDeletePersonCommand(target);
    }

    @Override
    protected void handleResolveConflict() {
        // TODO
    }

    @Override
    protected void handleRetry() {
        cancelCommand();
        model.execNewEditPersonCommand(target, () -> Optional.of(input));
    }

    @Override
    protected Optional<ReadOnlyPerson> getRemoteConflictData() {
        return Optional.empty(); // TODO add after cloud individual check implemented
    }

    @Override
    protected boolean requestRemoteChange() {
        assert input != null;

        final CompletableFuture<ReadOnlyPerson> responseHolder = new CompletableFuture<>();
        eventRaiser.accept(new UpdatePersonOnRemoteRequestEvent(responseHolder, addressbookName, target.getId(), input));
        try {
            final ReadOnlyPerson remoteVersion = responseHolder.get();
            PlatformExecUtil.runAndWait(() -> target.getBacking().update(remoteVersion));
            return true;
        } catch (ExecutionException | InterruptedException e) {
            return false;
        }
    }

    @Override
    protected void finishWithCancel() {
        // for now, already handled by #after()
    }

    @Override
    protected void finishWithSuccess() {
        // nothing to do, viewableperson's auto update will handle it
    }

}
