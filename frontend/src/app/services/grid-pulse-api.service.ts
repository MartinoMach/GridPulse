import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export interface AlertResponse {
  assetId: string;
  failureProbability: number;
  estimatedTimeToFailure: string;
  severity: 'critical' | 'warning' | 'healthy';
}

export interface AccuracyResponse {
  totalPredictionsMade: number;
  correctPredictions: number;
  falsePositives: number;
  falseNegatives: number;
  overallAccuracyPercent: number;
}

@Injectable({ providedIn: 'root' })
export class GridPulseApiService {
  constructor(private readonly http: HttpClient) {}

  getAlerts(): Observable<AlertResponse[]> {
    return this.http.get<AlertResponse[]>('/api/alerts');
  }

  getAccuracy(): Observable<AccuracyResponse> {
    return this.http.get<AccuracyResponse>('/api/accuracy');
  }
}
