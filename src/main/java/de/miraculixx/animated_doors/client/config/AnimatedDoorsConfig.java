package de.miraculixx.animated_doors.client.config;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Properties;

public final class AnimatedDoorsConfig {
    public static final float MIN_DURATION_SECONDS = 0.0f;
    public static final float MAX_DURATION_SECONDS = 3.0f;
    public static final float DEFAULT_DURATION_SECONDS = 0.3f;

    private static final String DURATION_SECONDS_KEY = "animationDurationSeconds";
    private static final String EASING_KEY = "easing";
    private static final String DOORS_ENABLED_KEY = "doorsEnabled";
    private static final String TRAPDOORS_ENABLED_KEY = "trapdoorsEnabled";
    private static final String FENCE_GATES_ENABLED_KEY = "fenceGatesEnabled";
    private static final String CONNECTED_DOORS_ENABLED_KEY = "connectedDoorsEnabled";
    private static final String CONNECTED_TRAPDOORS_ENABLED_KEY = "connectedTrapdoorsEnabled";
    private static final String CONNECTED_FENCE_GATES_ENABLED_KEY = "connectedFenceGatesEnabled";
    private static final String CONNECTED_BLOCKS_ON_SERVERS_ENABLED_KEY = "connectedBlocksOnServersEnabled";
    private static final AnimatedDoorsConfig INSTANCE = new AnimatedDoorsConfig();

    private float durationSeconds = DEFAULT_DURATION_SECONDS;
    private Easing easing = Easing.EASE_IN_OUT;
    private boolean doorsEnabled = true;
    private boolean trapdoorsEnabled = true;
    private boolean fenceGatesEnabled = true;
    private boolean connectedDoorsEnabled = true;
    private boolean connectedTrapdoorsEnabled = true;
    private boolean connectedFenceGatesEnabled = true;
    private boolean connectedBlocksOnServersEnabled = true;

    private AnimatedDoorsConfig() {
    }

    public static AnimatedDoorsConfig instance() {
        return INSTANCE;
    }

