Proyecto CriptoWallet (Demo Spring Boot & JPA)

Este proyecto es una aplicación de demostración que implementa la lógica de negocio de una cartera de criptomonedas (Exchange simplificado). Utiliza Spring Boot, Spring Data JPA, Thymeleaf para la interfaz web y se conecta a una base de datos MySQL.

Para visualizar el contenido una vez arrancada la bd y compilado el proyecto buscamos http://localhost:8080

1. Conexión a la Base de Datos

La conexión a la base de datos se define en el fichero src/main/resources/application.properties.

spring.datasource.url=jdbc:mysql://localhost:3306/springbootdb
spring.datasource.username=root
spring.datasource.password=password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.defer-datasource-initialization=true

spring.sql.init.mode=always

El comando de creacion de la base de datos es:
docker run -d --name mysql-container -e MYSQL_ROOT_PASSWORD=password -e MYSQL_DATABASE=springbootdb -e MYSQL_USER=usuario -e MYSQL_PASSWORD=clave123 -p 3306:3306 mysql:8


2. Estructura de Clases (Entidades Refactorizadas)

El proyecto implementa un modelo de datos avanzado para soportar cantidades exactas de activos y precios de mercado.

Usuario.java

Es la entidad central del sistema.

@OneToOne con Historial: Cada usuario tiene un log de actividad.

@OneToMany con Cartera: Un usuario puede gestionar múltiples carteras.

@OneToMany con Transaccion: Mapeado como origen y destino de los fondos.

Cartera.java

Representa un monedero digital.

balanceTotal (Fiat): Representa el dinero efectivo (USD/EUR) disponible para invertir.

getPatrimonioEstimado(): Método calculado que suma el efectivo + el valor de mercado de todos los activos.

Relación con Activos: @OneToMany hacia la entidad Activo.

Activo.java

Sustituye a la antigua relación N:M simple. Permite saber cuánto se posee de cada criptomoneda.

@ManyToOne con Cartera.

@ManyToOne con Criptomoneda.

cantidad: Cantidad exacta del activo (ej. 0.05 BTC).

Criptomoneda.java

Datos maestros de las monedas.

precioActual:  Precio de mercado utilizado para calcular conversiones en tiempo real.

@OneToMany con Transacción.

Transaccion.java

Registro inmutable de movimientos entre usuarios.

Registra usuario origen, destino, la criptomoneda y la cantidad de unidades transferidas.

Historial.java

Registro de texto (log) vinculado al usuario para mostrar actividad en el dashboard.

3. Capa de Servicios (Lógica de Negocio)

La lógica compleja se ha encapsulado en servicios dedicados en src/main/java/.../services/.

UsuarioService

Creación de usuarios con validaciones (Regex email, longitud nombre).

ELiminación de usuarios.

Creación automática de Historial al registrar un usuario.

CarteraService

Creación: Apertura de nuevas carteras.

Inversión (Compra):

Recibe una orden de compra en Fiat (ej. "$500").

Calcula la conversión: Cantidad = Inversión / PrecioActual.

Resta saldo Fiat de la cartera y crea/actualiza el Activo correspondiente.

Cálculo de Patrimonio: Lógica para valorizar todas las carteras de un usuario.

ELiminación de cartera.

TransaccionService

Transferencias entre Usuarios:

Valida que la cartera de origen pertenece al usuario.

Verifica que existe el Activo y tiene cantidad suficiente.

Resta unidades al origen y suma al destino (creando el activo si el destino no lo tenía).

Genera el registro en Transaccion y actualiza los textos en Historial.

4. Capa Web (Controlador y Vistas)

Se ha implementado una interfaz web completa utilizando Thymeleaf y Bootstrap 5 (Dark Theme).

UserDemoWebController.java

Controlador Spring MVC que gestiona el flujo de la aplicación.

Inyecta dependencias de servicios y repositorios.

Maneja excepciones y mensajes de error/éxito mediante RedirectAttributes (alertas visuales).

Vistas HTML (src/main/resources/templates/)

index.html (Home):

Listado de usuarios existentes.

Botones para crear nuevos usuarios y borrarlos (cascada), así como para acceder a su cartera (al dashboard).

dashboard.html (Panel Principal):

Resumen: Muestra el Patrimonio Total Estimado (Cripto + Fiat).

Gestión de Carteras:

Visualización de saldo Fiat y lista de Activos.

Formulario de Inversión: Permite comprar cripto directamente usando el saldo disponible.

Formulario para creación de nueva cartera indicando el saldo.

