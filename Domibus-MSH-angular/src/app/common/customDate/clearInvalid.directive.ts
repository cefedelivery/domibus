import {Directive, ElementRef, OnInit, OnDestroy} from '@angular/core';
import {Md2Datepicker} from 'md2';

@Directive({
  selector: '[appClearInvalid]'
})
export class ClearInvalidDirective implements OnInit, OnDestroy {
  private input: any;

  constructor(private el: ElementRef, private host: Md2Datepicker) {
  }

  onBlurHandler(event) {
    if (!this.host.value) {
      this.input.value = '';
    }
  }

  ngOnInit() {
    const inputList = this.el.nativeElement.getElementsByTagName('input');
    if (inputList.length !== 1) {
      return;
    }

    this.input = inputList[0];
    this.input.addEventListener('blur', this.onBlurHandler.bind(this));
  }

  ngOnDestroy() {
    if (this.input) {
      this.input.removeEventListener('blur', this.onBlurHandler);
    }
  }
}
