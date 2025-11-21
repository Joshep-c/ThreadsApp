package com.app.threadsapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TaskViewModel : ViewModel() {

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks

    private val _statusMessage = MutableStateFlow("Listo para agregar tareas")
    val statusMessage: StateFlow<String> = _statusMessage

    private var nextId = 1

    /**
     * 1. Agregar tarea (Main Thread)
     * El usuario ingresa los datos y se agregan inmediatamente
     */
    fun addTask(title: String, description: String, priority: Int) {
        viewModelScope.launch(Dispatchers.Main) {
            val newTask = Task(nextId++, title, description, priority)
            _tasks.value = _tasks.value + newTask
            _statusMessage.value = "Tarea '${title}' agregada en Main Thread"
        }
    }

    /**
     * 2. Procesar una tarea (Worker Thread - Dispatchers.Default)
     * Simula procesamiento CPU-intensivo en segundo plano
     */
    fun processTask(task: Task) {
        viewModelScope.launch {
            _statusMessage.value = "Procesando '${task.title}' en Worker Thread..."

            val result = withContext(Dispatchers.Default) {
                // Simula trabajo pesado de CPU
                delay(2000)
                "Procesamiento completado"
            }

            _statusMessage.value = "${task.title}: $result (Prioridad ${getPriorityName(task.priority)})"
        }
    }

    /**
     * 3. Ordenar tareas por prioridad (Worker Thread - Dispatchers.Default)
     * Algoritmo de ordenamiento que se ejecuta en un hilo de trabajo
     */
    fun sortTasksByPriority() {
        viewModelScope.launch {
            _statusMessage.value = "Ordenando tareas en Worker Thread (Dispatchers.Default)..."

            val sortedTasks = withContext(Dispatchers.Default) {
                // Simula un proceso de ordenamiento más complejo
                delay(1000)

                // Algoritmo de ordenamiento: primero por prioridad (descendente), luego por id
                _tasks.value.sortedWith(
                    compareByDescending<Task> { it.priority }
                        .thenBy { it.id }
                )
            }

            _tasks.value = sortedTasks
            _statusMessage.value = "Tareas ordenadas por prioridad (Alta→Baja) en Worker Thread"
        }
    }

    /**
     * 4. Procesar todas las tareas (Worker Thread - Dispatchers.IO)
     * Simula procesamiento de múltiples tareas en paralelo
     */
    fun processAllTasks() {
        viewModelScope.launch {
            if (_tasks.value.isEmpty()) {
                _statusMessage.value = "No hay tareas para procesar"
                return@launch
            }

            _statusMessage.value = "Procesando todas las tareas en Worker Thread (IO)..."

            withContext(Dispatchers.IO) {
                delay(2000)
            }

            _statusMessage.value = "${_tasks.value.size} tareas procesadas en Worker Thread (IO)"
        }
    }

    /**
     * 5. Limpiar todas las tareas (Main Thread)
     */
    fun clearAllTasks() {
        viewModelScope.launch(Dispatchers.Main) {
            _tasks.value = emptyList()
            _statusMessage.value = "Todas las tareas eliminadas en Main Thread"
        }
    }

    /**
     * 6. Cargar tareas de ejemplo (Worker Thread - Dispatchers.IO)
     * Simula carga desde base de datos o red
     */
    fun loadSampleTasks() {
        viewModelScope.launch {
            _statusMessage.value = "Cargando tareas de ejemplo desde Worker Thread (IO)..."

            val loaded = withContext(Dispatchers.IO) {
                delay(1500) // Simula acceso a BD/red
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

    // Función auxiliar para obtener el nombre de la prioridad
    private fun getPriorityName(priority: Int): String {
        return when (priority) {
            1 -> "Baja"
            2 -> "Media"
            3 -> "Alta"
            else -> "Desconocida"
        }
    }
}


