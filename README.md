# ThreadsApp - Gestor de Tareas con Threads y Corrutinas

## 📋 Descripción

**ThreadsApp** es una aplicación Android desarrollada en Kotlin que demuestra de manera práctica y visual el uso de **Threads** (hilos) y **Corrutinas** para la gestión de operaciones asíncronas. La aplicación implementa un gestor de tareas interactivo donde el usuario puede crear, ordenar y procesar tareas con diferentes niveles de prioridad, mientras observa en tiempo real cómo se distribuyen las operaciones entre el hilo principal (Main Thread) y los hilos de trabajo (Worker Threads).

## 🎯 Objetivo del Proyecto

Demostrar los conceptos fundamentales de programación concurrente en Android:
- Diferencia entre **Main Thread** (UI Thread) y **Worker Threads**
- Uso correcto de **Corrutinas** para operaciones asíncronas
- Implementación de diferentes **Dispatchers** según el tipo de operación
- Gestión de estado reactivo con **StateFlow**
- Prevención de bloqueos en la interfaz de usuario

---

## 🧵 Uso de Threads y Corrutinas

### 1. **Main Thread (Hilo Principal)**

El **Main Thread** es responsable de todas las interacciones con la interfaz de usuario. Es crucial no bloquearlo con operaciones pesadas para mantener la aplicación fluida.

#### Operaciones en Main Thread:

**✅ Agregar Tarea**
```kotlin
fun addTask(title: String, description: String, priority: Int) {
    viewModelScope.launch(Dispatchers.Main) {
        val newTask = Task(nextId++, title, description, priority)
        _tasks.value = _tasks.value + newTask
        _statusMessage.value = "Tarea '${title}' agregada en Main Thread"
    }
}
```
- **Dispatcher**: `Dispatchers.Main`
- **Tiempo de ejecución**: Instantáneo
- **Propósito**: Actualización inmediata de la UI sin operaciones pesadas

**✅ Limpiar Tareas**
```kotlin
fun clearAllTasks() {
    viewModelScope.launch(Dispatchers.Main) {
        _tasks.value = emptyList()
        _statusMessage.value = "Todas las tareas eliminadas en Main Thread"
    }
}
```
- **Dispatcher**: `Dispatchers.Main`
- **Tiempo de ejecución**: Instantáneo
- **Propósito**: Limpieza rápida de datos en memoria

---

### 2. **Worker Threads - Dispatchers.Default**

**Dispatchers.Default** está optimizado para operaciones **CPU-intensivas** como cálculos, algoritmos y procesamiento de datos.

#### Operaciones CPU-intensivas:

**⚙️ Procesar Tarea Individual**
```kotlin
fun processTask(task: Task) {
    viewModelScope.launch {
        _statusMessage.value = "Procesando '${task.title}' en Worker Thread..."

        val result = withContext(Dispatchers.Default) {
            // Simula trabajo pesado de CPU (2 segundos)
            delay(2000)
            "Procesamiento completado"
        }

        _statusMessage.value = "${task.title}: $result (Prioridad ${getPriorityName(task.priority)})"
    }
}
```
- **Dispatcher**: `Dispatchers.Default`
- **Tiempo de ejecución**: ~2 segundos
- **Propósito**: Simula procesamiento intensivo sin bloquear la UI
- **Uso real**: Análisis de datos, cálculos matemáticos, procesamiento de imágenes

**🔄 Ordenar Tareas por Prioridad (Algoritmo de Ordenamiento)**
```kotlin
fun sortTasksByPriority() {
    viewModelScope.launch {
        _statusMessage.value = "Ordenando tareas en Worker Thread (Dispatchers.Default)..."

        val sortedTasks = withContext(Dispatchers.Default) {
            // Simula proceso de ordenamiento complejo (1 segundo)
            delay(1000)
            
            // Algoritmo: Ordenamiento por prioridad descendente y luego por ID
            _tasks.value.sortedWith(
                compareByDescending<Task> { it.priority }
                    .thenBy { it.id }
            )
        }

        _tasks.value = sortedTasks
        _statusMessage.value = "Tareas ordenadas por prioridad (Alta→Baja) en Worker Thread"
    }
}
```
- **Dispatcher**: `Dispatchers.Default`
- **Tiempo de ejecución**: ~1 segundo
- **Algoritmo**: `sortedWith()` con comparadores múltiples
- **Complejidad**: O(n log n) - Utiliza Merge Sort / Tim Sort
- **Propósito**: Demostrar algoritmos ejecutados en background
- **Uso real**: Ordenamiento de grandes conjuntos de datos, búsquedas, filtros complejos

---

### 3. **Worker Threads - Dispatchers.IO**

**Dispatchers.IO** está optimizado para operaciones de **entrada/salida** como acceso a bases de datos, red, archivos o cualquier operación que involucre espera.

#### Operaciones de I/O:

