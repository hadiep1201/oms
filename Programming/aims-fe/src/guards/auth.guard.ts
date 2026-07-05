import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const authGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!authService.isAuthenticated()) {
    router.navigate(['/login']);
    return false;
  }

  if (!authService.canAccessManager()) {
    if (authService.hasAnyRole(['ADMIN'])) {
      router.navigate(['/admin']);
    } else {
      router.navigate(['/login']);
    }
    return false;
  }

  return true;
};
