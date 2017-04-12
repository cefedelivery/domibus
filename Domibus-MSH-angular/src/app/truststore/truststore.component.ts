import {Component, OnInit} from '@angular/core';
import {FileUploader, FileUploaderOptions} from "ng2-file-upload";
import {Http} from "@angular/http";
import {AlertService} from "app/alert/alert.service";
import {Observable} from 'rxjs/Rx';

// Import RxJs required methods
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';

@Component({
  selector: 'app-truststore',
  templateUrl: './truststore.component.html',
  styleUrls: ['./truststore.component.css']
})
export class TruststoreComponent implements OnInit {

  public options: FileUploaderOptions = {
    url: "/rest/truststore",
    itemAlias: "file"
  };
  public uploader: FileUploader = new FileUploader(this.options);

  private password: String;
  private url = "/rest/truststore";

  constructor(private http: Http, private alertService: AlertService) {
  }

  ngOnInit() {
  }

  submitTruststore($event): Observable<any> {
    this.alertService.success("Plm", true);
    var files = $event.srcElement.files;
    return this.http.post(this.url, {
      password:this.password,
      truststore:files
    }, null);
  }

}