**📥 Cargar Tareas de Ejemplo**
```kotlin
fun loadSampleTasks() {
    viewModelScope.launch {
        _statusMessage.value = "Cargando tareas de ejemplo desde Worker Thread (IO)..."

        val loaded = withContext(Dispatchers.IO) {
            delay(1500) // Simula acceso a base de datos o red
            listOf(
                Task(nextId++, "Estudiar Kotlin", "Repasar corrutinas y threads", 3),
                Task(nextId++, "Hacer ejercicio", "Rutina de 30 minutos", 2),
                Task(nextId++, "Leer libro", "Capítulo 5", 1),
                Task(nextId++, "Proyecto final", "Completar la aplicación", 3)
            )
        }

        _tasks.value = _tasks.value + loaded
        _statusMessage.value = "${loaded.size} tareas cargadas desde Worker Thread (IO)"
    }
}
```
- **Dispatcher**: `Dispatchers.IO`
- **Tiempo de ejecución**: ~1.5 segundos
- **Propósito**: Simula carga de datos desde fuentes externas
- **Uso real**: Consultas a base de datos, llamadas API REST, lectura/escritura de archivos

**⚙️ Procesar Todas las Tareas**
```kotlin
fun processAllTasks() {
    viewModelScope.launch {
        if (_tasks.value.isEmpty()) {
            _statusMessage.value = "No hay tareas para procesar"
            return@launch
        }

        _statusMessage.value = "Procesando todas las tareas en Worker Thread (IO)..."

        withContext(Dispatchers.IO) {
            delay(2000) // Simula operación de I/O
        }

        _statusMessage.value = "${_tasks.value.size} tareas procesadas en Worker Thread (IO)"
    }
}
```
- **Dispatcher**: `Dispatchers.IO`
- **Tiempo de ejecución**: ~2 segundos
- **Propósito**: Simula procesamiento batch en background
- **Uso real**: Sincronización de datos, operaciones por lotes, exportación de datos

---

## 📊 Comparación de Dispatchers

| Dispatcher | Uso Principal | Pool de Threads | Ejemplo de Uso |
|------------|---------------|-----------------|----------------|
| **Dispatchers.Main** | Operaciones de UI | 1 (Main Thread) | Actualizar vistas, eventos de usuario |
| **Dispatchers.Default** | CPU-intensivo | Núcleos de CPU | Algoritmos, cálculos, procesamiento |
| **Dispatchers.IO** | Entrada/Salida | 64+ threads | BD, red, archivos, APIs |

---

## 🏗️ Arquitectura del Proyecto

### Componentes Principales

```
app/src/main/java/com/app/threadsapp/
├── Task.kt              # Modelo de datos
├── TaskViewModel.kt     # Lógica de negocio y gestión de threads
└── MainActivity.kt      # Interfaz de usuario (Jetpack Compose)
```

### Modelo de Datos

```kotlin
data class Task(
    val id: Int,
    val title: String,
    val description: String,
    val priority: Int  // 1 = Baja, 2 = Media, 3 = Alta
)
```

### Gestión de Estado

El proyecto utiliza **StateFlow** para la gestión reactiva del estado:

```kotlin
private val _tasks = MutableStateFlow<List<Task>>(emptyList())
val tasks: StateFlow<List<Task>> = _tasks

private val _statusMessage = MutableStateFlow("Listo para agregar tareas")
val statusMessage: StateFlow<String> = _statusMessage
```

**Ventajas de StateFlow:**
- ✅ Reactivo: La UI se actualiza automáticamente
- ✅ Thread-safe: Seguro para concurrencia
- ✅ Lifecycle-aware: Se maneja automáticamente con el ciclo de vida

---

## 🎨 Características de la Interfaz

### Sistema de Prioridades

Cada tarea tiene un nivel de prioridad visual:

