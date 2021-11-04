package global.minecraft.MinecraftGlobalAnalytics;

public class TPSMeasurer implements Runnable {
    private double startedAt;
    private int ticks;

    public TPSMeasurer() {
        super();

        startedAt = System.currentTimeMillis() / 1000.0;
    }

    @Override
    public void run() {
        ticks += 1;
    }

    public float getAverageTPS() {
        return ticks / (float)((System.currentTimeMillis() / 1000.0) - startedAt);
    }

    public void reset() {
        startedAt = System.currentTimeMillis() / 1000.0;
        ticks = 0;
    }
}
