xlentity
===================================
`Server-side` mod `1.21.1` `NeoForge` `21.1.117` (not tested on others).

`xlentity` lets you customize nearly every aspect of mob spawning-attribute boosts, 
endless potion effects, armor and weapon assignment, enchantments and more - by 
editing a single JSON file, without touching any code.  

Please, if you like what I do, put at least a star on Github.

### Configuration `config/xlentity.json`

```json5
{
  // Apply for friendly 0 or 1
  "modifyFriendly": 0, 
  
  "attributes": {
    
    "maxHealth": {
      // 1% for boost health x6
      "1": 6.0,
      // 2% for boost health x5
      "2": 5.0,
      "3": 4.5,
      "5": 4.0,
      "10": 3.5,
      "20": 3.0,
      "80": 2.5
    },
    
    "attackDamage": {
      // 1% for boost damage x4
      "1": 4.0,
      "2": 3.0,
      "3": 2.0,
      "5": 1.5,
      "7": 1.3,
      "10": 1.2
    },
    
    "movementSpeed": {
      // 1% for boost movement speed x1.7
      "1": 1.7,
      "2": 1.4,
      "3": 1.3,
      "4": 1.2,
      "5": 1.1,
      "10": 1.05
    }
  },
  
  "potions": {
    // The effects are endless
    // 5% chance for regeneration1 effect
    "regeneration1": 5,
    // 10% chance for regeneration2 effect
    "regeneration2": 10
  },
  
  "equipment": {
    // 10% that there will be an attempt to issue armor
    "armorDropChance": 10.0,
    // 10% that there will be an attempt to give weapons
    "weaponDropChance": 10.0,
    // 10% that the armor will be enchanted, for each thing we are trying separately to be guaranteed, and not for all things at once
    "armorEnchantChance": 10.0,
    // 10% that the weapon will be enchanted
    "weaponEnchantChance": 10.0,
    
    "armor": {
      // if armorDropChance has worked, then we begin to accidentally try to work every element of armor
      "item": {
        "helmet": 25,
        "chestplate": 25,
        "leggings": 25,
        "boots": 25
      },
      "type": {
        // If we have a helmet, then the type of this helmet is random
        // 0.2% on nether thing
        "nether": 0.2,
        // 1% on diamond thing
        "diamond": 1,
        "iron": 3,
        "gold": 5,
        "chainmail": 10,
        "leather": 20
      }
    },
    
    "weapon": {
      // if weaponDropChance has worked, then we begin to accidentally try to dare weapons
      "item": {
        // 10% for sword
        "sword": 10,
        // 5% for axe
        "axe": 5,
        "pickaxe": 5,
        "shovel": 5
      },
      "type": {
        // 1% for diamond weapon
        "diamond": 1,
        "iron": 3,
        "gold": 5,
        "stone": 6,
        "wooden": 10
      }
    },
    
    "enchant": {
      // Protection is used only for armor, Sharpness for weapons (swords, axes, picks, shovels), Power for onions
      // 5% for sharpness1
      "sharpness1": 5,
      // 4% for sharpness2
      "sharpness2": 4,
      "sharpness3": 3,
      "sharpness4": 2,
      "sharpness5": 1,
      "protection1": 5,
      "protection2": 4,
      "protection3": 3,
      "protection4": 2,
      "power1": 5,
      "power2": 4,
      "power3": 3,
      "power4": 2,
      "power5": 1
    }
  }
}
```