# Animated & Connected Doors

Add a fancy opening and closing animation to your doors, trapdoors & fence gates!<br>
Two connecting doors will open together if only one side is opened (and closed).

All animations and door connections work on manual opening/closing and on redstone impulses like buttons, pressure plates, redstone, ... 
This will also work on iron doors, so only one side needs to be powered.

![Image](https://i.imgur.com/SRGKZYd.gif)<br>
View the [**Galery**](https://modrinth.com/mod/animated-doors/gallery) for more previews

## Installation
> The [Resource Pack](https://modrinth.com/datapack/animated-door/versions) is required to work!

The datapack (zip file) needs to be inside the `datapacks` folder inside your (main) world folder.
<details><summary>Single Player</summary>

> **Creating a new world?**

While creating a new world, switch to the last tab at the top named `More` and click on the button `Data Packs`. Now drag and drop the ``AnimatedDoors.zip`` file on your Minecraft window (you might need to confirm) and move the new entry from the left side to the right by clicking on the arrow. Thats it!


> **Already have a world?**

Inside your world list, click on the target world and press the ``Edit`` button. Inside the new menu click on `Open World Folder` which opens your file explorer. Now you need to enter the ``datapacks`` folder or create it if you never installed a datapack before. Move the ``AnimatedDoors.zip`` file inside this datapacks folder and simply join your world. Thats it!
</details>
<details><summary>Multiplayer - Server</summary>

1. Open your server folder with any tool or inside your hosters web explorer.
2. Navigate to your **main** world. Your main world is the world that always loads first (by default `world`. Can be viewed inside the console). If you use Paper or any fork, its always the overworld (also `world` by default).
2. Navigate inside the ``datapacks`` folder
3. Upload the ``AnimatedDoors.zip`` file
4. Restart/Start your server

</details>


You or your players need to use the resource pack that hides vanilla doors and particles (and display the animation).
<details><summary>Single Player</summary>
Download the latest Resource Pack file from the version tab and open the resource pack menu ingame. Here you can drag and drop the resource pack zip file on your Minecraft window and confirm importing.

Make sure you enable it everytime you join the world and disable it while playing on other servers/worlds (or you will not see doors).
</details>
<details><summary>Multiplayer - Server</summary>

You **don`t** need to tell your players they should download the resource pack from this page!

> Just a simple server & no proxy?<br>
1. Open your server folder with any tool or on your hosters web file browser
2. Open/edit the `server.properties` file
3. Find the line `resource-pack:` and paste the following link behind (`resource-pack: <link>`) - <https://cdn.modrinth.com/data/EuloLapn/versions/PnIroRTM/AnimatedDoors-RP.zip>
4. You can set `require-resource-pack: true` to force download the resource pack and `resource-pack-prompt: <message>` for a custom message
5. Save and restart your server


> Multiple worlds/servers or using a proxy?<br>

Install the plugin/mod [MWeb](https://modrinth.com/mod/mweb) to (force) send the resource pack to targeted players if required instead of on joining anywhere. You might need to setup a few things before, learn more at the [Docs](https://mutils.net/mweb)

</details>


## Credits & QA
This datapack was originaly started by [Scommander](https://www.planetminecraft.com/data-pack/animated-double-doors-better-doors/) and discontinued<br>
I picked it up and optimizied a few things and also ported it into the newer versions to support all doors again.<br>
If you have any questions or need Animated Doors for a newer version i did not updated yet hit me up on [Discord](https://dc.mutils.net).

If you want to use this pack on a public server or in your adventure map, please credit it by adding the modrinth link and name somewhere visible!

**Q:** My doors are invisible!<br>
**A:** On first installation, all previous placed doors needs to update once before they are visible. Just look at them once.


**Q:** There are floating iron ingots now! / My doors are not animated!<br>
**A:** If you see floating iron ingots at doors or your doors simply are not animated you need to activate the resource pack.


**Q:** How do i uninstall Animated Doors?<br>
**A:** To uninstall Animated Doors you need to enter `/function anim-doors:uninstall` in every region where doors exist or simply remove the datapack and enter `/kill @e[tag=_betdor_marker]` everytime you find a region that was not loaded yet.


**Q:** How do i add custom/modded doors?<br>
**A:** This involve some editing of the data- & resource-pack files, but i'll try to guide you through:
<details><summary>Guide to add custom doors</summary>

First, unzip the data-pack & resource-pack folders to edit all files inside more easily. You will need the mod-key and door type key for this. Press F3+H to enable item IDs in Minecraft and hover over any item. You will see `<mod-key>:<door-type>` in dark gray.

## Let's start with editing the resource pack:

> **NOTE** - This method only work if your mod follows the vanilla style of adding blocks!


First navigate into the folder ``assets\minecraft\models\block\type``. There are two files (left and right) for each door type. Copy both files for any door type and rename it to `<door_type>_left` and `<door_type>_right`.<br>
Next, open both files in any text editor and replace the top and bottom texture with the path to the path of your door like the following:
```json
{
    "parent": "minecraft:block/main/right_hinge",
    "textures": {
        "top": "<mod-key>:block/<door-type>_top",
        "bottom": "<mod-key>:block/<door-type>_bottom"
    }
}
```


At last, navigate into the folder `assets\minecraft\models\item` and open the `iron_nugget.json` file.<br>
Scroll to the bottom and add for each door you want to add a new entry. I suggest to count up from 1000 to prevent incompatibilities with future updates:
```json
{
    "parent": "item/generated",
    "textures": {
        "layer0": "item/iron_nugget"
    },
    "overrides": [
      ...

      { "predicate": {"custom_model_data": 1001}, "model": "<mod-key>:block/type/<door-type>_right"},
      { "predicate": {"custom_model_data": 1002}, "model": "<mod-key>:block/type/<door-type>_left"}
    ]
}
```

Remember the numbers you assigned to your door! You will need those later and i will mention them with ``<id-1>`` and ``<id-2>``.<br>**Congratulations!** You finished the resource pack!

## Editing the data pack

It will seem like a lot work but it's mostly just copy pasting stuff from a to b.

At first, open the data pack folder and navigate to `data\anim-doors\functions`. Here you will find the `.index.md` file, it's like my own little cheat code. Inside are all files that needs to be edited.

---
`setup/on_reload.mcfunction` -> Add new scoreboard to track door. Add the following line below the first block:
```js
scoreboard objectives add _betdor_<id-1> minecraft.used:<mod-key>.<door-type>
```

---
`change/<door>.mcfunction (add)` -> Copy the `acacia.mcfunction` file, rename it to `<door-type>.mcfunction` and replace inside all `minecraft:acacia_door` text with your `<mod-key>:<door-type>` text. Most editors support something like `ctrl` + `r`.

---
`setup_type/<door>.mcfunction (add)` -> Copy the `acacia.mcfunction` file, rename it to `<door-type>.mcfunction` and edit numbers in each line after `CustomModelData` to your `<id-1>` and `<id-2>` like the following:
```js
execute if block ~ ~ ~ #anim-doors:valid_door[hinge=left] run data merge entity @s {ArmorItems:[{},{},{},{id:"minecraft:iron_nugget",Count:1b,tag:{CustomModelData:<id-1>}}]}
execute if block ~ ~ ~ #anim-doors:valid_door[hinge=right] run data merge entity @s {ArmorItems:[{},{},{},{id:"minecraft:iron_nugget",Count:1b,tag:{CustomModelData:<id-2>}}]}
```

---
`as_player.mcfunction` -> Add player tracking by tracking your scoreboard created in the first step. Add the following line below the first block:
```js
execute if score @s _betdor_<id-1> matches 1.. run function anim-doors:placed
```

---
`fake_change.mcfunction *2` -> Here we need to edit two things. In the first big block add the following line
```js
execute if block ~ ~ ~ <mod-key>:<door-type>[half=lower] run scoreboard players set type_check _betdor <id-1>
```
and in the last big block add the following line
```js
execute if score type_check _betdor matches <id-1> run function anim-doors:change/<door-type>
```

---
`placed.mcfunction` -> Simply add the following line before the last command:
```js
scoreboard players reset @s _betdor_<id-1>
```

---
`setup_door.mcfunction` -> Last thing we need to do is actually loading the door. Just add the following line to the first block:
```js
execute if block ~ ~ ~ <mod-key>:<door-type> run function anim-doors:setup_type/<door-type>
```

## Data pack functions done!

All technical stuff is done! Congrats! Now you just need to tell Minecraft whether your door is an iron door (only usable by redstone) or wooden door (usable by players & redstone)

Navigate to `data\anim-doors\tags\blocks`, open `valid-door.json` and add `<mod-key>:<door-type>` to the list.<br>
Now you either open `iron_door.json` or `wooden_door.json` and add the same there too.

## That's it!
You successfully added your custom door to the pack! You can share your work with others on our Discord, we love to see new additions ♥
</details>