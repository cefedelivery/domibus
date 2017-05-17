import {Component, OnInit} from '@angular/core';
import {Http, Headers, Response} from "@angular/http";
import {AlertService} from "../alert/alert.service";
import {MessagesRequestRO} from "./ro/messages-request-ro";

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

  private _selectedSource: any;
  get selectedSource(): any {
    return this._selectedSource;
  }

  set selectedSource(value: any) {
    this._selectedSource = value;
    this.request.source = value.name;
  }

  private selectedMessages: Array<any> = [];
  private loading: boolean = false;

  private rows: Array<any> = [];
  private pageSizes: Array<any> = [
    {key: '5', value: 5},
    {key: '10', value: 10},
    {key: '25', value: 25},
    {key: '50', value: 50},
    {key: '100', value: 100}
  ];
  private pageSize: number = this.pageSizes[0].value;

  private request = new MessagesRequestRO()

  constructor(private http: Http, private alertService: AlertService) {
  }

  ngOnInit() {
    this.http.get("rest/jms/destinations").subscribe(
      (response: Response) => {
        this.queues = [];
        let destinations = response.json().jmsDestinations;
        for (let key in destinations) {
          this.queues.push(destinations[key])
          if (key.match('domibus\.DLQ')) {
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
    // console.log('Select Event', selectedMessages, this.selectedMessages);
    this.selectedMessages.splice(0, this.selectedMessages.length);
    this.selectedMessages.push(...selected);
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
    this.selectedMessages = [];
    this.http.post("rest/jms/messages", JSON.stringify(this.request), {headers: headers}).subscribe(
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
