package com.xlentity.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashMap;
import java.util.Map;

@SuppressWarnings("unused")
public final class Config {

    /* ---------- PUBLIC: структуры, которыми пользуются остальные классы ---------- */

    public static final Map<Integer, Double> HEALTH_CONFIG = new LinkedHashMap<>();
    public static final Map<Integer, Double> DAMAGE_CONFIG = new LinkedHashMap<>();
    public static final Map<Integer, Double> SPEED_CONFIG = new LinkedHashMap<>();

    public static final Map<String, Double> POTIONS = new LinkedHashMap<>();

    public static volatile double ARMOR_DROP_CHANCE;
    public static volatile double WEAPON_DROP_CHANCE;
    public static volatile double ARMOR_ENCHANT_CHANCE;
    public static volatile double WEAPON_ENCHANT_CHANCE;
    public static volatile boolean MODIFY_FRIENDLY;

    public static final Map<String, Double> ARMOR_ITEM = new LinkedHashMap<>();
    public static final Map<String, Double> ARMOR_TYPE = new LinkedHashMap<>();
    public static final Map<String, Double> WEAPON_ITEM = new LinkedHashMap<>();
    public static final Map<String, Double> WEAPON_TYPE = new LinkedHashMap<>();
    public static final Map<String, Double> ENCHANT = new LinkedHashMap<>();

    /* ---------- внутренняя кухня ---------- */

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FMLPaths.CONFIGDIR.get().resolve("xlentity.json");

    private static ConfigData DATA;   // хранит последнюю загруженную версию

    /* ---------- статический блок ---------- */

    static {
        DATA = loadOrCreate();
        applyToStatics(DATA);
    }

    private Config() {
    }  // utility-class

    /* =====================================================================
     *  PUBLIC   :: /xlentity reload
     * ===================================================================*/
    public static synchronized void reload() {
        DATA = loadOrCreate();
        applyToStatics(DATA);
    }

    /* =====================================================================
     *  JSON-файл: загрузка или автогенерация
     * ===================================================================*/
    private static ConfigData loadOrCreate() {
        try {
            if (Files.notExists(PATH)) {
                ConfigData defaults = createDefaults();
                save(defaults);
                return defaults;
            }
            String json = Files.readString(PATH);
            return GSON.fromJson(json, ConfigData.class);
        } catch (Exception ex) {
            ex.printStackTrace();
            ConfigData fallback = createDefaults();
            save(fallback);
            return fallback;
        }
    }

