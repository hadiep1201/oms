import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { ManagerSidebarComponent } from '../../components/manager-sidebar/manager-sidebar.component';

@Component({
  selector: 'app-manager-setting',
  standalone: true,
  imports: [ManagerSidebarComponent, RouterOutlet],
  templateUrl: './manager-setting.component.html',
  styleUrls: ['./manager-setting.component.css'],
})
export class ManagerSettingComponent {}
