import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';

import { AdminRoutingModule } from './admin-routing-module';
import { Dashboard } from './dashboard/dashboard';
import { Usuarios } from './usuarios/usuarios';

@NgModule({
  declarations: [Dashboard, Usuarios],
  imports: [CommonModule, ReactiveFormsModule, FormsModule, AdminRoutingModule],
})
export class AdminModule {}
