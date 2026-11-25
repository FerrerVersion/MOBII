import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth';

export interface Usuario {
  id: number;
  nombre: string;
  email: string;
  rol: string;
  activo: number;
  discapacidad?: string;
  created_at: string;
}

export interface ParkingFavorito {
  id: number;
  record_id: string;
  direccion: string;
  created_at: string;
}

export interface Reserva {
  id: number;
  nombre: string;
  recogida: string;
  destino: string;
  fecha: string;
  hora: string;
  plazas: number;
  telefono: string;
  notas: string;
  created_at: string;
}

@Injectable({ providedIn: 'root' })
export class UsuariosService {
  private readonly API = 'http://localhost:3000/api/usuarios';

  constructor(private http: HttpClient, private auth: AuthService) {}

  private headers(): HttpHeaders {
    return new HttpHeaders({ Authorization: `Bearer ${this.auth.getToken()}` });
  }

  getAll(): Observable<Usuario[]> {
    return this.http.get<Usuario[]>(this.API, { headers: this.headers() });
  }

  getFavoritos(id: number): Observable<ParkingFavorito[]> {
    return this.http.get<ParkingFavorito[]>(`${this.API}/${id}/favoritos`, { headers: this.headers() });
  }

  getReservas(id: number): Observable<Reserva[]> {
    return this.http.get<Reserva[]>(`${this.API}/${id}/reservas`, { headers: this.headers() });
  }

  create(data: Partial<Usuario> & { password: string }): Observable<Usuario> {
    return this.http.post<Usuario>(this.API, data, { headers: this.headers() });
  }

  update(id: number, data: Partial<Usuario> & { password?: string }): Observable<Usuario> {
    return this.http.put<Usuario>(`${this.API}/${id}`, data, { headers: this.headers() });
  }

  delete(id: number): Observable<any> {
    return this.http.delete(`${this.API}/${id}`, { headers: this.headers() });
  }
}
