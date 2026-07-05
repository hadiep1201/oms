import { Component, computed, inject, signal } from '@angular/core';
import { NavigationEnd, Router, RouterLink } from '@angular/router';
import { filter } from 'rxjs';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [RouterLink, NzIconModule],
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css'],
})
export class HeaderComponent {
  private router = inject(Router);
  private authService = inject(AuthService);
  isLoginPage = signal(this.router.url.includes('/login'));
  isManagerPage = signal(this.router.url.startsWith('/manager'));
  isSearchPage = signal(this.router.url.includes('/search') && !this.router.url.includes('fromCategory=true'));
  searchQuery = signal<string>('');

  isManagerLoggedIn = computed(() => {
    this.authService.session();
    return this.authService.isAuthenticated() && this.authService.canAccessManager();
  });

  isAdminSession = computed(() => {
    this.authService.session();
    return this.authService.isAuthenticated() && this.authService.isAdminOnly();
  });

  managerLabel = computed(() => this.authService.session()?.userName ?? 'Người dùng');

  constructor() {
    this.router.events
      .pipe(filter((e): e is NavigationEnd => e instanceof NavigationEnd))
      .subscribe(() => {
        const url = this.router.url;
        this.isLoginPage.set(url.includes('/login'));
        this.isManagerPage.set(url.startsWith('/manager'));
        this.isSearchPage.set(url.includes('/search') && !url.includes('fromCategory=true'));

        const urlTree = this.router.parseUrl(url);
        const keyword = urlTree.queryParams['keyword'] || '';
        this.searchQuery.set(keyword);
      });
  }

  goToSearchPage() {
    this.router.navigate(['/search']);
  }

  onSearch(value: string) {
    this.router.navigate(['/search'], { queryParams: { keyword: value } });
  }

  onLogout(): void {
    this.authService.logout();
  }
}
