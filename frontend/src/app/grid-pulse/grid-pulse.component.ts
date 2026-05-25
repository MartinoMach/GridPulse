import { CommonModule, DatePipe, DecimalPipe } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { interval, Subject, switchMap, takeUntil, timer } from 'rxjs';
import {
  AccuracyResponse,
  AlertResponse,
  GridPulseApiService
} from '../services/grid-pulse-api.service';

@Component({
  selector: 'gp-root',
  standalone: true,
  imports: [CommonModule, DatePipe, DecimalPipe],
  templateUrl: './grid-pulse.component.html',
  styleUrl: './grid-pulse.component.css'
})
export class GridPulseComponent implements OnInit, OnDestroy {
  alerts: AlertResponse[] = [];
  accuracy: AccuracyResponse | null = null;
  lastRefresh = new Date();
  private readonly destroyed$ = new Subject<void>();

  constructor(private readonly api: GridPulseApiService) {}

  ngOnInit(): void {
    timer(0, 30_000)
      .pipe(
        switchMap(() => this.api.getAlerts()),
        takeUntil(this.destroyed$)
      )
      .subscribe((alerts) => {
        this.alerts = alerts;
        this.lastRefresh = new Date();
      });

    interval(30_000)
      .pipe(
        switchMap(() => this.api.getAccuracy()),
        takeUntil(this.destroyed$)
      )
      .subscribe((accuracy) => (this.accuracy = accuracy));

    this.api.getAccuracy().subscribe((accuracy) => (this.accuracy = accuracy));
  }

  ngOnDestroy(): void {
    this.destroyed$.next();
    this.destroyed$.complete();
  }

  rowClass(alert: AlertResponse): string {
    return `risk-${alert.severity}`;
  }

  probabilityPercent(value: number): number {
    return value * 100;
  }
}
