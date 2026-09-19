# Gestor de Comercio — Sprints 2, 3 y 4

*Etapa 1 (MVP) — continuación del Sprint 1*

---

## ⚠️ Nota sobre el orden de sprints

El plan original tenía: Sprint 2 Ingresos, Sprint 3 Categorías+Egresos, Sprint 4 Proveedores. Al diseñar el detalle de `Egreso`, se detectó que necesita una asociación opcional hacia `Proveedor` (`private Proveedor proveedor;`). Si se programa Egresos antes que Proveedores, la tabla `egresos` necesitaría una foreign key hacia una tabla (`proveedores`) que todavía no existe.

**Se invierte el orden:** ahora es Sprint 2 Ingresos → **Sprint 3 Proveedores** → **Sprint 4 Categorías + Egresos** → Sprint 5 Compras a proveedores → Sprint 6 Pedidos → Sprint 7 Dashboard.

---

# SPRINT 2 — Ingresos

## Objetivo
Permitir registrar, listar, filtrar y totalizar ingresos del negocio, con navegación real desde el menú (reemplaza el placeholder del Sprint 1).

## Resultado esperado
Podés abrir la sección "Ingresos", cargar un nuevo ingreso con fecha/monto/medio de pago/descripción, verlo aparecer en la tabla, filtrarlos por rango de fechas y medio de pago, y ver el total del período filtrado.

### 1. Base de datos

Tabla `ingresos` (generada por Hibernate a partir de la entity, `hibernate.hbm2ddl.auto=update`):
- `id` BIGSERIAL PK
- `fecha` DATE NOT NULL
- `monto` NUMERIC(12,2) NOT NULL
- `medio_pago` VARCHAR(20) NOT NULL
- `descripcion` VARCHAR(255) NULL

Índice: `idx_ingreso_fecha` sobre `fecha` — vas a filtrar por rango de fechas constantemente y la tabla va a crecer con el uso diario.

*Nota:* Hibernate no genera `CHECK (monto > 0)` automáticamente desde las anotaciones. La validación real de "monto mayor a 0" vive en el Service (sección 4); el `CHECK` a nivel de base es una mejora opcional para más adelante, no bloqueante para el MVP.

### 2. Domain

- **Enum `MedioPago`** (`domain/enums/MedioPago.java`): `EFECTIVO`, `MERCADO_PAGO`, `TRANSFERENCIA`, `TARJETA`, `OTRO`.
- **Entity `Ingreso`** (`domain/entity/Ingreso.java`): `id` (`Long`, `@Id @GeneratedValue`), `fecha` (`LocalDate`), `monto` (`BigDecimal`), `medioPago` (enum, `@Enumerated(EnumType.STRING)`), `descripcion` (`String`). Constructor vacío (JPA lo exige) + constructor con los campos de negocio.

*Concepto JPA:* `@Enumerated(EnumType.STRING)` guarda el nombre del enum como texto ("EFECTIVO"), no su posición numérica (`EnumType.ORDINAL`). Así, si el día de mañana agregás un valor en el medio del enum, no se corrompen los datos ya guardados.

### 3. Repository

`IngresoRepository` (con `EntityManager` inyectado por constructor, JPA puro, sin Spring):
- `Ingreso save(Ingreso ingreso)` — `persist` si `id` es null, `merge` si ya existe.
- `Optional<Ingreso> findById(Long id)`
- `List<Ingreso> findByFechaBetween(LocalDate desde, LocalDate hasta)` — JPQL: `SELECT i FROM Ingreso i WHERE i.fecha BETWEEN :desde AND :hasta ORDER BY i.fecha DESC`
- `List<Ingreso> findByFechaBetweenAndMedioPago(LocalDate desde, LocalDate hasta, MedioPago medioPago)` — mismo query + `AND i.medioPago = :medioPago`
- `BigDecimal sumMontoByFechaBetween(LocalDate desde, LocalDate hasta)` — JPQL: `SELECT COALESCE(SUM(i.monto), 0) FROM Ingreso i WHERE i.fecha BETWEEN :desde AND :hasta`

*Concepto:* `COALESCE(SUM(...), 0)` evita que la consulta devuelva `null` cuando no hay ningún ingreso en el rango (la suma de cero filas es `null` en SQL, no `0`).

### 4. Service

