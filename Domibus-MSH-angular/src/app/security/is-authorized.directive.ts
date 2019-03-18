import {Directive, ElementRef, Input} from '@angular/core';
import {SecurityService} from './security.service';

@Directive({
    selector:'[isAuthorized]',
    providers:[]
})
export class IsAuthorized {
    @Input('isAuthorized') role:string;
    constructor(private _elementRef:ElementRef, private securityService:SecurityService) {

    }
    ngOnInit():void {
      if(this.role && this.role.trim() !== '') {
        const isAuthorized = this.securityService.isAuthorized([this.role]);
        if (!isAuthorized) {
          let el: HTMLElement = this._elementRef.nativeElement;
          el.parentNode.removeChild(el);
        }
      }
    }
}
