package de.miraculixx.animated_doors.client.update;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;
import com.mojang.logging.LogUtils;
import de.miraculixx.animated_doors.client.AnimatedDoorsClient;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public final class UpdateManager {
    private static final boolean DEBUG = false;
    private static final String UNKNOWN_VERSION = "unknown";
    private static final String MODRINTH_API = "https://api.modrinth.com/v2/project/animated-doors";
    private static final Gson GSON = new Gson();
    private static final Logger LOGGER = LogUtils.getLogger();

    private UpdateManager() {
    }

    public static VersionInfo checkForUpdates(String loaderName, String gameVersion, String modVersion, Path configDir) throws IOException {
        ModrinthVersion latest = findLatestVersion(loaderName, gameVersion);
        if (latest == null) {
            LOGGER.warn("No version found for {} ({}, {})", AnimatedDoorsClient.MOD_ID, loaderName, gameVersion);
            return new VersionInfo(false, installedVersion(modVersion), UNKNOWN_VERSION);
        }

        String latestVersion = latest.versionNumber == null ? UNKNOWN_VERSION : latest.versionNumber;
        boolean outdated = false;
        if (Objects.equals(latestVersion, modVersion)) {
            LOGGER.info("{} is up to date", AnimatedDoorsClient.MOD_ID);
        } else if (modVersion != null) {
            LOGGER.warn(
                "{} is outdated ({}). Installed: {} -> Latest: {}",
                AnimatedDoorsClient.MOD_ID,
                gameVersion,
                modVersion,
                latestVersion
            );
            outdated = true;
        }

        if (DEBUG && outdated && latest.files != null && !latest.files.isEmpty()) { // never call, mod is client side
            update(latest.files.getFirst(), configDir);
        }

        return new VersionInfo(outdated, installedVersion(modVersion), latestVersion);
    }

    public static void startUpdateChecker(
        String loaderName,
        String gameVersion,
        String modVersion,
        Path configDir
    ) {
        startUpdateChecker(loaderName, gameVersion, modVersion, configDir, info -> {});
    }

    public static CompletableFuture<Void> startUpdateChecker(
        String loaderName,
        String gameVersion,
        String modVersion,
        Path configDir,
        Consumer<VersionInfo> onOutdated
    ) {
        return CompletableFuture.runAsync(() -> {
            try {
                VersionInfo info = checkForUpdates(loaderName, gameVersion, modVersion, configDir);
                if (info.outdated()) {
                    onOutdated.accept(info);
                }
            } catch (Exception exception) {
                LOGGER.warn("Error while checking for updates: {}", exception.getMessage());
            }
        });
    }

    private static ModrinthVersion findLatestVersion(String loaderName, String gameVersion) throws IOException {
        for (String candidateVersion : gameVersionCandidates(gameVersion)) {
            URI target = modrinthVersionsUri(loaderName, candidateVersion);
            ModrinthVersion latest = requestLatestVersion(target);
            if (latest != null) {
                return latest;
            }
        }
        return null;
    }

    private static ModrinthVersion requestLatestVersion(URI target) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) target.toURL().openConnection();
        connection.setRequestProperty("User-Agent", "Miraculixx/AnimatedDoors update checker");
        connection.setConnectTimeout(10_000);
        connection.setReadTimeout(10_000);

        try {
            String content = new String(connection.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            ModrinthVersion[] versions = GSON.fromJson(content, ModrinthVersion[].class);
            if (versions == null || versions.length == 0) {
                return null;
            }
            return versions[0];
        } catch (JsonSyntaxException exception) {
            throw new IOException("Invalid Modrinth response", exception);
        } finally {
            connection.disconnect();
        }
    }

    private static URI modrinthVersionsUri(String loaderName, String gameVersion) {
        String loaders = encodeJsonArray(loaderName);
        String gameVersions = encodeJsonArray(gameVersion);
        return URI.create(MODRINTH_API + "/version?loaders=" + loaders + "&game_versions=" + gameVersions);
    }

    private static String encodeJsonArray(String value) {
        return URLEncoder.encode("[\"" + value + "\"]", StandardCharsets.UTF_8);
    }

    private static List<String> gameVersionCandidates(String gameVersion) {
        List<String> versions = new ArrayList<>();
        versions.add(gameVersion);

        int firstSeparator = gameVersion.indexOf('.');
        int patchSeparator = gameVersion.lastIndexOf('.');
        if (firstSeparator != patchSeparator && patchSeparator > 0) {
            String shortenedVersion = gameVersion.substring(0, patchSeparator);
            if (!shortenedVersion.equals(gameVersion)) {
                versions.add(shortenedVersion);
            }
        }

        return versions;
    }

    private static void update(ModrinthFile file, Path configDir) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) URI.create(file.url).toURL().openConnection();
        connection.setConnectTimeout(10_000);
        connection.setReadTimeout(30_000);

        try {
            byte[] content = connection.getInputStream().readAllBytes();
            Path targetFolder = configDir.resolve("AnimatedDoors").resolve("update");
            Files.createDirectories(targetFolder);
            Path targetFile = targetFolder.resolve(file.filename);
            Files.write(targetFile, content);
        } finally {
            connection.disconnect();
        }
    }

    private static String installedVersion(String modVersion) {
        return modVersion == null ? UNKNOWN_VERSION : modVersion;
    }

    private static final class ModrinthVersion {
        @SerializedName("version_number")
        private String versionNumber;
        private List<ModrinthFile> files;
    }

    private static final class ModrinthFile {
        private String url;
        private String filename;
    }

    public record VersionInfo(boolean outdated, String currentVersion, String latestVersion) {
    }
}