`IngresoService` (recibe `IngresoRepository` por constructor):
- `Ingreso registrar(RegistrarIngresoCommand command)` — valida: monto no nulo y mayor a 0 (si no, `IllegalArgumentException` "El monto debe ser mayor a 0"); fecha no nula y no futura (`IllegalArgumentException` "La fecha no puede ser futura"); medioPago no nulo. Si es válido, crea el `Ingreso` y llama a `repository.save()`.
- `List<Ingreso> listar(LocalDate desde, LocalDate hasta, MedioPago medioPagoOpcional)` — si `medioPagoOpcional` es null usa `findByFechaBetween`, si no `findByFechaBetweenAndMedioPago`.
- `BigDecimal totalPeriodo(LocalDate desde, LocalDate hasta)` — delega en `repository.sumMontoByFechaBetween`.

**Nota de diseño:** no se implementa edición ni borrado de ingresos en este sprint. Un ingreso ya registrado afecta reportes y el dashboard; permitir editarlo o borrarlo libremente en el MVP abre la puerta a inconsistencias evitables simplemente no dando esa opción todavía. Si hace falta corregir errores de carga, se agrega en Etapa 2 una función de "anular" (soft delete con motivo), no un borrado físico.

### 5. JavaFX

**`IngresosView.fxml`** (reemplaza el placeholder del Sprint 1):
- Fila superior: `ComboBox` `filtroRapido` ("Hoy", "Esta semana", "Este mes", "Personalizado"); si se elige "Personalizado" se habilitan dos `DatePicker` (desde/hasta); `ComboBox` `filtroMedioPago` (incluye "Todos" + los valores del enum).
- `TableView<Ingreso>` columnas: Fecha, Monto (formateado con separador de miles y `$`), Medio de pago, Descripción.
- Debajo de la tabla: `Label` resumen "Total del período: $X".
- Botón "Nuevo Ingreso" abre un modal (`Stage`, `Modality.APPLICATION_MODAL`) con `IngresoFormView.fxml`.
- Estado vacío: si la tabla queda sin filas tras un filtro, mostrar `Label` centrado "No hay ingresos registrados en este período".

**`IngresoFormView.fxml`** (modal):
- `DatePicker` `fecha` (default: hoy)
- `TextField` `monto` (borde rojo si no es numérico o es ≤ 0 al perder foco)
- `ComboBox` `medioPago` (obligatorio, sin selección por defecto)
- `TextArea` `descripcion` (opcional)
- Botón "Guardar" (deshabilitado hasta que fecha, monto y medioPago sean válidos) y "Cancelar"

### 6. Integración

```
IngresosController
  → botón "Nuevo Ingreso" abre IngresoFormController (modal)
  → IngresoFormController arma un RegistrarIngresoCommand con los datos del form
  → llama a IngresoService.registrar(command)
  → si OK: cierra el modal, dispara un callback para que IngresosController
    recargue la tabla (vía Task, no en el hilo de UI)
  → si falla (IllegalArgumentException): muestra el mensaje en un Label
    rojo dentro del modal, sin cerrarlo

IngresosController
  → al cambiar cualquier filtro, lanza un Task<List<Ingreso>> que llama a
    IngresoService.listar(...)
  → al completar, actualiza la ObservableList de la tabla y el Label de total
```

### 7. Reglas de negocio
- El monto debe ser mayor a 0.
- La fecha no puede ser futura.
- El medio de pago es obligatorio.
- No se permite editar ni eliminar un ingreso ya registrado en este sprint.

### 8. Manejo de errores

| Caso | Comportamiento esperado |
|---|---|
| Monto vacío o no numérico | Validación visual en el `TextField` (borde rojo + tooltip), sin llamar al Service |
| Fecha futura | Service lanza `IllegalArgumentException`, se muestra como texto dentro del modal |
| Falla de conexión al guardar/listar | `Alert` genérico (mismo patrón Task + Alert del Sprint 1) |

### 9. Tests

`IngresoServiceTest` (JUnit + Mockito, mockeando `IngresoRepository`):
- `registrar_conDatosValidos_guardaCorrectamente`
- `registrar_conMontoNegativoOCero_lanzaIllegalArgumentException`
- `registrar_conFechaFutura_lanzaIllegalArgumentException`
- `registrar_sinMedioPago_lanzaIllegalArgumentException`
- `totalPeriodo_delegaCorrectamenteEnRepository` (con `Mockito.verify`, confirmando que se llama a `sumMontoByFechaBetween` con los parámetros correctos)

### 10. Checklist

