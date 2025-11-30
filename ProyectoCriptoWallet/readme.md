Proyecto CriptoWallet (Demo Spring Boot & JPA)

Este proyecto es una aplicación de demostración que implementa la lógica de negocio de una cartera de criptomonedas (Exchange simplificado). Utiliza Spring Boot, Spring Data JPA, Thymeleaf para la interfaz web y se conecta a una base de datos MySQL.

1. Conexión a la Base de Datos

La conexión a la base de datos se define en el fichero src/main/resources/application.properties.

# Configuración para MySQL 8+
spring.datasource.url=jdbc:mysql://localhost:3306/springbootdb?defaultAuthenticationPlugin=caching_sha2_password&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=password

# JPA y Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# CRÍTICO: Carga de datos iniciales
# Obliga a Spring a esperar a que Hibernate cree las tablas antes de ejecutar data.sql
spring.jpa.defer-datasource-initialization=true


2. Estructura de Clases (Entidades Refactorizadas)

El proyecto implementa un modelo de datos avanzado para soportar cantidades exactas de activos y precios de mercado.

Usuario.java

Es la entidad central del sistema.

@OneToOne con Historial: Cada usuario tiene un log de actividad.

@OneToMany con Cartera: Un usuario puede gestionar múltiples carteras.

@OneToMany con Transaccion: Mapeado como origen y destino de los fondos.

Cartera.java (Actualizada)

Representa un monedero digital.

balanceTotal (Fiat): Representa el dinero efectivo (USD/EUR) disponible para invertir.

getPatrimonioEstimado(): Método calculado que suma el efectivo + el valor de mercado de todos los activos.

Relación con Activos: @OneToMany hacia la entidad Activo.

Activo.java (NUEVA ENTIDAD)

Sustituye a la antigua relación N:M simple. Permite saber cuánto se posee de cada criptomoneda.

@ManyToOne con Cartera.

@ManyToOne con Criptomoneda.

cantidad: Cantidad exacta del activo (ej. 0.05 BTC).

Criptomoneda.java

Datos maestros de las monedas.

precioActual: (Nuevo) Precio de mercado utilizado para calcular conversiones en tiempo real.

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

Creación automática de Historial al registrar un usuario.

CarteraService

Creación: Apertura de nuevas carteras.

Inversión (Compra):

Recibe una orden de compra en Fiat (ej. "$500").

Calcula la conversión: Cantidad = Inversión / PrecioActual.

Resta saldo Fiat de la cartera y crea/actualiza el Activo correspondiente.

Cálculo de Patrimonio: Lógica para valorizar todas las carteras de un usuario.

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

Botones para crear nuevos usuarios y borrarlos (cascada).

dashboard.html (Panel Principal):

Resumen: Muestra el Patrimonio Total Estimado (Cripto + Fiat).

Gestión de Carteras:

Visualización de saldo Fiat y lista de Activos.

Formulario de Inversión: Permite comprar cripto directamente usando el saldo disponible.

Historial: Consola visual estilo hacker.

transferencia.html (Operaciones):

Formulario avanzado con JavaScript.

Lógica Cliente: Al seleccionar cartera y cripto, calcula dinámicamente el saldo disponible y muestra el límite en dólares en tiempo real.

form-usuario.html: Formulario de registro.

5. Pruebas y Casos de Uso (Tests)

El proyecto incluye tests de integración (UserDemoCasosUso.java) que validan el correcto funcionamiento de la capa de datos.

Test Usuario: Creación, búsqueda y validación de integridad referencial.

Test Cartera: Cálculo de balances y relaciones.

Test Integridad: Verificación de borrado en cascada (al borrar un Usuario se eliminan sus carteras, activos y transacciones).

6. Cómo Ejecutar el Proyecto

Asegúrate de tener un servidor MySQL en ejecución (Puerto 3306).

Desde la terminal en la raíz del proyecto:

# En Windows
.\mvnw.cmd spring-boot:run


La aplicación cargará automáticamente los datos de prueba definidos en data.sql.

Accede desde el navegador a: http://localhost:8080


El diagrama de Entidad-Relacion se adjunta en el proyecto.

![img.png](img.png)