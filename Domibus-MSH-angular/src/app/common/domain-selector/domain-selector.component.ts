import {Component, OnInit, Input} from '@angular/core';
import {SecurityService} from '../../security/security.service';
import {DomainService} from '../../security/domain.service';
import {Domain} from '../../security/domain';
import {MdDialog} from '@angular/material';
import {CancelDialogComponent} from '../cancel-dialog/cancel-dialog.component';
import {Title} from '@angular/platform-browser';

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

  constructor (private domainService: DomainService,
               private securityService: SecurityService,
               private dialog: MdDialog) {
  }

  async ngOnInit () {
    const isMultiDomain = await this.domainService.isMultiDomain().first().toPromise();

    if (isMultiDomain && this.securityService.isCurrentUserSuperAdmin()) {
      this.showDomains = true;
      const domain = await this.domainService.getCurrentDomain().first().toPromise();
      this.domainCode = this.currentDomainCode = domain ? domain.code : null;
      const domains = await this.domainService.getDomains().toPromise();
      this.domains = domains;
    }
  }

  async changeDomain () {
    let canChangeDomain = Promise.resolve(true);
    if (this.currentComponent && this.currentComponent.isDirty && this.currentComponent.isDirty()) {
      canChangeDomain = this.dialog.open(CancelDialogComponent).afterClosed().toPromise<boolean>();
    }

    try {
      const canChange = await canChangeDomain;
      if (!canChange) throw false;

      const domain = this.domains.find(d => d.code == this.domainCode);
      await this.domainService.setCurrentDomain(domain);

      this.domainService.setAppTitle();

      if (this.currentComponent.ngOnInit)
        this.currentComponent.ngOnInit();

    } catch (ex) { // domain not changed -> reset the combo value
      this.domainCode = this.currentDomainCode;
    }
  }

}