- [ ] `Ingreso` entity creada
- [ ] `MedioPago` enum creado
- [ ] `IngresoRepository` con los 4 métodos
- [ ] `IngresoService` con `registrar`/`listar`/`totalPeriodo` y sus validaciones
- [ ] `IngresosView.fxml` reemplaza el placeholder
- [ ] `IngresoFormView.fxml` modal funcionando
- [ ] Filtros por fecha y medio de pago funcionando
- [ ] Total del período visible y correcto
- [ ] Estado vacío mostrado cuando corresponde
- [ ] Tests de `IngresoServiceTest` pasando
- [ ] Toda consulta a la base corre en `Task`, no en el hilo de JavaFX

### Documentación / Recursos
- Hibernate/JPA — JPQL básico (BETWEEN, funciones de agregación): https://docs.jboss.org/hibernate/orm/6.5/userguide/html_single/Hibernate_User_Guide.html#hql
- JavaFX — TableView y ObservableList: https://openjfx.io/javadoc/21/javafx.controls/javafx/scene/control/TableView.html
- JavaFX — Modal con Stage (`Modality.APPLICATION_MODAL`): https://openjfx.io/javadoc/21/javafx.graphics/javafx/stage/Modality.html
- Mockito — Mockear un Repository en un test de Service: https://site.mockito.org/

---

# SPRINT 3 — Proveedores

## Objetivo
Permitir dar de alta, editar, buscar y ver el detalle de proveedores, con sus tipos configurables.

## Resultado esperado
Podés crear un tipo de proveedor nuevo, crear un proveedor asignándole un tipo, verlo en un listado con buscador, y entrar a su ficha de detalle (el detalle de "total comprado/pagado/deuda" se completa recién en el Sprint 5, cuando exista Compras).

### 1. Base de datos

Tabla `tipos_proveedor`:
- `id` BIGSERIAL PK
- `nombre` VARCHAR(100) NOT NULL UNIQUE

Tabla `proveedores`:
- `id` BIGSERIAL PK
- `nombre` VARCHAR(150) NOT NULL
- `telefono` VARCHAR(30) NULL
- `datos_importantes` TEXT NULL
- `activo` BOOLEAN NOT NULL DEFAULT true
- `tipo_proveedor_id` BIGINT NOT NULL REFERENCES tipos_proveedor(id)

*Nota sobre el nombre único:* la regla real es "único **entre proveedores activos**" (un proveedor desactivado no debería bloquear el nombre para uno nuevo). Hibernate no soporta un `UNIQUE` condicional simple desde anotaciones, así que esta regla se implementa como validación en el Service (sección 4), no como constraint de base en este sprint. *(Dato de aprendizaje: en PostgreSQL puro sí se puede lograr con `CREATE UNIQUE INDEX ... WHERE activo = true`; queda como mejora futura, no bloqueante.)*

Índice: `idx_proveedor_nombre` sobre `proveedores.nombre` para búsquedas rápidas.

### 2. Domain

- **Entity `TipoProveedor`**: `id`, `nombre`.
- **Entity `Proveedor`**: `id`, `nombre`, `telefono`, `datosImportantes`, `activo` (default `true`), y asociación `@ManyToOne(fetch = FetchType.LAZY) private TipoProveedor tipo`.

*Concepto JPA:* `FetchType.LAZY` vs `EAGER` — `LAZY` significa que Hibernate no trae el `TipoProveedor` de la base hasta que accedés a `proveedor.getTipo()` en el código; `EAGER` lo trae siempre junto con el `Proveedor`. Para `@ManyToOne` como este, `LAZY` es la práctica recomendada salvo que sepas que siempre vas a necesitar el dato relacionado.

### 3. Repository

`TipoProveedorRepository`: `save`, `findAll`, `findById`.

`ProveedorRepository`:
- `save(Proveedor proveedor)`
- `findById(Long id)`
- `List<Proveedor> findAllActivos()`
- `List<Proveedor> buscarPorNombre(String texto)` — JPQL con `LOWER(p.nombre) LIKE LOWER(:texto)` (case-insensitive)
- `List<Proveedor> findByTipo(TipoProveedor tipo)`
- `boolean existeActivoConNombre(String nombre)` — usado por el Service para la validación de duplicados

### 4. Service

`TipoProveedorService`: `crear(String nombre)` (valida no vacío y no duplicado), `listar()`.

