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

  submitKotlinClaim(): void {
    this.call(
      this.http.post('/kotlin/claims', {
        policyId: 'POL-001',
        description: 'Demo claim',
        estimatedAmount: 1500,
      })
    );
  }

  getPythonPolicies(): void {
    this.call(this.http.get('/python/policies'));
  }

  submitPythonClaim(): void {
    this.call(
      this.http.post('/python/claims', {
        policyId: 'POL-002',
        estimatedAmount: 2200,
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
