import { Component, OnInit } from '@angular/core';
import { UsuariosService, Usuario } from '../../shared/usuarios';
import { AuthService } from '../../shared/auth';

@Component({
  selector: 'app-dashboard',
  standalone: false,
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class Dashboard implements OnInit {
  total = 0;
  admins = 0;
  activos = 0;
  ultimosUsuarios: Usuario[] = [];

  constructor(private svc: UsuariosService, public auth: AuthService) {}

  ngOnInit(): void {
    this.svc.getAll().subscribe(lista => {
      this.total = lista.length;
      this.admins = lista.filter(u => u.rol === 'admin').length;
      this.activos = lista.filter(u => u.activo === 1).length;
      this.ultimosUsuarios = lista.slice(-5).reverse();
    });
  }
}
