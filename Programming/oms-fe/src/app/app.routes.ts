import { Routes } from '@angular/router';
import { adminGuard } from '../guards/admin.guard';
import { adminOnlyRedirectGuard } from '../guards/admin-only-redirect.guard';
import { authGuard } from '../guards/auth.guard';
import { HomeComponent } from '../pages/home/home.component';

export const routes: Routes = [
  {
    path: '',
    component: HomeComponent,
    pathMatch: 'full',
    canActivate: [adminOnlyRedirectGuard],
  },
  { path: 'home', redirectTo: '', pathMatch: 'full' },
  {
    path: 'admin',
    canActivate: [adminGuard],
    children: [
      { path: '', redirectTo: 'users', pathMatch: 'full' },
      { path: 'users', loadComponent: () => import('../pages/admin/user-list/user-list.component').then(c => c.UserListComponent) },
      { path: 'users/new', loadComponent: () => import('../pages/admin/user-form/user-form.component').then(c => c.UserFormComponent) },
      { path: 'users/edit/:id', loadComponent: () => import('../pages/admin/user-form/user-form.component').then(c => c.UserFormComponent) },
      { path: 'users/view/:id', loadComponent: () => import('../pages/admin/user-detail/user-detail.component').then(c => c.UserDetailComponent) }
    ]
  },
  { path: 'login', loadComponent: () => import('../pages/login/login.component').then(c => c.LoginComponent) },
  {
    path: 'search',
    loadComponent: () => import('../pages/search-product/search-product.component').then(c => c.SearchProductComponent),
    canActivate: [adminOnlyRedirectGuard],
  },
  {
    path: 'cart',
    loadComponent: () => import('../pages/cart/cart.component').then(c => c.CartComponent),
    canActivate: [adminOnlyRedirectGuard],
  },
  {
    path: 'delivery',
    loadComponent: () => import('../pages/delivery/delivery.component').then(c => c.DeliveryComponent),
    canActivate: [adminOnlyRedirectGuard],
  },
  {
    path: 'payment',
    loadComponent: () => import('../pages/payment/payment.component').then(c => c.PaymentComponent),
    canActivate: [adminOnlyRedirectGuard],
  },
  {
    path: 'order-success',
    loadComponent: () => import('../pages/order-success/order-success.component').then(c => c.OrderSuccessComponent),
    canActivate: [adminOnlyRedirectGuard],
  },
  {
    path: 'product/:id',
    loadComponent: () => import('../pages/product-detail/product-detail.component').then(c => c.ProductDetailComponent),
    canActivate: [adminOnlyRedirectGuard],
  },
  {
    path: 'manager',
    canActivate: [authGuard],
    loadComponent: () =>
      import('../pages/manager/manager-setting.component').then(c => c.ManagerSettingComponent),
    children: [
      { path: '', loadComponent: () => import('../pages/manager/manager-overview.component').then(c => c.ManagerOverviewComponent) },
      {
        path: 'product',
        loadComponent: () =>
          import('../pages/manager/manager-product-list.component').then(c => c.ManagerProductListComponent),
      },
      {
        path: 'order',
        loadComponent: () =>
          import('../pages/manager/manager-order-list.component').then(c => c.ManagerOrderListComponent),
      },
      { path: 'history', loadComponent: () => import('../pages/manager/manager-overview.component').then(c => c.ManagerOverviewComponent) },
    ],
  },
  { path: '**', redirectTo: '' },
];
