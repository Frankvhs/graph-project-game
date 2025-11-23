Room 1 to Room 16: Basic rooms for connection
Room 17 to Room 76: Repetition of each basic room with one staircase down or staircases up in a corner

Clasificación de Habitaciones Base

Tienes 16 tipos base de habitaciones (excluyendo la totalmente cerrada):

    1 habitación sin puertas:  base (no se usa para conexiones).
    4 habitaciones con 1 puerta: Una en cada dirección (N, S, E, O).
    6 habitaciones con 2 puertas: Combinaciones de direcciones (NS, NE, NO, SE, SO, EO).
    4 habitaciones con 3 puertas: Combinaciones (NSE, NSO, NEO, SEO).
    1 habitación con 4 puertas: NSEO.
    
Cada una tiene 4 variantes de escalera (en esquinas: NW, NE, SW, SE), totalizando 64 habitaciones con escaleras. Las escaleras suben o bajan, y deben emparejarse.

Estructura de Conexiones Principales
1. Reglas Básicas de Puertas

Cada puerta en una habitación debe conectarse a exactly una puerta en otra habitación.
Las conexiones son bidireccionales: Si la habitación A tiene puerta al Norte, la habitación B debe tener puerta al Sur.
Evitar "puertas colgantes": Toda puerta generada debe tener su pareja.

2. Generación de la Mazmorra

Comienza con una habitación inicial:
Usa una habitación de 1 puerta (ej: entrada al Norte).
Esta puerta definirá la dirección de expansión.

Expansión procedural:

Para cada puerta sin conectar, genera una habitación compatible en esa dirección.
Ejemplo: Si una habitación tiene puerta al Este, la siguiente debe tener puerta al Oeste.
Elige aleatoriamente entre las habitaciones que cumplan el patrón de puertas requerido.

Manejo de habitaciones especiales:

La habitación sin puertas (osea de solo una coexion que se utiliza) se coloca al final de una rama (ej: tras una habitación de 1 puerta).
Las habitaciones de 4 puertas sirven como nodos centrales o cruces.

Lógica de Escaleras
1. Tipos de Escaleras

Escalera que sube (+1): Conduce a un nivel superior.
Escalera que baja (-1): Conduce a un nivel inferior.
Emparejamiento: Cada escalera que sube debe conectarse con una que baje en otro lugar del mapa.

2. Colocación de Escaleras

Cada habitación base (16 tipos) puede tener 0 o 1 escalera.
Las escaleras se ubican en esquinas (NW, NE, SW, SE), sin afectar las puertas.

Regla crítica:

Si una habitación tiene escalera que sube, debe existir otra habitación con escalera que baje en el mismo nivel o en otro.
Ambos extremos deben estar conectados por el graph de la mazmorra.

3. Generación con Escaleras

Asignar escaleras durante la expansión:
Al generar una habitación, decide aleatoriamente si incluye escalera (ej: 20% de probabilidad).
Si se añade, elige una esquina libre y asígnale dirección sube/baja de forma balanceada.

Emparejar escaleras:

Tras generar el mapa, verifica que por cada escalera que sube haya una que baje.
Si hay desbalance, ajusta convirtiendo escaleras o añadiendo habitaciones extras.