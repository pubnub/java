package com.pubnub.api.integration.managers.subscription;

import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.enums.PNStatusCategory;
import com.pubnub.api.integration.util.ITTestConfig;
import com.pubnub.api.models.consumer.PNStatus;
import org.aeonbits.owner.ConfigFactory;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.AssumptionViolatedException;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class AssumingProperConfig implements TestRule {
    private ITTestConfig itPamTestConfig = ConfigFactory.create(ITTestConfig.class, System.getenv());

    @NotNull
    @Override
    public Statement apply(@NotNull final Statement base, @NotNull final Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                if (itPamTestConfig.pamSecKey() != null) {
                    base.evaluate();
                }
                else {
                    throw new AssumptionViolatedException("missing config");
                }
            }
        };
    }
}

public abstract class AbstractReconnectionProblem {
    protected ITTestConfig itPamTestConfig = ConfigFactory.create(ITTestConfig.class, System.getenv());

    @ClassRule
    public static AssumingProperConfig assumingProperConfig = new AssumingProperConfig();

    protected static final int SUBSCRIBE_TIMEOUT = 5;
    protected final List<CollectedStatus> collectedStatuses = Collections.synchronizedList(new ArrayList<>());
    protected PubNub  pn;

    protected String authKey = randomId();

    private static String randomId() {
        return UUID.randomUUID().toString();
    }

    private void grantAccess(final String... protectedChannelNames) throws PubNubException {
        final PubNub pnAdmin = adminPubNub();
        pnAdmin.grant()
                .authKeys(singletonList(authKey))
                .channels(asList(protectedChannelNames))
                .read(true)
                .sync();
    }

    private void grantAccessToChannelGroup(final String... protectedChannelGroupNames) throws PubNubException {
        final PubNub pnAdmin = adminPubNub();
        pnAdmin.grant()
                .authKeys(singletonList(authKey))
                .channelGroups(asList(protectedChannelGroupNames))
                .read(true)
                .sync();
    }

    @Before
    public void setupClients() {
        pn = spy(privilegedClientPubNub());
    }

    @After
    public void disconnectClients() {
        pn.forceDestroy();
        pn = null;
    }

    protected void subscribe(final PubNub pnClient, final boolean reportCallStack, final String... channels) {
        subscribe(pnClient, reportCallStack, null, channels);
    }

    protected void subscribe(final PubNub pnClient, final String... channelNames) {
        subscribe(pnClient, false, null, channelNames);
    }

    protected void subscribe(final PubNub pnClient, final BiConsumer<PubNub, PNStatus> block, final String... channels) {
        subscribe(pnClient, false, block, channels);
    }

    protected void subscribe(final PubNub pnClient, boolean reportCallStack, final BiConsumer<PubNub, PNStatus> block, final String... channels) {
        pnClient.addListener(new SubscribeCallbackAdapter() {
            @Override
            public void status(final PubNub pubnub, final PNStatus pnStatus) {
                final Exception exception = new Exception();
                synchronized (collectedStatuses) {
                    collectedStatuses.add(new CollectedStatus(pnStatus, exception));
                }
                System.out.println("status: " + pnStatus);
                System.out.println("affected channels: " + pnStatus.getAffectedChannels());
                if (reportCallStack) {
                    exception.printStackTrace(System.out);
                }
                if (block != null) {
                    block.accept(pubnub, pnStatus);
                }
            }
        });

        pnClient.subscribe().channels(asList(channels)).execute();
    }

    protected void subscribeToGroup(final PubNub pnClient, final boolean reportCallStack, final String... channelGroups) {
        subscribeToGroup(pnClient, reportCallStack, null, channelGroups);
    }

    protected void subscribeToGroup(final PubNub pnClient, final String... channelGroups) {
        subscribeToGroup(pnClient, false, null, channelGroups);
    }

    protected void subscribeToGroup(final PubNub pnClient, final BiConsumer<PubNub, PNStatus> block, final String... channelGroups) {
        subscribeToGroup(pnClient, false, block, channelGroups);
    }

    protected void subscribeToGroup(final PubNub pnClient, boolean reportCallStack, final BiConsumer<PubNub, PNStatus> block, final String... channelGroups) {
        pnClient.addListener(new SubscribeCallbackAdapter() {
            @Override
            public void status(final PubNub pubnub, final PNStatus pnStatus) {
                final Exception exception = new Exception();
                synchronized (collectedStatuses) {
                    collectedStatuses.add(new CollectedStatus(pnStatus, exception));
                }
                System.out.println("status: " + pnStatus);
                System.out.println("affected channels: " + pnStatus.getAffectedChannels());
                System.out.println("affected channel groups: " + pnStatus.getAffectedChannelGroups());
                if (reportCallStack) {
                    exception.printStackTrace(System.out);
                }
                if (block != null) {
                    block.accept(pubnub, pnStatus);
                }
            }
        });

        pnClient.subscribe()
                .channelGroups(asList(channelGroups))
                .execute();
    }



