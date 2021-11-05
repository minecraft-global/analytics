package global.minecraft.MinecraftGlobalAnalytics;

public class TPSMeasurer implements Runnable {
    private double startedAt = -1;
    private int ticks;

    @Override
    public void run() {
        if (startedAt == -1) startedAt = System.currentTimeMillis() / 1000.0;
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
