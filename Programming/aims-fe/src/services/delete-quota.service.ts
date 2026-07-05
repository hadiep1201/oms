import { Injectable } from '@angular/core';

const DAILY_DELETE_KEY_PREFIX = 'aims-daily-delete-count-';

/**
 * SRP: daily delete quota persistence only.
 * Coupling: common coupling with localStorage.
 * Cohesion: functional cohesion.
 */
@Injectable({ providedIn: 'root' })
export class DeleteQuotaService {
  getDailyDeleteCount(): number {
    const raw = localStorage.getItem(this.getDailyDeleteKey());
    return raw ? Number.parseInt(raw, 10) || 0 : 0;
  }

  incrementDailyDeleteCount(delta: number): void {
    if (delta <= 0) {
      return;
    }
    const next = this.getDailyDeleteCount() + delta;
    localStorage.setItem(this.getDailyDeleteKey(), String(next));
  }

  private getDailyDeleteKey(): string {
    return `${DAILY_DELETE_KEY_PREFIX}${new Date().toISOString().slice(0, 10)}`;
  }
}
