import {Component, OnInit} from "@angular/core";
import {SecurityService} from "../../security/security.service";
import {DomainService} from "../../security/domain.service";
import {Domain} from "../../security/domain";

@Component({
  selector: 'domain-selector',
  templateUrl: './domain-selector.component.html',
  styleUrls: ['./domain-selector.component.css']
})
export class DomainSelectorComponent implements OnInit {

  showDomains: boolean;
  currentDomainCode: string;
  domains: Domain[];

  constructor(private domainService : DomainService, private securityService: SecurityService) {
  }

  ngOnInit() {
    this.domainService.isMultiDomain().subscribe((isMultiDomain: boolean) => {
      if (isMultiDomain && this.securityService.isCurrentUserSuperAdmin()) {
        this.showDomains = true;
        this.domainService.getCurrentDomain().subscribe((domain: Domain) => this.currentDomainCode = domain ? domain.code : null);
        this.domainService.getDomains().subscribe((domains: Domain[]) => this.domains = domains);
      }
    });
  }

  changeDomain() {
    let domain = this.domains.find(d => d.code == this.currentDomainCode);
    this.domainService.setCurrentDomain(domain);
  }
}
