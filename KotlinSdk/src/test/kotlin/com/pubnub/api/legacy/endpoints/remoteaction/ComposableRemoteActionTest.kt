package com.pubnub.apikt.legacy.endpoints.remoteaction

import com.pubnub.apikt.PubNubException
import com.pubnub.apikt.endpoints.remoteaction.ComposableRemoteAction.Companion.firstDo
import com.pubnub.apikt.endpoints.remoteaction.RemoteAction
import com.pubnub.apikt.enums.PNOperationType
import com.pubnub.apikt.enums.PNStatusCategory
import com.pubnub.apikt.models.consumer.PNStatus
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.Assert
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class ComposableRemoteActionTest {
    @Test
    @Throws(PubNubException::class)
    fun sync_happyPath() {
        // given
        val composedAction: RemoteAction<Int> = firstDo(TestRemoteAction.successful(668))
            .then { TestRemoteAction.successful(it * 2) }
            .then { TestRemoteAction.successful(it + 1) }

        // when
        val result = composedAction.sync()!!

        // then
        Assert.assertEquals(1337, result.toLong())
    }

    @Test
    @Throws(InterruptedException::class)
    fun async_happyPath() {
        // given
        val latch = CountDownLatch(1)
        val result = AtomicInteger(0)
        val composedAction: RemoteAction<Int> = firstDo(TestRemoteAction.successful(668))
            .then { TestRemoteAction.successful(it * 2) }
            .then { TestRemoteAction.successful(it + 1) }

        // when
        composedAction.async { r: Int?, _: PNStatus? ->
            if (r != null) {
                result.set(r)
                latch.countDown()
            }
        }

        // then
        Assert.assertTrue(latch.await(1, TimeUnit.SECONDS))
        Assert.assertEquals(1337, result.get().toLong())
    }

    @Test(expected = PubNubException::class)
    @Throws(PubNubException::class)
    fun sync_whenFirstFails_RestIsNotCalled() {
        firstDo(TestRemoteAction.failing<Int>())
            .then {
                Assert.fail("fail")
                TestRemoteAction.successful(15)
            }.sync()
    }

    @Test
    @Throws(InterruptedException::class)
    fun async_whenFirstFails_RestIsNotCalled() {
        // given
        val latch = CountDownLatch(1)
        val successful: TestRemoteAction<Int> = TestRemoteAction.successful(15)
        firstDo(TestRemoteAction.failing<Int>())
            .then { successful } // when
            .async { _, _ -> latch.countDown() }

        // then
        Assert.assertTrue(latch.await(1, TimeUnit.SECONDS))
        assertThat(successful.howManyTimesAsyncCalled(), Matchers.`is`(0))
    }

    @Test
    @Throws(InterruptedException::class)
    fun cancel_cancelsCurrentlyRunningTask_RestIsNotCalled() {
        // given
        val cancelSynchronisingLatch = CountDownLatch(1)
        val resultSynchronisingLatch = CountDownLatch(1)
        val firstAsyncFinished = AtomicBoolean(false)
        val longRunningTask: CancellableRemoteAction<Any?> = object : CancellableRemoteAction<Any?> {
            @Throws(InterruptedException::class)
            override fun doAsync(callback: (result: Any?, status: PNStatus) -> Unit) {
                cancelSynchronisingLatch.await()
                println("async")
                callback(null, PNStatus(category = PNStatusCategory.PNAcknowledgmentCategory, error = false, operation = operationType()))
                firstAsyncFinished.set(true)
                resultSynchronisingLatch.countDown()
            }

            override fun silentCancel() {
                println("silentCancel")
                cancelSynchronisingLatch.countDown()
            }

            override fun operationType(): PNOperationType {
                return PNOperationType.FileOperation
            }
        }
        val successful: TestRemoteAction<Int> = TestRemoteAction.successful(15)
        val composedAction: RemoteAction<Int> = firstDo(longRunningTask).then { successful }
        composedAction.async { _: Int?, _: PNStatus? -> }

        // when
        composedAction.silentCancel()

        // then
        Assert.assertTrue(resultSynchronisingLatch.await(1, TimeUnit.SECONDS))
        Assert.assertTrue(firstAsyncFinished.get())
        assertThat(successful.howManyTimesAsyncCalled(), Matchers.`is`(0))
    }

    @Test
    @Throws(InterruptedException::class)
    fun retry_withoutCheckpointStartsFromBeginning() {
        // given
        val countDownLatch = CountDownLatch(2)
        val firstSuccessful: TestRemoteAction<Int> = TestRemoteAction.successful(1)
        val secondSuccessful: TestRemoteAction<Int> = TestRemoteAction.successful(1)
        val firstFailing: TestRemoteAction<Int> = TestRemoteAction.failingFirstCall(1)
        val composedAction: RemoteAction<Int> = firstDo(firstSuccessful)
            .then { secondSuccessful }
            .then { firstFailing }

        // when
        composedAction.async { _: Int?, s: PNStatus ->
            countDownLatch.countDown()
            if (s.error) {
                s.retry()
            }
        }

        // then
        Assert.assertTrue(countDownLatch.await(1000, TimeUnit.MILLISECONDS))
        assertThat(firstSuccessful.howManyTimesAsyncCalled(), Matchers.`is`(2))
        assertThat(secondSuccessful.howManyTimesAsyncCalled(), Matchers.`is`(2))
        assertThat(firstFailing.howManyTimesAsyncCalled(), Matchers.`is`(2))
    }

    @Test
    @Throws(InterruptedException::class)
    fun retry_startsFromCheckpoint() {
        // given
        val countDownLatch = CountDownLatch(2)
        val firstSuccessful: TestRemoteAction<Int> = TestRemoteAction.successful(1)
        val secondSuccessful: TestRemoteAction<Int> = TestRemoteAction.successful(1)
        val firstFailing: TestRemoteAction<Int> = TestRemoteAction.failingFirstCall(1)
        val composedAction: RemoteAction<Int> = firstDo(firstSuccessful)
            .checkpoint()
            .then { secondSuccessful }
            .then { firstFailing }

        // when
        composedAction.async { _: Int?, s: PNStatus ->
            countDownLatch.countDown()
            if (s.error) {
                s.retry()
            }
        }

        // then
        Assert.assertTrue(countDownLatch.await(1000, TimeUnit.MILLISECONDS))
        assertThat(firstSuccessful.howManyTimesAsyncCalled(), Matchers.`is`(1))
        assertThat(secondSuccessful.howManyTimesAsyncCalled(), Matchers.`is`(2))
        assertThat(firstFailing.howManyTimesAsyncCalled(), Matchers.`is`(2))
    }
}
