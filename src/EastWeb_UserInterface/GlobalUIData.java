package EastWeb_UserInterface;

import java.util.ArrayList;
import java.util.Collections;

import EastWeb_UserInterface.PluginWindow.PluginExtension.Modis.ModisEvent;
import EastWeb_UserInterface.PluginWindow.PluginExtension.Modis.ModisListener;

public class GlobalUIData {

    private GlobalUIData ()
    {

    }

    private static int id;

    // modis global
    private static ArrayList<String> globalModisTiles;
    private static ModisEvent modisEvent;

    public ArrayList<String> GetModisTiles()
    {
        if(globalModisTiles == null) {
            globalModisTiles = new ArrayList<String>();
        }

        return globalModisTiles;
    }

    public void SetModisTiles(ArrayList<String> modisTiles)
    {
        if(globalModisTiles == null) {
            globalModisTiles = new ArrayList<String>();
        }
        if(equalLists(globalModisTiles, modisTiles)){
            return;
        }
        else
        {
            globalModisTiles.clear();

            for(String m:modisTiles)
            {
                globalModisTiles.add(m);
            }

            modisEvent.fire();
        }
    }

    public void AddModisTile(String modisTile)
    {
        if(globalModisTiles == null) {
            globalModisTiles = new ArrayList<String>();
        }

        globalModisTiles.add(modisTile);
    }

    public void AddModisListner(ModisListener m)
    {
        if(modisEvent == null) {
            modisEvent = new ModisEvent();
        }

        modisEvent.addListener(m);
    }

    public static GlobalUIData Instance()
    {
        if(instance == null) {
            instance = new GlobalUIData ();
        }
        return instance;
    }
    private static GlobalUIData instance;

    public static void ClearInstance()
    {
        globalModisTiles = null;
        id = 0;
        instance = null;
    }

    public int GetId() {
        id ++;
        return id;
    }

    public  boolean equalLists(ArrayList<String> one, ArrayList<String> two){
        if (one == null && two == null){
            return true;
        }

        if((one == null && two != null)
                || one != null && two == null
                || one.size() != two.size()){
            return false;
        }

        //to avoid messing the order of the lists we will use a copy
        //as noted in comments by A. R. S.
        one = new ArrayList<String>(one);
        two = new ArrayList<String>(two);

        Collections.sort(one);
        Collections.sort(two);
        return one.equals(two);
    }
}
