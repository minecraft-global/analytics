package global.minecraft.MinecraftGlobalAnalytics;

import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

public class StatsRecord {
    @SerializedName("cpu_percent")
    private final float cpuPercent;
    @SerializedName("mem_usage_bytes")
    private final long memUsageBytes;
    @SerializedName("ticks_per_second")
    private final float ticksPerSecond;
    @SerializedName("world_size_bytes")
    private final long worldSizeBytes;
    @SerializedName("chat_msgs_since")
    private final long chatMessages;

    public StatsRecord(float cP, long mU, float tP, long wS, long cM) {
        cpuPercent = cP;
        memUsageBytes = mU;
        ticksPerSecond = tP;
        worldSizeBytes = wS;
        chatMessages = cM;
    }

    public String toJsonString() {
        return new GsonBuilder().create().toJson(this);
    }
}