    protected void createChannelGroup(final PubNub pnClient, final String channelGroup, final String... channelNames) throws PubNubException {
        pnClient.addChannelsToChannelGroup()
                .channelGroup(channelGroup)
                .channels(asList(channelNames))
                .sync();
    }

    protected abstract PubNub privilegedClientPubNub();

    private PubNub adminPubNub() {
        final PNConfiguration pnConfiguration = new PNConfiguration(PubNub.generateUUID());
        pnConfiguration.setSubscribeKey(itPamTestConfig.pamSubKey());
        pnConfiguration.setPublishKey(itPamTestConfig.pamPubKey());
        pnConfiguration.setSecretKey(itPamTestConfig.pamSecKey());
        return new PubNub(pnConfiguration);
    }

    @Test
    public void alwaysContinueSubscriptionToChannelGroupIfNoActionTaken() throws PubNubException, InterruptedException {
        final String channelGroup = "chg-1-" + randomId();

        createChannelGroup(adminPubNub(), channelGroup, "ch-1-" + randomId(), "ch-2-" + randomId());

        subscribeToGroup(pn, true, channelGroup);

        TimeUnit.SECONDS.sleep(SUBSCRIBE_TIMEOUT * 3);

        long countAccessDenied = collectedStatuses.stream()
                .filter(collectedStatus ->
                        collectedStatus.getPnStatus().getCategory() == PNStatusCategory.PNAccessDeniedCategory
                                && collectedStatus.getPnStatus().getAffectedChannelGroups().contains(channelGroup))
                .count();

        assertThat(countAccessDenied, greaterThan(1L));
    }


    @Test
    public void alwaysContinueSubscriptionIfNoActionTaken() throws InterruptedException {
        final String channel = "ch-" + randomId();

        subscribe(pn, true, channel);

        TimeUnit.SECONDS.sleep(SUBSCRIBE_TIMEOUT * 3);

        long countAccessDenied = collectedStatuses.stream()
                .filter(collectedStatus ->
                        collectedStatus.getPnStatus().getCategory() == PNStatusCategory.PNAccessDeniedCategory
                                && collectedStatus.getPnStatus().getAffectedChannels().contains(channel))
                .count();

        assertThat(countAccessDenied, greaterThan(1L));
    }

