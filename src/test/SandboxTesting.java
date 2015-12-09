package test;


import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import version2.prototype.util.EASTWebQuery;
import version2.prototype.util.EASTWebResult;
import version2.prototype.util.EASTWebResults;

public class SandboxTesting {
    private static ArrayList<String> myList;

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        //        SandboxTesting tester = new SandboxTesting();
        //        testPersistance();
        //        Test3();
        //        test4();
        //        test5();
        //        tester.MinMax_NDWI();
        //        tester.MinMax_EVI();
        //        tester.ModisCompositeStartDays();
        //        tester.testEASTWebResults();

        //        for(int i=1; i<367; i+=8) {
        //            System.out.println(i);
        //        }
    }

    public void testEASTWebResults()
    {
        try {
            String[] zones = EASTWebResults.GetZonesListFromProject("Test Both TRMM 101615", "TRMM3B42RT");
            System.out.println(zones.length + " : " + zones);
            EASTWebQuery query = EASTWebResults.GetEASTWebQuery("EASTWeb", "Test Both TRMM 101615", "TRMM3B42RT", true, true, true, true, true, true, true, zones, "=", 2014, ">",
                    300, new String[]{"TRMM3B42RTIndex"}, new Integer[]{1});
            System.out.println(query.toString());
            ArrayList<EASTWebResult> results = new ArrayList<EASTWebResult>();
            results = EASTWebResults.GetEASTWebResults(query);
            System.out.println(results.size());
        } catch (ClassNotFoundException | SQLException | ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
    }

    public void ModisCompositeStartDays()
    {
        for(int i=1; i < 365; i += 8) {
            System.out.println(i);
        }
    }

    // NDWI6 = (NIR-SWIR)/(NIR+SWIR)
    public void MinMax_NDWI() throws InterruptedException, ExecutionException
    {
        final int MAX = 32766;
        //int NIR, SWIR;
        //double ndwi;
        double currentMax = -999999999;
        double currentMin = Double.MAX_VALUE;
        int numOfCores = Runtime.getRuntime().availableProcessors();
        ExecutorService processWorkerExecutor = Executors.newFixedThreadPool(numOfCores);
        int idxsPerThread = Math.floorDiv(MAX, numOfCores);

        ArrayList<Future<ArrayList<Double>>> results = new ArrayList<Future<ArrayList<Double>>>();
        for(int i=0; i < numOfCores; i++)
        {
            results.add(processWorkerExecutor.submit(new MyCallable1(i * idxsPerThread, idxsPerThread)));
        }
        processWorkerExecutor.shutdown();

        for(Future<ArrayList<Double>> result : results)
        {
            ArrayList<Double> resultValues = result.get();
            if(resultValues.get(0) < currentMin)
            {
                currentMin = resultValues.get(0);
            }
            if(resultValues.get(1) > currentMax)
            {
                currentMax = resultValues.get(1);
            }
        }

        System.out.println("ModisNBARNDWI6 Max Value: " + currentMax);
        System.out.println("ModisNBARNDWI6 Min Value: " + currentMin);


    }

    private class MyCallable1 implements Callable<ArrayList<Double>>
    {
        private final int startIdx;
        private final int numOfIdxsToProcess;

        public MyCallable1(int startIdx, int numOfIdxsToProcess)
        {
            this.startIdx = startIdx;
            this.numOfIdxsToProcess = numOfIdxsToProcess;
        }

        @Override
        public ArrayList<Double> call() throws Exception {
            int NIR, SWIR;
            double ndwi;
            double currentMax = -999999999;
            double currentMin = Double.MAX_VALUE;
            final int MAX = 32766;

            for(NIR=startIdx; (NIR < startIdx + numOfIdxsToProcess) && NIR <= MAX; NIR++)
            {
                for(SWIR=0; SWIR < MAX; SWIR++)
                {
                    if((NIR + SWIR) != 0) {
                        ndwi = (NIR - SWIR) / (NIR + SWIR);
                        if(ndwi > currentMax)
                        {
                            currentMax = ndwi;
                        }
                        if(ndwi < currentMin)
                        {
                            currentMin = ndwi;
                        }
                    }
                }
                System.out.println("Done with NIR=" + NIR);
            }
            ArrayList<Double> output = new ArrayList<Double>(2);
            output.add(currentMin);
            output.add(currentMax);
            return output;
        }
    }

    // EVI = G * (NIR - RED)/(NIR + C1*RED - C2*BLUE + L) where L=1, C1=6, C2=7.5, and G=2.5
    public void MinMax_EVI() throws InterruptedException, ExecutionException
    {
        final int MAX = 32766;
        double currentMax = -999999999;
        double currentMin = Double.MAX_VALUE;
        int numOfCores = Runtime.getRuntime().availableProcessors() - Math.floorDiv(Runtime.getRuntime().availableProcessors(), 4);
        ExecutorService processWorkerExecutor = Executors.newFixedThreadPool(numOfCores);
        int idxsPerThread = Math.floorDiv(MAX, numOfCores);

        System.out.println("Threads being made: " + numOfCores);
        ArrayList<Future<ArrayList<Double>>> results = new ArrayList<Future<ArrayList<Double>>>();
        for(int i=0; i < numOfCores; i++)
        {
            results.add(processWorkerExecutor.submit(new MyCallable2(i * idxsPerThread, idxsPerThread, i)));
        }
        processWorkerExecutor.shutdown();

        for(Future<ArrayList<Double>> result : results)
        {
            ArrayList<Double> resultValues = result.get();
            if(resultValues.get(0) < currentMin)
            {
                currentMin = resultValues.get(0);
            }
            if(resultValues.get(1) > currentMax)
            {
                currentMax = resultValues.get(1);
            }
        }

        System.out.println("ModisNBAREVI Max Value: " + currentMax);
        System.out.println("ModisNBAREVI Min Value: " + currentMin);
    }

    private class MyCallable2 implements Callable<ArrayList<Double>>
    {
        private final int startIdx;
        private final int numOfIdxsToProcess;
        private final int myID;

        public MyCallable2(int startIdx, int numOfIdxsToProcess, int id)
        {
            this.startIdx = startIdx;
            this.numOfIdxsToProcess = numOfIdxsToProcess;
            myID = id;
        }

        @Override
        public ArrayList<Double> call() throws Exception {
            final double G=2.5;
            final int L = 1;
            final int C1 = 6;
            final double C2 = 7.5;
            int NIR, RED, BLUE;
            double evi;
            double currentMax = -999999999;
            double currentMin = Double.MAX_VALUE;
            final int MAX = 32766;
            final int lastIdx = (numOfIdxsToProcess < (MAX - startIdx)) ? (startIdx + numOfIdxsToProcess) : MAX;

            for(NIR=startIdx; (NIR < startIdx + numOfIdxsToProcess) && NIR <= MAX; NIR++)
            {
                //                if(((NIR - startIdx) + myID) % 4 == 1)
                //                {
                //                    Thread.sleep(9000);
                //                }
                //                else if(((NIR - startIdx) + myID) % 4 == 2)
                //                {
                //                    Thread.sleep(6000);
                //                }
                //                else if(((NIR - startIdx) + myID) % 4 == 3)
                //                {
                //                    Thread.sleep(3000);
                //                }

                for(RED=0; RED < MAX; RED++)
                {
                    for(BLUE=0; BLUE < MAX; BLUE++)
                    {
                        if((NIR + C1*RED - C2*BLUE + L) > 0)
                        {
                            evi = G * (NIR - RED)/(NIR + C1*RED - C2*BLUE + L);
                            if(evi > currentMax)
                            {
                                currentMax = evi;
                            }
                            else if(evi < currentMin)
                            {
                                currentMin = evi;
                            }
                        }
                    }
                    if(RED % 10 == 1) {
                        Thread.sleep(1);
                    }
                }
                float temp = ((float)(NIR - startIdx) / (float)(lastIdx - startIdx)) * 100;
                System.out.println("Finshed NIR=" + NIR + ". Thread " + myID + " " + Math.round(temp) + "% Complete. Min=" + String.format("%.2f", currentMin) + ". Max=" + String.format("%.2f", currentMax) + ".");
            }
            ArrayList<Double> output = new ArrayList<Double>(2);
            output.add(currentMin);
            output.add(currentMax);
            return output;
        }
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

        @SuppressWarnings("unused")
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
