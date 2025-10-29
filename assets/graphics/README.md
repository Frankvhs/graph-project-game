# Graphics Resources

All visual assets for the game.

## Subdirectories:
- [**sprites/**](sprites/README.md) - Character animations
- [**ui/**](ui/README.md) - Interface elements
- [**tilesets/**](tilesets/README.md) - Level tiles
- [**particles/**](particles/README.md)
- [**fonts/**](fonts/README.md)


## **Format Standards

### **1. Technical Specifications**

| Property | Specification | Notes |
| :--- | :--- | :--- |
| **File Format** | PNG (Recommended) | Supports transparency and lossless quality. |
| **Color Mode** | RGBA (32-bit) | Essential for smooth transparency (alpha channel). |
| **Canvas Size** | Power of Two (e.g., 16x16, 32x32, 64x64) | **Mandatory for tilesets.** Improves rendering performance. |
| **Background** | Transparent | All non-object pixels must be transparent. |

### **2. Naming Conventions**

*   **Use Lowercase:** `player_idle.png` instead of `PlayerIdle.PNG`.
*   **Use Underscores:** `run_01.png` instead of `run 01.png` or `run01.png`.
*   **Sequence Numbering:** Always use leading zeros for animation frames (e.g., `01`, `02`, ... `10`). This ensures proper file sorting.