Botón de vuelta a la lista de usuarios.

Historial: Consola visual estilo hacker.

transferencia.html (Operaciones):

Formulario avanzado con JavaScript.

Lógica Cliente: Al seleccionar cartera y cripto, calcula dinámicamente el saldo disponible y muestra el límite en dólares en tiempo real.

form-usuario.html: Formulario de registro.

5. Pruebas y Casos de Uso (Tests)

5.1. Casos de Uso: USUARIO (Tests 1-5)

Test 1: Verificar Carga Inicial: Comprueba que data.sql ha cargado los 3 usuarios, 3 criptos, 4 carteras, 3 historiales y 3 transacciones.

Test 2: Crear Usuario y Historial: Demuestra que CascadeType.ALL funciona al crear un Usuario nuevo ("David") y su Historial se guarda automáticamente en la misma operación.

Test 3: Encontrar por Email/Nombre: Valida las consultas simples del repositorio (findByEmail, findByNombreStartingWith).

Test 4: Query Múltiples Carteras: Valida la consulta @Query findUsuariosConMultiplesCarteras(), verificando que solo encuentra a 'Ana' (que es la única con 2 carteras en data.sql).

Test 5: Query por Cripto: Valida la consulta @Query findUsuariosByCriptoSimbolo(), comprobando que encuentra a los usuarios correctos que poseen "BTC".

5.2. Casos de Uso: CARTERA (Tests 6-10)

Test 6: Crear Cartera: Demuestra que se puede crear una nueva cartera (Cartera ID 5) y asignarla a un usuario existente ('Luis').

Test 7: Encontrar Carteras por Usuario: Valida carteraRepository.findByUsuario(ana), confirmando que devuelve 2 carteras (la ID 1 y la ID 4).

Test 8: Query Calcular Balance Total: Valida getBalanceTotalPorUsuario(ana), confirmando que suma los balances de las carteras de Ana (1500 + 100 = 1600.0).

Test 9: Añadir Cripto (N:M): Simula que 'Luis' compra 'BTC'. El test añade 'BTC' al Set de la Cartera 2 y guarda (carteraRepository.save()). JPA inserta la fila en la tabla cartera_cripto.

Test 10: Quitar Cripto (N:M): Simula que 'Ana' vende 'ETH'. El test quita 'ETH' del Set de la Cartera 1 (usando .remove(), que funciona gracias a equals/hashCode) y guarda. JPA elimina la fila de cartera_cripto.

5.3. Casos de Uso: CRIPTOMONEDA (Tests 11-15)

Test 11 y 12: CRUD Básico: Prueba save() creando "Dogecoin" y findBySimbolo() encontrando "BTC".

Test 13: Query Criptos por Cartera: Valida findCriptomonedasByCarterasId(3L), confirmando que encuentra "BTC" y "SOL" (las criptos de la Cartera 3 de Carla).

Test 14: Borrado Fallido (Relaciones): Demuestra que no se puede borrar 'BTC' (criptomonedaRepository.delete(btc)) porque está siendo referenciada por carteras. El test confirma que se lanza una DataIntegrityViolationException.

Test 15: Borrado Exitoso: Demuestra el borrado seguro.

Crea "Cardano (ADA)".

La asigna a la Cartera 2 de Luis y guarda.

La quita de la Cartera 2 de Luis y guarda (rompiendo la relación N:M).

Ahora, criptomonedaRepository.delete(ada) funciona porque no hay referencias.

5.4. Casos de Uso: TRANSACCION (Tests 16-19)

Test 16: Crear Transacción: Crea una nueva transacción de 'Ana' a 'Luis' y verifica que el contador total de transacciones aumenta.

Test 17: Query Transacciones por Usuario: Valida findAllTransaccionesByUsuario(luis), confirmando que encuentra las 2 transacciones donde Luis participa (la 1 como destino, la 3 como origen).

Test 18: Query por Rango de Fechas: Valida findTransaccionesEnRangoDeFechas(), pidiendo transacciones entre dos fechas y confirmando que devuelve 2.

Test 19: Query Transacciones Internas: Valida findTransaccionesInternas() creando una transacción de 'Ana' para 'Ana' y verificando que la consulta la encuentra.

5.5. Casos de Uso: HISTORIAL (Tests 20-22)

Test 20: Encontrar por Usuario (1:1): Valida historialRepository.findByUsuario(carla), comprobando que devuelve el historial correcto.

Test 21: Actualizar Historial: Obtiene el historial de 'Ana', le añade texto (.setDetalle(...)) y guarda. Se comprueba que el texto se ha actualizado.

