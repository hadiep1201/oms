import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { UserService, UserResponse } from '../../../services/user.service';

@Component({
  selector: 'app-user-list',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './user-list.component.html',
  styleUrls: ['./user-list.component.css'],})
export class UserListComponent implements OnInit {
  users: UserResponse[] = [];

  constructor(private userService: UserService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    console.log('UserListComponent initialized');
    this.loadUsers();
  }

  loadUsers(): void {
    console.log('Fetching users from API...');
    this.userService.getAllUsers().subscribe({
      next: (data) => {
        console.log('Users loaded successfully:', data);
        this.users = data;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Failed to load users', err);
      }
    });
  }

  deactivateUser(id: number): void {
    if (confirm('Are you sure you want to deactivate this user?')) {
      this.userService.deactivateUser(id).subscribe({
        next: () => {
          this.loadUsers();
        },
        error: (err) => alert('Failed to deactivate user')
      });
    }
  }
}
