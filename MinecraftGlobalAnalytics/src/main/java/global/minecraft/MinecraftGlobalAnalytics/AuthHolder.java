package global.minecraft.MinecraftGlobalAnalytics;

public class AuthHolder {
    private boolean defaulted;
    private String value;

    public AuthHolder(String v) {
        defaulted = false;
        value = v;
    }

    public void set(String v) {
        value = v;
    }

    public String get() {
        return value;
    }

    public void setDefaulted(boolean d) {
        defaulted = d;
    }

    public boolean isDefault() {
        return defaulted;
    }
}