`ProveedorService`:
- `crear(CreateProveedorCommand)` — valida nombre obligatorio (2-150 caracteres), que no exista otro proveedor activo con el mismo nombre (usando `existeActivoConNombre`), que el `tipoProveedorId` corresponda a un tipo existente. Si todo ok, guarda.
- `actualizar(Long id, UpdateProveedorCommand)` — mismas validaciones de nombre (excluyendo al propio proveedor de la comprobación de duplicados); permite cambiar teléfono, datos importantes y tipo.
- `desactivar(Long id)` — pone `activo = false` (soft delete), no borra físicamente porque puede tener compras asociadas en el historial.
- `buscar(String texto)` — delega en `buscarPorNombre`.
- `listarActivos()`

### 5. JavaFX

**`ProveedorListView.fxml`:**
- `TextField` `searchField` (busca en vivo o con botón "Buscar")
- `ComboBox` `filtroTipo` (incluye "Todos" + tipos existentes)
- `TableView<Proveedor>` columnas: Nombre, Tipo, Teléfono, Estado (Activo/Inactivo)
- Botones: "Nuevo Proveedor", "Ver Detalle", "Editar", "Desactivar" (pide confirmación con `Alert` tipo `CONFIRMATION`)

**`ProveedorFormView.fxml`** (modal, alta y edición comparten la misma vista):
- `TextField` `nombre`
- `ComboBox` `tipoProveedor` (con opción "+ Crear nuevo tipo…" que abre un diálogo simple de texto para crear un `TipoProveedor` sin salir del formulario)
- `TextField` `telefono`
- `TextArea` `datosImportantes`
- Guardar/Cancelar

**`ProveedorDetalleView.fxml`:**
- Encabezado: nombre, tipo, teléfono, datos importantes
- Sección "Resumen financiero": placeholders "Total comprado: (disponible desde Sprint 5)", "Total pagado: (disponible desde Sprint 5)", "Deuda pendiente: (disponible desde Sprint 5)"
- Sección "Historial de compras": tabla vacía con mensaje "Todavía no hay compras registradas a este proveedor" (se completa en Sprint 5)

### 6. Integración

```
ProveedorListController
  → botón "Nuevo Proveedor" abre ProveedorFormController (modal, modo ALTA)
  → arma CreateProveedorCommand → ProveedorService.crear(command)
  → botón "Editar" abre el mismo form (modo EDICIÓN, precargado)
    → arma UpdateProveedorCommand → ProveedorService.actualizar(id, command)
  → botón "Desactivar" → Alert de confirmación →
    si confirma: ProveedorService.desactivar(id) → recarga tabla
  → doble clic en una fila (o "Ver Detalle") → abre ProveedorDetalleController
    pasándole el Proveedor seleccionado

Todas las consultas (buscar, listar, cargar detalle) corren en Task.
```

### 7. Reglas de negocio
- Nombre obligatorio, entre 2 y 150 caracteres.
- No puede haber dos proveedores activos con el mismo nombre.
- Un proveedor debe tener un tipo asignado (obligatorio).
- No se borra físicamente un proveedor: se desactiva (`activo = false`).

### 8. Manejo de errores

| Caso | Comportamiento esperado |
|---|---|
| Nombre vacío o muy corto | Validación visual en el formulario |
| Nombre duplicado entre activos | Excepción de negocio mostrada como texto en el formulario, sin cerrar el modal |
| Tipo no seleccionado | Botón "Guardar" deshabilitado hasta elegir un tipo |

*Nota:* este es el primer sprint donde conviene empezar la carpeta `exception/` con una excepción propia (ej. `NombreDuplicadoException`) en vez de usar `IllegalStateException` genérica — ayuda a que el Controller distinga este caso puntual de otros errores inesperados.

### 9. Tests

`ProveedorServiceTest` (Mockito):
- `crear_conNombreValidoYUnico_guardaCorrectamente`
- `crear_conNombreDuplicadoEntreActivos_lanzaExcepcion`
- `crear_sinTipoProveedor_lanzaIllegalArgumentException`
- `desactivar_marcaActivoEnFalse_sinBorrarFisicamente` (verificar con Mockito que se llama a `save` con `activo=false`, nunca a un método de borrado)

### 10. Checklist

- [ ] `TipoProveedor` y `Proveedor` entities creadas
- [ ] Repositories con los métodos listados
- [ ] `ProveedorService` con `crear`/`actualizar`/`desactivar`/`buscar` validando todo lo de la sección 7
- [ ] `ProveedorListView` con buscador y filtro por tipo funcionando
- [ ] Alta y edición desde el mismo formulario
- [ ] Desactivar pide confirmación
- [ ] `ProveedorDetalleView` muestra info básica con placeholders para lo que llega en Sprint 5
- [ ] Tests pasando

