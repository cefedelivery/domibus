import {Component, OnInit} from '@angular/core';
import {Http, Headers, Response} from "@angular/http";
import {AlertService} from "../alert/alert.service";

@Component({
  selector: 'app-jms',
  templateUrl: './jms.component.html',
  styleUrls: ['./jms.component.css']
})
export class JmsComponent implements OnInit {

  dateFormat: String = 'yyyy-MM-dd HH:mm:ssZ';

  timestampFromMaxDate: Date = new Date();
  timestampToMinDate: Date = null;

  private queues = [];

  private selectedSource: any;
  private fromDateTime: string;
  private toDateTime: string;

  private selected = [];
  private loading: boolean = false;

  rows = [];
  private pageSizes: Array<any> = [
    {key: '5', value: 5},
    {key: '10', value: 10},
    {key: '25', value: 25},
    {key: '50', value: 50},
    {key: '100', value: 100}
  ];
  private pageSize: number = this.pageSizes[0].value;

  constructor(private http: Http, private alertService: AlertService) {
  }

  ngOnInit() {
    this.http.get("rest/jms/destinations").subscribe(
      (response: Response) => {
        this.queues = [];
        let destinations = response.json().jmsDestinations;
        for (let key in destinations) {
          this.queues.push(destinations[key])
          if(key.match('domibus\.DLQ')){
            this.selectedSource = destinations[key];
          }
        }

        // console.log(this.queues);
      },
      (error: Response) => {
        this.alertService.error('Could not load queues: ' + error);
      }
    )
  }

  changePageSize(newPageSize: number) {
    this.pageSize = newPageSize;
    this.search();
  }

  onSelect({selected}) {
    // console.log('Select Event', selected, this.selected);
    this.selected.splice(0, this.selected.length);
    this.selected.push(...selected);
  }

  onActivate(event) {
    // console.log('Activate Event', event);
  }

  onTimestampFromChange(event) {
    this.timestampToMinDate = event.value;
  }

  onTimestampToChange(event) {
    this.timestampFromMaxDate = event.value;
  }

  search() {
    let headers = new Headers({'Content-Type': 'application/json'});
    this.loading = true;
    this.selected = [];
    this.http.post("rest/jms/messages", {"source": "domibus.notification.webservice"}, {headers: headers}).subscribe(
      (response: Response) => {
        let messages = response.json().messages;
        this.rows = messages;
        this.loading = false;
        // console.log(messages);
      },
      error => {
        this.alertService.error('Could not load messages: ' + error);
        this.loading = false;
      }
    )
  }

  cancel() {

  }

  save() {

  }

  move() {

  }

  delete() {

  }

}
