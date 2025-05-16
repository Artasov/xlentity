/*--------------------------------------------------------------------
 *  File: src/main/java/com/xlentity/main/EntitySpawnHandler.java
 *------------------------------------------------------------------*/
package com.xlentity.main;

import com.xlentity.Core;
import com.xlentity.config.Config;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.LevelAccessor;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@EventBusSubscriber(modid = Core.MODID)
public final class EntitySpawnHandler {

    /* ------------------------------------------------------------------
     *  helpers to pull live config maps
     * ---------------------------------------------------------------- */
    private static Map<Integer, Double> HEALTH() {
        return Config.HEALTH_CONFIG;
    }

    private static Map<Integer, Double> DAMAGE() {
        return Config.DAMAGE_CONFIG;
    }

    private static Map<Integer, Double> SPEED() {
        return Config.SPEED_CONFIG;
    }

    /**
     * true ⇢ моб должен быть изменён
     */
    private static boolean shouldModify(Mob mob) {
        return Config.MODIFY_FRIENDLY || mob.getType().getCategory() == MobCategory.MONSTER;
    }

    /* ==================================================================
     *  Первый этап — изменяем атрибуты / выдаём экипировку
     *  (FinalizeSpawn срабатывает ДО помещения сущности в мир)
     * =================================================================*/
    @SubscribeEvent
    public static void onFinalizeSpawn(FinalizeSpawnEvent event) {
        if (!(event.getEntity() instanceof Mob mob)) return;
        if (!shouldModify(mob)) return;

        LevelAccessor level = event.getLevel();
        RandomSource rnd = level.getRandom();

        /* --- 1. атрибуты ------------------------------------------------*/
        boolean hpChanged =
                modifyAttribute(mob, Attributes.MAX_HEALTH, rnd, HEALTH());

        modifyAttribute(mob, Attributes.ATTACK_DAMAGE, rnd, DAMAGE());
        modifyAttribute(mob, Attributes.MOVEMENT_SPEED, rnd, SPEED());

        /* после изменения MAX_HEALTH сразу лечим моба до нового максимума */
        if (hpChanged) mob.setHealth(mob.getMaxHealth());

        /* --- 2. экипировка ---------------------------------------------*/
        if (rnd.nextDouble() < Config.ARMOR_DROP_CHANCE / 100.0) equipArmor(mob, rnd);
        if (rnd.nextDouble() < Config.WEAPON_DROP_CHANCE / 100.0
                && !(mob instanceof AbstractSkeleton)) equipWeapon(mob, rnd);
    }

