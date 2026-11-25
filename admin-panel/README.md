# MOBII — Panel de Administración

Panel web de administración para la plataforma MOBII. Desarrollado en Angular 21.

## Descripción

Interfaz web para que el administrador gestione las cuentas de usuario registradas en la app Android. Permite ver estadísticas básicas, crear, editar y eliminar usuarios, y consultar el historial de favoritos y reservas de cada usuario.

## Requisitos

- Node.js 20+
- Angular CLI (`npm install -g @angular/cli`)
- Backend MOBII corriendo en `http://localhost:3000`

## Instalación

```bash
npm install
```

## Desarrollo

```bash
ng serve
```

Abre `http://localhost:4200` en el navegador. El servidor recarga automáticamente al guardar cambios.

## Build de producción

```bash
ng build
```

Los archivos compilados quedan en `dist/admin-panel/`.

## Acceso

Credenciales por defecto (creadas automáticamente por el backend):

- **Email:** `admin@mobii.com`
- **Contraseña:** `admin123`

Solo los usuarios con `rol: admin` pueden acceder al panel.

## Estructura

```
src/app/
├── auth/
│   └── login/          pantalla de inicio de sesión
├── admin/
│   ├── dashboard/      estadísticas generales
│   └── usuarios/       CRUD de usuarios con modal de detalle
└── shared/
    ├── auth.ts         servicio de autenticación (JWT en localStorage)
    ├── usuarios.ts     servicio HTTP para la API de usuarios
    └── auth-guard.ts   guard que protege las rutas de admin
```
