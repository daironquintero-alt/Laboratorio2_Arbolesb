# Laboratorio de árboles B+

Implementación en Java de un árbol B+ de nombres, en memoria, con menú de consola.
Al ejecutar inicia vacío y solicita un orden entre 4 y 9. No usa bases de datos ni
precarga nombres. Los datos de prueba están exclusivamente en `src/test`.

## Ejecutar en Windows

Requiere un **JDK 17 o superior**, disponible en PATH o configurado en JAVA_HOME.
Desde PowerShell, dentro de esta carpeta:

```powershell
.\ejecutar.ps1
```

También se puede abrir como proyecto Java/Maven en VS Code o IntelliJ y ejecutar
`com.udea.co.demo.Main`. No es necesario instalar dependencias para los scripts.
Si la política local impide ejecutar scripts, se puede compilar manualmente:

```powershell
New-Item -ItemType Directory -Force build/classes
$sources = Get-ChildItem src/main/java -Recurse -Filter *.java | ForEach-Object FullName
javac -encoding UTF-8 --release 17 -d build/classes $sources
java '-Dfile.encoding=UTF-8' -cp build/classes com.udea.co.demo.Main
```

El menú permite visualizar, agregar, buscar, eliminar y salir. Para conservar
tildes en la terminal, use una consola configurada para UTF-8.

## Organización y relación con el ejemplo del profesor

- `Main`: Scanner, validación y menú con switch, siguiendo el ejemplo.
- `Node`: listas de claves e hijos y atributo isLeaf. Se agrega next para enlazar hojas.
- `BPlusTree`: lógica del árbol, con root y order privados, inserción recursiva,
  búsqueda, eliminación, divisiones e impresión por niveles.
- `InsertResult`, `InsertState`, `SplitResult`, `NodeLevel`: conservan las
  responsabilidades de las clases auxiliares del ejemplo.

Las claves pasan de Integer a String. Se mantiene la estructura de paquetes
`com.udea.co.demo`. Se incluye pom.xml para importar el proyecto, pero no Spring:
el menú y la estructura de datos sólo necesitan la biblioteca estándar de Java.

## Convenciones de nombres

Se eliminan espacios al principio y al final; no se aceptan nombres vacíos.
Los espacios internos se conservan y se permiten nombres compuestos y tildes.
La comparación usa String.compareTo: distingue mayúsculas, minúsculas y tildes,
y sigue el orden lexicográfico de Java, sin aplicar colación del español.
Por ejemplo, `Ana` y `ana` son claves diferentes. Un nombre exactamente repetido
se rechaza. Esta decisión se aplica por igual a insertar, buscar y eliminar.

## Propiedades y decisiones del algoritmo

Para orden m:

| Propiedad | Valor |
| --- | --- |
| Máximo de hijos internos | m |
| Máximo de claves por nodo | m - 1 |
| Mínimo de hijos internos no raíz | ceil(m / 2) |
| Mínimo de claves internas no raíz | ceil(m / 2) - 1 |
| Mínimo de entradas de hoja no raíz | ceil((m - 1) / 2) |

Una raíz hoja admite entre 1 y m - 1 nombres. Una raíz interna tiene al menos
dos hijos. El árbol sin registros se representa con root = null.
Todas las hojas quedan a la misma profundidad. Cada separador interno es el mínimo
del subárbol inmediatamente a su derecha. La igualdad baja al hijo derecho;
una búsqueda sólo confirma la existencia al llegar a una hoja.

En un split de hoja, se divide después de la mediana inferior y se **copia** al
padre el primer nombre de la hoja derecha. Ese nombre permanece en la hoja.
Se reutiliza la hoja izquierda para conservar el enlace de la hoja anterior.
En un split interno, se **promueve** la mediana inferior y se reparten los hijos.
La clave promovida sale de ese nodo interno, pero sigue existiendo en las hojas.

### Eliminación y trazas

El enunciado enumera Caso 1, Caso 2a, Caso 2b y Caso 3, sin asignar explícitamente
las letras a la dirección del préstamo. Este proyecto usa la siguiente convención:

