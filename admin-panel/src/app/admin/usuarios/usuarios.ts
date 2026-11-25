import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { UsuariosService, Usuario, ParkingFavorito, Reserva } from '../../shared/usuarios';
import { AuthService } from '../../shared/auth';

@Component({
  selector: 'app-usuarios',
  standalone: false,
  templateUrl: './usuarios.html',
  styleUrl: './usuarios.css',
})
export class Usuarios implements OnInit {
  usuarios: Usuario[] = [];
  filtro = '';
  loading = false;
  error = '';
  exito = '';

  // Modal editar/crear
  mostrarModal = false;
  modoEdicion = false;
  usuarioSeleccionado: Usuario | null = null;
  form: FormGroup;

  // Modal detalle
  mostrarDetalle = false;
  usuarioDetalle: Usuario | null = null;
  favoritos: ParkingFavorito[] = [];
  reservas: Reserva[] = [];
  loadingDetalle = false;

  constructor(
    private svc: UsuariosService,
    public auth: AuthService,
    private fb: FormBuilder
  ) {
    this.form = this.fb.group({
      nombre: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      password: [''],
      rol: ['usuario', Validators.required],
      activo: [true],
    });
  }

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.loading = true;
    this.svc.getAll().subscribe({
      next: data => { this.usuarios = data; this.loading = false; },
      error: () => { this.error = 'Error al cargar usuarios'; this.loading = false; },
    });
  }

  get usuariosFiltrados(): Usuario[] {
    const f = this.filtro.toLowerCase();
    return this.usuarios.filter(u =>
      u.nombre.toLowerCase().includes(f) ||
      u.email.toLowerCase().includes(f) ||
      u.rol.toLowerCase().includes(f)
    );
  }

  abrirDetalle(u: Usuario): void {
    this.usuarioDetalle = u;
    this.favoritos = [];
    this.reservas = [];
    this.mostrarDetalle = true;
    this.loadingDetalle = true;

    forkJoin({
      favoritos: this.svc.getFavoritos(u.id).pipe(catchError(() => of([] as ParkingFavorito[]))),
      reservas:  this.svc.getReservas(u.id).pipe(catchError(() => of([] as Reserva[]))),
    }).subscribe(({ favoritos, reservas }) => {
      this.favoritos = favoritos;
      this.reservas  = reservas;
      this.loadingDetalle = false;
    });
  }

  cerrarDetalle(): void {
    this.mostrarDetalle = false;
    this.usuarioDetalle = null;
  }

  abrirCrear(): void {
    this.modoEdicion = false;
    this.usuarioSeleccionado = null;
    this.form.reset({ rol: 'usuario', activo: true });
    this.form.get('password')!.setValidators([Validators.required, Validators.minLength(6)]);
    this.form.get('password')!.updateValueAndValidity();
    this.mostrarModal = true;
  }

  abrirEditar(u: Usuario): void {
    this.modoEdicion = true;
    this.usuarioSeleccionado = u;
    this.form.patchValue({ nombre: u.nombre, email: u.email, rol: u.rol, activo: u.activo === 1, password: '' });
    this.form.get('password')!.clearValidators();
    this.form.get('password')!.updateValueAndValidity();
    this.mostrarModal = true;
  }

  cerrarModal(): void {
    this.mostrarModal = false;
    this.error = '';
  }

  guardar(): void {
    if (this.form.invalid) return;
    const datos: any = { ...this.form.value };
    if (!datos.password) delete datos.password;

    const op = this.modoEdicion && this.usuarioSeleccionado
      ? this.svc.update(this.usuarioSeleccionado.id, datos)
      : this.svc.create(datos);

    op.subscribe({
      next: () => {
        this.exito = this.modoEdicion ? 'Usuario actualizado' : 'Usuario creado';
        this.cerrarModal();
        this.cargar();
        setTimeout(() => this.exito = '', 3000);
      },
      error: err => { this.error = err.error?.error || 'Error al guardar'; },
    });
  }

  eliminar(u: Usuario): void {
    if (!confirm(`¿Eliminar a ${u.nombre}?`)) return;
    this.svc.delete(u.id).subscribe({
      next: () => {
        this.exito = 'Usuario eliminado';
        this.cargar();
        setTimeout(() => this.exito = '', 3000);
      },
      error: err => { this.error = err.error?.error || 'Error al eliminar'; },
    });
  }
}