    @Test
    public void continueSubscriptionAfterUnsubscribeFromForbiddenChannel() throws InterruptedException, PubNubException {
        final String channel1 = "ch-1-" + randomId();
        final String channel2 = "ch-2-" + randomId();

        grantAccess(channel1);

        subscribe(pn, true, new BiConsumer<PubNub, PNStatus>() {
            @Override
            public void accept(final PubNub pubNub, final PNStatus status) {
                if (status.isError()) {
                    if (status.getCategory() == PNStatusCategory.PNAccessDeniedCategory) {
                        final List<String> channelsToUnsubscribe = status.getAffectedChannels();
                        try {
                            System.out.println("Unsubscribing from: " + channelsToUnsubscribe);
                            pubNub.unsubscribe().channels(channelsToUnsubscribe).execute();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }, channel1, channel2);

        TimeUnit.SECONDS.sleep(SUBSCRIBE_TIMEOUT * 3);

        long countAccessDenied = collectedStatuses.stream()
                .filter(collectedStatus ->
                        collectedStatus.getPnStatus().getCategory() == PNStatusCategory.PNAccessDeniedCategory
                                && collectedStatus.getPnStatus().getAffectedChannels().contains(channel2))
                .count();

        long countConnected = collectedStatuses.stream()
                .filter(collectedStatus ->
                        collectedStatus.getPnStatus().getCategory() == PNStatusCategory.PNConnectedCategory
                                && collectedStatus.getPnStatus().getAffectedChannels().contains(channel1))
                .count();

        assertThat(countAccessDenied, equalTo(1L));
        assertThat(countConnected, equalTo(1L));
    }

    @Test
    public void continueSubscriptionToChannelGroupAfterUnsubscribeFromForbiddenChannel() throws InterruptedException, PubNubException {
        final String channelGroup1 = "chg-1-" + randomId();
        final String channelGroup2 = "chg-2-" + randomId();

        createChannelGroup(adminPubNub(), channelGroup1, "ch-1-" + randomId(), "ch-2-" + randomId());
        createChannelGroup(adminPubNub(), channelGroup2, "ch-1-" + randomId(), "ch-2-" + randomId());

        grantAccessToChannelGroup(channelGroup1);

        subscribeToGroup(pn, true, new BiConsumer<PubNub, PNStatus>() {
            @Override
            public void accept(final PubNub pubNub, final PNStatus status) {
                if (status.isError()) {
                    if (status.getCategory() == PNStatusCategory.PNAccessDeniedCategory) {
                        final List<String> channelGroupsToUnsubscribe = status.getAffectedChannelGroups();
                        try {
                            System.out.println("Unsubscribing from groups: " + channelGroupsToUnsubscribe);
                            pubNub.unsubscribe().channelGroups(channelGroupsToUnsubscribe).execute();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }, channelGroup1, channelGroup2);

        TimeUnit.SECONDS.sleep(SUBSCRIBE_TIMEOUT * 3);

        long countAccessDenied = collectedStatuses.stream()
                .filter(collectedStatus ->
                        collectedStatus.getPnStatus().getCategory() == PNStatusCategory.PNAccessDeniedCategory
                                && collectedStatus.getPnStatus().getAffectedChannelGroups().contains(channelGroup2))
                .count();

        long countConnected = collectedStatuses.stream()
                .filter(collectedStatus ->
                        collectedStatus.getPnStatus().getCategory() == PNStatusCategory.PNConnectedCategory
                                && collectedStatus.getPnStatus().getAffectedChannelGroups().contains(channelGroup1))
                .count();

        assertThat(countAccessDenied, equalTo(1L));
        assertThat(countConnected, equalTo(1L));
    }


    @Test
    public void stopSubscriptionWhenRequestedToDisconnectOnAccessDenied() throws InterruptedException {
        final String channel = "ch-" + randomId();

        subscribe(pn, true, new BiConsumer<PubNub, PNStatus>() {
            @Override
            public void accept(final PubNub pubNub, final PNStatus status) {
                if (status.isError()) {
                    if (status.getCategory() == PNStatusCategory.PNAccessDeniedCategory) {
                        pn.disconnect();
                    }
                }
            }
        }, channel);

        TimeUnit.SECONDS.sleep(SUBSCRIBE_TIMEOUT * 3);

        long countAccessDenied = collectedStatuses.stream()
                .filter(collectedStatus ->
                        collectedStatus.getPnStatus().getCategory() == PNStatusCategory.PNAccessDeniedCategory
                                && collectedStatus.getPnStatus().getAffectedChannels().contains(channel))
                .count();

        assertThat(countAccessDenied, equalTo(1L));
        verify(pn, times(1)).disconnect();
    }

    @Test
    public void stopSubscriptionToChannelGroupWhenRequestedToDisconnectOnAccessDenied() throws InterruptedException {
        final String channelGroup = "chg-" + randomId();

        subscribeToGroup(pn, true, new BiConsumer<PubNub, PNStatus>() {
            @Override
            public void accept(final PubNub pubNub, final PNStatus status) {
                if (status.isError()) {
                    if (status.getCategory() == PNStatusCategory.PNAccessDeniedCategory) {
                        pn.disconnect();
                    }
                }
            }
        }, channelGroup);

        TimeUnit.SECONDS.sleep(SUBSCRIBE_TIMEOUT * 3);

        long countAccessDenied = collectedStatuses.stream()
                .filter(collectedStatus ->
                        collectedStatus.getPnStatus().getCategory() == PNStatusCategory.PNAccessDeniedCategory
                                && collectedStatus.getPnStatus().getAffectedChannelGroups().contains(channelGroup))
                .count();

        assertThat(countAccessDenied, equalTo(1L));
        verify(pn, times(1)).disconnect();
    }


    @Test
    public void stopSubscriptionWhenRequestedToForceDestroyOnAccessDenied() throws InterruptedException {
        final String channel = "ch-" + randomId();

        subscribe(pn, true, new BiConsumer<PubNub, PNStatus>() {
            @Override
            public void accept(final PubNub pubNub, final PNStatus status) {
                if (status.isError()) {
                    if (status.getCategory() == PNStatusCategory.PNAccessDeniedCategory) {
                        pn.forceDestroy();
                    }
                }
            }
        }, channel);

        TimeUnit.SECONDS.sleep(SUBSCRIBE_TIMEOUT * 3);

        long countAccessDenied = collectedStatuses.stream()
                .filter(collectedStatus ->
                        collectedStatus.getPnStatus().getCategory() == PNStatusCategory.PNAccessDeniedCategory
                                && collectedStatus.getPnStatus().getAffectedChannels().contains(channel))
                .count();

        assertThat(countAccessDenied, equalTo(1L));
        verify(pn, times(1)).forceDestroy();
    }

    @Test
    public void stopSubscriptionToChannelGroupWhenRequestedToForceDestroyOnAccessDenied() throws InterruptedException {
        final String channelGroup = "chg-" + randomId();

        subscribeToGroup(pn, true, new BiConsumer<PubNub, PNStatus>() {
            @Override
            public void accept(final PubNub pubNub, final PNStatus status) {
                if (status.isError()) {
                    if (status.getCategory() == PNStatusCategory.PNAccessDeniedCategory) {
                        pn.forceDestroy();
                    }
                }
            }
        }, channelGroup);

        TimeUnit.SECONDS.sleep(SUBSCRIBE_TIMEOUT * 3);

        long countAccessDenied = collectedStatuses.stream()
                .filter(collectedStatus ->
                        collectedStatus.getPnStatus().getCategory() == PNStatusCategory.PNAccessDeniedCategory
                                && collectedStatus.getPnStatus().getAffectedChannelGroups().contains(channelGroup))
                .count();

        assertThat(countAccessDenied, equalTo(1L));
        verify(pn, times(1)).forceDestroy();
    }
}