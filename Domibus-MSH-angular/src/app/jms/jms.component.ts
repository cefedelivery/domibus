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

  private selectedSource: any;
  private queues = [];
  private fromDateTime: string;
  private toDateTime: string;

  private selected: string;
  private loading: boolean = false;

  rows = [
    {name: 'Austin', gender: 'Male', company: 'Swimlane'},
    {name: 'Dany', gender: 'Male', company: 'KFC'},
    {name: 'Molly', gender: 'Female', company: 'Burger King'},
  ];
  // columns = [
  //   {name: 'Name 1', prop: 'name'},
  //   {name: 'Gender 1', prop: 'gender'},
  //   {name: 'Company 1', prop: 'company'}
  // ];
  private count: number = 3;
  private offset: number = 1;
  private pageSize: number = 10;

  constructor(private http: Http, private alertService: AlertService) {
  }

  ngOnInit() {
    this.http.get("rest/jms/destinations").subscribe(
      (response: Response) => {
        this.queues = [];
        let destinations = response.json().jmsDestinations;
        for (let key in destinations) {
          this.queues.push(destinations[key])
        }

        console.log(this.queues);
      },
      (error: Response) => {
        this.alertService.error('Could not load queues: ' + error);
      }
    )
  }

  page() {
    let headers = new Headers({'Content-Type': 'application/json'});
    this.http.post("rest/jms/messages", {"source": "domibus.notification.webservice"}, {headers: headers}).subscribe(
      (response: Response) => {
        let messages = response.json().messages;
        this.rows = messages;
        this.count= this.rows.length;
        console.log(messages);
      },
      error => {
        this.alertService.error('Could not load messages: ' + error);
      }
    )
  }


  onSelect({selected}) {
    console.log('Select Event', selected);
  }

  onActivate(event) {
    console.log('Activate Event', event);
  }

  onTimestampFromChange(event) {
    this.timestampToMinDate = event.value;
  }

  onTimestampToChange(event) {
    this.timestampFromMaxDate = event.value;
  }

  apply() {
    this.page();
  }

  onPage(event) {
    console.log('Page Event', event);
    // this.page(event.offset, event.pageSize, this.orderBy, this.asc);
  }

  onSort(event) {
    console.log('Sort Event', event);
    // let ascending = true;
    // if(event.newValue === 'desc') {
    //   ascending = false;
    // }
    // this.page(this.offset, this.pageSize, event.column.prop, ascending);
  }


}
