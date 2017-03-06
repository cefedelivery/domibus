import {Component, OnInit} from "@angular/core";
import {DomibusInfoService} from "../appinfo/domibusinfo.service";
import {DomibusInfo} from "../appinfo/domibusinfo";

@Component({
  moduleId: module.id,
  templateUrl: 'footer.component.html',
  selector: 'footer',
  providers: [],
  styleUrls: ['./footer.component.css']
})

export class FooterComponent implements OnInit {
  domibusVersion: string;

  constructor(private domibusInfoService: DomibusInfoService) {
  }

  ngOnInit(): void {
    console.log("FooterComponent onInit");
    this.domibusInfoService.getDomibusInfo().subscribe((domibusInfo: DomibusInfo) => {
      this.domibusVersion = domibusInfo.version;
      }
    );
  }


}
