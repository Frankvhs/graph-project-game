# Sprites

Sprites are the 2D images and animations used for characters, objects, and other visual elements in a game.

## Directory Structure

The project organizes sprites into two main categories:

- **characters/**: Contains all playable and non-playable characters.
- **objects/**: Contains interactive items, collectibles, and weapons.

## Example File Structure
```
.
├── characters/
│   ├── player/
│   │   ├── idle_01.png
│   │   ├── run_01.png
│   │   ├── jump_01.png
│   │   ├── jump_02.png
│   │   └── jump_03.png
│   └── enemies/
│       ├── slime/          # (Folder for slime enemy sprites)
│       ├── ghost/          # (Folder for ghost enemy sprites)
│       └── soldier/
│           ├── idle_01.png
│           ├── idle_02.png
│           ├── idle_03.png
│           └── run_01.png
└── objects/
    ├── weapons/            # (Folder for weapon sprites)
    └── items/
        ├── coin.png
        └── potion.png
```