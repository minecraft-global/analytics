package global.minecraft.MinecraftGlobalAnalytics;

import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

public class StatsRecord {
    @SerializedName("cpu_percent")
    private float cpuPercent;
    @SerializedName("mem_usage_bytes")
    private long memUsageBytes;
    @SerializedName("ticks_per_second")
    private float ticksPerSecond;
    @SerializedName("world_size_bytes")
    private long worldSizeBytes;
    @SerializedName("chat_messages")
    private long chatMessages;

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