Test 22: Query Usuarios Activos: Valida countHistorialesConTransacciones(), confirmando que los 3 usuarios de data.sql han participado en transacciones.

5.6. Caso de Uso: BORRADO EN CASCADA (Test 23)

Este es el test más importante de la lógica de persistencia.

Objetivo: Eliminar al usuario 'Luis' (ID 2) y verificar que todo lo que le pertenece se borra automáticamente.

Lógica en Java:

Se verifican los datos de 'Luis' (ID 2): su Historial (ID 2), su Cartera (ID 2), su Transaccion enviada (ID 3) y su Transaccion recibida (ID 1).

Se ejecuta usuarioRepository.deleteById(luisId);.

Qué hace JPA (SQL):
Gracias a las reglas CascadeType.ALL y orphanRemoval=true definidas en la entidad Usuario.java:

JPA detecta que luis (ID 2) va a ser borrado.

Cascada 1:1 (Historial): Borra el Historial de Luis (ID 2).

Cascada 1:N (Carteras): Borra la Cartera de Luis (ID 2).

Cascada 1:N (Transacciones Origen): Borra la Transaccion ID 3.

Cascada 1:N (Transacciones Destino): Borra la Transaccion ID 1.

Final: JPA borra a luis de la tabla usuarios.

Comprobaciones del Test:

Se verifica que Luis, su Cartera (ID 2), su Historial (ID 2), la TX 3 y la TX 1 están borradas (isEmpty()).

Se verifica que los datos de otros usuarios (como la Cartera ID 3 de Carla y la TX 2 de Ana) siguen existiendo (isPresent()).


Continuamos con los detalles del siguiente test en este caso:

5.7. Caso de Uso: Ciclo de Vida CRUD (de UserDemoCRUDTest.java)

Este test prueba la creación en cascada desde una base de datos limpia (no usa data.sql).

Objetivo: Crear un nuevo usuario ("David") con su historial y carteras, y luego borrarlo.

Lógica en Java (CREATE):

Se crea un Usuario ("David").

Se crea un Historial y se enlaza (david.setHistorial(hDavid)).

Se crea una Cartera y se enlaza (david.addCartera(cDavid1)).

Se llama una sola vez a usuarioRepository.save(david).

Qué hace JPA (SQL):
Gracias a CascadeType.ALL en Usuario, JPA ejecuta múltiples sentencias INSERT: una para usuarios, una para historiales y otra para carteras, gestionando las claves foráneas automáticamente.

Lógica en Java (DELETE):

Al final del test, se borra a "David" (usuarioRepository.delete(davidLeido)).

Comprobaciones del Test:

Se verifica que "David" y todas sus entidades (historial, cartera, transacciones) se han borrado, pero los usuarios originales de data.sql (Ana, Luis, Carla) siguen intactos (ya que este test se ejecuta sobre los datos de data.sql).


El último test es el generico que simula un flujo de negocio complejo:

5.8. Caso de Uso: Lógica de Negocio (de UserDemoGenericTests.java)

Este test simula un flujo de negocio complejo sobre los datos de data.sql.

Objetivo: 'Luis' crea una nueva cartera, la financia con BTC y realiza una transferencia a 'Ana'.

Lógica en Java:

READ: Se obtienen 'Luis', 'Ana' y 'BTC' de los repositorios.

CREATE: Se crea una Cartera y se asocia a 'Luis' (luis.addCartera(carteraAhorroLuis)). Se guarda la cartera.

UPDATE (N:M): Se añade 'BTC' a la nueva cartera (carteraAhorroLuis.addCriptomoneda(btc)). Se guarda la cartera.

CREATE: Se crea una Transaccion de 'Luis' a 'Ana' (transaccionRepository.save(nuevaTx)).

UPDATE: Se actualiza el detalle de los Historial de 'Luis' y 'Ana' (historialRepository.save(...)).

Comprobaciones del Test:

Se verifica que Luis ahora tiene 2 carteras, que se ha creado 1 nueva transacción, y que los historiales de ambos usuarios contienen el nuevo detalle.

6. Cómo Ejecutar el Proyecto

Asegúrate de tener un servidor MySQL en ejecución (Puerto 3306).

Desde la terminal en la raíz del proyecto:

# En Windows
.\mvnw.cmd spring-boot:run


La aplicación cargará automáticamente los datos de prueba definidos en data.sql.

Accede desde el navegador a: http://localhost:8080

El diagrama de Entidad-Relacion se adjunta en el proyecto.

![img.png](img.png)