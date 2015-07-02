package version2.prototype;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

public class EASTWebManagerTester {
    private static ArrayList<String> myList;

    public static void main(String[] args) {
        myList = new ArrayList<String>();
        myList.add("Hello World.");
        myList.add("Hello again.");

        ArrayList<Watcher> watchers = new ArrayList<Watcher>(1);
        EASTWebManagerTester tester = new EASTWebManagerTester();
        Watcher watcher = tester.new Watcher();
        Watched watched = tester.new Watched(watcher);

        watchers.add(watcher);
        if(watchers.contains(tester.new Watcher())) {
            System.out.println("True on new object");
        }
        else if(watchers.contains(watcher)) {
            System.out.println("True on watcher object");
        }

        watched.ChangeState(true);
        watched.ChangeState(false);


        for(String str :  GetList())
        {
            System.out.println(str);
        }
        System.out.println(myList.size());

        //        EASTWebManager em = EASTWebManager.GetInstance();
        //        ExecutorService executor = Executors.newSingleThreadExecutor(new ThreadFactory() {
        //            @Override
        //            public Thread newThread(Runnable target) {
        //                final Thread thread = new Thread(target);
        //                //                log.debug("Creating new worker thread");
        //                thread.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
        //                    @Override
        //                    public void uncaughtException(Thread t, Throwable e) {
        //                        //                        log.error("Uncaught Exception", e);
        //                    }
        //                });
        //                return thread;
        //            }
        //        });
        //        executor.execute(em);
        //        executor.shutdown();
        //
        //        System.out.println();
        //        System.out.println("Global Downloaders Running: " + EASTWebManager.GetNumberOfGlobalDownloaders());
        //        System.out.println("Schedulers Running: " + EASTWebManager.GetNumberOfSchedulerResources());
    }

    private static ArrayList<String> GetList()
    {
        ArrayList<String> output = new ArrayList<String>(myList);
        myList.clear();
        return output;
    }

    private static Watcher CreateWatcher(EASTWebManagerTester tester)
    {
        return tester.new Watcher();
    }

    private static Watched CreateWatched(EASTWebManagerTester tester, Watcher watcher)
    {
        return tester.new Watched(watcher);
    }

    private class Watcher implements Observer
    {
        @Override
        public void update(Observable o, Object state) {
            if(state instanceof Boolean)
            {
                Boolean myState = (Boolean) state;
                if(myState) {
                    System.out.println("State is true");
                } else {
                    System.out.println("State is false");
                }
            }
        }
    }

    private class Watched extends Observable
    {
        private Boolean state;

        public Watched(Observer o)
        {
            state = false;
            addObserver(o);
        }

        public void ChangeState(Boolean state)
        {
            synchronized(this)
            {
                this.state = state;
            }
            setChanged();
            notifyObservers(state);     // Automatically calls clearChanged
        }

        public Boolean GetState()
        {
            return state;
        }
    }
}
