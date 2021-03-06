package address.model;

import static address.model.ChangeObjectInModelCommand.State.*;
import static address.model.datatypes.person.ReadOnlyViewablePerson.ChangeInProgress.*;

import address.events.BaseEvent;
import address.events.CommandFinishedEvent;
import address.events.DeletePersonOnRemoteRequestEvent;
import address.model.datatypes.person.Person;
import address.model.datatypes.person.ReadOnlyPerson;
import address.model.datatypes.person.ViewablePerson;
import commons.PlatformExecUtil;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Handles optimistic UI updating, cancellation/changing command, and remote consistency logic
 * for deleting a person from the addressbook.
 */
public class DeletePersonCommand extends ChangePersonInModelCommand {

    public static final String COMMAND_TYPE = "Delete";

    private final Consumer<BaseEvent> eventRaiser;
    private final ModelManager model;
    private final ViewablePerson target;
    private final String addressbookName;

    // Person state snapshots
    private ReadOnlyPerson personDataBeforeExecution;

    /**
     * @see super#ChangePersonInModelCommand(int, Supplier, int)
     */
    public DeletePersonCommand(int commandId, ViewablePerson target, int gracePeriodDurationInSeconds,
                               Consumer<BaseEvent> eventRaiser, ModelManager model, String addressbookName) {
        // no input needed for delete commands
        super(commandId, () -> Optional.of(target), gracePeriodDurationInSeconds);
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
        personDataBeforeExecution = new Person(target);
        if (model.personHasOngoingChange(target)) {
            try {
                model.getOngoingChangeForPerson(target).waitForCompletion();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        model.assignOngoingChangeToPerson(target.getId(), this);
        target.stopSyncingWithBackingObject();
    }

    @Override
    protected void after() {
        PlatformExecUtil.runAndWait(() -> {
            target.clearChangeInProgress();
            target.continueSyncingWithBackingObject();
            target.forceSyncFromBacking();
        });
        model.unassignOngoingChangeForPerson(target.getId());
        final String targetName = personDataBeforeExecution.fullName(); // no name changes for deletes
        eventRaiser.accept(new CommandFinishedEvent(
                new SingleTargetCommandResult(getCommandId(), COMMAND_TYPE, getState().toResultStatus(), TARGET_TYPE,
                        target.idString(), targetName, targetName)
        ));
    }

    @Override
    protected void simulateResult() {
        PlatformExecUtil.runAndWait(() -> target.setChangeInProgress(DELETING));
    }

    @Override
    protected void handleChangeToSecondsLeftInGracePeriod(int secondsLeft) {
        PlatformExecUtil.runAndWait(() -> target.setSecondsLeftInPendingState(secondsLeft));
    }

    @Override
    protected void handleEditRequest(Supplier<Optional<ReadOnlyPerson>> editInputSupplier) {
        cancelCommand();
        model.execNewEditPersonCommand(target, editInputSupplier);
    }

    @Override
    protected void handleDeleteRequest() {
        // nothing to do here
    }

    @Override
    protected void handleResolveConflict() {
        // TODO
    }

    @Override
    protected void handleRetry() {
        cancelCommand();
        model.execNewDeletePersonCommand(target);
    }

    @Override
    protected Optional<ReadOnlyPerson> getRemoteConflictData() {
        return Optional.empty(); // TODO add after cloud individual check implemented
    }

    @Override
    protected boolean requestRemoteChange() {
        final CompletableFuture<Boolean> responseHolder = new CompletableFuture<>();
        eventRaiser.accept(new DeletePersonOnRemoteRequestEvent(responseHolder, addressbookName, target.getId()));
        try {
            return responseHolder.get();
        } catch (ExecutionException | InterruptedException e) {
            return false;
        }
    }

    @Override
    protected void finishWithCancel() {
        // nothing needed
    }

    @Override
    protected void finishWithSuccess() {
        // removing from backing will remove the front facing viewable too
        PlatformExecUtil.runAndWait(() -> model.backingModel().removePerson(target));
    }

}