    public void load() {
        Path path = configPath();
        if (!Files.isRegularFile(path)) {
            save();
            return;
        }

        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(path)) {
            properties.load(reader);
            durationSeconds = clamp(parseFloat(properties.getProperty(DURATION_SECONDS_KEY), DEFAULT_DURATION_SECONDS));
            easing = Easing.bySerializedName(properties.getProperty(EASING_KEY));
            doorsEnabled = parseBoolean(properties.getProperty(DOORS_ENABLED_KEY), true);
            trapdoorsEnabled = parseBoolean(properties.getProperty(TRAPDOORS_ENABLED_KEY), true);
            fenceGatesEnabled = parseBoolean(properties.getProperty(FENCE_GATES_ENABLED_KEY), true);
            connectedDoorsEnabled = parseBoolean(properties.getProperty(CONNECTED_DOORS_ENABLED_KEY), true);
            connectedTrapdoorsEnabled = parseBoolean(properties.getProperty(CONNECTED_TRAPDOORS_ENABLED_KEY), true);
            connectedFenceGatesEnabled = parseBoolean(properties.getProperty(CONNECTED_FENCE_GATES_ENABLED_KEY), true);
            connectedBlocksOnServersEnabled = parseBoolean(properties.getProperty(CONNECTED_BLOCKS_ON_SERVERS_ENABLED_KEY), false);
        } catch (IOException ignored) {
            reset();
        }
    }

    public void save() {
        Properties properties = new Properties();
        properties.setProperty(DURATION_SECONDS_KEY, String.format(Locale.ROOT, "%.2f", durationSeconds));
        properties.setProperty(EASING_KEY, easing.serializedName);
        properties.setProperty(DOORS_ENABLED_KEY, Boolean.toString(doorsEnabled));
        properties.setProperty(TRAPDOORS_ENABLED_KEY, Boolean.toString(trapdoorsEnabled));
        properties.setProperty(FENCE_GATES_ENABLED_KEY, Boolean.toString(fenceGatesEnabled));
        properties.setProperty(CONNECTED_DOORS_ENABLED_KEY, Boolean.toString(connectedDoorsEnabled));
        properties.setProperty(CONNECTED_TRAPDOORS_ENABLED_KEY, Boolean.toString(connectedTrapdoorsEnabled));
        properties.setProperty(CONNECTED_FENCE_GATES_ENABLED_KEY, Boolean.toString(connectedFenceGatesEnabled));
        properties.setProperty(CONNECTED_BLOCKS_ON_SERVERS_ENABLED_KEY, Boolean.toString(connectedBlocksOnServersEnabled));

        Path path = configPath();
        try {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path)) {
                properties.store(writer, "AnimatedDoors client config");
            }
        } catch (IOException ignored) {
        }
    }

    public float durationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(float durationSeconds) {
        this.durationSeconds = clamp(durationSeconds);
    }

    public long durationNanos() {
        return Math.max(1L, (long) (durationSeconds * 1_000_000_000L));
    }

    public Easing easing() {
        return easing;
    }

    public void setEasing(Easing easing) {
        this.easing = easing == null ? Easing.EASE_IN_OUT : easing;
    }

    public boolean doorsEnabled() {
        return doorsEnabled;
    }

    public void setDoorsEnabled(boolean doorsEnabled) {
        this.doorsEnabled = doorsEnabled;
    }

    public boolean trapdoorsEnabled() {
        return trapdoorsEnabled;
    }

    public void setTrapdoorsEnabled(boolean trapdoorsEnabled) {
        this.trapdoorsEnabled = trapdoorsEnabled;
    }

    public boolean fenceGatesEnabled() {
        return fenceGatesEnabled;
    }

    public void setFenceGatesEnabled(boolean fenceGatesEnabled) {
        this.fenceGatesEnabled = fenceGatesEnabled;
    }

    public boolean connectedDoorsEnabled() {
        return connectedDoorsEnabled;
    }

    public void setConnectedDoorsEnabled(boolean connectedDoorsEnabled) {
        this.connectedDoorsEnabled = connectedDoorsEnabled;
    }

    public boolean connectedTrapdoorsEnabled() {
        return connectedTrapdoorsEnabled;
    }

    public void setConnectedTrapdoorsEnabled(boolean connectedTrapdoorsEnabled) {
        this.connectedTrapdoorsEnabled = connectedTrapdoorsEnabled;
    }

    public boolean connectedFenceGatesEnabled() {
        return connectedFenceGatesEnabled;
    }

    public void setConnectedFenceGatesEnabled(boolean connectedFenceGatesEnabled) {
        this.connectedFenceGatesEnabled = connectedFenceGatesEnabled;
    }

    public boolean connectedBlocksOnServersEnabled() {
        return connectedBlocksOnServersEnabled;
    }

    public void setConnectedBlocksOnServersEnabled(boolean connectedBlocksOnServersEnabled) {
        this.connectedBlocksOnServersEnabled = connectedBlocksOnServersEnabled;
    }

    public void reset() {
        durationSeconds = DEFAULT_DURATION_SECONDS;
        easing = Easing.EASE_IN_OUT;
        doorsEnabled = true;
        trapdoorsEnabled = true;
        fenceGatesEnabled = true;
        connectedDoorsEnabled = true;
        connectedTrapdoorsEnabled = true;
        connectedFenceGatesEnabled = true;
        connectedBlocksOnServersEnabled = true;
    }

    private static Path configPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("animated_doors.properties");
    }

    private static float parseFloat(String value, float fallback) {
        if (value == null) {
            return fallback;
        }
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static boolean parseBoolean(String value, boolean fallback) {
        return value == null ? fallback : Boolean.parseBoolean(value);
    }

    private static float clamp(float value) {
        return Math.max(MIN_DURATION_SECONDS, Math.min(MAX_DURATION_SECONDS, value));
    }

    public enum Easing {
        LINEAR("linear", "Linear"),
        EASE_IN_OUT("ease_in_out", "Ease In/Out"),
        EASE_OUT("ease_out", "Ease Out"),
        EASE_IN("ease_in", "Ease In");

        private final String serializedName;
        private final String displayName;

        Easing(String serializedName, String displayName) {
            this.serializedName = serializedName;
            this.displayName = displayName;
        }

        public float apply(float value) {
            float clamped = Math.max(0.0f, Math.min(1.0f, value));
            return switch (this) {
                case LINEAR -> clamped;
                case EASE_IN_OUT -> clamped * clamped * (3.0f - 2.0f * clamped);
                case EASE_OUT -> 1.0f - (1.0f - clamped) * (1.0f - clamped);
                case EASE_IN -> clamped * clamped;
            };
        }

        public String displayName() {
            return displayName;
        }

        private static Easing bySerializedName(String name) {
            for (Easing easing : values()) {
                if (easing.serializedName.equals(name)) {
                    return easing;
                }
            }
            return EASE_IN_OUT;
        }
    }
}
