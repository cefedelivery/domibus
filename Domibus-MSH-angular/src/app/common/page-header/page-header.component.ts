import {Component, EventEmitter, Input, OnInit} from "@angular/core";
import {DomainService} from "../../security/domain.service";
import {Domain} from "../../security/domain";

@Component({
  selector: 'page-header',
  templateUrl: './page-header.component.html',
  styleUrls: ['./page-header.component.css']
})
export class PageHeaderComponent implements OnInit {

  isMultiDomain: boolean;
  currentDomain: string;

  constructor(private domainService : DomainService) {
  }

  ngOnInit() {
    this.domainService.isMultiDomain().subscribe((isMultiDomain: boolean) => {
      this.isMultiDomain = isMultiDomain;
      if (isMultiDomain) {
        this.domainService.getCurrentDomain().subscribe((domain: Domain) => this.currentDomain = domain ? domain.name : '');
      }
    });
  }

}
