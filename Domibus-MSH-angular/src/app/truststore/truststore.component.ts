import {Component, OnInit, ViewChild} from "@angular/core";
import {Http} from "@angular/http";
import {AlertService} from "app/alert/alert.service";
// Import RxJs required methods
import "rxjs/add/operator/map";
import "rxjs/add/operator/catch";
import {TrustStoreService} from "./trustore.service";
import {TrustStoreEntry} from "./trustore.model";

@Component({
  selector: 'app-truststore',
  templateUrl: './truststore.component.html',
  styleUrls: ['./truststore.component.css'],
  providers: [TrustStoreService]
})
export class TruststoreComponent implements OnInit {

  private url = "rest/truststore";
  trustStoreEntries: Array<TrustStoreEntry> = [];
  selectedMessages: Array<any> = [];
  loading: boolean = false;

  rows: Array<any> = [];
  pageSizes: Array<any> = [
    {key: '10', value: 10},
    {key: '25', value: 25},
    {key: '50', value: 50},
    {key: '100', value: 100}
  ];
  pageSize: number = this.pageSizes[0].value;


  @ViewChild('fileInput')
  private fileInput;

  @ViewChild('password')
  private password;

  constructor(private http: Http, private alertService: AlertService, private trustStoreService: TrustStoreService) {
  }

  ngOnInit(): void {
    this.getTrustStoreEntries();
  }

  getTrustStoreEntries(): void {
    this.trustStoreService.getEntries().subscribe(trustStoreEntries => this.trustStoreEntries = trustStoreEntries);
  }

  public submit() {
    let fi = this.fileInput.nativeElement;
    console.log(this.password.nativeElement);
    let input = new FormData();
    input.append('truststore', fi.files[0]);
    input.append('password', this.password.nativeElement.value);
    this.http.post(this.url, input).subscribe(res => {
        this.alertService.success(res.text(), false);
      },
      err => {
        this.alertService.error("Error updating truststore file", false);
      }
    );


  }

  onSelect({selected}) {
    console.log('Select Event');
    this.selectedMessages.splice(0, this.selectedMessages.length);
    this.selectedMessages.push(...selected);
  }

  onActivate(event) {
    console.log('Activate Event', event);

    if ("dblclick" === event.type) {
      //this.details(event.row);
    }
  }

}
