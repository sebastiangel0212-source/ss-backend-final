# API del Proyecto S&S (Gestión de Inventario y Ventas)

Esta es la documentación oficial para la API REST del proyecto S&S.

## Autenticación

### 1. Registrar Usuario
- **Endpoint:** `/api/auth/register`
- **Método:** `POST`
- **Body (JSON):**
  ```json
  {
      "username": "nombre_de_usuario",
      "password": "una_contraseña"
  }
  ```

### 2. Iniciar Sesión
- **Endpoint:** `/api/auth/login`
- **Método:** `POST`
- **Body (JSON):**
  ```json
  {
      "username": "nombre_de_usuario",
      "password": "una_contraseña"
  }
  ```

---
## Módulo de Productos

**Ruta Base:** `/api/productos`
_Nota: Todas las peticiones a este módulo requieren autenticación._

### 1. Obtener todos los productos
- **Endpoint:** `/`
- **Método:** `GET`

### 2. Obtener un producto por ID
- **Endpoint:** `/{id}`
- **Método:** `GET`

### 3. Crear un nuevo producto
- **Endpoint:** `/`
- **Método:** `POST`
- **Body (JSON):**
  ```json
  {
      "nombre": "Nuevo Producto",
      "descripcion": "Descripción del producto",
      "precio": 15000.00,
      "cantidadStock": 100
  }
  ```

### 4. Actualizar un producto existente
- **Endpoint:** `/{id}`
- **Método:** `PUT`
- **Body (JSON):**
  ```json
  {
      "nombre": "Producto Actualizado",
      "descripcion": "Nueva descripción",
      "precio": 16500.00,
      "cantidadStock": 95
  }
  ```

### 5. Eliminar un producto
- **Endpoint:** `/{id}`
- **Método:** `DELETE`