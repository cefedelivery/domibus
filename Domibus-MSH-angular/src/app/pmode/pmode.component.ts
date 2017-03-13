import {Component} from "@angular/core";
import {Http} from "@angular/http";
import {AlertService} from "../alert/alert.service";

@Component({
  moduleId: module.id,
  templateUrl: 'pmode.component.html',
  providers: [],
  styleUrls: ['./pmode.component.css']
})

export class PModeComponent {

  constructor(private http: Http, private alertService: AlertService) {
  }

  ngOnInit() {
  }

}
