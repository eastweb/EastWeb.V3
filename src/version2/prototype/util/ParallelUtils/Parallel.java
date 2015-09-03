package version2.prototype.util.ParallelUtils;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import version2.prototype.Config;
import version2.prototype.ErrorLog;

public class Parallel {
    private static final int NUM_CORES = Runtime.getRuntime().availableProcessors();
    private static ThreadPoolExecutor forPool = new ThreadPoolExecutor(NUM_CORES * 2, Integer.MAX_VALUE, 1l, TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>(),
            new NamedThreadFactory("Parallel.For", false));
    //    private static final ExecutorService forPool = Executors.newFixedThreadPool(NUM_CORES * 2, new NamedThreadFactory("Parallel.For", false));

    public static <T> void ForEach(final Iterable<T> elements, final Operation<T> operation) {
        // invokeAll blocks for us until all submitted tasks in the call complete
        try {
            //            forPool.invokeAll(createCallables(elements, operation));

            Collection<Callable<Void>> runners = createCallables(elements, operation);
            forPool.allowCoreThreadTimeOut(true);
            forPool.invokeAll(runners);

        } catch (InterruptedException e) {
            ErrorLog.add(Config.getInstance(), "Parallel.ForEach error during custom parallelized for each method.", e);
        }
    }

    public static <T> Collection<Callable<Void>> createCallables(final Iterable<T> elements, final Operation<T> operation) {
        List<Callable<Void>> callables = new LinkedList<Callable<Void>>();
        for (final T elem : elements) {
            callables.add(new Callable<Void>() {
                @Override
                public Void call() {
                    operation.perform(elem);
                    return null;
                }
            });
        }

        return callables;
    }

    public static interface Operation<T> {
        public void perform(T pParameter);
    }
}
