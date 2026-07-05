import { Component, output, signal } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { NzIconModule } from 'ng-zorro-antd/icon';

export interface ManagerNavItem {
  label: string;
  route: string;
  icon: string;
}

@Component({
  selector: 'app-manager-sidebar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, NzIconModule],
  templateUrl: './manager-sidebar.component.html',
  styleUrls: ['./manager-sidebar.component.css'],
})
export class ManagerSidebarComponent {
  collapsedChange = output<boolean>();

  collapsed = signal(false);

  navItems: ManagerNavItem[] = [
    { label: 'Overview', route: '/manager', icon: 'home' },
    { label: 'Product', route: '/manager/product', icon: 'appstore' },
    { label: 'Order', route: '/manager/order', icon: 'audit' },
    { label: 'History', route: '/manager/history', icon: 'history' },
  ];

  toggle(): void {
    this.collapsed.update((v) => !v);
    this.collapsedChange.emit(this.collapsed());
  }
}
