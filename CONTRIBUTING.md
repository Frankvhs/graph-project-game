# Guía de Contribución

## Convención de Ramas

### Formato
```
tipo/área:título
```

- `tipo/`: tipo de contribución ([ver tipos disponibles](#tipos-de-contribución))
- `área:`: área de contribución ([ver áreas disponibles](#áreas-del-proyecto))  
- `título`: título breve para la rama (en formato [kebab-case](https://en.wikipedia.org/wiki/Letter_case#Kebab_case))

### Ejemplos
```bash
feat/player:doble-salto
fix/combat:logica-slime
feat/assets:animacion-reposo
```

## Convención de Commits

Seguimos un formato estructurado para los mensajes de commit para mantener un historial de proyecto limpio y organizado.

### Formato
```
tipo(área): mensaje
```

### Reglas
- Commits de una sola línea
- Usa verbos en inglés en el mensaje de acción
- Mantén los mensajes claros y concisos
- Referencia issues cuando sea aplicable
- **Opcional**: Si el commit cierra un issue, añade `Closes #número` en una línea nueva (dos saltos de línea después del mensaje principal)

### Ejemplos
```bash
feat(player): add double jump ability

Closes #42
```

```bash
fix(collision): resolve wall clipping issue

Closes #15
```

```bash
refactor(combat): optimize damage calculation system
```

## Tipos de Contribución

| Tipo | Descripción |
|------|-------------|
| `feat` | Nueva funcionalidad |
| `fix` | Corrección de errores |
| `refactor` | Reestructuración sin cambiar comportamiento |
| `docs` | Documentación |
| `test` | Pruebas |
| `chore` | Tareas de mantenimiento |

## Áreas del Proyecto

### Áreas Comunes

| Área | Descripción |
|------|-------------|
| `core` | Sistema principal del juego |
| `engine` | Motor/base del framework |
| `player` | Control del jugador |
| `enemy` | Enemigos y NPCs |
| `ui` | Interfaz de usuario |
| `physics` | Sistema de física |
| `assets` | Música, sprites y recursos relacionados |

### Otras Áreas

| Área | Descripción |
|------|-------------|
| `architecture` | Estructura general del código |
| `ai` | Inteligencia artificial |
| `collision` | Detección de colisiones |
| `input` | Manejo de controles |
| `camera` | Sistema de cámara |
| `hud` | Interfaz de cabeza de pantalla |
| `menu` | Sistema de menús |
| `combat` | Sistema de combate |
| `movement` | Movimiento de entidades |
| `quest` | Sistema de misiones |
| `dialogue` | Diálogos y conversaciones |
| `inventory` | Inventario y objetos |
| `progression` | Progresión del juego |
| `level` | Diseño de niveles |
| `world` | Mundo del juego |
| `environment` | Ambiente y escenarios |
| `lighting` | Sistema de iluminación |
| `particles` | Efectos de partículas |
| `animation` | Animaciones |
| `vfx` | Efectos visuales |
| `render` | Renderizado gráfico |
| `performance` | Optimizaciones |
| `memory` | Gestión de memoria |
| `network` | Multijugador/redes |
| `save` | Sistema de guardado |
| `config` | Configuración y ajustes |
| `editor` | Herramientas internas |
| `build` | Sistema de build |
| `ci` | Integración continua |
| `tools` | Utilidades de desarrollo |

---

¡Gracias por contribuir!