import {Component, OnInit, Input} from '@angular/core';
import {SecurityService} from '../../security/security.service';
import {DomainService} from '../../security/domain.service';
import {Domain} from '../../security/domain';
import {MdDialog} from '@angular/material';
import {CancelDialogComponent} from '../cancel-dialog/cancel-dialog.component';

@Component({
  selector: 'domain-selector',
  templateUrl: './domain-selector.component.html',
  styleUrls: ['./domain-selector.component.css']
})
export class DomainSelectorComponent implements OnInit {

  showDomains: boolean;
  currentDomainCode: string;
  domainCode: string;
  domains: Domain[];

  @Input()
  currentComponent: any;

  constructor (private domainService: DomainService, private securityService: SecurityService, private dialog: MdDialog) {
  }

  ngOnInit () {
    this.domainService.isMultiDomain().subscribe((isMultiDomain: boolean) => {
      if (isMultiDomain && this.securityService.isCurrentUserSuperAdmin()) {
        this.showDomains = true;
        this.domainService.getCurrentDomain().subscribe((domain: Domain) => this.domainCode = this.currentDomainCode = domain ? domain.code : null);
        this.domainService.getDomains().subscribe((domains: Domain[]) => this.domains = domains);
      }
    });
  }

  changeDomain () {
    let canChangeDomain = Promise.resolve(true);
    if (this.currentComponent && this.currentComponent.isDirty && this.currentComponent.isDirty()) {
      canChangeDomain = this.dialog.open(CancelDialogComponent).afterClosed().toPromise<boolean>();
    }

    canChangeDomain.then((canChange: boolean) => {
      if (!canChange) throw false;

      let domain = this.domains.find(d => d.code == this.domainCode);
      this.domainService.setCurrentDomain(domain).then(() => {
        if (this.currentComponent.ngOnInit)
          this.currentComponent.ngOnInit();
      });

    }).catch(() => { // domain not changed -> reset the combo value
      this.domainCode = this.currentDomainCode;
    });
  }
}
