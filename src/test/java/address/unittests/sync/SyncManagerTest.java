package address.unittests.sync;

import address.events.EventManager;
import address.events.SyncFailedEvent;
import address.sync.RemoteService;
import address.sync.SyncManager;
import com.google.common.eventbus.Subscribe;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.sleep;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.*;

public class SyncManagerTest {
    SyncManager syncManager;
    ExecutorService executorService;
    ScheduledExecutorService scheduledExecutorService;

    private int syncFailedEventCount;

    @Subscribe
    public void handleSyncFailedEvent(SyncFailedEvent sfe) {
        syncFailedEventCount++;
    }

    @Before
    public void setup() {
        EventManager.getInstance().registerHandler(this);
        syncFailedEventCount = 0;
        RemoteService remoteService = mock(RemoteService.class);
        executorService = mock(ExecutorService.class);
        scheduledExecutorService = mock(ScheduledExecutorService.class);
        syncManager = new SyncManager(remoteService, executorService, scheduledExecutorService);
    }

    @Test
    public void updateModel_noActiveAddressBook_syncFailed() {
        when(scheduledExecutorService.scheduleWithFixedDelay(any(Runnable.class), anyLong(), anyLong(), any(TimeUnit.class))).thenAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            Runnable task = (Runnable) args[0];
            task.run();
            return null;
        });
        syncManager.updatePeriodically(1);
        assertEquals(1, syncFailedEventCount);
    }

    @After
    public void tearDown() {
        EventManager.clearSubscribers();
    }
}
