# ✨ Animated & Connected Doors*

*Actually doors, trapdoors and fence gates! But that would be too long of a name, right?

Add a fancy opening and closing animation to your doors, trapdoors & fence gates.<br>
Two connecting doors/trapdoors/gates will open together if only one side is opened (and closed).

All animations & connections are fully client-sided. 
To make connections work with redstone or similar, install this mod on your server too!<br>
Compatible with Sodium/Iris (Shaders may show invalid shadows on doors), custom models & other mods.

![Image](https://i.postimg.cc/FFMWbmsH/animated-doors.webp)<br>
View the [**Galery**](https://modrinth.com/mod/animated-doors/gallery) for more previews

## ⚙️ Configuration
The settings menu is accessible via the Mod Menu (Fabric - requires ModMenu).

|     **Setting**     | **Description**                                                                                  |  **Default**  |
|:-------------------:|--------------------------------------------------------------------------------------------------|:-------------:|
| **Animation Speed** | Adjust the speed of the opening and closing animation.                                           |     `0.3`     |
|     **Easing**      | Change how the animation is smoothed. Linear removes smoothing                                   | `Ease In/Out` |
|  **Block Toggle**   | Enable/disable animation for doors, trapdoors or fence gates                                     |  All enabled  |
| **Server Linking**  | Enable/disable client-side linking on servers. This can help avoiding anti-cheat false positives |   `Enabled`   |
| **Linking Toggle**  | Enable/disable linking for doors, trapdoors or fence gates                                       |  All enabled  |
Tip: Making animations very slow can look epic on edits or story videos!

This pack also overrides vanilla door/trapdoor/gate models to make the UV consistent and avoiding jumping pixels.
Resource packs/mods that add custom models may reintroduce those UV inconsistencies, you can ask them to fix that too! ([examples](https://github.com/MiraculixxT/AnimatedDoors/tree/main/src/main/resources/assets))

## DataPack Version
There are also DataPack versions available for vanilla environments.
Those are **lite** versions and only support vanilla doors and require [this ResourcePack](https://modrinth.com/resourcepack/animated-door) to work!

Note: The DataPack uses entities to fake the animation to players, this is not intended for large scale use!
<details><summary>DataPack Troubleshooting</summary>

This datapack was originally started by [Scommander](https://www.planetminecraft.com/data-pack/animated-double-doors-better-doors/) and discontinued<br>
I picked it up and optimized a few things and also ported it into the newer versions to support all doors again.<br>

If you want to use this pack in your adventure map, please credit it by adding the modrinth link and name somewhere visible!

**Q:** My doors are invisible!<br>
**A:** On first installation, all previous placed doors needs to update once before they are visible. Just look at them once.


**Q:** There are floating iron ingots now! / My doors are not animated!<br>
**A:** If you see floating iron ingots at doors or your doors simply are not animated you need to activate the resource pack.


**Q:** How do i uninstall Animated Doors?<br>
**A:** To uninstall Animated Doors you need to enter `/function anim-doors:uninstall` in every region where doors exist or simply remove the datapack and enter `/kill @e[tag=_betdor_marker]` everytime you find a region that was not loaded yet.


**Q:** How do i add custom/modded doors?<br>
**A:** While you could edit the datapack to add custom doors, it's not advised and very tedious. Please use the mod version instead

</details>

## 👀 In search of the cake™
![Is the cake real?](https://i.imgur.com/SRGKZYd.gif)<br>