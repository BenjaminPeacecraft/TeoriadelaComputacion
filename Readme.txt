# Convertidor de AFD a Expresiones Regulares

Este proyecto es un software interactivo desarrollado en Java que permite la creación, edición y conversión de Autómatas Finitos Deterministas (AFD) a Expresiones Regulares (ER). La herramienta implementa el **algoritmo de eliminación de estados**, permitiendo al usuario visualizar el proceso de transformación paso a paso a través de una interfaz gráfica intuitiva.

## ✨ Características Principales

El software cumple con los siguientes requerimientos funcionales:

* **Gestión de Autómatas (GUI):** Creación y edición de AFD directamente mediante una interfaz gráfica interactiva.
* **Compatibilidad con JFLAP:** Importación nativa de archivos `.jff` creados en JFLAP. El sistema lee, interpreta y visualiza correctamente el autómata importado.
* **Persistencia de Datos:** Carga y guardado de AFD en un formato de archivo propio del proyecto.
* **Validación Rigurosa:** Verificación automática para asegurar que el autómata cargado, importado o creado sea un AFD válido antes de proceder.
* **Conversión Paso a Paso:** Ejecución interactiva del algoritmo de eliminación de estados. El usuario puede avanzar paso a paso observando cómo se eliminan los nodos y se actualizan las transiciones.
* **Resultado Claro:** Visualización final clara y legible de la Expresión Regular (ER) equivalente.

## 📋 Requisitos Previos

Para compilar y ejecutar este proyecto, necesitas tener instalado:

* **Java Development Kit (JDK):** Versión 8 o superior.
* **Entorno Gráfico:** Un sistema operativo capaz de renderizar interfaces gráficas en Java (Linux, Windows o macOS).

## 🚀 Cómo compilar y ejecutar

1. Abre una terminal y navega hasta el directorio raíz del proyecto donde se encuentra la clase principal (`Main.java`).
2. Compila el código fuente ejecutando el siguiente comando:
   ```bash
   javac Main.java
3. Ejecuta la aplicación gráfica: 
   java Main
