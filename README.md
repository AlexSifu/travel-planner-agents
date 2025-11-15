# Sistema multi-agente para planificacion de viajes turisticos (JADE)

Este proyecto implementa un sistema multi-agente usando **JADE** (Java Agent DEvelopment Framework) para apoyar la decision de realizar un viaje turistico dentro del Peru.

El sistema recibe como entrada:

- Ciudad de destino  
- Numero de dias del viaje  
- Presupuesto total en soles  

Y, a partir de esa informacion, varios agentes colaboran para:

- Estimar el clima simbolico de la ciudad
- Calcular un costo aproximado y evaluar el presupuesto
- Recomendar actividades turisticas
- Generar una recomendacion final sobre el viaje

---

## Tecnologias utilizadas

- Java 17  
- JADE 4.5.0  
- Maven 3+  
- Consola / terminal (entrada por teclado)  

---

## Arquitectura de agentes

El sistema se ejecuta en un contenedor principal de JADE (`Main-Container`) y utiliza **paginas amarillas** (Directory Facilitator) para registrar y descubrir servicios.

Agentes principales:

- **PlannerAgent**
  - Rol: agente coordinador.
  - Recibe ciudad, dias y presupuesto como argumentos.
  - Busca servicios en las paginas amarillas:
    - `weather-service`
    - `budget-service`
    - `activity-service`
  - Envia mensajes `ACLMessage.REQUEST` a los agentes especializados.
  - Recibe respuestas `ACLMessage.INFORM` y combina:
    - clima
    - evaluacion de presupuesto
    - actividades sugeridas
  - Genera la decision final: viaje **recomendado**, **posible con advertencias** o **no recomendado**.

- **WeatherAgent**
  - Servicio registrado: `weather-service`.
  - Mantiene un mapa de ciudades turisticas del Peru con perfiles climaticos simbolicos (Cusco, Huaraz, Lima, Arequipa, Iquitos, Puno, Paracas, Mancora, Trujillo, Nazca).
  - Responde con descripciones de clima como "clima bueno", "clima variable", "clima humedo", etc.

- **BudgetAgent**
  - Servicio registrado: `budget-service`.
  - Mantiene un mapa de ciudades con:
    - costo base por dia
    - nivel de costo (bajo, medio, medio-alto)
  - Calcula un costo estimado = costo base * dias.
  - Clasifica el presupuesto como:
    - **Adecuado**
    - **Ajustado**
    - **Insuficiente**

- **ActivityAgent**
  - Servicio registrado: `activity-service`.
  - Contiene un conjunto de actividades recomendadas por ciudad (Machu Picchu, islas Ballestas, playas, etc.).
  - Si la ciudad no esta en el mapa, devuelve actividades genericas (city tour, gastronomia local, etc.).

Todos los mensajes relacionados con una misma consulta usan `conversationId = "travel-planning"`.

---

## Estructura del proyecto

```text
travel-planner-agents/
├─ pom.xml
└─ src/
   └─ main/
      └─ java/
         └─ pe/
            └─ unasam/
               └─ multiagent/
                  ├─ MainContainer.java
                  ├─ PlannerAgent.java
                  ├─ WeatherAgent.java
                  ├─ BudgetAgent.java
                  └─ ActivityAgent.java
```

- `pom.xml`: definicion del proyecto Maven, dependencia local a JADE y configuracion del plugin `exec-maven-plugin`.
- `MainContainer.java`: punto de entrada. Lee datos por consola, inicializa el contenedor JADE y crea los agentes.
- `PlannerAgent.java`: agente coordinador, integra las respuestas de los demas agentes.
- `WeatherAgent.java`: agente de clima.
- `BudgetAgent.java`: agente de presupuesto.
- `ActivityAgent.java`: agente de actividades turisticas.

---

## Requisitos previos

- Java 17 instalado (`java -version`)
- Maven 3+ instalado (`mvn -version`)
- JADE JAR instalado en el repositorio local de Maven, por ejemplo:

```bash
mvn install:install-file   -Dfile=jade.jar   -DgroupId=com.tilab.jade   -DartifactId=jade   -Dversion=4.5.0   -Dpackaging=jar
```

*(Ajustar ruta y version segun el JAR que se use.)*

---

## Instalacion y ejecucion

Clonar el repositorio:

```bash
git clone https://github.com/<usuario>/travel-planner-agents.git
cd travel-planner-agents
```

Compilar el proyecto:

```bash
mvn clean compile
```

Ejecutar el contenedor principal y los agentes:

```bash
mvn exec:java
```

Flujo al ejecutar:

1. La aplicacion pide por consola:
   - Ciudad destino (por ejemplo: `Cusco`, `Huaraz`, `Lima`, `Paracas`, etc.)
   - Numero de dias
   - Presupuesto total

2. Se abre la GUI de JADE (Remote Agent Management GUI) mostrando:
   - Contenedor `Main-Container`
   - Agentes: `Planner`, `WeatherService`, `BudgetService`, `ActivityService`

3. En la consola se imprime:
   - Respuesta de `WeatherAgent` con el clima.
   - Respuesta de `BudgetAgent` con la evaluacion del presupuesto.
   - Respuesta de `ActivityAgent` con actividades sugeridas.
   - Recomendacion final generada por `PlannerAgent`.

---

## Autor

- ALEX GEISLER SIFUENTES AGURTO
