import { Injectable } from '@angular/core';
import {CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, CanDeactivate} from '@angular/router';
import { Observable } from 'rxjs/Observable';
import {MdDialog} from "@angular/material";
import {CancelDialogComponent} from "./cancel-dialog/cancel-dialog.component";

@Injectable()
export class DirtyGuard implements CanActivate, CanDeactivate<any> {

  constructor(public dialog: MdDialog) {

  };

  canActivate(next: ActivatedRouteSnapshot,
              state: RouterStateSnapshot): Observable<boolean> | Promise<boolean> | boolean {
    return true;
  }

  canDeactivate(component: any, currentRoute: ActivatedRouteSnapshot, currentState: RouterStateSnapshot, nextState?: RouterStateSnapshot): Observable<boolean> | Promise<boolean> | boolean {
    if (component.isDirty && !component.isDirty()) return true;
    return this.dialog.open(CancelDialogComponent).afterClosed();
  }
}
