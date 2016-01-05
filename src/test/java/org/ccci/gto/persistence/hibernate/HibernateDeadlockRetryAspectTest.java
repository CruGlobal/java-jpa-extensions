package org.ccci.gto.persistence.hibernate;

import static org.junit.Assert.assertEquals;

import org.ccci.gto.persistence.tx.RetryingTransactionService;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;
import javax.persistence.Table;
import javax.persistence.Version;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"deadlocks.xml"})
public class HibernateDeadlockRetryAspectTest {
    private static final Logger LOG = LoggerFactory.getLogger(HibernateDeadlockRetryAspectTest.class);

    private static final int SLEEP_TIME = 100;
    private static final int THREADS = 3;

    private static final SecureRandom RAND = new SecureRandom();
    private static ExecutorService EXECUTOR;

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private RetryingTransactionService txService;

    @BeforeClass
    public static void createExecutor() {
        EXECUTOR = Executors.newFixedThreadPool(THREADS);
    }

    @AfterClass
    public static void closeExecutor() {
        EXECUTOR.shutdownNow();
        EXECUTOR = null;
    }

    @Test
    public void testVariousDeadlockErrors() throws Throwable {
        for (final LockModeType lockMode : LockModeType.values()) {
            triggerDeadlocks(lockMode, true, true);
            triggerDeadlocks(lockMode, true, false);
            triggerDeadlocks(lockMode, false, true);
            triggerDeadlocks(lockMode, false, false);
        }
    }

    private void triggerDeadlocks(final LockModeType lockMode, final boolean shuffle, final boolean flush)
            throws Throwable {
        // create the objects
        final List<Integer> ids = new ArrayList<>();
        for (int i = 0; i < THREADS; i++) {
            ids.add(RAND.nextInt(Integer.MAX_VALUE));
        }
        txService.inTransaction(new Runnable() {
            @Override
            public void run() {
                for (int id : ids) {
                    em.merge(new SimpleObject(id));
                }
            }
        });

        // execute simultaneous updates
        final Callable<Void> task = new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                // shuffle list order
                final List<Integer> shuffled = new ArrayList<>(ids);
                if (shuffle) {
                    Collections.shuffle(shuffled, RAND);
                }

                // execute actual transaction
                return txService.inRetryingTransaction(new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        for (final int id : shuffled) {
                            final SimpleObject obj = em.find(SimpleObject.class, id, lockMode);
                            obj.counter++;
                            if (flush) {
                                em.flush();
                            }
                            Thread.sleep(RAND.nextInt(SLEEP_TIME));
                            LOG.info("updated {} {}", obj.id, obj.counter);
                        }
                        return null;
                    }
                });
            }
        };
        final List<Future<Void>> tasks = new ArrayList<>();
        for (int i = 0; i < THREADS; i++) {
            tasks.add(EXECUTOR.submit(task));
        }

        // wait for all tasks to finish, propagating any exceptions
        for (Future<Void> resp : tasks) {
            try {
                resp.get();
            } catch (final ExecutionException e) {
                throw new AssertionError("deadlock error, lock mode: " + lockMode + " shuffle: " + shuffle +
                        " flush: " + flush, e.getCause());
            }
        }

        // test final results
        txService.inTransaction(new Runnable() {
            @Override
            public void run() {
                for (final int id : ids) {
                    assertEquals(THREADS, em.find(SimpleObject.class, id).counter);
                }
            }
        });
    }

    @Entity(name = "SimpleObject")
    @Table(name = "SimpleObject")
    public static class SimpleObject {
        @Id
        public int id;

        @Version
        public int version;

        public int counter = 0;

        public SimpleObject() {
        }

        SimpleObject(final int id) {
            this.id = id;
        }
    }
}