- **Caso 1:** eliminación sin underflow, incluida la raíz hoja con mínimo especial.
- **Caso 2a:** préstamo desde el hermano izquierdo del mismo padre.
- **Caso 2b:** préstamo desde el hermano derecho del mismo padre.
- **Caso 3:** fusión porque ningún hermano adyacente puede prestar. Puede propagarse
  hacia los ancestros y reducir la altura de la raíz.

Se intenta primero préstamo izquierdo, después derecho y finalmente fusión.
En hojas, sólo se mueven entradas reales y se reparan los enlaces next.
En internos, se transfieren referencias a hijos y se calculan los separadores
desde sus mínimos. Esto equivale a bajar el separador del padre y subir el del
hermano. El método updateSeparators actualiza los nodos afectados al regresar
por la ruta recursiva, también cuando se elimina un mínimo sin underflow.

Una eliminación puede mostrar varios casos si provoca reparaciones en distintos
niveles. Las operaciones fallidas por duplicados o nombres ausentes no cambian el árbol.

La impresión incluye niveles, jerarquía indentada con índices de hijos y cadena
de hojas. Los índices empiezan en 0, como las listas de Java.

## Pruebas

```powershell
.\probar.ps1
```

Las pruebas no necesitan JUnit. Comparan los resultados con TreeSet y verifican
después de cada modificación: contenido completo, ocupación mínima y máxima,
orden estricto, cantidad de hijos, separadores, profundidad y enlaces entre hojas.
Incluyen 115.200 operaciones aleatorias con semillas reproducibles, vaciado
posterior, inserciones ordenadas y eliminaciones en ambos sentidos para todos los
órdenes. También comprueban nombres Unicode, duplicados y entradas inválidas.

### Escenarios manuales (cada fila comienza con un árbol nuevo de orden 4)

Ingrese cada nombre individualmente por la opción 2 y elimine por la opción 4.
Las secuencias de esta tabla son para pruebas manuales; Main no las carga.

| Insertar en este orden | Eliminar | Qué observar |
| --- | --- | --- |
| Ana, Beto, Cami | Beto | Caso 1 |
| Ana, Beto, Cami, Dani, Eva | Ana | Caso 2b, préstamo derecho |
| Ana, Cami, Dani, Eva, Beto | Dani | Caso 2a, préstamo izquierdo |
| Ana, Beto, Cami, Dani | Ana | Caso 3 y reducción de raíz |
| Ana, Beto, Cami, Dani, Eva, Fabi, Gina, Hugo, Iris, Juan | — | Splits de hojas y raíz interna |

Pruebe además buscar un nombre ausente, insertar dos veces el mismo nombre,
eliminar todos los nombres y visualizar el árbol vacío. En el menú pruebe texto
donde se espera un número, órdenes 3 y 10, opciones inexistentes y nombres vacíos.

## Sustentación

Recorra primero Main y Node, luego search y findChild. Continúe con insertRecursive
y las dos ramas de splitNode. Finalmente explique deleteRecursive, minKeys,
repairUnderflow, mergeNodes y updateSeparators. La parte central es justificar
por qué los datos sólo viven en hojas y cómo se conserva el mínimo derecho.

## Abrir en VS Code y trabajar en equipo

En VS Code seleccione **Archivo > Abrir carpeta** y elija esta carpeta, la que
contiene `pom.xml`. Con Extension Pack for Java instalado, VS Code reconoce el
proyecto Maven y permite ejecutar `Main.java` desde el editor. También puede usar
los scripts desde la terminal integrada.

```text
src/
  main/java/com/udea/co/demo/   Código del programa
  test/java/com/udea/co/demo/   Pruebas del árbol
pom.xml                       Configuración del proyecto Java
ejecutar.ps1                  Compilar y ejecutar el menú
probar.ps1                    Compilar y ejecutar las pruebas
README.md                     Instrucciones y explicación
.gitignore                    Exclusiones para el repositorio
```

El proyecto queda como código fuente, sin archivo ZIP. Puede subir esta carpeta
al repositorio y compartirlo con sus compañeros. Cada integrante necesitará un
JDK 17 o superior. Las carpetas generadas al compilar (`build` y `target`) quedan
excluidas por `.gitignore`, al igual que archivos locales del editor.