    private static void save(ConfigData data) {
        try {
            Files.createDirectories(PATH.getParent());
            Files.writeString(PATH, GSON.toJson(data),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    /* =====================================================================
     *  Перекладываем значения в публичные поля
     * ===================================================================*/
    private static void applyToStatics(ConfigData d) {

        syncMap(HEALTH_CONFIG, toIntDoubleMap(d.attributes.maxHealth));
        syncMap(DAMAGE_CONFIG, toIntDoubleMap(d.attributes.attackDamage));
        syncMap(SPEED_CONFIG, toIntDoubleMap(d.attributes.movementSpeed));

        syncMap(POTIONS, toStrDoubleMap(d.potions));

        ARMOR_DROP_CHANCE = d.equipment.armorDropChance;
        WEAPON_DROP_CHANCE = d.equipment.weaponDropChance;
        ARMOR_ENCHANT_CHANCE = d.equipment.armorEnchantChance;
        WEAPON_ENCHANT_CHANCE = d.equipment.weaponEnchantChance;
        MODIFY_FRIENDLY = d.modifyFriendly != 0;

        syncMap(ARMOR_ITEM, toStrDoubleMap(d.equipment.armor.item));
        syncMap(ARMOR_TYPE, toStrDoubleMap(d.equipment.armor.type));
        syncMap(WEAPON_ITEM, toStrDoubleMap(d.equipment.weapon.item));
        syncMap(WEAPON_TYPE, toStrDoubleMap(d.equipment.weapon.type));
        syncMap(ENCHANT, toStrDoubleMap(d.equipment.enchant));
    }

    private static <K, V> void syncMap(Map<K, V> dst, Map<K, V> src) {
        dst.clear();
        dst.putAll(src);
    }

    /* =====================================================================
     *  helpers
     * ===================================================================*/
    private static LinkedHashMap<Integer, Double> toIntDoubleMap(Map<String, Double> src) {
        LinkedHashMap<Integer, Double> out = new LinkedHashMap<>();
        if (src != null)
            src.forEach((k, v) -> {
                try {
                    out.put(Integer.parseInt(k), v);
                } catch (NumberFormatException ignored) {
                }
            });
        return out;
    }

    private static LinkedHashMap<String, Double> toStrDoubleMap(Map<String, Double> src) {
        return src == null ? new LinkedHashMap<>() : new LinkedHashMap<>(src);
    }

    /* =====================================================================
     *  Полный дефолт – 1-в-1 как в образце JSON
     * ===================================================================*/
    private static ConfigData createDefaults() {
        ConfigData d = new ConfigData();

        /* --- глобальный флаг ------------------------------------------- */
        d.modifyFriendly = 0; // 0 – friendly-мобы НЕ затрагиваются

        /* --- атрибуты --------------------------------------------------- */
        d.attributes.maxHealth.put("1", 6.0);
        d.attributes.maxHealth.put("2", 5.0);
        d.attributes.maxHealth.put("3", 4.5);
        d.attributes.maxHealth.put("5", 4.0);
        d.attributes.maxHealth.put("10", 3.5);
        d.attributes.maxHealth.put("20", 3.0);
        d.attributes.maxHealth.put("80", 2.5);

        d.attributes.attackDamage.put("1", 4.0);
        d.attributes.attackDamage.put("2", 3.0);
        d.attributes.attackDamage.put("3", 2.0);
        d.attributes.attackDamage.put("5", 1.5);
        d.attributes.attackDamage.put("7", 1.3);
        d.attributes.attackDamage.put("10", 1.2);

        d.attributes.movementSpeed.put("1", 1.7);
        d.attributes.movementSpeed.put("2", 1.4);
        d.attributes.movementSpeed.put("3", 1.3);
        d.attributes.movementSpeed.put("4", 1.2);
        d.attributes.movementSpeed.put("5", 1.1);
        d.attributes.movementSpeed.put("10", 1.05);

        /* --- бесконечные эффекты --------------------------------------- */
        d.potions.put("regeneration1", 5.0);
        d.potions.put("regeneration2", 10.0);

        /* --- шансы ------------------------------------------------------ */
        d.equipment.armorDropChance = 10.0;
        d.equipment.weaponDropChance = 10.0;
        d.equipment.armorEnchantChance = 10.0;
        d.equipment.weaponEnchantChance = 10.0;

        /* armor → item-slots */
        d.equipment.armor.item.put("helmet", 25.0);
        d.equipment.armor.item.put("chestplate", 25.0);
        d.equipment.armor.item.put("leggings", 25.0);
        d.equipment.armor.item.put("boots", 25.0);

        /* armor → material */
        d.equipment.armor.type.put("nether", 0.2);
        d.equipment.armor.type.put("diamond", 1.0);
        d.equipment.armor.type.put("iron", 3.0);
        d.equipment.armor.type.put("gold", 5.0);
        d.equipment.armor.type.put("chainmail", 10.0);
        d.equipment.armor.type.put("leather", 20.0);

        /* weapon → item-categories */
        d.equipment.weapon.item.put("sword", 10.0);
        d.equipment.weapon.item.put("axe", 5.0);
        d.equipment.weapon.item.put("pickaxe", 5.0);
        d.equipment.weapon.item.put("shovel", 5.0);

        /* weapon → material */
        d.equipment.weapon.type.put("diamond", 1.0);
        d.equipment.weapon.type.put("iron", 3.0);
        d.equipment.weapon.type.put("gold", 5.0);
        d.equipment.weapon.type.put("stone", 6.0);
        d.equipment.weapon.type.put("wooden", 10.0);

        /* enchants */
        d.equipment.enchant.put("sharpness1", 5.0);
        d.equipment.enchant.put("sharpness2", 4.0);
        d.equipment.enchant.put("sharpness3", 3.0);
        d.equipment.enchant.put("sharpness4", 2.0);
        d.equipment.enchant.put("sharpness5", 1.0);

        d.equipment.enchant.put("protection1", 5.0);
        d.equipment.enchant.put("protection2", 4.0);
        d.equipment.enchant.put("protection3", 3.0);
        d.equipment.enchant.put("protection4", 2.0);

        d.equipment.enchant.put("power1", 5.0);
        d.equipment.enchant.put("power2", 4.0);
        d.equipment.enchant.put("power3", 3.0);
        d.equipment.enchant.put("power4", 2.0);
        d.equipment.enchant.put("power5", 1.0);

        return d;
    }

    /* =====================================================================
     *  POJO-структуры для GSON
     * ===================================================================*/
    private static final class ConfigData {
        int modifyFriendly = 0;
        AttributeSection attributes = new AttributeSection();
        Map<String, Double> potions = new LinkedHashMap<>();
        EquipmentSection equipment = new EquipmentSection();
    }

    private static final class AttributeSection {
        Map<String, Double> maxHealth = new LinkedHashMap<>();
        Map<String, Double> attackDamage = new LinkedHashMap<>();
        Map<String, Double> movementSpeed = new LinkedHashMap<>();
    }

    private static final class EquipmentSection {
        double armorDropChance = 10.0;
        double weaponDropChance = 10.0;
        double armorEnchantChance = 10.0;
        double weaponEnchantChance = 10.0;
        ArmorSection armor = new ArmorSection();
        WeaponSection weapon = new WeaponSection();
        Map<String, Double> enchant = new LinkedHashMap<>();
    }

    private static final class ArmorSection {
        Map<String, Double> item = new LinkedHashMap<>();
        Map<String, Double> type = new LinkedHashMap<>();
    }

    private static final class WeaponSection {
        Map<String, Double> item = new LinkedHashMap<>();
        Map<String, Double> type = new LinkedHashMap<>();
    }
}
