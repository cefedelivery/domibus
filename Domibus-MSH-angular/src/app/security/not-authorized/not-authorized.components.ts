import {Component, OnInit} from "@angular/core";
import {DomibusInfoService} from "../../common/appinfo/domibusinfo.service";

@Component({
  moduleId: module.id,
  templateUrl: 'not-authorized.component.html'
})

/**
 * @author Catalin Enache
 * @since 4.1
 */
export class NotAuthorizedComponent implements OnInit {
  supportTeamInfoName: string;
  supportTeamInfoEmail: string;

  constructor(private domibusInfoService: DomibusInfoService) {
  }

  async ngOnInit() {
    const supportTeamInfo = await this.domibusInfoService.getSupportTeamInfo();
    this.supportTeamInfoName = supportTeamInfo.name;
    this.supportTeamInfoEmail = supportTeamInfo.email;
  }

}
