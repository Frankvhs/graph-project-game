SISTEMA DE GENERACIÓN DE MAZMORRAS PROGRESIVO

HABITACIONES BASE (15 tipos):
- 4x 1 puerta: [N], [S], [E], [O]
- 6x 2 puertas: [NS], [NE], [NO], [SE], [SO], [EO]
- 4x 3 puertas: [NSE], [NSO], [NEO], [SEO]
- 1x 4 puertas: [NSEO]

CONFIGURACIÓN POR NIVEL:
Nivel 1: 4-6 habitaciones, 60% 1puerta, 0% 4puertas
Nivel 2: 5-7 habitaciones, 50% 1puerta, 10% 4puertas  
Nivel 3: 6-9 habitaciones, 40% 1puerta, 15% 4puertas
Nivel 4: 8-12 habitaciones, 30% 1puerta, 20% 4puertas
Nivel 5: 10-15 habitaciones, 20% 1puerta, 25% 4puertas
Nivel 6+: 12-18+ habitaciones, 10% 1puerta, 30% 4puertas

ALGORITMO DE GENERACIÓN:

1. INICIALIZAR:
   - habitaciones = []
   - puertas_sin_conectar = []
   - config = obtener_config_nivel(nivel_actual)

2. ELEGIR HABITACIÓN INICIAL:
   - Si nivel = 1: usar [N] (fácil)
   - Si nivel > 1: aleatorio entre [1puerta, 2puertas, 3puertas]
   - agregar a habitaciones
   - agregar sus puertas a puertas_sin_conectar

3. EXPANDIR MAZMORRA:
   MIENTRAS tamaño_actual < config.max_habitaciones Y puertas_sin_conectar no vacío:
     
     a. Ordenar puertas_sin_conectar por prioridad (las más antiguas primero)
     b. Tomar primera puerta (ej: dirección Norte)
     c. Buscar habitaciones compatibles que tengan puerta Sur
     d. Filtrar por probabilidades del nivel actual
     e. Elegir aleatoriamente habitación compatible
     f. Conectar puertas (Norte-Sur)
     g. Agregar nueva habitación a la lista
     h. Agregar nuevas puertas de la habitación a puertas_sin_conectar
     i. Eliminar puerta conectada de la lista

4. COLOCAR ESCALERA:
   - Calcular habitación más alejada de la inicial
   - Colocar escalera que baja en esa habitación
   - En niveles bajos (1-3), puede estar más cerca

5. VALIDAR:
   - Todas las puertas conectadas
   - Solo una escalera por nivel
   - Laberinto completamente conexo

PRIORIDADES DE EXPANSIÓN:
- Niveles 1-3: Expansión lineal, evitar bucles
- Niveles 4+: Permitir bucles, crear caminos alternativos
- Niveles 6+: Expansión agresiva, máxima interconexión

NOTAS:
- Cada nivel es más grande y complejo que el anterior
- No hay habitaciones inaccesibles
- El jugador debe explorar para encontrar la escalera