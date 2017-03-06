import {Component} from "@angular/core";
import {Observable} from "rxjs";
import {Http, Response, URLSearchParams} from "@angular/http";
import {ErrorLogResult} from "./errorlogresult";

@Component({
  moduleId: module.id,
  templateUrl: 'errorlog.component.html',
  providers: [],
  styleUrls: ['./errorlog.component.css']
})

export class ErrorLogComponent {
  dateFormat: String = 'yyyy-MM-dd HH:mm:ssZ';

  filter: any = {};
  loading: boolean = false;
  rows = [];
  count: number = 0;
  offset: number = 0;

  ROW_LIMITS = [
    {key: '10', value: 10},
    {key: '25', value: 25},
    {key: '50', value: 50},
    {key: '100', value: 100}
  ];
  rowLimits: Array<any> = this.ROW_LIMITS;
  limit: number = this.ROW_LIMITS[0].value;

  mshRoles: Array<String>;
  errorCodes: Array<String>;

  constructor(private http: Http) {
  }

  ngOnInit() {
    this.page(this.offset, this.limit);
  }

  getErrorLogEntries(offset: number, limit: number): Observable<ErrorLogResult> {
    let params: URLSearchParams = new URLSearchParams();
    params.set('offset', offset.toString());
    params.set('limit', limit.toString());

    return this.http.get('api/errorlog', {
      search: params
    }).map((response: Response) =>
      response.json()
     );
  }

  page(offset, limit) {
    let start = offset * limit;
    let end = start + limit;
    console.log("offset:" + offset + ";limit:" + limit + ";start:" + start + ";end:" + end);
    this.loading = true;

    this.getErrorLogEntries(offset, limit).subscribe((result: ErrorLogResult) => {
      console.log("errorLog response:" + result);
      this.offset = offset;

      this.rows = result.errorLogEntries;
      this.count = result.count;

      this.mshRoles = result.mshRoles;
      this.errorCodes = result.errorCodes;

      this.loading = false;
    }, (error: any) => {
      console.log("error getting the error log:" + error);
      this.loading = false;
    });

  }

  onPage(event) {
    console.log('Page Event', event);
    this.page(event.offset, event.limit);
  }

  onSort(event) {
    console.log('Sort Event', event);
    //TODO pass other properties
    this.page(this.offset, this.limit);
  }

  changeRowLimits(event) {
    let newPageLimit = event.value;
    this.limit = newPageLimit;
    console.log('New page limit:', newPageLimit);
    this.page(this.offset, newPageLimit);
  }

  search() {
    console.log("Searching...");
  }

}
