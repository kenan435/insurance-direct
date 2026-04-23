import { Component } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './app.component.html',
})
export class AppComponent {
  response: unknown = null;
  loading = false;
  error: string | null = null;

  constructor(private http: HttpClient) {}

  getKotlinPolicies(): void {
    this.call(this.http.get('/kotlin/policies'));
  }

  getKotlinPolicy(id: string): void {
    this.call(this.http.get(`/kotlin/policies/${id}`));
  }

  submitKotlinClaim(): void {
    this.call(
      this.http.post('/kotlin/claims', {
        policyId: 'POL-001',
        description: 'Demo claim',
        estimatedAmount: 1500,
      })
    );
  }

  submitKotlinClaimOverLimit(): void {
    this.call(
      this.http.post('/kotlin/claims', {
        policyId: 'POL-001',
        description: 'Large claim over policy limit',
        estimatedAmount: 25000,
      })
    );
  }

  submitKotlinClaimExpired(): void {
    this.call(
      this.http.post('/kotlin/claims', {
        policyId: 'POL-004',
        description: 'Claim on expired policy',
        estimatedAmount: 800,
      })
    );
  }

  getPythonPolicies(): void {
    this.call(this.http.get('/python/policies'));
  }

  getPythonPolicy(id: string): void {
    this.call(this.http.get(`/python/policies/${id}`));
  }

  submitPythonClaim(): void {
    this.call(
      this.http.post('/python/claims', {
        policyId: 'POL-002',
        estimatedAmount: 2200,
      })
    );
  }

  submitPythonClaimOverLimit(): void {
    this.call(
      this.http.post('/python/claims', {
        policyId: 'POL-002',
        estimatedAmount: 25000,
      })
    );
  }

  submitPythonClaimExpired(): void {
    this.call(
      this.http.post('/python/claims', {
        policyId: 'POL-004',
        estimatedAmount: 800,
      })
    );
  }

  private call(req: ReturnType<HttpClient['get']>): void {
    this.loading = true;
    this.response = null;
    this.error = null;
    req.subscribe({
      next: (data) => {
        this.response = data;
        this.loading = false;
      },
      error: (err: { message: string }) => {
        this.error = err.message;
        this.loading = false;
      },
    });
  }
}
