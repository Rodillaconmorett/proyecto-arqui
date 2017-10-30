package simulation.clock;

public class Clock {

    







    private static Clock instance = null;

    protected Clock() {}

    public static Clock getInstance() {
        if(instance == null){
            instance = new Clock();
        }
        return instance;
    }
}