    /* ==================================================================
     *  Второй этап — то, что должно применяться, когда моб УЖЕ в мире:
     *  endless-potions и луки скелетов
     * =================================================================*/
    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof Mob mob)) return;
        if (!shouldModify(mob)) return;

        RandomSource rnd = mob.level().getRandom();

        /* --- 1. бесконечные эффекты (работают теперь корректно) --------*/
        addPotionEffects(mob, rnd);

        /* --- 2. лук скелета → Power I–V -------------------------------*/
        if (mob instanceof AbstractSkeleton sk) {
            if (rnd.nextDouble() < Config.WEAPON_ENCHANT_CHANCE / 100.0) {
                ItemStack bow = sk.getMainHandItem();
                if (bow.is(Items.BOW))
                    enchantItem(bow, rnd, ItemType.BOW, sk);
            }
        }
    }

    /* ==================================================================
     *  Броня
     * =================================================================*/
    private static void equipArmor(Mob mob, RandomSource rnd) {
        for (Entry<String, Double> slotChance : Config.ARMOR_ITEM.entrySet()) {
            if (rnd.nextDouble() >= slotChance.getValue() / 100.0) continue;

            String material = getWeighted(rnd, Config.ARMOR_TYPE);
            if (material == null) continue;

            EquipmentSlot slot = switch (slotChance.getKey()) {
                case "helmet" -> EquipmentSlot.HEAD;
                case "chestplate" -> EquipmentSlot.CHEST;
                case "leggings" -> EquipmentSlot.LEGS;
                case "boots" -> EquipmentSlot.FEET;
                default -> null;
            };
            if (slot == null) continue;

            ItemStack stack = new ItemStack(getArmorItem(material, slot));
            if (stack.isEmpty()) continue;

            if (rnd.nextDouble() < Config.ARMOR_ENCHANT_CHANCE / 100.0)
                enchantItem(stack, rnd, ItemType.ARMOR, mob);

            mob.setItemSlot(slot, stack);
        }
    }

    private static Item getArmorItem(String type, EquipmentSlot slot) {
        return switch (type) {
            case "nether" -> switch (slot) {
                case HEAD -> Items.NETHERITE_HELMET;
                case CHEST -> Items.NETHERITE_CHESTPLATE;
                case LEGS -> Items.NETHERITE_LEGGINGS;
                case FEET -> Items.NETHERITE_BOOTS;
                default -> Items.AIR;
            };
            case "diamond" -> switch (slot) {
                case HEAD -> Items.DIAMOND_HELMET;
                case CHEST -> Items.DIAMOND_CHESTPLATE;
                case LEGS -> Items.DIAMOND_LEGGINGS;
                case FEET -> Items.DIAMOND_BOOTS;
                default -> Items.AIR;
            };
            case "iron" -> switch (slot) {
                case HEAD -> Items.IRON_HELMET;
                case CHEST -> Items.IRON_CHESTPLATE;
                case LEGS -> Items.IRON_LEGGINGS;
                case FEET -> Items.IRON_BOOTS;
                default -> Items.AIR;
            };
            case "gold" -> switch (slot) {
                case HEAD -> Items.GOLDEN_HELMET;
                case CHEST -> Items.GOLDEN_CHESTPLATE;
                case LEGS -> Items.GOLDEN_LEGGINGS;
                case FEET -> Items.GOLDEN_BOOTS;
                default -> Items.AIR;
            };
            case "chainmail" -> switch (slot) {
                case HEAD -> Items.CHAINMAIL_HELMET;
                case CHEST -> Items.CHAINMAIL_CHESTPLATE;
                case LEGS -> Items.CHAINMAIL_LEGGINGS;
                case FEET -> Items.CHAINMAIL_BOOTS;
                default -> Items.AIR;
            };
            case "leather" -> switch (slot) {
                case HEAD -> Items.LEATHER_HELMET;
                case CHEST -> Items.LEATHER_CHESTPLATE;
                case LEGS -> Items.LEATHER_LEGGINGS;
                case FEET -> Items.LEATHER_BOOTS;
                default -> Items.AIR;
            };
            default -> Items.AIR;
        };
    }

    /* ==================================================================
     *  Оружие (не для скелетов)
     * =================================================================*/
    private static void equipWeapon(Mob mob, RandomSource rnd) {
        String category = getWeighted(rnd, Config.WEAPON_ITEM);
        String material = getWeighted(rnd, Config.WEAPON_TYPE);
        if (category == null || material == null) return;

        ItemStack stack = new ItemStack(getWeaponItem(material, category));
        if (stack.isEmpty()) return;

        if (rnd.nextDouble() < Config.WEAPON_ENCHANT_CHANCE / 100.0)
            enchantItem(stack, rnd, ItemType.MELEE, mob);

        mob.setItemSlot(EquipmentSlot.MAINHAND, stack);
    }

    private static Item getWeaponItem(String mat, String cat) {
        return switch (cat) {
            case "sword" -> switch (mat) {
                case "nether", "diamond" -> Items.DIAMOND_SWORD;
                case "iron" -> Items.IRON_SWORD;
                case "gold" -> Items.GOLDEN_SWORD;
                case "stone" -> Items.STONE_SWORD;
                case "wooden" -> Items.WOODEN_SWORD;
                default -> Items.AIR;
            };
            case "axe" -> switch (mat) {
                case "nether", "diamond" -> Items.DIAMOND_AXE;
                case "iron" -> Items.IRON_AXE;
                case "gold" -> Items.GOLDEN_AXE;
                case "stone" -> Items.STONE_AXE;
                case "wooden" -> Items.WOODEN_AXE;
                default -> Items.AIR;
            };
            case "pickaxe" -> switch (mat) {
                case "nether", "diamond" -> Items.DIAMOND_PICKAXE;
                case "iron" -> Items.IRON_PICKAXE;
                case "gold" -> Items.GOLDEN_PICKAXE;
                case "stone" -> Items.STONE_PICKAXE;
                case "wooden" -> Items.WOODEN_PICKAXE;
                default -> Items.AIR;
            };
            case "shovel" -> switch (mat) {
                case "nether", "diamond" -> Items.DIAMOND_SHOVEL;
                case "iron" -> Items.IRON_SHOVEL;
                case "gold" -> Items.GOLDEN_SHOVEL;
                case "stone" -> Items.STONE_SHOVEL;
                case "wooden" -> Items.WOODEN_SHOVEL;
                default -> Items.AIR;
            };
            default -> Items.AIR;
        };
    }

    /* ==================================================================
     *  Энчант
     * =================================================================*/
    private enum ItemType {ARMOR, MELEE, BOW}

    private static void enchantItem(ItemStack stack, RandomSource rnd,
                                    ItemType type, Mob mob) {

        /* --- 1. подходящие ключи --------------------------------------*/
        Map<String, Double> pool = new HashMap<>();
        for (Entry<String, Double> e : Config.ENCHANT.entrySet()) {
            String k = e.getKey();
            if (type == ItemType.MELEE && k.startsWith("sharpness")) pool.put(k, e.getValue());
            else if (type == ItemType.ARMOR && k.startsWith("protection")) pool.put(k, e.getValue());
            else if (type == ItemType.BOW && k.startsWith("power")) pool.put(k, e.getValue());
        }
        if (pool.isEmpty()) return;

        /* --- 2. взвешенный выбор --------------------------------------*/
        String chosen = getWeighted(rnd, pool);
        if (chosen == null) return;

        Matcher m = Pattern.compile("([a-z_]+)(\\d+)").matcher(chosen);
        if (!m.matches()) return;
        String base = m.group(1);
        int lvl = Integer.parseInt(m.group(2));

        /* --- 3. ResourceKey<Enchantment> ------------------------------*/
        ResourceKey<Enchantment> key = switch (base) {
            case "sharpness" -> Enchantments.SHARPNESS;
            case "protection" -> Enchantments.PROTECTION;
            case "power" -> Enchantments.POWER;
            default -> null;
        };
        if (key == null) return;

        /* --- 4. Holder<Enchantment> ----------------------------------*/
        Holder<Enchantment> holder =
                mob.registryAccess()
                        .registryOrThrow(Registries.ENCHANTMENT)
                        .getHolderOrThrow(key);

        /* --- 5. наложить чару ----------------------------------------*/
        stack.enchant(holder, lvl);
    }

    /* ==================================================================
     *  Endless potion-effects
     * =================================================================*/
    private static void addPotionEffects(Mob mob, RandomSource rnd) {
        Pattern p = Pattern.compile("([a-z_:]+)(\\d+)", Pattern.CASE_INSENSITIVE);

        for (Entry<String, Double> e : Config.POTIONS.entrySet()) {
            if (rnd.nextDouble() >= e.getValue() / 100.0) continue;

            Matcher m = p.matcher(e.getKey());
            if (!m.matches()) continue;

            ResourceLocation rl = ResourceLocation.parse(m.group(1));
            int amplifier = Integer.parseInt(m.group(2)) - 1;

            Holder<MobEffect> eff =
                    mob.registryAccess()
                            .registryOrThrow(Registries.MOB_EFFECT)
                            .getHolderOrThrow(ResourceKey.create(Registries.MOB_EFFECT, rl));

            mob.addEffect(
                    new MobEffectInstance(
                            eff,
                            Integer.MAX_VALUE,
                            amplifier,
                            false,
                            false
                    ));
        }
    }

    /* ==================================================================
     *  Атрибуты и взвешенные утилиты
     * =================================================================*/

    /**
     * @return true — если значение действительно изменялось
     */
    private static boolean modifyAttribute(Mob mob, Holder<Attribute> attr,
                                           RandomSource rnd, Map<Integer, Double> cfg) {
        var inst = mob.getAttribute(attr);
        if (inst == null) return false;

        if (rnd.nextDouble() < sum(cfg) / 100.0) {
            inst.setBaseValue(inst.getBaseValue() * weighted(cfg, rnd));
            return true;
        }
        return false;
    }

    private static int sum(Map<Integer, ?> m) {
        return m.keySet().stream().mapToInt(Integer::intValue).sum();
    }

    private static double weighted(Map<Integer, Double> cfg, RandomSource rnd) {
        int total = sum(cfg), roll = rnd.nextInt(total) + 1, cum = 0;
        for (Entry<Integer, Double> e : cfg.entrySet()) {
            cum += e.getKey();
            if (roll <= cum) return e.getValue();
        }
        return 1.0;
    }

    private static String getWeighted(RandomSource rnd, Map<String, Double> map) {
        double total = map.values().stream().mapToDouble(d -> d).sum();
        if (total <= 0) return null;
        double roll = rnd.nextDouble() * total, cum = 0;
        for (Entry<String, Double> e : map.entrySet()) {
            cum += e.getValue();
            if (roll <= cum) return e.getKey();
        }
        return null;
    }
}
