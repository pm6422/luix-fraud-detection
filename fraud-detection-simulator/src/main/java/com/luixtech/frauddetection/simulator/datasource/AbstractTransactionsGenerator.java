package com.luixtech.frauddetection.simulator.datasource;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.SplittableRandom;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

@Slf4j
public abstract class AbstractTransactionsGenerator implements Runnable {

    private static final long MAX_PAYEE_ID       = 100000;
    private static final long MAX_BENEFICIARY_ID = 100000;

    private static final double    MIN_PAYMENT_AMOUNT = 5d;
    private static final double    MAX_PAYMENT_AMOUNT = 20d;
    private final        Throttler throttler;

    private volatile boolean running = true;
    private final    Integer maxRecordsPerSecond;

    private final Consumer<Transaction> consumer;

    public AbstractTransactionsGenerator(Consumer<Transaction> consumer, int maxRecordsPerSecond) {
        this.consumer = consumer;
        this.maxRecordsPerSecond = maxRecordsPerSecond;
        this.throttler = new Throttler(maxRecordsPerSecond);
    }

    public void adjustMaxRecordsPerSecond(long maxRecordsPerSecond) {
        throttler.adjustMaxRecordsPerSecond(maxRecordsPerSecond);
    }

    protected Transaction randomEvent(SplittableRandom rnd) {
        long transactionId = rnd.nextLong(Long.MAX_VALUE);
        long payeeId = rnd.nextLong(MAX_PAYEE_ID);
        long beneficiaryId = rnd.nextLong(MAX_BENEFICIARY_ID);
        double paymentAmountDouble =
                ThreadLocalRandom.current().nextDouble(MIN_PAYMENT_AMOUNT, MAX_PAYMENT_AMOUNT);
        paymentAmountDouble = Math.floor(paymentAmountDouble * 100) / 100;
        BigDecimal paymentAmount = BigDecimal.valueOf(paymentAmountDouble);

        return Transaction.builder()
                .transactionId(transactionId)
                .payeeId(payeeId)
                .beneficiaryId(beneficiaryId)
                .paymentAmount(paymentAmount)
                .paymentType(paymentType(transactionId))
                .eventTime(System.currentTimeMillis())
                .build();
    }

    public Transaction generateOne() {
        return randomEvent(new SplittableRandom());
    }

    private static Transaction.PaymentType paymentType(long id) {
        int name = (int) (id % 2);
        switch (name) {
            case 0:
                return Transaction.PaymentType.CRD;
            case 1:
                return Transaction.PaymentType.CSH;
            default:
                throw new IllegalStateException("");
        }
    }

    @Override
    public final void run() {
        running = true;

        final SplittableRandom rnd = new SplittableRandom();

        while (running) {
            Transaction event = randomEvent(rnd);
            log.debug("{}", event);
            consumer.accept(event);
            try {
                throttler.throttle();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        log.info("Finished run()");
    }

    public final void cancel() {
        running = false;
        log.info("Cancelled");
    }
}