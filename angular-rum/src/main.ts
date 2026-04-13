import { bootstrapApplication } from '@angular/platform-browser';
import { CoralogixRum } from '@coralogix/browser';
import { AppComponent } from './app/app.component';
import { appConfig } from './app/app.config';

CoralogixRum.init({
  application: 'insurance-direct',
  environment: 'production',
  public_key: '<your-rum-public-key>',
  coralogixDomain: 'EU2',
  version: '1.0.0',
  runOutsideAngularZone: true,
  sessionRecordingConfig: {
    enable: true,
    autoStartSessionRecording: true,
    recordConsoleEvents: true,
    sessionRecordingSampleRate: 100,
  },
  traceParentInHeader: {
    enabled: true,
  },
  sessionConfig: {
    sessionSampleRate: 100,
  },
});

bootstrapApplication(AppComponent, appConfig).catch(console.error);
