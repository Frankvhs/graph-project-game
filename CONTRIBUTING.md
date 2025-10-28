# Contributing Guide

## Commit Convention

We follow a structured commit message format to maintain a clean and organized project history.

### Format
```
tipo(area): accion
```

### Commit Types
- `feat` - New functionality
- `fix` - Bug correction  
- `refactor` - Restructuring without changing behavior
- `docs` - Documentation
- `test` - Tests
- `chore` - Maintenance tasks

### Areas

#### Common Areas
- `core/` - Main game system
- `engine/` - Base engine/framework
- `player/` - Player control
- `enemy/` - Enemies and NPCs
- `ui/` - User interface
- `audio/` - Sound and music
- `physics/` - Physics system

#### Other Areas
- `architecture/` - General code structure
- `ai/` - Artificial intelligence
- `collision/` - Collision detection
- `input/` - Control handling
- `camera/` - Camera system
- `hud/` - Heads-up display
- `menu/` - Menu system
- `combat/` - Combat system
- `movement/` - Entity movement
- `quest/` - Mission system
- `dialogue/` - Dialogues and conversations
- `inventory/` - Inventory and items
- `progression/` - Game progression
- `level/` - Level design
- `world/` - Game world
- `environment/` - Environment and scenarios
- `lighting/` - Lighting system
- `particles/` - Particle effects
- `animation/` - Animations
- `vfx/` - Visual effects
- `render/` - Graphic rendering
- `performance/` - Optimizations
- `memory/` - Memory management
- `network/` - Multiplayer/networking
- `save/` - Save system
- `config/` - Configuration and settings
- `editor/` - Internal tools
- `build/` - Build system
- `ci/` - Continuous integration
- `tools/` - Development utilities

### Examples
```bash
feat(player): add double jump ability
fix(collision): resolve wall clipping issue
refactor(combat): optimize damage calculation system
docs(architecture): update entity component system diagram
test(ai): add pathfinding unit tests
chore(build): update Unity version to 2022.3
```

### Rules
- Single-line commits only
- Use English verbs in the action message
- Keep messages clear and concise
- Reference issues when applicable

Thank you for contributing!
