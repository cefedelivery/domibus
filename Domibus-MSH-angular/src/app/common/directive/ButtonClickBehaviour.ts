import {Directive, HostListener} from '@angular/core';
import {AlertService} from '../alert/alert.service';

@Directive({
  selector: '[button-click-behaviour]'
})
export class ButtonClickBehaviourDirective {
  constructor(private alertService: AlertService) {

  }

  @HostListener('click', ['$event'])
  public onClick(event: any): void {
    this.alertService.clearAlert();
  }
}
