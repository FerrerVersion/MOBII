import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../shared/auth';

@Component({
  selector: 'app-login',
  standalone: false,
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login {
  form: FormGroup;
  error = '';
  loading = false;

  constructor(private fb: FormBuilder, private auth: AuthService, private router: Router) {
    this.form = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required],
    });
    if (this.auth.isLoggedIn() && this.auth.isAdmin()) {
      this.router.navigate(['/admin/usuarios']);
    }
  }

  submit(): void {
    if (this.form.invalid) return;
    this.loading = true;
    this.error = '';
    const { email, password } = this.form.value;
    this.auth.login(email, password).subscribe({
      next: res => {
        if (res.usuario.rol !== 'admin') {
          this.auth.logout();
          this.error = 'Acceso restringido a administradores';
          this.loading = false;
          return;
        }
        this.router.navigate(['/admin/usuarios']);
      },
      error: err => {
        this.error = err.error?.error || 'Error al iniciar sesión';
        this.loading = false;
      },
    });
  }
}
