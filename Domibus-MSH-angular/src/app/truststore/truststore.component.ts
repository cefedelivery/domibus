import {Component, OnInit, ViewChild} from '@angular/core';
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

  private url = "rest/truststore";


  @ViewChild('fileInput')
  private fileInput;

  @ViewChild('password')
  private password;

  constructor(private http: Http, private alertService: AlertService) {
  }

  ngOnInit() {
  }

  public submit() {
    let fi = this.fileInput.nativeElement;
    console.log(this.password.nativeElement);
    let input = new FormData();
    input.append('truststore', fi.files[0]);
    input.append('password', this.password.nativeElement.value);
    this.http.post(this.url, input).subscribe(res => {
        this.alertService.success(res.json(), false);
      },
      err => {
        this.alertService.error(err.json(), false);
      }
    );


  }

}