### Documentación / Recursos
- JPA — `@ManyToOne` y `FetchType`: https://docs.jboss.org/hibernate/orm/6.5/userguide/html_single/Hibernate_User_Guide.html#associations
- JavaFX — Alert de confirmación (`Alert.AlertType.CONFIRMATION`): https://openjfx.io/javadoc/21/javafx.controls/javafx/scene/control/Alert.html
- JavaFX — ComboBox con opciones dinámicas: https://openjfx.io/javadoc/21/javafx.controls/javafx/scene/control/ComboBox.html

---

# SPRINT 4 — Categorías + Egresos

## Objetivo
Permitir crear categorías configurables y registrar egresos/gastos del negocio, opcionalmente asociados a un proveedor (usando lo construido en el Sprint 3).

## Resultado esperado
Podés crear categorías propias, registrar un egreso (con o sin proveedor), verlo en una tabla con filtros, y ver el total de egresos del período. Si el egreso tiene proveedor, queda vinculado (la vista completa de "historial de compras" dentro del proveedor se activa recién en el Sprint 5).

### 1. Base de datos

Tabla `categorias`:
- `id` BIGSERIAL PK
- `nombre` VARCHAR(100) NOT NULL UNIQUE
- `activa` BOOLEAN NOT NULL DEFAULT true

Tabla `egresos`:
- `id` BIGSERIAL PK
- `fecha` DATE NOT NULL
- `monto` NUMERIC(12,2) NOT NULL
- `medio_pago` VARCHAR(20) NOT NULL
- `descripcion` VARCHAR(255) NULL
- `tipo` VARCHAR(20) NOT NULL DEFAULT 'OPERATIVO'
- `estado_pago` VARCHAR(20) NOT NULL
- `categoria_id` BIGINT NOT NULL REFERENCES categorias(id)
- `proveedor_id` BIGINT NULL REFERENCES proveedores(id)

Índices: `idx_egreso_fecha` (igual que ingresos), `idx_egreso_proveedor` (para cuando en Sprint 5 se consulte el historial de compras por proveedor).

*Nota:* esta es la primera tabla que depende de otra creada en un sprint anterior (`proveedores`, Sprint 3) — por eso se invirtió el orden, para que la FK ya exista cuando se necesita.

### 2. Domain

- **Enum `TipoEgreso`**: `OPERATIVO`, `INVERSION`, `RETIRO_DUENO`, `SUELDO`. En este sprint solo se usa `OPERATIVO` en la práctica (la UI ni siquiera ofrece elegir otro valor todavía), pero el campo y el enum completo se crean ahora para no migrar datos en Etapa 2.
- **Enum `EstadoPago`**: `PAGADO`, `PENDIENTE`. *(En este sprint no se implementa `PARCIAL` todavía: los pagos parciales con historial son responsabilidad del Sprint 5 mediante una entidad `Pago` dedicada. Acá un egreso es pagado o pendiente, todo o nada.)*
- **Entity `Categoria`**: `id`, `nombre`, `activa`.
- **Entity `Egreso`**: `id`, `fecha`, `monto`, `medioPago`, `descripcion`, `tipo` (default `OPERATIVO` al crear), `estadoPago`, `@ManyToOne(fetch = LAZY) Categoria categoria` (obligatoria), `@ManyToOne(fetch = LAZY) Proveedor proveedor` (opcional, `nullable = true`).

### 3. Repository

`CategoriaRepository`: `save`, `findAllActivas`, `existeActivaConNombre(String nombre)`.

`EgresoRepository`:
- `save(Egreso egreso)`
- `findById(Long id)`
- `List<Egreso> findByFechaBetween(desde, hasta)`
- `List<Egreso> findByFechaBetweenAndCategoria(desde, hasta, categoria)`
- `List<Egreso> findByProveedor(Proveedor proveedor)` — se usa a fondo recién en Sprint 5, pero se crea ahora junto con el resto
- `BigDecimal sumMontoByFechaBetween(desde, hasta)` — mismo patrón que `IngresoRepository`

### 4. Service

`CategoriaService`: `crear(String nombre)` (valida no vacío y no duplicado entre activas), `listarActivas()`.

