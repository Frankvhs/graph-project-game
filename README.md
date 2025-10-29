# 🏰 Tree Dungeons

![Java](https://img.shields.io/badge/Java-17%2B-orange)
![LibGDX](https://img.shields.io/badge/LibGDX-1.12.0-red)
![Gradle](https://img.shields.io/badge/Gradle-Wrapper-green)
![License](https://img.shields.io/badge/License-MIT-blue)

Un juego de estrategia por turnos donde exploras mazmorras generadas progresivamente, combates enemigos inteligentes y progresas a través de un sistema de árbol de nodos.

## 🎮 Características

> Para más información consultar la [wiki](https://github.com/Frankvhs/graph-project-game/wiki/Idea-del-juego)

### ⚔️ Sistema de Juego
- **Combate por turnos** táctico
- **Generación progresiva** de mazmorras
- **Pathfinding inteligente** (algoritmo A*)
- **Sistema de economía** con comerciante
- **Progresión por árbol** de nodos

### 🎯 Mecánicas Principales
- Movimiento táctico por casillas
- Detección y persecución de enemigos
- Trampas y cofres ocultos
- Diferentes velocidades de unidades
- Recompensas escalables por nivel

## 🛠️ Get started

### Prerrequisitos
- **Java JDK 17** o superior ([Descargar JDK](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html))
- **Git** ([Descargar Git](https://git-scm.com/))

### 📋 Pasos de Instalación

1. **Clonar el repositorio**
   ```bash
   git clone https://github.com/tu-usuario/tree-dungeons.git
   cd tree-dungeons
   ```

> [!note]
> Para usuarios de Windows (CMD o Powershell) utilizar `.\gradlew.bat` en vez de `./gradlew`

2. **Ejecutar el Wrapper de Gradle**
   ```bash
   ./gradlew build
   ```

3. **Correr proyecto**
   ```bash
   ./gradlew run
   ```

## 🏗️ Arquitectura del Proyecto

### Gradle
Este proyecto utiliza [**Gradle**](https://gradle.org/) como sistema de construcción y gestión de dependencias. Gradle automatiza el proceso de compilación, testing y empaquetado. El proyecto incluye el **Gradle Wrapper**, lo que significa que no necesitas tener Gradle instalado globalmente.

### LibGDX
El juego está desarrollado con [**LibGDX**](https://libgdx.com/), un framework de código abierto para desarrollo de juegos en Java. LibGDX proporciona:

- **Renderizado 2D/3D** multiplataforma
- **Gestión de assets** y texturas
- **Sistema de audio**
- **Input handling** unificado
- **Herramientas de UI**

### Estructura de Módulos
- `core`: Módulo principal con la lógica de aplicación compartida por todas las plataformas
- `lwjgl3`: Plataforma desktop principal usando LWJGL3

## 🎮 Comandos de Gradle Útiles

### Desarrollo
```bash
# Ejecutar la aplicación
./gradlew desktop:run

# Construir JAR ejecutable
./gradlew desktop:jar

# Limpiar builds
./gradlew clean
```

### Testing
```bash
# Ejecutar tests unitarios
./gradlew test

# Ejecutar tests con reportes
./gradlew test --continue
```

### IDE
```bash
# Generar proyecto para Eclipse
./gradlew eclipse

# Generar proyecto para IntelliJ IDEA
./gradlew idea

# Limpiar configuración de IDE
./gradlew cleanEclipse
./gradlew cleanIdea
```

### Banderas Útiles
- `--continue`: Los errores no detienen la ejecución de tareas
- `--offline`: Usa dependencias en caché
- `--refresh-dependencies`: Fuerza la actualización de dependencias
- `--daemon`: Usa el daemon de Gradle para mejor rendimiento

## 🤝 Contribuir

¡Las contribuciones son bienvenidas! Por favor, lee nuestras [Guías de Contribución](CONTRIBUTING.md) antes de enviar un pull request.

---

> [!warning] 
> Asegúrate de tener Java JDK 17 o superior instalado y configurado correctamente en tu variable de entorno `JAVA_HOME` antes de ejecutar cualquier comando de Gradle.
