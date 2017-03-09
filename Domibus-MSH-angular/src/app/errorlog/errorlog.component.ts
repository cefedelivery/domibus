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
  orderBy: string;
  asc: boolean;

  ROW_LIMITS = [
    {key: '10', value: 10},
    {key: '25', value: 25},
    {key: '50', value: 50},
    {key: '100', value: 100}
  ];
  rowLimits: Array<any> = this.ROW_LIMITS;
  pageSize: number = this.ROW_LIMITS[0].value;

  mshRoles: Array<String>;
  errorCodes: Array<String>;

  constructor(private http: Http) {
  }

  ngOnInit() {
    this.page(this.offset, this.pageSize, this.offset, this.asc);
  }

  getErrorLogEntries(offset: number, pageSize: number, orderBy: string, asc: boolean): Observable<ErrorLogResult> {
    let params: URLSearchParams = new URLSearchParams();
    params.set('page', offset.toString());
    params.set('pageSize', pageSize.toString());
    params.set('orderBy', orderBy);
    params.set('asc', asc.toString());

    return this.http.get('rest/errorlogs', {
      search: params
    }).map((response: Response) =>
      response.json()
     );
  }

  page(offset, pageSize, orderBy, asc) {
    let start = offset * pageSize;
    let end = start + pageSize;
    console.log("offset:" + offset + ";pageSize:" + pageSize + ";start:" + start + ";end:" + end);
    this.loading = true;

    this.getErrorLogEntries(offset, pageSize, orderBy, asc).subscribe((result: ErrorLogResult) => {
      console.log("errorLog response:" + result);
      this.offset = offset;
      this.pageSize = pageSize;
      this.orderBy = orderBy;
      this.asc = asc;

      this.rows = result.errorLogEntries;
      this.count = result.count;

      this.filter = result.filter;

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
    this.page(event.offset, event.pageSize, this.orderBy, this.asc);
  }

  onSort(event) {
    console.log('Sort Event', event);
    //TODO pass other properties
    this.page(this.offset, this.pageSize, "TODO_PASS_COLUMN", "TODO_PASS_ORDER");
  }

  changeRowLimits(event) {
    let newPageLimit = event.value;
    console.log('New page limit:', newPageLimit);
    this.page(this.offset, newPageLimit, this.orderBy, this.asc);
  }

  search() {
    console.log("Searching...");

    //TODO get the filter and perform a search
  }

}