`EgresoService`:
- `registrar(RegistrarEgresoCommand)` — valida monto > 0, fecha no futura, categoría obligatoria y existente, medioPago obligatorio. Si viene un `proveedorId`, valida que el proveedor exista y esté **activo**. `estadoPago` se recibe del formulario (`PAGADO` o `PENDIENTE`); `tipo` se fija en `OPERATIVO` por default.
- `listar(desde, hasta, categoriaOpcional)` — mismo patrón que `IngresoService.listar`.
- `totalPeriodo(desde, hasta)`.

### 5. JavaFX

**Categorías:** no se crea una pantalla completa de administración todavía (evitamos una pantalla extra para el MVP). En el formulario de egreso, el `ComboBox categoria` tiene una opción "+ Crear nueva categoría…" — mismo patrón ya usado para tipos de proveedor en el Sprint 3.

**`EgresosView.fxml`** (reemplaza el placeholder del Sprint 1):
- Mismos filtros de fecha que `IngresosView` (reutilizar el patrón `ComboBox` "Hoy/Semana/Mes/Personalizado")
- `ComboBox` `filtroCategoria` (incluye "Todas")
- `TableView<Egreso>` columnas: Fecha, Monto, Categoría, Proveedor (muestra "-" si es null), Medio de pago, Estado
- `Label` de total del período
- Botón "Nuevo Egreso" abre modal `EgresoFormView`

**`EgresoFormView.fxml`:**
- `DatePicker` `fecha` (default hoy)
- `TextField` `monto`
- `ComboBox` `categoria` (con "+ Crear nueva categoría…")
- `ComboBox` `proveedor` (opcional, "Ninguno" como primera opción; solo muestra proveedores activos)
- `ComboBox` `medioPago`
- `ComboBox` `estadoPago` (PAGADO/PENDIENTE)
- `TextArea` `descripcion`
- Guardar/Cancelar

### 6. Integración

Mismo patrón que Ingresos (Sprint 2): Controller arma un Command, se lo pasa al Service, que valida y delega en el Repository, con `Task` para no bloquear la UI. La diferencia: el Controller/Service necesita cargar la `Categoria` y el `Proveedor` (si corresponde) por id **antes** de armar la entidad `Egreso`, usando `CategoriaRepository`/`ProveedorRepository`.

### 7. Reglas de negocio
- Monto > 0, fecha no futura, categoría obligatoria, medio de pago obligatorio (mismas reglas que `Ingreso`).
- El proveedor es opcional; si se indica, debe estar activo.
- El tipo se fija en `OPERATIVO` automáticamente en este sprint (no seleccionable desde la UI).
- No se implementan pagos parciales todavía: un egreso es `PAGADO` o `PENDIENTE` completo. El Sprint 5 resuelve el caso de pagos parciales sobre egresos con proveedor.

### 8. Manejo de errores

Mismo patrón que Sprint 2 para monto/fecha. Adicionalmente: "Proveedor seleccionado inactivo" (no debería poder pasar si el `ComboBox` solo carga activos, pero el Service lo revalida por si el dato quedó desactualizado en la sesión) → excepción de negocio mostrada en el formulario.

### 9. Tests

`EgresoServiceTest` (Mockito, mockeando `EgresoRepository`, `CategoriaRepository` y `ProveedorRepository`):
- `registrar_conCategoriaValidaSinProveedor_guardaCorrectamente`
- `registrar_conProveedorActivo_guardaCorrectamenteConProveedorAsociado`
- `registrar_conProveedorInactivo_lanzaExcepcion`
- `registrar_sinCategoria_lanzaIllegalArgumentException`
- `registrar_conMontoNegativo_lanzaIllegalArgumentException`

### 10. Checklist

- [ ] `Categoria` y `Egreso` entities creadas
- [ ] `TipoEgreso` y `EstadoPago` enums creados
- [ ] `CategoriaRepository` y `EgresoRepository` con los métodos listados
- [ ] `CategoriaService` y `EgresoService` con sus validaciones
- [ ] `EgresosView` reemplaza el placeholder
- [ ] Formulario permite crear categoría al vuelo
- [ ] Formulario permite asociar proveedor opcionalmente
- [ ] Filtros por fecha y categoría funcionando
- [ ] Tests pasando

### Documentación / Recursos
- JPA — `@ManyToOne` opcional (`nullable = true`): https://docs.jboss.org/hibernate/orm/6.5/userguide/html_single/Hibernate_User_Guide.html#associations
- JavaFX — `ComboBox` con conversores de texto personalizados (`StringConverter`, para mostrar el nombre del proveedor/categoría en vez del objeto): https://openjfx.io/javadoc/21/javafx.base/javafx/util/StringConverter.html
