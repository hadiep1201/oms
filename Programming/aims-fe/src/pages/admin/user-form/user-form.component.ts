import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { UserService, CreateUserRequest, UpdateUserRequest, UserResponse } from '../../../services/user.service';

@Component({
  selector: 'app-user-form',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './user-form.component.html',
  styleUrls: ['./user-form.component.css'],})
export class UserFormComponent implements OnInit {
  isEditMode = false;
  userId!: number;
  errorMessage = '';
  
  formData: any = {
    userName: '',
    email: '',
    password: '',
    avatarUrl: ''
  };

  constructor(
    private userService: UserService,
    private route: ActivatedRoute,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode = true;
      this.userId = +id;
      this.loadUser();
    }
  }

  loadUser(): void {
    this.userService.getUserById(this.userId).subscribe({
      next: (user) => {
        this.formData.userName = user.userName;
        this.formData.email = user.email;
        this.formData.avatarUrl = user.avatarUrl;
        this.cdr.detectChanges();
      },
      error: (err) => {
        alert('Failed to load user details');
        this.router.navigate(['/admin/users']);
      }
    });
  }

  onSubmit(): void {
    this.errorMessage = '';
    if (this.isEditMode) {
      const request: UpdateUserRequest = {
        userName: this.formData.userName,
        email: this.formData.email,
        avatarUrl: this.formData.avatarUrl
      };
      this.userService.updateUser(this.userId, request).subscribe({
        next: () => this.router.navigate(['/admin/users']),
        error: (err) => {
          this.errorMessage = err.error?.message || 'Failed to update user';
          this.cdr.detectChanges();
        }
      });
    } else {
      const request: CreateUserRequest = {
        userName: this.formData.userName,
        email: this.formData.email,
        password: this.formData.password,
        avatarUrl: this.formData.avatarUrl
      };
      this.userService.createUser(request).subscribe({
        next: () => this.router.navigate(['/admin/users']),
        error: (err) => {
          this.errorMessage = err.error?.message || 'Failed to create user';
          this.cdr.detectChanges();
        }
      });
    }
  }
}
