package com.pubnub.api.endpoints.remoteaction;

import com.pubnub.api.PubNubException;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.models.consumer.PNStatus;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static com.pubnub.api.endpoints.remoteaction.ComposableRemoteAction.firstDo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ComposableRemoteActionTest {

    @Test
    public void sync_happyPath() throws PubNubException {
        //given
        RemoteAction<Integer> composedAction = firstDo(TestRemoteAction.successful(668))
                .then(integerResult -> TestRemoteAction.successful(integerResult * 2))
                .then(integerResult -> TestRemoteAction.successful(integerResult + 1));

        //when
        int result = composedAction.sync();

        //then
        assertEquals(1337, result);
    }


    @Test
    public void async_happyPath() throws InterruptedException {
        //given
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicInteger result = new AtomicInteger(0);
        RemoteAction<Integer> composedAction = firstDo(TestRemoteAction.successful(668))
                .then(integerResult -> TestRemoteAction.successful(integerResult * 2))
                .then(integerResult -> TestRemoteAction.successful(integerResult + 1));

        //when
        composedAction.async((r, s) -> {
            if (r != null) {
                result.set(r);
                latch.countDown();
            }
        });

        //then
        assertTrue(latch.await(1, TimeUnit.SECONDS));
        assertEquals(1337, result.get());
    }

    @Test(expected = PubNubException.class)
    public void sync_whenFirstFails_RestIsNotCalled() throws PubNubException {
        firstDo(TestRemoteAction.failing())
                .then(integerResult -> {
                            fail("fail");
                            return TestRemoteAction.successful(15);
                        }
                ).sync();
    }

    @Test
    public void async_whenFirstFails_RestIsNotCalled() throws InterruptedException {
        //given
        final CountDownLatch latch = new CountDownLatch(1);
        final TestRemoteAction<Integer> successful = TestRemoteAction.successful(15);

        firstDo(TestRemoteAction.failing())
                .then(integerResult -> successful)
                //when
                .async((result, status) -> {
                    latch.countDown();
                });

        //then
        assertTrue(latch.await(1, TimeUnit.SECONDS));
        assertThat(successful.howManyTimesAsyncCalled(), is(0));
    }

    @Test
    public void cancel_cancelsCurrentlyRunningTask_RestIsNotCalled() throws InterruptedException {
        //given
        final CountDownLatch cancelSynchronisingLatch = new CountDownLatch(1);
        final CountDownLatch resultSynchronisingLatch = new CountDownLatch(1);
        final AtomicBoolean firstAsyncFinished = new AtomicBoolean(false);
        final CancellableRemoteAction<Object> longRunningTask = new CancellableRemoteAction<Object>() {
            @Override
            public void doAsync(@NotNull PNCallback<Object> callback) throws InterruptedException {
                cancelSynchronisingLatch.await();
                System.out.println("async");
                callback.onResponse(null, PNStatus.builder().build());
                firstAsyncFinished.set(true);
                resultSynchronisingLatch.countDown();
            }

            @Override
            public void silentCancel() {
                System.out.println("silentCancel");
                cancelSynchronisingLatch.countDown();
            }
        };
        final TestRemoteAction<Integer> successful = TestRemoteAction.successful(15);
        RemoteAction<Integer> composedAction = firstDo(longRunningTask).then(integerResult -> successful);
        composedAction.async((r, s) -> {
        });

        //when
        composedAction.silentCancel();

        //then
        assertTrue(resultSynchronisingLatch.await(1, TimeUnit.SECONDS));
        assertTrue(firstAsyncFinished.get());
        assertThat(successful.howManyTimesAsyncCalled(), is(0));
    }

    @Test
    public void retry_withoutCheckpointStartsFromBeginning() throws InterruptedException {
        //given
        CountDownLatch countDownLatch = new CountDownLatch(2);
        TestRemoteAction<Integer> firstSuccessful = TestRemoteAction.successful(1);
        TestRemoteAction<Integer> secondSuccessful = TestRemoteAction.successful(1);
        TestRemoteAction<Integer> firstFailing = TestRemoteAction.failingFirstCall(1);

        RemoteAction<Integer> composedAction = firstDo(firstSuccessful)
                .then(integerResult -> secondSuccessful)
                .then(integerResult -> firstFailing);

        //when
        composedAction.async((r, s) -> {
                    countDownLatch.countDown();
                    if (s.isError()) {
                        s.retry();
                    }
                }
        );

        //then
        assertTrue(countDownLatch.await(1000, TimeUnit.MILLISECONDS));
        assertThat(firstSuccessful.howManyTimesAsyncCalled(), is(2));
        assertThat(secondSuccessful.howManyTimesAsyncCalled(), is(2));
        assertThat(firstFailing.howManyTimesAsyncCalled(), is(2));
    }

    @Test
    public void retry_startsFromCheckpoint() throws InterruptedException {
        //given
        CountDownLatch countDownLatch = new CountDownLatch(2);
        TestRemoteAction<Integer> firstSuccessful = TestRemoteAction.successful(1);
        TestRemoteAction<Integer> secondSuccessful = TestRemoteAction.successful(1);
        TestRemoteAction<Integer> firstFailing = TestRemoteAction.failingFirstCall(1);

        RemoteAction<Integer> composedAction = firstDo(firstSuccessful)
                .checkpoint()
                .then(integerResult -> secondSuccessful)
                .then(integerResult -> firstFailing);

        //when
        composedAction.async((r, s) -> {
                    countDownLatch.countDown();
                    if (s.isError()) {
                        s.retry();
                    }
                }
        );

        //then
        assertTrue(countDownLatch.await(1000, TimeUnit.MILLISECONDS));
        assertThat(firstSuccessful.howManyTimesAsyncCalled(), is(1));
        assertThat(secondSuccessful.howManyTimesAsyncCalled(), is(2));
        assertThat(firstFailing.howManyTimesAsyncCalled(), is(2));
    }
}
