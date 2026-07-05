import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, NzIconModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css'],})
export class LoginComponent {
  private fb = inject(FormBuilder);
  private router = inject(Router);
  private authService = inject(AuthService);

  loading = signal(false);
  errorMessage = signal('');

  form = this.fb.nonNullable.group({
    username: ['', Validators.required],
    password: ['', Validators.required]
  });

  onSubmit(): void {
    if (this.form.invalid) {
      return;
    }

    this.loading.set(true);
    this.errorMessage.set('');

    const { username, password } = this.form.getRawValue();
    this.authService.login({ username, password }).subscribe({
      next: () => {
        this.loading.set(false);
        if (this.authService.canAccessManager()) {
          void this.router.navigateByUrl('/manager', { replaceUrl: true });
          return;
        }
        if (this.authService.isAdminOnly()) {
          void this.router.navigateByUrl('/admin', { replaceUrl: true });
          return;
        }
        this.errorMessage.set('Bạn không có quyền truy cập hệ thống.');
        this.authService.clearLocalSession();
      },
      error: (err: { error?: { message?: string }; message?: string }) => {
        this.loading.set(false);
        this.errorMessage.set(
          err?.error?.message ?? err?.message ?? 'Đăng nhập thất bại. Vui lòng kiểm tra lại tài khoản và mật khẩu.',
        );
      },
    });
  }
}