| Prioridad | Emoji | Color de Fondo | Valor |
|-----------|-------|----------------|-------|
| **Alta** | 🔴 | Rojo claro (#FFEBEE) | 3 |
| **Media** | 🟡 | Naranja claro (#FFF3E0) | 2 |
| **Baja** | 🟢 | Verde claro (#E8F5E9) | 1 |

### Indicador de Estado en Tiempo Real

El indicador muestra:
- ✅ Qué operación se está ejecutando
- ✅ En qué thread se ejecuta (Main Thread / Worker Thread)
- ✅ El tipo de Dispatcher utilizado (Main / Default / IO)
- ✅ El resultado de cada operación

---

## 🔄 Flujo de Operaciones

### Ejemplo: Agregar y Ordenar Tareas

```
1. Usuario ingresa: "Estudiar Kotlin" (Prioridad: Alta)
   ↓
2. [Main Thread] Se crea la tarea inmediatamente
   ↓
3. Usuario presiona "Ordenar"
   ↓
4. [Worker Thread - Default] Se ejecuta el algoritmo de ordenamiento
   ↓ (1 segundo)
5. [Main Thread] Se actualiza la UI con la lista ordenada
```

### Ejemplo: Cargar Ejemplos

```
1. Usuario presiona "📥 Ejemplos"
   ↓
2. [Worker Thread - IO] Inicia carga simulada de BD
   ↓ (1.5 segundos)
3. [Worker Thread - IO] Se recuperan 4 tareas
   ↓
4. [Main Thread] Se actualiza la UI con las nuevas tareas
```

---

## 🛠️ Tecnologías Utilizadas

- **Lenguaje**: Kotlin 2.0+
- **UI Framework**: Jetpack Compose
- **Arquitectura**: MVVM (Model-View-ViewModel)
- **Concurrencia**: Kotlin Coroutines
- **Gestión de Estado**: StateFlow
- **Ciclo de Vida**: viewModelScope
- **Material Design**: Material 3

---

## 📱 Requisitos del Sistema

- **Android Studio**: Arctic Fox o superior
- **Kotlin**: 2.0+
- **minSdk**: 24 (Android 7.0 Nougat)
- **targetSdk**: 36
- **Gradle**: 8.0+

---

## 🚀 Cómo Usar la Aplicación

### 1. Agregar una Tarea
- Escriba el **título** de la tarea (obligatorio)
- Agregue una **descripción** (opcional)
- Seleccione la **prioridad**: Baja 🟢 / Media 🟡 / Alta 🔴
- Presione **"➕ Agregar Tarea"**
- La tarea se agrega **instantáneamente en Main Thread**

### 2. Ordenar Tareas
- Presione **"🔄 Ordenar"**
- El algoritmo se ejecuta en **Worker Thread (Default)**
- Las tareas se ordenan de **Alta → Media → Baja** prioridad
- Tiempo: ~1 segundo

### 3. Procesar Tareas
- **Individual**: Presione "Procesar" en cada tarea
- **Todas**: Presione **"⚙️ Procesar"**
- Se ejecuta en **Worker Thread (Default o IO)**
- Tiempo: ~2 segundos

### 4. Cargar Ejemplos
- Presione **"📥 Ejemplos"**
- Se cargan 4 tareas de ejemplo
- Simula carga desde **base de datos** en **Worker Thread (IO)**
- Tiempo: ~1.5 segundos

### 5. Limpiar Todo
- Presione **"🗑️ Limpiar"**
- Elimina todas las tareas
- Operación en **Main Thread** (instantánea)

---

## 📚 Conceptos Clave Demostrados

### 1. **Prevención de ANR (Application Not Responding)**
- Todas las operaciones pesadas se ejecutan en Worker Threads
- El Main Thread solo maneja actualizaciones de UI
- La aplicación permanece responsiva en todo momento

### 2. **withContext() - Cambio de Contexto**
```kotlin
viewModelScope.launch {  // Inicia en Main Thread
    val result = withContext(Dispatchers.Default) {
        // Cambia a Worker Thread
        delay(2000)
        "Resultado"
    }
    // Regresa automáticamente a Main Thread
    _statusMessage.value = result
}
```

### 3. **viewModelScope - Gestión Automática**
- Se cancela automáticamente cuando el ViewModel se destruye
- Previene memory leaks
- Maneja el ciclo de vida de la actividad

### 4. **Algoritmo de Ordenamiento Concurrente**
```kotlin
_tasks.value.sortedWith(
    compareByDescending<Task> { it.priority }  // Primero por prioridad (3→1)
        .thenBy { it.id }                       // Luego por orden de creación
)
```
- **Complejidad temporal**: O(n log n)
- **Algoritmo**: Tim Sort (híbrido Merge Sort + Insertion Sort)
- **Estabilidad**: Ordenamiento estable
- **Ejecución**: Worker Thread para no bloquear UI

---

## 📖 Temas Relevantes

### Concurrencia en Android
- **Threads vs Coroutines**: Las corrutinas son más ligeras y fáciles de gestionar
- **Structured Concurrency**: viewModelScope garantiza limpieza automática
- **Cancelación Cooperativa**: Las corrutinas respetan el ciclo de vida

### Dispatchers y Pool de Threads
- **Main**: 1 thread dedicado a UI
- **Default**: Pool de threads = número de núcleos CPU
- **IO**: Pool expandible hasta 64+ threads para operaciones bloqueantes

### Buenas Prácticas
- ✅ Nunca bloquear el Main Thread
- ✅ Usar el Dispatcher apropiado según la tarea
- ✅ Evitar GlobalScope (usar viewModelScope)
- ✅ Manejar errores con try-catch en corrutinas
- ✅ Preferir StateFlow sobre LiveData para Compose

### Casos de Uso Reales
- **Dispatchers.Main**: Clicks, animaciones, actualización de vistas
- **Dispatchers.Default**: Parsing JSON, encriptación, compresión, algoritmos
- **Dispatchers.IO**: Room DB, Retrofit, File I/O, SharedPreferences

---

## 👥 Autores

Desarrollado por estudiantes del curso de Desarrollo de Aplicaciones Móviles:

- **Taylor Betanzos**
- **Joshep Ccahuana**
- **Jorge Condorios**
- **Geraldine Umasi**
- **Carlo Valdivia**

---

## 📝 Notas Finales

Este proyecto es una herramienta educativa diseñada para comprender de manera práctica y visual cómo funcionan los threads y las corrutinas en Android. Cada operación está claramente identificada con el thread que la ejecuta, permitiendo a los estudiantes observar en tiempo real la diferencia entre operaciones síncronas y asíncronas.

**Fecha de Desarrollo**: Noviembre 2025  
**Versión**: 1.0

