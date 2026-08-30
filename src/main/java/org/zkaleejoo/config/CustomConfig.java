package org.zkaleejoo.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.zkaleejoo.OnlyClans;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class CustomConfig {
    private final OnlyClans plugin;
    private final String fileName;
    private FileConfiguration fileConfiguration = null;
    private File file = null;
    private final String folderName;
    private final boolean newFile;

    public CustomConfig(String fileName, String folderName, OnlyClans plugin, boolean newFile) {
        this.fileName = fileName;
        this.folderName = folderName;
        this.plugin = plugin;
        this.newFile = newFile;
    }

    public String getPath() {
        return this.fileName;
    }

    public void registerConfig() {
        if (folderName != null) {
            File folder = new File(plugin.getDataFolder(), folderName);
            if (!folder.exists()) {
                folder.mkdirs();
            }
            file = new File(folder, fileName);
        } else {
            file = new File(plugin.getDataFolder(), fileName);
        }

        if (!file.exists()) {
            if (newFile) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                String resourcePath = (folderName != null) ? folderName + "/" + fileName : fileName;
                plugin.saveResource(resourcePath, false);
            }
        }

        fileConfiguration = new YamlConfiguration();
        try {
            fileConfiguration.load(file);
            if (!newFile) {
                updateConfig();
            }
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public void updateConfig() {
        if (fileName.equalsIgnoreCase("menus.yml")) {
            updateMenusConfig();
            return;
        }

        try {
            String resourcePath = (folderName != null) ? folderName + "/" + fileName : fileName;
            InputStream resourceStream = plugin.getResource(Objects.requireNonNull(resourcePath));

            if (resourceStream == null)
                return;

            YamlConfiguration jarConfig = YamlConfiguration
                    .loadConfiguration(new InputStreamReader(resourceStream, StandardCharsets.UTF_8));

            boolean changed = false;
            for (String key : jarConfig.getKeys(true)) {
                if (!fileConfiguration.contains(key)) {
                    fileConfiguration.set(key, jarConfig.get(key));
                    changed = true;
                }
            }

            if (changed) {
                saveConfig();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateMenusConfig() {
        try {
            String resourcePath = (folderName != null) ? folderName + "/" + fileName : fileName;
            InputStream resourceStream = plugin.getResource(Objects.requireNonNull(resourcePath));

            if (resourceStream == null)
                return;

            YamlConfiguration jarConfig = YamlConfiguration
                    .loadConfiguration(new InputStreamReader(resourceStream, StandardCharsets.UTF_8));

            ConfigurationSection jarMenusSection = jarConfig.getConfigurationSection("menus");
            if (jarMenusSection == null)
                return;

            boolean changed = false;
            for (String menuId : jarMenusSection.getKeys(false)) {
                String menuPath = "menus." + menuId;
                if (!fileConfiguration.contains(menuPath)) {
                    fileConfiguration.set(menuPath, jarMenusSection.get(menuId));
                    changed = true;
                }
            }

            if (changed) {
                saveConfig();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveConfig() {
        try {
            fileConfiguration.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public FileConfiguration getConfig() {
        if (fileConfiguration == null) {
            reloadConfig();
        }
        return fileConfiguration;
    }

    public boolean reloadConfig() {
        if (folderName != null) {
            file = new File(plugin.getDataFolder() + File.separator + folderName, fileName);
        } else {
            file = new File(plugin.getDataFolder(), fileName);
        }

        fileConfiguration = YamlConfiguration.loadConfiguration(file);

        if (!fileName.equalsIgnoreCase("menus.yml")) {
            String resourcePath = (folderName != null) ? folderName + "/" + fileName : fileName;
            InputStream resourceStream = plugin.getResource(Objects.requireNonNull(resourcePath));

            if (resourceStream != null) {
                YamlConfiguration defConfig = YamlConfiguration
                        .loadConfiguration(new InputStreamReader(resourceStream, StandardCharsets.UTF_8));
                fileConfiguration.setDefaults(defConfig);
            }
        } else {
            updateMenusConfig();
        }

        return true;
    }
}
