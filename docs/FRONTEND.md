
    # Sitemap y Flujos QuimbayaEVAL

    This is a code bundle for Sitemap y Flujos QuimbayaEVAL. The original project is available at https://www.figma.com/design/P2CRl5kxgRyecFqPkuye3k/Sitemap-y-Flujos-QuimbayaEVAL.

    ## Running the code

    Run `npm i` to install the dependencies.

    Run `npm run dev` to start the development server.

    # Descripción Detallada del Frontend - QuimbayaEVAL

  ## 1. Resumen Ejecutivo

  **QuimbayaEVAL** es un sistema web de gestión de evaluaciones académicas construido con:
  - **Framework**: React 18 con TypeScript
  - **Routing**: React Router v6
  - **UI Components**: Shadcn/ui (Radix UI + Tailwind CSS)
  - **Build Tool**: Vite
  - **Estilos**: Tailwind CSS + personalización con variables
  - **Estado**: React Hooks (Context API) + localStorage
  - **Autenticación**: Simulada en frontend (a implementar en backend)
  - **Notificaciones**: Sonner Toast

  **Propósito**: Permitir a estudiantes, maestros y coordinadores gestionar evaluaciones académicas de manera eficiente en una plataforma web.

  ---

  ## 2. Arquitectura Global

  ### 2.1 Estructura de Carpetas

  ```
  src/
  ├── pages/           # Páginas/Vistas por rol
  ├── components/      # Componentes reutilizables
  │   ├── ui/         # Componentes base Shadcn/ui
  │   └── figma/      # Componentes personalizados (ej: ImageWithFallback)
  ├── hooks/          # Hooks personalizados (useCursos, useEvaluaciones, etc.)
  ├── contexts/       # Contextos React (AuthContext)
  ├── services/       # Data loading (mockData.ts)
  ├── constants/      # ROUTES, ROLES
  ├── types/          # Tipos TypeScript compartidos
  ├── utils/          # Funciones auxiliares (date, format, validation)
  ├── styles/         # CSS global (globals.css, Tailwind imports)
  ├── guidelines/     # Documentación interna
  ├── App.tsx         # Root component con routing
  └── main.tsx        # Punto de entrada

  ```

  ### 2.2 Flujo de Datos Principal

  ```
  User (Browser)
      ↓
  LoginPage → AuthContext → Stores in localStorage
      ↓
  Router (App.tsx) → ProtectedRoute → Dashboard(según rol)
      ↓
  Pages (Dashboard/Evaluaciones/etc) → Hooks (useCursos, useEvaluaciones)
      ↓
  Hooks → mockData (Services) → UI Components (Shadcn + Custom)
      ↓
  Browser DOM + localStorage
  ```

  ---

  ## 3. Autenticación y Autorización

  ### 3.1 AuthContext (`src/contexts/AuthContext.tsx`)

  **Responsabilidad**: Gestionar estado de autenticación y sesión del usuario.

  ```typescript
  interface User {
    id: string;
    name: string;
    email: string;
    role: UserRole;  // 'maestro' | 'estudiante' | 'coordinador'
  }

  interface AuthContextType {
    user: User | null;
    login(email: string, password: string, role: UserRole): Promise<void>;
    logout(): void;
    isAuthenticated: boolean;
  }
  ```

  **Características**:
  - Login simulado (actualmente genera mock users; en producción debe validar contra backend)
  - Persiste usuario en `localStorage` bajo clave `'quimbayaeval_user'`
  - Hook `useAuth()` para acceder en componentes
  - Notificaciones con toast (Sonner)

  **IMPORTANTE**: Actualmente no usa JWT ni tokens. Necesita actualización para:
  1. Recibir token del backend
  2. Guardar token en localStorage
  3. Incluir token en headers de peticiones HTTP

  ### 3.2 Roles y Autorización

  **Archivo**: `src/constants/roles.ts`

  ```typescript
  export const ROLES = {
    ESTUDIANTE: 'estudiante',
    MAESTRO: 'maestro',
    COORDINADOR: 'coordinador',
  } as const;

  export const ROLE_NAMES = {
    estudiante: 'Estudiante',
    maestro: 'Maestro',
    coordinador: 'Coordinador',
  };
  ```

  **Protección de Rutas**: Componente `ProtectedRoute` valida:
  1. `isAuthenticated`: redirige a `/` si no
  2. `allowedRoles` (opcional): muestra "Acceso Denegado" si rol no coincide

  ---

  ## 4. Enrutamiento

  ### 4.1 Rutas Disponibl (`src/constants/routes.ts`)

  ```typescript
  ROUTES = {
    HOME: '/',                          // Login (público)
    DASHBOARD: '/dashboard',            // Dashboard según rol (protegido)
    
    // Estudiante
    MIS_CURSOS: '/mis-cursos',
    MIS_EVALUACIONES: '/mis-evaluaciones',
    REALIZAR_EVALUACION: '/realizar-evaluacion',
    HISTORIAL: '/historial',
    
    // Maestro
    EVALUACIONES: '/evaluaciones',
    CREAR_EVALUACION: '/evaluaciones/nueva',
    CALIFICAR: '/calificar',
    REPORTES: '/reportes',
    
    // Coordinador
    USUARIOS: '/usuarios',
    
    // Común
    PQRS: '/pqrs',
  }
  ```

  ### 4.2 Estructura de Routing (`src/App.tsx`)

  - **Router raíz**: `BrowserRouter` + `AuthProvider` + `ErrorBoundary`
  - **Rutas públicas**: `LoginPage` (render condicionado: si autenticado → `/dashboard`)
  - **Rutas protegidas**: Envueltas en `ProtectedRoute` con roles requeridos
  - **Lazy loading**: Todas las páginas se cargan perezosamente con `React.lazy()` para mejor performance
  - **Fallback**: `LoadingSpinner` mientras se cargan módulos
  - **Catch-all**: Redirige a `/` rutas inválidas

  ---

  ## 5. Tipos de Datos (`src/types/index.ts`)

  ### 5.1 Entidades Principales

  #### Evaluacion
  ```typescript
  interface Evaluacion {
    id: number;
    name: string;           // Título ej: "Parcial 1 - Cálculo Integral"
    curso: string;          // Código ej: "MAT-301"
    profesor?: string;      // Nombre del instructor
    deadline: string;       // ISO datetime
    estado: EstadoEvaluacion;  // 'Activa' | 'Cerrada' | 'Programada' | 'Borrador'
    tipo: TipoEvaluacion;   // 'Examen' | 'Quiz' | 'Taller' | 'Proyecto' | 'Tarea'
    intentos?: number;      // Cantidad de intentos permitidos
    duracion?: string;      // Ej: "120 min"
    pendientes?: number;    // Cantidad de estudiantes / respuestas pendientes
  }
  ```

  #### Curso
  ```typescript
  interface Curso {
    id: number;
    codigo: string;     // Ej: "MAT-301"
    nombre: string;     // Ej: "Cálculo Integral"
    progreso: number;   // 0-100 (%)
    proxEval?: string;  // Próxima evaluación
    evalDate?: string;  // Fecha de próxima evaluación
  }
  ```

  #### TicketPQRS
  ```typescript
  interface TicketPQRS {
    id: number;
    tipo: TipoPQRS;          // 'Pregunta' | 'Reclamo' | 'Sugerencia' | 'Queja'
    asunto: string;          // Título
    descripcion: string;     // Contenido
    estado: EstadoPQRS;      // 'Pendiente' | 'En Proceso' | 'Resuelto'
    fecha: string;           // ISO datetime de creación
    curso: string;           // Código del curso
    respuesta?: string | null;  // Respuesta del docente/coordinador
  }
  ```

  #### Estadistica (para dashboards)
  ```typescript
  interface Estadistica {
    label: string;
    value: string | number;
    icon: React.ComponentType<{ className?: string }>;  // Icono Lucide
    color: string;           // Ej: "text-blue-600"
    bg: string;              // Ej: "bg-blue-50"
  }
  ```

  ---

  ## 6. Páginas por Rol

  ### 6.1 Página de Login

  **Ruta**: `/`
  **Archivo**: `src/pages/LoginPage.tsx`

  **Características**:
  1. **Sección de bienvenida**: Logo QuimbayaEVAL + descripción
  2. **Selección de rol en tabs**: Estudiante / Maestro / Coordinador
  3. **Formulario dinámico**: Email y contraseña (campos opcionales en prototipo)
  4. **Validación**:
    - Email: formato válido si se proporciona
    - Campos opcionales (envía valores default si están vacíos)
  5. **Responsive**: Optimizado para mobile (sm, lg breakpoints)
  6. **Estilos Gestalt**: Agrupación visual por rol, proximidad de elementos
  7. **Accesibilidad**: ARIA labels, roles semánticos

  **Flujo**:
  1. Selecciona rol (tab)
  2. Ingresa email/contraseña (opcionales)
  3. Click "Iniciar Sesión"
  4. `login()` crea mock user y guarda en localStorage
  5. Redirige a `/dashboard`

  ---

  ### 6.2 Dashboard - Estudiante

  **Ruta**: `/dashboard` (rol = 'estudiante')
  **Archivo**: `src/pages/DashboardEstudiante.tsx`

  **Componentes**:
  1. **Header & Layout**: Breadcrumbs, título
  2. **Grid de Estadísticas** (4 cards):
    - Cursos Inscritos
    - Evaluaciones Abiertas
    - Promedio General
    - Completadas
  3. **Evaluaciones Abiertas** (Card principal con borde naranja):
    - Lista de evaluaciones con estado "Activa"
    - Para cada evaluación:
      - Nombre, tipo (badge), curso, profesor
      - Fecha límite, intentos, duración
      - Botón "Iniciar Evaluación"
  4. **Cursos Activos** (Grid de cards):
    - Nombre, código, progreso (progress bar)
    - Próxima evaluación y fecha
  5. **Estados vacíos**: Si no hay evaluaciones/cursos
  6. **Loading states**: Skeletons mientras carga

  **Datos**:
  - Proviene de `useEvaluaciones()` y `useCursos()` hooks
  - Actualmente desde mock data

  ---

  ### 6.3 Dashboard - Maestro

  **Ruta**: `/dashboard` (rol = 'maestro')
  **Archivo**: `src/pages/DashboardMaestro.tsx`

  **Componentes**:
  1. **Grid de Estadísticas** (4 cards):
    - Evaluaciones Activas
    - Por Calificar (respuestas pendientes)
    - Estudiantes (totales inscritos)
    - PQRS Pendientes
  2. **Evaluaciones Recientemente Modificadas**
  3. **Actividad Reciente** (estudiantes que finalizaron evaluaciones)
  4. **Acción rápida**: Botón "Crear Evaluación" → `/evaluaciones/nueva`

  ---

  ### 6.4 Dashboard - Coordinador

  **Ruta**: `/dashboard` (rol = 'coordinador')
  **Archivo**: `src/pages/DashboardCoordinador.tsx`

  **Componentes**:
  1. **Estadísticas consolidadas**: Total de evaluaciones, estudiantes, PQRS, reportes
  2. **Sistemas/Alertas**: Evaluaciones críticas, PQRS sin resolver
  3. **Acceso rápido** a: Usuarios, Reportes, PQRS

  ---

  ### 6.5 Evaluaciones - Listado y Gestión

  **Ruta**: `/evaluaciones`
  **Archivo**: `src/pages/EvaluacionesPage.tsx`
  **Roles**: maestro, coordinador

  **Componentes**:
  1. **Barra de herramientas**:
    - Búsqueda por nombre
    - Filtros: Estado, Tipo, Curso
    - Paginación
  2. **Tabla de evaluaciones**:
    - Columnas: ID, Nombre, Curso, Tipo, Estado, Deadline, Acciones
    - Acciones: Ver, Editar, Duplicar, Publicar, Eliminar
  3. **Botón flotante/sticky**: "Crear Evaluación" → `/evaluaciones/nueva`

  **Datos**:
  - `useEvaluaciones()` hook
  - Mock data (requiere GET `/api/evaluaciones` en backend)

  ---

  ### 6.6 Crear/Editar Evaluación

  **Ruta**: `/evaluaciones/nueva`
  **Archivo**: `src/pages/CrearEvaluacionPage.tsx`
  **Rol**: maestro

  **Componentes**:
  1. **Formulario en pasos/secciones**:
    - Metadatos: Nombre, Curso, Tipo, Deadline
    - Preguntas: Interface para agregar preguntas (tipo selección múltiple, V/F, respuesta corta)
    - Opciones: Intentos, duración, publicar ahora
  2. **Editor de preguntas**:
    - Drag & drop para ordenar
    - Opciones por tipo
    - Guardar como borrador o publicar

  ---

  ### 6.7 Realizar Evaluación

  **Ruta**: `/realizar-evaluacion`
  **Archivo**: `src/pages/RealizarEvaluacionPage.tsx`
  **Rol**: estudiante

  **Componentes**:
  1. **Timer**: Cuenta regresiva según duración
  2. **Pregunta actual**: Renderiza según tipo (selección múltiple, V/F, respuesta corta)
  3. **Navegador de preguntas**: Vista previa de completadas/no completadas
  4. **Botones**: Anterior, Siguiente, Revisar, Enviar
  5. **Confirmación**: Diálogo antes de enviar

  **IMPORTANTE**: Tiene comentario en código diciendo "Aquí se enviaría la evaluación al backend" → necesita POST `/api/evaluaciones/{id}/submit`

  ---

  ### 6.8 Calificar Evaluaciones

  **Ruta**: `/calificar`
  **Archivo**: `src/pages/CalificarPage.tsx`
  **Rol**: maestro

  **Componentes**:
  1. **Selector de evaluación**: Dropdown con evaluaciones sin calificar
  2. **Lista de estudiantes**: Con estado (revisado/sin revisar)
  3. **Panel de calificación**:
    - Pregunta + respuesta de estudiante
    - Rúbrica/puntuación
    - Comentarios
  4. **Botones**: Guardar borrador, Enviar calificaciones

  ---

  ### 6.9 Reportes

  **Ruta**: `/reportes`
  **Archivo**: `src/pages/ReportesPage.tsx`
  **Rol**: maestro, coordinador

  **Componentes**:
  1. **Filtros**: Evaluación, Período, Tipo de reporte
  2. **Gráficas**: 
    - Distribución de calificaciones (histograma)
    - Promedio por evaluación
    - Progreso de estudiantes
  3. **Tabla de resultados**: Exportable a CSV/PDF

  ---

  ### 6.10 Mis Cursos (Estudiante)

  **Ruta**: `/mis-cursos`
  **Archivo**: `src/pages/MisCursosPage.tsx`
  **Rol**: estudiante

  **Componentes**:
  1. **Grid de cards por curso**:
    - Nombre, código, profesor
    - Progreso general (barra)
    - Próxima evaluación
    - Botón "Ver evaluaciones"

  ---

  ### 6.11 Mis Evaluaciones (Estudiante)

  **Ruta**: `/mis-evaluaciones`
  **Archivo**: `src/pages/MisEvaluacionesPage.tsx`
  **Rol**: estudiante

  **Componentes**:
  1. **Filtros**: Estado (Pendientes, Completadas), Curso
  2. **Tabla/Cards**: 
    - Nombre, curso, estado, calificación (si completada)
    - Botón "Realizar" si pendiente
    - Botón "Ver resultado" si completada

  ---

  ### 6.12 Historial (Estudiante)

  **Ruta**: `/historial`
  **Archivo**: `src/pages/HistorialPage.tsx`
  **Rol**: estudiante

  **Componentes**:
  1. **Timeline o tabla** de evaluaciones completadas
  2. **Estadísticas acumuladas**:
    - Promedio general
    - Tendencia de desempeño
  3. **Detalles por evaluación**: Calificación, fecha, feedback del profesor

  ---

  ### 6.13 Usuarios (Coordinador)

  **Ruta**: `/usuarios`
  **Archivo**: `src/pages/UsuariosPage.tsx`
  **Rol**: coordinador

  **Componentes**:
  1. **Tabla de usuarios**:
    - Nombre, email, rol, estado
    - Acciones: Ver, Editar, Desactivar, Eliminar
  2. **Formulario de nuevo usuario**: Agregar estudiantes/maestros
  3. **Filtros/búsqueda**: Por rol, por nombre

  ---

  ### 6.14 PQRS (Común)

  **Ruta**: `/pqrs`
  **Archivo**: `src/pages/PQRSPage.tsx`
  **Roles**: estudiante, maestro, coordinador (diferentes vistas)

  **Vistas**:

  **Estudiante**:
  - Crear nuevo ticket (Pregunta, Reclamo, Sugerencia, Queja)
  - Listar propios tickets
  - Ver respuestas de coordinadores/maestros

  **Maestro/Coordinador**:
  - Listar todas los PQRS del sistema
  - Filtrar por tipo, estado, curso
  - Responder ticket
  - Marcar como resuelto

  **Componentes**:
  1. **Formulario de creación**:
    - Campo tipo (selector)
    - Asunto, descripción, curso
  2. **Tabla de tickets**:
    - Columnas: ID, Tipo, Asunto, Estado, Fecha, Acciones
  3. **Modal/Panel de detalle**:
    - Descripción completa
    - Historial de respuestas (si coordinador)
    - Campo para responder

  ---

  ## 7. Componentes Reutilizables

  ### 7.1 Layout (`src/components/Layout.tsx`)

  **Responsabilidad**: Envuelve páginas protegidas, provee sidebar/navbar y breadcrumbs.

  **Props**:
  ```typescript
  interface LayoutProps {
    breadcrumbs?: Array<{ label: string; href?: string }>;
    children: React.ReactNode;
  }
  ```

  **Contenido**:
  - Sidebar con navegación según rol
  - Header con usuario, notificaciones, logout
  - Breadcrumbs
  - Área de contenido

  ---

  ### 7.2 Componentes Shadcn/ui Base

  Ubicados en `src/components/ui/`:

  - **button.tsx**: `<Button>` - estilos personalizados, variantes (primary, secondary, destructive, outline, ghost)
  - **card.tsx**: `<Card>`, `<CardHeader>`, `<CardTitle>`, `<CardDescription>`, `<CardContent>`
  - **dialog.tsx**: `<Dialog>`, `<DialogTrigger>`, `<DialogContent>` - modales
  - **tabs.tsx**: `<Tabs>`, `<TabsList>`, `<TabsTrigger>`, `<TabsContent>`
  - **input.tsx**: `<Input>` - campos de texto
  - **label.tsx**: `<Label>` - etiquetas de formularios
  - **select.tsx**: `<Select>` - dropdowns
  - **table.tsx**: `<Table>`, `<TableHeader>`, `<TableBody>`, `<TableRow>`, `<TableCell>`
  - **progress.tsx**: `<Progress>` - barras de progreso
  - **badge.tsx**: `<Badge>` - etiquetas / chips
  - **alert.tsx**: `<Alert>`, `<AlertTitle>`, `<AlertDescription>` - alertas
  - **dropdown-menu.tsx**: Menús contextuales
  - **form.tsx**: Integración con react-hook-form
  - **pagination.tsx**: Paginación
  - **scroll-area.tsx**: Áreas scrolleables customizadas
  - **slider.tsx**: Sliders de rango
  - **switch.tsx**: Toggles
  - Y más...

  ### 7.3 Componentes Personalizados

  #### StatCard (`src/components/StatCard.tsx`)
  Muestra una métrica con icono, número y contexto.

  ```typescript
  interface StatCardProps {
    stat: Estadistica;
  }
  ```

  #### DataTable (`src/components/DataTable.tsx`)
  Tabla reutilizable con sorting, filtering, pagination.

  #### LoadingSpinner (`src/components/LoadingSpinner.tsx`)
  Spinner circular con mensaje de carga.

  #### EmptyState (`src/components/EmptyState.tsx`)
  Muestra cuando no hay datos (icono + título + descripción).

  #### ErrorBoundary (`src/components/ErrorBoundary.tsx`)
  Captura errores en árbol de componentes.

  #### ProtectedRoute (`src/components/ProtectedRoute.tsx`)
  Valida autenticación y roles antes de renderizar.

  #### SearchInput (`src/components/SearchInput.tsx`)
  Input de búsqueda con debounce.

  #### StatusBadge (`src/components/StatusBadge.tsx`)
  Badge que colorea según estado (Activa, Cerrada, Pendiente, etc).

  #### ConfirmDialog (`src/components/ConfirmDialog.tsx`)
  Diálogo de confirmación para acciones destructivas.

  #### SkeletonLoader (`src/components/SkeletonLoader.tsx`)
  Skeletons para loading states.

  #### ImageWithFallback (`src/components/figma/ImageWithFallback.tsx`)
  Imagen con fallback SVG si falla carga.

  ---

  ## 8. Hooks Personalizados

  ### 8.1 useAuth (`src/hooks/` + contexto)
  ```typescript
  const { user, login, logout, isAuthenticated } = useAuth();
  ```

  ### 8.2 useCursos (`src/hooks/useCursos.ts`)
  ```typescript
  const { cursos, cursosActivos, getById, getByCodigo } = useCursos();
  ```

  **Funcionalidades**:
  - Retorna lista de cursos del mock
  - Métodos de búsqueda por ID y código

  ### 8.3 useEvaluaciones (`src/hooks/useEvaluaciones.ts`)
  ```typescript
  const { 
    evaluaciones, 
    evaluacionesAbiertas, 
    evaluacionesRecientes,
    getByEstado,
    getById,
    getByCurso 
  } = useEvaluaciones();
  ```

  ### 8.4 usePQRS (`src/hooks/usePQRS.ts`)
  ```typescript
  const { 
    tickets, 
    ticketsAbiertas,
    getByTipo,
    getByEstado,
    getById 
  } = usePQRS();
  ```

  ### 8.5 useLocalStorage (`src/hooks/useLocalStorage.ts`)
  Hook genérico para persistencia en localStorage.

  ```typescript
  const [value, setValue] = useLocalStorage<T>(key, defaultValue);
  ```

  ---

  ## 9. Servicios y Mock Data

  ### 9.1 mockData (`src/services/mockData.ts`)

  **Contiene**:
  - `mockEvaluaciones`: Array de 5 evaluaciones de ejemplo
  - `mockCursos`: Array de 4 cursos
  - `mockTicketsPQRS`: Array de 3 tickets PQRS
  - `estadisticasEstudiante`: Métricas para dashboard estudiante
  - `estadisticasMaestro`: Métricas para dashboard maestro
  - Funciones auxiliares: `getEvaluacionesByEstado()`, `getEvaluacionesAbiertas()`, etc.

  **Próximo paso**: Reemplazar con llamadas HTTP al backend.

  ---

  ## 10. Estilos y Tema

  ### 10.1 Tailwind CSS

  **Configuración**:
  - Tema extendido con colores personalizados
  - Tipografía: Fuentes San Francisco (sistema)
  - Spacing: Sistema de 4px base

  **Uso**: Classes utility en JSX (e.g., `className="flex items-center gap-4"`)

  ### 10.2 Globals.css (`src/styles/globals.css`)

  Estilos globales:
  - Variables CSS (colores primarios)
  - Resets de HTML
  - Utilidades personalizadas

  ### 10.3 Paleta de Colores

  ```
  Primario: Blue (600 para texto, 50 para fondo)
  Secundario: Indigo
  Éxito: Green
  Advertencia: Orange
  Error/Destructivo: Red
  Información: Blue
  Neutral: Gray (50-900)
  ```

  ---

  ## 11. Utilidades

  ### 11.1 date.ts (`src/utils/date.ts`)
  - `formatDate()`: Formatea fecha ISO a string legible
  - `formatDateTime()`: Formatea fecha+hora
  - Cálculo de tiempos relativos

  ### 11.2 format.ts (`src/utils/format.ts`)
  - Formateo de números, porcentajes
  - Formateo moneda (para calificaciones)

  ### 11.3 validation.ts (`src/utils/validation.ts`)
  - `isValidEmail()`: Valida formato email
  - `isValidPassword()`: Reglas de contraseña
  - Validadores personalizados

  ### 11.4 debounce.ts (`src/utils/debounce.ts`)
  Función debounce para búsquedas con retraso.

  ---

  ## 12. Configuración del Proyecto

  ### 12.1 main.tsx
  Punto de entrada de React. Renderiza `<App />` en DOM.

  ### 12.2 vite.config.ts
  Configuración de Vite:
  - Plugin React
  - Rutas alias (e.g., `@` → `src`)

  ### 12.3 tsconfig.json
  Configuración de TypeScript (target ES2020, módulos ESM).

  ### 12.4 package.json

  **Scripts principales**:
  ```
  npm i              # Instalar dependencias
  npm run dev        # Dev server (HMR)
  npm run build      # Build para producción
  npm run preview    # Previsualizar build
  npm run lint       # (si está configurado)
  ```

  **Dependencias clave**:
  - `react` y `react-dom` (v18)
  - `react-router-dom` (v6)
  - `shadcn/ui` (componentes base)
  - `radix-ui/*` (primitivas accesibles)
  - `tailwindcss` (estilos)
  - `lucide-react` (iconos)
  - `sonner` (notificaciones toast)
  - `react-hook-form` (formularios)
  - `zod` (validación schemas)

  ---

  ## 13. Características Clave del Frontend

  ### 13.1 Responsividad
  - Mobile-first design
  - Breakpoints: sm (640px), md (768px), lg (1024px), xl, 2xl
  - Componentes adaptan layout según pantalla

  ### 13.2 Accesibilidad
  - ARIA labels, roles semánticos
  - Color no es único medio de información
  - Teclado navegable
  - Contraste suficiente

  ### 13.3 Performance
  - Lazy loading de páginas
  - Code splitting con React.lazy()
  - Suspense + loading states
  - Memoización de costos (useMemo en hooks)

  ### 13.4 UX & Diseño
  - Principios Gestalt: proximidad, continuidad, cierre
  - Feedback visual (loading, toasts, transiciones)
  - Consistencia de componentes
  - Micro-interacciones (hover, focus)

  ### 13.5 Seguridad (Protección Básica)
  - ProtectedRoute valida autenticación
  - Roles restringidos por ruta
  - localStorage para sesión (en producción: usar httpOnly cookies)
  - **IMPORTANTE**: No confiar en frontend para seguridad crítica

  ---

  ## 14. Diferencias Rol por Rol

  | Característica | Estudiante | Maestro | Coordinador |
  |---|---|---|---|
  | **Dashboard** | Evaluaciones pendientes, progreso de cursos | Evaluaciones a calificar, estudiantes | Visión global del sistema |
  | **Acceso a** | Mis cursos, Mis evaluaciones, Historial | Evaluaciones, Crear eval, Calificar, Reportes | Usuarios, Reportes, PQRS |
  | **Crear contenido** | Respuestas en evaluaciones | Evaluaciones | Usuarios |
  | **Calificar** | ✗ | ✓ | (solo PQRS) |
  | **Ver reportes** | ✗ | ✓ | ✓ |
  | **Gestionar usuarios** | ✗ | ✗ | ✓ |

  ---

  ## 15. Flujos Principales de Usuario

  ### 15.1 Flujo Estudiante

  1. Llega a `/` (login)
  2. Selecciona rol "Estudiante"
  3. Ingresa email/contraseña (opcionales)
  4. Click "Iniciar Sesión"
  5. Redirige a `/dashboard` (DashboardEstudiante)
  6. Ve evaluaciones abiertas y cursos
  7. Click en "Realizar Evaluación"
  8. Navega `/realizar-evaluacion` con timer y preguntas
  9. Envía respuestas → POST `/api/evaluaciones/{id}/submit`
  10. Ve resultado y puede acceder a Historial

  ### 15.2 Flujo Maestro

  1. Login como "Maestro"
  2. Redirige a `/dashboard` (DashboardMaestro)
  3. Ve evaluaciones activas, por calificar, estudiantes
  4. Click "Crear Evaluación" → `/evaluaciones/nueva`
  5. Crea evaluación (metadatos + preguntas)
  6. Publica → POST `/api/evaluaciones` (crear)
  7. Estuaiantes responden
  8. Maestro va a `/calificar`
  9. Selecciona evaluación + estudiante
  10. Califica cada pregunta
  11. Envía calificaciones → POST `/api/calificaciones`
  12. Ve reportes en `/reportes`

  ### 15.3 Flujo PQRS (Común)

  **Estudiante**:
  1. Click `/pqrs` en sidebar
  2. Click "Crear Ticket"
  3. Selecciona tipo (Pregunta, Reclamo, etc)
  4. Ingresa asunto + descripción + curso
  5. Submit →e POST `/api/pqrs`
  6. Ve ticket en lista con estado "Pendiente"
  7. Espera respuesta

  **Coordinador**:
  1. Click `/pqrs`
  2. Ve lista de todos los tickets (filtrable)
  3. Click en ticket → abre detalle
  4. Click "Responder"
  5. Ingresa respuesta
  6. Submit → POST `/api/pqrs/{id}/respond`
  7. Marca como "Resuelto"

  ---

  ## 16. Validaciones Actuales

  ### 16.1 Frontend Validation (Login)

  - Email: Formato `usuario@dominio.ext`
  - Campos opcionales: Si vacíos, envía valores default
  - Contraseña: Sin validación especial (prototipo)

  ### 16.2 Backend Validation (Por Implementar)

  - Email: Válido + existe en DB
  - Contraseña: Mínimo 8 caracteres, complejidad
  - Evaluaciones: Deadline en futuro, preguntas > 0
  - Calificaciones: Puntaje en rango [0, max]

  ---

  ## 17. Pendientes / TODO para Integrar Backend

  ### 17.1 AuthContext Cambios

  ```typescript
  // Antes:
  const mockUser = { id, name, email, role };
  setUser(mockUser);

  // Después:
  const response = await fetch('/api/auth/login', { ... });
  const { user, token } = await response.json();
  setUser(user);
  setToken(token);
  localStorage.setItem('token', token);
  ```

  ### 17.2 Hooks de Datos

  Reemplazar mockData con fetch:

  ```typescript
  // Antes:
  const [cursos] = useState(mockCursos);

  // Después:
  const [cursos, setCursos] = useState([]);
  useEffect(() => {
    fetch('/api/cursos', {
      headers: { 'Authorization': `Bearer ${token}` }
    })
      .then(r => r.json())
      .then(data => setCursos(data));
  }, [token]);
  ```

  ### 17.3 Llamadas POST/PUT/DELETE

  Paginas de formularios necesitan:
  - `POST /api/evaluaciones` (crear)
  - `PUT /api/evaluaciones/{id}` (editar)
  - `DELETE /api/evaluaciones/{id}` (eliminar)
  - `POST /api/evaluaciones/{id}/submit` (responder estudiante)
  - `POST /api/calificaciones` (calificar)
  - `POST /api/pqrs` (crear ticket)
  - `POST /api/pqrs/{id}/respond` (responder ticket)
  - `GET /api/usuarios`, `POST /api/usuarios`, etc.

  ### 17.4 Interceptores HTTP

  Crear interceptor para:
  - Añadir `Authorization: Bearer <token>` a cada request
  - Refrescar token si expira
  - Manejar 401 → logout

  ### 17.5 Manejo de Errores

  Mejorar feedback de errores:
  - Mostrar error específico del backend
  - Reintentar automáticamente en timeouts
  - Logging de errors para debugging

  ### 17.6 Testing

  Añadir pruebas:
  - Unitarias (hooks, utils)
  - Integración (rutas, flujos)
  - E2E (Cypress/Playwright)

  ---

  ## 18. Notas Importantes

  ### 18.1 Convenciones de Código

  - **Componentes**: PascalCase, export nombrado
  - **Hooks**: camelCase, prefijo `use`
  - **Tipos**: PascalCase, interfaz cuando es contracto
  - **Constantes**: UPPER_SNAKE_CASE
  - **CSS**: Tailwind utility classes, evitar CSS custom

  ### 18.2 Flujo de Props

  Props descienden de padre a hijo. Contexto (`AuthContext`) para gran profundidad.

  ### 18.3 Estructura de Carpetas

  Escalable: cada "feature" (ej: Evaluaciones) puede vivir en carpeta dedicada si crece.

  ### 18.4 Deploy

  - Build: `npm run build` → carpeta `dist`
  - Servir estático en servidor web (nginx, Vercel, etc)
  - Usar variables de entorno para API baseURL

  ---

  ## 19. Resumen Visual - Arquitectura

  ```
  ┌─────────────────────────────────────────────────────────────┐
  │                        App.tsx                              │
  │  (ErrorBoundary + BrowserRouter + AuthProvider + Routes)    │
  └──────────────────────┬──────────────────────────────────────┘
                        │
            ┌───────────┼───────────┐
            ▼           ▼           ▼
          LoginPage   ProtectedRoute Dashboard(s)
                        │              │
                        └──────┬───────┘
                              │
                      ┌────────┴────────┐
                      ▼                 ▼
                    Pages         Layout + Sidebar
                      │                 │
          ┌───────────┼───────────┐     │
          ▼           ▼           ▼     │
        useCursos useEvaluaciones usePQRS
          │           │           │
          └───────────┴───────────┘
                      │
              mockData (soon: APIs)
                      │
                      ▼
                UI Components (Shadcn + Custom)
                      │
                      ▼
                Browser DOM
  ```

  ---

  ## 20. Conclusión

  QuimbayaEVAL es un **frontend moderno, escalable y accesible** para gestionar evaluaciones académicas. Utiliza tecnologías estándar (React, TypeScript, Tailwind) y patrones probados (Context API, hooks, componentes). 

  **Próximo paso**: Integrar con backend Java/Spring Boot que implemente los endpoints REST especificados para reemplazar mock data y autenticación real.

  ---

  **Documento generado**: Febrero 26, 2026
  **Estado**: Completo (prototipo, mock data)
  **Prioridad integración backend**: Alta - bloqueada en auth + llamadas API

    