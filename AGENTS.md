# AGENTS.md

Инструкции для `xlentity`.

- Отвечай на русском, коротко и по делу.
- Текстовые файлы сохраняй в UTF-8 без BOM.
- Для поиска используй `rg` / `rg --files`.
- Сборку Gradle можно запускать для проверки изменений.
- Не трогай чужие несвязанные изменения.

## Что это

`xlentity` - серверный NeoForge-мод для контроля мобов на XLMine.
Он меняет параметры сущностей при спавне и управляется конфигом `config/xlentity.json`.

## Что меняет

- На `FinalizeSpawnEvent` меняет атрибуты мобов по конфигу: здоровье, урон, скорость, follow range и другие поддержанные параметры.
- Может выдавать мобам броню/оружие и настраивать шансы выпадения экипировки.
- Учитывает категории/типы сущностей из конфига.
- Добавляет команду `/xlentity reload` для перечитывания `xlentity.json` без перезапуска.

## Важные файлы

- `src/main/java/com/xlentity/Core.java` - точка входа мода.
- `src/main/java/com/xlentity/main/EntitySpawnHandler.java` - применение настроек при спавне.
- `src/main/java/com/xlentity/config/Config.java` - структура и загрузка `config/xlentity.json`.
- `src/main/java/com/xlentity/command/ReloadCommand.java` - команда reload.
- `src/main/templates/META-INF/neoforge.mods.toml` - metadata мода.

## Проверка

- `./gradlew build`
