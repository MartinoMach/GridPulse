import { bootstrapApplication } from '@angular/platform-browser';
import { provideHttpClient } from '@angular/common/http';
import { GridPulseComponent } from './app/grid-pulse/grid-pulse.component';

bootstrapApplication(GridPulseComponent, {
  providers: [provideHttpClient()]
}).catch((error) => console.error(error));
