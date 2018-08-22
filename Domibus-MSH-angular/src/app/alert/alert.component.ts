import {Component} from '@angular/core';
import {AlertService} from './alert.service';

@Component({
  moduleId: module.id,
  selector: 'alert',
  templateUrl: 'alert.component.html',
  styleUrls: ['./alert.component.css']
})

export class AlertComponent {
  public static readonly MAX_COUNT_CSV: number = 5;
  public static readonly CSV_ERROR_MESSAGE = 'Maximum number of rows reached for downloading CSV';

  message: any;

  constructor (private alertService: AlertService) {
  }

  ngOnInit () {
    this.alertService.getMessage().subscribe(message => {
      this.message = message;
    });
  }

  clearAlert (): void {
    this.alertService.clearAlert();
  }
}
