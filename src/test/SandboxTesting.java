package test;


import java.util.ArrayList;
import java.util.BitSet;
import java.util.Observable;
import java.util.Observer;

public class SandboxTesting {
    private static ArrayList<String> myList;

    public static void main(String[] args) {
        //        testPersistance();
        //        Test3();
        //        test4();
        test5();
    }

    private static void test5()
    {
        for(int i=0; i < 4; i++)
        {
            System.out.println(Character.toChars('E' + i)[0] +  ".\"FilePath\"");
        }
    }

    private static void test4()
    {
        BitSet bits = new BitSet(1);
        System.out.println("Next clear bit: " + bits.nextClearBit(0));
        bits.set(0);
        System.out.println("Next clear bit: " + bits.nextClearBit(0));
        bits.set(bits.nextClearBit(0));
        System.out.println("Next clear bit: " + bits.nextClearBit(0));
    }

    private static void testPersistance() {
        PersistanceTester pTest = new PersistanceTester();
        System.out.println(pTest.getList().size());
        pTest.addValue(1);
        System.out.println(pTest.getList().size());
        pTest.addValue(2);
        System.out.println(pTest.getList().size());

        pTest = null;

        pTest = new PersistanceTester();
        System.out.println(pTest.getList().size());
        pTest.addValue(1);
        System.out.println(pTest.getList().size());
        pTest.addValue(2);
        System.out.println(pTest.getList().size());

        pTest = null;

        PersistanceTester pTest1 = new PersistanceTester();
        System.out.println(pTest1.getList().size());
        pTest1.addValue(1);
        System.out.println(pTest1.getList().size());
        pTest1.addValue(2);
        System.out.println(pTest1.getList().size());

        pTest1 = null;

        pTest1 = new PersistanceTester();
        System.out.println(pTest1.getList().size());
        pTest1.addValue(1);
        System.out.println(pTest1.getList().size());
        pTest1.addValue(2);
        System.out.println(pTest1.getList().size());

        pTest1 = null;
    }

    private static void Test3()
    {
        myList = new ArrayList<String>();
        myList.add("Hello World.");
        myList.add("Hello again.");

        ArrayList<Watcher> watchers = new ArrayList<Watcher>(1);
        SandboxTesting tester = new SandboxTesting();
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
    }

    private static ArrayList<String> GetList()
    {
        ArrayList<String> output = new ArrayList<String>(myList);
        myList.clear();
        return output;
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

    private static class PersistanceTester{
        private static ArrayList<Integer> values = new ArrayList<Integer>(0); // Needed
        public PersistanceTester()
        {
        }

        public void addValue(int n)
        {
            values.add(n);
        }

        public ArrayList<Integer> getList()
        {
            return values;
        }
    }
}
